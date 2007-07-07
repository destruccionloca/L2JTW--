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

import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.AdminForgePacket;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;

/**
 * This class handles commands for gm to forge packets
 * 
 * @author Maktakien
 *
 */
public class AdminPForge implements IAdminCommandHandler
{
    //private static Logger _log = Logger.getLogger(AdminKick.class.getName());
    private static final String[] ADMIN_COMMANDS = {"admin_forge","admin_forge2","admin_forge3" };
    private static final int REQUIRED_LEVEL = Config.GM_MIN;
	

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {

        if (!Config.ALT_PRIVILEGES_ADMIN)
        {
    		if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
            {
                //System.out.println("Not required level");
                return false;
            }
        }
        
        if (command.equals("admin_forge"))
        {
        	showMainPage(activeChar);
        }
        else if (command.startsWith("admin_forge2"))
        {
        	  try
              {
                  StringTokenizer st = new StringTokenizer(command);
                  st.nextToken();
                  String format = st.nextToken();
                  showPage2(activeChar,format);
              }
              catch(Exception ex)
              {                  
              }            
        }
        else if (command.startsWith("admin_forge3"))
        {
        	  try
              {
                  StringTokenizer st = new StringTokenizer(command);
                  st.nextToken();
                  String format = st.nextToken();
                  boolean broadcast = false;
                  if(format.toLowerCase().equals("broadcast"))
                  {
                	  format = st.nextToken();
                	  broadcast = true;
                  }
                  AdminForgePacket sp = new AdminForgePacket();
                  for(int i = 0; i < format.length();i++)
          		  {
                	  String val = st.nextToken();
                	  if(val.toLowerCase().equals("$objid"))
                	  {
                		 val = String.valueOf(activeChar.getObjectId());
                	  }
                	  else if(val.toLowerCase().equals("$tobjid"))
                	  {
                		  val = String.valueOf(activeChar.getTarget().getObjectId());
                	  }
                	  else if(val.toLowerCase().equals("$bobjid"))
                	  {
                		  if(activeChar.getBoat() != null)
                		  {
                			  val = String.valueOf(activeChar.getBoat().getObjectId());
                		  }                		  
                	  }
                	  else if(val.toLowerCase().equals("$clanid"))
                	  {
                		  val = String.valueOf(activeChar.getCharId());
                	  }
                	  else if(val.toLowerCase().equals("$allyid"))
                	  {
                		  val = String.valueOf(activeChar.getAllyId());
                	  }
                	  else if(val.toLowerCase().equals("$tclanid"))
                	  {
                		  val = String.valueOf(((L2PcInstance) activeChar.getTarget()).getCharId());
                	  }
                	  else if(val.toLowerCase().equals("$tallyid"))
                	  {
                		  val = String.valueOf(((L2PcInstance) activeChar.getTarget()).getAllyId());
                	  }
                	  else if(val.toLowerCase().equals("$x"))
                	  {
                 		 val = String.valueOf(activeChar.getX());
                 	  }
                	  else if(val.toLowerCase().equals("$y"))
                	  {
                 		 val = String.valueOf(activeChar.getY());
                 	  }
                	  else if(val.toLowerCase().equals("$z"))
                	  {
                 		 val = String.valueOf(activeChar.getZ());
                 	  } 
                	  else if(val.toLowerCase().equals("$heading"))
                	  {
                 		 val = String.valueOf(activeChar.getHeading());
                 	  } 
                	  else if(val.toLowerCase().equals("$tx"))
                	  {
                 		 val = String.valueOf(activeChar.getTarget().getX());
                 	  }
                	  else if(val.toLowerCase().equals("$ty"))
                	  {
                 		 val = String.valueOf(activeChar.getTarget().getY());
                 	  }
                	  else if(val.toLowerCase().equals("$tz"))
                	  {
                 		 val = String.valueOf(activeChar.getTarget().getZ());
                 	  }
                	  else if(val.toLowerCase().equals("$theading"))
                	  {
                 		 val = String.valueOf(((L2PcInstance) activeChar.getTarget()).getHeading());
                 	  } 
                	  
                	  sp.addPart(format.getBytes()[i],val);
          		  }
                  if(broadcast == true)
                  {
                	  activeChar.broadcastPacket(sp);
                  }
                  else
                  {
                	  activeChar.sendPacket(sp);
                  }
                  showPage3(activeChar,format,command);
              }
              catch(Exception ex)
              {
            	  ex.printStackTrace();
              }            
        }
        return true;
    }
	public void showMainPage(L2PcInstance activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		TextBuilder replyMSG = new TextBuilder("<html><body>");
		
		replyMSG.append("<center>L2JTW Forge 控制</center><br>");
		replyMSG.append("格式:<edit var=\"format\" width=100><br>");
		replyMSG.append("<button value=\"第二步驟\" action=\"bypass -h admin_forge2 $format\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
        replyMSG.append("單獨 c h d f s b or x 有作用<br>");
        replyMSG.append("</body></html>");

        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply); 
	}
	public void showPage3(L2PcInstance activeChar,String format,String command)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		TextBuilder replyMSG = new TextBuilder("<html><body>");
		
		replyMSG.append("<center>L2JTW Forge 控制</center><br>");
		replyMSG.append("<br>");
		replyMSG.append("封包 ("+format+") 送出<br><br>");
		replyMSG.append("<button value=\"重新嘗試\" action=\"bypass -h admin_forge\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br><br>Debug: CMD 字串 :"+command+"<br>");
        replyMSG.append("</body></html>");

        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply); 
	}
	public void showPage2(L2PcInstance activeChar,String format)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		TextBuilder replyMSG = new TextBuilder("<html><body>");		
		replyMSG.append("<center>L2JTW Forge 控制</center><br>Format:"+format);
		replyMSG.append("<br>請物有任何空格<br>小數為 c h d,  飄動數字為 (點跟者) f, 字串 s 與 x/b 為HEX");
		replyMSG.append("<br>Values<br>");
		for(int i = 0; i < format.length();i++)
		{
			replyMSG.append(format.charAt(i)+" : <edit var=\"v"+i+"\" width=100> <br>");
		}
		replyMSG.append("<br><button value=\"送出\" action=\"bypass -h admin_forge3 "+format);
		for(int i = 0; i < format.length();i++)
		{
			replyMSG.append(" $v"+i);
		}		
		replyMSG.append("\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		
		replyMSG.append("<br><button value=\"監控\" action=\"bypass -h admin_forge3 broadcast "+format);
		for(int i = 0; i < format.length();i++)
		{
			replyMSG.append(" $v"+i);
		}		
		replyMSG.append("\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
        replyMSG.append("</body></html>");

        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply); 
	}
    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
    
    private boolean checkLevel(int level) {
        return (level >= REQUIRED_LEVEL);
    }
}
