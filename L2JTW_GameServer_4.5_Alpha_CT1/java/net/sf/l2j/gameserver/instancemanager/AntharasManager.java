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

import java.util.logging.Logger;
import java.util.concurrent.ScheduledFuture;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2BossInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.entity.GrandBossState;

/**
 * 
 * This class ...
 * control for sequence of fight against Antharas.
 * @version $Revision: $ $Date: $
 * @author  L2J_JP SANDMAN
 */
public class AntharasManager
{
    protected static Logger _log = Logger.getLogger(AntharasManager.class.getName());
    private static AntharasManager _instance = new AntharasManager();

    // location of teleport cube.
    private final int _TeleportCubeId = 31859;
    private final int _TeleportCubeLocation[][] =
    	{
    		{177615, 114941, -7709,0}
    	};
    protected List<L2Spawn> _TeleportCubeSpawn = new FastList<L2Spawn>();
    protected List<L2NpcInstance> _TeleportCube = new FastList<L2NpcInstance>();

    // list of intruders.
    protected List<L2PcInstance> _PlayersInLair = new FastList<L2PcInstance>();

    // spawn data of monsters.
    protected Map<Integer,L2Spawn> _MonsterSpawn = new FastMap<Integer,L2Spawn>();

    // instance of monsters.
    protected List<L2NpcInstance> _Monsters = new FastList<L2NpcInstance>();
    
    // tasks.
    protected ScheduledFuture _CubeSpawnTask = null;
    protected ScheduledFuture _MonsterSpawnTask = null;
    protected ScheduledFuture _IntervalEndTask = null;
    protected ScheduledFuture _ActivityTimeEndTask = null;
    protected ScheduledFuture _OnPlayersAnnihilatedTask = null;
    protected ScheduledFuture _SocialTask = null;
    protected ScheduledFuture _MobiliseTask = null;
    protected ScheduledFuture _BehemothSpawnTask = null;
    protected ScheduledFuture _BomberSpawnTask = null;
    protected ScheduledFuture _SelfDestructionTask = null;
    protected ScheduledFuture _MoveAtRandomTask = null;
    protected ScheduledFuture _MovieTsak = null;
    
    // status in lair.
    protected GrandBossState _State = new GrandBossState(29019);
    protected String _ZoneType;
    protected String _QuestName;
    
    // location of banishment
    private final int _BanishmentLocation[][] =
    	{
    		{79959, 151774, -3532},
    		{81398, 148055, -3468},
    		{82286, 149113, -3468},
    		{84264, 147427, -3404}
		};
    
    public AntharasManager()
    {
    }

    public static AntharasManager getInstance()
    {
        if (_instance == null) _instance = new AntharasManager();

        return _instance;
    }

