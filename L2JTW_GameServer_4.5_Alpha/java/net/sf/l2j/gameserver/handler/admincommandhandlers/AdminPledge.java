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

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * <B>Pledge Manipulation:</B><BR>
 * <LI>With target in a character without clan:<BR>
 * //pledge create clanname
 * <LI>With target in a clan leader:<BR>
 * //pledge dismiss<BR>
 * //pledge setlevel level<BR>
 * //pledge rep reputation_points<BR>
 */
public class AdminPledge implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {"admin_pledge"};

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
			if (!activeChar.isGM() || activeChar.getAccessLevel() < Config.GM_ACCESSLEVEL || activeChar.getTarget() == null || !(activeChar.getTarget() instanceof L2PcInstance))
				return false;

		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
			player = (L2PcInstance)target;
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			showMainPage(activeChar);
			return false;
		}
		String name = player.getName();
		if(command.startsWith("admin_pledge"))
		{
			String action = null;
			String parameter = null;
			GMAudit.auditGMAction(activeChar.getName(), command, activeChar.getName(), "");
			StringTokenizer st = new StringTokenizer(command);
			try
			{
				st.nextToken();
				action = st.nextToken(); // create|dismiss|setlevel|rep
				parameter = st.nextToken(); // clanname|nothing|level|rep_points
			}
			catch (NoSuchElementException nse)
			{
				activeChar.sendMessage("使用方法: //pledge <create|dismiss|setlevel|rep> [name|level|points]");	
			}
			if (action.equals("create"))
			{
				long cet=player.getClanCreateExpiryTime();
				player.setClanCreateExpiryTime(0);
				L2Clan clan = ClanTable.getInstance().createClan(player, parameter);
				if (clan != null)
					activeChar.sendMessage("血盟 " + parameter + " 創立. 盟主: " + player.getName());
				else
				{
					player.setClanCreateExpiryTime(cet);
					activeChar.sendMessage("創造血盟出錯.");
				}
			}
			else if (!player.isClanLeader())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER).addString(name));
				showMainPage(activeChar);
				return false;
			}
			else if(action.equals("dismiss"))
			{
				ClanTable.getInstance().destroyClan(player.getClanId());
				L2Clan clan = player.getClan();
				if (clan==null)
					activeChar.sendMessage("血盟解散.");
				else
					activeChar.sendMessage("血盟解散出問題.");
			}
			else if (parameter == null)
				activeChar.sendMessage("Usage: //pledge <setlevel|rep> <number>");
			else if(action.equals("setlevel"))
			{
				int level = Integer.parseInt(parameter);
				if (level>=0 && level <9)
				{
					player.getClan().changeLevel(level);
					activeChar.sendMessage("血盟等級設為 " + level + ". 血盟 " + player.getClan().getName());
				}
				else
					activeChar.sendMessage("等級錯誤");
			}
			else if (action.startsWith("rep"))
			{
				int points = Integer.parseInt(parameter);
				L2Clan clan = player.getClan();
				if (clan.getLevel() < 5)
				{
					activeChar.sendMessage("只有等級5以上可以取得.");
					showMainPage(activeChar);
					return false;
				}

				clan.setReputationScore(clan.getReputationScore()+points, true);
				activeChar.sendMessage(""+(points>0?"增加 ":"移除 ")+Math.abs(points)+" 分數至 "+clan.getName()+"");

			}
		}
		showMainPage(activeChar);
		return true;
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void showMainPage(L2PcInstance activeChar)
	{
		AdminHelpPage.showHelpPage(activeChar, "game_menu.htm");
	}

}
