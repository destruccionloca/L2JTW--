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
package net.sf.l2j.gameserver.serverpackets;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.model.L2Object;


/**
 *
 * sample
 *
 * 0000: 8e  d8 a8 10 48  10 04 00 00  01 00 00 00  01 00 00    ....H...........
 * 0010: 00  d8 a8 10 48                                     ....H
 *
 *
 * format   ddddd d
 *
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class MagicSkillLaunched extends L2GameServerPacket
{
	private static final String _S__8E_MAGICSKILLLAUNCHED = "[S] 8E MagicSkillLaunched";
	private int _charObjId;
	private int _skillId;
	private int _skillLevel;
	private int _numberOfTargets;
	private L2Object[] _targets;
	private int _singleTargetId;
    private int _flags;
	

	public MagicSkillLaunched(L2Character cha, int skillId, int skillLevel, L2Object[] targets)
	{
		_charObjId = cha.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_numberOfTargets = targets.length;
		_targets = targets;
		_singleTargetId = 0;
         _flags |= 0x20;
	}

	public MagicSkillLaunched(L2Character cha, int skillId, int skillLevel)
	{
		_charObjId = cha.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_numberOfTargets = 1;
		_singleTargetId = cha.getTargetId();
        _flags |= 0x20;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x76);
		writeD(_charObjId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_numberOfTargets); // also failed or not?
		if (_singleTargetId != 0 || _numberOfTargets == 0)
			writeD(_singleTargetId);
		else for(L2Object target : _targets)
		{
			try
			{
				writeD(target.getObjectId());
			}
			catch (NullPointerException e)
			{
				writeD(0); // untested
			}
		}
        if (L2Skill.CRIT_ATTACK==1)
            writeC(_flags);

	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__8E_MAGICSKILLLAUNCHED;
	}

}
