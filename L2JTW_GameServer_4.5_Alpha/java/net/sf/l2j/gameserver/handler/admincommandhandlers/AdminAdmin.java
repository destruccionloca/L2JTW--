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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.Olympiad;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.TeleportLocationTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.Manager;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import javolution.text.TextBuilder;


/**
 * This class handles following admin commands:
 * - admin|admin1/admin2/admin3/admin4/admin5 = slots for the 5 starting admin menus
 * - gmliston/gmlistoff = includes/excludes active character from /gmlist results
 * - silence = toggles private messages acceptance mode
 * - diet = toggles weight penalty mode
 * - tradeoff = toggles trade acceptance mode
 * - reload = reloads specified component from multisell|skill|npc|htm|item|instancemanager
 * - set/set_menu/set_mod = alters specified server setting
 * - saveolymp = saves olympiad state manually
 * - manualhero = cycles olympiad and calculate new heroes.
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2007/07/28 10:06:06 $
 */
public class AdminAdmin implements IAdminCommandHandler {


 private static final String[] ADMIN_COMMANDS = {"admin_admin","admin_play_sounds","admin_play_sound",
                                           "admin_gmliston","admin_gmlistoff","admin_silence",
                                           "admin_atmosphere","admin_diet","admin_tradeoff",

                                           "admin_config_option", "admin_config_altsetting", "admin_config_other", "admin_config_rate",
                                           "admin_reload", "admin_set", "admin_admin2","admin_admin3", "admin_admin4", "admin_admin5", "admin_cache", "admin_saveolymp", "admin_manualhero","admin_eventmenu"
                                           ,"admin_manualhero","admin_set_mod", "admin_set", "admin_set_menu"};



	private static final int REQUIRED_LEVEL = Config.GM_MENU;

