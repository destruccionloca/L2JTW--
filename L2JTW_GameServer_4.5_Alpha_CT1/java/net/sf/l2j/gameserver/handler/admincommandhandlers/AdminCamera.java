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
 * [URL]http://www.gnu.org/copyleft/gpl.html[/URL]
 */
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;


/**
 * @version $Revision: $ $Date: $
 * @author  Made in Taiwan
 */
public class AdminCamera implements IAdminCommandHandler 
{


	private static final String[] ADMIN_COMMANDS = {
	"admin_camera",
	"admin_camset",
	};

	public boolean useAdminCommand(String command, L2PcInstance activeChar) 
	{

	if (command.equals("admin_camera"))
		AdminHelpPage.showHelpPage(activeChar, "camera_menu.htm");

	// 測試 Camera Debug 使用
	else if(command.startsWith("admin_camset"))
    {
        if(activeChar.getTarget() == null)
        {
            activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
        }
        else
        {
            StringTokenizer st = new StringTokenizer(command);
            st.nextToken();

            try
            {
            	L2Object target = activeChar.getTarget();
            	int scDist = Integer.parseInt(st.nextToken());
            	int scYaw = Integer.parseInt(st.nextToken());
            	int scPitch = Integer.parseInt(st.nextToken());
            	int scTime = Integer.parseInt(st.nextToken());
            	int scDuration = Integer.parseInt(st.nextToken());
                activeChar.sendMessage("camera " + scDist + "," + scYaw + "," + scPitch + "," + scTime + "," + scDuration);
            	activeChar.enterMovieMode();
            	activeChar.specialCamera(target, scDist, scYaw, scPitch, scTime, scDuration);
            }
            catch(Exception e)
            {
                activeChar.sendMessage("使用方法: //camera dist yaw pitch time duration");
            }
            finally
            {
            	activeChar.leaveMovieMode();
            }
            
        }
	}
	return true;
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
