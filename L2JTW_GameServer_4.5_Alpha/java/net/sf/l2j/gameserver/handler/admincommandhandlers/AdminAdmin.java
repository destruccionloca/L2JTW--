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
            activeChar.sendMessage("註冊GM列表");
		}
		else if(command.startsWith("admin_gmlistoff"))
		{
		    GmListTable.getInstance().deleteGm(activeChar);
            activeChar.sendMessage("移除GM列表");
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
            
            activeChar.sendMessage("奧林匹亞資料儲存");
            
        }
        
        else if(command.startsWith("admin_manualhero"))
        {
            try 
            {
                Olympiad.getInstance().manualSelectHeroes();
            }
            catch(Exception e){e.printStackTrace();}
            
            activeChar.sendMessage("設置英雄");
            
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
                    activeChar.sendMessage("無重量開始");
                }
                else if(st.nextToken().equalsIgnoreCase("off"))
                {
                    activeChar.setDietMode(false);
                    activeChar.sendMessage("無重量關閉");
                }
            }
            catch(Exception ex)
            {
                if(activeChar.getDietMode())
                    activeChar.sendMessage("無重量目前啟動");
                else
                    activeChar.sendMessage("無重量目前關閉");
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
                    activeChar.sendMessage("交易關閉啟動");
                }
                else if (mode.equalsIgnoreCase("off"))
                {
                    activeChar.setTradeRefusal(false);
                    activeChar.sendMessage("交易關閉取消");
                }
            }
            catch(Exception ex)
            {
                if(activeChar.getTradeRefusal())
                    activeChar.sendMessage("交易關閉目前為啟動");
                else
                    activeChar.sendMessage("交易關閉目前為取消");
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
					activeChar.sendMessage("multisell 資料重新讀取");
				}
				else if(type.startsWith("teleport"))
				{
					TeleportLocationTable.getInstance().reloadAll();
					activeChar.sendMessage("teleport 資料重新讀取");
				}
				else if(type.startsWith("skill"))
				{
					SkillTable.getInstance().reload();
					activeChar.sendMessage("skills 資料重新讀取");
				}
				else if(type.equals("npc"))
				{
					NpcTable.getInstance().reloadAllNpc();
					activeChar.sendMessage("npcs 資料重新讀取");
				}
				else if(type.startsWith("htm"))
				{
					HtmCache.getInstance().reload();
					activeChar.sendMessage("Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage()  + " megabytes 在 " + HtmCache.getInstance().getLoadedFiles() + " 個檔案讀取");
				}
				else if(type.startsWith("item"))
				{
					ItemTable.getInstance().reload();
					activeChar.sendMessage("Item 資料重新讀取");
				}
				else if(type.startsWith("instancemanager"))
				{
					Manager.reloadAll();
					activeChar.sendMessage("所有Instance重新讀取");
				}
			}
			catch(Exception e)
			{
				activeChar.sendMessage("使用方法:  //reload <multisell|skill|npc|htm|item|instancemanager>");
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
					activeChar.sendMessage("數值 "+pName+" 成功設定為 "+pValue);
				else 
					activeChar.sendMessage("錯誤設定值");
			}
			catch(Exception e)
			{

				if (cmd.length==2)
					activeChar.sendMessage("使用方法:  //set parameter=數值");
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




	        StringBuffer replyMSG = new StringBuffer("<html><title>L2JTW 伺服器控制介面</title><body>");
	        replyMSG.append("<br><center><table width=260>");
	       	replyMSG.append("<tr>");
	        replyMSG.append("<td><button value=\"設定\" action=\"bypass -h admin_config_option\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
	        replyMSG.append("<td><button value=\"遊戲\" action=\"bypass -h admin_admin2\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
	        replyMSG.append("<td><button value=\"效果\" action=\"bypass -h admin_admin3\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
	        replyMSG.append("<td><button value=\"伺服器\" action=\"bypass -h admin_admin4\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
	        replyMSG.append("<td><button value=\"外掛\" action=\"bypass -h admin_admin5\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
	        replyMSG.append("</tr>");
	        replyMSG.append("</table></center>");
            //replyMSG.append("<center><table width=270><tr><td width=60><button value=\"操作\" action=\"bypass -h admin_admin2\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td width=150><center><font color=\"LEVEL\">伺服器管理台</font></center></td><td width=60><button value=\"設置\" action=\"bypass -h admin_config_option\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></center><br>");
            replyMSG.append("<center><table width=200><tr><td>");
	        replyMSG.append("<button value=\"線上玩家\" action=\"bypass -h admin_show_characters 0\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"伺服器管理\" action=\"bypass -h admin_server_shutdown\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">"); 
	        replyMSG.append("<button value=\"公告管理\" action=\"bypass -h admin_list_announcements\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"根據地管理\" action=\"bypass -h admin_siege\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<br>");
	        replyMSG.append("<button value=\"使用飛龍\" action=\"bypass -h admin_ride_wyvern\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"解除飛龍\" action=\"bypass -h admin_unride_wyvern\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"隱形狀態\" action=\"bypass -h admin_invisible\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"解除隱形\" action=\"bypass -h admin_visible\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</td><td>");
	        replyMSG.append("<button value=\"GM 商店\" action=\"bypass -h admin_gmshop\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"創造物品\" action=\"bypass -h admin_itemcreate\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"玩家管理\" action=\"bypass -h admin_char_manage\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"傳送選單\" action=\"bypass -h admin_show_moves\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<br>");
	        replyMSG.append("<button value=\"物品強化管理\" action=\"bypass -h admin_enchant\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"Cache控制介面\" action=\"bypass -h admin_cache\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");    
	        replyMSG.append("<button value=\"訴求管理介面\" action=\"bypass -h admin_view_petitions\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"NPC管理介面\" action=\"bypass -h admin_show_spawns\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<br>");
	        replyMSG.append("</td></tr></table></center>");
	        replyMSG.append("<center><table><tr><td>");
	        //replyMSG.append("<button value=\"伺服器狀態\" action=\"bypass -h admin_server_login\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        //replyMSG.append("<button value=\"多功能控制\" action=\"bypass -h admin_eventmenu\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        //replyMSG.append("<button value=\"Cache控制\" action=\"bypass -h admin_cache\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        //replyMSG.append("<button value=\"檢視訴求\" action=\"bypass -h admin_view_petitions\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        //replyMSG.append("<button value=\"第二頁\" action=\"bypass -h admin_admin2\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</td></tr></table></center><br>");
	        replyMSG.append("<center>玩家:</center>");
	        replyMSG.append("<center><edit var=\"menu_command\" width=100 height=15></center><br>");
	        replyMSG.append("<center><table><tr><td>");
	        replyMSG.append("<button value=\"滅亡\" action=\"bypass -h admin_kill $menu_command\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
	        replyMSG.append("<button value=\"踢除\" action=\"bypass -h admin_kick $menu_command\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
	        replyMSG.append("<button value=\"封鎖\" action=\"bypass -h admin_ban $menu_command\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
	        replyMSG.append("</table><br><table width=200><tr><td>");
	        replyMSG.append("<button value=\"聊天封鎖\" action=\"bypass -h admin_banchat $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
	        replyMSG.append("<button value=\"解除聊天封鎖\" action=\"bypass -h admin_unbanchat $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</td></tr></table></center><br>");
	        replyMSG.append("<font color=\"LEVEL\"><tr><td>伺服器版本: L2JTW Server 4.5</td></tr></font>");
	        replyMSG.append("</body></html>");


	        adminReply.setHtml(replyMSG.toString());
	        activeChar.sendPacket(adminReply); 
	    }
	    
	    public void showMainPage2(L2PcInstance activeChar)
	    {
	        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

	        StringBuffer replyMSG = new StringBuffer("<html><body>");
	        replyMSG.append("<center><table width=260><tr><td width=40>");
	        replyMSG.append("<button value=\"首頁\" action=\"bypass -h admin_admin\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</td><td width=180>");
	        replyMSG.append("<center>伺服器管理台</center>");
	        replyMSG.append("</td><td width=40><button value=\"返回\" action=\"bypass -h admin_admin\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</td></tr></table></center>");
	        replyMSG.append("<center><table width=200><tr><td>");
	        replyMSG.append("<button value=\"召喚寶箱\" action=\"bypass -h admin_spawn_monster 1042\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"寶箱權限\" action=\"bypass -h admin_box_access $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">"); 
	        replyMSG.append("<button value=\"開門\" action=\"bypass -h admin_open $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"關門\" action=\"bypass -h admin_close $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<br>");
	        replyMSG.append("<button value=\"麻痺所有玩家\" action=\"bypass -h admin_para_all\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"解除所有麻痺\" action=\"bypass -h admin_unpara_all\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"巨頭效果\" action=\"bypass -h admin_bighead\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<br>");
	        replyMSG.append("<button value=\"GM詢問處\" action=\"bypass -h admin_teleport_character_to_menu $menu_command -114560 -249360 -2980\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"轉換自己\" action=\"bypass -h admin_polyself $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"解除轉換自己\" action=\"bypass -h admin_unpolyself\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</td><td>");       
	        replyMSG.append("<button value=\"設置性向\" action=\"bypass -h admin_setkarma $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"解除性向\" action=\"bypass -h admin_nokarma\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"怪物競賽\" action=\"bypass -h admin_mons\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"播放音樂\" action=\"bypass -h admin_play_sounds\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<br>");
	        replyMSG.append("<button value=\"麻痺目標\" action=\"bypass -h admin_para\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"解除目標麻痺\" action=\"bypass -h admin_unpara\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"解除巨頭效果\" action=\"bypass -h admin_shrinkhead\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<br>");
	        replyMSG.append("<button value=\"更改名子\" action=\"bypass -h admin_changename $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"創造物品\" action=\"bypass -h admin_itemcreate\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"強化武器\" action=\"bypass -h admin_setew $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</td></tr></table></center><br>");
	        replyMSG.append("<center><table><tr><td>");
	        replyMSG.append("<button value=\"地震效果\" action=\"bypass -h admin_earthquake $menu_command\" width=95 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"復活效果\" action=\"bypass -h admin_res $menu_command\" width=95 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"回覆效果\" action=\"bypass -h admin_heal $menu_command\" width=95 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"尋找NPC位置\" action=\"bypass -h admin_list_spawns $menu_command\" width=95 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"NPC互打計算\" action=\"bypass -h admin_fight_calculator\" width=95 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"目標互打計算\" action=\"bypass -h admin_fcs\" width=95 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"重讀系統資料\" action=\"bypass -h admin_reload $menu_command\" width=95 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</td></tr></table></center><br>");
	        replyMSG.append("<center>指令數值</center>");
	        replyMSG.append("<center><edit var=\"menu_command\" width=100 height=15></center><br>");
	        replyMSG.append("<center><table><tr><td>");
	        replyMSG.append("<button value=\"滅亡\" action=\"bypass -h admin_kill $menu_command\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
	        replyMSG.append("<button value=\"踢除\" action=\"bypass -h admin_kick $menu_command\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
	        replyMSG.append("<button value=\"封鎖\" action=\"bypass -h admin_ban $menu_command\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
	        replyMSG.append("</table><br><table width=200><tr><td>");
	        replyMSG.append("</td><td>");
	        replyMSG.append("");
	        replyMSG.append("</td></tr></table></center>");
	        replyMSG.append("<center><table><br>");
	        replyMSG.append("<tr><td><button value=\"白天\" action=\"bypass -h admin_atmosphere sky day\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
	        replyMSG.append("<td><button value=\"黑夜\" action=\"bypass -h admin_atmosphere sky night\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
	        replyMSG.append("<tr><td><button value=\"黎明天空\" action=\"bypass -h admin_atmosphere signsky dawn\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
	        replyMSG.append("<td><button value=\"黃昏天空\" action=\"bypass -h admin_atmosphere signsky dusk\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
	        replyMSG.append("</table></center><br><br><br>");

	        replyMSG.append("<tr><td>伺服器版本: L2JTW Server 4.5</td></tr>");
	        replyMSG.append("</body></html>");

	        adminReply.setHtml(replyMSG.toString());
	        activeChar.sendPacket(adminReply); 
	    }
	    public void showMainPageCache(L2PcInstance activeChar)
	    {
	        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

	        StringBuffer replyMSG = new StringBuffer("<html><body>");
	        replyMSG.append("<center><table width=260><tr><td width=40>");
	        replyMSG.append("<button value=\"首頁\" action=\"bypass -h admin_admin\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</td><td width=180>");
	        replyMSG.append("<center>Cache控制</center>");
	        replyMSG.append("</td><td width=40><button value=\"返回\" action=\"bypass -h admin_admin\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</td></tr></table></center>");
	        replyMSG.append("<center><table width=200><tr><td>");
	        replyMSG.append("<br><center>");
	        replyMSG.append("<button value=\"重制HTML\" action=\"bypass -h admin_cache_htm_rebuild\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"讀取HTML\" action=\"bypass -h admin_cache_htm_reload\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"重制盟徽\" action=\"bypass -h admin_cache_crest_rebuild\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"修正盟徽\" action=\"bypass -h admin_cache_crest_fix\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"讀取Skill\" action=\"bypass -h admin_reload skill\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"讀取Items\" action=\"bypass -h admin_reload item\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
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
	        replyMSG.append("<tr><td>伺服器版本: L2JTW Server 4.5</td></tr>");
	        replyMSG.append("</body></html>");
	        
	        adminReply.setHtml(replyMSG.toString());
	        activeChar.sendPacket(adminReply); 
	    }
	    
	    public void showMainPageEventMenu(L2PcInstance activeChar)
	    {
	        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

	        StringBuffer replyMSG = new StringBuffer("<html><body>");
	        replyMSG.append("<center><table width=260><tr><td width=40>");
	        replyMSG.append("<button value=\"首頁\" action=\"bypass -h admin_admin\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</td><td width=180>");
	        replyMSG.append("<center>活動操作</center>");
	        replyMSG.append("</td><td width=40><button value=\"返回\" action=\"bypass -h admin_admin\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</td></tr></table></center>");
	        replyMSG.append("<center><table width=200><tr><td>");
	        replyMSG.append("<br><center>");
	        replyMSG.append("<button value=\"CTF活動介面\" action=\"bypass -h admin_ctf\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
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
	        replyMSG.append("<tr><td>伺服器版本: L2JTW Server 4.5</td></tr>");
	        replyMSG.append("</body></html>");
	        
	        adminReply.setHtml(replyMSG.toString());
	        activeChar.sendPacket(adminReply); 
	    }
        public void showOptionConfigPage(L2PcInstance activeChar)
        {
            NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
            
            TextBuilder replyMSG = new TextBuilder("<html><body>");
            
            replyMSG.append("<center><table width=270><tr><td width=60><button value=\"首頁\" action=\"bypass -h admin_admin\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td width=150><center><font color=\"LEVEL\">伺服器管理主頁</font></center></td><td width=60><button value=\"返回\" action=\"bypass -h admin_config_option\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></center><br>");
            replyMSG.append("<center><table width=260><tr>");
            replyMSG.append("<td width=60>");
            replyMSG.append("<button value=\"選擇設定\" action=\"bypass -h admin_config_option\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td><td width=60>");
            replyMSG.append("<button value=\"替代設定\" action=\"bypass -h admin_config_altsetting\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td><td width=60>");
            replyMSG.append("<button value=\"其他設定\" action=\"bypass -h admin_config_other\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td><td width=60>");
            replyMSG.append("<button value=\"倍率設定\" action=\"bypass -h admin_config_rate\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td></tr></table></center>");
            
            replyMSG.append("<br><center><table width=260>");
            /*----選項設定----*/
            replyMSG.append("<tr><td><font color=\"LEVEL\">幾秒道具自動消失</font> = " + Config.AUTODESTROY_ITEM_AFTER + "</td><td><edit var=\"menu_command1\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set AutoDestroyDroppedItemAfter $menu_command1\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">遊戲內座標同步化</font> = " + Config.COORD_SYNCHRONIZE + "</td><td><edit var=\"menu_command2\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set CoordSynchronize $menu_command2\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">安全區域範圍判斷</font> = " + Config.ZONE_TOWN + "</td><td><edit var=\"menu_command3\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set ZoneTown $menu_command3\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">刪除角色保留天數</font> = " + Config.DELETE_DAYS + "</td><td><edit var=\"menu_command4\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set DeleteCharAfterDays $menu_command4\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            /*----啟用管理----*/
            replyMSG.append("<tr><td><font color=\"LEVEL\">顯示怪物等級主動</font> = " + Config.SHOW_NPC_LVL + "</td><td></td><td><button value=\"" + !Config.SHOW_NPC_LVL + "\" action=\"bypass -h admin_set ShowNpcLevel " + !Config.SHOW_NPC_LVL + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">管理租借寵物功能</font> = " + Config.ALLOW_RENTPET + "</td><td></td><td><button value=\"" + !Config.ALLOW_RENTPET + "\" action=\"bypass -h admin_set AllowRentPet " + !Config.ALLOW_RENTPET + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            //replyMSG.append("<tr><td><font color=\"LEVEL\">允許玩家丟棄物品</font> = " + Config.ALLOW_DISCARDITEM + "</td><td></td><td><button value=\"" + !Config.ALLOW_DISCARDITEM + "\" action=\"bypass -h admin_set AllowDiscardItem " + !Config.ALLOW_DISCARDITEM + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">精確掉落計算處理</font> = " + Config.PRECISE_DROP_CALCULATION + "</td><td></td><td><button value=\"" + !Config.PRECISE_DROP_CALCULATION + "\" action=\"bypass -h admin_set PreciseDropCalculation " + !Config.PRECISE_DROP_CALCULATION + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">堆疊物品掉落設定</font> = " + Config.MULTIPLE_ITEM_DROP + "</td><td></td><td><button value=\"" + !Config.MULTIPLE_ITEM_DROP + "\" action=\"bypass -h admin_set MultipleItemDrop " + !Config.MULTIPLE_ITEM_DROP + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">公布欄玩家等級顯示</font> = " + Config.SHOW_LEVEL_COMMUNITYBOARD + "</td><td></td><td><button value=\"" + !Config.SHOW_LEVEL_COMMUNITYBOARD + "\" action=\"bypass -h admin_set ShowLevelOnCommunityBoard " + !Config.SHOW_LEVEL_COMMUNITYBOARD + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">公布欄玩家狀態顯示</font> = " + Config.SHOW_STATUS_COMMUNITYBOARD + "</td><td></td><td><button value=\"" + !Config.SHOW_STATUS_COMMUNITYBOARD + "\" action=\"bypass -h admin_set ShowStatusOnCommunityBoard " + !Config.SHOW_STATUS_COMMUNITYBOARD + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("</table></center></body></html>");
            adminReply.setHtml(replyMSG.toString());
            activeChar.sendPacket(adminReply);
        }
        
        /**
         * 選擇設定處理頁 - 來自altsettings.properties檔案資料
         * @param activeChar
         */
        public void showAltsetConfigPage(L2PcInstance activeChar)
        {
            NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
            
            TextBuilder replyMSG = new TextBuilder("<html><body>");
            
            replyMSG.append("<center><table width=270><tr><td width=60><button value=\"首頁\" action=\"bypass -h admin_admin\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td width=150><center><font color=\"LEVEL\">選擇設定處理頁</font></center></td><td width=60><button value=\"返回\" action=\"bypass -h admin_config_option\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></center><br>");
            replyMSG.append("<center><table width=260><tr>");
            replyMSG.append("<td width=60>");
            replyMSG.append("<button value=\"選擇設定\" action=\"bypass -h admin_config_option\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td><td width=60>");
            replyMSG.append("<button value=\"替代設定\" action=\"bypass -h admin_config_altsetting\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td><td width=60>");
            replyMSG.append("<button value=\"其他設定\" action=\"bypass -h admin_config_other\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td><td width=60>");
            replyMSG.append("<button value=\"倍率設定\" action=\"bypass -h admin_config_rate\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td></tr></table></center>");
            
            replyMSG.append("<br><center><table width=260>");
            /*----啟用管理----*/
            replyMSG.append("<tr><td><font color=\"LEVEL\">自動撿起物品</font> = " + Config.AUTO_LOOT + "</td><td></td><td><button value=\"" + !Config.AUTO_LOOT + "\" action=\"bypass -h admin_set AutoLoot " + !Config.AUTO_LOOT + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">死亡掉等模式</font> = " + Config.ALT_GAME_DELEVEL + "</td><td></td><td><button value=\"" + !Config.ALT_GAME_DELEVEL + "\" action=\"bypass -h admin_set Delevel " + !Config.ALT_GAME_DELEVEL + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">魔法傷害處理</font> = " + Config.ALT_GAME_MAGICFAILURES + "</td><td></td><td><button value=\"" + !Config.ALT_GAME_MAGICFAILURES + "\" action=\"bypass -h admin_set MagicFailures " + !Config.ALT_GAME_MAGICFAILURES + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">買書學習技能</font> = " + Config.SP_BOOK_NEEDED + "</td><td></td><td><button value=\"" + !Config.SP_BOOK_NEEDED + "\" action=\"bypass -h admin_set SpBookNeeded " + !Config.SP_BOOK_NEEDED + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">物品製作模式</font> = " + Config.IS_CRAFTING_ENABLED + "</td><td></td><td><button value=\"" + !Config.IS_CRAFTING_ENABLED + "\" action=\"bypass -h admin_set CraftingEnabled " + !Config.IS_CRAFTING_ENABLED + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            //replyMSG.append((new StringBuilder()).append("<tr><td><font color=\"LEVEL\">可使用轉職員</font> = ").append(Config.ALLOW_CLASS_MASTERS).append("</td><td></td><td><button value=\"").append(!Config.ALLOW_CLASS_MASTERS).append("\" action=\"bypass -h admin_set AllowClassMasters ").append(!Config.ALLOW_CLASS_MASTERS).append("\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>").toString());
            replyMSG.append("<tr><td><font color=\"LEVEL\">盾牌格檔模式</font> = " + Config.ALT_GAME_SHIELD_BLOCKS + "</td><td></td><td><button value=\"" + !Config.ALT_GAME_SHIELD_BLOCKS + "\" action=\"bypass -h admin_set AltShieldBlocks " + !Config.ALT_GAME_SHIELD_BLOCKS + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">允許免費傳送</font> = " + Config.ALT_GAME_FREE_TELEPORT + "</td><td></td><td><button value=\"" + !Config.ALT_GAME_FREE_TELEPORT + "\" action=\"bypass -h admin_set AltFreeTeleporting " + !Config.ALT_GAME_FREE_TELEPORT + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            //replyMSG.append("<tr><td><font color=\"LEVEL\">副職業免任務道具</font> = " + Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS + "</td><td></td><td><button value=\"" + !Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS + "\" action=\"bypass -h admin_set AltSubClassWithoutQuests " + !Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("</table></center></body></html>");
            adminReply.setHtml(replyMSG.toString());
            activeChar.sendPacket(adminReply);
        }
        
        /**
         * 其他設定處理頁 - 來自other.properties檔案資料
         * @param activeChar
         */
        public void showOtherConfigPage(L2PcInstance activeChar)
        {
            NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
            
            TextBuilder replyMSG = new TextBuilder("<html><body>");
            
            replyMSG.append("<center><table width=270><tr><td width=60><button value=\"主頁\" action=\"bypass -h admin_admin\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td width=150><center><font color=\"LEVEL\">其他設定處理頁</font></center></td><td width=60><button value=\"返回\" action=\"bypass -h admin_config_option\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></center><br>");
            replyMSG.append("<center><table width=260><tr>");
            replyMSG.append("<td width=60>");
            replyMSG.append("<button value=\"選擇設定\" action=\"bypass -h admin_config_option\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td><td width=60>");
            replyMSG.append("<button value=\"替代設定\" action=\"bypass -h admin_config_altsetting\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td><td width=60>");
            replyMSG.append("<button value=\"其他設定\" action=\"bypass -h admin_config_other\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td><td width=60>");
            replyMSG.append("<button value=\"倍率設定\" action=\"bypass -h admin_config_rate\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td></tr></table></center>");
            
            replyMSG.append("<br><center><table width=260>");
            /*----其他設定----*/
            replyMSG.append("<tr><td><font color=\"LEVEL\">創好新手獲得金額</font> = " + Config.STARTING_ADENA + "</td><td><edit var=\"menu_command1\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set StartingAdena $menu_command1\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">騎上飛龍增加速度</font> = " + Config.WYVERN_SPEED + "</td><td><edit var=\"menu_command2\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set WyvernSpeed $menu_command2\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">騎上座龍增加速度</font> = " + Config.STRIDER_SPEED + "</td><td><edit var=\"menu_command3\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set StriderSpeed $menu_command3\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">玩家攜帶物品限制</font> = " + Config.INVENTORY_MAXIMUM_NO_DWARF + "</td><td><edit var=\"menu_command4\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set MaximumSlotsForNoDwarf $menu_command4\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">矮人攜帶物品限制</font> = " + Config.INVENTORY_MAXIMUM_DWARF + "</td><td><edit var=\"menu_command5\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set MaximumSlotsForDwarf $menu_command5\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">強化卷軸成功機率</font> = " + Config.ENCHANT_CHANCE_ARMOR + "</td><td><edit var=\"menu_command6\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set EnchantChanceArmor $menu_command6\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">強化卷軸成功機率</font> = " + Config.ENCHANT_CHANCE_WEAPON + "</td><td><edit var=\"menu_command6\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set EnchantChanceWeapon $menu_command6\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            //replyMSG.append("<tr><td><font color=\"LEVEL\">強化等級最高上限</font> = " + Config.ENCHANT_MAX_ARMOR + "</td><td><edit var=\"menu_command7\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set EnchantMaxArmor $menu_command7\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">強化等級最高上限</font> = " + Config.ENCHANT_MAX_WEAPON + "</td><td><edit var=\"menu_command7\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set EnchantMaxWeapon $menu_command7\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            
            /*----回覆倍率----*/
            replyMSG.append("<tr><td><font color=\"LEVEL\">體力回覆速度倍率</font> = " + Config.HP_REGEN_MULTIPLIER + "</td><td><edit var=\"menu_command8\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set HpRegenMultiplier $menu_command8\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">魔法回覆速度倍率</font> = " + Config.MP_REGEN_MULTIPLIER + "</td><td><edit var=\"menu_command9\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set MpRegenMultiplier $menu_command9\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">鬥志回覆速度倍率</font> = " + Config.CP_REGEN_MULTIPLIER + "</td><td><edit var=\"menu_command10\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set CpRegenMultiplier $menu_command10\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">首領體力回覆倍率</font> = " + Config.RAID_HP_REGEN_MULTIPLIER + "</td><td><edit var=\"menu_command11\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set RaidHpRegenMultiplier $menu_command11\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">首領魔法回覆倍率</font> = " + Config.RAID_MP_REGEN_MULTIPLIER + "</td><td><edit var=\"menu_command12\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set RaidMpRegenMultiplier $menu_command12\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">首領強化防禦倍率</font> = " + Config.RAID_DEFENCE_MULTIPLIER + "</td><td><edit var=\"menu_command13\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set RaidDefenceMultiplier $menu_command13\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            /*----處理機制----*/
            replyMSG.append("<tr><td><font color=\"LEVEL\">脫逃指令處理時間</font> = " + Config.UNSTUCK_INTERVAL + "</td><td><edit var=\"menu_command14\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set UnstuckInterval $menu_command14\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">登入傳送保護時間</font> = " + Config.PLAYER_SPAWN_PROTECTION + "</td><td><edit var=\"menu_command15\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set PlayerSpawnProtection $menu_command15\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">重生後鬥志恢復值</font> = " + Config.RESPAWN_RESTORE_CP + "</td><td><edit var=\"menu_command16\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set RespawnRestoreCP $menu_command16\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">重生後體力恢復值</font> = " + Config.RESPAWN_RESTORE_HP + "</td><td><edit var=\"menu_command17\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set RespawnRestoreHP $menu_command17\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">重生後魔法恢復值</font> = " + Config.RESPAWN_RESTORE_MP + "</td><td><edit var=\"menu_command18\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set RespawnRestoreMP $menu_command18\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">一般商店最大數量</font> = " + Config.MAX_PVTSTORE_SLOTS_OTHER + "</td><td><edit var=\"menu_command19\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set MaxPvtStoreSlotsOther $menu_command19\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">矮人商店最大數量</font> = " + Config.MAX_PVTSTORE_SLOTS_DWARF + "</td><td><edit var=\"menu_command20\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set MaxPvtStoreSlotsDwarf $menu_command20\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            /*----啟用管理----*/
            replyMSG.append("<tr><td><font color=\"LEVEL\">允許寵物進化處理</font> = " + Config.ALLOW_WYVERN_UPGRADER + "</td><td></td><td><button value=\"" + !Config.ALLOW_WYVERN_UPGRADER + "\" action=\"bypass -h admin_set AllowWyvernUpgrader " + !Config.ALLOW_WYVERN_UPGRADER + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">覆蓋技能較低效果</font> = " + Config.EFFECT_CANCELING + "</td><td></td><td><button value=\"" + !Config.EFFECT_CANCELING + "\" action=\"bypass -h admin_set CancelLesserEffect " + !Config.EFFECT_CANCELING + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">允許警衛攻擊怪物</font> = " + Config.ALLOW_GUARDS + "</td><td></td><td><button value=\"" + !Config.ALLOW_GUARDS + "\" action=\"bypass -h admin_set AllowGuards " + !Config.ALLOW_GUARDS + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">允許藍怪掉落物品</font> = " + Config.DEEPBLUE_DROP_RULES + "</td><td></td><td><button value=\"" + !Config.DEEPBLUE_DROP_RULES + "\" action=\"bypass -h admin_set UseDeepBlueDropRules " + !Config.DEEPBLUE_DROP_RULES + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">保留技能效果機制</font> = " + Config.STORE_SKILL_COOLTIME + "</td><td></td><td><button value=\"" + !Config.STORE_SKILL_COOLTIME + "\" action=\"bypass -h admin_set StoreSkillCooltime " + !Config.STORE_SKILL_COOLTIME + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">財富商人公告處理</font> = " + Config.ANNOUNCE_MAMMON_SPAWN + "</td><td></td><td><button value=\"" + !Config.ANNOUNCE_MAMMON_SPAWN + "\" action=\"bypass -h admin_set AnnounceMammonSpawn " + !Config.ANNOUNCE_MAMMON_SPAWN + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("</table></center></body></html>");
            adminReply.setHtml(replyMSG.toString());
            activeChar.sendPacket(adminReply);
        }
        
        /**
         * 特殊設定處理頁 - 來自rates.properties檔案資料
         * @param activeChar
         */
        public void showRateConfigPage(L2PcInstance activeChar)
        {
            NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
            
            TextBuilder replyMSG = new TextBuilder("<html><body>");
            
            replyMSG.append("<center><table width=270><tr><td width=60><button value=\"首頁\" action=\"bypass -h admin_admin\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td width=150><center><font color=\"LEVEL\">特殊設定處理</font></center></td><td width=60><button value=\"返回\" action=\"bypass -h admin_config_option\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></center><br>");
            replyMSG.append("<center><table width=260><tr>");
            replyMSG.append("<td width=60>");
            replyMSG.append("<button value=\"選擇設定\" action=\"bypass -h admin_config_option\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td><td width=60>");
            replyMSG.append("<button value=\"替代設定\" action=\"bypass -h admin_config_altsetting\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td><td width=60>");
            replyMSG.append("<button value=\"其他設定\" action=\"bypass -h admin_config_other\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td><td width=60>");
            replyMSG.append("<button value=\"倍率設定\" action=\"bypass -h admin_config_rate\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td></tr></table></center>");
            
            replyMSG.append("<br><center><table width=260>");
            /*----倍率設定----*/
            replyMSG.append("<tr><td><font color=\"LEVEL\">經驗倍率</font> = " + Config.RATE_XP + "</td><td><edit var=\"menu_command1\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set RateXP $menu_command1\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">技能倍率</font> = " + Config.RATE_SP + "</td><td><edit var=\"menu_command2\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set RateSP $menu_command2\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">組隊經驗倍率</font> = " + Config.RATE_PARTY_XP + "</td><td><edit var=\"menu_command3\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set RatePartyXp $menu_command3\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">組隊技能倍率</font> = " + Config.RATE_PARTY_SP + "</td><td><edit var=\"menu_command4\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set RatePartySp $menu_command4\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">金幣掉落倍率</font> = " + Config.RATE_DROP_ADENA + "</td><td><edit var=\"menu_command5\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set RateDropAdena $menu_command5\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">物品掉落倍率</font> = " + Config.RATE_DROP_ITEMS + "</td><td><edit var=\"menu_command6\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set RateDropItems $menu_command6\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">回收材料倍率</font> = " + Config.RATE_DROP_SPOIL + "</td><td><edit var=\"menu_command7\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set RateDropSpoil $menu_command7\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">任務獎勵倍率</font> = " + Config.RATE_QUESTS_REWARD + "</td><td><edit var=\"menu_command8\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set RateQuestsReward $menu_command8\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">耗品花費倍率</font> = " + Config.RATE_CONSUMABLE_COST + "</td><td><edit var=\"menu_command9\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set RateConsumableCost $menu_command9\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">性向經驗損失倍率</font> = " + Config.RATE_KARMA_EXP_LOST + "</td><td><edit var=\"menu_command10\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set RateKarmaExpLost $menu_command10\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td><font color=\"LEVEL\">警衛價格倍率</font> = " + Config.RATE_SIEGE_GUARDS_PRICE + "</td><td><edit var=\"menu_command11\" width=40 height=15></td><td><button value=\"設定\" action=\"bypass -h admin_set RateSiegeGuardsPrice $menu_command11\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("</table></center></body></html>");
            adminReply.setHtml(replyMSG.toString());
            activeChar.sendPacket(adminReply);
        }

}
