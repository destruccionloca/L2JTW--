/*
 * $HeadURL: $
 *
 * $Author: $
 * $Date: $
 * $Revision: $
 *
 * 
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

import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javolution.util.FastMap;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.instancemanager.FourSepulchersManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.util.Rnd;

/**
 * This class ...
 * ４大霊廟NPC
 * @version $Revision: $ $Date: $
 * @author  sandman
 */
public class L2SepulcherNpcInstance extends L2NpcInstance
{
    protected static Logger _log = Logger.getLogger(L2SepulcherNpcInstance.class.getName());

    // 会堂門番とドアの組み?わせ(<門番のID、ドアのID>)
    protected static Map<Integer,Integer> _HallGateKeepers = new FastMap<Integer,Integer>();

    // 会堂の扉を閉めるタスク
    protected Future _CloseTask = null;
    // 謎の?を出現させるタスク
    protected Future _SpawnNextMysteriousBoxTask = null;
    // ??スターを出現させるタスク
    protected Future _SpawnMonsterTask = null;
    
    // 会話用HTMLパス
    private final String _HTML_FILE_PATH = "data/html/SepulcherNpc/";
    
    // 会堂の鍵
    private final int _HallsKey = 7260;

    // コ?スト?クター
    public L2SepulcherNpcInstance(int objectID, L2NpcTemplate template)
    {
        super(objectID, template);

        // 会堂門番とドアの組み?わせを作成
        _HallGateKeepers.clear();
        _HallGateKeepers.put(31925, 25150012);  // 征?者の霊廟：男爵の会堂門番
        _HallGateKeepers.put(31926, 25150013);  // 征?者の霊廟：子爵の会堂門番
        _HallGateKeepers.put(31927, 25150014);  // 征?者の霊廟：?爵の会堂門番
        _HallGateKeepers.put(31928, 25150015);  // 征?者の霊廟：侯爵の会堂門番
        _HallGateKeepers.put(31929, 25150016);  // 征?者の霊廟：公爵の会堂門番
        _HallGateKeepers.put(31930, 25150002);  // 支配者の霊廟：男爵の会堂門番
        _HallGateKeepers.put(31931, 25150003);  // 支配者の霊廟：子爵の会堂門番
        _HallGateKeepers.put(31932, 25150004);  // 支配者の霊廟：?爵の会堂門番
        _HallGateKeepers.put(31933, 25150005);  // 支配者の霊廟：侯爵の会堂門番
        _HallGateKeepers.put(31934, 25150006);  // 支配者の霊廟：公爵の会堂門番
        _HallGateKeepers.put(31935, 25150032);  // 大賢者の霊廟：男爵の会堂門番
        _HallGateKeepers.put(31936, 25150033);  // 大賢者の霊廟：子爵の会堂門番
        _HallGateKeepers.put(31937, 25150034);  // 大賢者の霊廟：?爵の会堂門番
        _HallGateKeepers.put(31938, 25150035);  // 大賢者の霊廟：侯爵の会堂門番
        _HallGateKeepers.put(31939, 25150036);  // 大賢者の霊廟：公爵の会堂門番
        _HallGateKeepers.put(31940, 25150022);  // 審判者の霊廟：男爵の会堂門番
        _HallGateKeepers.put(31941, 25150023);  // 審判者の霊廟：子爵の会堂門番
        _HallGateKeepers.put(31942, 25150024);  // 審判者の霊廟：?爵の会堂門番
        _HallGateKeepers.put(31943, 25150025);  // 審判者の霊廟：侯爵の会堂門番
        _HallGateKeepers.put(31944, 25150026);  // 審判者の霊廟：公爵の会堂門番

        if(_CloseTask != null) _CloseTask.cancel(true);
        if(_SpawnNextMysteriousBoxTask != null) _SpawnNextMysteriousBoxTask.cancel(true);
        if(_SpawnMonsterTask != null) _SpawnMonsterTask.cancel(true);
        _CloseTask = null;
        _SpawnNextMysteriousBoxTask = null;
        _SpawnMonsterTask = null; 

    }

    public void deleteMe()
    {
        if (_CloseTask != null)
        {
        	_CloseTask.cancel(true);
        	_CloseTask = null;
        }
        if (_SpawnNextMysteriousBoxTask != null)
        {
        	_SpawnNextMysteriousBoxTask.cancel(true);
        	_SpawnNextMysteriousBoxTask = null;
        }
        if(_SpawnMonsterTask != null)
    	{
        	_SpawnMonsterTask.cancel(true);
            _SpawnMonsterTask = null; 
    	}

        super.deleteMe();
    }

