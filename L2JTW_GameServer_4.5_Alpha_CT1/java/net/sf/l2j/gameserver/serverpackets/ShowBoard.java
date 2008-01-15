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
package net.sf.l2j.gameserver.serverpackets;

import java.util.Iterator;
import java.util.List;

// Referenced classes of package net.sf.l2j.gameserver.serverpackets:
//            L2GameServerPacket

public class ShowBoard extends L2GameServerPacket
{

    private static final String _S__6E_SHOWBOARD = "[S] 7b ShowBoard";
    private String _htmlCode;
    private String _id;
    private List _arg;

    public ShowBoard(String htmlCode, String id)
    {
        _id = id;
        _htmlCode = htmlCode;
    }

    public ShowBoard(List arg)
    {
        _id = "1002";
        _htmlCode = null;
        _arg = arg;
    }

    private byte[] get1002()
    {
        int len = _id.getBytes().length * 2 + 2;
        for(Iterator i$ = _arg.iterator(); i$.hasNext();)
        {
            String arg = (String)i$.next();
            len += (arg.getBytes().length + 4) * 2;
        }

        byte data[] = new byte[len];
        int i = 0;
        for(int j = 0; j < _id.getBytes().length;)
        {
            data[i] = _id.getBytes()[j];
            data[i + 1] = 0;
            j++;
            i += 2;
        }

        data[i] = 8;
        i++;
        data[i] = 0;
        i++;
        for(Iterator i$ = _arg.iterator(); i$.hasNext();)
        {
            String arg = (String)i$.next();
            for(int j = 0; j < arg.getBytes().length;)
            {
                data[i] = arg.getBytes()[j];
                data[i + 1] = 0;
                j++;
                i += 2;
            }

            data[i] = 32;
            i++;
            data[i] = 0;
            i++;
            data[i] = 8;
            i++;
            data[i] = 0;
            i++;
        }

        return data;
    }

    protected final void writeImpl()
    {
        writeC(123);
        writeC(1);
        writeS("bypass _bbshome");
        writeS("bypass _bbsgetfav");
        writeS("bypass _bbsloc");
        writeS("bypass _bbsclan");
        writeS("bypass _bbsmemo");
        writeS("bypass _bbsmail");
        writeS("bypass _bbsfriends");
        writeS("bypass bbs_add_fav");
        if(!_id.equals("1002"))
        {
            byte htmlBytes[] = null;
            if(_htmlCode != null)
            {
                htmlBytes = _htmlCode.getBytes();
            }
            byte data[] = new byte[6 + _id.getBytes().length * 2 + 2 * (_htmlCode == null ? 0 : htmlBytes.length)];
            int i = 0;
            for(int j = 0; j < _id.getBytes().length;)
            {
                data[i] = _id.getBytes()[j];
                data[i + 1] = 0;
                j++;
                i += 2;
            }

            data[i] = 8;
            i++;
            data[i] = 0;
            i++;
            if(_htmlCode != null)
            {
                for(int j = 0; j < htmlBytes.length; j++)
                {
                    data[i] = htmlBytes[j];
                    data[i + 1] = 0;
                    i += 2;
                }

            }
            data[i] = 0;
            i++;
            data[i] = 0;
            writeS(_htmlCode);
            writeB(data);
        } else
        {
            writeB(get1002());
        }
    }

    public String getType()
    {
        return "[S] 7b ShowBoard";
    }
}
