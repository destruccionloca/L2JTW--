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
package net.sf.l2j.gameserver.handler.admincommandhandlers;


import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.CharInfo;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.UserInfo;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import javolution.text.TextBuilder;



/**
 * This class handles following admin commands:
 * - enchant_armor
 *
 * @version $Revision: 1.3.2.1.2.10 $ $Date: 2005/08/24 21:06:06 $
 */
public class AdminEnchant implements IAdminCommandHandler
{
    private static Logger _log = Logger.getLogger(AdminEnchant.class.getName());
    private static final String[] ADMIN_COMMANDS = {"admin_seteh",//6
                                              "admin_setec",//10
                                              "admin_seteg",//9
                                              "admin_setel",//11
                                              "admin_seteb",//12
                                              "admin_setew",//7
                                              "admin_setes",//8
                                              "admin_setle",//1
                                              "admin_setre",//2
                                              "admin_setlf",//4
                                              "admin_setrf",//5
                                              "admin_seten",//3
                                              "admin_setun",//0
                                              "admin_setba",//13
                                              "admin_enchant"};

    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (command.equals("admin_enchant"))
        {
            showMainPage(activeChar);
        } else
        {
            int armorType = -1;

            if (command.startsWith("admin_seteh"))
                armorType = Inventory.PAPERDOLL_HEAD;
            else if (command.startsWith("admin_setec"))
                armorType = Inventory.PAPERDOLL_CHEST;
            else if (command.startsWith("admin_seteg"))
                armorType = Inventory.PAPERDOLL_GLOVES;
            else if (command.startsWith("admin_seteb"))
                armorType = Inventory.PAPERDOLL_FEET;
            else if (command.startsWith("admin_setel"))
                armorType = Inventory.PAPERDOLL_LEGS;
            else if (command.startsWith("admin_setew"))
                armorType = Inventory.PAPERDOLL_RHAND;
            else if (command.startsWith("admin_setes"))
                armorType = Inventory.PAPERDOLL_LHAND;
            else if (command.startsWith("admin_setle"))
                armorType = Inventory.PAPERDOLL_LEAR;
            else if (command.startsWith("admin_setre"))
                armorType = Inventory.PAPERDOLL_REAR;
            else if (command.startsWith("admin_setlf"))
                armorType = Inventory.PAPERDOLL_LFINGER;
            else if (command.startsWith("admin_setrf"))
                armorType = Inventory.PAPERDOLL_RFINGER;
            else if (command.startsWith("admin_seten"))
                armorType = Inventory.PAPERDOLL_NECK;
            else if (command.startsWith("admin_setun"))
                armorType = Inventory.PAPERDOLL_UNDER;
            else if (command.startsWith("admin_setba"))
                armorType = Inventory.PAPERDOLL_BACK;

            if (armorType != -1)
            {
                try
                {
                    int ench = Integer.parseInt(command.substring(12));

                    // check value
                    if (ench < 0 || ench > 65535)
                        activeChar.sendMessage("�j�Ƶ��Žd��u���� 0-65535 �����C");
                    else
                        setEnchant(activeChar, ench, armorType);
                }
                catch (StringIndexOutOfBoundsException e)
                {
                    if (Config.DEVELOPER) 
                        _log.warning("Set enchant error: " + e);
                        activeChar.sendMessage("�Э��s���w�j�ƼƭȡC");
                }
                catch (NumberFormatException e)
                {
                    if (Config.DEVELOPER) 
                        _log.warning("Set enchant error: " + e);
                        activeChar.sendMessage("�Э��s���w�j�ƼƭȡC");
                }
            }

            // show the enchant menu after an action
            showMainPage(activeChar);
        }

