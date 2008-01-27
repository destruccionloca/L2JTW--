/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */

package net.sf.l2j.gameserver.instancemanager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2BossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.Earthquake;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.funcs.Func;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Point3D;
import net.sf.l2j.util.Rnd;

public class FrintezzaManager
{
	
	protected static Logger _log = Logger.getLogger(FrintezzaManager.class.getName());
	
	private static FrintezzaManager	_instance = new FrintezzaManager();
	
	// Must be done this way :( Each of the 11 mobs is an individual with different tasks and values
	// So there is no point to place them in an array
	private static L2Spawn frintezzaSpawn, scarletSpawnWeak, scarletSpawnStrong, portraitSpawn1, portraitSpawn2, portraitSpawn3, portraitSpawn4, demonSpawn1, demonSpawn2, demonSpawn3, demonSpawn4;
	
	private static L2BossInstance frintezza, weakScarlet, strongScarlet, portrait1, portrait2, portrait3, portrait4;
	
	// The minions be used as L2MonsterInstance, instead of L2MinionInstance, since they
	// have 3 Bosses: weak scarlet, strong scarlet, and frintezza. All 3 bosses control
	// the minions. Also we do not want the portraits to respawn next to the boss,
	// and we need different respawn intervals for
	// demons and portraits.
	private static L2MonsterInstance demon1, demon2, demon3, demon4;
	
	// Interval time of Monsters.
	protected int _intervalOfBoss, _intervalOfDemons, _intervalOfRetarget, _intervalOfFrintezzaSongs, _callForHelpInterval;
	
	// Delay of appearance time of Boss.
	protected int _appTimeOfBoss;
	
	// Activity time of Boss.
	protected int _activityTimeOfBoss;
	
	// list of intruders.
	protected List<L2PcInstance> _playersInLair = new FastList<L2PcInstance>();
	
	// lists of last saved positions <objectId, location>
	protected Map<Integer, Point3D> _lastLocation = new FastMap<Integer, Point3D>();
	
	// status in lair.
	protected boolean _isBossSpawned = false, _isIntervalForNextSpawn = false, _respawningDemon1 = false, _respawningDemon2 = false, _respawningDemon3 = false, _respawningDemon4 = false, _scarletIsWeakest = true;
	
	protected Future<?> _monsterSpawnTask = null, _activityTimeEndTask = null;
	
	// Actually questname should be "Last Imperial Prince" or "Journey to a Settlement"
	protected String _questName = "frintezza";
    protected String _ZoneType = "PilgrimsTemple";
	
	// location of banishment
	private final Point3D[] _banishmentLocation	= { 
			new Point3D(79959, 151774, -3532),
			new Point3D(81398, 148055, -3468),
			new Point3D(82286, 149113, -3468),
			new Point3D(84264, 147427, -3404)
			};

	private Func _DecreaseRegHp = null;
	private int _debuffPeriod = 0;
	
	/** ************************************ Initial Functions ************************************* */
	
	/**
	 * Empty constructor Does nothing
	 */
	public FrintezzaManager()
	{
		// nothing.
	}
	
	/**
	 * returns an instance of <b>this</b> InstanceManager.
	 */
	public static FrintezzaManager getInstance()
	{
		if (_instance == null)
			_instance = new FrintezzaManager();
		
		return _instance;
	}
	
	/**
	 * initialize <b>this</b> Frintezza Manager
	 */
	public void init()
	{
		_callForHelpInterval = 2000;
		_intervalOfRetarget = 10000;
		_intervalOfFrintezzaSongs = 30000;
		_intervalOfDemons = 60000;
		_intervalOfBoss = 17280000;
		_appTimeOfBoss = 60000;
		_activityTimeOfBoss = 7200000;

		// initialize status in lair.
		_scarletIsWeakest = true;
		_isBossSpawned = false;
		_isIntervalForNextSpawn = false;
		_playersInLair.clear();

		// setting spawn data of monsters.
		try
		{
			createMonsterSpawns();
		}
		catch (Throwable t)
		{
			_log.warning(t.getMessage());
		}
		_log.info("FrintezzaManager:Init FrintezzaManager.");
	}

	private void createMonsterSpawns()
	{
		// The boss of all bosses ofcourse
		frintezzaSpawn = createNewSpawn(29045, 174240, -89805, -5022, 16048, _intervalOfBoss);
		// weak Scarlet Van Halisha.
		scarletSpawnWeak = createNewSpawn(29046, 173203, -88484, -3513, 48028, _intervalOfBoss);
		// Strong Scarlet Van Halisha -> x , y , z , heading, and Hp are set when the morph actually
		scarletSpawnStrong = createNewSpawn(29047, 174234, -88015, -5116, 48028, _intervalOfBoss);
		// Portrait spawns - 4 portraits = 4 spawns
		portraitSpawn1 = createNewSpawn(29048, 175833, -87165, -4972, 35048, _intervalOfBoss);
		portraitSpawn2 = createNewSpawn(29049, 175876, -88713, -4972, 28205, _intervalOfBoss);
		portraitSpawn3 = createNewSpawn(29048, 172608, -88702, -4972, 64817, _intervalOfBoss);
		portraitSpawn4 = createNewSpawn(29049, 172634, -87165, -4972, 57730, _intervalOfBoss);
		// Demon spawns - 4 portraits = 4 demons (?)
		demonSpawn1 = createNewSpawn(29050, 175833, -87165, -4972, 35048, _intervalOfDemons);
		demonSpawn2 = createNewSpawn(29051, 175876, -88713, -4972, 28205, _intervalOfDemons);
		demonSpawn3 = createNewSpawn(29051, 172608, -88702, -4972, 64817, _intervalOfDemons);
		demonSpawn4 = createNewSpawn(29050, 172634, -87165, -4972, 57730, _intervalOfDemons);
	}
	
	private L2Spawn createNewSpawn(int templateId, int x, int y, int z, int heading, int respawnDelay)
	{
		L2Spawn tempSpawn = null;
		L2NpcTemplate template1;
		try
		{
			template1 = NpcTable.getInstance().getTemplate(templateId);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(x);
			tempSpawn.setLocy(y);
			tempSpawn.setLocz(z);
			tempSpawn.setHeading(heading);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(respawnDelay);
			tempSpawn.stopRespawn();
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
		}
		catch (Throwable t)
		{
			_log.warning(t.getMessage());
		}
		return tempSpawn;
	}

	public List<L2PcInstance> getPlayersInLair()
	{
		return _playersInLair;
	}
	
	/**
	 * Checks if a player is in this zone (Frintezza's Lair)
	 * 
	 * @param pc
	 *            L2PcInstance of the player
	 * @return boolean true if the player is inside this zone.
	 */
	public boolean checkIfInZone(L2PcInstance pc)
	{
		return BossZoneManager.getInstance().checkIfInZone(_ZoneType, pc);
	}

	// Whether it lairs is confirmed.
	public boolean isEnableEnterToLair()
	{
		return (_isBossSpawned == false && _isIntervalForNextSpawn == false);
	}
	
	/**
	 * Update the list of intruders.
	 * 
	 * @param pc
	 *            L2PcInstance of the player
	 */
	public void addPlayerToLair(L2PcInstance pc)
	{
		if (!_playersInLair.contains(pc))
			_playersInLair.add(pc);
	}
	