    // アクシ??
    public void onAction(L2PcInstance player)
    {
        // Check if the L2PcInstance already target the L2NpcInstance
        if (this != player.getTarget())
        {
            if (Config.DEBUG) _log.fine("new target selected:"+getObjectId());
            
            // Set the target of the L2PcInstance player
            player.setTarget(this);
            
            // Send a Server->Client packet MyTargetSelected to the L2PcInstance player
            // The player.getLevel() - getLevel() permit to display the correct color in the select window
            MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
            player.sendPacket(my);
            
            // Check if the player is attackable (without a forced attack)
            if (isAutoAttackable(player))
            {   
                // Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
                StatusUpdate su = new StatusUpdate(getObjectId());
                su.addAttribute(StatusUpdate.CUR_HP, (int)getCurrentHp() );
                su.addAttribute(StatusUpdate.MAX_HP, getMaxHp() );
                player.sendPacket(su);
            }
            
            // Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
            player.sendPacket(new ValidateLocation(this));
        }
        else
        {
            // Send a Server->Client packet MyTargetSelected to the L2PcInstance player
            // The player.getLevel() - getLevel() permit to display the correct color
            MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
            player.sendPacket(my);
            
            // Check if the player is attackable (without a forced attack) and isn't dead
            if (isAutoAttackable(player) && !isAlikeDead())
            {
                // Check the height difference
                if (Math.abs(player.getZ() - getZ()) < 400) // this max heigth difference might need some tweaking
                {
                    // Set the L2PcInstance Intention to AI_INTENTION_ATTACK
                    player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
                }
                else
                {
                    // Send a Server->Client packet ActionFailed (target is out of attack range) to the L2PcInstance player
                    player.sendPacket(ActionFailed.STATIC_PACKET);
                }
            }
            
            if(!isAutoAttackable(player)) 
            {
                // Calculate the distance between the L2PcInstance and the L2NpcInstance
                if (!isInsideRadius(player, INTERACTION_DISTANCE, false, false))
                {
                    // player.setCurrentState(L2Character.STATE_INTERACT);
                    // player.setInteractTarget(this);
                    // player.moveTo(this.getX(), this.getY(), this.getZ(), INTERACTION_DISTANCE);
                    
                    // Notify the L2PcInstance AI with AI_INTENTION_INTERACT
                    player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
                    
                    // Send a Server->Client packet ActionFailed (target is out of interaction range) to the L2PcInstance player
                    player.sendPacket(ActionFailed.STATIC_PACKET);
                } 
                else 
                {
                    // Send a Server->Client packet SocialAction to the all L2PcInstance on the _knownPlayer of the L2NpcInstance
                    // to display a social action of the L2NpcInstance on their client
                    SocialAction sa = new SocialAction(getObjectId(), Rnd.get(8));
                    broadcastPacket(sa);
                    
                    doAction(player);

                    // Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
                    player.sendPacket(ActionFailed.STATIC_PACKET);                  
                }
            }
        }
    }