    // initialize
    public void init()
    {
    	// initialize status in lair.
    	_PlayersInLair.clear();
        _ZoneType = "LairofAntharas";
        _QuestName = "antharas";

        // setting spawn data of monsters.
        try
        {
            L2NpcTemplate template1;
            L2Spawn tempSpawn;
            
            // old Antharas.
            template1 = NpcTable.getInstance().getTemplate(29019);
            tempSpawn = new L2Spawn(template1);
            tempSpawn.setLocx(181323);
            tempSpawn.setLocy(114850);
            tempSpawn.setLocz(-7623);
            tempSpawn.setHeading(32542);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(Config.FWA_ACTIVITYTIMEOFANTHARAS * 2);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _MonsterSpawn.put(29019, tempSpawn);
            
            // weak Antharas.
            template1 = NpcTable.getInstance().getTemplate(29066);
            tempSpawn = new L2Spawn(template1);
            tempSpawn.setLocx(181323);
            tempSpawn.setLocy(114850);
            tempSpawn.setLocz(-7623);
            tempSpawn.setHeading(32542);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(Config.FWA_ACTIVITYTIMEOFANTHARAS * 2);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _MonsterSpawn.put(29066, tempSpawn);
            
            // normal Antharas.
            template1 = NpcTable.getInstance().getTemplate(29067);
            tempSpawn = new L2Spawn(template1);
            tempSpawn.setLocx(181323);
            tempSpawn.setLocy(114850);
            tempSpawn.setLocz(-7623);
            tempSpawn.setHeading(32542);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(Config.FWA_ACTIVITYTIMEOFANTHARAS * 2);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _MonsterSpawn.put(29067, tempSpawn);
            
            // strong Antharas.
            template1 = NpcTable.getInstance().getTemplate(29068);
            tempSpawn = new L2Spawn(template1);
            tempSpawn.setLocx(181323);
            tempSpawn.setLocy(114850);
            tempSpawn.setLocz(-7623);
            tempSpawn.setHeading(32542);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(Config.FWA_ACTIVITYTIMEOFANTHARAS * 2);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _MonsterSpawn.put(29068, tempSpawn);
        }
        catch (Exception e)
        {
            _log.warning(e.getMessage());
        }

        // setting spawn data of teleport cube.
        try
        {
            L2NpcTemplate Cube = NpcTable.getInstance().getTemplate(_TeleportCubeId);
            L2Spawn spawnDat;
            for(int i = 0;i < _TeleportCubeLocation.length; i++)
            {
                spawnDat = new L2Spawn(Cube);
                spawnDat.setAmount(1);
                spawnDat.setLocx(_TeleportCubeLocation[i][0]);
                spawnDat.setLocy(_TeleportCubeLocation[i][1]);
                spawnDat.setLocz(_TeleportCubeLocation[i][2]);
                spawnDat.setHeading(_TeleportCubeLocation[i][3]);
                spawnDat.setRespawnDelay(60);
                spawnDat.setLocation(0);
                SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                _TeleportCubeSpawn.add(spawnDat);
            }
        }
        catch (Exception e)
        {
            _log.warning(e.getMessage());
        }
        
        _log.info("AntharasManager : State of Antharas is " + _State.getState() + ".");
        if (!_State.getState().equals(GrandBossState.StateEnum.NOTSPAWN))
        	setInetrvalEndTask();
        
		Date dt = new Date(_State.getRespawnDate());
        _log.info("AntharasManager : Next spawn date of Antharas is " + dt + ".");
        _log.info("AntharasManager : Init AntharasManager.");
    }

    // return Antaras state.
    public GrandBossState.StateEnum getState()
    {
    	return _State.getState();
    }
    
    // return list of intruders.
    public List<L2PcInstance> getPlayersInLair()
	{
		return _PlayersInLair;
	}
    
    // Whether it lairs is confirmed. 
    public boolean isEnableEnterToLair()
    {
    	if(_State.getState().equals(GrandBossState.StateEnum.NOTSPAWN))
    	{
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }

    // update list of intruders.
    public void addPlayerToLair(L2PcInstance pc)
    {
        if (!_PlayersInLair.contains(pc)) _PlayersInLair.add(pc);
    }
    
    // Whether the players was annihilated is confirmed. 
    public synchronized boolean isPlayersAnnihilated()
    {
    	for (L2PcInstance pc : _PlayersInLair)
		{
			// player is must be alive and stay inside of lair.
			if (!pc.isDead()
					&& CustomZoneManager.getInstance().checkIfInZone(_ZoneType, pc))
			{
				return false;
			}
		}
		return true;
    }

    // banishes players from lair.
    public void banishesPlayers()
    {
    	for(L2PcInstance pc : _PlayersInLair)
    	{
    		if(pc.getQuestState(_QuestName) != null) pc.getQuestState(_QuestName).exitQuest(true);
    		if(CustomZoneManager.getInstance().checkIfInZone(_ZoneType, pc))
    		{
        		int driftX = Rnd.get(-80,80);
        		int driftY = Rnd.get(-80,80);
        		int loc = Rnd.get(4);
        		pc.teleToLocation(_BanishmentLocation[loc][0] + driftX,_BanishmentLocation[loc][1] + driftY,_BanishmentLocation[loc][2]);
    		}
    	}
    	_PlayersInLair.clear();
    }
    
    // do spawn teleport cube.
    public void spawnCube()
    {
		if(_BehemothSpawnTask != null)
		{
			_BehemothSpawnTask.cancel(true);
			_BehemothSpawnTask = null;
		}
		if(_BomberSpawnTask != null)
		{
			_BomberSpawnTask.cancel(true);
			_BomberSpawnTask = null;
		}
		if(_SelfDestructionTask != null)
		{
			_SelfDestructionTask.cancel(true);
			_SelfDestructionTask = null;
		}
		for (L2Spawn spawnDat : _TeleportCubeSpawn)
		{
			_TeleportCube.add(spawnDat.doSpawn());
		}
		
    }
    
	// When the party is annihilated, they are banished.
    public void checkAnnihilated()
    {
    	if(isPlayersAnnihilated())
    	{
    		_OnPlayersAnnihilatedTask =
				ThreadPoolManager.getInstance().scheduleGeneral(new OnPlayersAnnihilatedTask(),5000);    			
    	}
    }

	// When the party is annihilated, they are banished.
	private class OnPlayersAnnihilatedTask implements Runnable
	{
		public OnPlayersAnnihilatedTask()
		{
		}
		
		public void run()
		{
		    // banishes players from lair.
			banishesPlayers();
		}
	}

    // setting Antharas spawn task.
    public void setAntharasSpawnTask()
    {
    	// When someone has already invaded the lair, nothing is done.
    	if (_PlayersInLair.size() >= 1) return;

    	if (_MonsterSpawnTask == null)
        {
   			_MonsterSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(1,null),Config.FWA_APPTIMEOFANTHARAS);
        }
    }
    
