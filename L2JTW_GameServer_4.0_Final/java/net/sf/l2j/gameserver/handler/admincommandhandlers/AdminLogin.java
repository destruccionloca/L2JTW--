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

import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.gameserverpackets.ServerStatus;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;

/**
 * This class handles the admin commands that acts on the login
 * 
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/04/11 10:05:56 $
 */
public class AdminLogin implements IAdminCommandHandler
{
	//private static Logger _log = Logger.getLogger(AdminDelete.class.getName());

    private static String[] _adminCommands = { "admin_server_gm_only",
                                               "admin_server_all",
                                               "admin_server_max_player",
                                               "admin_server_list_clock",
                                               "admin_server_login"};

    private static final int REQUIRED_LEVEL = Config.GM_ACCESSLEVEL;
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IAdminCommandHandler#useAdminCommand(java.lang.String, net.sf.l2j.gameserver.model.L2PcInstance)
	 */
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
        if (!Config.ALT_PRIVILEGES_ADMIN)
        {
    		if(activeChar.getAccessLevel() < REQUIRED_LEVEL)
    			return false;
        }
        
		if(command.equals("admin_server_gm_only"))
		{
			gmOnly();
			activeChar.sendMessage("���A���ثe��GM�M��");
			showWindow(activeChar);
		}
		else if(command.equals("admin_server_all"))
		{
			AllowToAll();
			activeChar.sendMessage("���A���ثe���}��");
			showWindow(activeChar);
		}
		else if(command.startsWith("admin_server_max_player"))
		{
			StringTokenizer st = new StringTokenizer(command);
			if (st.countTokens() > 1)
            {
                st.nextToken();
                String number = st.nextToken();
                try
                {
                	LoginServerThread.getInstance().setMaxPlayer(new Integer(number).intValue());
                	activeChar.sendMessage("�̤j���a�ƶq�]�m�� "+new Integer(number).intValue());
                	showWindow(activeChar);
                }
                catch(NumberFormatException e)
                {
                	activeChar.sendMessage("�榡���~");
                }
            }
			else
			{
				activeChar.sendMessage("�榡�� //server_max_player <max>");
			}
		}
		else if(command.startsWith("admin_server_list_clock"))
		{
			StringTokenizer st = new StringTokenizer(command);
			if (st.countTokens() > 1)
            {
                st.nextToken();
                String mode = st.nextToken();
                if(mode.equals("on"))
                {
                	LoginServerThread.getInstance().sendServerStatus(ServerStatus.SERVER_LIST_CLOCK,ServerStatus.ON);
                	activeChar.sendMessage("�b�n�J���A���ɱN�|�ݨ����");
                	Config.SERVER_LIST_CLOCK = true;
                	showWindow(activeChar);
                }
                else if(mode.equals("off"))
                {
                	LoginServerThread.getInstance().sendServerStatus(ServerStatus.SERVER_LIST_CLOCK,ServerStatus.OFF);
                	Config.SERVER_LIST_CLOCK = false;
                	activeChar.sendMessage("�����������");
                	showWindow(activeChar);
                }
                else
                {
                	activeChar.sendMessage("�榡�� //server_list_clock <on/off>");
                }
            }
			else
			{
				activeChar.sendMessage("�榡�� //server_list_clock <on/off>");
			}
		}
		else if(command.equals("admin_server_login"))
		{
			showWindow(activeChar);
		}
		return true;
	}

	/**
	 * 
	 */
	private void showWindow(L2PcInstance activeChar)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile("data/html/admin/login.html");
		html.replace("%server_name%",LoginServerThread.getInstance().getServerName());
		html.replace("%status%",LoginServerThread.getInstance().getStatusString());
		html.replace("%clock%",String.valueOf(Config.SERVER_LIST_CLOCK));
		html.replace("%brackets%",String.valueOf(Config.SERVER_LIST_BRACKET));
		html.replace("%max_players%",String.valueOf(LoginServerThread.getInstance().getMaxPlayer()));
		activeChar.sendPacket(html);
		
	}

	/**
	 * 
	 */
	private void AllowToAll()
	{
		LoginServerThread.getInstance().setServerStatus(ServerStatus.STATUS_AUTO);
		Config.SERVER_GMONLY = false;
	}

	/**
	 * 
	 */
	private void gmOnly()
	{
		LoginServerThread.getInstance().setServerStatus(ServerStatus.STATUS_GM_ONLY);
		Config.SERVER_GMONLY = true;
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IAdminCommandHandler#getAdminCommandList()
	 */
	public String[] getAdminCommandList()
	{
		return _adminCommands;
	}
	
}