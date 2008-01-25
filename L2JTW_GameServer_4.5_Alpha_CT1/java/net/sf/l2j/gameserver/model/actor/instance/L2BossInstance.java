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
package net.sf.l2j.gameserver.model.actor.instance;

import java.util.List;
import java.util.concurrent.Future;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class manages all RaidBoss.
 *
 * @version $Revision: 1.0.0.0 $ $Date: 2006/06/16 $
 */
public final class L2BossInstance extends L2MonsterInstance
{
// L2J_JP addon start ======================================
	//protected static Logger _log = Logger.getLogger(L2BossInstance.class.getName());
	private boolean _teleportedToNest;

    private static final int BOSS_MAINTENANCE_INTERVAL = 10000;

    protected int doTeleport = 0;
    protected L2Object _target;
    protected L2Character _Atacker;
    protected static final int NurseAntRespawnDelay = Config.NURSEANT_RESPAWN_DELAY;

    protected Future minionMaintainTask = null;

    protected boolean _isInSocialAction = false;
    
    public boolean IsInSocialAction()
    {
        return _isInSocialAction;
    }
    
    public void setIsInSocialAction(boolean value)
    {
        _isInSocialAction = value;
    }
// L2J_JP addon end ========================================
    /**
     * Constructor for L2BossInstance. This represent all grandbosses:
     * <ul>
     * <li>12001    Queen Ant</li>
     * <li>12169    Orfen</li>
     * <li>12211    Antharas</li>
     * <li>12372    Baium</li>
     * <li>12374    Zaken</li>
     * <li>12899    Valakas</li>
     * <li>12052    Core</li>
     * </ul>
     * <br>
     * <b>For now it's nothing more than a L2Monster but there'll be a scripting<br>
     * engine for AI soon and we could add special behaviour for those boss</b><br>
     * <br>
     * @param objectId ID of the instance
     * @param template L2NpcTemplate of the instance
     */
	public L2BossInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

    @Override
	protected int getMaintenanceInterval() { return BOSS_MAINTENANCE_INTERVAL; }

    // [L2J_JP ADD SANDMAN]
    @Override
    public boolean doDie(L2Character killer)
    {
        if (!super.doDie(killer))
            return false;
        if (killer instanceof L2PlayableInstance)
        {
            SystemMessage msg = new SystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL);
            broadcastPacket(msg);
        }
        return true;
    }

    /**
     * Used by Orfen to set 'teleported' flag, when hp goes to <50%
     * @param flag
     */
    public void setTeleported(boolean flag)
    {
        _teleportedToNest = flag;
    }

    public boolean getTeleported()
    {
        return _teleportedToNest;
    }

    @Override
	public void onSpawn()
    {
        // [L2J_JP ADD START SANDMAN]
    	// get players in lair and update known list.
    	getKnownList().getKnownPlayers().clear();
        switch (getNpcId())
		{
			case 29022:	// Zaken (Note:teleport-out of instant-move execute onSpawn.)
				if(GameTimeController.getInstance().isNowNight())
					setIsInvul(true);
				else
					setIsInvul(false);
				break;
		}
        
    	super.onSpawn();
    }

    /**
     * Reduce the current HP of the L2Attackable, update its _aggroList and launch the doDie Task if necessary.<BR><BR>
     *
     */
    @Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
    {
        // [L2J_JP ADD SANDMAN]
        if (this.IsInSocialAction() || this.isInvul()) return;

        switch (getTemplate().npcId)
        {
            case 29014: // Orfen
                if ((getCurrentHp() - damage) < getMaxHp() / 2 && !getTeleported())
                {
                    clearAggroList();
                    getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
                    teleToLocation(43577,15985,-4396, false);
                    setTeleported(true);
                    setCanReturnToSpawnPoint(false);
                }
                break;
            // [L2J_JP ADD SANDMAN]
            case 29001: // Queen ant
                List<L2MinionInstance> _minions = _minionList.getSpawnedMinions();

                if (_minions.isEmpty())
                {
                    if (minionMaintainTask == null)
                    {
                        try
                        {
                            minionMaintainTask = 
                            	ThreadPoolManager.getInstance().scheduleGeneral(
                            			new RespawnNurseAnts(),NurseAntRespawnDelay);
                        }
                        catch (NullPointerException e)
                        {
                        }
                    }
                }
                else
                {
                    L2Skill _heal1 = SkillTable.getInstance().getInfo(4020, 1);
                    L2Skill _heal2 = SkillTable.getInstance().getInfo(4024, 1);

                    for (L2MinionInstance m : _minions)
                    {
                        this.callMinions();
                        m.setTarget(this);
                        m.doCast(_heal1);
                        m.setTarget(this);
                        m.doCast(_heal2);
                    }
                }
                break;
            default:
        }

        super.reduceCurrentHp(damage, attacker, awake);
    }

    @Override
	public boolean isRaid()
    {
        return true;
    }

    // [L2J_JP ADD START SANDMAN]
    // respawn nurse ants.
    private class RespawnNurseAnts implements Runnable
    {

        public RespawnNurseAnts()
        {
        }

        public void run()
        {
            try
            {
                _minionList.maintainMinions();
            }
            catch (Throwable e)
            {
                System.err.println("Error in RespawnNurseAnts.");
                e.printStackTrace();
            }
            finally
            {
            	minionMaintainTask = null;
            }
        }
    }

    @Override
    public void doAttack(L2Character target)
    {
    	if(_isInSocialAction) return;
    	else super.doAttack(target);
    }

    @Override
    public void doCast(L2Skill skill)
    {
    	if(_isInSocialAction) return;
    	else super.doCast(skill);
    }
    // [L2J_JP ADD END SANDMAN]
}
