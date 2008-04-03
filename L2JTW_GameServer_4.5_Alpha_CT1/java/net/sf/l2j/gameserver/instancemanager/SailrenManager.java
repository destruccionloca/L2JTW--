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
import javolution.util.FastList;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.status.GrandBossStatus;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;

/**
 *
 * This class ...
 * サイ??との戦闘をコ?ト?ー?するク?ス。
 * @version $Revision: $ $Date: $
 * @author  L2J_JP SANDMAN
 */
public class SailrenManager
{
    protected static Logger _log = Logger.getLogger(SailrenManager.class.getName());
    private static SailrenManager _instance = new SailrenManager();

    // 帰還用キ?ーブの出現データ
    private final int _SailrenCubeLocation[][] =
    	{
    		{27734,-6838,-1982,0}
    	};
    protected List<L2Spawn> _SailrenCubeSpawn = new FastList<L2Spawn>();
    protected List<L2NpcInstance> _SailrenCube = new FastList<L2NpcInstance>();

    // サイ??の?窟に侵入したプ?イ?ーの?スト
    protected List<L2PcInstance> _PlayersInSailrenLair = new FastList<L2PcInstance>();

    // ??スター の出現データ
    protected L2Spawn _VelociraptorSpawn;	// ?ァ?キ?プト?
    protected L2Spawn _PterosaurSpawn;		// ??フォ??クス
    protected L2Spawn _TyrannoSpawn;		// ティ?ノザウ?ス
    protected L2Spawn _SailrenSapwn;		// サイ??

    // ??スター のイ?スタ?ス
    protected L2NpcInstance _Velociraptor;	// ?ァ?キ?プト?
    protected L2NpcInstance _Pterosaur;		// ??フォ??クス
    protected L2NpcInstance _Tyranno;		// ティ?ノザウ?ス
    protected L2NpcInstance _Sailren;		// サイ??

    // タスク
    protected ScheduledFuture<?> _CubeSpawnTask = null;
    protected ScheduledFuture<?> _SailrenSpawnTask = null;
    protected ScheduledFuture<?> _IntervalEndTask = null;
    protected ScheduledFuture<?> _ActivityTimeEndTask = null;
    protected ScheduledFuture<?> _OnPartyAnnihilatedTask = null;
    protected ScheduledFuture<?> _SocialTask = null;

    // サイ??の?窟の状態
    protected String _ZoneType;
    protected String _QuestName;
    protected boolean _IsAlreadyEnteredOtherParty = false;
    protected StatsSet _StateSet;
    protected int _Alive;
    protected int _BossId = 29065;

    public SailrenManager()
    {
    }

    public static SailrenManager getInstance()
    {
        if (_instance == null) _instance = new SailrenManager();

        return _instance;
    }

