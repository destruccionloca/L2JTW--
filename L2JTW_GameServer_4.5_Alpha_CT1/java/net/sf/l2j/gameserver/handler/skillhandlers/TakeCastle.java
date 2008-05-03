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

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2ArtefactInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.util.Util;

/**
 * @author _drunk_
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TakeCastle implements ISkillHandler
{
    //private static Logger _log = Logger.getLogger(TakeCastle.class.getName());
	private static final SkillType[] SKILL_IDS = {SkillType.TAKECASTLE};

    public void useSkill(L2Character activeChar, @SuppressWarnings("unused") L2Skill skill, @SuppressWarnings("unused") L2Object[] targets)
    {
        if (activeChar == null || !(activeChar instanceof L2PcInstance)) return;

        L2PcInstance player = (L2PcInstance)activeChar;

        if (player.getClan() == null || player.getClan().getLeaderId() != player.getObjectId()) return;

        Castle castle = CastleManager.getInstance().getCastle(player);
        if (castle == null || !checkIfOkToCastSealOfRule(player, castle, true)) return;

        try
        {
        	if(targets[0] instanceof L2ArtefactInstance)
        		castle.Engrave(player.getClan(), targets[0].getObjectId());
        }
        catch(Exception e)
        {}
    }

    public SkillType[] getSkillIds()
    {
        return SKILL_IDS;
    }

    /**
     * Return true if character clan place a flag<BR><BR>
     *
     * @param activeChar The L2Character of the character placing the flag
     *
     */
    public static boolean checkIfOkToCastSealOfRule(L2Character activeChar, boolean isCheckOnly)
    {
        return checkIfOkToCastSealOfRule(activeChar, CastleManager.getInstance().getCastle(activeChar), isCheckOnly);
    }

    public static boolean checkIfOkToCastSealOfRule(L2Character activeChar, Castle castle, boolean isCheckOnly)
    {
        if (activeChar == null || !(activeChar instanceof L2PcInstance))
            return false;

        String text = "";
        L2PcInstance player = (L2PcInstance)activeChar;

        if (castle == null || castle.getCastleId() <= 0)
            text = "必須在城堡地面使用此技能。";
        else if (player.getTarget() == null && !(player.getTarget() instanceof L2ArtefactInstance))
            text = "目標必須是守護者封印。";
        else if (!castle.getSiege().getIsInProgress())
            text = "此技能只能在攻城戰期間使用。";
        else if (!Util.checkIfInRange(200, player, player.getTarget(), true))
            text = "守護者封印距離太遠。";
        else if (castle.getSiege().getAttackerClan(player.getClan()) == null)
            text = "你必須是攻城方才能使用此技能。";
        else
        {
            if (!isCheckOnly) castle.getSiege().announceToPlayer("血盟 " + player.getClan().getName() + " 開始在刻上守護者封印。", true);                
            return true;
        }

        if (!isCheckOnly)
            player.sendMessage(text);
        return false;
    }
}