    // do spawn Antharas.
    private class AntharasSpawn implements Runnable
	{
    	int _distance = 6502500;
    	int _taskId = 0;
		L2BossInstance _antharas = null;

		AntharasSpawn(int taskId,L2BossInstance antharas)
		{
			_taskId = taskId;
			_antharas = antharas;
		}

		public void run()
		{
			int npcId;
			L2Spawn antharasSpawn = null;
			SocialAction sa = null;

			switch (_taskId)
			{
				case 1: // spawn.
					// Strength of Antharas is decided by the number of players that
					// invaded the lair.
					if (Config.FWA_OLDANTHARAS)
						npcId = 29019; // old
					else if (_PlayersInLair.size() <= Config.FWA_LIMITOFWEAK)
						npcId = 29066; // weak
					else if (_PlayersInLair.size() >= Config.FWA_LIMITOFNORMAL)
						npcId = 29068; // strong
					else
						npcId = 29067; // normal
	
					// do spawn.
					antharasSpawn = _MonsterSpawn.get(npcId);
					_antharas = (L2BossInstance) antharasSpawn.doSpawn();
					_Monsters.add(_antharas);
					_antharas.setIsImobilised(true);
					_antharas.setIsInSocialAction(true);

					_State.setRespawnDate(
			    			Rnd.get(Config.FWA_FIXINTERVALOFANTHARAS,Config.FWA_FIXINTERVALOFANTHARAS + Config.FWA_RANDOMINTERVALOFANTHARAS)
			    			+ Config.FWA_ACTIVITYTIMEOFANTHARAS);
			    	_State.setState(GrandBossState.StateEnum.ALIVE);
					_State.update();
	
					// set KnownList
					updateKnownList(_antharas);
	
					// setting 1st time of minions spawn task.
					if (!Config.FWA_OLDANTHARAS)
					{
						int intervalOfBehemoth;
						int intervalOfBomber;
	
						// Interval of minions is decided by the number of players
						// that invaded the lair.
						if (_PlayersInLair.size() <= Config.FWA_LIMITOFWEAK) // weak
						{
							intervalOfBehemoth = Config.FWA_INTERVALOFBEHEMOTHONWEAK;
							intervalOfBomber = Config.FWA_INTERVALOFBOMBERONWEAK;
						} else if (_PlayersInLair.size() >= Config.FWA_LIMITOFNORMAL) // strong
						{
							intervalOfBehemoth = Config.FWA_INTERVALOFBEHEMOTHONSTRONG;
							intervalOfBomber = Config.FWA_INTERVALOFBOMBERONSTRONG;
						} else
						// normal
						{
							intervalOfBehemoth = Config.FWA_INTERVALOFBEHEMOTHONNORMAL;
							intervalOfBomber = Config.FWA_INTERVALOFBOMBERONNORMAL;
						}
	
						// spawn Behemoth.
						_BehemothSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new BehemothSpawn(intervalOfBehemoth),30000);
	
						// spawn Bomber.
						_BomberSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new BomberSpawn(intervalOfBomber),30000);
					}
	