    // ?期化
    public void init()
    {
    	// ?の状態の?期化
    	_PlayersInSailrenLair.clear();
    	_IsAlreadyEnteredOtherParty = false;
        _ZoneType = "LairofSailren";
        _QuestName = "sailren";
        _StateSet = GrandBossManager.getInstance().getStatsSet(_BossId);
        _Alive = GrandBossManager.getInstance().getBossStatus(_BossId);

        // ボスの出現データを設定する
        try
        {
            L2NpcTemplate template1;

            // ?ァ?キ?プト?
            template1 = NpcTable.getInstance().getTemplate(22218); //Velociraptor
            _VelociraptorSpawn = new L2Spawn(template1);
            _VelociraptorSpawn.setLocx(27852);
            _VelociraptorSpawn.setLocy(-5536);
            _VelociraptorSpawn.setLocz(-1983);
            _VelociraptorSpawn.setHeading(44732);
            _VelociraptorSpawn.setAmount(1);
            _VelociraptorSpawn.setRespawnDelay(Config.FWS_ACTIVITYTIMEOFMOBS * 2);
            SpawnTable.getInstance().addNewSpawn(_VelociraptorSpawn, false);

            // ??フォ??クス
            template1 = NpcTable.getInstance().getTemplate(22199); //Pterosaur
            _PterosaurSpawn = new L2Spawn(template1);
            _PterosaurSpawn.setLocx(27852);
            _PterosaurSpawn.setLocy(-5536);
            _PterosaurSpawn.setLocz(-1983);
            _PterosaurSpawn.setHeading(44732);
            _PterosaurSpawn.setAmount(1);
            _PterosaurSpawn.setRespawnDelay(Config.FWS_ACTIVITYTIMEOFMOBS * 2);
            SpawnTable.getInstance().addNewSpawn(_PterosaurSpawn, false);

            // ティ?ノザウ?ス
            template1 = NpcTable.getInstance().getTemplate(22217); //Tyrannosaurus
            _TyrannoSpawn = new L2Spawn(template1);
            _TyrannoSpawn.setLocx(27852);
            _TyrannoSpawn.setLocy(-5536);
            _TyrannoSpawn.setLocz(-1983);
            _TyrannoSpawn.setHeading(44732);
            _TyrannoSpawn.setAmount(1);
            _TyrannoSpawn.setRespawnDelay(Config.FWS_ACTIVITYTIMEOFMOBS * 2);
            SpawnTable.getInstance().addNewSpawn(_TyrannoSpawn, false);

            // サイ??
            template1 = NpcTable.getInstance().getTemplate(29065); //Sailren
            _SailrenSapwn = new L2Spawn(template1);
            _SailrenSapwn.setLocx(27810);
            _SailrenSapwn.setLocy(-5655);
            _SailrenSapwn.setLocz(-1983);
            _SailrenSapwn.setHeading(44732);
            _SailrenSapwn.setAmount(1);
            _SailrenSapwn.setRespawnDelay(Config.FWS_ACTIVITYTIMEOFMOBS * 2);
            SpawnTable.getInstance().addNewSpawn(_SailrenSapwn, false);

        }
        catch (Exception e)
        {
            _log.warning(e.getMessage());
        }

        // テ?ポートキ?ーブの出現データを作成する
        try
        {
            L2NpcTemplate Cube = NpcTable.getInstance().getTemplate(32107);
            L2Spawn spawnDat;

            for(int i = 0;i < _SailrenCubeLocation.length; i++)
            {
                spawnDat = new L2Spawn(Cube);
                spawnDat.setAmount(1);
                spawnDat.setLocx(_SailrenCubeLocation[i][0]);
                spawnDat.setLocy(_SailrenCubeLocation[i][1]);
                spawnDat.setLocz(_SailrenCubeLocation[i][2]);
                spawnDat.setHeading(_SailrenCubeLocation[i][3]);
                spawnDat.setRespawnDelay(60);
                spawnDat.setLocation(0);
                SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                _SailrenCubeSpawn.add(spawnDat);
            }
        }
        catch (Exception e)
        {
            _log.warning(e.getMessage());
        }

        _log.info("SailrenManager : State of Sailren is " + _Alive + ".");
        if (_Alive != GrandBossStatus.NOTSPAWN)
        	setInetrvalEndTask();

		Date dt = new Date(_StateSet.getLong("respawn_time"));
        _log.info("SailrenManager : Next spawn date of Sailren is " + dt + ".");
        _log.info("SailrenManager : Init SailrenManager.");

    }

    // 住?に入ったプ?イ?ー?ストを渡す
    public List<L2PcInstance> getPlayersInLair()
	{
		return _PlayersInSailrenLair;
	}

    // サイ??の?に入る?が出?るか確認する。
    public int canIntoSailrenLair(L2PcInstance pc)
    {
    	if ((Config.FWS_ENABLESINGLEPLAYER == false) && (pc.getParty() == null)) return 4;
    	else if (_IsAlreadyEnteredOtherParty) return 2;
    	else if (_Alive == GrandBossStatus.NOTSPAWN) return 0;
    	else if (_Alive == GrandBossStatus.ALIVE || _Alive != GrandBossStatus.DEAD) return 1;
    	else if (_Alive == GrandBossStatus.INTERVAL) return 3;
    	else return 0;
    }

    // サイ??出現タスクの設定
    public void setSailrenSpawnTask(int NpcId)
    {
    	if ((NpcId == 22218) && (_PlayersInSailrenLair.size() >= 1)) return;

    	if (_SailrenSpawnTask == null)
        {
        	_SailrenSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(
            		new SailrenSpawn(NpcId),Config.FWS_INTERVALOFNEXTMONSTER);
        }
    }