    // アクシ??実行
    private void doAction(L2PcInstance player)
    {
    	// ?んでいる場?は何もしない
        if(isDead())
        {
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        
        switch(getNpcId())
        {
            // 謎の?ならば、?んでMobを湧かせる
            case 31468:
            case 31469:
            case 31470:
            case 31471:
            case 31472:
            case 31473:
            case 31474:
            case 31475:
            case 31476:
            case 31477:
            case 31478:
            case 31479:
            case 31480:
            case 31481:
            case 31482:
            case 31483:
            case 31484:
            case 31485:
            case 31486:
            case 31487:
                reduceCurrentHp(getMaxHp() + 1, player);
                if(_SpawnMonsterTask != null) _SpawnMonsterTask.cancel(true);
                _SpawnMonsterTask = 
                	ThreadPoolManager.getInstance().scheduleEffect(new SpawnMonster(getNpcId()),3500); 
                break;
                
            // 鍵の?ならば、?んで鍵をPCに渡す
            case 31455:
            case 31456:
            case 31457:
            case 31458:
            case 31459:
            case 31460:
            case 31461:
            case 31462:
            case 31463:
            case 31464:
            case 31465:
            case 31466:
            case 31467:
                reduceCurrentHp(getMaxHp() + 1, player);
				player.addItem("Quest", _HallsKey, 1, player, true);
                break;

                // それ以外は?通に会話する
            default:
                showChatWindow(player,0);
        }
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }
    
    // チ?ット用HTMLファイ?パスの取得
    public String getHtmlPath(int npcId, int val)
    {
        String pom = "";
        if (val == 0)
        {
            pom = "" + npcId;
        }
        else
        {
            pom = npcId + "-" + val;
        }

        return _HTML_FILE_PATH + pom + ".htm";
    }

    // チ?ット?表示
    public void showChatWindow(L2PcInstance player, int val)
    {
        String filename = getHtmlPath(getNpcId(),val);
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile(filename);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        player.sendPacket(html);
        player.sendPacket( ActionFailed.STATIC_PACKET );
    }
    
    // バイパスコマ?ド??
    public void onBypassFeedback(L2PcInstance player, String command)
    {
        if (player == null) return;
        // Get the distance between the L2PcInstance and the L2SepulcherNpcInstance
        if (!isInsideRadius(player, INTERACTION_DISTANCE, false, false))
        {
            player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
        } 
        else 
        {
            if (isBusy())
            {
                player.sendPacket( ActionFailed.STATIC_PACKET );
                
                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                html.setFile("data/html/npcbusy.htm");
                html.replace("%busymessage%", getBusyMessage());
                html.replace("%npcname%", getName());
                html.replace("%playername%", player.getName());
                player.sendPacket(html);
            }
            // チ?ット
            else if (command.startsWith("Chat"))
            {
                int val = 0;
                try 
                {
                    val = Integer.parseInt(command.substring(5));
                } catch (IndexOutOfBoundsException ioobe) {
                } catch (NumberFormatException nfe) {}
                showChatWindow(player, val);
            }
            // 会堂の扉を開く
            else if (command.startsWith("open_gate"))
            {
                L2ItemInstance HallsKey = player.getInventory().getItemByItemId(_HallsKey);
                // 会堂の鍵を?っていて、かつ、霊廟攻略?間で?れば、扉は開く
                if(HallsKey != null && FourSepulchersManager.getInstance().IsAttackTime())
                {
                    switch(getNpcId())
                    {
                    	// 公爵の街道門番の場?はハ?シ?シ?ドーを出現させる。
                    	case 31929:
                    	case 31934:
                    	case 31939:
                    	case 31944:
                    		FourSepulchersManager.getInstance().SpawnShadow(getNpcId());
                    	default:
                        	// 扉を開ける
                            OpenNextDoor(getNpcId());
                            // 会堂の鍵をイ?ベ?ト?から消す
                            player.destroyItemByItemId("Quest", _HallsKey, HallsKey.getCount(), player, true);
                    }                	
                }
                else
                {
        			{
        				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
        				sm.addString("条?を?たしていないため、入場が?否されました。");
        				player.sendPacket(sm);
        			}
                }
            }
            // 霊廟内に入る
            else if (command.startsWith("Entry"))
            {
            	FourSepulchersManager.getInstance().Entry(getNpcId(),player);
            }
            else
            {
                super.onBypassFeedback(player, command);
            }
        }
    }

    // ?の会堂のドアを開ける
    public void OpenNextDoor(int npcId)
    {
        int DoorId = _HallGateKeepers.get(npcId).intValue();
        DoorTable _doorTable = DoorTable.getInstance();
        _doorTable.getDoor(DoorId).openMe();
        
        // 10秒後に閉じる
        if(_CloseTask != null) _CloseTask.cancel(true);
        _CloseTask = ThreadPoolManager.getInstance().scheduleEffect(new CloseNextDoor(npcId,DoorId),10000);
    }

    // 会堂のドアを閉じる
    private class CloseNextDoor implements Runnable
    {
        final DoorTable _DoorTable = DoorTable.getInstance();
        private int _NpcId;
        private int _DoorId;
        
        public CloseNextDoor(int npcId,int doorId)
        {
        	_NpcId = npcId;
            _DoorId = doorId;
        }
        
        public void run()
        {
            try
            {
                _DoorTable.getDoor(_DoorId).closeMe();
            }
            catch (Exception e)
            {
                _log.warning(e.getMessage());
            }
            
            // 謎の?を出現させる(不正な進入経路からの攻略を防ぐため、ドアが閉まらないと出現しない)
            if(_SpawnNextMysteriousBoxTask != null) _SpawnNextMysteriousBoxTask.cancel(true);
            _SpawnNextMysteriousBoxTask = ThreadPoolManager.getInstance().scheduleEffect(new SpawnNextMysteriousBox(_NpcId),10000);
        }
    }

    // 謎の?を出現させるタスク
    private class SpawnNextMysteriousBox implements Runnable
    {
    	private int _NpcId;
    	public SpawnNextMysteriousBox(int npcId)
    	{
    		_NpcId = npcId;
    	}
        public void run()
        {
        	FourSepulchersManager.getInstance().SpawnMysteriousBox(_NpcId);
        }
    }

    // ??スターを出現させるタスク
    private class SpawnMonster implements Runnable
    {
    	private int _NpcId;
    	public SpawnMonster(int npcId)
    	{
    		_NpcId = npcId;
    	}
        public void run()
        {
            FourSepulchersManager.getInstance().SpawnMonster(_NpcId);
        }
    }
}
