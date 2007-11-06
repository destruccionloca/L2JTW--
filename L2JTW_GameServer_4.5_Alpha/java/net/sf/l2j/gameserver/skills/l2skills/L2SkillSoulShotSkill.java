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
package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.serverpackets.ExAutoSoulShot;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.gameserver.util.Broadcast;
import net.sf.l2j.gameserver.skills.effects.EffectCharge;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.4.4 $ $Date: 2005/03/27 15:30:07 $
 */

public class L2SkillSoulShotSkill extends L2Skill
{
   
    int num_soulshot;
    
    public L2SkillSoulShotSkill(StatsSet set) 
    {
        super(set);
        
    }

    public void useSkill(L2Character caster, @SuppressWarnings("unused") L2Object[] targets) {
        if (caster.isAlikeDead())
            return;
        
        // get the effect
        EffectCharge effect = (EffectCharge) caster.getFirstEffect(this);

        if (effect != null) {
            if (effect.numCharges < num_soulshot)
            {
                effect.numCharges++;
                caster.updateEffectIcons();
                SystemMessage sm = new SystemMessage(323);
                sm.addNumber(effect.numCharges);
                caster.sendPacket(sm);
            }
            else
            {
                SystemMessage sm = new SystemMessage(324);
                caster.sendPacket(sm);
            }
            return;
        }
        this.getEffects(caster, caster);
    }
}
