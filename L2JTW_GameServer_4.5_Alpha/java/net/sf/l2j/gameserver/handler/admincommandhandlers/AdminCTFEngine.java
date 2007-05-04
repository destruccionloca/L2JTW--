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

/**
 *
 * @author: FBIagent
 *
 */

package net.sf.l2j.gameserver.handler.admincommandhandlers;

import javolution.text.TextBuilder;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.CTF;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;

public class AdminCTFEngine implements IAdminCommandHandler {

 private static String[] _adminCommands = {"admin_ctf",
                                           "admin_ctf_name", "admin_ctf_desc", "admin_ctf_join_loc",
                                           "admin_ctf_npc", "admin_ctf_npc_pos",
                                           "admin_ctf_reward", "admin_ctf_reward_amount",
                                           "admin_ctf_team_add", "admin_ctf_team_remove", "admin_ctf_team_pos", "admin_ctf_team_color", "admin_ctf_team_flag",
                                           "admin_ctf_join", "admin_ctf_teleport", "admin_ctf_start", "admin_ctf_finish",
                                           "admin_ctf_sit",
                                           "admin_ctf_dump"};
 
 private static final int REQUIRED_LEVEL = 100;

    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
        
        if (command.equals("admin_ctf"))
            showMainPage(activeChar);
        else if (command.startsWith("admin_ctf_name "))
        {
            CTF._eventName = command.substring(15);
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_ctf_desc "))
        {
            CTF._eventDesc = command.substring(15);
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_ctf_join_loc "))
        {
            CTF._joiningLocationName = command.substring(19);
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_ctf_npc "))
        {
            CTF._npcId = Integer.valueOf(command.substring(14));
            showMainPage(activeChar);
        }
        else if (command.equals("admin_ctf_npc_pos"))
        {
            CTF.setNpcPos(activeChar);
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_ctf_reward "))
        {
            CTF._rewardId = Integer.valueOf(command.substring(17));
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_ctf_reward_amount "))
        {
            CTF._rewardAmount = Integer.valueOf(command.substring(24));
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_ctf_team_add "))
        {
            String teamName = command.substring(19);
            
            CTF.addTeam(teamName);
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_ctf_team_remove "))
        {
            String teamName = command.substring(22);

            CTF.removeTeam(teamName);
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_ctf_team_flag "))
        {
        	String[] params;

            params = command.split(" ");
            
            if (params.length != 3)
            {
            	activeChar.sendMessage("Wrong usge: //ctf_flag_id <npcId> <teamName>");
                return false;
            }

            CTF.setTeamFlag(params[2], Integer.valueOf(params[1]));
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_ctf_team_pos "))
        {
            String teamName = command.substring(19);

            CTF.setTeamPos(teamName, activeChar);
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_ctf_team_color "))
        {
            String[] params;

            params = command.split(" ");
            
            if (params.length != 3)
            {
            	activeChar.sendMessage("Wrong usge: //ctf_team_color <colorHex> <teamName>");
                return false;
            }

            CTF.setTeamColor(command.substring(params[0].length()+params[1].length()+2), Integer.decode("0x" + params[1]));
            showMainPage(activeChar);
        }
        else if(command.equals("admin_ctf_join"))
        {
            CTF.startJoin();
            showMainPage(activeChar);
        }
        else if (command.equals("admin_ctf_teleport"))
        {
            CTF.teleportStart();
            showMainPage(activeChar);
        }
        else if(command.equals("admin_ctf_start"))
        {
            CTF.startEvent();
            showMainPage(activeChar);
        }
        else if(command.equals("admin_ctf_finish"))
        {
            CTF.finishEvent(activeChar);
            showMainPage(activeChar);
        }
        else if (command.equals("admin_ctf_sit"))
        {
            CTF.sit();
            showMainPage(activeChar);
        }
        else if (command.equals("admin_ctf_dump"))
            CTF.dumpData();

        return true;
    }

    public String[] getAdminCommandList()
    {
        return _adminCommands;
    }

    private boolean checkLevel(int level) 
    {
        return (level >= REQUIRED_LEVEL);
    }

    public void showMainPage(L2PcInstance activeChar)
    {
        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        TextBuilder replyMSG = new TextBuilder("<html><body>");
        
        replyMSG.append("<center><font color=\"LEVEL\">[CTF 操作]</font></center><br><br><br>");
        replyMSG.append("<table><tr><td><edit var=\"input1\" width=\"125\"></td><td><edit var=\"input2\" width=\"125\"></td></tr></table>");
        replyMSG.append("<table border=\"0\"><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"名稱\" action=\"bypass -h admin_ctf_name $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"簡介\" action=\"bypass -h admin_ctf_desc $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"參加地點\" action=\"bypass -h admin_ctf_join_loc $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr></table><br><table><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"NPC編號\" action=\"bypass -h admin_ctf_npc $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"NPC位置\" action=\"bypass -h admin_ctf_npc_pos\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr></table><table><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"獎勵\" action=\"bypass -h admin_ctf_reward $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"獎勵數量\" action=\"bypass -h admin_ctf_reward_amount $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr></table><br><table><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"隊伍增加\" action=\"bypass -h admin_ctf_team_add $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"隊伍顏色\" action=\"bypass -h admin_ctf_team_color $input1 $input2\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"隊伍旗子\" action=\"bypass -h admin_ctf_team_flag $input1 $input2\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"隊伍地點\" action=\"bypass -h admin_ctf_team_pos $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"隊伍移除\" action=\"bypass -h admin_ctf_team_remove $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr></table><br><table><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"開始參加\" action=\"bypass -h admin_ctf_join\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"隊伍傳送\" action=\"bypass -h admin_ctf_teleport\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"活動開始\" action=\"bypass -h admin_ctf_start\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"結束活動\" action=\"bypass -h admin_ctf_finish\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr></table><br><table><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"強制坐下\" action=\"bypass -h admin_ctf_sit\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=\"100\"><button value=\"Log資料\" action=\"bypass -h admin_ctf_dump\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr></table><br><br>");
        replyMSG.append("目前狀態<br1>");
        replyMSG.append("    ... 名稱:&nbsp;<font color=\"00FF00\">" + CTF._eventName + "</font><br1>");
        replyMSG.append("    ... 簡介:&nbsp;<font color=\"00FF00\">" + CTF._eventDesc + "</font><br1>");
        replyMSG.append("    ... 參加地點:&nbsp;<font color=\"00FF00\">" + CTF._joiningLocationName + "</font><br1>");
        replyMSG.append("    ... 參加NPC:&nbsp;<font color=\"00FF00\">" + CTF._npcId + " 座標 " + CTF._npcX + "," + CTF._npcY + "," + CTF._npcZ + "</font><br1>");
        replyMSG.append("    ... 獎勵物品:&nbsp;<font color=\"00FF00\">" + CTF._rewardId + "</font><br1>");
        replyMSG.append("    ... 獎勵數量:&nbsp;<font color=\"00FF00\">" + CTF._rewardAmount + "</font><br><br>");
        replyMSG.append("目前隊伍:<br1>");
        replyMSG.append("<center><table border=\"0\">");
        
        for (String team : CTF._teams)
        {
            replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font>");

            if (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE"))
                replyMSG.append("&nbsp;(" + CTF.teamPlayersCount(team) + " joined)");
            else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
            {
                if (CTF._teleport || CTF._started)
                    replyMSG.append("&nbsp;(" + CTF.teamPlayersCount(team) + " in)");
            }

            replyMSG.append("</td></tr><tr><td>");
            replyMSG.append(CTF._teamColors.get(CTF._teams.indexOf(team)));
            replyMSG.append("</td></tr><tr><td>");
            replyMSG.append(CTF._flagsX.get(CTF._teams.indexOf(team)) + ", " + CTF._flagsY.get(CTF._teams.indexOf(team)) + ", " + CTF._flagsZ.get(CTF._teams.indexOf(team)));
            replyMSG.append("</td></tr><tr><td width=\"60\"><button value=\"移除\" action=\"bypass -h admin_ctf_team_remove " + team + "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
        }
        
        replyMSG.append("</table></center>");
        
        if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
        {
            if (!CTF._started)
            {
                replyMSG.append("<br1>");
                replyMSG.append(CTF._playersShuffle.size() + "個參賽者正在等候隨機入隊!");
                replyMSG.append("<br><br>");
            }
        }

        replyMSG.append("</body></html>");
        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply); 
    }
}