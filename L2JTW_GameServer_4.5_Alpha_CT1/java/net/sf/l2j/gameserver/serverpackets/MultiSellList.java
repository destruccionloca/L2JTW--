/*
 * $Header: MultiSellList.java, 2/08/2005 14:21:01 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 2/08/2005 14:21:01 $
 * $Revision: 1 $
 * $Log: MultiSellList.java,v $
 * Revision 1  2/08/2005 14:21:01  luisantonioa
 * Added copyright notice
 *
 *
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
package net.sf.l2j.gameserver.serverpackets;

import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellEntry;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellIngredient;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellListContainer;


/**
 * This class ...
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public final class MultiSellList extends L2GameServerPacket
{
    private static final String _S__D0_MULTISELLLIST = "[S] d0 MultiSellList";

    protected int _listId, _page, _finished;
    protected MultiSellListContainer _list;

    public MultiSellList(MultiSellListContainer list, int page, int finished)
    {
    	_list = list;
    	_listId = list.getListId();
    	_page = page;
    	_finished = finished;
    }

    @Override
	protected void writeImpl()
    {
    	// [ddddd] [dchh] [hdhdh] [hhdh]

        writeC(0xd0);
        writeD(_listId);    // list id
        writeD(_page);		// page
        writeD(_finished);	// finished
        writeD(0x28);	// size of pages
        writeD(_list == null ? 0 : _list.getEntries().size()); //list length

        if(_list != null)
        {
            for(MultiSellEntry ent : _list.getEntries())
            {
            	writeD(ent.getEntryId());
            	writeC(1);
                writeH(0x00); // C6
                writeD(0x00); // C6
                writeD(-2); // T1
                writeD(0x00); // T1
                writeD(0x00); // T1
                writeD(0x00); // T1
                writeD(0x00); // T1
                writeD(0x00); // T1
                writeD(0x00); // T1
                writeD(0x00); // T1
                writeD(0x00); // T1
            	writeH(ent.getProducts().size());
            	writeH(ent.getIngredients().size());

            	for(MultiSellIngredient i: ent.getProducts())
            	{
	            	writeD(i.getItemId());
	            	writeD(0);
	            	writeH(ItemTable.getInstance().getTemplate(i.getItemId()).getType2());
	            	writeD(i.getItemCount());
	        	    writeH(i.getEnchantmentLevel()); //enchtant lvl
	            	writeD(0x00); // C6
	            	writeD(0x00); // C6
                    writeD(-2); // T1
                    writeD(0x00); // T1
                    writeD(0x00); // T1
                    writeD(0x00); // T1
                    writeD(0x00); // T1
                    writeD(0x00); // T1
                    writeD(0x00); // T1
                    writeD(0x00); // T1
            	}

                for(MultiSellIngredient i : ent.getIngredients())
                {
                	int items = i.getItemId();
                	int typeE = 65535;
                	if (items != -200)
                		typeE = ItemTable.getInstance().getTemplate(i.getItemId()).getType2();
                    writeD(items);      //ID
                    writeH(typeE);
                    writeD(i.getItemCount());	//Count
                    writeH(i.getEnchantmentLevel()); //Enchant Level
                	writeD(0x00); // C6
                	writeD(0x00); // C6
                    writeD(-2); // T1
                    writeD(0x00); // T1
                    writeD(0x00); // T1
                    writeD(0x00); // T1
                    writeD(0x00); // T1
                    writeD(0x00); // T1
                    writeD(0x00); // T1
                    writeD(0x00); // T1
                }
            }
        }
    }

    @Override
    public String getType()
    {
        return _S__D0_MULTISELLLIST;
    }

}