	public boolean useAdminCommand(String command, L2PcInstance activeChar) 
	{

        if (!Config.ALT_PRIVILEGES_ADMIN)
            if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
        
        //if (command.equals("admin_admin")) showMainPage(activeChar);
        //if (command.equals("admin_admin2")) showMainPage2(activeChar);
        if (command.equals("admin_cache")) showMainPageCache(activeChar);
        if (command.equals("admin_eventmenu")) showMainPageEventMenu(activeChar);
        
        if (command.equals("admin_config_option"))      showOptionConfigPage(activeChar);
        if (command.equals("admin_config_altsetting"))  showAltsetConfigPage(activeChar);
        if (command.equals("admin_config_other"))       showOtherConfigPage(activeChar);
        if (command.equals("admin_config_rate"))        showRateConfigPage(activeChar);
        
        

		if (!Config.ALT_PRIVILEGES_ADMIN)
			if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
				return false;
		
		GMAudit.auditGMAction(activeChar.getName(), command, (activeChar.getTarget() != null?activeChar.getTarget().getName():"no-target"), "");
		
		if (command.startsWith("admin_admin"))
		{
			showPage(activeChar,command);
		}
		else if(command.startsWith("admin_gmliston"))
		{
			GmListTable.getInstance().addGm(activeChar);
            activeChar.sendMessage("���UGM�C��");
		}
		else if(command.startsWith("admin_gmlistoff"))
		{
		    GmListTable.getInstance().deleteGm(activeChar);
            activeChar.sendMessage("����GM�C��");
		}
		else if(command.startsWith("admin_silence"))
		{     	
			if (activeChar.getMessageRefusal()) // already in message refusal mode
			{
				activeChar.setMessageRefusal(false);
				activeChar.sendPacket(new SystemMessage(SystemMessageId.MESSAGE_ACCEPTANCE_MODE));
			}
			else
			{
				activeChar.setMessageRefusal(true);
				activeChar.sendPacket(new SystemMessage(SystemMessageId.MESSAGE_REFUSAL_MODE));
			}	    
		}

        
        else if(command.startsWith("admin_saveolymp"))
        {
            try 
            {
                Olympiad.getInstance().save();
            }
            catch(Exception e){e.printStackTrace();}
            
            activeChar.sendMessage("���L�Ǩȸ���x�s");
            
        }
        
        else if(command.startsWith("admin_manualhero"))
        {
            try 
            {
                Olympiad.getInstance().manualSelectHeroes();
            }
            catch(Exception e){e.printStackTrace();}
            
            activeChar.sendMessage("�]�m�^��");
            
        }
        else if(command.startsWith("admin_diet"))
        {
            try
            {
                StringTokenizer st = new StringTokenizer(command);
                st.nextToken();
                if(st.nextToken().equalsIgnoreCase("on"))
                {
                    activeChar.setDietMode(true);
                    activeChar.refreshOverloaded();
                    activeChar.sendMessage("�L���q�}�l");
                }
                else if(st.nextToken().equalsIgnoreCase("off"))
                {
                    activeChar.setDietMode(false);
                    activeChar.sendMessage("�L���q����");
                }
            }
            catch(Exception ex)
            {
                if(activeChar.getDietMode())
                    activeChar.sendMessage("�L���q�ثe�Ұ�");
                else
                    activeChar.sendMessage("�L���q�ثe����");
            }            
        }
        else if(command.startsWith("admin_tradeoff"))
        {
            try
            {
                String mode = command.substring(15);
                if (mode.equalsIgnoreCase("on"))
                {
                    activeChar.setTradeRefusal(true);
                    activeChar.sendMessage("��������Ұ�");
                }
                else if (mode.equalsIgnoreCase("off"))
                {
                    activeChar.setTradeRefusal(false);
                    activeChar.sendMessage("�����������");
                }
            }
            catch(Exception ex)
            {
                if(activeChar.getTradeRefusal())
                    activeChar.sendMessage("��������ثe���Ұ�");
                else
                    activeChar.sendMessage("��������ثe������");
            }            
        }

		else if(command.startsWith("admin_reload"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			try
			{
				String type = st.nextToken();
				if(type.equals("multisell"))
				{
					L2Multisell.getInstance().reload();
					activeChar.sendMessage("multisell ��ƭ��sŪ��");
				}
				else if(type.startsWith("teleport"))
				{
					TeleportLocationTable.getInstance().reloadAll();
					activeChar.sendMessage("teleport ��ƭ��sŪ��");
				}
				else if(type.startsWith("skill"))
				{
					SkillTable.getInstance().reload();
					activeChar.sendMessage("skills ��ƭ��sŪ��");
				}
				else if(type.equals("npc"))
				{
					NpcTable.getInstance().reloadAllNpc();
					activeChar.sendMessage("npcs ��ƭ��sŪ��");
				}
				else if(type.startsWith("htm"))
				{
					HtmCache.getInstance().reload();
					activeChar.sendMessage("Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage()  + " megabytes �b " + HtmCache.getInstance().getLoadedFiles() + " ���ɮ�Ū��");
				}
				else if(type.startsWith("item"))
				{
					ItemTable.getInstance().reload();
					activeChar.sendMessage("Item ��ƭ��sŪ��");
				}
				else if(type.startsWith("instancemanager"))
				{
					Manager.reloadAll();
					activeChar.sendMessage("�Ҧ�Instance���sŪ��");
				}
			}
			catch(Exception e)
			{
				activeChar.sendMessage("�ϥΤ�k:  //reload <multisell|skill|npc|htm|item|instancemanager>");
			}
		}


		else if(command.startsWith("admin_set"))
		{
			StringTokenizer st = new StringTokenizer(command);
			String[] cmd=st.nextToken().split("_");
			try
			{
				String[] parameter = st.nextToken().split("=");
				String pName = parameter[0].trim();
				String pValue = parameter[1].trim();
				if (Config.setParameterValue(pName, pValue))
					activeChar.sendMessage("�ƭ� "+pName+" ���\�]�w�� "+pValue);
				else 
					activeChar.sendMessage("���~�]�w��");
			}
			catch(Exception e)
			{

				if (cmd.length==2)
					activeChar.sendMessage("�ϥΤ�k:  //set parameter=�ƭ�");
			}
			finally
			{
				if (cmd.length==3)
				{
					if (cmd[2].equalsIgnoreCase("menu"))
						AdminHelpPage.showHelpPage(activeChar, "settings.htm");
					else if (cmd[2].equalsIgnoreCase("mod"))
						AdminHelpPage.showHelpPage(activeChar, "mods_menu.htm");
				}
			}
		}
				return true;

		}
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private boolean checkLevel(int level) 
	{
		return (level >= REQUIRED_LEVEL);
	}


	private void showPage(L2PcInstance activeChar, String command)
	{

		

		int mode = 0;
		String filename=null;
		try
		{
			mode = Integer.parseInt(command.substring(11));
		}
		catch (Exception e) {}
		switch (mode)
		{
		case 1:
			{
			showMainPage(activeChar);
			break;
			}
		case 2:
			{
			filename="game";
			break;
			}
		case 3:
			{
			filename="effects";
			break;
			}
		case 4:
			{
			filename="server";
			break;
			}
		case 5:
			{
			filename="mods";
			break;
			}
		default:
			showMainPage(activeChar);
			
/*
			if (Config.GM_ADMIN_MENU_STYLE.equals("modern"))
				filename="main";
			else
				filename="classic";
*/
		break;
		}
		if (filename!=null)
		AdminHelpPage.showHelpPage(activeChar, filename+"_menu.htm");
		else
		showMainPage(activeChar);

	}


	 public void showMainPage(L2PcInstance activeChar)
	    {
	        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);




	        StringBuffer replyMSG = new StringBuffer("<html><title>L2JTW ���A�������</title><body>");
	        replyMSG.append("<br><center><table width=260>");
	       	replyMSG.append("<tr>");
	        replyMSG.append("<td><button value=\"�]�w\" action=\"bypass -h admin_config_option\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
	        replyMSG.append("<td><button value=\"�C��\" action=\"bypass -h admin_admin2\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
	        replyMSG.append("<td><button value=\"�ĪG\" action=\"bypass -h admin_admin3\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
	        replyMSG.append("<td><button value=\"���A��\" action=\"bypass -h admin_admin4\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
	        replyMSG.append("<td><button value=\"�~��\" action=\"bypass -h admin_admin5\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
	        replyMSG.append("</tr>");
	        replyMSG.append("</table></center>");
            //replyMSG.append("<center><table width=270><tr><td width=60><button value=\"�ާ@\" action=\"bypass -h admin_admin2\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td width=150><center><font color=\"LEVEL\">���A���޲z�x</font></center></td><td width=60><button value=\"�]�m\" action=\"bypass -h admin_config_option\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></center><br>");
            replyMSG.append("<center><table width=200><tr><td>");
	        replyMSG.append("<button value=\"�u�W���a\" action=\"bypass -h admin_show_characters 0\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"���A���޲z\" action=\"bypass -h admin_server_shutdown\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">"); 
	        replyMSG.append("<button value=\"���i�޲z\" action=\"bypass -h admin_list_announcements\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"�ھڦa�޲z\" action=\"bypass -h admin_siege\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<br>");
	        replyMSG.append("<button value=\"�ϥέ��s\" action=\"bypass -h admin_ride_wyvern\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"�Ѱ����s\" action=\"bypass -h admin_unride_wyvern\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"���Ϊ��A\" action=\"bypass -h admin_invisible\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"�Ѱ�����\" action=\"bypass -h admin_visible\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</td><td>");
	        replyMSG.append("<button value=\"GM �ө�\" action=\"bypass -h admin_gmshop\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"�гy���~\" action=\"bypass -h admin_itemcreate\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"���a�޲z\" action=\"bypass -h admin_char_manage\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"�ǰe���\" action=\"bypass -h admin_show_moves\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<br>");
	        replyMSG.append("<button value=\"���~�j�ƺ޲z\" action=\"bypass -h admin_enchant\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"Cache�����\" action=\"bypass -h admin_cache\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");    
	        replyMSG.append("<button value=\"�D�D�޲z����\" action=\"bypass -h admin_view_petitions\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"NPC�޲z����\" action=\"bypass -h admin_show_spawns\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<br>");
	        replyMSG.append("</td></tr></table></center>");
	        replyMSG.append("<center><table><tr><td>");
	        //replyMSG.append("<button value=\"���A�����A\" action=\"bypass -h admin_server_login\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        //replyMSG.append("<button value=\"�h�\�౱��\" action=\"bypass -h admin_eventmenu\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        //replyMSG.append("<button value=\"Cache����\" action=\"bypass -h admin_cache\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        //replyMSG.append("<button value=\"�˵��D�D\" action=\"bypass -h admin_view_petitions\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        //replyMSG.append("<button value=\"�ĤG��\" action=\"bypass -h admin_admin2\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</td></tr></table></center><br>");
	        replyMSG.append("<center>���a:</center>");
	        replyMSG.append("<center><edit var=\"menu_command\" width=100 height=15></center><br>");
	        replyMSG.append("<center><table><tr><td>");
	        replyMSG.append("<button value=\"���`\" action=\"bypass -h admin_kill $menu_command\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
	        replyMSG.append("<button value=\"��\" action=\"bypass -h admin_kick $menu_command\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
	        replyMSG.append("<button value=\"����\" action=\"bypass -h admin_ban $menu_command\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
	        replyMSG.append("</table><br><table width=200><tr><td>");
	        replyMSG.append("<button value=\"��ѫ���\" action=\"bypass -h admin_banchat $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
	        replyMSG.append("<button value=\"�Ѱ���ѫ���\" action=\"bypass -h admin_unbanchat $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</td></tr></table></center><br>");
	        replyMSG.append("<font color=\"LEVEL\"><tr><td>���A������: L2JTW Server 4.5</td></tr></font>");
	        replyMSG.append("</body></html>");


	        adminReply.setHtml(replyMSG.toString());
	        activeChar.sendPacket(adminReply); 
	    }
	    
	    public void showMainPage2(L2PcInstance activeChar)
	    {
	        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

	        StringBuffer replyMSG = new StringBuffer("<html><body>");
	        replyMSG.append("<center><table width=260><tr><td width=40>");
	        replyMSG.append("<button value=\"����\" action=\"bypass -h admin_admin\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</td><td width=180>");
	        replyMSG.append("<center>���A���޲z�x</center>");
	        replyMSG.append("</td><td width=40><button value=\"��^\" action=\"bypass -h admin_admin\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</td></tr></table></center>");
	        replyMSG.append("<center><table width=200><tr><td>");
	        replyMSG.append("<button value=\"�l���_�c\" action=\"bypass -h admin_spawn_monster 1042\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"�_�c�v��\" action=\"bypass -h admin_box_access $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">"); 
	        replyMSG.append("<button value=\"�}��\" action=\"bypass -h admin_open $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"����\" action=\"bypass -h admin_close $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<br>");
	        replyMSG.append("<button value=\"�·��Ҧ����a\" action=\"bypass -h admin_para_all\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"�Ѱ��Ҧ��·�\" action=\"bypass -h admin_unpara_all\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"���Y�ĪG\" action=\"bypass -h admin_bighead\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<br>");
	        replyMSG.append("<button value=\"GM�߰ݳB\" action=\"bypass -h admin_teleport_character_to_menu $menu_command -114560 -249360 -2980\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"�ഫ�ۤv\" action=\"bypass -h admin_polyself $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"�Ѱ��ഫ�ۤv\" action=\"bypass -h admin_unpolyself\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</td><td>");       
	        replyMSG.append("<button value=\"�]�m�ʦV\" action=\"bypass -h admin_setkarma $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"�Ѱ��ʦV\" action=\"bypass -h admin_nokarma\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"�Ǫ��v��\" action=\"bypass -h admin_mons\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"���񭵼�\" action=\"bypass -h admin_play_sounds\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<br>");
	        replyMSG.append("<button value=\"�·��ؼ�\" action=\"bypass -h admin_para\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"�Ѱ��ؼг·�\" action=\"bypass -h admin_unpara\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"�Ѱ����Y�ĪG\" action=\"bypass -h admin_shrinkhead\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<br>");
	        replyMSG.append("<button value=\"���W�l\" action=\"bypass -h admin_changename $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"�гy���~\" action=\"bypass -h admin_itemcreate\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"�j�ƪZ��\" action=\"bypass -h admin_setew $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</td></tr></table></center><br>");
	        replyMSG.append("<center><table><tr><td>");
	        replyMSG.append("<button value=\"�a�_�ĪG\" action=\"bypass -h admin_earthquake $menu_command\" width=95 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"�_���ĪG\" action=\"bypass -h admin_res $menu_command\" width=95 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"�^�ЮĪG\" action=\"bypass -h admin_heal $menu_command\" width=95 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"�M��NPC��m\" action=\"bypass -h admin_list_spawns $menu_command\" width=95 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"NPC�����p��\" action=\"bypass -h admin_fight_calculator\" width=95 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"�ؼФ����p��\" action=\"bypass -h admin_fcs\" width=95 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"��Ū�t�θ��\" action=\"bypass -h admin_reload $menu_command\" width=95 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</td></tr></table></center><br>");
	        replyMSG.append("<center>���O�ƭ�</center>");
	        replyMSG.append("<center><edit var=\"menu_command\" width=100 height=15></center><br>");
	        replyMSG.append("<center><table><tr><td>");
	        replyMSG.append("<button value=\"���`\" action=\"bypass -h admin_kill $menu_command\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
	        replyMSG.append("<button value=\"��\" action=\"bypass -h admin_kick $menu_command\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
	        replyMSG.append("<button value=\"����\" action=\"bypass -h admin_ban $menu_command\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
	        replyMSG.append("</table><br><table width=200><tr><td>");
	        replyMSG.append("</td><td>");
	        replyMSG.append("");
	        replyMSG.append("</td></tr></table></center>");
	        replyMSG.append("<center><table><br>");
	        replyMSG.append("<tr><td><button value=\"�դ�\" action=\"bypass -h admin_atmosphere sky day\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
	        replyMSG.append("<td><button value=\"�©]\" action=\"bypass -h admin_atmosphere sky night\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
	        replyMSG.append("<tr><td><button value=\"�����Ѫ�\" action=\"bypass -h admin_atmosphere signsky dawn\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
	        replyMSG.append("<td><button value=\"�����Ѫ�\" action=\"bypass -h admin_atmosphere signsky dusk\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
	        replyMSG.append("</table></center><br><br><br>");

	        replyMSG.append("<tr><td>���A������: L2JTW Server 4.5</td></tr>");
	        replyMSG.append("</body></html>");

	        adminReply.setHtml(replyMSG.toString());
	        activeChar.sendPacket(adminReply); 
	    }
	    public void showMainPageCache(L2PcInstance activeChar)
	    {
	        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

	        StringBuffer replyMSG = new StringBuffer("<html><body>");
	        replyMSG.append("<center><table width=260><tr><td width=40>");
	        replyMSG.append("<button value=\"����\" action=\"bypass -h admin_admin\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</td><td width=180>");
	        replyMSG.append("<center>Cache����</center>");
	        replyMSG.append("</td><td width=40><button value=\"��^\" action=\"bypass -h admin_admin\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</td></tr></table></center>");
	        replyMSG.append("<center><table width=200><tr><td>");
	        replyMSG.append("<br><center>");
	        replyMSG.append("<button value=\"����HTML\" action=\"bypass -h admin_cache_htm_rebuild\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"Ū��HTML\" action=\"bypass -h admin_cache_htm_reload\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"�������\" action=\"bypass -h admin_cache_crest_rebuild\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"�ץ�����\" action=\"bypass -h admin_cache_crest_fix\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"Ū��Skill\" action=\"bypass -h admin_reload skill\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"Ū��Items\" action=\"bypass -h admin_reload item\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</center></td><td>");
	        replyMSG.append("</td></tr></table></center><br>");
	        replyMSG.append("<br>");
	        replyMSG.append("<br>");
	        replyMSG.append("<br>");
	        replyMSG.append("<br>");
	        replyMSG.append("<br>");
	        replyMSG.append("<br>");
	        replyMSG.append("<br>");
	        replyMSG.append("<br>");
	        replyMSG.append("<tr><td>���A������: L2JTW Server 4.5</td></tr>");
	        replyMSG.append("</body></html>");
	        
	        adminReply.setHtml(replyMSG.toString());
	        activeChar.sendPacket(adminReply); 
	    }
	    
	    public void showMainPageEventMenu(L2PcInstance activeChar)
	    {
	        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

	        StringBuffer replyMSG = new StringBuffer("<html><body>");
	        replyMSG.append("<center><table width=260><tr><td width=40>");
	        replyMSG.append("<button value=\"����\" action=\"bypass -h admin_admin\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</td><td width=180>");
	        replyMSG.append("<center>���ʾާ@</center>");
	        replyMSG.append("</td><td width=40><button value=\"��^\" action=\"bypass -h admin_admin\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</td></tr></table></center>");
	        replyMSG.append("<center><table width=200><tr><td>");
	        replyMSG.append("<br><center>");
	        replyMSG.append("<button value=\"CTF���ʤ���\" action=\"bypass -h admin_ctf\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</center></td><td>");
	        replyMSG.append("</td></tr></table></center><br>");
	        replyMSG.append("<br>");
	        replyMSG.append("<br>");
	        replyMSG.append("<br>");
	        replyMSG.append("<br>");
	        replyMSG.append("<br>");
	        replyMSG.append("<br>");
	        replyMSG.append("<br>");
	        replyMSG.append("<br>");
	        replyMSG.append("<tr><td>���A������: L2JTW Server 4.5</td></tr>");
	        replyMSG.append("</body></html>");
	        
	        adminReply.setHtml(replyMSG.toString());
	        activeChar.sendPacket(adminReply); 
	    }
        public void showOptionConfigPage(L2PcInstance activeChar)
        {
            NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
            
            TextBuilder replyMSG = new TextBuilder("<html><body>");
            
            replyMSG.append("<center><table width=270><tr><td width=60><button value=\"����\" action=\"bypass -h admin_admin\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td width=150><center><font color=\"LEVEL\">���A���޲z�D��</font></center></td><td width=60><button value=\"��^\" action=\"bypass -h admin_config_option\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></center><br>");
            replyMSG.append("<center><table width=260><tr>");
            replyMSG.append("<td width=60>");
            replyMSG.append("<button value=\"��ܳ]�w\" action=\"bypass -h admin_config_option\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td><td width=60>");
            replyMSG.append("<button value=\"���N�]�w\" action=\"bypass -h admin_config_altsetting\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td><td width=60>");
            replyMSG.append("<button value=\"��L�]�w\" action=\"bypass -h admin_config_other\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td><td width=60>");
            replyMSG.append("<button value=\"���v�]�w\" action=\"bypass -h admin_config_rate\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td></tr></table></center>");
            
            replyMSG.append("<br><center><table width=260>");
            /*----�ﶵ�]�w----*/
            replyMSG.append("<tr><td><font color=\"LEVEL\">�X��D��۰ʮ���</font> = " + Config.AUTODESTROY_ITEM_AFTER + "</td><td><edit var=\"menu_command1\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set AutoDestroyDroppedItemAfter $menu_command1\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�C�����y�ЦP�B��</font> = " + Config.COORD_SYNCHRONIZE + "</td><td><edit var=\"menu_command2\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set CoordSynchronize $menu_command2\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�w���ϰ�d��P�_</font> = " + Config.ZONE_TOWN + "</td><td><edit var=\"menu_command3\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set ZoneTown $menu_command3\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�R������O�d�Ѽ�</font> = " + Config.DELETE_DAYS + "</td><td><edit var=\"menu_command4\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set DeleteCharAfterDays $menu_command4\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            /*----�ҥκ޲z----*/
            replyMSG.append("<tr><td><font color=\"LEVEL\">��ܩǪ����ťD��</font> = " + Config.SHOW_NPC_LVL + "</td><td></td><td><button value=\"" + !Config.SHOW_NPC_LVL + "\" action=\"bypass -h admin_set ShowNpcLevel " + !Config.SHOW_NPC_LVL + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�޲z�����d���\��</font> = " + Config.ALLOW_RENTPET + "</td><td></td><td><button value=\"" + !Config.ALLOW_RENTPET + "\" action=\"bypass -h admin_set AllowRentPet " + !Config.ALLOW_RENTPET + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            //replyMSG.append("<tr><td><font color=\"LEVEL\">���\���a��󪫫~</font> = " + Config.ALLOW_DISCARDITEM + "</td><td></td><td><button value=\"" + !Config.ALLOW_DISCARDITEM + "\" action=\"bypass -h admin_set AllowDiscardItem " + !Config.ALLOW_DISCARDITEM + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">��T�����p��B�z</font> = " + Config.PRECISE_DROP_CALCULATION + "</td><td></td><td><button value=\"" + !Config.PRECISE_DROP_CALCULATION + "\" action=\"bypass -h admin_set PreciseDropCalculation " + !Config.PRECISE_DROP_CALCULATION + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">���|���~�����]�w</font> = " + Config.MULTIPLE_ITEM_DROP + "</td><td></td><td><button value=\"" + !Config.MULTIPLE_ITEM_DROP + "\" action=\"bypass -h admin_set MultipleItemDrop " + !Config.MULTIPLE_ITEM_DROP + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�����檱�a�������</font> = " + Config.SHOW_LEVEL_COMMUNITYBOARD + "</td><td></td><td><button value=\"" + !Config.SHOW_LEVEL_COMMUNITYBOARD + "\" action=\"bypass -h admin_set ShowLevelOnCommunityBoard " + !Config.SHOW_LEVEL_COMMUNITYBOARD + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�����檱�a���A���</font> = " + Config.SHOW_STATUS_COMMUNITYBOARD + "</td><td></td><td><button value=\"" + !Config.SHOW_STATUS_COMMUNITYBOARD + "\" action=\"bypass -h admin_set ShowStatusOnCommunityBoard " + !Config.SHOW_STATUS_COMMUNITYBOARD + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("</table></center></body></html>");
            adminReply.setHtml(replyMSG.toString());
            activeChar.sendPacket(adminReply);
        }
        
        /**
         * ��ܳ]�w�B�z�� - �Ӧ�altsettings.properties�ɮ׸��
         * @param activeChar
         */
        public void showAltsetConfigPage(L2PcInstance activeChar)
        {
            NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
            
            TextBuilder replyMSG = new TextBuilder("<html><body>");
            
            replyMSG.append("<center><table width=270><tr><td width=60><button value=\"����\" action=\"bypass -h admin_admin\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td width=150><center><font color=\"LEVEL\">��ܳ]�w�B�z��</font></center></td><td width=60><button value=\"��^\" action=\"bypass -h admin_config_option\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></center><br>");
            replyMSG.append("<center><table width=260><tr>");
            replyMSG.append("<td width=60>");
            replyMSG.append("<button value=\"��ܳ]�w\" action=\"bypass -h admin_config_option\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td><td width=60>");
            replyMSG.append("<button value=\"���N�]�w\" action=\"bypass -h admin_config_altsetting\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td><td width=60>");
            replyMSG.append("<button value=\"��L�]�w\" action=\"bypass -h admin_config_other\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td><td width=60>");
            replyMSG.append("<button value=\"���v�]�w\" action=\"bypass -h admin_config_rate\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td></tr></table></center>");
            
            replyMSG.append("<br><center><table width=260>");
            /*----�ҥκ޲z----*/
            replyMSG.append("<tr><td><font color=\"LEVEL\">�۰ʾ߰_���~</font> = " + Config.AUTO_LOOT + "</td><td></td><td><button value=\"" + !Config.AUTO_LOOT + "\" action=\"bypass -h admin_set AutoLoot " + !Config.AUTO_LOOT + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">���`�����Ҧ�</font> = " + Config.ALT_GAME_DELEVEL + "</td><td></td><td><button value=\"" + !Config.ALT_GAME_DELEVEL + "\" action=\"bypass -h admin_set Delevel " + !Config.ALT_GAME_DELEVEL + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�]�k�ˮ`�B�z</font> = " + Config.ALT_GAME_MAGICFAILURES + "</td><td></td><td><button value=\"" + !Config.ALT_GAME_MAGICFAILURES + "\" action=\"bypass -h admin_set MagicFailures " + !Config.ALT_GAME_MAGICFAILURES + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�R�Ѿǲߧޯ�</font> = " + Config.SP_BOOK_NEEDED + "</td><td></td><td><button value=\"" + !Config.SP_BOOK_NEEDED + "\" action=\"bypass -h admin_set SpBookNeeded " + !Config.SP_BOOK_NEEDED + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">���~�s�@�Ҧ�</font> = " + Config.IS_CRAFTING_ENABLED + "</td><td></td><td><button value=\"" + !Config.IS_CRAFTING_ENABLED + "\" action=\"bypass -h admin_set CraftingEnabled " + !Config.IS_CRAFTING_ENABLED + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            //replyMSG.append((new StringBuilder()).append("<tr><td><font color=\"LEVEL\">�i�ϥ���¾��</font> = ").append(Config.ALLOW_CLASS_MASTERS).append("</td><td></td><td><button value=\"").append(!Config.ALLOW_CLASS_MASTERS).append("\" action=\"bypass -h admin_set AllowClassMasters ").append(!Config.ALLOW_CLASS_MASTERS).append("\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>").toString());
            replyMSG.append("<tr><td><font color=\"LEVEL\">�޵P���ɼҦ�</font> = " + Config.ALT_GAME_SHIELD_BLOCKS + "</td><td></td><td><button value=\"" + !Config.ALT_GAME_SHIELD_BLOCKS + "\" action=\"bypass -h admin_set AltShieldBlocks " + !Config.ALT_GAME_SHIELD_BLOCKS + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">���\�K�O�ǰe</font> = " + Config.ALT_GAME_FREE_TELEPORT + "</td><td></td><td><button value=\"" + !Config.ALT_GAME_FREE_TELEPORT + "\" action=\"bypass -h admin_set AltFreeTeleporting " + !Config.ALT_GAME_FREE_TELEPORT + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            //replyMSG.append("<tr><td><font color=\"LEVEL\">��¾�~�K���ȹD��</font> = " + Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS + "</td><td></td><td><button value=\"" + !Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS + "\" action=\"bypass -h admin_set AltSubClassWithoutQuests " + !Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("</table></center></body></html>");
            adminReply.setHtml(replyMSG.toString());
            activeChar.sendPacket(adminReply);
        }
        
        /**
         * ��L�]�w�B�z�� - �Ӧ�other.properties�ɮ׸��
         * @param activeChar
         */
        public void showOtherConfigPage(L2PcInstance activeChar)
        {
            NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
            
            TextBuilder replyMSG = new TextBuilder("<html><body>");
            
            replyMSG.append("<center><table width=270><tr><td width=60><button value=\"�D��\" action=\"bypass -h admin_admin\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td width=150><center><font color=\"LEVEL\">��L�]�w�B�z��</font></center></td><td width=60><button value=\"��^\" action=\"bypass -h admin_config_option\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></center><br>");
            replyMSG.append("<center><table width=260><tr>");
            replyMSG.append("<td width=60>");
            replyMSG.append("<button value=\"��ܳ]�w\" action=\"bypass -h admin_config_option\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td><td width=60>");
            replyMSG.append("<button value=\"���N�]�w\" action=\"bypass -h admin_config_altsetting\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td><td width=60>");
            replyMSG.append("<button value=\"��L�]�w\" action=\"bypass -h admin_config_other\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td><td width=60>");
            replyMSG.append("<button value=\"���v�]�w\" action=\"bypass -h admin_config_rate\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td></tr></table></center>");
            
            replyMSG.append("<br><center><table width=260>");
            /*----��L�]�w----*/
            replyMSG.append("<tr><td><font color=\"LEVEL\">�Цn�s����o���B</font> = " + Config.STARTING_ADENA + "</td><td><edit var=\"menu_command1\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set StartingAdena $menu_command1\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�M�W���s�W�[�t��</font> = " + Config.WYVERN_SPEED + "</td><td><edit var=\"menu_command2\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set WyvernSpeed $menu_command2\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�M�W�y�s�W�[�t��</font> = " + Config.STRIDER_SPEED + "</td><td><edit var=\"menu_command3\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set StriderSpeed $menu_command3\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">���a��a���~����</font> = " + Config.INVENTORY_MAXIMUM_NO_DWARF + "</td><td><edit var=\"menu_command4\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set MaximumSlotsForNoDwarf $menu_command4\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�G�H��a���~����</font> = " + Config.INVENTORY_MAXIMUM_DWARF + "</td><td><edit var=\"menu_command5\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set MaximumSlotsForDwarf $menu_command5\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�j�ƨ��b���\���v</font> = " + Config.ENCHANT_CHANCE_ARMOR + "</td><td><edit var=\"menu_command6\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set EnchantChanceArmor $menu_command6\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�j�ƨ��b���\���v</font> = " + Config.ENCHANT_CHANCE_WEAPON + "</td><td><edit var=\"menu_command6\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set EnchantChanceWeapon $menu_command6\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            //replyMSG.append("<tr><td><font color=\"LEVEL\">�j�Ƶ��ų̰��W��</font> = " + Config.ENCHANT_MAX_ARMOR + "</td><td><edit var=\"menu_command7\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set EnchantMaxArmor $menu_command7\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�j�Ƶ��ų̰��W��</font> = " + Config.ENCHANT_MAX_WEAPON + "</td><td><edit var=\"menu_command7\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set EnchantMaxWeapon $menu_command7\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            
            /*----�^�Э��v----*/
            replyMSG.append("<tr><td><font color=\"LEVEL\">��O�^�гt�׭��v</font> = " + Config.HP_REGEN_MULTIPLIER + "</td><td><edit var=\"menu_command8\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set HpRegenMultiplier $menu_command8\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�]�k�^�гt�׭��v</font> = " + Config.MP_REGEN_MULTIPLIER + "</td><td><edit var=\"menu_command9\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set MpRegenMultiplier $menu_command9\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">���Ӧ^�гt�׭��v</font> = " + Config.CP_REGEN_MULTIPLIER + "</td><td><edit var=\"menu_command10\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set CpRegenMultiplier $menu_command10\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">������O�^�Э��v</font> = " + Config.RAID_HP_REGEN_MULTIPLIER + "</td><td><edit var=\"menu_command11\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set RaidHpRegenMultiplier $menu_command11\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�����]�k�^�Э��v</font> = " + Config.RAID_MP_REGEN_MULTIPLIER + "</td><td><edit var=\"menu_command12\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set RaidMpRegenMultiplier $menu_command12\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">����j�ƨ��m���v</font> = " + Config.RAID_DEFENCE_MULTIPLIER + "</td><td><edit var=\"menu_command13\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set RaidDefenceMultiplier $menu_command13\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            /*----�B�z����----*/
            replyMSG.append("<tr><td><font color=\"LEVEL\">��k���O�B�z�ɶ�</font> = " + Config.UNSTUCK_INTERVAL + "</td><td><edit var=\"menu_command14\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set UnstuckInterval $menu_command14\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�n�J�ǰe�O�@�ɶ�</font> = " + Config.PLAYER_SPAWN_PROTECTION + "</td><td><edit var=\"menu_command15\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set PlayerSpawnProtection $menu_command15\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">���ͫᰫ�ӫ�_��</font> = " + Config.RESPAWN_RESTORE_CP + "</td><td><edit var=\"menu_command16\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set RespawnRestoreCP $menu_command16\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">���ͫ���O��_��</font> = " + Config.RESPAWN_RESTORE_HP + "</td><td><edit var=\"menu_command17\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set RespawnRestoreHP $menu_command17\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">���ͫ��]�k��_��</font> = " + Config.RESPAWN_RESTORE_MP + "</td><td><edit var=\"menu_command18\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set RespawnRestoreMP $menu_command18\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�@��ө��̤j�ƶq</font> = " + Config.MAX_PVTSTORE_SLOTS_OTHER + "</td><td><edit var=\"menu_command19\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set MaxPvtStoreSlotsOther $menu_command19\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�G�H�ө��̤j�ƶq</font> = " + Config.MAX_PVTSTORE_SLOTS_DWARF + "</td><td><edit var=\"menu_command20\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set MaxPvtStoreSlotsDwarf $menu_command20\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            /*----�ҥκ޲z----*/
            replyMSG.append("<tr><td><font color=\"LEVEL\">���\�d���i�ƳB�z</font> = " + Config.ALLOW_WYVERN_UPGRADER + "</td><td></td><td><button value=\"" + !Config.ALLOW_WYVERN_UPGRADER + "\" action=\"bypass -h admin_set AllowWyvernUpgrader " + !Config.ALLOW_WYVERN_UPGRADER + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�л\�ޯ���C�ĪG</font> = " + Config.EFFECT_CANCELING + "</td><td></td><td><button value=\"" + !Config.EFFECT_CANCELING + "\" action=\"bypass -h admin_set CancelLesserEffect " + !Config.EFFECT_CANCELING + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">���\ĵ�ç����Ǫ�</font> = " + Config.ALLOW_GUARDS + "</td><td></td><td><button value=\"" + !Config.ALLOW_GUARDS + "\" action=\"bypass -h admin_set AllowGuards " + !Config.ALLOW_GUARDS + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">���\�ũǱ������~</font> = " + Config.DEEPBLUE_DROP_RULES + "</td><td></td><td><button value=\"" + !Config.DEEPBLUE_DROP_RULES + "\" action=\"bypass -h admin_set UseDeepBlueDropRules " + !Config.DEEPBLUE_DROP_RULES + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�O�d�ޯ�ĪG����</font> = " + Config.STORE_SKILL_COOLTIME + "</td><td></td><td><button value=\"" + !Config.STORE_SKILL_COOLTIME + "\" action=\"bypass -h admin_set StoreSkillCooltime " + !Config.STORE_SKILL_COOLTIME + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�]�I�ӤH���i�B�z</font> = " + Config.ANNOUNCE_MAMMON_SPAWN + "</td><td></td><td><button value=\"" + !Config.ANNOUNCE_MAMMON_SPAWN + "\" action=\"bypass -h admin_set AnnounceMammonSpawn " + !Config.ANNOUNCE_MAMMON_SPAWN + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("</table></center></body></html>");
            adminReply.setHtml(replyMSG.toString());
            activeChar.sendPacket(adminReply);
        }
        
        /**
         * �S��]�w�B�z�� - �Ӧ�rates.properties�ɮ׸��
         * @param activeChar
         */
        public void showRateConfigPage(L2PcInstance activeChar)
        {
            NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
            
            TextBuilder replyMSG = new TextBuilder("<html><body>");
            
            replyMSG.append("<center><table width=270><tr><td width=60><button value=\"����\" action=\"bypass -h admin_admin\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td width=150><center><font color=\"LEVEL\">�S��]�w�B�z</font></center></td><td width=60><button value=\"��^\" action=\"bypass -h admin_config_option\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></center><br>");
            replyMSG.append("<center><table width=260><tr>");
            replyMSG.append("<td width=60>");
            replyMSG.append("<button value=\"��ܳ]�w\" action=\"bypass -h admin_config_option\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td><td width=60>");
            replyMSG.append("<button value=\"���N�]�w\" action=\"bypass -h admin_config_altsetting\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td><td width=60>");
            replyMSG.append("<button value=\"��L�]�w\" action=\"bypass -h admin_config_other\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td><td width=60>");
            replyMSG.append("<button value=\"���v�]�w\" action=\"bypass -h admin_config_rate\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td></tr></table></center>");
            
            replyMSG.append("<br><center><table width=260>");
            /*----���v�]�w----*/
            replyMSG.append("<tr><td><font color=\"LEVEL\">�g�筿�v</font> = " + Config.RATE_XP + "</td><td><edit var=\"menu_command1\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set RateXP $menu_command1\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�ޯ୿�v</font> = " + Config.RATE_SP + "</td><td><edit var=\"menu_command2\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set RateSP $menu_command2\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�ն��g�筿�v</font> = " + Config.RATE_PARTY_XP + "</td><td><edit var=\"menu_command3\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set RatePartyXp $menu_command3\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�ն��ޯ୿�v</font> = " + Config.RATE_PARTY_SP + "</td><td><edit var=\"menu_command4\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set RatePartySp $menu_command4\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�����������v</font> = " + Config.RATE_DROP_ADENA + "</td><td><edit var=\"menu_command5\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set RateDropAdena $menu_command5\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">���~�������v</font> = " + Config.RATE_DROP_ITEMS + "</td><td><edit var=\"menu_command6\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set RateDropItems $menu_command6\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�^�����ƭ��v</font> = " + Config.RATE_DROP_SPOIL + "</td><td><edit var=\"menu_command7\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set RateDropSpoil $menu_command7\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">���ȼ��y���v</font> = " + Config.RATE_QUESTS_REWARD + "</td><td><edit var=\"menu_command8\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set RateQuestsReward $menu_command8\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�ӫ~��O���v</font> = " + Config.RATE_CONSUMABLE_COST + "</td><td><edit var=\"menu_command9\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set RateConsumableCost $menu_command9\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">�ʦV�g��l�����v</font> = " + Config.RATE_KARMA_EXP_LOST + "</td><td><edit var=\"menu_command10\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set RateKarmaExpLost $menu_command10\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">ĵ�û��歿�v</font> = " + Config.RATE_SIEGE_GUARDS_PRICE + "</td><td><edit var=\"menu_command11\" width=40 height=15></td><td><button value=\"�]�w\" action=\"bypass -h admin_set RateSiegeGuardsPrice $menu_command11\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("</table></center></body></html>");
            adminReply.setHtml(replyMSG.toString());
            activeChar.sendPacket(adminReply);
        }

}
