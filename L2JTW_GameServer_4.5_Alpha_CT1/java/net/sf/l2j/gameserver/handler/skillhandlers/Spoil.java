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

import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;

/**
 * @author _drunk_
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Spoil implements ISkillHandler
{
    //private static Logger _log = Logger.getLogger(Spoil.class.getName());
	private static final SkillType[] SKILL_IDS = {SkillType.SPOIL,SkillType.SPOILATK};

    public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
    {
        if (!(activeChar instanceof L2PcInstance))
			return;

        if (targets == null)
            return;

              for (int index = 0; index < targets.length; index++)
		{
                     if (!(targets[index] instanceof L2MonsterInstance))
				continue;

                     L2MonsterInstance target = (L2MonsterInstance) targets[index];

			if (target.isSpoil()) {
				activeChar.sendPacket(new SystemMessage(SystemMessageId.ALREADY_SPOILED));
				continue;
			}

			// SPOIL SYSTEM by Lbaldi
            boolean spoil = false;
            int damage;
			if ( target.isDead() == false ) 
			{
                spoil = Formulas.getInstance().calcMagicSuccess(activeChar, (L2Character)targets[index], skill);
                if (skill.getSkillType()== SkillType.SPOILATK)
                {
                Formulas f = Formulas.getInstance();
                L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
                boolean crit = false;
                boolean dual = activeChar.isUsingDualWeapon();
                boolean shld = f.calcShldUse(activeChar, target);
                boolean soul = (weapon != null && weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT);
                if (skill.getBaseCritRate() > 0)
                    crit = f.calcCrit(skill.getBaseCritRate() * 10 * f.getSTRBonus(activeChar));
                damage = (int) f.calcPhysDam(activeChar, target, skill, shld, false, dual, soul);
                if (crit) damage *= 2; // PDAM Critical damage always 2x and not affected by buffs
                activeChar.sendDamageMessage(target, damage, false, crit, false);
                target.reduceCurrentHp(damage, activeChar);
                }
				if (spoil)
				{
					target.setSpoil(true);
					target.setIsSpoiledBy(activeChar.getObjectId());
					activeChar.sendPacket(new SystemMessage(SystemMessageId.SPOIL_SUCCESS));
				}
				else 
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
					sm.addCharName(target);
					sm.addSkillName(skill);
					activeChar.sendPacket(sm);
				}
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
			}
		}
    } 
    
    public SkillType[] getSkillIds()
    { 
        return SKILL_IDS; 
    } 
}
