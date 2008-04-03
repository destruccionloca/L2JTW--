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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.status.GrandBossStatus;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.util.Rnd;

/**
 *
 * This class ...
 * control for sequence of fight against Valakas.
 * @version $Revision: $ $Date: $
 * @author  L2J_JP SANDMAN
 */
public class ValakasManager
{
    protected static Logger _log = Logger.getLogger(ValakasManager.class.getName());
    private static ValakasManager _instance = new ValakasManager();

    // location of teleport cube.
    private final int _TeleportCubeId = 31759;
    private final int _TeleportCubeLocation[][] =
    	{
    		{214880, -116144, -1644, 0},
    		{213696, -116592, -1644, 0},
    		{212112, -116688, -1644, 0},
    		{211184, -115472, -1664, 0},
    		{210336, -114592, -1644, 0},
    		{211360, -113904, -1644, 0},
    		{213152, -112352, -1644, 0},
    		{214032, -113232, -1644, 0},
    		{214752, -114592, -1644, 0},
    		{209824, -115568, -1421, 0},
    		{210528, -112192, -1403, 0},
    		{213120, -111136, -1408, 0},
    		{215184, -111504, -1392, 0},
    		{215456, -117328, -1392, 0},
    		{213200, -118160, -1424, 0}
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
    protected ScheduledFuture<?> _CubeSpawnTask = null;
    protected ScheduledFuture<?> _MonsterSpawnTask = null;
    protected ScheduledFuture<?> _IntervalEndTask = null;
    protected ScheduledFuture<?> _ActivityTimeEndTask = null;
    protected ScheduledFuture<?> _OnPlayersAnnihilatedTask = null;
    protected ScheduledFuture<?> _SocialTask = null;
    protected ScheduledFuture<?> _MobiliseTask = null;
    protected ScheduledFuture<?> _MoveAtRandomTask = null;
    protected ScheduledFuture<?> _RespawnValakasTask = null;

    // status in lair.
    protected StatsSet _StateSet;
    protected String _ZoneType;
    protected String _QuestName;
    protected int _Alive;
    protected int _BossId = 29028;

    // location of banishment
    private final int _BanishmentLocation[][] =
    	{
    		{150604, -56283, -2980},
    		{144857, -56386, -2980},
    		{147696, -56845, -2780}
    	};

    public ValakasManager()
    {
    }

    public static ValakasManager getInstance()
    {
        if (_instance == null) _instance = new ValakasManager();

        return _instance;
    }

    // initialize
    public void init()
    {

    	// initialize status in lair.
    	_PlayersInLair.clear();
        _ZoneType = "LairofValakas";
        _QuestName = "valakas";
        _StateSet = GrandBossManager.getInstance().getStatsSet(_BossId);
        _Alive = GrandBossManager.getInstance().getBossStatus(_BossId);

        // setting spawn data of monsters.
        try
        {
            L2NpcTemplate template1;
            L2Spawn tempSpawn;

            // Valakas.
            template1 = NpcTable.getInstance().getTemplate(_BossId);
            tempSpawn = new L2Spawn(template1);
            tempSpawn.setLocx(212852);
            tempSpawn.setLocy(-114842);
            tempSpawn.setLocz(-1632);
            //tempSpawn.setHeading(22106);
            tempSpawn.setHeading(833);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(Config.FWV_ACTIVITYTIMEOFVALAKAS * 2);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _MonsterSpawn.put(_BossId, tempSpawn);

            // Dummy Valakas.
            template1 = NpcTable.getInstance().getTemplate(32123);
            tempSpawn = new L2Spawn(template1);
            tempSpawn.setLocx(212852);
            tempSpawn.setLocy(-114842);
            tempSpawn.setLocz(-1632);
            //tempSpawn.setHeading(22106);
            tempSpawn.setHeading(833);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(Config.FWV_ACTIVITYTIMEOFVALAKAS * 2);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _MonsterSpawn.put(32123, tempSpawn);
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

        _log.info("ValakasManager : State of Valakas is " + _Alive + ".");
        if (_Alive == GrandBossStatus.ALIVE)
        	restartValakas();
        else if (_Alive != GrandBossStatus.NOTSPAWN)
        	setInetrvalEndTask();

		Date dt = new Date(_StateSet.getLong("respawn_time"));
        _log.info("ValakasManager : Next spawn date of Valakas is " + dt + ".");
        _log.info("ValakasManager : Init ValakasManager.");
    }

    // return list of intruders.
    public List<L2PcInstance> getPlayersInLair()
	{
		return _PlayersInLair;
	}

    // Whether it lairs is confirmed.
    public boolean isEnableEnterToLair()
    {
    	if(_PlayersInLair.size() >= Config.FWV_CAPACITYOFLAIR) return false;

       	if(_Alive == GrandBossStatus.NOTSPAWN)
    		return true;
    	else
    		return false;
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
					&& GrandBossManager.getInstance().checkIfInZone(_ZoneType, pc))
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
    		if(GrandBossManager.getInstance().checkIfInZone(_ZoneType, pc))
    		{
        		int driftX = Rnd.get(-80,80);
        		int driftY = Rnd.get(-80,80);
        		int loc = Rnd.get(3);
        		pc.teleToLocation(_BanishmentLocation[loc][0] + driftX,_BanishmentLocation[loc][1] + driftY,_BanishmentLocation[loc][2]);
    		}
    	}
    	_PlayersInLair.clear();
    }

