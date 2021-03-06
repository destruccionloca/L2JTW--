/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.model.entity;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.clientpackets.Say2;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

/**
 * @author FBIagent
 */
public class TvTEvent {
	enum EventState {
		INACTIVE,
		INACTIVATING,
		PARTICIPATING,
		STARTING,
		STARTED,
		REWARDING
	}

	protected static final Logger _log = Logger.getLogger( TvTEvent.class.getName() );
	/**	The teams of the TvTEvent<br> */
	private static TvTEventTeam[] _teams = new TvTEventTeam[ 2 ];
	/** The state of the TvTEvent<br> */
	private static EventState _state = EventState.INACTIVE;
	/** The spawn of the participation npc<br> */
	private static L2Spawn _npcSpawn = null;
	/** the npc instance of the participation npc<br> */
	private static L2NpcInstance _lastNpcSpawn = null;

	/**
	 * No instance of this class!<br>
	 */
	private TvTEvent() {
	}

	/**
	 * Teams initializing<br>
	 */
	public static void init() {
		_teams[ 0 ] = new TvTEventTeam( Config.TVT_EVENT_TEAM_1_NAME, Config.TVT_EVENT_TEAM_1_COORDINATES );
		_teams[ 1 ] = new TvTEventTeam( Config.TVT_EVENT_TEAM_2_NAME, Config.TVT_EVENT_TEAM_2_COORDINATES );
	}

	/**
	 * Starts the participation of the TvTEvent<br>
	 * 1. Get L2NpcTemplate by Config.TVT_EVENT_PARTICIPATION_NPC_ID<br>
	 * 2. Try to spawn a new npc of it<br><br>
	 *
	 * @return boolean: true if success, otherwise false<br>
	 */
	public static boolean startParticipation() {
		L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate( Config.TVT_EVENT_PARTICIPATION_NPC_ID );

		if ( tmpl == null ) {
			_log.warning("TvTEventEngine[TvTEvent.startParticipation()]: L2NpcTemplate is a NullPointer -> Invalid npc id in configs?");
			return false;
		}

        try {
            _npcSpawn = new L2Spawn( tmpl );

            _npcSpawn.setLocx( Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[ 0 ] );
            _npcSpawn.setLocy( Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[ 1 ] );
            _npcSpawn.setLocz( Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[ 2 ] );
            _npcSpawn.setAmount( 1 );
            _npcSpawn.setHeading( 0 );
            _npcSpawn.setRespawnDelay( 1 );
            // later no need to delete spawn from db, we don't store it (false)
            SpawnTable.getInstance().addNewSpawn( _npcSpawn, false );
            _npcSpawn.init();
            _lastNpcSpawn = _npcSpawn.getLastSpawn();
            _lastNpcSpawn.setCurrentHp( _lastNpcSpawn.getMaxHp() );
            _lastNpcSpawn.setTitle( "TvT Event Participation" );
            _lastNpcSpawn.isAggressive();
            _lastNpcSpawn.decayMe();
            _lastNpcSpawn.spawnMe( _npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ() );
            _lastNpcSpawn.broadcastPacket( new MagicSkillUse( _lastNpcSpawn, _lastNpcSpawn, 1034, 1, 1, 1 ) );
        } catch ( Exception e ) {
            _log.warning( "TvTEventEngine[TvTEvent.startParticipation()]: exception: " + e );
            return false;
        }

		setState( EventState.PARTICIPATING );
		return true;
	}

	/**
	 * Starts the TvTEvent fight<br>
	 * 1. Set state EventState.STARTING<br>
	 * 2. Close doors specified in configs<br>
	 * 3. Abort if not enought participants(return false)<br>
	 * 4. Set state EventState.STARTED<br>
	 * 5. Teleport all participants to team spot<br><br>
	 *
	 * @return boolean: true if success, otherwise false<br>
	 */
	public static boolean startFight() {
		// Set state to STARTING
		setState( EventState.STARTING );

		// Check for enought participants
		if (	_teams[ 0 ].getParticipatedPlayerCount() < Config.TVT_EVENT_MIN_PLAYERS_IN_TEAMS ||
				_teams[ 1 ].getParticipatedPlayerCount() < Config.TVT_EVENT_MIN_PLAYERS_IN_TEAMS ) {
			// Set state INACTIVE
			setState( EventState.INACTIVE );
			// Cleanup of teams
			_teams[ 0 ].cleanMe();
			_teams[ 1 ].cleanMe();
			// Unspawn the event NPC
			unSpawnNpc();
			return false;
		}

		// Opens all doors specified in configs for tvt
		openDoors();
		// Closes all doors specified in configs for tvt
		closeDoors();
		// Set state STARTED
		setState( EventState.STARTED );

		// Iterate over all teams
		for ( TvTEventTeam team : _teams ) {
			// Iterate over all participated player instances in this team
			for ( L2PcInstance playerInstance : team.getParticipatedPlayers().values() ) {
				if ( playerInstance != null ) {
					// Teleporter implements Runnable and starts itself
					new TvTEventTeleporter( playerInstance, team.getCoordinates(), false, false );
				}
			}
		}

		return true;
	}

