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
 * [URL]http://www.gnu.org/copyleft/gpl.html[/URL]
 */
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.AutoSpawnHandler;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.AutoSpawnHandler.AutoSpawnInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * Admin Command Handler for Mammon NPCs
 * 
 * @author Tempy
 */
public class AdminMammon implements IAdminCommandHandler {

    private static String[] _adminCommands = {"admin_mammon_find", "admin_mammon_respawn", "admin_list_spawns", "admin_msg"};
    private static final int REQUIRED_LEVEL = Config.GM_MENU;
    
    private boolean _isSealValidation = SevenSigns.getInstance().isSealValidationPeriod();
    
    public boolean useAdminCommand(String command, L2PcInstance activeChar) 
    {
        if (!Config.ALT_PRIVILEGES_ADMIN)
        {
            if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) 
                return false;
        }
        
        int npcId = 0;
        int teleportIndex = -1;
        AutoSpawnInstance blackSpawnInst = AutoSpawnHandler.getInstance().getAutoSpawnInstance(SevenSigns.MAMMON_BLACKSMITH_ID, false);
        AutoSpawnInstance merchSpawnInst = AutoSpawnHandler.getInstance().getAutoSpawnInstance(SevenSigns.MAMMON_MERCHANT_ID, false);
        
        if (command.startsWith("admin_mammon_find"))
        {
            try {
                if (command.length() > 17)
                    teleportIndex = Integer.parseInt(command.substring(18));
            }
            catch (Exception NumberFormatException) {
                activeChar.sendMessage("格式為 //mammon_find <teleportIndex> (where 1 = Blacksmith, 2 = Merchant)");
            }

            if (!_isSealValidation) {
                activeChar.sendMessage("目前為競爭時期.");
                return true;
            }
            
            L2NpcInstance[] blackInst = blackSpawnInst.getNPCInstanceList();
            L2NpcInstance[] merchInst = merchSpawnInst.getNPCInstanceList();
            
            if (blackInst.length > 0) {
                activeChar.sendMessage("財富的鐵匠: " + blackInst[0].getX() + " " + blackInst[0].getY() + " " + blackInst[0].getZ());
                
                if (teleportIndex == 0)

                    activeChar.teleToLocation(blackInst[0].getX(), blackInst[0].getY(),
                                              blackInst[0].getZ(), true);

            }
            
            if (merchInst.length > 0) {
                activeChar.sendMessage("財富的商人: " + merchInst[0].getX() + " " + merchInst[0].getY() + " " + merchInst[0].getZ());
                
                if (teleportIndex == 1)

                    activeChar.teleToLocation(merchInst[0].getX(), merchInst[0].getY(),
                                              merchInst[0].getZ(), true);

            }
        }
        
        else if (command.startsWith("admin_mammon_respawn"))
        {
            if (!_isSealValidation) {
                activeChar.sendMessage("目前為競爭時期.");
                return true;
            }
                
            long blackRespawn = AutoSpawnHandler.getInstance().getTimeToNextSpawn(blackSpawnInst);
            long merchRespawn = AutoSpawnHandler.getInstance().getTimeToNextSpawn(merchSpawnInst);

            activeChar.sendMessage("財富的鐵匠將在 " + (merchRespawn / 1000 / 60) + " 分鐘內出現.");
            activeChar.sendMessage("財富的商人將在 " + (blackRespawn / 1000 / 60) + " 分鐘內出現.");

        }
        
        else if (command.startsWith("admin_list_spawns"))
        {
            try { // admin_list_spawns x[xxxx] x[xx]
                String[] params = command.split(" ");
                
                npcId = Integer.parseInt(params[1]);
                
                if (params.length > 2)
                    teleportIndex = Integer.parseInt(params[2]);
            }
            catch (Exception e) {
                activeChar.sendPacket(SystemMessage.sendString("指令為 //list_spawns <NPC_ID> <TELE_INDEX>"));
            }

            findNPCInstances(activeChar, npcId, teleportIndex);
        }
        
        // Used for testing SystemMessage IDs   - Use //msg <ID>
        else if (command.startsWith("admin_msg"))
        {
            int msgId = -1;
            
            try {
                msgId = Integer.parseInt(command.substring(10).trim());
            }
            catch (Exception e) {
                activeChar.sendMessage("指令為 //msg <SYSTEM_MSG_ID>");
                return true;
            }
        
            activeChar.sendPacket(new SystemMessage(msgId));
        }

        return true;
    }

    public String[] getAdminCommandList()
    {
        return _adminCommands;
    }

    private boolean checkLevel(int level) 
    {
        return (level >= REQUIRED_LEVEL);
    }

    // FIXME: Need a good viable alternative than getAllVisibleObjects() but a SpawnListener
    // seems unusable for this method.
    public void findNPCInstances(L2PcInstance activeChar, int npcId, int teleportIndex)
    {
        int index = 0;
        
        for (L2Object obj : L2World.getInstance().getAllVisibleObjects()) 
        {
            if (obj instanceof L2NpcInstance) {
                L2NpcInstance npcInst = (L2NpcInstance)obj;
                int currNpcId = npcInst.getNpcId();
                
                if (currNpcId == npcId) 
                {
                    index++;
                    
                    if (teleportIndex > -1 && teleportIndex == index)
                        activeChar.teleToLocation(npcInst.getX(), npcInst.getY(), npcInst.getZ());
                    else
                        activeChar.sendMessage(index + " - " + npcInst.getName() + " (" + npcInst.getObjectId() + "): " + npcInst.getX() + " " + npcInst.getY() + " " + npcInst.getZ());
                }
            }
        }
        
        if (index == 0)
            activeChar.sendMessage("無法搜尋");
    }
}
