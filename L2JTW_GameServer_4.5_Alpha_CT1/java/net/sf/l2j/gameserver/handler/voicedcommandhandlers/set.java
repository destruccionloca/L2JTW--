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
package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 *
 *
 */
public class set implements IVoicedCommandHandler
{
    private static final String[] VOICED_COMMANDS = { "set name", "set home", "set group" };

    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
    {
    	if(command.startsWith("set privileges")){
    		int n = Integer.parseInt(command.substring(15));
    		L2PcInstance pc = (L2PcInstance) activeChar.getTarget();
    		if(pc!=null){
    			if(activeChar.getClan().getClanId()==pc.getClan().getClanId()&&(activeChar.getClanPrivileges()>n)||activeChar.isClanLeader()){
    				pc.setClanPrivileges(n);
    	            activeChar.sendMessage("血盟權限設置為 " + n + " | 修改者 " + activeChar.getName());
    			}

    		}

     	}

    	return true;
    }


    public String[] getVoicedCommandList()
    {
        return VOICED_COMMANDS;
    }
}