	/**
	 * Calculates the TvTEvent reward<br>
	 * 1. If both teams are at a tie(points equals), send it as system message to all participants, if one of the teams have 0 participants left online abort rewarding<br>
	 * 2. Wait till teams are not at a tie anymore<br>
	 * 3. Set state EvcentState.REWARDING<br>
	 * 4. Reward team with more points<br>
	 * 5. Show win html to wining team participants<br><br>
	 *
	 * @return String: winning team name<br>
	 */
	public static String calculateRewards()
	{
		if (_teams[ 0 ].getPoints() == _teams[ 1 ].getPoints())
		{
			// Check if one of the teams have no more players left
			if ( _teams[ 0 ].getParticipatedPlayerCount() == 0 || _teams[ 1 ].getParticipatedPlayerCount() == 0 )
			{
				// set state to rewarding
				setState(EventState.REWARDING);
				// return here, the fight can't be completed
				return "TvT Event: Event has ended. No team won due to inactivity!";
			}

			// Both teams have equals points
			sysMsgToAllParticipants("TvT Event: Event has ended, both teams have tied.");
			if (Config.TVT_REWARD_TEAM_TIE)
			{
				rewardTeamOne();
				rewardTeamTwo();
				return "TvT Event: Event has ended with both teams tying.";
			}
			else 
				return "TvT Event: Event has ended with both teams tying.";
		}

		// Set state REWARDING so nobody can point anymore
		setState( EventState.REWARDING );

		// Get team which has more points
		TvTEventTeam team = _teams[ _teams[ 0 ].getPoints() > _teams[ 1 ].getPoints() ? 0 : 1 ];
		
		if (team == _teams[0])
			rewardTeamOne();
		else
			rewardTeamTwo();
			
		return "TvT Event: Event finish. Team " + team.getName() + " won with " + team.getPoints() + " kills.";
	}
	
	private static void rewardTeamOne()
	{
		TvTEventTeam team = _teams[0];
		// Iterate over all participated player instances of the winning team
		for ( L2PcInstance playerInstance : team.getParticipatedPlayers().values() ) {
			// Check for nullpointer
			if ( playerInstance == null )
			{
				continue;
			}

			SystemMessage systemMessage = null;

			// Iterate over all tvt event rewards
			for ( int[] reward : Config.TVT_EVENT_REWARDS ) {
				PcInventory inv = playerInstance.getInventory();

				// Check for stackable item, non stackabe items need to be added one by one
				if ( ItemTable.getInstance().createDummyItem( reward[ 0 ] ).isStackable() ) {
					inv.addItem( "TvT Event", reward[ 0 ], reward[ 1 ], playerInstance, playerInstance );

					if ( reward[ 1 ] > 1 ) {
						systemMessage = new SystemMessage( SystemMessageId.EARNED_S2_S1_S );
						systemMessage.addItemName( reward[ 0 ] );
						systemMessage.addNumber( reward[ 1 ] );
					} else {
						systemMessage = new SystemMessage( SystemMessageId.EARNED_ITEM );
						systemMessage.addItemName( reward[ 0 ] );
					}

					playerInstance.sendPacket( systemMessage );
				} else {
					for ( int i = 0;i < reward[ 1 ];++ i ) {
						inv.addItem( "TvT Event", reward[ 0 ], 1, playerInstance, playerInstance );
						systemMessage = new SystemMessage( SystemMessageId.EARNED_ITEM );
						systemMessage.addItemName( reward[ 0 ] );
						playerInstance.sendPacket( systemMessage );
					}
				}
			}

			StatusUpdate statusUpdate = new StatusUpdate( playerInstance.getObjectId() );
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage( 0 );

			statusUpdate.addAttribute( StatusUpdate.CUR_LOAD, playerInstance.getCurrentLoad() );
			npcHtmlMessage.setHtml( "<html><head><title>TvT Event</title></head><body>Your team won the event. Look in your inventory, there should be your reward.</body></html>" );
			playerInstance.sendPacket( statusUpdate );
			playerInstance.sendPacket( npcHtmlMessage );
		}
	}
	
