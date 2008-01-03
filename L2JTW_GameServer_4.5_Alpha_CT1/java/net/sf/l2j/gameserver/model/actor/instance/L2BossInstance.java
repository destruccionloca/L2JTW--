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
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.gameserver.clientpackets.Say2;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.StopMove;
import net.sf.l2j.gameserver.ThreadPoolManager;
import javolution.util.FastList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;


/**
 * This class manages all RaidBoss.
 *
 * @version $Revision: 1.0.0.0 $ $Date: 2006/06/16 $
 */
public final class L2BossInstance extends L2MonsterInstance
{
	//protected static Logger _log = Logger.getLogger(L2BossInstance.class.getName());
	private boolean _teleportedToNest;

	// Baium variables
    private int _BaiumTalk = 0;
    private int _BaiumStance = 0;

    private int _BaiumStanceChange = 0;
    private int _BaiumMinionTick = 0;
    private int _BaiumConfuseTick = 0;
    private int _BaiumFearTick = 0;
    
    private boolean _BaiumBerserk = false;
    private boolean _BaiumFlash = false;
    private boolean _BaiumCherubs = false;
    private boolean _BaiumDeath = false;
    private boolean _BaiumThroes = false;
    
    private ScheduledFuture _taskBaiumTalk;
    
    public static final int BAIUM_STANCE_NEUTRAL = 0;
    public static final int BAIUM_STANCE_OFFENSIVE = 1;
    public static final int BAIUM_STANCE_DEFENSIVE = 2;
    public static final int BAIUM_STANCE_BERSERK = 3;
    
    protected Future minionMaintainTask = null;
    private L2Character [] _OldChar = new L2Character[7];
    
    protected L2PcInstance _TargetForKill = null;
    private static final int BOSS_MAINTENANCE_INTERVAL = 10000;

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
    
    protected boolean _isInSocialAction = false;
    
    public boolean IsInSocialAction()
    {
        return _isInSocialAction;
    }
    public void setIsInSocialAction(boolean value)
    {
        _isInSocialAction = value;
    }
    
    // [L2J_JP ADD END SANDMAN]

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

