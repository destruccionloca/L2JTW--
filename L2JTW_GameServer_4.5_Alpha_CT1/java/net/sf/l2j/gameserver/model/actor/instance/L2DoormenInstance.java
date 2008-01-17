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
package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 *
 * @version $Revision$ $Date$
 */
public class L2DoormenInstance extends L2FolkInstance
{
    private ClanHall _clanHall;
    private static int COND_ALL_FALSE = 0;
    private static int COND_BUSY_BECAUSE_OF_SIEGE = 1;
    private static int COND_CASTLE_OWNER = 2;
    private static int COND_HALL_OWNER = 3;

    /**
     * @param template
     */
    public L2DoormenInstance(int objectID, L2NpcTemplate template)
    {
        super(objectID, template);
    }

    public final ClanHall getClanHall()
    {
        //_log.warning(this.getName()+" searching ch");
        if (_clanHall == null)
            _clanHall = ClanHallManager.getInstance().getNearbyClanHall(getX(), getY(), 500);
        //if (_ClanHall != null)
        //    _log.warning(this.getName()+" found ch "+_ClanHall.getName());
        return _clanHall;
    }

    @Override
    public void onBypassFeedback(L2PcInstance player, String command)
    {
        int condition = validateCondition(player);
        if (condition <= COND_ALL_FALSE) return;
        if (condition == COND_BUSY_BECAUSE_OF_SIEGE) return;
        else if (condition == COND_CASTLE_OWNER || condition == COND_HALL_OWNER)
        {
            if (command.startsWith("Chat"))
            {
                showMessageWindow(player);
                return;
            }
            else if (command.startsWith("open_doors"))
            {
                if (condition == COND_HALL_OWNER)
                {
                    getClanHall().openCloseDoors(true);
                    player.sendPacket(new NpcHtmlMessage(getObjectId(),
                    		"<html><body><font color=\"LEVEL\">已經開啟</font>根據地的門。<br>若開著門，除了血盟成員以外的角色也可進出，辦完事請務必關門。<br><center><br>" +
                    		"<button action=\"bypass -h npc_" + getObjectId()+ "_close_doors\" value=\"關閉\" width=74 height=21 back=\"L2UI_CH3.Btn1_normalOn\" fore=\"L2UI_CH3.Btn1_normal\"></center></body></html>"));
                }
                else
                {
                    //DoorTable doorTable = DoorTable.getInstance();
                    StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
                    st.nextToken(); // Bypass first value since its castleid/hallid

                    if (condition == 2)
                    {
                        while (st.hasMoreTokens())
                        {
                            getCastle().openDoor(player, Integer.parseInt(st.nextToken()));
                        }
                        return;
                    }

                }
            }
            else if (command.startsWith("close_doors"))
            {
                if (condition == COND_HALL_OWNER)
                {
                    getClanHall().openCloseDoors(false);
                    player.sendPacket(new NpcHtmlMessage(getObjectId(),
                    		"<html><body><font color=\"LEVEL\">已經關閉</font>根據地的門。<br>祝您有個愉快的一天！<br><center><br>" +
                    		"<button action=\"bypass -h npc_" + getObjectId()+ "_Chat\" value=\"首頁\" width=74 height=21 back=\"L2UI_CH3.Btn1_normalOn\" fore=\"L2UI_CH3.Btn1_normal\"></center></body></html>"));
                }
                else
                {
                    //DoorTable doorTable = DoorTable.getInstance();
                    StringTokenizer st = new StringTokenizer(command.substring(11), ", ");
                    st.nextToken(); // Bypass first value since its castleid/hallid

                    //L2Clan playersClan = player.getClan();

                    if (condition == 2)
                    {
                        while (st.hasMoreTokens())
                        {
                            getCastle().closeDoor(player, Integer.parseInt(st.nextToken()));
                        }
                        return;
                    }
                }
            }
        }

        super.onBypassFeedback(player, command);
    }

	/**
	* this is called when a player interacts with this NPC
	* @param player
	*/
	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player)) return;

		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);

			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				showMessageWindow(player);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(new ActionFailed());
	}

    public void showMessageWindow(L2PcInstance player)
    {
        player.sendPacket(new ActionFailed());
        String filename = "data/html/doormen/" + getTemplate().npcId + "-no.htm";

        int condition = validateCondition(player);
        if (condition == COND_BUSY_BECAUSE_OF_SIEGE) filename = "data/html/doormen/"
            + getTemplate().npcId + "-busy.htm"; // Busy because of siege
        else if (condition == COND_CASTLE_OWNER) // Clan owns castle
            filename = "data/html/doormen/" + getTemplate().npcId + ".htm"; // Owner message window

        // Prepare doormen for clan hall
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        String str;
        if (getClanHall() != null)
        {
            if (condition == COND_HALL_OWNER)
            {
                str = "<html><body>啊，您來了？<br><font color=\"55FFFF\">" + getName()+ "</font>很榮幸迎接到血盟成員。<br>有什麼事情需要我幫忙？<br><center>";
                str += "<br><button action=\"bypass -h npc_%objectId%_open_doors\" value=\"開門\" width=74 height=21 back=\"L2UI_CH3.Btn1_normalOn\" fore=\"L2UI_CH3.Btn1_normal\">";
                str += "<br><button action=\"bypass -h npc_%objectId%_close_doors\" value=\"關門\" width=74 height=21 back=\"L2UI_CH3.Btn1_normalOn\" fore=\"L2UI_CH3.Btn1_normal\"></center></body></html>";
            }
            else
            {
                L2Clan owner = ClanTable.getInstance().getClan(getClanHall().getOwnerId());
                if (owner != null && owner.getLeader() != null)
                {
                    str = "<html><body>您好！<br>這根據地的主人是<font color=\"55FFFF\">"
                        + owner.getLeader().getName() + "血盟之血盟主";
                    str += owner.getName() + "</font>陛下。<br>";
                    str += "很抱歉，非<font color=\"55FFFF\">"
                        + owner.getName() + "</font>血盟之血盟成員不得進入。</body></html>";
                }
                else str = "<html><body>" + getName() + "<br1>根據地<font color=\"LEVEL\">"
                    + getClanHall().getName()
                    + "</font>尚未有主人。<br>您可以找拍賣管理者投標它..</body></html>";
            }
            html.setHtml(str);
        }
        else html.setFile(filename);

        html.replace("%objectId%", String.valueOf(getObjectId()));
        player.sendPacket(html);
    }

    private int validateCondition(L2PcInstance player)
    {
        if (player.getClan() != null)
        {
            // Prepare doormen for clan hall
            if (getClanHall() != null)
            {
                if (player.getClanId() == getClanHall().getOwnerId()) return COND_HALL_OWNER;
                else return COND_ALL_FALSE;
            }
            if (getCastle() != null && getCastle().getCastleId() > 0)
            {
                //		        if (getCastle().getSiege().getIsInProgress())
                //		            return COND_BUSY_BECAUSE_OF_SIEGE;									// Busy because of siege
                //		        else
                if (getCastle().getOwnerId() == player.getClanId()) // Clan owns castle
                    return COND_CASTLE_OWNER; // Owner
            }
        }

        return COND_ALL_FALSE;
    }
}