	private static void rewardTeamTwo()
	{
		TvTEventTeam team = _teams[1];
		
		// Iterate over all participated player instances of the winning team
		for ( L2PcInstance playerInstance : team.getParticipatedPlayers().values() ) {
			// Check for nullpointer
			if ( playerInstance == null ) {
				continue;
			}

			SystemMessage systemMessage = null;

			// Iterate over all tvt event rewards
			for ( int[] reward : Config.TVT_EVENT_REWARDS ) {
				PcInventory inv = playerInstance.getInventory();

				// Check for stackable item, non stackabe items need to be added one by one
				if ( ItemTable.getInstance().createDummyItem( reward[ 0 ] ).isStackable() ) {
					inv.addItem( "TvT Event", reward[ 0 ], reward[ 1 ], playerInstance, playerInstance );

					if ( reward[ 1 ] > 1 ) {
						systemMessage = new SystemMessage( SystemMessageId.EARNED_S2_S1_S );
						systemMessage.addItemName( reward[ 0 ] );
						systemMessage.addNumber( reward[ 1 ] );
					} else {
						systemMessage = new SystemMessage( SystemMessageId.EARNED_ITEM );
						systemMessage.addItemName( reward[ 0 ] );
					}

					playerInstance.sendPacket( systemMessage );
				} else {
					for ( int i = 0;i < reward[ 1 ];++ i ) {
						inv.addItem( "TvT Event", reward[ 0 ], 1, playerInstance, playerInstance );
						systemMessage = new SystemMessage( SystemMessageId.EARNED_ITEM );
						systemMessage.addItemName( reward[ 0 ] );
						playerInstance.sendPacket( systemMessage );
					}
				}
			}

			StatusUpdate statusUpdate = new StatusUpdate( playerInstance.getObjectId() );
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage( 0 );

			statusUpdate.addAttribute( StatusUpdate.CUR_LOAD, playerInstance.getCurrentLoad() );
			npcHtmlMessage.setHtml( "<html><head><title>TvT Event</title></head><body>Your team won the event. Look in your inventory, there should be your reward.</body></html>" );
			playerInstance.sendPacket( statusUpdate );
			playerInstance.sendPacket( npcHtmlMessage );
		}
	}

	/**
	 * Stops the TvTEvent fight<br>
	 * 1. Set state EventState.INACTIVATING<br>
	 * 2. Remove tvt npc from world<br>
	 * 3. Open doors specified in configs<br>
	 * 4. Teleport all participants back to participation npc location<br>
	 * 5. Teams cleaning<br>
	 * 6. Set state EventState.INACTIVE<br>
	 */
	public static void stopFight()
	{
		// Set state INACTIVATING
		setState(EventState.INACTIVATING);
		//Unspawn event npc
		unSpawnNpc();
		// Opens all doors specified in configs for tvt
		openDoors();
		// Closes all doors specified in Configs for tvt
		closeDoors();

		// Iterate over all teams
		for ( TvTEventTeam team : _teams ) {
			for ( L2PcInstance playerInstance : team.getParticipatedPlayers().values() ) {
				// Check for nullpointer
				if ( playerInstance != null ) {
					new TvTEventTeleporter( playerInstance, Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES, false, false );
				}
			}
		}

		// Cleanup of teams
		_teams[ 0 ].cleanMe();
		_teams[ 1 ].cleanMe();
		// Set state INACTIVE
		setState( EventState.INACTIVE );
	}

	/**
	 * Adds a player to a TvTEvent team<br>
	 * 1. Calculate the id of the team in which the player should be added<br>
	 * 2. Add the player to the calculated team<br><br>
	 *
	 * @param playerInstance as L2PcInstance<br>
	 * @return boolean: true if success, otherwise false<br>
	 */
	public static synchronized boolean addParticipant( L2PcInstance playerInstance ) {
		// Check for nullpoitner
		if ( playerInstance == null ) {
			return false;
		}

		byte teamId = 0;

		// Check to which team the player should be added
		if ( _teams[0].getParticipatedPlayerCount() == _teams[1].getParticipatedPlayerCount() ) {
			teamId = ( byte )( Rnd.get( 2 ) );
		} else {
			teamId = ( byte )( _teams[ 0 ].getParticipatedPlayerCount() > _teams[ 1 ].getParticipatedPlayerCount() ? 1 : 0 );
		}

		return _teams[ teamId ].addPlayer( playerInstance );
	}

