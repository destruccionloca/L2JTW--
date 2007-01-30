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
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;


public class EffectSoulShotSkill extends L2Effect
{
    public int num_soulshot;
    public EffectSoulShotSkill(Env env, EffectTemplate template)
    {
        super(env, template);
 
        num_soulshot = 1;
 
    }

    public EffectType getEffectType()
    {
        return EffectType.SOULSHOT;
    }

    public boolean onActionTime()
    {
        // ignore
        return true;
    }

    public int getLevel() { return num_soulshot; }
}