	/**
	 * Checks whether the players were annihilated. If all players in the lair are dead, return boolean <b>true</b> else returns boolean <b>false</b>
	 */
	public synchronized boolean isPlayersAnnihilated()
	{
		for (L2PcInstance pc : _playersInLair)
		{
			// player is must be alive and stay inside of lair.
			//if (!pc.isDead() && checkIfInZone(pc))
			if (!pc.isDead()
					&& BossZoneManager.getInstance().checkIfInZone(_ZoneType, pc))
			{
				// 1 alive is enough.
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Banishes all players from the lair
	 */
	public void banishesPlayers()
	{
		if (_playersInLair == null || _playersInLair.isEmpty())
			return;
/*		
		for (L2PcInstance pc : _playersInLair)
		{
			if(pc.getQuestState(_QuestName) != null) pc.getQuestState(_QuestName).exitQuest(true);
			
			if(BossZoneManager.getInstance().checkIfInZone(_ZoneType, pc));
			{
				int driftX = Rnd.get(-80, 80);
				int driftY = Rnd.get(-80, 80);
				int loc = Rnd.get(4);
				pc.teleToLocation(_banishmentLocation[loc].getX() + driftX, _banishmentLocation[loc].getY() + driftY, _banishmentLocation[loc].getZ());
			}
		}
*/
		_playersInLair.clear();
	}
	
	/**
	 * When the party is annihilated, they are banished.
	 */
	public void checkAnnihilated()
	{
		if (isPlayersAnnihilated())
		{
			OnPlayersAnnihilatedTask o = new OnPlayersAnnihilatedTask();
			Future _onPlayersAnnihilatedTask = ThreadPoolManager.getInstance().scheduleEffect(o, 5000);
			o.setTask(_onPlayersAnnihilatedTask);
		}
	}
	
	/**
	 * When the party is annihilated, they are banished.
	 */
	private class OnPlayersAnnihilatedTask implements Runnable
	{
		private Future<?> _task;
		public OnPlayersAnnihilatedTask()
		{ /* Nothing */
		}
		public void setTask(Future<?> task)
		{
			_task = task;
		}
		public void run()
		{
			// banishes players from lair.
			banishesPlayers();
			// clean up task.
			_task = cancelTask(_task);
			// Note that in THIS case, there is NO setUnspawn, so theoretically
			// The players can do the quest again and continue to fight Frintezza
			// But also note there is a limited time to do this.
		}
	}

	/**
	 * setting Scarlet Van Halisha spawn task which also starts the whole Frintezza battle.
	 */
	public void setScarletSpawnTask()
	{
		// When someone has already invaded the lair, nothing is done.
		if (_playersInLair.size() >= 1 || _isIntervalForNextSpawn)
			return;
		
		if (_monsterSpawnTask == null)
		{
			_monsterSpawnTask = ThreadPoolManager.getInstance().scheduleEffect(new ScarletWeakSpawn(1), _appTimeOfBoss);
			_isIntervalForNextSpawn = true;
		}
	}
	
	private void showSocialActionMovie(L2NpcInstance target, int dist, int yaw, int pitch, int time, int duration, int socialAction)
	{
		if (target == null)
			return;
		
		updateKnownList(target);
		
		if (_playersInLair == null || _playersInLair.isEmpty())
			return;
		
		// set camera.
		for (L2PcInstance pc : _playersInLair)
		{
			setIdle(pc);
			pc.setTarget(null);
			
			if (pc.getPlanDistanceSq(target) <= 6502500)
			{
				pc.enterMovieMode();
				pc.specialCamera(target, dist, yaw, pitch, time, duration);
			}
			else
			{
				pc.leaveMovieMode();
			}
		}
		
		// do social.
		if (socialAction > 0 && socialAction < 5)
		{
			SocialAction sa = new SocialAction(target.getObjectId(), socialAction);
			target.broadcastPacket(sa);
		}
	}
	
	/**
	 * Cancels a given task if it's still active and returns null.
	 * 
	 * @param task
	 *            Future task that is still active
	 * @return null Future value to reset that task
	 */
	private Future cancelTask(Future<?> task)
	{
		if (task != null)
			task.cancel(true);
		return null;
	}
	
	/**
	 * I noticed that if the players do not stand at a certain position, they can not watch the entire movie, so I set them to the center during the entire
	 * movie.
	 */
	private void teleportToStart()
	{
		
		if (_lastLocation == null)
			_lastLocation = new FastMap<Integer, Point3D>();
		
		Point3D p = new Point3D(174233, -88212, -5116);
		
		for (L2PcInstance pc : _playersInLair)
		{
			if (pc == null)
			{
				_playersInLair.remove(pc);
				continue;
			}
			else if (pc.getX() != p.getX() && pc.getY() != p.getY() && pc.getZ() != p.getZ())
			{
				if (!_lastLocation.containsKey(pc.getObjectId()))
					_lastLocation.put(pc.getObjectId(), new Point3D(pc.getX(), pc.getY(), pc.getZ()));
				pc.teleToLocation(p.getX(), p.getY(), p.getZ());
			}
		}
	}
	
	/**
	 * Teleports the players back to their last positions before the movie started
	 */
	private void teleportToFinish()
	{
		if (_lastLocation == null || _lastLocation.isEmpty() || _playersInLair == null)
			return;
		else
		{
			for (L2PcInstance pc : _playersInLair)
			{
				if (pc == null)
				{
					_playersInLair.remove(pc);
					continue;
				}
				else if (_lastLocation.containsKey(pc.getObjectId()))
				{
					Point3D loc = _lastLocation.get(pc.getObjectId());
					pc.teleToLocation(loc.getX(), loc.getY(), loc.getZ());
				}
			}
			_lastLocation.clear();
		}
	}
	
	/**
	 * Spawns Frintezza, the weak version of Scarlet Van Halisha, the minions, and all that is shown in a movie to the observing players.
	 */
	private class ScarletWeakSpawn implements Runnable
	{
		private int			_taskId	= 0;
		private Future<?>	_task;
		public ScarletWeakSpawn(int taskId)
		{
			_taskId = taskId;
		}
		public void setTask(Future<?> task)
		{
			_task = task;
		}
		public void run()
		{
			_task = cancelTask(_task);
			ScarletWeakSpawn s = null;
			SetMobilised mobilise;
			Future<?> _mobiliseTask;
			
			switch (_taskId)
			{
				case 1: // spawn.
					frintezza = (L2BossInstance) frintezzaSpawn.doSpawn();
					frintezza.setIsImobilised(true);
					frintezza.disableAllSkills();
					frintezza.setIsInSocialAction(true);
					
					updateKnownList(frintezza);
					teleportToStart();
					
					// set next task.
					s = new ScarletWeakSpawn(2);
					_task = ThreadPoolManager.getInstance().scheduleEffect(s, 1000);
					
					break;
				
				case 2:
					// Needed twice, once to know the players we teleport,
					// and then once more to update the known list after they teleported
					updateKnownList(frintezza);
					
					// show movie
					showSocialActionMovie(frintezza, 1000, 90, 30, 0, 5000, 0);
					
					// set next task.
					s = new ScarletWeakSpawn(200);
					_task = ThreadPoolManager.getInstance().scheduleEffect(s, 3000);
					
					break;
				
				case 200:
					// show movie
					showSocialActionMovie(frintezza, 1000, 90, 30, 0, 5000, 0);
					
					// set next task.
					s = new ScarletWeakSpawn(3);
					_task = ThreadPoolManager.getInstance().scheduleEffect(s, 3000);
					break;
				
				case 3:
					// show movie
					showSocialActionMovie(frintezza, 140, 90, 0, 6000, 6000, 2);
					
					// set next task.
					s = new ScarletWeakSpawn(5);
					_task = ThreadPoolManager.getInstance().scheduleEffect(s, 5990);
					
					break;
				
				case 5:
					// show movie
					showSocialActionMovie(frintezza, 240, 90, 3, 22000, 6000, 3);
					
					// set next task.
					s = new ScarletWeakSpawn(6);
					_task = ThreadPoolManager.getInstance().scheduleEffect(s, 5800);
					
					break;
				
				case 6:
					// show movie
					showSocialActionMovie(frintezza, 240, 90, 3, 300, 6000, 0);
					frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5006, 1, _intervalOfFrintezzaSongs, 0), 360000/* 600 */);
					
					// set next task.
					s = new ScarletWeakSpawn(7);
					_task = ThreadPoolManager.getInstance().scheduleEffect(s, 5800);
					
					weakScarlet = (L2BossInstance) scarletSpawnWeak.doSpawn();
					weakScarlet.setIsImobilised(true);
					weakScarlet.setIsInSocialAction(true);
					
					portrait1 = (L2BossInstance) portraitSpawn1.doSpawn();
					portrait1.setIsImobilised(true);
					
					portrait2 = (L2BossInstance) portraitSpawn2.doSpawn();
					portrait2.setIsImobilised(true);
					
					portrait3 = (L2BossInstance) portraitSpawn3.doSpawn();
					portrait3.setIsImobilised(true);
					
					portrait4 = (L2BossInstance) portraitSpawn4.doSpawn();
					portrait4.setIsImobilised(true);
					
					demon1 = (L2MonsterInstance) demonSpawn1.doSpawn();
					demon1.setIsImobilised(true);
					
					demon2 = (L2MonsterInstance) demonSpawn2.doSpawn();
					demon2.setIsImobilised(true);
					
					demon3 = (L2MonsterInstance) demonSpawn3.doSpawn();
					demon3.setIsImobilised(true);
					
					demon4 = (L2MonsterInstance) demonSpawn4.doSpawn();
					demon4.setIsImobilised(true);
					
					Earthquake eq = new Earthquake(weakScarlet.getX(), weakScarlet.getY(), weakScarlet.getZ(), 50, 6);
					
					for (L2PcInstance pc : _playersInLair)
						pc.broadcastPacket(eq);
					
					break;
				
				case 7:

					showSocialActionMovie(demon1, 140, 0, 3, 22000, 3000, 1);
					s = new ScarletWeakSpawn(8);
					_task = ThreadPoolManager.getInstance().scheduleEffect(s, 2800);
					break;
				
				case 8:

					showSocialActionMovie(demon2, 140, 0, 3, 22000, 3000, 1);
					s = new ScarletWeakSpawn(9);
					_task = ThreadPoolManager.getInstance().scheduleEffect(s, 2800);
					break;
				
				case 9:

					showSocialActionMovie(demon3, 140, 180, 3, 22000, 3000, 1);
					s = new ScarletWeakSpawn(10);
					_task = ThreadPoolManager.getInstance().scheduleEffect(s, 2800);
					break;
				
				case 10:

					showSocialActionMovie(demon4, 140, 180, 3, 22000, 3000, 1);
					s = new ScarletWeakSpawn(17);
					_task = ThreadPoolManager.getInstance().scheduleEffect(s, 2800);
					
					SocialAction sa = new SocialAction(weakScarlet.getObjectId(), 2);
					weakScarlet.broadcastPacket(sa);
					break;
				
				case 17:

					// show movie
					for (L2PcInstance pc : _playersInLair)
					{
						pc.setTarget(null);
						
						pc.disableAllSkills();
						
						if (pc.getPlanDistanceSq(weakScarlet) <= 6502500)
						{
							pc.enterMovieMode();
							pc.specialCamera(weakScarlet, 1700, 180, -90, 0, 4000);
						}
						
						else
						{
							pc.leaveMovieMode();
						}
					}
					
					// set next task.
					s = new ScarletWeakSpawn(18);
					_task = ThreadPoolManager.getInstance().scheduleEffect(s, 3800);
					
					break;
				
				case 18:
					// show movie
					showSocialActionMovie(weakScarlet, 1500, 270, -70, 6000, 7000, 2);
					
					// set next task.
					s = new ScarletWeakSpawn(19);
					_task = ThreadPoolManager.getInstance().scheduleEffect(s, 6900);
					
					break;
				
				case 19:
					// show movie
					showSocialActionMovie(weakScarlet, 1500, 0, -60, 0, 5000, 2);
					
					// set next task.
					s = new ScarletWeakSpawn(20);
					_task = ThreadPoolManager.getInstance().scheduleEffect(s, 4900);
					
					break;
				
				case 20:
					// show movie
					showSocialActionMovie(weakScarlet, 1220, 90, -70, 300, 2000, 0);
					
					// set next task.
					s = new ScarletWeakSpawn(21);
					_task = ThreadPoolManager.getInstance().scheduleEffect(s, 1900);
					
					break;
				
				case 21:

					weakScarlet.abortCast();
					weakScarlet.enableAllSkills();
					
					mobilise = new SetMobilised(weakScarlet);
					_mobiliseTask = ThreadPoolManager.getInstance().scheduleEffect(mobilise, 16);
					mobilise.setTask(_mobiliseTask);
					
					// L2CharPosition pos = new L2CharPosition(174234, -88015, -5116, 48028);
					// _moveAtRandomTask = ThreadPoolManager.getInstance().scheduleEffect(new MoveAtRandom(weakScarlet, pos) , 32);
					weakScarlet.teleToLocation(174234, -88015, -5116, false);
					
					updateKnownList(weakScarlet);
					
					showSocialActionMovie(weakScarlet, 1000, 270, 19, 300, 3000, 2);
					
					s = new ScarletWeakSpawn(32);
					_task = ThreadPoolManager.getInstance().scheduleEffect(s, 3100);
					
					teleportToFinish();
					
					// set delete task.
					ActivityTimeEnd ate = new ActivityTimeEnd();
					_activityTimeEndTask = ThreadPoolManager.getInstance().scheduleEffect(ate, _activityTimeOfBoss);
					
					break;
				
				case 32:

					// reset camera.
					for (L2PcInstance pc : _playersInLair)
					{
						pc.leaveMovieMode();
						pc.enableAllSkills();
					}
					
					frintezza.abortCast();
					frintezza.enableAllSkills();
					
					L2Skill skill = SkillTable.getInstance().getInfo(1086, 1);
					
					demon1.setIsImobilised(false);
					doSkill ds = new doSkill(demon1, skill, _intervalOfFrintezzaSongs, 1000);
					Future<?> _doSkillTask = ThreadPoolManager.getInstance().scheduleEffect(ds, 4000);
					ds.setTask(_doSkillTask);
					
					demon2.setIsImobilised(false);
					ds = new doSkill(demon2, skill, _intervalOfFrintezzaSongs, 1000);
					_doSkillTask = ThreadPoolManager.getInstance().scheduleEffect(ds, 4100);
					ds.setTask(_doSkillTask);
					
					demon3.setIsImobilised(false);
					ds = new doSkill(demon3, skill, _intervalOfFrintezzaSongs, 1000);
					_doSkillTask = ThreadPoolManager.getInstance().scheduleEffect(ds, 4200);
					ds.setTask(_doSkillTask);
					
					demon4.setIsImobilised(false);
					ds = new doSkill(demon4, skill, _intervalOfFrintezzaSongs, 1000);
					_doSkillTask = ThreadPoolManager.getInstance().scheduleEffect(ds, 4300);
					ds.setTask(_doSkillTask);
					
					mobilise = new SetMobilised(frintezza);
					_mobiliseTask = ThreadPoolManager.getInstance().scheduleEffect(mobilise, 16);
					mobilise.setTask(_mobiliseTask);
					
					// Start random attacks on players for Frintezza
					ReTarget _retarget = new ReTarget(frintezza);
					Future<?> _reTargetTask = ThreadPoolManager.getInstance().scheduleEffect(_retarget, _intervalOfRetarget);
					_retarget.setTask(_reTargetTask);
					
					// Start random attacks on players for Scarlet
					_retarget = new ReTarget(weakScarlet);
					_reTargetTask = ThreadPoolManager.getInstance().scheduleEffect(_retarget, _intervalOfRetarget + 16);
					_retarget.setTask(_reTargetTask);
					
					Music music = new Music();
					Future<?> _MusicTask = ThreadPoolManager.getInstance().scheduleEffect(music, Rnd.get(_intervalOfFrintezzaSongs));
					music.setTask(_MusicTask);
					
					startAttackListeners();
					
					break;
				
			}
			
			// Setup the new task to be deleted after the thread runs again.
			if (s != null)
				s.setTask(_task);
			
		}
		
	}
	
	/** ******************************************************************************************** */
	
	/***********************************************************************************************************************************************************
	 * ****** M M PPPPPPP TTTTTTTTT Y Y /* MM MM P P T Y Y /* M M M M P P T Y Y /******* M M M M P P T Y /* M M M PPPPPPP T Y /* M M P T Y /* M M P T Y
	 * /******** M M P T Y /*********************************************************************************************** /** Frintezza's songs, needed
	 * special implementation since core doesn't support 3 stage skills * /
	 **********************************************************************************************************************************************************/
	
	/**
	 * @author Darki699 Three stages of casts: 1. Song, cast on Frintezza, for the music to play (skill 5007, levels 1-5) Each level is a different tune =) 2.
	 *         Visual Effect, cast on targets, to show the effect (skill 5008, levels 1-5) Each level has a different animation effect and different targets and
	 *         purpose 3. Actual skill, which is different since NCSoft has a different skill system. I used other skill Ids to implement these effects: song
	 *         effects: 1. skill 1217 - Greater Heal (5007,1 -> 5008,1 -> 1217,33) 2. skill 1204 - Wind Walk (5007,2 -> 5008,2 -> 1204,2) 3. skill 1086 - Haste
	 *         Buff (5007,3 -> 5008,3 -> 1086,2) 4. skill 406 - Angelic Icon (5007,4 -> 5008,4 -> 406,3 only gainHp*0.2 func added) 5. no skill only immobilize
	 *         (5007,5 -> 5008,5 -> dance+stun animation + Immobilizes)
	 */
	
	private class Music implements Runnable
	{
		
		private Future<?>	_MusicTask;
		
		public Music()
		{
		}
		
		public void setTask(Future<?> task)
		{
			_MusicTask = task;
		}
		
		public void run()
		{
			_MusicTask = cancelTask(_MusicTask);
			
			if (frintezza == null || frintezza.isAlikeDead())
				return;
			
			int song = getSong();
			if (song < 1)
				song = 1;
			else if (song > 5)
				song = 5;
			
			frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5007, song, _intervalOfFrintezzaSongs, 0), 10000);
			
			int currentHp = (int) (frintezza.getStatus().getCurrentHp());
			
			// Launch the song's effects (they start about 10 seconds after he starts to play)
			SongEffectLaunched launchSong = new SongEffectLaunched(getSongTargets(song), song, currentHp, 10000);
			Future<?> _songLaunchedTask = ThreadPoolManager.getInstance().scheduleGeneral(launchSong, 10000);
			launchSong.setTask(_songLaunchedTask);
			
			// Schedule a new song to be played in 30-40 seconds...
			Music music = new Music();
			_MusicTask = ThreadPoolManager.getInstance().scheduleEffect(music, _intervalOfFrintezzaSongs + Rnd.get(10000));
			music.setTask(_MusicTask);
			
		}
		