	/**
	 * Removes a TvTEvent player from it's team<br>
	 * 1. Get team id of the player<br>
	 * 2. Remove player from it's team<br><br>
	 *
	 * @param playerName as String<br>
	 * @return boolean: true if success, otherwise false<br>
	 */
	public static boolean removeParticipant( int playerObjectId ) {
		// Get the teamId of the player
		byte teamId = getParticipantTeamId( playerObjectId );

		// Check if the player is participant
		if ( teamId != -1 ) {
			// Remove the player from team
			_teams[ teamId ].removePlayer( playerObjectId );
			return true;
		}

		return false;
	}

	/**
	 * Send a SystemMessage to all participated players<br>
	 * 1. Send the message to all players of team number one<br>
	 * 2. Send the message to all players of team number two<br><br>
	 *
	 * @param message as String<br>
	 */
	public static void sysMsgToAllParticipants( String message ) {
		for ( L2PcInstance playerInstance : _teams[ 0 ].getParticipatedPlayers().values() ) {
			if ( playerInstance != null ) {
				playerInstance.sendMessage( message );
			}
		}

		for ( L2PcInstance playerInstance : _teams[ 1 ].getParticipatedPlayers().values() ) {
			if ( playerInstance != null ) {
				playerInstance.sendMessage( message );
			}
		}
	}

	/**
	 * Close doors specified in configs
	 */
	private static void closeDoors() {
		for ( int doorId : Config.TVT_DOORS_IDS_TO_CLOSE ) {
			L2DoorInstance doorInstance = DoorTable.getInstance().getDoor( doorId );

			if ( doorInstance != null ) {
				doorInstance.closeMe();
			}
		}
	}

	/**
	 * Open doors specified in configs
	 */
	private static void openDoors() {
		for (int doorId : Config.TVT_DOORS_IDS_TO_OPEN ) {
			L2DoorInstance doorInstance = DoorTable.getInstance().getDoor( doorId );

			if ( doorInstance != null ) {
				doorInstance.openMe();
			}
		}
	}

	/**
	 * UnSpawns the TvTEvent npc
	 */
	private static void unSpawnNpc() {
		// Delete the npc
		_lastNpcSpawn.deleteMe();
		// Stop respawningof the npc
        _npcSpawn.stopRespawn();
        _npcSpawn = null;
		_lastNpcSpawn = null;
	}

	/**
	 * Called when a player logs in<br><br>
	 *
	 * @param playerInstance as L2PcInstance<br>
	 */
	public static void onLogin( L2PcInstance playerInstance ) {
		if (	playerInstance == null ||
				( !isStarting() && !isStarted() ) ) {
			return;
		}

		byte teamId = getParticipantTeamId( playerInstance.getObjectId() );

		if ( teamId == -1 ) {
			return;
		}

		_teams[ teamId ].addPlayer( playerInstance );
		new TvTEventTeleporter( playerInstance, _teams[ teamId ].getCoordinates(), true, false );
	}

	/**
	 * Called when a player logs out<br><br>
	 *
	 * @param playerInstance as L2PcInstance<br>
	 */
	public static void onLogout( L2PcInstance playerInstance )
	{
		if (	playerInstance != null &&
				( isStarting() || isStarted() ) ) {
			removeParticipant( playerInstance.getObjectId() );
		}
	}

