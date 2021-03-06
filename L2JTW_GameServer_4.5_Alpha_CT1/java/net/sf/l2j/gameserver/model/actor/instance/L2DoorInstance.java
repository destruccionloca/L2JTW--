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

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2DoorAI;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.FortManager;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.knownlist.DoorKnownList;
import net.sf.l2j.gameserver.model.actor.stat.DoorStat;
import net.sf.l2j.gameserver.model.actor.status.DoorStatus;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.Fort;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.DoorStatusUpdate;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.serverpackets.StaticObject; 

/**
 * This class ...
 *
 * @version $Revision: 1.3.2.2.2.5 $ $Date: 2005/03/27 15:29:32 $
 */
public class L2DoorInstance extends L2Character
{
    protected static final Logger log = Logger.getLogger(L2DoorInstance.class.getName());

    /** The castle index in the array of L2Castle this L2NpcInstance belongs to */
    private int _castleIndex = -2;
    private int _mapRegion = -1;
    /** The fort index in the array of L2Fort this L2NpcInstance belongs to */
    private int _fortIndex = -2;

    // when door is closed, the dimensions are
    private int _rangeXMin = 0;
    private int _rangeYMin = 0;
    private int _rangeZMin = 0;
    private int _rangeXMax = 0;
    private int _rangeYMax = 0;
    private int _rangeZMax = 0;

    protected final int _doorId;
    protected final String _name;
    private int _open;
    private boolean _unlockable;

    private ClanHall _clanHall;

    protected int _autoActionDelay = -1;
    private ScheduledFuture<?> _autoActionTask;


    /** This class may be created only by L2Character and only for AI */
    public class AIAccessor extends L2Character.AIAccessor
    {
        protected AIAccessor() {}
        @Override
		public L2DoorInstance getActor() { return L2DoorInstance.this; }
        @Override
		@SuppressWarnings("unused")
        public void moveTo(int x, int y, int z, int offset) {}
        @Override
		@SuppressWarnings("unused")
        public void moveTo(int x, int y, int z) {}
        @Override
		@SuppressWarnings("unused")
        public void stopMove(L2CharPosition pos) {}
        @Override
		@SuppressWarnings("unused")
        public void doAttack(L2Character target) {}
        @Override
		@SuppressWarnings("unused")
        public void doCast(L2Skill skill) {}
    }

    @Override
	public L2CharacterAI getAI() {
        if (_ai == null)
        {
            synchronized(this)
            {
                if (_ai == null)
                    _ai = new L2DoorAI(new AIAccessor());
            }
        }
        return _ai;
    }

    @Override
	public boolean hasAI() {
        return (_ai != null);
    }

    class CloseTask implements Runnable
    {
        public void run()
        {
            try
            {
                onClose();
            }
            catch (Throwable e)
            {
                log.log(Level.SEVERE, "", e);
            }
        }
    }

    /**
     * Manages the auto open and closing of a door.
     */
    class AutoOpenClose implements Runnable
    {
        public void run()
        {
            try {
                String doorAction;

                if (getOpen() == 1) {
                    doorAction = "opened";
                    openMe();
                }
                else {
                    doorAction = "closed";
                    closeMe();
                }

                if (Config.DEBUG)
                    log.info("Auto " + doorAction + " door ID " + _doorId + " (" + _name + ") for " + (_autoActionDelay / 60000) + " minute(s).");
            }
            catch (Exception e) {
                log.warning("Could not auto open/close door ID " + _doorId + " (" + _name + ")");
            }
        }
    }

    /**
     */
    public L2DoorInstance(int objectId, L2CharTemplate template, int doorId, String name, boolean unlockable)
    {
        super(objectId, template);
        getKnownList();	// init knownlist
        getStat();			// init stats
        getStatus();		// init status
        _doorId = doorId;
        _name = name;
        _unlockable = unlockable;
    }

    @Override
	public final DoorKnownList getKnownList()
    {
    	if(!(super.getKnownList() instanceof DoorKnownList))
    		setKnownList(new DoorKnownList(this));
    	return (DoorKnownList)super.getKnownList();
    }

    @Override
	public final DoorStat getStat()
    {
    	if(!(super.getStat() instanceof DoorStat))
    		setStat(new DoorStat(this));
    	return (DoorStat)super.getStat();
    }

    @Override
	public final DoorStatus getStatus()
    {
    	if(!(super.getStatus() instanceof DoorStatus))
    		setStatus(new DoorStatus(this));
    	return (DoorStatus)super.getStatus();
    }

    public final boolean isUnlockable()
    {
        return _unlockable;
    }

    @Override
	public final int getLevel()
    {
        return 1;
    }

    /**
     * @return Returns the doorId.
     */
    public int getDoorId()
    {
        return _doorId;
    }

    /**
     * @return Returns the open.
     */
    public int getOpen()
    {
        return _open;
    }
    /**
     * @param open The open to set.
     */
    public void setOpen(int open)
    {
        _open = open;
    }