        return true;
    }

    private void setEnchant(L2PcInstance activeChar, int ench, int armorType)
    {
        // get the target
        L2Object target = activeChar.getTarget();
        if (target == null) target = activeChar;
        L2PcInstance player = null;
        if (target instanceof L2PcInstance)
        {
            player = (L2PcInstance) target;
        }
        else
        {
            activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
            return;
        }

        // now we need to find the equipped weapon of the targeted character...
        int curEnchant = 0; // display purposes only
        L2ItemInstance itemInstance = null;

        // only attempt to enchant if there is a weapon equipped
        L2ItemInstance parmorInstance = player.getInventory().getPaperdollItem(armorType);
        if (parmorInstance != null && parmorInstance.getLocationSlot() == armorType)
        {
            itemInstance = parmorInstance;
        } else
        {
            // for bows and double handed weapons
            parmorInstance = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
            if (parmorInstance != null && parmorInstance.getLocationSlot() == Inventory.PAPERDOLL_LRHAND)
                itemInstance = parmorInstance;
        }

        if (itemInstance != null)
        {
            curEnchant = itemInstance.getEnchantLevel();

            // set enchant value
            player.getInventory().unEquipItemInSlotAndRecord(armorType);
            itemInstance.setEnchantLevel(ench);
            player.getInventory().equipItemAndRecord(itemInstance);

            // send packets
            InventoryUpdate iu = new InventoryUpdate();
            iu.addModifiedItem(itemInstance);
            player.sendPacket(iu);
            player.broadcastPacket(new CharInfo(player));
            player.sendPacket(new UserInfo(player));

            activeChar.sendMessage("�j�Ƹ˳�"+itemInstance.getItem().getName()+"�q +"+curEnchant+"�� +"+ench+"�C");
            // log
            GMAudit.auditGMAction(activeChar.getName(), "enchant", player.getName(), itemInstance.getItem().getName() + "(" + itemInstance.getObjectId() + ")" + " from " + curEnchant + " to " + ench);
        }
    }

    private void showMainPage(L2PcInstance activeChar)
    {
/*
        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

        TextBuilder replyMSG = new TextBuilder("<html><body>");
        replyMSG.append("<center><table width=260><tr><td width=40>");
        replyMSG.append("<button value=\"�D��\" action=\"bypass -h admin_admin\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
        replyMSG.append("</td><td width=180>");
        replyMSG.append("<center>�j�ƪ��~����</center>");
        replyMSG.append("</td><td width=40>");
        replyMSG.append("</td></tr></table></center><br>");
        replyMSG.append("<center><table width=270><tr><td>");
        replyMSG.append("<button value=\"����\" action=\"bypass -h admin_setun $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"�Y��\" action=\"bypass -h admin_seteh $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");        
        replyMSG.append("<button value=\"�ܭ�\" action=\"bypass -h admin_setba $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"�Y��\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"����\" action=\"bypass -h admin_seten $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
        replyMSG.append("</center><center><table width=270><tr><td>");
        replyMSG.append("<button value=\"�Z��\" action=\"bypass -h admin_setew $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"�W��\" action=\"bypass -h admin_setec $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");        
        replyMSG.append("<button value=\"����\" action=\"bypass -h admin_setes $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"����\" action=\"bypass -h admin_setre $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"����\" action=\"bypass -h admin_setle $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
        replyMSG.append("</center><center><table width=270><tr><td>");
        replyMSG.append("<button value=\"��M\" action=\"bypass -h admin_seteg $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"�Ǥl\" action=\"bypass -h admin_setel $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");        
        replyMSG.append("<button value=\"�c�l\" action=\"bypass -h admin_seteb $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"�٫�\" action=\"bypass -h admin_setrf $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"�٫�\" action=\"bypass -h admin_setlf $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
        replyMSG.append("</center><br>");
        replyMSG.append("<center>[�j�ƽd�� 0-65535]</center>");
        replyMSG.append("<center><edit var=\"menu_command\" width=100 height=15></center><br>");
        replyMSG.append("</body></html>");

        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply);
*/
    	AdminHelpPage.showHelpPage(activeChar, "enchant.htm");
    }

    public String[] getAdminCommandList()
    {
        return ADMIN_COMMANDS;
    }
}