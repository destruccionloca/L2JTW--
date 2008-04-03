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
 * �T�C??�Ƃ̐퓬���R?�g?�[?����N?�X�B
 * @version $Revision: $ $Date: $
 * @author  L2J_JP SANDMAN
 */
public class SailrenManager
{
    protected static Logger _log = Logger.getLogger(SailrenManager.class.getName());
    private static SailrenManager _instance = new SailrenManager();

    // �A�җp�L?�[�u�̏o���f�[�^
    private final int _SailrenCubeLocation[][] =
    	{
    		{27734,-6838,-1982,0}
    	};
    protected List<L2Spawn> _SailrenCubeSpawn = new FastList<L2Spawn>();
    protected List<L2NpcInstance> _SailrenCube = new FastList<L2NpcInstance>();

    // �T�C??��?�A�ɐN�������v?�C?�[��?�X�g
    protected List<L2PcInstance> _PlayersInSailrenLair = new FastList<L2PcInstance>();

    // ??�X�^�[ �̏o���f�[�^
    protected L2Spawn _VelociraptorSpawn;	// ?�@?�L?�v�g?
    protected L2Spawn _PterosaurSpawn;		// ??�t�H??�N�X
    protected L2Spawn _TyrannoSpawn;		// �e�B?�m�U�E?�X
    protected L2Spawn _SailrenSapwn;		// �T�C??

    // ??�X�^�[ �̃C?�X�^?�X
    protected L2NpcInstance _Velociraptor;	// ?�@?�L?�v�g?
    protected L2NpcInstance _Pterosaur;		// ??�t�H??�N�X
    protected L2NpcInstance _Tyranno;		// �e�B?�m�U�E?�X
    protected L2NpcInstance _Sailren;		// �T�C??

    // �^�X�N
    protected ScheduledFuture<?> _CubeSpawnTask = null;
    protected ScheduledFuture<?> _SailrenSpawnTask = null;
    protected ScheduledFuture<?> _IntervalEndTask = null;
    protected ScheduledFuture<?> _ActivityTimeEndTask = null;
    protected ScheduledFuture<?> _OnPartyAnnihilatedTask = null;
    protected ScheduledFuture<?> _SocialTask = null;

    // �T�C??��?�A�̏��
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

    // ?����
    public void init()
    {
    	// ?�̏�Ԃ�?����
    	_PlayersInSailrenLair.clear();
    	_IsAlreadyEnteredOtherParty = false;
        _ZoneType = "LairofSailren";
        _QuestName = "sailren";
        _StateSet = GrandBossManager.getInstance().getStatsSet(_BossId);
        _Alive = GrandBossManager.getInstance().getBossStatus(_BossId);

        // �{�X�̏o���f�[�^��ݒ肷��
        try
        {
            L2NpcTemplate template1;

            // ?�@?�L?�v�g?
            template1 = NpcTable.getInstance().getTemplate(22218); //Velociraptor
            _VelociraptorSpawn = new L2Spawn(template1);
            _VelociraptorSpawn.setLocx(27852);
            _VelociraptorSpawn.setLocy(-5536);
            _VelociraptorSpawn.setLocz(-1983);
            _VelociraptorSpawn.setHeading(44732);
            _VelociraptorSpawn.setAmount(1);
            _VelociraptorSpawn.setRespawnDelay(Config.FWS_ACTIVITYTIMEOFMOBS * 2);
            SpawnTable.getInstance().addNewSpawn(_VelociraptorSpawn, false);

            // ??�t�H??�N�X
            template1 = NpcTable.getInstance().getTemplate(22199); //Pterosaur
            _PterosaurSpawn = new L2Spawn(template1);
            _PterosaurSpawn.setLocx(27852);
            _PterosaurSpawn.setLocy(-5536);
            _PterosaurSpawn.setLocz(-1983);
            _PterosaurSpawn.setHeading(44732);
            _PterosaurSpawn.setAmount(1);
            _PterosaurSpawn.setRespawnDelay(Config.FWS_ACTIVITYTIMEOFMOBS * 2);
            SpawnTable.getInstance().addNewSpawn(_PterosaurSpawn, false);

            // �e�B?�m�U�E?�X
            template1 = NpcTable.getInstance().getTemplate(22217); //Tyrannosaurus
            _TyrannoSpawn = new L2Spawn(template1);
            _TyrannoSpawn.setLocx(27852);
            _TyrannoSpawn.setLocy(-5536);
            _TyrannoSpawn.setLocz(-1983);
            _TyrannoSpawn.setHeading(44732);
            _TyrannoSpawn.setAmount(1);
            _TyrannoSpawn.setRespawnDelay(Config.FWS_ACTIVITYTIMEOFMOBS * 2);
            SpawnTable.getInstance().addNewSpawn(_TyrannoSpawn, false);

            // �T�C??
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

        // �e?�|�[�g�L?�[�u�̏o���f�[�^���쐬����
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

    // �Z?�ɓ������v?�C?�[?�X�g��n��
    public List<L2PcInstance> getPlayersInLair()
	{
		return _PlayersInSailrenLair;
	}

    // �T�C??��?�ɓ���?���o?�邩�m�F����B
    public int canIntoSailrenLair(L2PcInstance pc)
    {
    	if ((Config.FWS_ENABLESINGLEPLAYER == false) && (pc.getParty() == null)) return 4;
    	else if (_IsAlreadyEnteredOtherParty) return 2;
    	else if (_Alive == GrandBossStatus.NOTSPAWN) return 0;
    	else if (_Alive == GrandBossStatus.ALIVE || _Alive != GrandBossStatus.DEAD) return 1;
    	else if (_Alive == GrandBossStatus.INTERVAL) return 3;
    	else return 0;
    }

    // �T�C??�o���^�X�N�̐ݒ�
    public void setSailrenSpawnTask(int NpcId)
    {
    	if ((NpcId == 22218) && (_PlayersInSailrenLair.size() >= 1)) return;

    	if (_SailrenSpawnTask == null)
        {
        	_SailrenSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(
            		new SailrenSpawn(NpcId),Config.FWS_INTERVALOFNEXTMONSTER);
        }
    }

    // �T�C??��?�ɓ������v?�C?�[?�X�g�̍X�V
    public void addPlayerToSailrenLair(L2PcInstance pc)
    {
        if (!_PlayersInSailrenLair.contains(pc)) _PlayersInSailrenLair.add(pc);
    }

    // �v?�C?�[���T�C??��?�Ɉړ�������
    public void entryToSailrenLair(L2PcInstance pc)
    {
		int driftx;
		int drifty;

		if(canIntoSailrenLair(pc) != 0)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("��?��?�����Ă��Ȃ����߁A���ꂪ?�ۂ���܂����B");
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
			List<L2PcInstance> members = new FastList<L2PcInstance>(); // �e?�|�[�g�\��??�o�[��?�X�g
			for (L2PcInstance mem : pc.getParty().getPartyMembers())
			{
				// �����Ă��āA�p�[�e�B?�[�_�[�̔F���͈͓��ɂ���΁A�e?�|�[�g������
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

    // �p�[�e�B���S�ł��������m�F
    public void checkAnnihilated(L2PcInstance pc)
    {
    	// �S�ł�����?�͂T�b��ɑ��Â�?�̑D?��ɔ�΂��B
    	if(isPartyAnnihilated(pc))
    	{
    		_OnPartyAnnihilatedTask =
				ThreadPoolManager.getInstance().scheduleGeneral(new OnPartyAnnihilatedTask(pc),5000);
    	}
    }

    // �p�[�e�B���S�ł��������m�F
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

    // ?�Ԑ؂ꂨ��ёS��?�Ƀv?�C?�[���T�C??��?�A���狭���ړ�����??
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

    // �T�C??��?�A��|?
    public void setUnspawn()
	{
    	// ?�̃v?�C?�[��r?
    	banishesPlayers();

    	// �e?�|�[�g�L?�[�u����?
		for (L2NpcInstance cube : _SailrenCube)
		{
			cube.getSpawn().stopRespawn();
			cube.deleteMe();
		}
		_SailrenCube.clear();

		// �d?�܂�Ă���^�X�N���L??�Z?
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

		// ��Ԃ�?����
		_Velociraptor = null;
		_Pterosaur = null;
		_Tyranno = null;
		_Sailren = null;

		// �����?�Ԃ܂�?�ɓ���Ȃ��悤�ɂ��Ă���
		setInetrvalEndTask();
	}

    // �A�җp�L?�[�u���o��������
    public void spawnCube()
    {
		for (L2Spawn spawnDat : _SailrenCubeSpawn)
		{
			_SailrenCube.add(spawnDat.doSpawn());
		}
    }

    // �A�җp�L?�[�u���o��������^�X�N�̎d?��
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

    // �T�C??�̏o���֎~��?�^�X�N�̎d?��
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

    // ??�X�^�[���o��������
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
            	case 22218:		// ?�F?�L?�v�g?
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
            	case 22199:		// ??�t�H??�N�X
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
            	case 22217:		// �e�B?�m�U�E?�X
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
            	case 29065:		// �T�C??
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

    // �A�җp�L?�[�u���o��������
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

    // ?�Ԑ؂�??
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
    	    // �T�C??��?�A��|?
    		setUnspawn();
    	}
    }

    // �T�C??�o���C?�^�[�o?�̏I��
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

    // �p�[�e�B���S�ł��Ă���Α��Â�?�̑D?��֔�΂�
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

	// �\�[�V??�A�N�V??�̎��s
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