    // サイ??の?に入ったプ?イ?ー?ストの更新
    public void addPlayerToSailrenLair(L2PcInstance pc)
    {
        if (!_PlayersInSailrenLair.contains(pc)) _PlayersInSailrenLair.add(pc);
    }

    // プ?イ?ーをサイ??の?に移動させる
    public void entryToSailrenLair(L2PcInstance pc)
    {
		int driftx;
		int drifty;

		if(canIntoSailrenLair(pc) != 0)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("条?を?たしていないため、入場が?否されました。");
			pc.sendPacket(sm);
			_IsAlreadyEnteredOtherParty = false;
			return;
		}

		if(pc.getParty() == null)
		{
			driftx = Rnd.get(-80, 80);
			drifty = Rnd.get(-80, 80);
			pc.teleToLocation(27734 + driftx,-6938 + drifty,-1982);
			addPlayerToSailrenLair(pc);
		}
		else
		{
			List<L2PcInstance> members = new FastList<L2PcInstance>(); // テ?ポート可能な??バーの?スト
			for (L2PcInstance mem : pc.getParty().getPartyMembers())
			{
				// 生きていて、パーティ?ーダーの認識範囲内にいれば、テ?ポートさせる
				if (!mem.isDead() && Util.checkIfInRange(700, pc, mem, true))
				{
					members.add(mem);
				}
			}
			for (L2PcInstance mem : members)
			{
				driftx = Rnd.get(-80, 80);
				drifty = Rnd.get(-80, 80);
				mem.teleToLocation(27734 + driftx,-6938 + drifty,-1982);
				addPlayerToSailrenLair(mem);
			}
		}
		_IsAlreadyEnteredOtherParty = true;
    }

    // パーティが全滅したかを確認
    public void checkAnnihilated(L2PcInstance pc)
    {
    	// 全滅した場?は５秒後に太古の?の船?場に飛ばす。
    	if(isPartyAnnihilated(pc))
    	{
    		_OnPartyAnnihilatedTask =
				ThreadPoolManager.getInstance().scheduleGeneral(new OnPartyAnnihilatedTask(pc),5000);
    	}
    }

    // パーティが全滅したかを確認
    public synchronized boolean isPartyAnnihilated(L2PcInstance pc)
    {
		if(pc.getParty() != null)
		{
			for(L2PcInstance mem:pc.getParty().getPartyMembers())
			{
				if(!mem.isDead() && GrandBossManager.getInstance().checkIfInZone("LairofSailren", pc))
				{
					return false;
				}
			}
			return true;
		}
		else
		{
			return true;
		}
    }

    // ?間切れおよび全滅?にプ?イ?ーをサイ??の?窟から強制移動する??
    public void banishesPlayers()
    {
    	for(L2PcInstance pc : _PlayersInSailrenLair)
    	{
    		if(pc.getQuestState("sailren") != null) pc.getQuestState("sailren").exitQuest(true);
    		if(GrandBossManager.getInstance().checkIfInZone("LairofSailren", pc))
    		{
        		int driftX = Rnd.get(-80,80);
        		int driftY = Rnd.get(-80,80);
        		pc.teleToLocation(10468 + driftX,-24569 + driftY,-3650);
    		}
    	}
    	_PlayersInSailrenLair.clear();
    	_IsAlreadyEnteredOtherParty = false;
    }

    // サイ??の?窟を掃?
    public void setUnspawn()
	{
    	// ?のプ?イ?ーを排?
    	banishesPlayers();

    	// テ?ポートキ?ーブを消?
		for (L2NpcInstance cube : _SailrenCube)
		{
			cube.getSpawn().stopRespawn();
			cube.deleteMe();
		}
		_SailrenCube.clear();

		// 仕?まれているタスクをキ??セ?
		if(_CubeSpawnTask != null)
		{
			_CubeSpawnTask.cancel(true);
			_CubeSpawnTask = null;
		}
		if(_SailrenSpawnTask != null)
		{
			_SailrenSpawnTask.cancel(true);
			_SailrenSpawnTask = null;
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

		// 状態の?期化
		_Velociraptor = null;
		_Pterosaur = null;
		_Tyranno = null;
		_Sailren = null;

		// 既定の?間まで?に入れないようにしておく
		setInetrvalEndTask();
	}

    // 帰還用キ?ーブを出現させる
    public void spawnCube()
    {
		for (L2Spawn spawnDat : _SailrenCubeSpawn)
		{
			_SailrenCube.add(spawnDat.doSpawn());
		}
    }

    // 帰還用キ?ーブを出現させるタスクの仕?み
    public void setCubeSpawn()
    {
    	_Alive = GrandBossStatus.DEAD;
    	_StateSet.set("respawn_time", Calendar.getInstance().getTimeInMillis() + Rnd.get(Config.FWS_FIXINTERVALOFSAILRENSPAWN,Config.FWS_FIXINTERVALOFSAILRENSPAWN + Config.FWS_RANDOMINTERVALOFSAILRENSPAWN));
    	GrandBossManager.getInstance().setBossStatus(_BossId, _Alive);
    	GrandBossManager.getInstance().setStatsSet(_BossId, _StateSet);
    	GrandBossManager.getInstance().save();

    	_CubeSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new CubeSpawn(),10000);

    	Date dt = new Date(_StateSet.getLong("respawn_time"));
        _log.info("SailrenManager : Sailren is dead.");
        _log.info("SailrenManager : Next spawn date of Sailren is " + dt + ".");
    }

    // サイ??の出現禁止解?タスクの仕?み
    public void setInetrvalEndTask()
    {
    	if (_Alive != GrandBossStatus.INTERVAL)
    	{
        	_Alive = GrandBossStatus.INTERVAL;
        	GrandBossManager.getInstance().setBossStatus(_BossId, _Alive);
        	GrandBossManager.getInstance().save();
    	}

    	_IntervalEndTask = ThreadPoolManager.getInstance().scheduleGeneral(new IntervalEnd(),GrandBossManager.getInstance().getInterval(_BossId));
    	_log.info("SailrenManager : Interval START.");
    }

    // update knownlist.
    protected void updateKnownList(L2NpcInstance boss)
    {
    	boss.getKnownList().getKnownPlayers().clear();
		for (L2PcInstance pc : _PlayersInSailrenLair)
		{
			boss.getKnownList().getKnownPlayers().put(pc.getObjectId(), pc);
		}
    }

    // ??スターを出現させる
    private class SailrenSpawn implements Runnable
    {
    	int _NpcId;
    	L2CharPosition _pos = new L2CharPosition(27628,-6109,-1982,44732);
    	public SailrenSpawn(int NpcId)
    	{
    		_NpcId = NpcId;
    	}

        public void run()
        {
        	switch (_NpcId)
            {
            	case 22218:		// ?ェ?キ?プト?
            		_Velociraptor = _VelociraptorSpawn.doSpawn();
            		_Velociraptor.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,_pos);
            		if(_SocialTask != null)
            		{
            			_SocialTask.cancel(true);
            			_SocialTask = null;
            		}
            		_SocialTask =
                        ThreadPoolManager.getInstance().scheduleGeneral(
                        		new Social(_Velociraptor,2),6000);
            		if(_ActivityTimeEndTask != null)
            		{
            			_ActivityTimeEndTask.cancel(true);
            			_ActivityTimeEndTask = null;
            		}
            		_ActivityTimeEndTask =
                        ThreadPoolManager.getInstance().scheduleGeneral(
                        		new ActivityTimeEnd(_Velociraptor),Config.FWS_ACTIVITYTIMEOFMOBS);
            		break;
            	case 22199:		// ??フォ??クス
            		_VelociraptorSpawn.stopRespawn();
            		_Pterosaur = _PterosaurSpawn.doSpawn();
            		_Pterosaur.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,_pos);
            		if(_SocialTask != null)
            		{
            			_SocialTask.cancel(true);
            			_SocialTask = null;
            		}
            		_SocialTask =
                        ThreadPoolManager.getInstance().scheduleGeneral(
                        		new Social(_Pterosaur,2),6000);
            		if(_ActivityTimeEndTask != null)
            		{
            			_ActivityTimeEndTask.cancel(true);
            			_ActivityTimeEndTask = null;
            		}
            		_ActivityTimeEndTask =
                        ThreadPoolManager.getInstance().scheduleGeneral(
                        		new ActivityTimeEnd(_Pterosaur),Config.FWS_ACTIVITYTIMEOFMOBS);
            		break;
            	case 22217:		// ティ?ノザウ?ス
            		_PterosaurSpawn.stopRespawn();
            		_Tyranno = _TyrannoSpawn.doSpawn();
            		_Tyranno.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,_pos);
            		if(_SocialTask != null)
            		{
            			_SocialTask.cancel(true);
            			_SocialTask = null;
            		}
            		_SocialTask =
                        ThreadPoolManager.getInstance().scheduleGeneral(
                        		new Social(_Tyranno,2),6000);
            		if(_ActivityTimeEndTask != null)
            		{
            			_ActivityTimeEndTask.cancel(true);
            			_ActivityTimeEndTask = null;
            		}
            		_ActivityTimeEndTask =
                        ThreadPoolManager.getInstance().scheduleGeneral(
                        		new ActivityTimeEnd(_Tyranno),Config.FWS_ACTIVITYTIMEOFMOBS);
            		break;
            	case 29065:		// サイ??
            		_TyrannoSpawn.stopRespawn();
            		_Sailren = _SailrenSapwn.doSpawn();

	            	_StateSet.set("respawn_time", Calendar.getInstance().getTimeInMillis() + Rnd.get(Config.FWS_FIXINTERVALOFSAILRENSPAWN,Config.FWS_FIXINTERVALOFSAILRENSPAWN + Config.FWS_RANDOMINTERVALOFSAILRENSPAWN) + Config.FWS_ACTIVITYTIMEOFMOBS);
	            	_Alive = GrandBossStatus.ALIVE;
	            	GrandBossManager.getInstance().setBossStatus(_BossId, _Alive);
	            	GrandBossManager.getInstance().setStatsSet(_BossId, _StateSet);
	            	GrandBossManager.getInstance().save();
	            	_log.info("SailrenManager : Spawn Sailren.");

            		_Sailren.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,_pos);
            		if(_SocialTask != null)
            		{
            			_SocialTask.cancel(true);
            			_SocialTask = null;
            		}
            		_SocialTask =
                        ThreadPoolManager.getInstance().scheduleGeneral(
                        		new Social(_Sailren,2),6000);
            		if(_ActivityTimeEndTask != null)
            		{
            			_ActivityTimeEndTask.cancel(true);
            			_ActivityTimeEndTask = null;
            		}
            		_ActivityTimeEndTask =
                        ThreadPoolManager.getInstance().scheduleGeneral(
                        		new ActivityTimeEnd(_Sailren),Config.FWS_ACTIVITYTIMEOFMOBS);
            		break;
            	default:
            		break;
            }

            if(_SailrenSpawnTask != null)
            {
            	_SailrenSpawnTask.cancel(true);
            	_SailrenSpawnTask = null;
            }
        }
    }

    // 帰還用キ?ーブを出現させる
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

    // ?間切れ??
    private class ActivityTimeEnd implements Runnable
    {
    	L2NpcInstance _Mob;
    	public ActivityTimeEnd(L2NpcInstance npc)
    	{
    		_Mob = npc;
    	}

    	public void run()
    	{
    		if(!_Mob.isDead())
    		{
    			_Mob.deleteMe();
    			_Mob.getSpawn().stopRespawn();
    			_Mob = null;
    		}
    	    // サイ??の?窟を掃?
    		setUnspawn();
    	}
    }

    // サイ??出現イ?ターバ?の終了
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
		_PlayersInSailrenLair.clear();
    	_Alive = GrandBossStatus.NOTSPAWN;
    	GrandBossManager.getInstance().setBossStatus(_BossId, _Alive);
    	GrandBossManager.getInstance().save();
    	_log.info("SailrenManager : Interval END.");
    }

    // パーティが全滅していれば太古の?の船?場へ飛ばす
	private class OnPartyAnnihilatedTask implements Runnable
	{
		L2PcInstance _player;

		public OnPartyAnnihilatedTask(L2PcInstance player)
		{
			_player = player;
		}

		public void run()
		{
			setUnspawn();
		}
	}

	// ソーシ??アクシ??の実行
    private class Social implements Runnable
    {
        private int _action;
        private L2NpcInstance _npc;

        public Social(L2NpcInstance npc,int actionId)
        {
        	_npc = npc;
            _action = actionId;
        }

        public void run()
        {

        	updateKnownList(_npc);

    		SocialAction sa = new SocialAction(_npc.getObjectId(), _action);
            _npc.broadcastPacket(sa);
        }
    }
}
