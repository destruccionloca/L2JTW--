/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.model.CursedWeapon;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class handles following admin commands:
 * - cw_info = displays cursed weapon status
 * - cw_remove = removes a cursed weapon from the world, item id or name must be provided
 * - cw_add = adds a cursed weapon into the world, item id or name must be provided. Target will be the weilder
 * - cw_goto = teleports GM to the specified cursed weapon
 * - cw_reload = reloads instance manager
 * @version $Revision: 1.1.6.3 $ $Date: 2007/07/31 10:06:06 $
 */
public class AdminCursedWeapons implements IAdminCommandHandler {
	private static final String[] ADMIN_COMMANDS = {"admin_cw_info", "admin_cw_remove", "admin_cw_goto", "admin_cw_reload", "admin_cw_add", "admin_cw_info_menu"};
	private static final int REQUIRED_LEVEL = Config.GM_MIN;
	private int itemId;

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
			if (!(checkLevel(activeChar.getAccessLevel())))
				return false;


		CursedWeaponsManager cwm = CursedWeaponsManager.getInstance();
		int id=0;


		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();

		if (command.startsWith("admin_cw_info"))
		{
			if (!command.contains("menu"))

			{
				activeChar.sendMessage("====== 受詛咒的武器資訊 ======");
				for (CursedWeapon cw : cwm.getCursedWeapons())
				{

					activeChar.sendMessage("> "+cw.getName()+" ("+cw.getItemId()+")");
					if (cw.isActivated())
					{
						L2PcInstance pl = cw.getPlayer();
		        		activeChar.sendMessage("擁有人物: "+pl.getName());
		        		activeChar.sendMessage("性向指數: "+cw.getPlayerKarma());
		        		activeChar.sendMessage("時間剩下: "+(cw.getTimeLeft()/60000)+" 分鐘。");
		        		activeChar.sendMessage("殺人數量: "+cw.getNbKills());
					}
					else if (cw.isDropped())
					{
						activeChar.sendMessage("掉落地面");
		        		activeChar.sendMessage("時間剩下: "+(cw.getTimeLeft()/60000)+" 分鍾。");
		        		activeChar.sendMessage("殺人數量: "+cw.getNbKills());
					}
					else
					{
						activeChar.sendMessage("並未出現。");
					}
					activeChar.sendPacket(new SystemMessage(SystemMessageId.FRIEND_LIST_FOOT));
				}
			}
			else
			{
				TextBuilder replyMSG = new TextBuilder();
				NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
				adminReply.setFile("data/html/admin/cwinfo.htm");
				for (CursedWeapon cw : cwm.getCursedWeapons())

				{

					itemId=cw.getItemId();
					replyMSG.append("<table width=270><tr><td>名稱:</td><td>"+cw.getName()+"</td></tr>");
					if (cw.isActivated())
					{
						L2PcInstance pl = cw.getPlayer();

						replyMSG.append("<tr><td>持有者:</td><td>"+ (pl==null?"null" : pl.getName())+"</td></tr>");
						replyMSG.append("<tr><td>性向:</td><td>"+String.valueOf(cw.getPlayerKarma())+"</td></tr>");
						replyMSG.append("<tr><td>殺人數量:</td><td>"+String.valueOf(cw.getPlayerPkKills())+"/"+String.valueOf(cw.getNbKills())+"</td></tr>");
						replyMSG.append("<tr><td>剩下時間:</td><td>"+String.valueOf(cw.getTimeLeft()/60000)+" 分.</td></tr>");
						replyMSG.append("<tr><td><button value=\"移除\" action=\"bypass -h admin_cw_remove "+String.valueOf(itemId)+"\" width=73 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
						replyMSG.append("<td><button value=\"繼續\" action=\"bypass -h admin_cw_goto "+String.valueOf(itemId)+"\" width=73 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
					}
					else if (cw.isDropped())
					{
						replyMSG.append("<tr><td>Position:</td><td>掉落在地上</td></tr>");
						replyMSG.append("<tr><td>剩下時間:</td><td>"+String.valueOf(cw.getTimeLeft()/60000)+" min.</td></tr>");
						replyMSG.append("<tr><td>殺人數量:</td><td>"+String.valueOf(cw.getNbKills())+"</td></tr>");
						replyMSG.append("<tr><td><button value=\"移除\" action=\"bypass -h admin_cw_remove "+String.valueOf(itemId)+"\" width=73 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
						replyMSG.append("<td><button value=\"繼續\" action=\"bypass -h admin_cw_goto "+String.valueOf(itemId)+"\" width=73 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
					}
					else
					{
						replyMSG.append("<tr><td>Position:</td><td>並不存在。</td></tr>");
						replyMSG.append("<tr><td><button value=\"交給對方\" action=\"bypass -h admin_cw_add "+String.valueOf(itemId)+"\" width=99 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td></td></tr>");
					}
					replyMSG.append("</table>");
					replyMSG.append("<br>");

				}
				adminReply.replace("%cwinfo%", replyMSG.toString());
				activeChar.sendPacket(adminReply);
			}
		}
		else if (command.startsWith("admin_cw_reload"))
		{
			cwm.reload();
		}
		else
		{
			CursedWeapon cw=null;
			try
			{
				String parameter = st.nextToken();
				if (parameter.matches("[0-9]*"))
					id = Integer.parseInt(parameter);
				else
				{
					parameter = parameter.replace('_', ' ');
					for (CursedWeapon cwp : cwm.getCursedWeapons())
					{
						if (cwp.getName().toLowerCase().contains(parameter.toLowerCase()))
						{
							id=cwp.getItemId();
							break;
						}
					}
				}
				cw = cwm.getCursedWeapon(id);
				if (cw==null)
				{
					activeChar.sendMessage("未知的受詛咒的武器ID。");
					return false;
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("使用方法: //cw_remove|//cw_goto|//cw_add <itemid|name>");
			}

			if (command.startsWith("admin_cw_remove "))
			{
				cw.endOfLife();
			}
			else if (command.startsWith("admin_cw_goto "))
			{
				cw.goTo(activeChar);
			}
			else if (command.startsWith("admin_cw_add"))
			{
				if (cw==null)
				{
					activeChar.sendMessage("使用方法: //cw_add <itemid|name>");
					return false;
				}
				else if (cw.isActive())
					activeChar.sendMessage("受詛咒的武器已正在使用。");
				else
				{
					L2Object target = activeChar.getTarget();
					if (target != null && target instanceof L2PcInstance)
						((L2PcInstance)target).addItem("AdminCursedWeaponAdd", id, 1, target, true);
					else
						activeChar.addItem("AdminCursedWeaponAdd", id, 1, activeChar, true);
				}
			}
			else
			{
				activeChar.sendMessage("未知的指令。");
			}
		}
		return true;
	}

	public String[] getAdminCommandList() {
		return ADMIN_COMMANDS;
	}

	private boolean checkLevel(int level) {
		return (level >= REQUIRED_LEVEL);
	}
}
