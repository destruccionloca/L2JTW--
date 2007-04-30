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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.CharInfo;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.UserInfo;

/**
 * This class handles following admin commands:
 * - enchant_armor
 * 
 * @version $Revision: 1.3.2.1.2.10 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminEncArmor implements IAdminCommandHandler {
    private static Logger _log = Logger.getLogger(AdminEncArmor.class.getName());
    private static String[] _adminCommands = {
        "admin_seteh",//頭6
        "admin_setec",//上衣10
        "admin_seteg",//手9
        "admin_setel",//褲子11
        "admin_seteb",//腳12
        "admin_setes",//左手盾8
        "admin_setle",//左耳1
        "admin_setre",//右耳2
        "admin_setlf",//左戒指4
        "admin_setrf",//右戒指5
        "admin_seten",//項鍊3
        "admin_setun",//泳裝0
        "admin_setba",//披風13
        "admin_enchant",
        };
    private static final int REQUIRED_LEVEL = Config.GM_MENU;

    public boolean useAdminCommand(String command, L2PcInstance activeChar) 
    {
        if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
        if (command.startsWith("admin_seteh"))
        {
            try
            {   
                String val = command.substring(12);
                int ench = Integer.parseInt(val);
                setTarget(activeChar, ench ,Inventory.PAPERDOLL_HEAD);
            }
            catch (StringIndexOutOfBoundsException e)
            {
                if ( Config.DEVELOPER ) System.out.println("Set helmet enchant error: "+e);
                SystemMessage sm = new SystemMessage(614);
                sm.addString("SYS");
                sm.addString("請輸入強化數值");
                activeChar.sendPacket(sm);
            }
        }   
        else if (command.equals("admin_enchant"))
        {
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_setec"))
        {
            try
            {   
                String val = command.substring(12);
                int ench = Integer.parseInt(val);
                setTarget(activeChar, ench ,Inventory.PAPERDOLL_CHEST);
            }
            catch (StringIndexOutOfBoundsException e)
            {
                if ( Config.DEVELOPER ) System.out.println("Set chest armor enchant error: "+e);
                SystemMessage sm = new SystemMessage(614);
                sm.addString("SYS");
                sm.addString("請輸入強化數值");
                activeChar.sendPacket(sm);
            }
        }   
        else if (command.startsWith("admin_seteg"))
        {
            try
            {   
                String val = command.substring(12);
                int ench = Integer.parseInt(val);
                setTarget(activeChar, ench ,Inventory.PAPERDOLL_GLOVES);
            }
            catch (StringIndexOutOfBoundsException e)
            {
                if ( Config.DEVELOPER ) System.out.println("Set gloves enchant error: "+e);
                SystemMessage sm = new SystemMessage(614);
                sm.addString("SYS");
                sm.addString("請輸入欲精鍊的值");
                activeChar.sendPacket(sm);
            }
            }
        else if (command.startsWith("admin_seteb"))
        {
            try
            {   
                String val = command.substring(12);
                int ench = Integer.parseInt(val);
                setTarget(activeChar, ench ,Inventory.PAPERDOLL_FEET);
            }
            catch (StringIndexOutOfBoundsException e)
            {
                if ( Config.DEVELOPER ) System.out.println("Set boots enchant error: "+e);
                SystemMessage sm = new SystemMessage(614);
                sm.addString("SYS");
                sm.addString("請輸入強化數值");
                activeChar.sendPacket(sm);
            }
        }
        else if (command.startsWith("admin_setel"))
        {
            try
            {   
                String val = command.substring(12);
                int ench = Integer.parseInt(val);
                setTarget(activeChar, ench ,Inventory.PAPERDOLL_LEGS);
            }
            catch (StringIndexOutOfBoundsException e)
            {
                if ( Config.DEVELOPER ) System.out.println("Set leggings enchant error: "+e);
                SystemMessage sm = new SystemMessage(614);
                sm.addString("SYS");
                sm.addString("請輸入強化數值");
                activeChar.sendPacket(sm);
            }
        }
        else if (command.startsWith("admin_setes"))
        {
            try
            {   
                String val = command.substring(12);
                int ench = Integer.parseInt(val);
                setTarget(activeChar, ench ,Inventory.PAPERDOLL_LHAND);
            }
            catch (StringIndexOutOfBoundsException e)
            {
                if ( Config.DEVELOPER ) System.out.println("Set shield enchant error: "+e);
                SystemMessage sm = new SystemMessage(614);
                sm.addString("SYS");
                sm.addString("請輸入強化數值");
                activeChar.sendPacket(sm);
            }
        }
        else if (command.startsWith("admin_setle"))
        {
            try
            {   
                String val = command.substring(12);
                int ench = Integer.parseInt(val);
                setTarget(activeChar, ench ,Inventory.PAPERDOLL_LEAR);
            }
            catch (StringIndexOutOfBoundsException e)
            {
                if ( Config.DEVELOPER ) System.out.println("Set Left Earring enchant error: "+e);
                SystemMessage sm = new SystemMessage(614);
                sm.addString("SYS");
                sm.addString("請輸入強化數值");
                activeChar.sendPacket(sm);
            }
        }
        else if (command.startsWith("admin_setre"))
        {
            try
            {   
                String val = command.substring(12);
                int ench = Integer.parseInt(val);
                setTarget(activeChar, ench ,Inventory.PAPERDOLL_REAR);
            }
            catch (StringIndexOutOfBoundsException e)
            {
                if ( Config.DEVELOPER ) System.out.println("Set Right Earring enchant error: "+e);
                SystemMessage sm = new SystemMessage(614);
                sm.addString("SYS");
                sm.addString("請輸入強化數值");
                activeChar.sendPacket(sm);
            }
        }
        else if (command.startsWith("admin_setlf"))
        {
            try
            {   
                String val = command.substring(12);
                int ench = Integer.parseInt(val);
                setTarget(activeChar, ench ,Inventory.PAPERDOLL_LFINGER);
            }
            catch (StringIndexOutOfBoundsException e)
            {
                if ( Config.DEVELOPER ) System.out.println("Set Left Ring enchant error: "+e);
                SystemMessage sm = new SystemMessage(614);
                sm.addString("SYS");
                sm.addString("請輸入強化數值");
                activeChar.sendPacket(sm);
            }
        }
        else if (command.startsWith("admin_setrf"))
        {
            try
            {   
                String val = command.substring(12);
                int ench = Integer.parseInt(val);
                setTarget(activeChar, ench ,Inventory.PAPERDOLL_RFINGER);
            }
            catch (StringIndexOutOfBoundsException e)
            {
                if ( Config.DEVELOPER ) System.out.println("Set Right Ring enchant error: "+e);
                SystemMessage sm = new SystemMessage(614);
                sm.addString("SYS");
                sm.addString("請輸入強化數值");
                activeChar.sendPacket(sm);
            }
        }
        else if (command.startsWith("admin_seten"))
        {
            try
            {   
                String val = command.substring(12);
                int ench = Integer.parseInt(val);
                setTarget(activeChar, ench ,Inventory.PAPERDOLL_NECK);
            }
            catch (StringIndexOutOfBoundsException e)
            {
                if ( Config.DEVELOPER ) System.out.println("Set Necklace enchant error: "+e);
                SystemMessage sm = new SystemMessage(614);
                sm.addString("SYS");
                sm.addString("請輸入強化數值");
                activeChar.sendPacket(sm);
            }
        }
        else if (command.startsWith("admin_setun"))
        {
            try
            {   
                String val = command.substring(12);
                int ench = Integer.parseInt(val);
                setTarget(activeChar, ench ,Inventory.PAPERDOLL_UNDER);
            }
            catch (StringIndexOutOfBoundsException e)
            {
                if ( Config.DEVELOPER ) System.out.println("Set Underwear enchant error: "+e);
                SystemMessage sm = new SystemMessage(614);
                sm.addString("SYS");
                sm.addString("請輸入強化數值");
                activeChar.sendPacket(sm);
            }
        }
        else if (command.startsWith("admin_setba"))
        {
            try
            {   
                String val = command.substring(12);
                int ench = Integer.parseInt(val);
                setTarget(activeChar, ench ,Inventory.PAPERDOLL_BACK);
            }
            catch (StringIndexOutOfBoundsException e)
            {
                if ( Config.DEVELOPER ) System.out.println("Set Cloak enchant error: "+e);
                SystemMessage sm = new SystemMessage(614);
                sm.addString("SYS");
                sm.addString("請輸入強化數值");
                activeChar.sendPacket(sm);
            }
        }
        return true;
    }
    
    private void setTarget(L2PcInstance activeChar, int ench ,int armorType)
    {
        // 設置要精鍊的目標
        L2Object target = activeChar.getTarget();
        if (target == null)
            target = activeChar;
        L2PcInstance player = null;
        if (target instanceof L2PcInstance) {
            player = (L2PcInstance)target;
        } else {
            return;
        }
        
        // 判定輸入的精煉值範圍
        if ( ench >= 0 && ench <= 65535 ) {
            // now we need to find the equipped weapon of the targeted character...
            int curEnchant = 0; // display purposes only
            int dropSlot = armorType;
            boolean canEnchant = false;
            L2ItemInstance parmorInstance = null;
            L2ItemInstance armorToEnchant = null;
            
            // only attempt to enchant if there is a weapon equipped
            if ( canEnchant == false )
            {
                // 檢查裝備欄有無物品
                parmorInstance = player.getInventory().getPaperdollItem(dropSlot);
                if ( parmorInstance.getEquipSlot() == dropSlot ) { // 若是有裝備         
                    armorToEnchant = parmorInstance;
                    curEnchant = armorToEnchant.getEnchantLevel();//取得精練值
                    canEnchant = true;//設置可精煉
                }
            }
            
            if ( canEnchant == true && armorToEnchant != null ) {
                // 設置裝備的精煉值
                player.getInventory().unEquipItemInSlotAndRecord(dropSlot);
                parmorInstance.setEnchantLevel(ench);                               
                player.getInventory().equipItemAndRecord(armorToEnchant);
                
                // 整理物品欄
                InventoryUpdate iu = new InventoryUpdate(); 
                iu.addModifiedItem(armorToEnchant);
                player.sendPacket(iu);
                
                CharInfo info1 = new CharInfo(player);
                player.broadcastPacket(info1);
                UserInfo info2 = new UserInfo(player);
                player.sendPacket(info2);
                
                // 管理員訊息
                SystemMessage smA = new SystemMessage(614);
                smA.addString("改變裝備");
                smA.addString("("+armorToEnchant.getItem().getName()+") 從 +"+curEnchant+" 到 +"+ench+".");       
                activeChar.sendPacket(smA);
                
                // 玩家訊息
                SystemMessage sm = new SystemMessage(614);
                sm.addString("GM");
                sm.addString("強化你的裝備 ("+armorToEnchant.getItem().getName()+") 從 +"+curEnchant+" 到 +"+ench+".");
                player.sendPacket(sm);
            }
        }
        else {
            // inform our gm of their mistake
            SystemMessage smA = new SystemMessage(614);
            smA.addString("You must set the enchant level to be between 0-65535.");     
            activeChar.sendPacket(smA);         
        }
    }
    
    public void showMainPage(L2PcInstance activeChar)
    {
        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

        StringBuffer replyMSG = new StringBuffer("<html><body>");
        replyMSG.append("<center><table width=260><tr><td width=40>");
        replyMSG.append("<button value=\"主頁\" action=\"bypass -h admin_admin\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
        replyMSG.append("</td><td width=180>");
        replyMSG.append("<center>精練裝備</center>");
        replyMSG.append("</td><td width=40>");
        replyMSG.append("</td></tr></table></center><br>");
        replyMSG.append("<center><table width=270><tr><td>");
        replyMSG.append("<button value=\"內衣\" action=\"bypass -h admin_setun $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"頭具\" action=\"bypass -h admin_seteh $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");        
        replyMSG.append("<button value=\"披風\" action=\"bypass -h admin_setba $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"頭飾\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"項鍊\" action=\"bypass -h admin_seten $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
        replyMSG.append("</center><center><table width=270><tr><td>");
        replyMSG.append("<button value=\"武器\" action=\"bypass -h admin_setew $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"上衣\" action=\"bypass -h admin_setec $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");        
        replyMSG.append("<button value=\"盾類\" action=\"bypass -h admin_setes $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"耳環\" action=\"bypass -h admin_setre $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"耳環\" action=\"bypass -h admin_setle $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
        replyMSG.append("</center><center><table width=270><tr><td>");
        replyMSG.append("<button value=\"手套\" action=\"bypass -h admin_seteg $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"褲子\" action=\"bypass -h admin_setel $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");        
        replyMSG.append("<button value=\"鞋子\" action=\"bypass -h admin_seteb $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"戒指\" action=\"bypass -h admin_setrf $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"戒指\" action=\"bypass -h admin_setlf $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
        replyMSG.append("</center><br>");
        replyMSG.append("<center>[強化範圍 0-65535]</center>");
        replyMSG.append("<center><edit var=\"menu_command\" width=100 height=15></center><br>");
        replyMSG.append("</body></html>");

        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply); 
    }

    public String[] getAdminCommandList() {
        return _adminCommands;
    }

    private boolean checkLevel(int level) { 
        return (level >= REQUIRED_LEVEL);
    }
}
