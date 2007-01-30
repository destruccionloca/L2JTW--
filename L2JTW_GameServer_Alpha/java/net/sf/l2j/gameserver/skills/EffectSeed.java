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
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

class EffectSeed extends L2Effect {
    
int num_seeds;
    
public EffectSeed(Env env, EffectTemplate template)
{
    super(env, template);
    num_seeds = 1;
    env._target.updateEffectIcons();
    SystemMessage sm = new SystemMessage(614);

    if (this.getSkill().getId() == 1285)
    {
        sm.addString("SYS");
        sm.addString("感受到火的種子效果");
    }
    else if (this.getSkill().getId() == 1286)
    {
        sm.addString("SYS");
        sm.addString("感受到水的種子效果");
    }
    else if (this.getSkill().getId() == 1287)
    {
        sm.addString("SYS");
        sm.addString("感受到風的種子效果");
    }
    else
    {
        sm.addString("SYS");
        sm.addString("感受到種子效果");
    }
    getEffected().sendPacket(sm);
}

public EffectType getEffectType()
{
    return EffectType.SEED;
}

public boolean onActionTime()
{
    // just stop this effect
    return false;
}
   
}