					// set next task.
		            if(_SocialTask != null)
		            {
		            	_SocialTask.cancel(true);
		            	_SocialTask = null;
		            }
					_SocialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(2,_antharas), 16);
	
					break;
	
				case 2:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_antharas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 700, 13, -19, 0, 10000);
						} else
						{
							pc.leaveMovieMode();
						}
					}
	
					// set next task.
		            if(_SocialTask != null)
		            {
		            	_SocialTask.cancel(true);
		            	_SocialTask = null;
		            }
					_SocialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(3,_antharas), 3000);
	
					break;
	
				case 3:
					// do social.
					sa = new SocialAction(_antharas.getObjectId(), 1);
					_antharas.broadcastPacket(sa);
	
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_antharas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 700, 13, 0, 6000, 10000);
						} else
						{
							pc.leaveMovieMode();
						}
					}
	
					// set next task.
		            if(_SocialTask != null)
		            {
		            	_SocialTask.cancel(true);
		            	_SocialTask = null;
		            }
					_SocialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(4,_antharas), 10000);
	
					break;
	
				case 4:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_antharas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 3800, 0, -3, 0, 10000);
						} else
						{
							pc.leaveMovieMode();
						}
					}
	
					// set next task.
		            if(_SocialTask != null)
		            {
		            	_SocialTask.cancel(true);
		            	_SocialTask = null;
		            }
					_SocialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(5,_antharas), 200);
	
					break;
	
				case 5:
					// do social.
					sa = new SocialAction(_antharas.getObjectId(), 2);
					_antharas.broadcastPacket(sa);
	
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_antharas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 1200, 0, -3, 22000, 11000);
						} else
						{
							pc.leaveMovieMode();
						}
					}
	
					// set next task.
		            if(_SocialTask != null)
		            {
		            	_SocialTask.cancel(true);
		            	_SocialTask = null;
		            }
					_SocialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(6,_antharas), 10800);
	
					break;
	
				case 6:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_antharas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 1200, 0, -3, 300, 2000);
						} else
						{
							pc.leaveMovieMode();
						}
					}
	
					// set next task.
		            if(_SocialTask != null)
		            {
		            	_SocialTask.cancel(true);
		            	_SocialTask = null;
		            }
					_SocialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(7,_antharas), 1900);
	
					break;
	
				case 7:
					_antharas.abortCast();		
					// reset camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						pc.leaveMovieMode();
					}

					_MobiliseTask = ThreadPoolManager.getInstance().scheduleGeneral(new SetMobilised(_antharas), 16);
	
					// move at random.
					if (Config.FWA_MOVEATRANDOM)
					{
						L2CharPosition pos = new L2CharPosition(Rnd.get(175000,	178500), Rnd.get(112400, 116000), -7707, 0);
						_MoveAtRandomTask = ThreadPoolManager.getInstance().scheduleGeneral(new MoveAtRandom(_antharas, pos),
										32);
					}
	
					// set delete task.
					_ActivityTimeEndTask = ThreadPoolManager.getInstance().scheduleGeneral(new ActivityTimeEnd(),Config.FWA_ACTIVITYTIMEOFANTHARAS);
	
		            if(_SocialTask != null)
		            {
		            	_SocialTask.cancel(true);
		            	_SocialTask = null;
		            }
					break;
			}
		}
	}

    // do spawn Behemoth.
    private class BehemothSpawn implements Runnable
    {
    	private int _interval;
    	
    	public BehemothSpawn(int interval)
    	{
    		_interval = interval;
    	}
    	
    	public void run()
    	{
            L2NpcTemplate template1;
            L2Spawn tempSpawn;

            try
            {
            	// set spawn.
                template1 = NpcTable.getInstance().getTemplate(29069);
                tempSpawn = new L2Spawn(template1);
                // allocates it at random in the lair of Antharas.
                tempSpawn.setLocx(Rnd.get(175000, 179900));
                tempSpawn.setLocy(Rnd.get(112400, 116000));
                tempSpawn.setLocz(-7709);
                tempSpawn.setHeading(0);
                tempSpawn.setAmount(1);
                tempSpawn.setRespawnDelay(Config.FWA_ACTIVITYTIMEOFANTHARAS * 2);
                SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
                
        		// do spawn.
            	_Monsters.add(tempSpawn.doSpawn());

            }
            catch (Exception e)
            {
                _log.warning(e.getMessage());
            }
            
            if(_BehemothSpawnTask != null)
            {
            	_BehemothSpawnTask.cancel(true);
            	_BehemothSpawnTask = null;
            }
            
            // repeat.
        	_BehemothSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(
            		new BehemothSpawn(_interval),_interval);

    	}
    }
    
    // do spawn Bomber.
    private class BomberSpawn implements Runnable
    {
    	private int _interval;
    	
    	public BomberSpawn(int interval)
    	{
    		_interval = interval;
    	}

    	public void run()
    	{
    		int npcId = Rnd.get(29070, 29076);
            L2NpcTemplate template1;
            L2Spawn tempSpawn;
            L2NpcInstance bomber = null;
            
            try
            {
            	// set spawn.
                template1 = NpcTable.getInstance().getTemplate(npcId);
                tempSpawn = new L2Spawn(template1);
                // allocates it at random in the lair of Antharas. 
                tempSpawn.setLocx(Rnd.get(175000, 179900));
                tempSpawn.setLocy(Rnd.get(112400, 116000));
                tempSpawn.setLocz(-7709);
                tempSpawn.setHeading(0);
                tempSpawn.setAmount(1);
                tempSpawn.setRespawnDelay(Config.FWA_ACTIVITYTIMEOFANTHARAS * 2);
                SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
                
        		// do spawn.
                bomber = tempSpawn.doSpawn();
            	_Monsters.add(bomber);

            }
            catch (Exception e)
            {
                _log.warning(e.getMessage());
            }
            
            // set self destruction.
            if(bomber != null)
            {
                _SelfDestructionTask = ThreadPoolManager.getInstance().scheduleGeneral(
                		new SelfDestructionOfBomber(bomber),1000);
            }
            
            if(_BomberSpawnTask != null)
            {
            	_BomberSpawnTask.cancel(true);
            	_BomberSpawnTask = null;
            }

            // repeat.
            _BomberSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(
            		new BomberSpawn(_interval),_interval);

    	}
    }

    // do self destruction.
    private class SelfDestructionOfBomber implements Runnable
    {
    	L2NpcInstance _bomber;
    	public SelfDestructionOfBomber(L2NpcInstance bomber)
    	{
    		_bomber = bomber;
    	}
    	
    	public void run()
    	{
    		L2Skill skill = null;
    		switch (_bomber.getNpcId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
					skill = SkillTable.getInstance().getInfo(5097, 1);
					break;
				case 29076:
					skill = SkillTable.getInstance().getInfo(5094, 1);
					break;
				default:
					skill = null;
			}
    		
    		_bomber.doCast(skill);
    	}
    }
    
    // at end of activitiy time.
    private class ActivityTimeEnd implements Runnable
    {
    	public ActivityTimeEnd()
    	{
    	}
    	
    	public void run()
    	{
    		setUnspawn();
    	}
    }
    
    // clean Antharas's lair.
    public void setUnspawn()
	{
    	// eliminate players.
    	banishesPlayers();

    	// delete monsters.
    	for(L2NpcInstance mob : _Monsters)
    	{
    		mob.getSpawn().stopRespawn();
    		mob.deleteMe();
    	}
    	_Monsters.clear();
    	
    	// delete teleport cube.
		for (L2NpcInstance cube : _TeleportCube)
		{
			cube.getSpawn().stopRespawn();
			cube.deleteMe();
		}
		_TeleportCube.clear();
		
		// not executed tasks is canceled.
		if(_CubeSpawnTask != null)
		{
			_CubeSpawnTask.cancel(true);
			_CubeSpawnTask = null;
		}
		if(_MonsterSpawnTask != null)
		{
			_MonsterSpawnTask.cancel(true);
			_MonsterSpawnTask = null;
		}
		if(_IntervalEndTask != null)
		{
			_IntervalEndTask.cancel(true);
			_IntervalEndTask = null;
		}
		if(_ActivityTimeEndTask != null)
		{
			_ActivityTimeEndTask.cancel(true);
			_ActivityTimeEndTask = null;
		}
		if(_OnPlayersAnnihilatedTask != null)
		{
			_OnPlayersAnnihilatedTask.cancel(true);
			_OnPlayersAnnihilatedTask = null;
		}
		if(_SocialTask != null)
		{
			_SocialTask.cancel(true);
			_SocialTask = null;
		}
		if(_MobiliseTask != null)
		{
			_MobiliseTask.cancel(true);
			_MobiliseTask = null;
		}
		if(_BehemothSpawnTask != null)
		{
			_BehemothSpawnTask.cancel(true);
			_BehemothSpawnTask = null;
		}
		if(_BomberSpawnTask != null)
		{
			_BomberSpawnTask.cancel(true);
			_BomberSpawnTask = null;
		}
		if(_SelfDestructionTask != null)
		{
			_SelfDestructionTask.cancel(true);
			_SelfDestructionTask = null;
		}
		if(_MoveAtRandomTask != null)
		{
			_MoveAtRandomTask.cancel(true);
			_MoveAtRandomTask = null;
		}

		// interval begin.
		setInetrvalEndTask();
	}

    // start interval.
    public void setInetrvalEndTask()
    {
		// init state of Antharas's lair.
    	if (!_State.getState().equals(GrandBossState.StateEnum.INTERVAL))
    	{
    		_State.setRespawnDate(Rnd.get(Config.FWA_FIXINTERVALOFANTHARAS,Config.FWA_FIXINTERVALOFANTHARAS + Config.FWA_RANDOMINTERVALOFANTHARAS));
    		_State.setState(GrandBossState.StateEnum.INTERVAL);
    		_State.update();
    	}
		
    	_IntervalEndTask = ThreadPoolManager.getInstance().scheduleGeneral(
            	new IntervalEnd(),_State.getInterval());
    }

    // at end of interval.
    private class IntervalEnd implements Runnable
    {
    	public IntervalEnd()
    	{
    	}
    	
    	public void run()
    	{
    		_PlayersInLair.clear();
    		_State.setState(GrandBossState.StateEnum.NOTSPAWN);
    		_State.update();
    	}
    }
    
    // setting teleport cube spawn task.
    public void setCubeSpawn()
    {
    	_State.setState(GrandBossState.StateEnum.DEAD);
    	_State.update();

    	_CubeSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new CubeSpawn(),10000);
    }
    
    // update knownlist.
    protected void updateKnownList(L2NpcInstance boss)
    {
    	boss.getKnownList().getKnownPlayers().clear();
		for (L2PcInstance pc : _PlayersInLair)
		{
			boss.getKnownList().getKnownPlayers().put(pc.getObjectId(), pc);
		}
    }
    
    // do spawn teleport cube.
    private class CubeSpawn implements Runnable
    {
    	public CubeSpawn()
    	{
    	}
    	
        public void run()
        {
        	spawnCube();
        }
    }
    
    // action is enabled the boss.
    private class SetMobilised implements Runnable
    {
        private L2BossInstance _boss;
        public SetMobilised(L2BossInstance boss)
        {
        	_boss = boss;
        }

        public void run()
        {
        	_boss.setIsImobilised(false);
        	_boss.setIsInSocialAction(false);
            
            // When it is possible to act, a social action is canceled.
            if (_SocialTask != null)
            {
            	_SocialTask.cancel(true);
                _SocialTask = null;
            }
        }
    }
    
    // Move at random on after Antharas appears.
    private class MoveAtRandom implements Runnable
    {
    	private L2NpcInstance _npc;
    	L2CharPosition _pos;
    	
    	public MoveAtRandom(L2NpcInstance npc,L2CharPosition pos)
    	{
    		_npc = npc;
    		_pos = pos;
    	}
    	
    	public void run()
    	{
    		_npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,_pos);
    	}
    }
    
}
