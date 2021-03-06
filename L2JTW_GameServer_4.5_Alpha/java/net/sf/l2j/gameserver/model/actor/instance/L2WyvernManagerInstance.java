/* This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.Ride;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2WyvernManagerInstance extends L2CastleChamberlainInstance
{

    public L2WyvernManagerInstance (int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    @Override
	public void onBypassFeedback(L2PcInstance player, String command)
    {
        if (command.startsWith("RideWyvern"))
        {
        	if (!player.isClanLeader())
        	{
        		player.sendMessage("Only clan leaders are allowed.");
        		return;
        	}
        	if(player.getPet() == null)
        	{
        		if(player.isMounted())
        		{
        			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
                    sm.addString("SYS");
                    sm.addString("已經有使魔召喚.");

        			player.sendPacket(sm);
        			return;
        		}
        		else
        		{
        			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);

                    sm.addString("SYS");
                    sm.addString("請招喚座龍.");

        			player.sendPacket(sm);
        			return;
        		}
        	}
        	else if ((player.getPet().getNpcId()==12526) || (player.getPet().getNpcId()==12527) || (player.getPet().getNpcId()==12528))
            {
        		if (player.getInventory().getItemByItemId(1460) != null && player.getInventory().getItemByItemId(1460).getCount() >= 10)
        		{
        			if (player.getPet().getLevel() < 55)
        			{
        				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
                        sm.addString("SYS");
                        sm.addString("座龍尚未達到需求的等級");

                		player.sendPacket(sm);
                		return;
        			}
        			else
        			{
        				if(!player.disarmWeapons()) return;
        				player.getPet().unSummon(player);
        				player.getInventory().destroyItemByItemId("Wyvern", 1460, 10, player, player.getTarget());
        				Ride mount = new Ride(player.getObjectId(), Ride.ACTION_MOUNT, 12621);
        				player.sendPacket(mount);
        				player.broadcastPacket(mount);
        				player.setMountType(mount.getMountType());
        				player.addSkill(SkillTable.getInstance().getInfo(4289, 1));
        				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
                        sm.addString("SYS");
                        sm.addString("使魔召喚.");
                		player.sendPacket(sm);
                		return;
        			}
        		}
        		else
        		{
        			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
                    sm.addString("SYS");
                    sm.addString("需要10個B級-結晶");
            		player.sendPacket(sm);
            		return;
        		}
            }

        	else
        	{
        		SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
                sm.addString("SYS");
                sm.addString("使魔反召喚.");
        		player.sendPacket(sm);
        		return;
        	}
        }
		else
			super.onBypassFeedback(player, command);
    }

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
		player.sendPacket(new ActionFailed());
	}

    private void showMessageWindow(L2PcInstance player)
    {
        player.sendPacket( new ActionFailed() );
        String filename = "data/html/wyvernmanager/wyvernmanager-no.htm";

        int condition = validateCondition(player);
        if (condition > COND_ALL_FALSE)
        {
            if (condition == COND_OWNER)                                     // Clan owns castle
                filename = "data/html/wyvernmanager/wyvernmanager.htm";      // Owner message window
        }
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(filename);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%npcname%", getName());
        player.sendPacket(html);
    }
}