    /**
     * Sets the delay in milliseconds for automatic opening/closing
     * of this door instance.
     * <BR>
     * <B>Note:</B> A value of -1 cancels the auto open/close task.
     *
     * @param int actionDelay
     */
    public void setAutoActionDelay(int actionDelay)
    {
        if (_autoActionDelay == actionDelay)
            return;

        if (actionDelay > -1) {
            AutoOpenClose ao = new AutoOpenClose();
            ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(ao, actionDelay, actionDelay);
        }
        else {
            if (_autoActionTask != null)
                _autoActionTask.cancel(false);
        }

        _autoActionDelay = actionDelay;
    }

    public int getDamage()
    {
        int dmg = 6 - (int)Math.ceil(getCurrentHp() / getMaxHp() * 6);
        if (dmg > 6)
            return 6;
        if (dmg < 0)
            return 0;
        return dmg;
    }

    public final Castle getCastle()
    {
        if (_castleIndex < 0) _castleIndex = CastleManager.getInstance().getCastleIndex(this);
        if (_castleIndex < 0) return null;
        return CastleManager.getInstance().getCastles().get(_castleIndex);
    }

    public final Fort getFort()
    {
        if (_fortIndex < 0) _fortIndex = FortManager.getInstance().getFortIndex(this);
        if (_fortIndex < 0) return null;
        return FortManager.getInstance().getForts().get(_fortIndex);
    }
    
    public void setClanHall(ClanHall clanhall)
    {
	_clanHall = clanhall;
    }
    public ClanHall getClanHall()
    {
	return _clanHall;
    }

    public boolean isEnemy()
    {
    	if (getCastle() != null
                && getCastle().getCastleId() > 0
                && getCastle().getSiege().getIsInProgress())
    		return true;
    	if (getFort() != null
                && getFort().getFortId() > 0
                && getFort().getSiege().getIsInProgress())
    		return true;
    	return false;
    }

    @Override
	public boolean isAutoAttackable(L2Character attacker)
    {
        if (isUnlockable())
            return true;

        // Doors can`t be attacked by NPCs
        if (!(attacker instanceof L2PcInstance)) return false;
        
        // Attackable  only during siege by everyone
        boolean isCastle = (getCastle() != null
                           && getCastle().getCastleId() > 0
                           && getCastle().getSiege().getIsInProgress());

        boolean isFort = (getFort() != null
                && getFort().getFortId() > 0
                && getFort().getSiege().getIsInProgress());
                           
        return (isCastle || isFort);
    }

    public boolean isAttackable(L2Character attacker)
    {
        return isAutoAttackable(attacker);
    }

    @Override
	public void updateAbnormalEffect() {}

    public int getDistanceToWatchObject(L2Object object)
    {
        if (!(object instanceof L2PcInstance))
            return 0;
        return 2000;
    }

    /**
     * Return the distance after which the object must be remove from _knownObject according to the type of the object.<BR><BR>
     *
     * <B><U> Values </U> :</B><BR><BR>
     * <li> object is a L2PcInstance : 4000</li>
     * <li> object is not a L2PcInstance : 0 </li><BR><BR>
     *
     */
    public int getDistanceToForgetObject(L2Object object)
     {
        if (!(object instanceof L2PcInstance))
            return 0;

        return 4000;
    }

    /**
     * Return null.<BR><BR>
     */
    @Override
	public L2ItemInstance getActiveWeaponInstance()
    {
        return null;
    }

    @Override
	public L2Weapon getActiveWeaponItem()
    {
        return null;
    }

    @Override
	public L2ItemInstance getSecondaryWeaponInstance()
    {
        return null;
    }

    @Override
    public L2Weapon getSecondaryWeaponItem()
    {
        return null;
    }

