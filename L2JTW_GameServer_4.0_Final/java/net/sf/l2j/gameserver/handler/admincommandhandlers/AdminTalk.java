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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class handles following admin commands:
 * - kill = kills target L2Character
 * 
 * @version $Revision: 1.1.6.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminTalk implements IAdminCommandHandler {
    //private static Logger _log = Logger.getLogger(AdminBan.class.getName());
    private static String[] _adminCommands = {"admin_talk"};
    private static final int REQUIRED_LEVEL = Config.GM_TALK_BLOCK;
    private static boolean _canTalk = true;

    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (!(checkLevel(activeChar.getAccessLevel())))
        {
            System.out.println("無需權限");
            return false;
        }
        if (command.startsWith("admin_talk on"))
	{
	    _canTalk = true;
	    activeChar.sendMessage("GM交談起動.");
            //System.out.println("ADMIN TALKON");
        } else if (command.startsWith("admin_talk off"))
	{
	    _canTalk = false;
	    activeChar.sendMessage("GM交談關閉.");
                //System.out.println("ADMIN TALKOFF");
        }
	return true;
    }
    
    public String[] getAdminCommandList() {
        return _adminCommands;
    }
    public static boolean canTalk() {
        return _canTalk;
    }
    
    private boolean checkLevel(int level) {
        return (level >= REQUIRED_LEVEL);
    }
}