	/**
	 * Called on every bypass by npc of type L2TvTEventNpc<br>
	 * Needs synchronization cause of the max player check<br><br>
	 *
	 * @param command as String<br>
	 * @param playerInstance as L2PcInstance<br>
	 */
	public static synchronized void onBypass( String command, L2PcInstance playerInstance ) {
		if ( playerInstance == null || !isParticipating() ) {
			return;
		}

		if ( command.equals( "tvt_event_participation" ) ) {
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage( 0 );
			int playerLevel = playerInstance.getLevel();

			if ( playerInstance.isCursedWeaponEquipped() ) {
				npcHtmlMessage.setHtml("<html><head><title>TvT Event</title></head><body>Cursed weapon owners are not allowed to participate.</body></html>");
			} else if ( playerInstance.getKarma() > 0 ) {
				npcHtmlMessage.setHtml("<html><head><title>TvT Event</title></head><body>Chaotic players are not allowed to participate.</body></html>");
			} else if ( playerLevel < Config.TVT_EVENT_MIN_LVL || playerLevel > Config.TVT_EVENT_MAX_LVL ) {
				npcHtmlMessage.setHtml("<html><head><title>TvT Event</title></head><body>Only players from level " + Config.TVT_EVENT_MIN_LVL + " to level " + Config.TVT_EVENT_MAX_LVL + " are allowed tro participate.</body></html>");
			} else if (	_teams[ 0 ].getParticipatedPlayerCount() == Config.TVT_EVENT_MAX_PLAYERS_IN_TEAMS &&
						_teams[ 1 ].getParticipatedPlayerCount() == Config.TVT_EVENT_MAX_PLAYERS_IN_TEAMS ) {
				npcHtmlMessage.setHtml("<html><head><title>TvT Event</title></head><body>The event is full! Only " + Config.TVT_EVENT_MAX_PLAYERS_IN_TEAMS + " players are allowed per team.</body></html>");
			} else if ( addParticipant( playerInstance ) ) {
				npcHtmlMessage.setHtml("<html><head><title>TvT Event</title></head><body>You are on the registration list now.</body></html>");
			} else {
				return;
			}

			playerInstance.sendPacket( npcHtmlMessage );
		} else if ( command.equals( "tvt_event_remove_participation" ) ) {
			removeParticipant( playerInstance.getObjectId() );

			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);

			npcHtmlMessage.setHtml( "<html><head><title>TvT Event</title></head><body>You are not longer on the registration list.</body></html>" );
			playerInstance.sendPacket( npcHtmlMessage );
		}
	}

	/**
	 * Called on every onAction in L2PcIstance<br><br>
	 *
	 * @param playerName as String<br>
	 * @param targetPlayerName as String<br>
	 * @return boolean: true if player is allowed to target, otherwise false<br>
	 */
	public static boolean onAction( L2PcInstance playerInstance, int targetedPlayerObjectId ) {
		if ( playerInstance == null || !isStarted() ) {
			return true;
		}

		if ( playerInstance.isGM() ) {
			return true;
		}

		byte playerTeamId = getParticipantTeamId( playerInstance.getObjectId() );
		byte targetedPlayerTeamId = getParticipantTeamId( targetedPlayerObjectId );

		if (	( playerTeamId != -1 && targetedPlayerTeamId == -1 ) ||
				( playerTeamId == -1 && targetedPlayerTeamId != -1 ) ) {
			return false;
		}

		if (playerTeamId != -1 && targetedPlayerTeamId != -1 && playerTeamId == targetedPlayerTeamId && playerInstance.getObjectId() != targetedPlayerObjectId && !Config.TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED )
		{
			return false;
		}

		return true;
	}
	
	/**
	 * Called on every scroll use<br><br>
	 *
	 * @param playerName as String<br>
	 * @return boolean: true if player is allowed to use scroll, otherwise false<br>
	 */
	public static boolean onScrollUse(int playerObjectId)
	{
		if (!isStarted())
			return true;
		
		if (isPlayerParticipant(playerObjectId) && !Config.TVT_EVENT_SCROLL_ALLOWED)
			return false;
		
		return true;
	}

	/**
	 * Called on every potion use<br><br>
	 *
	 * @param playerName as String<br>
	 * @return boolean: true if player is allowed to use potions, otherwise false<br>
	 */
	public static boolean onPotionUse(int playerObjectId)
	{
		if (!isStarted())
			return true;
		
		if (isPlayerParticipant(playerObjectId) && !Config.TVT_EVENT_POTIONS_ALLOWED)
			return false;
		
		return true;
	}

	/**
	 * Called on every escape use(thanks to nbd)<br><br>
	 *
	 * @param playerName as String<br>
	 * @return boolean: true if player is not in tvt event, otherwise false<br>
	 */
	public static boolean onEscapeUse( int playerObjectId ) {
		if ( !isStarted() ) {
			return true;
		}

		if ( isPlayerParticipant( playerObjectId ) ) {
			return false;
		}

		return true;
	}

	/**
	 * Called on every summon item use<br><br>
	 *
	 * @param playerName as String<br>
	 * @return boolean: true if player is allowed to summon by item, otherwise false<br>
	 */
	public static boolean onItemSummon( int playerObjectId )
	{
		if ( !isStarted() ) {
			return true;
		}

		if ( isPlayerParticipant( playerObjectId ) && !Config.TVT_EVENT_SUMMON_BY_ITEM_ALLOWED ) {
			return false;
		}

		return true;
	}

	/**
	 * Is called when a player is killed<br><br>
	 * 
	 * @param killerCharacter as L2Character<br>
	 * @param killedPlayerInstance as L2PcInstance<br>
	 */
	public static void onKill( L2Character killerCharacter, L2PcInstance killedPlayerInstance )
	{
	    if ( killedPlayerInstance == null || !isStarted() ) {
	        return;
	    }

	    byte killedTeamId = getParticipantTeamId( killedPlayerInstance.getObjectId() );

	    if ( killedTeamId == -1 ) {
	        return;
	    }

	    new TvTEventTeleporter( killedPlayerInstance, _teams[ killedTeamId ].getCoordinates(), false, false );

	    if ( killerCharacter == null ) {
	        return;
	    }

		L2PcInstance killerPlayerInstance = null;

		if ( killerCharacter instanceof L2PetInstance || killerCharacter instanceof L2SummonInstance ) {
			killerPlayerInstance = ( ( L2Summon )killerCharacter ).getOwner();

			if ( killerPlayerInstance == null ) {
				return;
			}
		} else if ( killerCharacter instanceof L2PcInstance ) {
		    killerPlayerInstance = ( L2PcInstance )killerCharacter;
		} else {
			return;
		}

		byte killerTeamId = getParticipantTeamId( killerPlayerInstance.getObjectId() );

		if ( killerTeamId != -1 && killedTeamId != -1 && killerTeamId != killedTeamId ) {
			TvTEventTeam killerTeam = _teams[ killerTeamId ];

		    killerTeam.increasePoints();

		    CreatureSay cs = new CreatureSay( killerPlayerInstance.getObjectId(), Say2.TELL, killerPlayerInstance.getName(), "I have killed " + killedPlayerInstance.getName() + "!" );

		    for ( L2PcInstance playerInstance : _teams[ killerTeamId ].getParticipatedPlayers().values() ) {
		        if ( playerInstance != null ) {
		            playerInstance.sendPacket( cs );
		        }
		    }
		}
	}

	/**
	 * Sets the TvTEvent state<br><br>
	 *
	 * @param state as EventState<br>
	 */
	private static void setState( EventState state ) {
		synchronized ( _state ) {
			_state = state;
		}
	}

	/**
	 * Is TvTEvent inactive?<br><br>
	 *
	 * @return boolean: true if event is inactive(waiting for next event cycle), otherwise false<br>
	 */
	public static boolean isInactive() {
		boolean isInactive;

		synchronized ( _state ) {
			isInactive = _state == EventState.INACTIVE;
		}

		return isInactive;
	}

	/**
	 * Is TvTEvent in inactivating?<br><br>
	 *
	 * @return boolean: true if event is in inactivating progress, otherwise false<br>
	 */
	public static boolean isInactivating() {
		boolean isInactivating;

		synchronized ( _state ) {
			isInactivating = _state == EventState.INACTIVATING;
		}

		return isInactivating;
	}

	/**
	 * Is TvTEvent in participation?<br><br>
	 *
	 * @return boolean: true if event is in participation progress, otherwise false<br>
	 */
	public static boolean isParticipating() {
		boolean isParticipating;

		synchronized ( _state ) {
			isParticipating = _state == EventState.PARTICIPATING;
		}

		return isParticipating;
	}

	/**
	 * Is TvTEvent starting?<br><br>
	 *
	 * @return boolean: true if event is starting up(setting up fighting spot, teleport players etc.), otherwise false<br>
	 */
	public static boolean isStarting() {
		boolean isStarting;

		synchronized ( _state ) {
			isStarting = _state == EventState.STARTING;
		}

		return isStarting;
	}

	/**
	 * Is TvTEvent started?<br><br>
	 *
	 * @return boolean: true if event is started, otherwise false<br>
	 */
	public static boolean isStarted() {
		boolean isStarted;

		synchronized ( _state ) {
			isStarted = _state == EventState.STARTED;
		}

		return isStarted;
	}

	/**
	 * Is TvTEvent rewadrding?<br><br>
	 *
	 * @return boolean: true if event is currently rewarding, otherwise false<br>
	 */
	public static boolean isRewarding() {
		boolean isRewarding;

		synchronized ( _state ) {
			isRewarding = _state == EventState.REWARDING;
		}

		return isRewarding;
	}

	/**
	 * Returns the team id of a player, if player is not participant it returns -1<br><br>
	 *
	 * @param playerName as String<br>
	 * @return byte: team name of the given playerName, if not in event -1<br>
	 */
	public static byte getParticipantTeamId( int playerObjectId ) {
		return ( byte )( _teams[ 0 ].containsPlayer( playerObjectId ) ? 0 : ( _teams[ 1 ].containsPlayer( playerObjectId ) ? 1 : -1 ) );
	}

	/**
	 * Returns the team of a player, if player is not participant it returns null <br><br>
	 *
	 * @param player objectId as Integer<br>
	 * @return TvTEventTeam: team of the given playerObjectId, if not in event null <br>
	 */
	public static TvTEventTeam getParticipantTeam( int playerObjectId ) {
		return ( _teams[ 0 ].containsPlayer( playerObjectId ) ? _teams[ 0 ] : ( _teams[ 1 ].containsPlayer( playerObjectId ) ? _teams[ 1 ] : null ) );
	}

	/**
	 * Returns the enemy team of a player, if player is not participant it returns null <br><br>
	 *
	 * @param player objectId as Integer<br>
	 * @return TvTEventTeam: enemy team of the given playerObjectId, if not in event null <br>
	 */
	public static TvTEventTeam getParticipantEnemyTeam( int playerObjectId ) {
		return ( _teams[ 0 ].containsPlayer( playerObjectId ) ? _teams[ 1 ] : ( _teams[ 1 ].containsPlayer( playerObjectId ) ? _teams[ 0 ] : null ) );
	}

	/**
	 * Returns the team coordinates in which the player is in, if player is not in a team return null<br><br>
	 *
	 * @param playerName as String<br>
	 * @return int[]: coordinates of teams, 2 elements, index 0 for team 1 and index 1 for team 2<br>
	 */
	public static int[] getParticipantTeamCoordinates( int playerObjectId ) {
		return _teams[0].containsPlayer( playerObjectId ) ? _teams[ 0 ].getCoordinates() : ( _teams[ 1 ].containsPlayer( playerObjectId ) ? _teams[ 1 ].getCoordinates() : null);
	}


	/**
	 * Is given player participant of the event?<br><br>
	 *
	 * @param playerName as String<br>
	 * @return boolean: true if player is participant, ohterwise false<br>
	 */
	public static boolean isPlayerParticipant( int playerObjectId ) {
		if ( !isParticipating() && !isStarting() && !isStarted() ) {
			return false;
		}

		return _teams[ 0 ].containsPlayer( playerObjectId ) || _teams[ 1 ].containsPlayer( playerObjectId );
	}

	/**
	 * Returns participated player count<br><br>
	 *
	 * @return int: amount of players registered in the event<br>
	 */
	public static int getParticipatedPlayersCount() {
		if ( !isParticipating() && !isStarting() && !isStarted() ) {
			return 0;
		}

		return _teams[ 0 ].getParticipatedPlayerCount() + _teams[ 1 ].getParticipatedPlayerCount();
	}

	/**
	 * Returns teams names<br><br>
	 *
	 * @return String[]: names of teams, 2 elements, index 0 for team 1 and index 1 for team 2<br>
	 */
	public static String[] getTeamNames() {
		return new	String[] {
						_teams[ 0 ].getName(),
						_teams[ 1 ].getName()
					};
	}

	/**
	 * Returns player count of both teams<br><br>
	 *
	 * @return int[]: player count of teams, 2 elements, index 0 for team 1 and index 1 for team 2<br>
	 */
	public static int[] getTeamsPlayerCounts() {
		return new	int[] {
						_teams[ 0 ].getParticipatedPlayerCount(),
						_teams[ 1 ].getParticipatedPlayerCount()
					};
	}

	/**
	 * Returns points count of both teams
	 *
	 * @return int[]: points of teams, 2 elements, index 0 for team 1 and index 1 for team 2<br>
	 */
	public static int[] getTeamsPoints() {
		return new	int[] {
						_teams[ 0 ].getPoints(),
						_teams[ 1 ].getPoints()
					};
	}
}