    // do spawn teleport cube.
    public void spawnCube()
    {
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

            // clean up task.
            if(_OnPlayersAnnihilatedTask != null)
            {
            	_OnPlayersAnnihilatedTask.cancel(true);
            	_OnPlayersAnnihilatedTask = null;
            }
		}
	}

    // setting Valakas spawn task.
    public void setValakasSpawnTask()
    {
    	// When someone has already invaded the lair, nothing is done.
    	if (_PlayersInLair.size() >= 1) return;

    	if (_MonsterSpawnTask == null)
        {
        	_MonsterSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(	new ValakasSpawn(1,null),Config.FWV_APPTIMEOFVALAKAS);
        }
    }

    // do spawn Valakas.
    private class ValakasSpawn implements Runnable
    {
    	int _distance = 6502500;
    	int _taskId;
    	L2GrandBossInstance _valakas = null;

    	ValakasSpawn(int taskId,L2GrandBossInstance valakas)
    	{
    		_taskId = taskId;
    		_valakas = valakas;
    	}

    	public void run()
    	{
    		SocialAction sa = null;

    		switch(_taskId)
    		{
	    		case 1:
	            	// do spawn.
	            	L2Spawn valakasSpawn = _MonsterSpawn.get(_BossId);
	            	_valakas = (L2GrandBossInstance)valakasSpawn.doSpawn();
	            	_Monsters.add(_valakas);
	            	_valakas.setIsImmobilized(true);
	            	_valakas.setIsInSocialAction(true);
	            	updateKnownList(_valakas);

	            	_StateSet.set("respawn_time", Calendar.getInstance().getTimeInMillis() + Rnd.get(Config.FWV_FIXINTERVALOFVALAKAS,Config.FWV_FIXINTERVALOFVALAKAS + Config.FWV_RANDOMINTERVALOFVALAKAS) + Config.FWV_ACTIVITYTIMEOFVALAKAS);
	            	_Alive = GrandBossStatus.ALIVE;
	            	GrandBossManager.getInstance().setBossStatus(_BossId, _Alive);
	            	GrandBossManager.getInstance().setStatsSet(_BossId, _StateSet);
	            	GrandBossManager.getInstance().save();
	                _log.info("ValakasManager : Spawn Valakas.");

					// set next task.
		            if(_SocialTask != null)
		            {
		            	_SocialTask.cancel(true);
		            	_SocialTask = null;
		            }
					_SocialTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(2,_valakas), 16);

					break;

	    		case 2:
	            	// do social.
	            	updateKnownList(_valakas);
	                sa = new SocialAction(_valakas.getObjectId(), 1);
	                _valakas.broadcastPacket(sa);

					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1800,180,-1,1500,15000);
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
					_SocialTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(3,_valakas), 1500);

					break;

	    		case 3:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1300,180,-5,3000,15000);
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
					_SocialTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(4,_valakas), 3300);

					break;

	    		case 4:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 500,180,-8,600,15000);
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
					_SocialTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(5,_valakas), 1300);

					break;

	    		case 5:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1200,180,-5,300,15000);
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
					_SocialTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(6,_valakas), 1600);

					break;

	    		case 6:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 2800,250,70,0,15000);
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
					_SocialTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(7,_valakas), 200);

					break;

	    		case 7:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 2600,30,60,3400,15000);
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
					_SocialTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(8,_valakas), 5700);

					break;

	    		case 8:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 700,150,-65,0,15000);
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
					_SocialTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(9,_valakas), 1400);

					break;

	    		case 9:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1200,150,-55,2900,15000);
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
					_SocialTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(10,_valakas), 6700);

					break;

	    		case 10:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 750,170,-10,1700,5700);
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
					_SocialTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(11,_valakas), 3700);

					break;

	    		case 11:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 840,170,-5,1200,2000);
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
					_SocialTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(12,_valakas), 2000);

					break;

	    		case 12:
					// reset camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						pc.leaveMovieMode();
					}

					_MobiliseTask = ThreadPoolManager.getInstance().scheduleGeneral(new SetMobilised(_valakas),16);

	                // move at random.
	                if(Config.FWV_MOVEATRANDOM)
	                {
	                	L2CharPosition pos = new L2CharPosition(Rnd.get(211080, 214909),Rnd.get(-115841, -112822),-1662,0);
	                	_MoveAtRandomTask = ThreadPoolManager.getInstance().scheduleGeneral(new MoveAtRandom(_valakas,pos),32);
	                }

	                // set delete task.
	                _ActivityTimeEndTask = ThreadPoolManager.getInstance().scheduleGeneral(new ActivityTimeEnd(),Config.FWV_ACTIVITYTIMEOFVALAKAS);

					break;

    		}
    	}
    }

    // at end of activity time.
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

    // clean Valakas's lair.
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
		if(_MoveAtRandomTask != null)
		{
			_MoveAtRandomTask.cancel(true);
			_MoveAtRandomTask = null;
		}
    	if(_RespawnValakasTask != null)
    	{
    		_RespawnValakasTask.cancel(true);
    		_RespawnValakasTask = null;
    	}

		// interval begin.
		setInetrvalEndTask();
	}

    // start interval.
    public void setInetrvalEndTask()
    {
		// init state of Valakas's lair.
    	//if (!_StateSet.getState().equals(GrandBossState.StateEnum.INTERVAL))
       	if (_Alive != GrandBossStatus.INTERVAL)
    	{
        	_Alive = GrandBossStatus.INTERVAL;
        	GrandBossManager.getInstance().setBossStatus(_BossId, _Alive);
        	GrandBossManager.getInstance().save();
    	}

    	_IntervalEndTask = ThreadPoolManager.getInstance().scheduleGeneral(new IntervalEnd(),GrandBossManager.getInstance().getInterval(_BossId));
        _log.info("ValakasManager : Interval START.");
    }

    // at end of interval.
    private class IntervalEnd implements Runnable
    {
    	public IntervalEnd()
    	{
    	}

    	public void run()
    	{
    		doIntervalEnd();
    	}
    }

    protected void doIntervalEnd()
    {
		_PlayersInLair.clear();
    	_Alive = GrandBossStatus.NOTSPAWN;
    	GrandBossManager.getInstance().setBossStatus(_BossId, _Alive);
    	GrandBossManager.getInstance().save();
        _log.info("ValakasManager : Interval END.");
    }

    // setting teleport cube spawn task.
    public void setCubeSpawn()
    {
		// init state of Valakas's lair.
    	_Alive = GrandBossStatus.DEAD;
    	_StateSet.set("respawn_time", Calendar.getInstance().getTimeInMillis() + Rnd.get(Config.FWV_FIXINTERVALOFVALAKAS,Config.FWV_FIXINTERVALOFVALAKAS + Config.FWV_RANDOMINTERVALOFVALAKAS));
    	GrandBossManager.getInstance().setBossStatus(_BossId, _Alive);
    	GrandBossManager.getInstance().setStatsSet(_BossId, _StateSet);
    	GrandBossManager.getInstance().save();

    	_CubeSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new CubeSpawn(),10000);

    	Date dt = new Date(_StateSet.getLong("respawn_time"));
        _log.info("ValakasManager : Valakas is dead.");
        _log.info("ValakasManager : Next spawn date of Valakas is " + dt + ".");
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
        private L2GrandBossInstance _boss;
        public SetMobilised(L2GrandBossInstance boss)
        {
        	_boss = boss;
        }

        public void run()
        {
        	_boss.setIsImmobilized(false);
        	_boss.setIsInSocialAction(false);

            // When it is possible to act, a social action is canceled.
            if (_SocialTask != null)
            {
            	_SocialTask.cancel(true);
                _SocialTask = null;
            }
        }
    }

    // Move at random on after Valakas appears.
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

    // when a server restart while fight against Valakas.
    protected void restartValakas()
    {
    	L2Spawn valakasSpawn = _MonsterSpawn.get(32123);
    	L2NpcInstance valakas = valakasSpawn.doSpawn();
    	_Monsters.add(valakas);

    	// set next task.
    	if(_RespawnValakasTask != null)
    	{
    		_RespawnValakasTask.cancel(true);
    		_RespawnValakasTask = null;
    	}
    	_RespawnValakasTask = ThreadPoolManager.getInstance().scheduleGeneral(new RestartValakas(valakas), Config.TIMELIMITOFINVADE + 1000);
    	_log.info("ValakasManager : Restart Valakas.");
    }

    private class RestartValakas implements Runnable
    {
    	private L2NpcInstance _Valakas;
    	public RestartValakas(L2NpcInstance valakas)
    	{
    		_Valakas = valakas;
    	}

    	public void run()
    	{
    		_Valakas.getSpawn().stopRespawn();
    		_Valakas.deleteMe();

			// set next task.
        	if (_MonsterSpawnTask != null)
            {
        		_MonsterSpawnTask.cancel(true);
        		_MonsterSpawnTask = null;
            }
        	_MonsterSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(1,null),15000);
    	}
    }
}
