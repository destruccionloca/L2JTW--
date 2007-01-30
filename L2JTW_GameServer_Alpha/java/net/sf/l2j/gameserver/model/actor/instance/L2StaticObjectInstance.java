/*
 * $Header: /cvsroot/l2j/L2_Gameserver/java/net/sf/l2j/gameserver/model/L2StaticObjectInstance.java,v 1.3.2.2.2.2 2005/02/04 13:05:27 maximas Exp $
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
import net.sf.l2j.util.L2ObjectSet;


/**
 * GODSON ROX!
 */
public class L2StaticObjectInstance extends L2Object
{
	private static Logger _log = Logger.getLogger(L2StaticObjectInstance.class.getName());
    
    /** The interaction distance of the L2StaticObjectInstance */
    public static final int INTERACTION_DISTANCE = 150;

    private int _staticObjectId;
    private int _type = -1;         // 0 - signs, 1 - throne    
    
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
        setKnownList(new NullKnownList(new L2StaticObjectInstance[] {this}));
    }
    
    public int getType()
    {
        return _type;
    }
    
    public void setType(int type)
    {
        _type = type;
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
    public void onAction(L2PcInstance player)
    {
        player.setTarget(this);
        MyTargetSelected my = new MyTargetSelected(getObjectId(), 2);
        player.sendPacket(my);
        
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
