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

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class manages all Grand Bosses.
 *
 * @version $Revision: 1.0.0.0 $ $Date: 2006/06/16 $
 */
public final class L2GrandBossInstance extends L2MonsterInstance
{
    private static final int BOSS_MAINTENANCE_INTERVAL = 10000;
    protected boolean _isInSocialAction = false;
 // L2J_JP addon start ======================================
	//protected static Logger _log = Logger.getLogger(L2BossInstance.class.getName());
	private boolean _teleportedToNest;
    protected Future minionMaintainTask = null;

     /**
     * Constructor for L2GrandBossInstance. This represent all grandbosses.
     * 
     * @param objectId ID of the instance
     * @param template L2NpcTemplate of the instance
     */
	public L2GrandBossInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

    @Override
	protected int getMaintenanceInterval() { return BOSS_MAINTENANCE_INTERVAL; }

    @Override
	public void onSpawn()
    {

    	getKnownList().getKnownPlayers().clear();
    	super.onSpawn();
    }

    /**
     * Reduce the current HP of the L2Attackable, update its _aggroList and launch the doDie Task if necessary.<BR><BR>
     *
     */
    @Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
    {
    	if (this.IsInSocialAction() || this.isInvul()) return;

        super.reduceCurrentHp(damage, attacker, awake);
    }
    public boolean getTeleported()
    {
        return _teleportedToNest;
    }

    public void setTeleported(boolean flag)
    {
        _teleportedToNest = flag;
    }

    @Override
	public boolean isRaid()
    {
        return true;
    }
    public void setIsInSocialAction(boolean value)
    {
        _isInSocialAction = value;
    }
    
    public boolean IsInSocialAction()
    {
        return _isInSocialAction;
    }
}
