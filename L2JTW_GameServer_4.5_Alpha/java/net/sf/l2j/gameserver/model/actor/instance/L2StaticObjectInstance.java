/*
 *
 * 
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
package net.sf.l2j.gameserver.model.actor.instance;


import java.util.logging.Logger;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.model.actor.knownlist.NullKnownList;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.ShowMiniMap;
import net.sf.l2j.gameserver.serverpackets.ShowTownMap;



/**
 * GODSON ROX!
 */
public class L2StaticObjectInstance extends L2Object
{
	private static Logger _log = Logger.getLogger(L2StaticObjectInstance.class.getName());

    /** The interaction distance of the L2StaticObjectInstance */
    public static final int INTERACTION_DISTANCE = 150;

    private int _staticObjectId;
    private int _type = -1;         // 0 - map signs, 1 - throne , 2 - arena signs
    private int _x;
    private int _y;
    private String _texture;

    /**
     * @return Returns the StaticObjectId.
     */
    public int getStaticObjectId()
    {
        return _staticObjectId;
    }
    /**
     * @param doorId The doorId to set.
     */
    public void setStaticObjectId(int StaticObjectId)
    {
        _staticObjectId = StaticObjectId;
    }
    /**
     */
    public L2StaticObjectInstance(int objectId)
    {
        super(objectId);
        setKnownList(new NullKnownList(this));
    }

    public int getType()
    {
        return _type;
    }

    public void setType(int type)
    {
        _type = type;
    }

    public void setMap(String texture, int x, int y)
    {
        _texture = "town_map."+texture;
        _x = x;
        _y = y;
    }

    private int getMapX()
    {
	return _x;
    }

    private int getMapY()
    {
	return _y;
    }

	public final double getDistance(int x, int y)
	{
		long dx = x-getX();
		long dy = y-getY();
		double distance = Math.sqrt(dx*dx + dy*dy);
		
		return distance;
	}
    
    /**
     * this is called when a player interacts with this NPC
     * @param player
     */
    @Override
	public void onAction(L2PcInstance player)
    {

	if(_type < 0) _log.info("L2StaticObjectInstance: StaticObject with invalid type! StaticObjectId: "+getStaticObjectId());
        // Check if the L2PcInstance already target the L2NpcInstance
        if (this != player.getTarget())
        {
            // Set the target of the L2PcInstance player
            player.setTarget(this);
            player.sendPacket(new MyTargetSelected(getObjectId(), 0));

        } else {

            MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
            player.sendPacket(my);

            // Calculate the distance between the L2PcInstance and the L2NpcInstance
            if (!player.isInsideRadius(this, INTERACTION_DISTANCE, false, false))
            {
                // Notify the L2PcInstance AI with AI_INTENTION_INTERACT
                player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);

                // Send a Server->Client packet ActionFailed (target is out of interaction range) to the L2PcInstance player
                player.sendPacket(new ActionFailed());
            } else {
			if(_type == 2) {
				String filename = "data/html/signboard.htm";
				String content = HtmCache.getInstance().getHtm(filename);
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());

				if (content == null) html.setHtml("<html><body>Signboard is missing:<br>"+filename+"</body></html>");
				else html.setHtml(content);

				player.sendPacket(html);
				player.sendPacket(new ActionFailed());
			} else if(_type == 0) player.sendPacket(new ShowTownMap(_texture, getMapX(), getMapY()));
                    // Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
                    player.sendPacket(new ActionFailed());
            }
        }

        
    	if (getDistance(player.getX(), player.getY()) > (double)INTERACTION_DISTANCE) 
    		player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
    	else
    	{
	        if(_type < 0)
	            _log.info("L2StaticObjectInstance: StaticObject with invalid type! StaticObjectId: "+getStaticObjectId());
	
	        if(_type == 0){
	        	player.sendPacket(new ShowMiniMap(1863));
	        }
	            
	        if(_type == 2)
	        {
	            String filename = "data/html/signboard/pvp.htm";
	            String content = HtmCache.getInstance().getHtm(filename);
	            if(content == null)
	            {
	                NpcHtmlMessage html = new NpcHtmlMessage(1);
	                html.setHtml((new StringBuilder()).append("<html><head><body>決鬥場看板遺失<br>請找回:<br>").append(filename).append("</body></html>").toString());
	                player.sendPacket(html);
	                player.sendPacket(new ActionFailed());
	            } else
	            {
	                NpcHtmlMessage itemReply = new NpcHtmlMessage(5);
	                itemReply.setHtml(content);
	                player.sendPacket(itemReply);
	            }
	            player.sendPacket(new ActionFailed());
	        }
    	}
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.model.L2Object#isAttackable()
     */
    @Override
    @SuppressWarnings("unused")
    public boolean isAutoAttackable(L2Character attacker)
    {
        return false;
    }
}
