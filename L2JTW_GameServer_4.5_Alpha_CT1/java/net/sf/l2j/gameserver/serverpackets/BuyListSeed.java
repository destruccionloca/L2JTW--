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
package net.sf.l2j.gameserver.serverpackets;

import java.util.Collection;

import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.model.L2TradeList.L2TradeItem;

/**
 * Format: c ddh[hdddhhd]
 * c - id (0xE8)
 *
 * d - money
 * d - manor id
 * h - size
 * [
 * h - item type 1
 * d - object id
 * d - item id
 * d - count
 * h - item type 2
 * h
 * d - price
 * ]
 *
 * @author l3x
 */

public final class BuyListSeed extends L2GameServerPacket
{
	private static final String _S__E8_BUYLISTSEED = "[S] e9 BuyListSeed";

	private int _manorId;
	private Collection<L2TradeItem> _list;
	private int _money;

	public BuyListSeed(L2TradeList list, int manorId, int currentMoney)
	{
		_money  = currentMoney;
		_manorId = manorId;
		_list   = list.getItems();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xe9);

		writeD(_money);                                 // current money
		writeD(_manorId);                               // manor id

		writeH(_list.size());                           // list length

		for (L2TradeItem item : _list)
		{
			writeH(0x04);                               // item->type1
			writeD(0x00);                               // objectId
			writeD(item.getItemId());                   // item id
			writeD(item.getCurrentCount());             // item count
			writeH(0x04);                               // item->type2
			writeH(0x00);                               // unknown :)
			writeD(item.getPrice());                    // price
		}
	}

	@Override
	public String getType()
	{
		return _S__E8_BUYLISTSEED;
	}
}