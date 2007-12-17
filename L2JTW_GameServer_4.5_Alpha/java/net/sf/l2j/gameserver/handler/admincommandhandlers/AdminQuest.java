/*
 * $Header: AdminTest.java, 25/07/2005 17:15:21 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 25/07/2005 17:15:21 $
 * $Revision: 1 $
 * $Log: AdminTest.java,v $
 * Revision 1  25/07/2005 17:15:21  luisantonioa
 * Added copyright notice
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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.QuestManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class AdminQuest implements IAdminCommandHandler
{
    private static final int REQUIRED_LEVEL = Config.GM_TEST;
    private static final String[] ADMIN_COMMANDS =
    {
        "admin_quest_reload"
    };

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IAdminCommandHandler#useAdminCommand(java.lang.String, net.sf.l2j.gameserver.model.L2PcInstance)
     */
    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (activeChar == null) return false;

        if (!Config.ALT_PRIVILEGES_ADMIN)
            if (activeChar.getAccessLevel() < REQUIRED_LEVEL) return false;

        // syntax will either be:
        //                           //quest_reload <id>
        //                           //quest_reload <questName>
        // The questName MUST start with a non-numeric character for this to work, 
        // regardless which of the two formats is used.
        // Example:  //quest_reload orc_occupation_change_1
        // Example:  //quest_reload chests
        // Example:  //quest_reload SagasSuperclass
        // Example:  //quest_reload 12
        if (command.startsWith("admin_quest_reload"))
        {
        	String[] parts = command.split(" ");
        	if (parts.length < 2)
        	{
        		activeChar.sendMessage("用法: //quest_reload <資料夾> 或者 //quest_reload <ID>");
        	}
        	else
        	{
        		// try the first param as id
        		try
        		{
        			int questId = Integer.parseInt(parts[1]);
        			if (QuestManager.getInstance().reload(questId))
            		{
            			activeChar.sendMessage("任務資料成功重新讀取.");
            		}
            		else
            		{
            			activeChar.sendMessage("任務資料讀取失敗.");
            		}
        		}
        		catch (NumberFormatException e)
        		{
        			if (QuestManager.getInstance().reload(parts[1]))
            		{
            			activeChar.sendMessage("任務資料除新讀取成功.");
            		}
            		else
            		{
            			activeChar.sendMessage("任務資料重新讀取失敗");
            		}
        		}
        	}
        }
        return true;
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IAdminCommandHandler#getAdminCommandList()
     */
    public String[] getAdminCommandList()
    {
        return ADMIN_COMMANDS;
    }

}
