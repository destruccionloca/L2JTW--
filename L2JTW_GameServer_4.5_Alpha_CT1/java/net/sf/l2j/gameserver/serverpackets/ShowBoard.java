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

import java.util.logging.Logger;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;


public class ShowBoard extends L2GameServerPacket
{
    private static final String _S__6E_SHOWBOARD = "[S] 6e ShowBoard";
    private L2PcInstance _player;
    private String _htmlCode;
    private Logger _log = Logger.getLogger(ShowBoard.class.getName());
    
    public ShowBoard(L2PcInstance player, String htmlCode)
    {
        _player = player;
        if(htmlCode.length() > 8192)
        {
            _log.warning("Html is too long! this will crash the client!");
            _htmlCode = "<html><body>Html was too long</body></html>";
            return;
        }
        _htmlCode = htmlCode; // html code must not exceed 8192 bytes 
    }

    protected void writeImpl()
    {
        writeC(0x6e);
        writeC(0x01); //c4 1 to show community 00 to hide 
        writeS("bypass bbs_top"); // top
        writeS("bypass bbs_favorite"); // favorite
        writeS("bypass bbs_region"); // region
        writeS(_player.getClan() != null ? "bypass bbs_clan" : "bypass bbs_default"); // clan
        writeS("bypass bbs_memo"); // memo
        writeS("bypass bbs_mail"); // mail
        writeS("bypass bbs_friends"); // friends
        writeS("bypass bbs_add_fav"); // add fav.
        writeS(_htmlCode); // current page
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
     */
    public String getType()
    {
        return _S__6E_SHOWBOARD;
    }


}