		/**
		 * Depending on the song, returns the song's targets (either mobs or players)
		 * 
		 * @param songId
		 *            (1-5 songs)
		 * @return L2Object[] targets
		 */
		private L2Object[] getSongTargets(int songId)
		{
			
			List<L2Object> targets = new FastList<L2Object>();
			
			if (songId < 4) // Target is the minions
			{
				
				if (weakScarlet != null && !weakScarlet.isDead())
					targets.add(weakScarlet);
				
				if (strongScarlet != null && !strongScarlet.isDead())
					targets.add(strongScarlet);
				
				if (portrait1 != null && !portrait1.isDead())
					targets.add(portrait1);
				
				if (portrait2 != null && !portrait2.isDead())
					targets.add(portrait2);
				
				if (portrait3 != null && !portrait3.isDead())
					targets.add(portrait3);
				
				if (portrait4 != null && !portrait4.isDead())
					targets.add(portrait4);
				
				if (demon1 != null && !demon1.isDead())
					targets.add(demon1);
				
				if (demon2 != null && !demon2.isDead())
					targets.add(demon2);
				
				if (demon3 != null && !demon3.isDead())
					targets.add(demon3);
				
				if (demon4 != null && !demon4.isDead())
					targets.add(demon4);
				
				targets.add(frintezza);
			}
			
			else
			// Target is the players
			{
				
				for (L2PcInstance pc : _playersInLair)
				{
					
					if (pc == null /* || pc.isInvul() || pc.getAppearance().getInvisible() */)
						continue;
					
					else if (pc != null && !pc.isDead())
						targets.add(pc);
					
				}
				
			}
			
			return targets.toArray(new L2Object[targets.size()]);
		}
		
