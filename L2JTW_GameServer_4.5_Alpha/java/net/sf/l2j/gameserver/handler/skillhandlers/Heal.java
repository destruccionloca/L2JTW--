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
package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.model.L2Attackable;
import java.util.logging.Logger;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.2.2.4 $ $Date: 2005/04/06 16:13:48 $
 */

public class Heal implements ISkillHandler
{

	// all the items ids that this handler knowns
	//private static Logger _log = Logger.getLogger(Heal.class.getName());

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
	 */

	private static final SkillType[] SKILL_IDS = {SkillType.HEAL, SkillType.HEAL_PERCENT, SkillType.HEAL_STATIC, SkillType.BALANCE};


	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		
		
        double teamcurrenthp=0;
        double teammaxhp=0;
        double balancemax=0;
//		L2Character activeChar = activeChar;
		//check for other effects
	    try {
	        ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(SkillType.BUFF);

            if (handler != null)
                handler.useSkill(activeChar, skill, targets);

	    }
        catch (Exception e) {}
        
        
        L2Character target = null;
        L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
        if (skill.getSkillType() == SkillType.BALANCE)
        {
            for (int index = 0; index < targets.length; index++)
            {
               
                target = (L2Character)targets[index];
                teamcurrenthp += target.getCurrentHp();
                teammaxhp += target.getMaxHp();
                
            }
        }

		L2PcInstance player = null;
		if (activeChar instanceof L2PcInstance)
			player = (L2PcInstance)activeChar;

        for (int index = 0; index < targets.length; index++)
        {
            target = (L2Character)targets[index];
            // We should not heal if char is dead
            if (target == null || target.isDead())
                continue;

            // We should not heal walls and door
            if(target instanceof L2DoorInstance)
            	continue;

            // Player holding a cursed weapon can't be healed and can't heal
            if (target != activeChar)
            {
            	if (target instanceof L2PcInstance && ((L2PcInstance)target).isCursedWeaponEquiped())
            		continue;
            	else if (player != null && player.isCursedWeaponEquiped())
            		continue;
            }

            double hp = (skill.getSkillType() == SkillType.HEAL) ? target.calcStat(Stats.HEAL_HP_RATE, skill.getPower(), null, null) : skill.getPower();


            if (skill.getSkillType() == SkillType.HEAL_PERCENT)
            {
                hp = target.getMaxHp() * hp / 100.0;
            }
            else if (skill.getSkillType() == SkillType.HEAL)
            {
                
                 //Added effect of SpS and Bsps
                if (weaponInst != null)
                {
                    double oldhp = hp;
                    
                    if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
                    {
                        //hp *= 1.5;
                        hp = hp*1.5 + 0.244*activeChar.getMAtk(target, skill);
                        
                    }
                    else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
                    {
                        //hp *= 1.3;
                        hp = hp*1.3 + 0.116*activeChar.getMAtk(target, skill);
                        
                    }
                    else
                    {
                        hp+=(0.02 * activeChar.getMAtk(target, skill));
                    }
                    
                   //_log.info("skillpower="+ activeChar.getMAtk(target, skill)+" MAtkHPadd="+(hp - oldhp));
                }

                // If there is no weapon equipped, check for an active summon.
                else if (activeChar instanceof L2Summon)
                {

                    L2Summon activeSummon = (L2Summon)activeChar;

                    if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
                    {
                        //hp *= 1.5;
                        hp = (hp*1.5) + 0.244*activeChar.getMAtk(target, skill);
                        activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
                    }
                    else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
                    {
                        //hp *= 1.3;
                        hp = (hp*1.3) + 0.116*activeChar.getMAtk(target, skill);
                        activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
                    }
                    else
                    {
                        hp+=(0.02 * activeChar.getMAtk(target, skill));
                    }
                }
            }

            if (skill.getSkillType() != SkillType.BALANCE)
            {


			//int cLev = activeChar.getLevel();
			//hp += skill.getPower()/*+(Math.sqrt(cLev)*cLev)+cLev*/;
            if (skill.getSkillType() == SkillType.HEAL_STATIC)
            	hp = skill.getPower();
            else if (skill.getSkillType() != SkillType.HEAL_PERCENT)

				hp *= target.calcStat(Stats.HEAL_HP_RATE, 100, null, null) / 100;		


			target.setCurrentHp(hp + target.getCurrentHp());
			target.setLastHealAmount((int)hp);
			StatusUpdate su = new StatusUpdate(target.getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int)target.getCurrentHp());
			target.sendPacket(su);

            if (target instanceof L2PcInstance)
            {

                if (skill.getId() == 4051)
                {
                    SystemMessage sm = new SystemMessage(SystemMessageId.REJUVENATING_HP);
                    target.sendPacket(sm);
                }
                else
                {
                    if (activeChar instanceof L2PcInstance && activeChar != target)
                    {
                        SystemMessage sm = new SystemMessage(SystemMessageId.S2_HP_RESTORED_BY_S1);
                        sm.addString(activeChar.getName());
                        sm.addNumber((int)hp);
                        target.sendPacket(sm);
                    }
                    else
                    {
                        SystemMessage sm = new SystemMessage(SystemMessageId.S1_HP_RESTORED);
                        sm.addNumber((int)hp);
                        target.sendPacket(sm);
                    }
                }


            }
            }
            else if (skill.getSkillType() == SkillType.BALANCE)
            {
                balancemax = target.getMaxHp();
                target.setCurrentHp((int)(teamcurrenthp/teammaxhp*balancemax)); 
                StatusUpdate suhp = new StatusUpdate(target.getObjectId()); 
                suhp.addAttribute(StatusUpdate.CUR_HP, (int)target.getCurrentHp()); 
                target.sendPacket(suhp); 
            }
                
        }
        if (weaponInst != null)
        {
            if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT
                    ||weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
            {
                weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
            }
        }
        
    }
    
    
    public SkillType[] getSkillIds()
    {
        return SKILL_IDS;
    }


}

