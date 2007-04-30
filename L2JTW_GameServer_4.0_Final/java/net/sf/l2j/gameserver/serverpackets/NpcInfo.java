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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
/**
 * This class ...
 * 
 * @version $Revision: 1.7.2.4.2.9 $ $Date: 2005/04/11 10:05:54 $
 */
public class NpcInfo extends ServerBasePacket
{
	//   ddddddddddddddddddffffdddcccccSSddd dddddc
	//   ddddddddddddddddddffffdddcccccSSddd dddddccffd
	     
	     
	private static final String _S__22_NPCINFO = "[S] 16 NpcInfo";
	private L2Character _cha;
	private int _x, _y, _z, _heading;
	private int _idTemplate;
	private boolean _isAttackable, _isSummoned;
	private int _mAtkSpd, _pAtkSpd;
	private int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd,_atkspdMul,_movespdMul;
	private int _rhand, _lhand,_lrhand,enchlvl, armor, head, boot,pant, glove, charrace,charhair,charface,charcolor,charclass,charhero,charsex;
    private int collisionHeight, collisionRadius;
    private String _name = "";
    private String _title = "";
    private boolean IsChar;

	/**
	 * @param _characters
	 */
	public NpcInfo(L2NpcInstance cha, L2Character attacker)
	{
		_cha = cha;
		_idTemplate = cha.getTemplate().idTemplate;
		_isAttackable = cha.isAutoAttackable(attacker);
		_rhand = cha.getTemplate().rhand;
		_lhand = cha.getTemplate().lhand;
		_isSummoned = false;
        collisionHeight = _cha.getTemplate().collisionHeight;
        collisionRadius = _cha.getTemplate().collisionRadius;
        if (_cha.getTemplate().basePAtkSpd>0 &&  _cha.getPAtkSpd()>0)
        _atkspdMul = _cha.getPAtkSpd()/_cha.getTemplate().basePAtkSpd;
        else _atkspdMul = 1;
        if (_cha.getTemplate().baseRunSpd>0 && _cha.getRunSpeed()>0)
        _movespdMul = _cha.getRunSpeed()/_cha.getTemplate().baseRunSpd;
        else _movespdMul = 1;
        if (_atkspdMul<1) _atkspdMul = 1;
        if (_movespdMul<1) _movespdMul = 1;
        if (cha.getTemplate().serverSideName)
        	_name = cha.getTemplate().name;

    	if (cha.getTemplate().serverSideTitle)
    		_title = cha.getTemplate().title;
    	else
    		_title = cha.getTitle();
    	
        if (Config.SHOW_NPC_LVL && _cha instanceof L2MonsterInstance)
	    {
			String t = "Lv " + cha.getLevel() + (cha.getAggroRange() > 0 ? "*" : "");
			if (_title != null)
				t += " " + _title;
			
			_title = t;
	    }
        if (cha.getTemplate().ischar > 0)
        {
            IsChar = true;
            armor = cha.getTemplate().armor;
            pant = cha.getTemplate().pant;
            head = cha.getTemplate().head;
            boot = cha.getTemplate().boot;
            glove = cha.getTemplate().glove;
            head = cha.getTemplate().head;
            _lrhand = cha.getTemplate().lrhand;
            charrace = cha.getTemplate().charrace;
            charclass = cha.getTemplate().charclass;
            charface = cha.getTemplate().charface;
            charcolor = cha.getTemplate().charcolor;
            charhair = cha.getTemplate().charhair;
            charhero = cha.getTemplate().charhero;
            charsex = cha.getTemplate().charsex;
            if (cha.getTemplate().enchlvl>0)
            {
                enchlvl = Math.min(127, cha.getTemplate().enchlvl);
            }
            else enchlvl= 0; 
        }
	}
	
	public NpcInfo(L2Summon cha, L2Character attacker)
	{
		_cha = cha;
		_idTemplate = cha.getTemplate().idTemplate;
		_isAttackable = cha.isAutoAttackable(attacker); //(cha.getKarma() > 0);
		_rhand = cha.getTemplate().rhand;
		_lhand = cha.getTemplate().lhand;
		_isSummoned = cha.isShowSummonAnimation();
        collisionHeight = _cha.getTemplate().collisionHeight;
        collisionRadius = _cha.getTemplate().collisionRadius;
        _atkspdMul = _cha.getPAtkSpd()/_cha.getTemplate().basePAtkSpd;
        _movespdMul = _cha.getRunSpeed()/_cha.getTemplate().baseRunSpd;
        if (_atkspdMul<1) _atkspdMul = 1;
        if (_movespdMul<1) _movespdMul = 1;
        if (cha.getTemplate().serverSideName || cha instanceof L2PetInstance)
    	{
            _name = _cha.getName();
    		_title = cha.getTitle();
    	}
        if (cha.getTemplate().ischar > 0)
        {
            IsChar = true;
            armor = cha.getTemplate().armor;
            pant = cha.getTemplate().pant;
            head = cha.getTemplate().head;
            boot = cha.getTemplate().boot;
            glove = cha.getTemplate().glove;
            head = cha.getTemplate().head;
            _lrhand = cha.getTemplate().lrhand;
            charrace = cha.getTemplate().charrace;
            charclass = cha.getTemplate().charclass;
            charface = cha.getTemplate().charface;
            charcolor = cha.getTemplate().charcolor;
            charhair = cha.getTemplate().charhair;
            charhero = cha.getTemplate().charhero;
            charsex = cha.getTemplate().charsex;
            if (cha.getTemplate().enchlvl>0)
            {
                enchlvl = Math.min(127, cha.getTemplate().enchlvl);
            }
            else enchlvl= 0; 
            
        }
	}
	
