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
package net.sf.l2j.gameserver.clientpackets;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.serverpackets.ExRpItemLink;

/**
 *
 * @author  KenM
 */
public class RequestExRqItemLink extends L2GameClientPacket
{
    private int _objectId;
    /**
     * @see net.sf.l2j.gameserver.clientpackets.L2GameClientPacket#getType()
     */
    @Override
    public String getType()
    {
        return "[C] DO:1E RequestExRqItemLink";
    }

    /**
     * @see net.sf.l2j.gameserver.clientpackets.L2GameClientPacket#readImpl()
     */
    @Override
    protected void readImpl()
    {
        _objectId = readD();
    }

    /**
     * @see net.sf.l2j.gameserver.clientpackets.L2GameClientPacket#runImpl()
     */
    @Override
    protected void runImpl()
    {
        L2GameClient client = this.getClient();
        if (client != null)
        {
            L2Object object = L2World.getInstance().findObject(_objectId);
            if (object != null && object instanceof L2ItemInstance)
            {
                L2ItemInstance item = (L2ItemInstance)object;
                client.sendPacket(new ExRpItemLink(item));
            }
        }
    }
    
}