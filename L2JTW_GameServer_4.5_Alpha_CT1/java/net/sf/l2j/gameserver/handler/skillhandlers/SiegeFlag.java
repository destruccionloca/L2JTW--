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
package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeFlagInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * @author _drunk_
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SiegeFlag implements ISkillHandler
{
    //private static Logger _log = Logger.getLogger(SiegeFlag.class.getName());
	private static final SkillType[] SKILL_IDS = {SkillType.SIEGEFLAG};

    public void useSkill(L2Character activeChar, @SuppressWarnings("unused") L2Skill skill, @SuppressWarnings("unused") L2Object[] targets)
    {
        if (activeChar == null || !(activeChar instanceof L2PcInstance)) return;

        L2PcInstance player = (L2PcInstance)activeChar;

        if (player.getClan() == null || player.getClan().getLeaderId() != player.getObjectId()) return;

        Castle castle = CastleManager.getInstance().getCastle(player);

        if (castle == null || !checkIfOkToPlaceFlag(player, castle, true)) return;

        try
        {
            // Spawn a new flag
            L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(35062));
            flag.setTitle(player.getClan().getName());
            flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
            flag.setHeading(player.getHeading());
            flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
            castle.getSiege().getFlag(player.getClan()).add(flag);
        }
        catch (Exception e)
        {
            player.sendMessage("放置錯誤 " + e);
        }
    }

    public SkillType[] getSkillIds()
    {
        return SKILL_IDS;
    }

    /**
     * Return true if character clan place a flag<BR><BR>
     *
     * @param activeChar The L2Character of the character placing the flag
     * @param isCheckOnly if false, it will send a notification to the player telling him
     * why it failed
     */
    public static boolean checkIfOkToPlaceFlag(L2Character activeChar, boolean isCheckOnly)
    {
        return checkIfOkToPlaceFlag(activeChar, CastleManager.getInstance().getCastle(activeChar), isCheckOnly);
    }

    public static boolean checkIfOkToPlaceFlag(L2Character activeChar, Castle castle, boolean isCheckOnly)
    {
        if (activeChar == null || !(activeChar instanceof L2PcInstance))
            return false;

        SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
        L2PcInstance player = (L2PcInstance)activeChar;

        if (castle == null || castle.getCastleId() <= 0)
        {
            sm.addString("必須在城堡周圍放置陣旗。");
        }
        else if (!castle.getSiege().getIsInProgress())
        {
            sm.addString("只能再攻城戰期間放置陣旗。");
        }
        else if (castle.getSiege().getAttackerClan(player.getClan()) == null)
        {
            sm.addString("必須是攻城方才能放置陣旗。");
        }
        else if (player.getClan() == null || !player.isClanLeader())
        {
            sm.addString("必須是血盟盟主。");
        }

        else if (castle.getSiege().getAttackerClan(player.getClan()).getNumFlags() >= SiegeManager.getInstance().getFlagMaxCount())
        	sm.addString("陣旗放置數量已達到極限。");

        else
            return true;

        if (!isCheckOnly) {player.sendPacket(sm);}
        return false;
    }
}
