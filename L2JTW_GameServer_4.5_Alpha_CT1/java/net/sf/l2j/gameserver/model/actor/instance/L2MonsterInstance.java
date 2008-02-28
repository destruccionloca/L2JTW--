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

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import net.sf.l2j.gameserver.ThreadPoolManager;

import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.knownlist.MonsterKnownList;
import net.sf.l2j.gameserver.serverpackets.StopMove;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.MinionList;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.gameserver.model.L2Spawn;

/**
 * This class manages all Monsters.
 *
 * L2MonsterInstance :<BR><BR>
 * <li>L2MinionInstance</li>
 * <li>L2RaidBossInstance </li>
 * <li>L2GrandBossInstance </li>
 *
 * @version $Revision: 1.20.4.6 $ $Date: 2005/04/06 16:13:39 $
 */
public class L2MonsterInstance extends L2Attackable
{
	//private static Logger _log = Logger.getLogger(L2MonsterInstance.class.getName());

    protected final MinionList _minionList;

    @SuppressWarnings("unused")
    protected ScheduledFuture<?> _minionMaintainTask = null;

    private static final int MONSTER_MAINTENANCE_INTERVAL = 1000;

    // [L2J_JP ADD SANDMAN]
    private ScheduledFuture hideTask;
	
	/**
	 * Constructor of L2MonsterInstance (use L2Character and L2NpcInstance constructor).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to set the _template of the L2MonsterInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR) </li>
	 * <li>Set the name of the L2MonsterInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it </li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param L2NpcTemplate Template to apply to the NPC
	 */
	public L2MonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList();	// init knownlist
        _minionList  = new MinionList(this);
        
