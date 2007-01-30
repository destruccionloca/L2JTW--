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
package net.sf.l2j.gameserver.skills;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;


/**
 * @author decad
 *
 * Implementation of the Cancel_taget_disarm Effect
 */
final class EffectCancTargetDisarm extends L2Effect {

    public EffectCancTargetDisarm(Env env, EffectTemplate template)
    {
        super(env, template);
    }

    public EffectType getEffectType()
    {
        return EffectType.CANCEL_TARGET_DISARM;
    }
    
    /** Notify started */
    public void onStart() {
        L2ItemInstance weaponInst = getEffected().getActiveWeaponInstance();
        //TODO:unequip the weapon and update inv.
        if (getEffected() instanceof L2PcInstance)
        getEffected().setTarget(null);
        getEffected().breakAttack();
        getEffected().breakCast();
        onActionTime();
    }
    
    public boolean onActionTime()
    {
       return false;
    }
}

