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
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.CameraMode;
import net.sf.l2j.gameserver.serverpackets.ObservationMode;
import net.sf.l2j.gameserver.serverpackets.SpecialCamera;


/**
 * @version $Revision: $ $Date: $
 * @author  Made in Taiwan
 */
public class AdminCamera implements IAdminCommandHandler 
{


 private static final String[] ADMIN_COMMANDS = {
	 "admin_camera",
	 "admin_camera_menu",
	 "admin_camera_cord_menu",
	 "admin_camera_on",
	 "admin_camera_off",
	 "admin_camera_cord"
	 };


	public boolean useAdminCommand(String command, L2PcInstance activeChar) 
	{

        if (command.equals("admin_camera_menu"))
           AdminHelpPage.showHelpPage(activeChar, "camera_menu.htm");

        if (command.equals("admin_camera_cord_menu"))
            AdminHelpPage.showHelpPage(activeChar, "camera_cord.htm");

           if (command.equals("admin_camera_on"))
           {
        	   CameraMode(activeChar, 1);
           }

           else if (command.equals("admin_camera_off"))
           {
        	   CameraMode(activeChar, 0);
           }
           else if(command.startsWith("admin_camera"))
           {
               StringTokenizer st = new StringTokenizer(command);
               if(st.countTokens() == 5)
               {
                  try
                  {
                      int scdist = Integer.parseInt(st.nextToken());
                      int scyaw = Integer.parseInt(st.nextToken());
                      int scpitch = Integer.parseInt(st.nextToken());
                      int sctime = Integer.parseInt(st.nextToken());
                      int scduration = Integer.parseInt(st.nextToken());
                      specialCamera(activeChar, scdist, scyaw, scpitch, sctime, scduration);
                  }
                  catch(Exception e) 
                  { 
                      activeChar.sendMessage("使用方法: //admin_camera dist yaw pitch time duration");
                  }
               } 
               else
               {
                  activeChar.sendMessage("使用方法: //admin_camera dist yaw pitch time duration");
               }
           }
           else if(command.startsWith("admin_camera_cord"))
           {
               StringTokenizer st = new StringTokenizer(command);
               if(st.countTokens() == 3)
               {
                   try
                   {
                       int x = Integer.parseInt(st.nextToken());
                       int y = Integer.parseInt(st.nextToken());
                       int z = Integer.parseInt(st.nextToken());
                       CameraModeCord(activeChar, x, y, z);
                   }
                   catch(Exception e) 
                   {
                       activeChar.sendMessage("使用方法: //admin_camera_cord x y z");
                   }
               } 
               else
               {
                   activeChar.sendMessage("使用方法: //admin_camera_cord x y z");
               }
           }

          return true;
	}

	/**
	 * @param activeChar
	 * @param scdist
	 * @param scyaw
	 * @param scpitch
	 * @param sctime
	 * @param scduration
	 */
	private void specialCamera(L2PcInstance activeChar, int scdist, int scyaw, int scpitch, int sctime, int scduration)
	{
        int objid = activeChar.getTarget().getObjectId();
        int dist = scdist;
        int yaw = scyaw;
        int pitch = scpitch;
        int time = sctime;
        int duration = scduration;
        SpecialCamera sc = new SpecialCamera(objid, dist, yaw, pitch, time, duration);
        activeChar.broadcastPacket(sc);
	}

	private void CameraMode(L2PcInstance activeChar, int mode)
	{
        CameraMode cam = new CameraMode(mode);
        activeChar.sendPacket(cam);
	}

    private void CameraModeCord(L2PcInstance activeChar, int x, int y, int z)
    {
        ObservationMode om = new ObservationMode(x, y, z);
        activeChar.broadcastPacket(om);
    }
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