	final void runImpl()
	{
		_x = _cha.getX();
		_y = _cha.getY();
		_z = _cha.getZ();
		_heading = _cha.getHeading();
		_mAtkSpd = _cha.getMAtkSpd();
		_pAtkSpd = _cha.getPAtkSpd();
		_runSpd = _cha.getRunSpeed();
		_walkSpd = _cha.getWalkSpeed();
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
	}
	
	final void writeImpl()
	{
        
        if (IsChar)
        {
            writeC(0x03);       
            writeD(_x);
            writeD(_y);
            writeD(_z);
            writeD(_heading);
            writeD(_cha.getObjectId());
            writeS(_cha.getName());
            writeD(charrace);
            writeD(charsex);
            writeD(charclass);
            
            writeD(0); // unknown, maybe underwear?
            writeD(head);
            writeD(_rhand);
            writeD(_lhand);
            writeD(glove);
            writeD(armor);
            writeD(pant);
            writeD(boot);
            writeD(0);
            writeD(_lrhand);
            writeD(0);
            
            writeD(0);
            writeD(0);

            writeD(_mAtkSpd);
            writeD(_pAtkSpd);
            
            writeD(0);
            writeD(0);

            writeD(_runSpd);
            writeD(_walkSpd);
            writeD(_swimRunSpd/*0x32*/);  // swimspeed
            writeD(_swimWalkSpd/*0x32*/);  // swimspeed
            writeD(_flRunSpd);
            writeD(_flWalkSpd);
            writeD(_flyRunSpd);
            writeD(_flyWalkSpd);
            writeF(_movespdMul); // _cha.getProperMultiplier()
            writeF(_atkspdMul); // _cha.getAttackSpeedMultiplier()
            writeF(collisionRadius);
            writeF(collisionHeight);

            writeD(charhair);
            writeD(charcolor);
            writeD(charface);
            
            writeS(_title);
            writeD(0);
            writeD(0);
            writeD(0);
            writeD(0);
            writeD(0);  // new in rev 417   siege-flags
            
            writeC(1);   // standing = 1  sitting = 0
            writeC(1);   // running = 1   walking = 0
            writeC(_cha.isInCombat() ? 1 : 0);
            writeC(_cha.isAlikeDead() ? 1 : 0);
            
            writeC(0);    // invisible = 1  visible =0
            writeC(0);    // 1 on strider   2 on wyvern   0 no mount
            writeC(0);   //  1 - sellshop
            
            writeH(0);
            //writeH(0);
            
            writeC(0x00);   // find party members
            
            writeD(_cha.getAbnormalEffect());

//          Code that works for getEnchantEffect()

            writeC(0x00);                       //Changed by Thorgrim
            writeH(0); //Blue value for name (0 = white, 255 = pure blue)
            writeD(0x00);
            
            writeD(0x00); // unknown
            writeD(0x00); // unknown
            //writeC(_cha.isMounted() ? 0 :_cha.getEnchantEffect());
            writeC(enchlvl);
            writeC(0x00); //??
            
            writeD(0); 
            writeC(0); // Symbol on char menu ctrl+I  
            writeC(charhero); // Hero Aura 
            
            writeC(0); //0x01: Fishing Mode (Cant be undone by setting back to 0)
            writeD(0);  
            writeD(0);
            writeD(0);
            writeD(0x6d9aff);
        }
        else
        {
        if (_cha instanceof L2Summon)
            if (((L2Summon)_cha).getOwner() != null 
                    && ((L2Summon)_cha).getOwner().getAppearance().getInvisible())
                return;
		writeC(0x16);
		writeD(_cha.getObjectId());
		writeD(_idTemplate+1000000);  // npctype id
		writeD(_isAttackable ? 1 : 0); 
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
		writeD(0x00);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd/*0x32*/);  // swimspeed
		writeD(_swimWalkSpd/*0x32*/);  // swimspeed
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(_movespdMul)/*_cha.getProperMultiplier()*/;
		//writeF(1/*_cha.getAttackSpeedMultiplier()*/);
		writeF(_atkspdMul);
		writeF(collisionRadius);
		writeF(collisionHeight);
		writeD(_rhand); // right hand weapon
		writeD(0);
		writeD(_lhand); // left hand weapon
		writeC(1);	// name above char 1=true ... ??
		writeC(_cha.isRunning() ? 1 : 0);
		writeC(_cha.isInCombat() ? 1 : 0);
		writeC(_cha.isAlikeDead() ? 1 : 0);
		writeC(_isSummoned ? 2 : 0); // invisible ?? 0=false  1=true   2=summoned (only works if model has a summon animation)
		writeS(_name);
		writeS(_title);
		writeD(0);
		writeD(0);
		writeD(0000);  // hmm karma ??

		writeD(_cha.getAbnormalEffect());  // C2
		writeD(0000);  // C2
		writeD(0000);  // C2
		writeD(0000);  // C2
		writeD(0000);  // C2
		writeC(0000);  // C2
        
		writeC(0x00);  // C3  team circle 1-blue, 2-red 
		writeF(0x00);  // C4 i think it is collisionRadius a second time
		writeF(0x00);  // C4      "        collisionHeight     "
		writeD(0x00);  // C4 
        }
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__22_NPCINFO;
	}
}