    	switch (getTemplate().npcId) {
        case 29020: // Baium does his animation
            setIsInvul(true);
            disableAllSkills();
            SocialAction atk = new SocialAction(getObjectId(), 2);
            broadcastPacket(atk);
            startBaiumTalk();
            
            _BaiumDeath = true;
            _BaiumThroes = false;
            _BaiumBerserk = false;
            _BaiumFlash = false;
            _BaiumCherubs = false;

            break;
        default:
            break;

    }
    	super.onSpawn(); // ????? 

    }
    

    

    
    
    /**
     * Reduce the current HP of the L2Attackable, update its _aggroList and launch the doDie Task if necessary.<BR><BR>
     *
     */
    @Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
    {
        switch (getTemplate().npcId)
        {
            case 29014: // Orfen
                if ((getCurrentHp() - damage) < getMaxHp() / 2 && !getTeleported())
                {
                    clearAggroList();
                    getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
                    teleToLocation(43577,15985,-4396, false);
                    setTeleported(true);
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

    /**
     * Ends Baium's speech loop.<BR>
     */
    public void stopBaiumTalk()
    {
        if (_taskBaiumTalk != null)
        {
            _taskBaiumTalk.cancel(false);
            _taskBaiumTalk = null;
        }
    }
    
    /**
     * Begin Baium's initial speech loop.<BR>
     */
    public void startBaiumTalk()
    {
        if (_taskBaiumTalk == null)
            _taskBaiumTalk = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new BaiumIntro(), 5000, 6000);
    }
    
    
    class BaiumIntro implements Runnable
    {
        public void run()
        {
            switch (_BaiumTalk) 
            {
                case 0:
                    CreatureSay cs = new CreatureSay(getObjectId(), Say2.SHOUT, "巴溫", "自由...終於獲得自由了....");
                    
                    for (L2PcInstance player : L2World.getInstance().getAllPlayers())
                    {
                        player.sendPacket(cs);
                    }
                    break;
                case 1:
                    cs = new CreatureSay(getObjectId(), Say2.SHOUT, "巴溫", "愚蠢的人類! 你們的行為將會造成末日. 就連神也阻止不了我.");
                    for (L2PcInstance player : L2World.getInstance().getAllPlayers())
                    {
                            player.sendPacket(cs);
                    }
                    break;
                case 2:
                    cs = new CreatureSay(getObjectId(), Say2.SHOUT, "巴溫", "讓我感謝你們讓我獲得自由,代價就是換取你們的亡魂.");
                    for (L2PcInstance player : L2World.getInstance().getAllPlayers())
                    {
                            player.sendPacket(cs);
                    }
                    break;
                case 3:
                    cs = new CreatureSay(getObjectId(), Say2.SHOUT, "巴溫", "在地獄狀態,將釋放到這世界...");
                    for (L2PcInstance player : L2World.getInstance().getAllPlayers())
                    {
                            player.sendPacket(cs);
                    }
                    break;
                case 4:
                    setIsInvul(false);
                    enableAllSkills();
                    SocialAction atk = new SocialAction(getObjectId(), 1);
                    broadcastPacket(atk);
                    cs = new CreatureSay(getObjectId(), Say2.SHOUT, "巴溫", "現在..... 死吧!!");
                    for (L2PcInstance player : L2World.getInstance().getAllPlayers())
                    {
                            player.sendPacket(cs);
                    }
                    //setIsAttacked(true);
                    break;
            }
            _BaiumTalk++;
            if (_BaiumTalk == 5) 
            {
                _BaiumTalk = 0;
                stopBaiumTalk();
            }
            
            if (_BaiumTalk >= 6) {
                switch (_BaiumTalk) {
                // "ENOUGH! You mortals have proven yourselves a threat."
                // "You have entered my prison and sanctum to awaken me and then entrap me once again?"
                // "I am IMMORTAL.  I have the power of the Gods at my disposal. You cannot win, despite your tenacity."
                // "Perhaps you should now realize your own futility.  Do you think you can defeat a GOD?"
                
                    case 6:
                        CreatureSay cs = new CreatureSay(getObjectId(), Say2.ALL, "巴溫", "夠了...你們人類已經活夠了.");
                        
                        for (L2PcInstance player : L2World.getInstance().getAllPlayers())
                        {
                            player.sendPacket(cs);
                        }
                        break;
                    case 7:
                        cs = new CreatureSay(getObjectId(), Say2.ALL, "巴溫", "你們人類進入我的監牢喚醒我又想把我給封印?");
                        
                        for (L2PcInstance player : L2World.getInstance().getAllPlayers())
                        {
                            player.sendPacket(cs);
                        }
                        break;
                    case 8:
                        cs = new CreatureSay(getObjectId(), Say2.ALL, "巴溫", "我是永生的,只要有神的力量,你們是無法贏的.放棄吧人類..");
                        
                        for (L2PcInstance player : L2World.getInstance().getAllPlayers())
                        {
                            player.sendPacket(cs);
                        }
                        break;
                    case 9:
                        cs = new CreatureSay(getObjectId(), Say2.ALL, "巴溫", "看來你們已經了解自己的實力,你們還認為可以阻止神嗎?");
                        
                        for (L2PcInstance player : L2World.getInstance().getAllPlayers())
                        {
                            player.sendPacket(cs);
                        }
                        break;
                    case 10:
                        // transform
                        _log.info("Baium: Transform");
                        break;
                    case 11:
                        cs = new CreatureSay(getObjectId(), Say2.ALL, "巴溫", "再見了..人類...");
                        
                        for (L2PcInstance player : L2World.getInstance().getAllPlayers())
                        {
                            player.sendPacket(cs);
                        }
                        setIsInvul(false);
                        enableAllSkills();
                        break;
                }
                
                if (_BaiumTalk == 11) {
                    stopBaiumTalk();
                }
            }

        } // run
        
    } // class
    

    
}
