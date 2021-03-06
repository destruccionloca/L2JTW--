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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.communitybbs.Manager.RegionBBSManager;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class handles following admin commands:
 * - ban account_name = changes account access level to -100 and logs him off. If no account is specified, target's account is used.
 * - unban account_name = changes account access level to 0.
 * - jail charname [penalty_time] = jails character. Time specified in minutes. For ever if no time is specified.
 * - unjail charname = Unjails player, teleport him to Floran.
 *
 * @version $Revision: 1.1.6.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminBan implements IAdminCommandHandler {
	private static final String[] ADMIN_COMMANDS = {"admin_ban", "admin_unban","admin_jail","admin_unjail"};

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		String account_name = "";
		String player = "";
		L2PcInstance plyr = null;
		if (command.startsWith("admin_ban"))
		{
			try
			{
				player = st.nextToken();
				plyr = L2World.getInstance().getPlayer(player);
			}
			catch(Exception e)
			{
				L2Object target = activeChar.getTarget();
				if (target instanceof L2PcInstance)
					plyr = (L2PcInstance)target;
				else
					activeChar.sendMessage("使用方法︰//ban [帳號名稱] (如果是空的將會鎖定目標角色)");
			}
			if (plyr != null && plyr.equals(activeChar))
				plyr.sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_ON_YOURSELF));
			else if (plyr==null)
			{
				account_name=player;
				LoginServerThread.getInstance().sendAccessLevel(account_name, 0);
				activeChar.sendMessage("錯誤資料 "+account_name+"。請查詢 //ban_menu");
			}
			else
			{
				plyr.setAccountAccesslevel(-100);
				account_name = plyr.getAccountName();
				RegionBBSManager.getInstance().changeCommunityBoard();
				plyr.logout();
				activeChar.sendMessage("帳號 "+account_name+" 封鎖。");
			}
		}
		else if (command.startsWith("admin_unban"))
		{
			try
			{
				account_name = st.nextToken();
				LoginServerThread.getInstance().sendAccessLevel(account_name, 0);
				activeChar.sendMessage("解除封鎖 "+account_name+"。請查詢 //unban_menu");
			}
			catch(Exception e)
			{
				activeChar.sendMessage("使用方法︰//unban <帳號名稱>");
				if (Config.DEBUG)
					e.printStackTrace();
			}
		}
		else if (command.startsWith("admin_jail"))
		{
			try
			{
				player = st.nextToken();
				int delay = 0;
				try
				{
					delay = Integer.parseInt(st.nextToken());
				}
				catch (NumberFormatException nfe)
				{
					activeChar.sendMessage("使用方法︰//jail <角色名稱> [懲罰時間]");
				}
				catch (NoSuchElementException nsee) {}
				L2PcInstance playerObj = L2World.getInstance().getPlayer(player);
				if (playerObj != null)
				{
					playerObj.setInJail(true, delay);
					activeChar.sendMessage("角色 "+player+" 被鎖定 "+(delay>0 ? delay+" 分鐘。" : "永遠！"));
				}
				else
					jailOfflinePlayer(activeChar, player, delay);
			}
			catch (NoSuchElementException nsee)
			{
				activeChar.sendMessage("使用方法︰//jail <角色名稱> [懲罰時間]");
			}
			catch(Exception e)
			{
				if (Config.DEBUG)
					e.printStackTrace();
			}
		}
		else if (command.startsWith("admin_unjail"))
		{
			try
			{
				player = st.nextToken();
				L2PcInstance playerObj = L2World.getInstance().getPlayer(player);

				if (playerObj != null)
				{
					playerObj.setInJail(false, 0);
					activeChar.sendMessage("角色 "+player+" 解除鎖定。");
				}
				else
					unjailOfflinePlayer(activeChar, player);
			}
			catch (NoSuchElementException nsee)
			{
				activeChar.sendMessage("請輸入角色名稱。");
			}
			catch(Exception e)
			{
				if (Config.DEBUG)
					e.printStackTrace();
			}
		}
		return true;
	}

	private void jailOfflinePlayer(L2PcInstance activeChar, String name, int delay)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, in_jail=?, jail_timer=? WHERE char_name=?");
			statement.setInt(1, -114356);
			statement.setInt(2, -249645);
			statement.setInt(3, -2984);
			statement.setInt(4, 1);
			statement.setLong(5, delay * 60000L);
			statement.setString(6, name);

			statement.execute();
			int count = statement.getUpdateCount();
			statement.close();

			if (count == 0)
				activeChar.sendMessage("角色無法查詢！");
			else
				activeChar.sendMessage("角色 "+name+" 鎖定為 "+(delay>0 ? delay+" 分鐘。" : "永遠！"));
		} catch (SQLException se)
		{
			activeChar.sendMessage("SQLException");
			if (Config.DEBUG) se.printStackTrace();
		} finally
		{
			try { con.close(); } catch (Exception e)
			{
				if (Config.DEBUG)
					e.printStackTrace();
			}
		}
	}

	private void unjailOfflinePlayer(L2PcInstance activeChar, String name)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, in_jail=?, jail_timer=? WHERE char_name=?");
			statement.setInt(1, 17836);
			statement.setInt(2, 170178);
			statement.setInt(3, -3507);
			statement.setInt(4, 0);
			statement.setLong(5, 0);
			statement.setString(6, name);
			statement.execute();
			int count = statement.getUpdateCount();
			statement.close();
			if (count == 0)
				activeChar.sendMessage("無法查詢！");
			else
				activeChar.sendMessage("角色 "+name+" 解除。");
		} catch (SQLException se)
		{
			activeChar.sendMessage("SQLException");
			if (Config.DEBUG) se.printStackTrace();
		} finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
				if (Config.DEBUG)
					e.printStackTrace();
			}
		}
	}

	public String[] getAdminCommandList() {
		return ADMIN_COMMANDS;
	}
}