        // [L2J_JP ADD]
        if (this.getNpcId() == 29002)   // Queen Ant Larva is invulnerable.
        {
            this.setIsInvul(true);
        }
	}

    @Override
	public final MonsterKnownList getKnownList()
    {
    	if(super.getKnownList() == null || !(super.getKnownList() instanceof MonsterKnownList))
    		setKnownList(new MonsterKnownList(this));
    	return (MonsterKnownList)super.getKnownList();
    }

	/**
	 * Return True if the attacker is not another L2MonsterInstance.<BR><BR>
	 */
	//@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (attacker instanceof L2MonsterInstance)
			return false;

		return !isEventMob;
	}

	/**
	 * Return True if the L2MonsterInstance is Agressive (aggroRange > 0).<BR><BR>
	 */
	@Override
	public boolean isAggressive()
	{
		return (getTemplate().aggroRange > 0) && !isEventMob;
	}

    @Override
	public void onSpawn()
    {
        super.onSpawn();
        
        // [L2J_JP ADD SANDMAN]
        // in Restless Forest
        // They are repeat themselves it visible or invisible.
        switch (this.getNpcId())
        {
            case 21548:
            case 21551:
            case 21552:
                hideTask = ThreadPoolManager.getInstance().scheduleEffect(new doHide(this), (this.getSpawn().getRespawnDelay()));
                break;

        }

        // [L2J_JP EDIT SANDMAN]
        if (getTemplate().getMinionData()!=null)
        {
            try
            {
                for (L2MinionInstance minion : getSpawnedMinions())
                {
                    if (minion == null) continue;
                    getSpawnedMinions().remove(minion);
                    minion.deleteMe();
                }
                _minionList.clearRespawnList();

                manageMinions();
            }
            catch ( NullPointerException e )
            {
                // [L2J_JP ADD SANDMAN]
                e.printStackTrace();
            }
        }
    }

    protected int getMaintenanceInterval() { return MONSTER_MAINTENANCE_INTERVAL; }

    /**
     * Spawn all minions at a regular interval
     *
     */
    protected void manageMinions ()
    {
        _minionMaintainTask = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
            public void run()
            {
                _minionList.spawnMinions();
            }
        }, getMaintenanceInterval());
    }

    public void callMinions()
    {
        if (_minionList.hasMinions())
        {
            for (L2MinionInstance minion : _minionList.getSpawnedMinions())
            {
                // Get actual coords of the minion and check to see if it's too far away from this L2MonsterInstance
                if (!isInsideRadius(minion, 200, false, false))
                {
                    // Get the coords of the master to use as a base to move the minion to
                    int masterX = getX();
                    int masterY = getY();
                    int masterZ = getZ();

                    // Calculate a new random coord for the minion based on the master's coord
                    int minionX = masterX + (Rnd.nextInt(401) - 200);
                    int minionY = masterY + (Rnd.nextInt(401) - 200);
                    int minionZ = masterZ;
                    while (((minionX != (masterX + 30)) && (minionX != (masterX - 30))) || ((minionY != (masterY + 30)) && (minionY != (masterY - 30))))
                    {
                        minionX = masterX + (Rnd.nextInt(401) - 200);
                        minionY = masterY + (Rnd.nextInt(401) - 200);
                    }

                    // Move the minion to the new coords
                    if (!minion.isInCombat() && !minion.isDead() && !minion.isMovementDisabled())
                    {
                        minion.moveToLocation(minionX, minionY, minionZ, 0);
                    }
                }
            }
        }
    }

    public void callMinionsToAssist(L2Character attacker)
    {
        if (_minionList.hasMinions())
        {
            List<L2MinionInstance> spawnedMinions = _minionList.getSpawnedMinions();
            if (spawnedMinions != null && spawnedMinions.size() > 0)
            {
                Iterator<L2MinionInstance> itr = spawnedMinions.iterator();
                L2MinionInstance minion;
                while (itr.hasNext())
                {
                    minion = itr.next();
                    // Trigger the aggro condition of the minion
                    if (minion != null && !minion.isDead())
                    {
                        if(this instanceof L2RaidBossInstance)
                        	minion.addDamage(attacker, 100);
                        else minion.addDamage(attacker, 1);
                    }
                }
            }
        }
    }

    @Override
	public boolean doDie(L2Character killer)
    {
    	if (!super.doDie(killer))
    		return false;

    	((L2Attackable)this)._ssrecharged = true;
    	((L2Attackable)this)._spsrecharged = true;
    	
    	if (_minionMaintainTask != null)
            _minionMaintainTask.cancel(true); // doesn't do it?

        if (this instanceof L2RaidBossInstance)
        	deleteSpawnedMinions();
        return true;
    }

    public List<L2MinionInstance> getSpawnedMinions()
    {
        return _minionList.getSpawnedMinions();
    }

    public int getTotalSpawnedMinionsInstances()
    {
        return _minionList.countSpawnedMinions();
    }

    public int getTotalSpawnedMinionsGroups()
    {
        return _minionList.lazyCountSpawnedMinionsGroups();
    }

    public void notifyMinionDied(L2MinionInstance minion)
    {
        _minionList.moveMinionToRespawnList(minion);
    }

    public void notifyMinionSpawned(L2MinionInstance minion)
    {
        _minionList.addSpawnedMinion(minion);
    }

    public boolean hasMinions()
    {
        return _minionList.hasMinions();
    }

    @Override
	public void addDamageHate(L2Character attacker, int damage, int aggro)
    {
        //if (!(attacker instanceof L2MonsterInstance))
        //{
            super.addDamageHate(attacker, damage, aggro);
        //}
    }

    @Override
	public void deleteMe()
    {
        // [L2J_JP ADD SANDMAN]
        if(hideTask != null)
            hideTask.cancel(true);

        if (hasMinions())
        {
            if (_minionMaintainTask != null)
                _minionMaintainTask.cancel(true);

            deleteSpawnedMinions();
        }
        super.deleteMe();
    }

    public void deleteSpawnedMinions()
    {
        for(L2MinionInstance minion : getSpawnedMinions())
        {
        	if (minion == null) continue;
        	minion.abortAttack();
        	minion.abortCast();
        	minion.deleteMe();
        	getSpawnedMinions().remove(minion);
        }
    	_minionList.clearRespawnList();
    }

    // [L2J_JP ADD SANDMAN START]
    public void hideMe()
    {
        if(hasMinions())
        {
            if (_minionMaintainTask != null)
            	_minionMaintainTask.cancel(true);

            deleteSpawnedMinions();
        }
        super.deleteMe();
    }
    
    public void shownMe()
    {
        if(!(this.isDead()))
            this.spawnMe();
    }
    
    private class doHide implements Runnable
    {
        private L2MonsterInstance _me;
        
        public doHide(L2MonsterInstance actor)
        {
            _me = actor;
        }
        
        public void run()
        {
            _me.hideMe();
            ThreadPoolManager.getInstance().scheduleGeneral(new doShown(_me),_me.getSpawn().getRespawnDelay());
        }
    }
    
    private class doShown implements Runnable
    {
        private L2MonsterInstance _me;
        
        public doShown(L2MonsterInstance actor)
        {
            _me = actor;
        }
        
        public void run()
        {
            if(_me.getSpawn().IsRespawnable())
                _me.shownMe();
        }
    }
}