		/**
		 * returns the chosen symphony for Frintezza to play If the minions are injured he has 40% to play a healing song If they are all dead, he will only
		 * play harmful player symphonies
		 * 
		 * @return
		 */
		
		private int getSong()
		{
			
			if (minionsNeedHeal())
				return 1;
			
			else if (minionsAreDead())
				return Rnd.get(4, 6);
			
			return Rnd.get(2, 6);
			
		}
		
		/**
		 * Checks if the main minions are dead (not including demons, only Scarlet and Portraits)
		 * 
		 * @return boolean true if all main minions are dead
		 */
		
		private boolean minionsAreDead()
		{
			
			if (weakScarlet != null && !weakScarlet.isDead())
				return false;
			
			else if (strongScarlet != null && !strongScarlet.isDead())
				return false;
			
			else if (portrait1 != null && !portrait1.isDead())
				return false;
			
			else if (portrait2 != null && !portrait2.isDead())
				return false;
			
			else if (portrait3 != null && !portrait3.isDead())
				return false;
			
			else if (portrait4 != null && !portrait4.isDead())
				return false;
			
			return true;
			
		}
		
		/**
		 * Checks if Frintezza's minions need heal (only major minions are checked) Return a "need heal" = true only 40% of the time
		 * 
		 * @return boolean value true if need to play a healing minion song
		 */
		private boolean minionsNeedHeal()
		{
			
			boolean returnValue = false;
			
			if (weakScarlet != null && !weakScarlet.isAlikeDead() && weakScarlet.getStatus().getCurrentHp() < weakScarlet.getMaxHp() * 2 / 3)
				
				returnValue = true;
			
			else if (strongScarlet != null && !strongScarlet.isAlikeDead() && strongScarlet.getStatus().getCurrentHp() < strongScarlet.getMaxHp() * 2 / 3)
				
				returnValue = true;
			
			else if ((portrait1 != null && !portrait1.isAlikeDead() && portrait1.getStatus().getCurrentHp() < portrait1.getMaxHp() / 3)
					|| (portrait2 != null && !portrait2.isAlikeDead() && portrait2.getStatus().getCurrentHp() < portrait2.getMaxHp() / 3)
					|| (portrait3 != null && !portrait3.isAlikeDead() && portrait3.getStatus().getCurrentHp() < portrait3.getMaxHp() / 3)
					|| (portrait4 != null && !portrait4.isAlikeDead() && portrait4.getStatus().getCurrentHp() < portrait4.getMaxHp() / 3))
				
				returnValue = true;
			
			if (returnValue && Rnd.get(100) > 40) // 40% to heal minions when needed.
				return false;
			
			return returnValue;
			
		}
		
	}
	
	/**
	 * The song was played, this class checks it's affects (if any)
	 * 
	 * @author Darki699
	 */
	private class SongEffectLaunched implements Runnable
	{
		
		private L2Object[]	_targets;
		
		private int			_song, _previousHp, _currentTime;
		
		private Future<?>	_songLaunchedTask;
		
		/**
		 * Constructor
		 * 
		 * @param targets -
		 *            song's targets L2Object[]
		 * @param song -
		 *            song id 1-5
		 * @param previousHp -
		 *            Frintezza's HP when he started to play
		 * @param currentTimeOfSong -
		 *            skills during music play are consecutive, repeating
		 */
		public SongEffectLaunched(L2Object[] targets, int song, int previousHp, int currentTimeOfSong)
		{
			_targets = targets;
			_song = song;
			_previousHp = previousHp;
			_currentTime = currentTimeOfSong;
		}
		
		public void setTask(Future<?> task)
		{
			_songLaunchedTask = task;
		}
		
		public void run()
		{
			_songLaunchedTask = cancelTask(_songLaunchedTask);
			
			if (frintezza == null)
				return;
			
			// If the song time is over stop this loop
			else if (frintezza.isDead() || _currentTime > _intervalOfFrintezzaSongs)
				return;
			
			// Skills are consecutive, so call them again
			SongEffectLaunched songLaunched = new SongEffectLaunched(_targets, _song, (int) frintezza.getStatus().getCurrentHp(), _currentTime
					+ _intervalOfFrintezzaSongs / 10);
			_songLaunchedTask = ThreadPoolManager.getInstance().scheduleGeneral(songLaunched, _intervalOfFrintezzaSongs / 10);
			songLaunched.setTask(_songLaunchedTask);
			
			// If Frintezza got injured harder than his regen rate, do not launch the song.
			if (_previousHp > frintezza.getStatus().getCurrentHp())
			{
				L2Object frintezzaTarget = frintezza.getTarget();
				
				if (frintezzaTarget != null && frintezzaTarget instanceof L2Character)
					callMinionsToAssist(((L2Character) frintezzaTarget), 200);
				
				return;
			}
			
			for (L2Object target : _targets)
			{
				
				if (target == null)
					continue;
				
				else if (!(target instanceof L2Character))
					continue;
				
				else if (((L2Character) target).isDead())
					continue;
				
				else if (target instanceof L2PcInstance && ((L2PcInstance) target).isInvul())
					continue;
				
				// show the magic effect on the target - visual effect
				((L2Character) target).broadcastPacket(new MagicSkillUse(frintezza, ((L2Character) target), 5008, _song, 2000, 0), 10000);
				
				// calculate the song's damage
				calculateSongEffects((L2Character) target);
			}
			
		}
		
		/**
		 * Calculates the music damage according to the current song played
		 * 
		 * @param target -
		 *            L2Character affected by the music
		 */
		private void calculateSongEffects(L2Character target)
		{
			
			if (target == null)
				return;
			
			try
			{
				
				L2Skill skill;
				
				switch (_song)
				{
					
					case 1: // Consecutive Heal : Greater Heal - on the monsters
						skill = SkillTable.getInstance().getInfo(1217, 33);
						frintezza.callSkill(skill, new L2Object[] { target });
						break;
					
					case 2: // Consecutive Dash : Wind Walk - monsters run faster
						skill = SkillTable.getInstance().getInfo(1204, 2);
						frintezza.callSkill(skill, new L2Object[] { target });
						break;
					
					case 3: // Affecting Atk Spd : Haste Buff - monsters attack faster
						skill = SkillTable.getInstance().getInfo(1086, 2);
						frintezza.callSkill(skill, new L2Object[] { target });
						break;
					
					case 4: // Offensive Skill: Decreases the effect of HP reg. on the players
					
						if (Rnd.get(100) < 80) // 80% success not considering m.def + p.def ???
						{
							// Launch the skill on the target to decrease it's HP
							decreaseEffectOfHpReg(target);
						}
						break;
					
					case 5: // Offensive Skill: Immoblizes - dance+stun. Player is immoblized
						skill = SkillTable.getInstance().getInfo(5008, 5);
						if (Rnd.get(100) < 80) // 80% success not considering m.def + p.def ???
						{
							new startStunDanceEffect(target, skill).run();
						}
						break;
					
				}
				
			}
			
			catch (Throwable t)
			{
				
				t.printStackTrace();
				
			}
			
		}
		
	}
	
	/**
	 * Decreases the HP Regeneration of the <b>target</b>
	 * 
	 * @param target
	 *            L2Character who's HP Regeneration is decreased.
	 */
	private void decreaseEffectOfHpReg(L2Character target)
	{
		
		// if (target instanceof L2PcInstance && (((L2PcInstance)target).isInvul() || ((L2PcInstance)target).getAppearance().getInvisible()))
		// return;
		
		L2Skill skill = SkillTable.getInstance().getInfo(5008, 4);
		
		frintezza.callSkill(skill, new L2Object[] { target });
		
		// send target the message, the skill was launched
		SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
		sm.addSkillName(skill.getId());
		target.sendPacket(sm);
		
		// Add stat funcs to the target
		target.addStatFunc(getDecreaseRegHpFunc());
		
		// Set exit timer for these stats
		exitDecreaseRegHp exit = new exitDecreaseRegHp(target, getDecreaseRegHpFunc());
		Future<?> task = ThreadPoolManager.getInstance().scheduleEffect(exit, getDebuffPeriod(skill, target));
		exit.setTask(task);
		
	}
	
	/**
	 * Returns the duration of the <b>skill</b> on the <b>target</b>.
	 * 
	 * @param skill
	 *            L2Skill to calculate it's duration
	 * @param target
	 *            L2Character to calculate the duration on it
	 * @return int value of skill duration before exit
	 */
	private int getDebuffPeriod(L2Skill skill, L2Character target)
	{
		
		// This is usually returned, unless _debuffPeriod needs initialization
		if (_debuffPeriod != 0)
			return _debuffPeriod;
		
		else
		// Initialize _debuffPeriod
		{
			
			if (skill == null || target == null)
			{
				
				_debuffPeriod = 15000;
				
				return _debuffPeriod;
				
			}
			
			for (L2Effect effect : skill.getEffects(frintezza, target))
			{
				if (effect == null)
					continue;
				
				else
					_debuffPeriod = effect.getPeriod() * 1000;
			}
			
			if (_debuffPeriod == 0)
				_debuffPeriod = 15000;
		}
		
		// return _debuffPeriod which is now initialized
		return _debuffPeriod;
	}
	
	/**
	 * This function simulates the functions of "Angelic Icon". Takes the 5th function which is gainHp*0.2 and adds it to the skill id 5007, level 4 to decrease
	 * gainHP
	 * 
	 * @return <b>Func</b> the functions needed to decrease the Hp Regeneration from the targets
	 */
	private Func getDecreaseRegHpFunc()
	{
		
		// If the Func[] _DecreaseRegHp is null we initialize it.
		if (_DecreaseRegHp == null)
		{
			
			L2Skill skill = SkillTable.getInstance().getInfo(406, 3);
			
			for (L2Effect effect : skill.getEffects(frintezza, frintezza))
			{
				
				if (effect == null)
					continue;
				
				else
				{
					
					Func[] func = effect.getStatFuncs();
					
					if (func.length > 5)
						_DecreaseRegHp = func[5];
					
					effect.exit(); // We don't want to leave the effect on frintezza
					
				}
				
			}
			
		}
		
		// Func _DecreaseRegHp is not null, so just return it ;]
		return _DecreaseRegHp;
		
	}
	
	/**
	 * Class made to exit the debuff effect of the DecreaseRegHp symphony
	 * 
	 * @author Darki699
	 */
	
	private class exitDecreaseRegHp implements Runnable
	{
		
		private Future<?>	_task;
		
		private Func		_func;
		
		private L2Character	_char;
		
		public exitDecreaseRegHp(L2Character character, Func func)
		{
			_func = func;
			_char = character;
		}
		
		public void setTask(Future<?> task)
		{
			_task = task;
		}
		
		public void run()
		{
			_task = cancelTask(_task);
			
			if (_func != null && _char != null)
			{
				_char.removeStatFunc(_func);
			}
			
		}
		
	}
	
	/**
	 * Further implementation into the core is needed. But this will do for now ;] Class needed to implement the start Frintezza dance+stun effect on a target.
	 * 
	 * @author Darki699
	 */
	
	private class startStunDanceEffect implements Runnable
	{
		
		private final L2Character	_effected;
		
		private final L2Skill		_skill;
		
		private Future<?>			_task;
		
		public startStunDanceEffect(L2Character target, L2Skill skill)
		{
			_effected = target;
			_skill = skill;
		}
		
		public void run()
		{
			
			try
			{
				
				if (_effected == null)
					return;
				
				if (_effected.getFirstEffect(_skill) != null)
					return;
				
				// if (_effected instanceof L2PcInstance && (((L2PcInstance)_effected).isInvul() || ((L2PcInstance)_effected).getAppearance().getInvisible()) )
				// return;
				
				// stop all actions
				setIdle(_effected);
				
				_effected.setTarget(null);
				
				// start the animation
				_effected.startAbnormalEffect(_effected.ABNORMAL_EFFECT_DANCE_STUNNED);
				
				// add the effect icon
				_effected.callSkill(_skill, new L2Object[] { _effected });
				
				// send target the message
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
				sm.addSkillName(_skill.getId());
				_effected.sendPacket(sm);
				
				// set the cancel task for this effect
				exitStunDanceEffect exit = new exitStunDanceEffect(_effected);
				
				_task = ThreadPoolManager.getInstance().scheduleEffect(exit, getDebuffPeriod(_skill, _effected));
				exit.setTask(_task); // We delete the _task task at run time on the exit effect.
				
			}
			
			catch (Throwable t)
			{
				
				if (_effected != null && !_effected.isAlikeDead())
				{
					_effected.enableAllSkills();
					_effected.setIsImobilised(false);
				}
				
				_log.warning(t.getMessage());
				
			}
			
		}
		
	}
	
	/**
	 * Ends the dance+stun effect on the target
	 * 
	 * @author Darki699
	 */
	
	private class exitStunDanceEffect implements Runnable
	{
		
		private final L2Character	_effected;
		
		private Future<?>			_task;
		
		public exitStunDanceEffect(L2Character target)
		{
			_effected = target;
		}
		
		public void setTask(Future<?> task)
		{
			_task = task;
		}
		
		public void run()
		{
			
			_task = cancelTask(_task);
			
			if (_effected == null)
				return;
			
			_effected.enableAllSkills();
			_effected.setIsImobilised(false);
			
			_effected.stopAbnormalEffect(_effected.ABNORMAL_EFFECT_DANCE_STUNNED);
			
		}
		
	}
	
	/** ************************** End of Frintezza's Musical effects ******************************** */
	
	/***********************************************************************************************************************************************************
	 * ****** M M PPPPPPP TTTTTTTTT Y Y /* MM MM P P T Y Y /* M M M M P P T Y Y /******* M M M M P P T Y /* M M M PPPPPPP T Y /* M M P T Y /* M M P T Y
	 * /******** M M P T Y /************************ Minion Control Attack + Respawn + Polymorph
	 **********************************************************************************************************************************************************/
	
	/**
	 * Initializes the Attack Listeners for <b>all</b> monsters in this zone. Sends a thread loop (tasks canceled ofcourse) with the mob to be listened to, and
	 * the amount of hate it sends to all other mobs regarding it's attacker.
	 */
	
	private void startAttackListeners()
	{
		
		// Set listeners for the Demons.
		attackerListener al = new attackerListener(demon1, 1);
		Future<?> task = ThreadPoolManager.getInstance().scheduleGeneral(al, Rnd.get(2000));
		al.setTask(task);
		
		al = new attackerListener(demon2, 1);
		task = ThreadPoolManager.getInstance().scheduleGeneral(al, Rnd.get(2000));
		al.setTask(task);
		
		al = new attackerListener(demon3, 1);
		task = ThreadPoolManager.getInstance().scheduleGeneral(al, Rnd.get(2000));
		al.setTask(task);
		
		al = new attackerListener(demon4, 1);
		task = ThreadPoolManager.getInstance().scheduleGeneral(al, Rnd.get(2000));
		al.setTask(task);
		
		// Set listeners for the Portraits.
		al = new attackerListener(portrait1, 50);
		task = ThreadPoolManager.getInstance().scheduleGeneral(al, Rnd.get(2000));
		al.setTask(task);
		
		al = new attackerListener(portrait2, 50);
		task = ThreadPoolManager.getInstance().scheduleGeneral(al, Rnd.get(2000));
		al.setTask(task);
		
		al = new attackerListener(portrait3, 50);
		task = ThreadPoolManager.getInstance().scheduleGeneral(al, Rnd.get(2000));
		al.setTask(task);
		
		al = new attackerListener(portrait4, 50);
		task = ThreadPoolManager.getInstance().scheduleGeneral(al, Rnd.get(2000));
		al.setTask(task);
		
		// Set a listener for Frintezza.
		al = new attackerListener(frintezza, 200);
		task = ThreadPoolManager.getInstance().scheduleGeneral(al, Rnd.get(2000));
		al.setTask(task);
		
		// Set a listener for the weaker version of Scarlet Van Halisha.
		al = new attackerListener(weakScarlet, 100);
		task = ThreadPoolManager.getInstance().scheduleGeneral(al, Rnd.get(2000));
		al.setTask(task);
		
		// Note that Scarlet's strong version isn't spawned yet. An attack listener for it will
		// be added when and if he's spawned.
	}
	
	/**
	 * Class is recalled at an interval for a monster's life time. Once the monster is <b>deleted</b> (null), this class is not called anymore If the monster
	 * is <b>dead</b>, this class is still called at the interval, but it does nothing until next respawn. If the monster is <b>alive</b> and is being
	 * attacked, it "tells" the other monsters that it's attacked.
	 * 
	 * @author Darki699
	 */
	
	private class attackerListener implements Runnable
	{
		
		private Future<?>	_task;
		
		private L2Character	_mob;
		
		private int			_aggroDamage;
		
		public attackerListener(L2Character controller, int hate)
		{
			
			_mob = controller;
			
			_aggroDamage = hate;
			
		}
		
		public void setTask(Future<?> task)
		{
			
			_task = task;
			
		}
		
		public void run()
		{
			
			// Delete the task that called for this thread.
			_task = cancelTask(_task);
			
			// If the monster is deleted, return.
			if (_mob == null)
				return;
			
			// Set next listener.
			attackerListener al = new attackerListener(_mob, _aggroDamage);
			Future<?> task = ThreadPoolManager.getInstance().scheduleGeneral(al, _callForHelpInterval + Rnd.get(500));
			al.setTask(task);
			
			// If the mob is dead, we do nothing until next respawn
			if (_mob.isDead())
			{
				
				try
				{
					
					// if this is a demon, decay it until next respawn.
					if (!(_mob instanceof L2BossInstance))
						_mob.decayMe();
					
					else
						// if this is a boss, we need to do a few checks:
						// portraits - unspawn demons, set both to null
						// Scarlet, Frintezza - check if all bosses are dead.
						bossDeadCheck((L2BossInstance) _mob);
					
				}
				
				catch (Throwable t)
				{
				}
				
				return;
				
			}
			
			// Added a check, since mobs sometimes move toward their targets (teleport)
			// He shouldn't move from his place
			else if (_mob == frintezza)
			{
				if (_mob.getX() != frintezzaSpawn.getLocx() || _mob.getY() != frintezzaSpawn.getLocy())
				{
					
					boolean[] targeted = getTargeted(_mob);
					L2Object target = _mob.getTarget();
					
					_mob.getPosition().setXYZ(frintezzaSpawn.getLocx(), frintezzaSpawn.getLocy(), frintezzaSpawn.getLocz());
					
					_mob.decayMe();
					_mob.spawnMe();
					
					_mob.setTarget(target);
					setTargeted(_mob, targeted);
					
				}
				
			}
			
			// Tell the other mobs "I'm attacked"
			L2Object target = _mob.getTarget();
			
			if (target == null || (target instanceof L2Character && ((L2Character) target).isDead()))
				return;
			
			/*
			 * if (target instanceof L2PcInstance && (((L2PcInstance)target).isInvul() || ((L2PcInstance)target).getAppearance().getInvisible())) {
			 * _mob.abortAttack(); _mob.abortCast(); _mob.setTarget(null); _mob.getKnownList().getKnownPlayers().remove((L2PcInstance)target); return; }
			 */

			if (target != null && target instanceof L2Character)
				callMinionsToAssist((L2Character) target, _aggroDamage);
			
			// Now set the mob's Target to the most hated:
			if (_mob instanceof L2Attackable)

			{
				L2Character mostHated = ((L2Attackable) _mob).getMostHated();
				
				if (mostHated != null)
				{
					
					_mob.setTarget(mostHated);
					
					if (mostHated instanceof L2PcInstance && !_playersInLair.contains((L2PcInstance) mostHated))
						_playersInLair.add((L2PcInstance) mostHated);
					
				}
				
			}
		}
		
	}
	
	/**
	 * If the dead boss is a Portrait, we delete it from the world, and it's demon as well If the dead boss is Scarlet or Frintezza, we do a bossesAreDead()
	 * check to see if both Frintezza and Scarlet are dead.
	 * 
	 * @param mob -
	 *            L2BossInstance that is (or is set as) dead.
	 */
	
	public void bossDeadCheck(L2BossInstance mob)
	{
		
		if (mob == null)
			return;
		
		// !!! Frintezza or Scarlet should NEVER be called from setUnspawn() to this function !!!
		// It will cause a deadlock.
		if (mob == frintezza || mob == weakScarlet || mob == strongScarlet)
		{
			if (bossesAreDead())
				doUnspawn();
			return;
		}
		
		L2MonsterInstance demon = null;
		
		if (mob == portrait1)
		{
			portrait1 = null;
			demon = demon1;
			demon1 = null;
		}
		
		else if (mob == portrait2)
		{
			portrait2 = null;
			demon = demon2;
			demon2 = null;
		}
		
		else if (mob == portrait3)
		{
			portrait3 = null;
			demon = demon3;
			demon3 = null;
		}
		
		else if (mob == portrait4)
		{
			portrait4 = null;
			demon = demon4;
			demon4 = null;
		}
		
		// Try to delete the portrait.
		try
		{
			mob.decayMe();
			mob.deleteMe();
			mob = null;
		}
		catch (Throwable t)
		{
		}
		
		// Try to delete the portraits demon.
		if (demon != null)
		{
			try
			{
				demon.decayMe();
				demon.deleteMe();
				demon = null;
			}
			catch (Throwable t)
			{
			}
			
		}
	}
	
	/**
	 * controls the assistance for all 3 bosses: 1. if Frintezza needs help, all (including Scarlet van Halisha) help him 2. if Scarlet needs help, all
	 * (including Frintezza) come to his help 3. if Strong Scarlet is already spawned, then he teleports to help Frintezza
	 * 
	 * @param L2Character
	 *            attacker - The player that attacked the boss
	 * @param int
	 *            hate - Damage hate to add to the attacker 1. Frintezza adds 200 hate 2. Weak Scarlet adds 100 hate 3. Stronger Scarlet adds 125 hate 4.
	 *            Strongest Scarlet adds 150 hate 5. Portraits adds 50 hate 6. Demons adds 1 hate
	 */
	
	public void callMinionsToAssist(L2Character attacker, int hate)
	{
		
		if (attacker == null)
			return;
		
		if (demon1 != null && !demon1.isDead())
			demon1.addDamage(attacker, hate);
		else
			checkRespawnTime(demon1);
		
		if (demon2 != null && !demon2.isDead())
			demon2.addDamage(attacker, hate);
		else
			checkRespawnTime(demon2);
		
		if (demon3 != null && !demon3.isDead())
			demon3.addDamage(attacker, hate);
		else
			checkRespawnTime(demon3);
		
		if (demon4 != null && !demon4.isDead())
			demon4.addDamage(attacker, hate);
		else
			checkRespawnTime(demon4);
		
		if (weakScarlet != null && !weakScarlet.isDead())
		{
			weakScarletHpListener();
			weakScarlet.addDamage(attacker, hate);
		}
		else
			bossesAreDead();
		
		if (strongScarlet != null && !strongScarlet.isDead())
			strongScarlet.addDamage(attacker, hate);
		else
			bossesAreDead();
		
		if (frintezza != null && !frintezza.isDead())
			frintezza.addDamage(attacker, hate);
		else
			bossesAreDead();
	}
	
	/**
	 * Checks on a killed demon to set it's respawn time (only done if the <b>Portrait</b> of <b>Demon</b> was not killed)
	 * 
	 * @param mob -
	 *            L2MonsterInstance mob of the Demon that was killed
	 * @return int value of the demon that should be respawned. -1 is returned if the demon should not respawn now.
	 */
	
	public int checkRespawnTime(L2MonsterInstance mob)
	{
		if (mob == null)
			return -1;
		
		if (mob == demon1)
		{
			if (portrait1 == null || portrait1.isDead())
				return -1;
			else if (_respawningDemon1)
				return 1;
			
			_respawningDemon1 = true;
		}
		
		else if (mob == demon2)
		{
			if (portrait2 == null || portrait2.isDead())
				return -1;
			else if (_respawningDemon2)
				return 2;
			
			_respawningDemon2 = true;
		}
		
		else if (mob == demon3)
		{
			if (portrait3 == null || portrait3.isDead())
				return -1;
			else if (_respawningDemon3)
				return 3;
			
			_respawningDemon3 = true;
		}
		
		else if (mob == demon4)
		{
			if (portrait4 == null || portrait4.isDead())
				return -1;
			else if (_respawningDemon4)
				return 4;
			
			_respawningDemon4 = true;
		}
		
		respawnDemon r = new respawnDemon(mob);
		Future<?> task = ThreadPoolManager.getInstance().scheduleEffect(r, _intervalOfDemons);
		r.setTask(task);
		
		return -1;
	}
	
	/**
	 * Class respawns a demon if it's portrait is not dead.
	 * 
	 * @author Darki699
	 */
	
	private class respawnDemon implements Runnable
	{
		
		private Future<?>			_task;
		
		private L2MonsterInstance	_mob;
		
		public respawnDemon(L2MonsterInstance mob)
		{
			_mob = mob;
		}
		
		public void setTask(Future<?> task)
		{
			_task = task;
		}
		
		public void run()
		{
			_task = cancelTask(_task);
			
			int demonId = checkRespawnTime(_mob);
			
			switch (demonId)
			{
				case -1: // If it's portrait is dead do not respawn the demon.
					try
					{
						_mob.decayMe();
						_mob.deleteMe();
					}
					catch (Throwable t)
					{
					}
					
					return;
					
				case 1: // Demon #1 is respawned
					_mob.getPosition().setXYZ(demonSpawn1.getLocx(), demonSpawn1.getLocy(), demonSpawn1.getLocz());
					_respawningDemon1 = false;
					break;
				
				case 2: // Demon #2 is respawned
					_mob.getPosition().setXYZ(demonSpawn2.getLocx(), demonSpawn2.getLocy(), demonSpawn2.getLocz());
					_respawningDemon2 = false;
					break;
				
				case 3: // Demon #3 is respawned
					_mob.getPosition().setXYZ(demonSpawn3.getLocx(), demonSpawn3.getLocy(), demonSpawn3.getLocz());
					_respawningDemon3 = false;
					break;
				
				case 4: // Demon #4 is respawned
					_mob.getPosition().setXYZ(demonSpawn4.getLocx(), demonSpawn4.getLocy(), demonSpawn4.getLocz());
					_respawningDemon4 = false;
					break;
			}
			
			_mob.doRevive();
			_mob.getStatus().setCurrentHp(_mob.getMaxHp());
			_mob.getStatus().setCurrentMp(_mob.getMaxMp());
			_mob.spawnMe();
			
			updateKnownList(_mob);
			
			L2Character target = _playersInLair.get(Rnd.get(_playersInLair.size()));
			_mob.setTarget(target);
			
			L2CharPosition pos = new L2CharPosition(target.getX(), target.getY(), target.getZ(), 0);
			new MoveToPos(_mob, pos);
		}
	}
	
	/**
	 * Listens to the weaker Scarlet Van Halisha HP status. If it's weakened enough, we spawn the Stronger version of Scarlet and delete the weaker one.
	 */
	
	public void weakScarletHpListener()
	{
		
		if (weakScarlet == null)
			return;
		
		double curHp = weakScarlet.getStatus().getCurrentHp(), maxHp = weakScarlet.getMaxHp();
		
		if (_scarletIsWeakest)
		{
			// Morph Scarlet Van Halisha into a Stronger one ;]
			if (curHp < maxHp * 2 / 3)
			{
				doSecondMorph();
			}
		}
		
		else
		{
			if (curHp < maxHp * 1 / 3)
			{
				// Do 3rd Morph, Scarlet Van Halisha now changes templates
				doThirdMorph();
			}
		}
	}
	
	/**
	 * Does the 3rd and last polymorph for Scarlet Van Halisha. Now he looks entirely different... (he is different)
	 */
	
	private void doThirdMorph()
	{
		
		setIdle(weakScarlet);
		
		weakScarlet.setIsInSocialAction(true);
		
		// animation
		weakScarlet.getPoly().setPolyInfo("npc", "29047");
		MagicSkillUse msk = new MagicSkillUse(weakScarlet, 1008, 1, 4000, 0);
		weakScarlet.broadcastPacket(msk);
		SetupGauge sg = new SetupGauge(0, 4000);
		weakScarlet.sendPacket(sg);
		
		// set Strong Scarlet's position and heading
		scarletSpawnStrong.setLocx(weakScarlet.getX());
		scarletSpawnStrong.setLocy(weakScarlet.getY());
		scarletSpawnStrong.setLocz(weakScarlet.getZ());
		scarletSpawnStrong.setHeading(weakScarlet.getHeading());
		scarletSpawnStrong.stopRespawn();
		
		// spawn Strong Scarlet and set his HP to the weaker version:
		strongScarlet = (L2BossInstance) scarletSpawnStrong.doSpawn();
		double newHp = weakScarlet.getStatus().getCurrentHp();
		strongScarlet.getStatus().setCurrentHp(newHp);
		
		// Immobilize Strong Scarlet
		setIdle(strongScarlet);
		strongScarlet.setIsInSocialAction(true);
		
		// do a social action "hello" ;]
		SocialAction sa = new SocialAction(strongScarlet.getObjectId(), 2);
		strongScarlet.broadcastPacket(sa);
		
		// update his knownlist
		updateKnownList(strongScarlet);
		
		// update his target
		strongScarlet.setTarget(weakScarlet.getTarget());
		
		// restore the original weapon into the template
		//weakScarlet.getTemplate().setRhand(8204);
		
		// get weakScarlet's list of attackers (or players that targeted it).
		boolean[] targeted = getTargeted(weakScarlet);
		
		// set the list of players to target strongScarlet
		setTargeted(strongScarlet, targeted);
		
		// delete the weakScarlet from the world
		weakScarlet.decayMe();
		weakScarlet.deleteMe();
		weakScarlet = null;
		
		// add Attack Listener
		attackerListener al = new attackerListener(strongScarlet, 150);
		Future<?> task = ThreadPoolManager.getInstance().scheduleGeneral(al, Rnd.get(4000) + 1000);
		al.setTask(task);
		
		// add retarget Listener
		ReTarget _retarget = new ReTarget(strongScarlet);
		Future<?> _reTargetTask = ThreadPoolManager.getInstance().scheduleEffect(_retarget, _intervalOfRetarget);
		_retarget.setTask(_reTargetTask);
		
		// mobilize Strong Scarlet
		SetMobilised mobilise = new SetMobilised(strongScarlet);
		Future<?> _mobiliseTask = ThreadPoolManager.getInstance().scheduleEffect(mobilise, 4000);
		mobilise.setTask(_mobiliseTask);
		
		// set teleport speed
		L2Skill skill = SkillTable.getInstance().getInfo(1086, 1);
		doSkill ds = new doSkill(strongScarlet, skill, _intervalOfRetarget, 300);
		Future<?> _doSkillTask = ThreadPoolManager.getInstance().scheduleEffect(ds, 4016);
		ds.setTask(_doSkillTask);
	}
	
	/**
	 * Receives a target and a list of players in _playersInLair that should target this target
	 * 
	 * @param target
	 *            L2Character
	 * @param targeted
	 *            boolean[]
	 * @return void
	 */
	
	private void setTargeted(L2Character target, boolean[] targeted)
	{
		int count = 0;
		
		// Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
		StatusUpdate su = new StatusUpdate(target.getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP, (int) target.getStatus().getCurrentHp());
		su.addAttribute(StatusUpdate.MAX_HP, target.getMaxHp());
		
		// set the target again on the players that targeted this _caster
		for (L2PcInstance pc : _playersInLair)
		{
			if (pc != null && targeted[count])
			{
				pc.setTarget(target);
				MyTargetSelected my = new MyTargetSelected(target.getObjectId(), pc.getLevel() - target.getLevel());
				pc.sendPacket(my);
				
				// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
				pc.sendPacket(su);
			}
			count++;
		}
	}
	
	/**
	 * Receives a target and returns the _playersInLair that target this target
	 * 
	 * @param target
	 *            L2Object
	 * @return boolean[] targeted players (true = target , false = not target)
	 */
	
	private boolean[] getTargeted(L2Object target)
	{
		boolean[] targeted = new boolean[_playersInLair.size()];
		int count = 0;
		
		// get the players that targeted this _caster
		for (L2PcInstance pc : _playersInLair)
		{
			if (pc == null || (pc != null && pc.getTarget() != target))
				targeted[count] = false;
			else
				targeted[count] = true;
			count++;
		}
		return targeted;
	}
	
	/**
	 * Sets a L2Character to idle state. Disables all skills, aborts attack and cast, immoblizies
	 * 
	 * @param target
	 *            L2Character
	 */
	public void setIdle(L2Character target)
	{
		
		target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		target.abortAttack();
		target.abortCast();
		target.setIsImobilised(true);
		target.disableAllSkills();
	}
	
	/**
	 * Does the 2nd Morph for Scarlet Van Halisha. Now he's bigger and he teleports to his targets
	 */
	
	private void doSecondMorph()
	{
		_scarletIsWeakest = false;
		
		//weakScarlet.getTemplate().setRhand(7903);
		
		L2Spawn scarletSpawnTemp = createNewSpawn(29046, weakScarlet.getX(), weakScarlet.getY(), weakScarlet.getZ(), weakScarlet.getHeading(), _intervalOfBoss);
		
		L2BossInstance tempScarlet = (L2BossInstance) scarletSpawnTemp.doSpawn();
		tempScarlet.getStatus().setCurrentHp(weakScarlet.getStatus().getCurrentHp());
		tempScarlet.setTarget(weakScarlet.getTarget());
		boolean[] targeted = getTargeted(weakScarlet);
		
		weakScarlet.decayMe();
		weakScarlet.deleteMe();
		weakScarlet = tempScarlet;
		
		setTargeted(weakScarlet, targeted);
		
		setIdle(weakScarlet);
		
		weakScarlet.setIsInSocialAction(true);
		
		SocialAction sa = new SocialAction(weakScarlet.getObjectId(), 2);
		weakScarlet.broadcastPacket(sa);
		
		// showSocialActionMovie (weakScarlet , 140, 90, 3, 0, 1000, 1);
		// showSocialActionMovie (weakScarlet , 120, 80, 3, 0, 1000, 2);
		
		// add a NEW Attack Listener
		attackerListener al = new attackerListener(weakScarlet, 125);
		Future task = ThreadPoolManager.getInstance().scheduleGeneral(al, Rnd.get(4000) + 1000);
		al.setTask(task);
		
		// add a NEW retarget Listener
		ReTarget _retarget = new ReTarget(weakScarlet);
		Future _reTargetTask = ThreadPoolManager.getInstance().scheduleEffect(_retarget, _intervalOfRetarget * 2 / 3);
		_retarget.setTask(_reTargetTask);
		
		// start teleporting fast
		L2Skill skill = SkillTable.getInstance().getInfo(1086, 1);
		doSkill ds = new doSkill(weakScarlet, skill, _intervalOfRetarget, 200);
		Future _doSkillTask = ThreadPoolManager.getInstance().scheduleEffect(ds, 50);
		ds.setTask(_doSkillTask);
		
		skill = SkillTable.getInstance().getInfo(1068, 3);
		weakScarlet.callSkill(skill, new L2Object[] { weakScarlet });
		
		SetMobilised mobilise = new SetMobilised(weakScarlet);
		Future _mobiliseTask = ThreadPoolManager.getInstance().scheduleEffect(mobilise, 1100);
		mobilise.setTask(_mobiliseTask);
		
		// reset camera.
		for (L2PcInstance pc : _playersInLair)
		{
			pc.leaveMovieMode();
			pc.enableAllSkills();
		}
	}
	
	/**
	 * Starts the skill effects for Scarlet Van Halisha. He moves like the wind ;] Continuous skills cast at _intervalOfRetarget
	 * 
	 * @author Darki699
	 */
	
	private class doSkill implements Runnable
	{
		private L2Character	_caster;
		private Future		_task;
		private L2Skill		_skill;
		private int			_interval, _range;
		
		/**
		 * Shows skill animation effect and teleports to the target if it's out of range
		 * 
		 * @param caster -
		 *            the monster
		 * @param skill -
		 *            the skill to animate
		 * @param interval -
		 *            the time between leaps
		 * @param range -
		 *            the range minimum to teleport
		 */
		
		public doSkill(L2Character caster, L2Skill skill, int interval, int range)
		{
			_caster = caster;
			_skill = skill;
			_interval = interval;
			_range = range;
		}
		
		public void setTask(Future task)
		{
			_task = task;
		}
		
		public void run()
		{
			// removes the task that called this process
			_task = cancelTask(_task);
			
			if (_caster == null || _caster.isDead())
				return;
			
			try
			{
				_caster.enableAllSkills();
				
				L2Object tempTarget = _caster.getTarget();
				
				if (tempTarget == null || !(tempTarget instanceof L2Character))
					tempTarget = _caster;
				
				int x = ((L2Character) tempTarget).getX() + Rnd.get(_range) - _range / 2, y = ((L2Character) tempTarget).getY() + Rnd.get(_range) - _range / 2, z = ((L2Character) tempTarget)
						.getZ();
				
				if (!_caster.isInsideRadius(x, y, _range, false) && tempTarget instanceof L2PcInstance && checkIfInZone((L2PcInstance) tempTarget))

				{
					
					// Returns a list of the players that targeted the _caster
					boolean[] targeted = getTargeted(_caster);
					
					_caster.broadcastPacket(new MagicSkillUse(_caster, ((L2Character) tempTarget), _skill.getId(), _skill.getLevel(), 0, 0), 10000);
					_caster.decayMe();
					_caster.getPosition().setXYZ(x, y, z);
					_caster.spawnMe(x, y, z);
					_caster.setTarget(tempTarget);
					
					// retarget all the players that targeted this _caster
					setTargeted(_caster, targeted);
					
				}
			}
			catch (Throwable t)
			{
			}
			
			doSkill ds = new doSkill(_caster, _skill, _interval, _range);
			_task = ThreadPoolManager.getInstance().scheduleEffect(ds, _interval + Rnd.get(500));
			ds.setTask(_task);
		}
	}
	
	/** * Re-Target Class to update a monster's known list and to re-target it again at an interval ** */
	
	private class ReTarget implements Runnable
	{
		
		private L2NpcInstance	_mob;
		private Future			_task;
		
		public ReTarget(L2NpcInstance mob)
		{
			_mob = mob;
		}
		
		public void setTask(Future task)
		{
			_task = task;
		}
		
		public void run()
		{
			// removes the task that called this process
			_task = cancelTask(_task);
			
			if (_mob == null || _mob.isDead() || _playersInLair == null || _playersInLair.isEmpty())
			{
				if (bossesAreDead() || _playersInLair == null || _playersInLair.isEmpty())
				{
					// Unspawn in 20 seconds...
					doUnspawn();
					
				}
				return;
			}
			_mob.setTarget(null);
			
			while (_mob.getTarget() != null)
			{
				try
				{
					int index = Rnd.get(_playersInLair.size());
					
					if (_playersInLair.get(index) == null)
						_playersInLair.remove(index);
					
					_mob.setTarget(_playersInLair.get(index));
				}
				catch (Throwable t)
				{
					_mob.setTarget(null);
				}
			}
			
			ReTarget retarget = new ReTarget(_mob);
			_task = ThreadPoolManager.getInstance().scheduleEffect(retarget, _intervalOfRetarget);
			retarget.setTask(_task);
		}
	}
	
	/** *********************************** End of re-target class *************************************** */
	
	/**
	 * starts the unspawn in 20 seconds.
	 */
	private void doUnspawn()
	{
		Unspawn unspawn = new Unspawn();
		Future unspawnTask = ThreadPoolManager.getInstance().scheduleGeneral(unspawn, 20000);
		unspawn.setTask(unspawnTask);
	}
	
	private class Unspawn implements Runnable
	{
		
		private Future	_task;
		
		public Unspawn()
		{
			// Nothing.
		}
		
		public void setTask(Future task)
		{
			_task = task;
		}
		
		public void run()
		{
			_task = cancelTask(_task);
			setUnspawn();
		}
	}
	
	/**
	 * Checks if Frintezza and Scarlet Van Halisha are <b>both</b> dead
	 * 
	 * @return boolean true if <b>all</b> bosses are dead.
	 */
	
	private boolean bossesAreDead()
	{
		if (weakScarlet == null && strongScarlet == null && frintezza == null)
			return true;
		
		int deadCount = 0;
		
		if ((weakScarlet != null && weakScarlet.isDead()) || weakScarlet == null)
			deadCount++;
		
		if ((strongScarlet != null && strongScarlet.isDead()) || strongScarlet == null)
			deadCount++;
		
		if ((frintezza != null && frintezza.isDead()) || frintezza == null)
			deadCount++;
		
		if (deadCount == 3) { return true; }
		
		return false;
	}
	
	private class ActivityTimeEnd implements Runnable
	{
		public ActivityTimeEnd()
		{ /* Nothing */
		}
		
		public void run()
		{
			setUnspawn();
		}
	}
	
	/**
	 * Clean Frintezza's lair.
	 */
	
	public void setUnspawn()
	{
		//NpcTable.getInstance().getTemplate(29046).setRhand(8204);
		
		// eliminate players.
		banishesPlayers();
		
		// delete monsters.
		bossDeadCheck(portrait1); // Deletes portrait and demon
		
		bossDeadCheck(portrait2); // Deletes portrait and demon
		
		bossDeadCheck(portrait3); // Deletes portrait and demon
		
		bossDeadCheck(portrait4); // Deletes portrait and demon
		
		try
		{
			if (frintezza != null)
			{
				frintezza.decayMe();
				frintezza.deleteMe();
			}
			
			if (strongScarlet != null)
			{
				strongScarlet.decayMe();
				strongScarlet.deleteMe();
			}
			
			if (weakScarlet != null)
			{
				weakScarlet.decayMe();
				weakScarlet.deleteMe();
			}
		}
		
		catch (Throwable t)
		{
		}
		
		frintezza = strongScarlet = weakScarlet = null;
		
		// delete spawns
		frintezzaSpawn = scarletSpawnWeak = scarletSpawnStrong =

		portraitSpawn1 = portraitSpawn2 = portraitSpawn3 = portraitSpawn4 =

		demonSpawn1 = demonSpawn2 = demonSpawn3 = demonSpawn4 = null;
		
		// not executed tasks are canceled.
		_monsterSpawnTask = cancelTask(_monsterSpawnTask);
		_activityTimeEndTask = cancelTask(_activityTimeEndTask);
		
		// init state of Frintezza's lair.
		_isBossSpawned = false;
		_isIntervalForNextSpawn = true;
		
		// interval begin.... Count until Frintezza is ready to respawn again.
		setInetrvalEndTask();
	}
	
	/**
	 * Creates a thread to initialize Frintezza again... until this loops ends, no one can enter the lair.
	 */
	public void setInetrvalEndTask()
	{
		IntervalEnd ie = new IntervalEnd();
		Future _intervalEndTask = ThreadPoolManager.getInstance().scheduleEffect(ie, _intervalOfBoss);
		ie.setTask(_intervalEndTask);
	}
	
	private class IntervalEnd implements Runnable
	{
		private Future	_task;
		
		public IntervalEnd()
		{ /* Nothing */
		}
		
		public void setTask(Future task)
		{
			_task = task;
		}
		
		public void run()
		{
			_isIntervalForNextSpawn = false;
			_task = cancelTask(_task);
			init();
		}
	}
	
	protected void updateKnownList(L2NpcInstance boss)
	{
		if (boss == null)
			return;
		
		boss.getKnownList().getKnownPlayers().clear();
		for (L2PcInstance pc : L2World.getInstance().getAllPlayers())
		{
			if (pc == null /* || pc.isInvul() || pc.getAppearance().getInvisible() */)
				continue;
			
			// If the player is in the Frintezza lair:
			else if (BossZoneManager.getInstance().checkIfInZone(_ZoneType, pc))
			{
				// add the player to the list
				if (!_playersInLair.contains(pc))
					_playersInLair.add(pc);
				
				// add the player to the mob known list
				if (!boss.getKnownList().getKnownPlayers().containsValue(pc))
					boss.getKnownList().getKnownPlayers().put(pc.getObjectId(), pc);
				
				// add the mob to the player's known list
				if (!pc.getKnownList().getKnownCharacters().contains(boss))
					pc.getKnownList().getKnownCharacters().add(boss);
			}
		}
		
		for (L2PcInstance pc : _playersInLair)
		{
			if (!BossZoneManager.getInstance().checkIfInZone(_ZoneType, pc))
				_playersInLair.remove(pc);
		}
	}
	
	private class SetMobilised implements Runnable
	{
		private L2BossInstance	_boss;
		private Future			_task;
		
		public SetMobilised(L2BossInstance boss)
		{
			_boss = boss;
		}
		
		public void setTask(Future task)
		{
			_task = task;
		}
		
		public void run()
		{
			_task = cancelTask(_task);
			_boss.setIsImobilised(false);
			_boss.setIsInSocialAction(false);
		}
	}
	
	private class MoveToPos implements Runnable
	{
		private L2NpcInstance	_npc;
		L2CharPosition			_pos;
		
		public MoveToPos(L2NpcInstance npc, L2CharPosition pos)
		{
			_npc = npc;
			_pos = pos;
		}
		
		public void run()
		{
			_npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, _pos);
		}
	}
}