    @Override
    public void onAction(L2PcInstance player)
    {
        if (player == null)
            return;

        // Check if the L2PcInstance already target the L2NpcInstance
        if (this != player.getTarget())
        {
            // Set the target of the L2PcInstance player
            player.setTarget(this);

            // Send a Server->Client packet MyTargetSelected to the L2PcInstance player
            MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
            player.sendPacket(my);

            StaticObject su = new StaticObject(this, false);

            // send HP amount if doors are inside castle/fortress zone
        	// TODO: needed to be added here doors from conquerable clanhalls
            if (getCastle() != null && getCastle().getCastleId() > 0
            		|| getFort() != null && getFort().getFortId() > 0)
            	su = new StaticObject(this, true);
			player.sendPacket(su);

            // Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
            player.sendPacket(new ValidateLocation(this));
        }
        else
        {
//            MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel());
//            player.sendPacket(my);
            if (isAutoAttackable(player) )
            {
                if (Math.abs(player.getZ() - getZ()) < 400) // this max heigth difference might need some tweaking
                {
                    player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
                }
            }
            else if (player.getClan()!=null && getClanHall() != null && player.getClanId() == getClanHall().getOwnerId())
            {
                if (!isInsideRadius(player, L2NpcInstance.INTERACTION_DISTANCE, false, false))
                {
                    player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
                }
                else
                {
                    //need find serverpacket which ask open/close gate. now auto
                        //if (getOpen() == 1) player.sendPacket(new SystemMessage(1140));
                        //else player.sendPacket(new SystemMessage(1141));
                    if (getOpen() == 1) openMe();
                    else closeMe();
                }
            }
        }
        // Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    @Override
    public void onActionShift(L2GameClient client)
    {
        L2PcInstance player = client.getActiveChar();
        if (player == null) return;

        if (player.getAccessLevel().isGm())
        {
            player.setTarget(this);
            MyTargetSelected my = new MyTargetSelected(getObjectId(), player
                    .getLevel());
            player.sendPacket(my);

            StaticObject su = new StaticObject(this, false);
            // send HP amount if doors are inside castle/fortress zone
        	// TODO: needed to be added here doors from conquerable clanhalls
            if (getCastle() != null && getCastle().getCastleId() > 0
            		|| getFort() != null && getFort().getFortId() > 0)
            	su = new StaticObject(this, true);

            player.sendPacket(su);

            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            TextBuilder html1 = new TextBuilder("<html><body><table border=0>");
            html1.append("<tr><td>狀態：</td></tr>");
            html1.append("<tr><td>目前HP "+getCurrentHp()+ "</td></tr>");
            html1.append("<tr><td>最大HP "+getMaxHp()+"</td></tr>");

            html1.append("<tr><td>物件ID： " + getObjectId() + "</td></tr>");
            html1.append("<tr><td>門的ID：<br>"+getDoorId()+"</td></tr>");
            html1.append("<tr><td><br></td></tr>");

            html1.append("<tr><td>類型： " + getClass().getName() + "</td></tr>");
            html1.append("<tr><td><br></td></tr>");
            html1.append("</table>");

            html1.append("<table><tr>");

            html1.append("<td><button value=\"開啟\" action=\"bypass -h admin_open "+getDoorId()+"\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
            html1.append("<td><button value=\"關閉\" action=\"bypass -h admin_close "+getDoorId()+"\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
            html1.append("<td><button value=\"摧毀\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
            html1.append("<td><button value=\"刪除\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");

            html1.append("</tr></table></body></html>");

            html.setHtml(html1.toString());
            player.sendPacket(html);
        } else {
            // ATTACK the mob without moving?
        }

        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    @Override
	public void broadcastStatusUpdate()
    {
        Collection<L2PcInstance> knownPlayers = getKnownList().getKnownPlayers().values();
        if (knownPlayers == null || knownPlayers.isEmpty())
            return;


        StaticObject su = new StaticObject(this, false);
        DoorStatusUpdate dsu  = new DoorStatusUpdate(this);

        for (L2PcInstance player : knownPlayers)
        {
            if (getCastle() != null && getCastle().getCastleId() > 0
            		|| getFort() != null && getFort().getFortId() > 0)
            	su = new StaticObject(this, true);

            player.sendPacket(su);
            player.sendPacket(dsu);
        }
    }

    public void onOpen()
    {
    	ThreadPoolManager.getInstance().scheduleGeneral(new CloseTask(), 60000);
    }

    public void onClose()
    {
        closeMe();
    }

    public final void closeMe()
    {
        setOpen(1);
        broadcastStatusUpdate();
    }

    public final void openMe()
    {
        setOpen(0);
        broadcastStatusUpdate();
    }

    @Override
	public String toString()
    {
        return "door "+_doorId;
    }

    public String getDoorName()
    {
        return _name;
    }

    public int getXMin()
    {
    	return _rangeXMin;
    }

    public int getYMin()
    {
    	return _rangeYMin;
    }

    public int getZMin()
    {
    	return _rangeZMin;
    }

    public int getXMax()
    {
    	return _rangeXMax;
    }

    public int getYMax()
    {
    	return _rangeYMax;
    }

    public int getZMax()
    {
    	return _rangeZMax;
    }

    public void setRange(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax)
    {
    	_rangeXMin = xMin;
    	_rangeYMin = yMin;
    	_rangeZMin = zMin;

    	_rangeXMax = xMax;
    	_rangeYMax = yMax;
    	_rangeZMax = zMax;
    }

    public int getMapRegion()
    {
    	return _mapRegion;
    }

    public void setMapRegion(int region)
    {
    	_mapRegion = region;
    }

    public Collection<L2SiegeGuardInstance> getKnownSiegeGuards()
    {
        FastList<L2SiegeGuardInstance> result = new FastList<L2SiegeGuardInstance>();

        for (L2Object obj : getKnownList().getKnownObjects().values())
        {
            if (obj instanceof L2SiegeGuardInstance) result.add((L2SiegeGuardInstance) obj);
        }

        return result;
    }
}
