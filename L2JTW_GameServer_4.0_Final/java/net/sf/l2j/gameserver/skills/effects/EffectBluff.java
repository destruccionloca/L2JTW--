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
package net.sf.l2j.gameserver.skills.effects;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * @author decad
 *
 * Implementation of the Bluff Effect
 */
final class EffectBluff extends L2Effect {

    public EffectBluff(Env env, EffectTemplate template)
    {
        super(env, template);
    }

    public EffectType getEffectType()
    {
        return EffectType.BLUFF; //test for bluff effect
    }
    
    /** Notify started */
    public void onStart()
    {
        getEffected().setTarget(null);
        int head = getEffector().getHeading();
        getEffected().setHeading(head);
        getEffected().setClientHeading(head);
        int head2 = getEffected().getHeadingTo( getEffector(),true);
        getEffected().setHeading(head2);
        getEffected().setClientHeading(head2);
        if (getEffected() instanceof L2PcInstance)
        {
        getEffected().setTarget(null);
        getEffected().startStunning();
        getEffected().sendPacket(SystemMessage.sendString("感受到捉弄的效果"));
        }
        else
        {
        getEffected().setTarget(null);
        getEffected().startStunning();
        }
        onActionTime();
    }
    public void onExit()
    {
        getEffected().stopStunning(this);
    }
    public boolean onActionTime()
    {
       
        return false;
    }
}

