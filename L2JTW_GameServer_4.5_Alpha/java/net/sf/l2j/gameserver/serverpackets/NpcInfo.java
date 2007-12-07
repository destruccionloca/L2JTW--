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
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.model.L2NpcCharData;


/**
 * This class ...
 *
 * @version $Revision: 1.7.2.4.2.9 $ $Date: 2005/04/11 10:05:54 $
 */
public class NpcInfo extends L2GameServerPacket
{
	//   ddddddddddddddddddffffdddcccccSSddd dddddc
	//   ddddddddddddddddddffffdddcccccSSddd dddddccffd


	private static final String _S__22_NPCINFO = "[S] 16 NpcInfo";
	private L2Character _activeChar;
	private int _x, _y, _z, _heading;
	private int _idTemplate;
	private boolean _isAttackable, _isSummoned;
	private int _mAtkSpd, _pAtkSpd;
	private int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd,_atkspdMul,_movespdMul;
	private int _rhand, _lhand,_lrhand,enchlvl, armor, head, boot,pant, glove, charrace,charhair,charface,charcolor,charclass,charhero,charsex,dhair,hair,face,augmentation;
    private int _collisionHeight, _collisionRadius;
    private String _name = "";
    private String _title = "";
    private boolean IsChar;

	/**
	 * @param _characters
	 */
	public NpcInfo(L2NpcInstance cha, L2Character attacker)
	{
		_activeChar = cha;
		_idTemplate = cha.getTemplate().idTemplate;
		_isAttackable = cha.isAutoAttackable(attacker);
		_rhand = cha.getTemplate().rhand;
		_lhand = cha.getTemplate().lhand;
		_isSummoned = false;
        _collisionHeight = _activeChar.getTemplate().collisionHeight;
        _collisionRadius = _activeChar.getTemplate().collisionRadius;
        if (_activeChar.getTemplate().basePAtkSpd>0 &&  _activeChar.getPAtkSpd()>0)
        _atkspdMul = _activeChar.getPAtkSpd()/_activeChar.getTemplate().basePAtkSpd;
        else _atkspdMul = 1;
        if (_activeChar.getTemplate().baseRunSpd>0 && _activeChar.getRunSpeed()>0)
        _movespdMul = cha.getRunSpeed()/cha.getTemplate().baseRunSpd;
        else _movespdMul = 1;
        if (_atkspdMul<1) _atkspdMul = 1;
        if (_movespdMul<1) _movespdMul = 1;
        if (cha.getTemplate().serverSideName)
        	_name = cha.getTemplate().name;

        if(Config.L2JMOD_CHAMPION_ENABLE && cha.isChampion())
            _title = ("Champion");
        else if (cha.getTemplate().serverSideTitle)
    		_title = cha.getTemplate().title;
    	else
    		_title = cha.getTitle();

        if (Config.SHOW_NPC_LVL && _activeChar instanceof L2MonsterInstance)
	    {
			String t = "Lv " + cha.getLevel() + (cha.getAggroRange() > 0 ? "*" : "");
			if (_title != null)
				t += " " + _title;

			_title = t;
	    }

        L2NpcCharData chardata = new L2NpcCharData();
        
        if (chardata.getIsChar() > 0)
        {
        	
            IsChar = true;
            armor = chardata.getArmor();
            pant = chardata.getPant();
            head = chardata.getHead();
            boot = chardata.getBoot();
            glove = chardata.getGlove();
            face = chardata.getFace();
            dhair = chardata.getDHair();
            hair = chardata.getHair();
            _lrhand = chardata.getLrhand();
            charrace = chardata.getCharRace();
            charclass = chardata.getCharClass();
            charface = chardata.getCharFace();
            charcolor = chardata.getCharColor();
            charhair = chardata.getCharHair();
            charhero = chardata.getCharHero();
            charsex = chardata.getCharSex();
            augmentation = chardata.getAugmentation();
            
            
            
            if (chardata.getEnchLvl()>0)
            {
                enchlvl = Math.min(127, chardata.getEnchLvl());
            }
            else enchlvl= 0; 
        }
        
        _x = _activeChar.getX();
		_y = _activeChar.getY();
		_z = _activeChar.getZ();
		_heading = _activeChar.getHeading();
		_mAtkSpd = _activeChar.getMAtkSpd();
		_pAtkSpd = _activeChar.getPAtkSpd();
		_runSpd = _activeChar.getRunSpeed();
		_walkSpd = _activeChar.getWalkSpeed();
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
	}

	public NpcInfo(L2Summon cha, L2Character attacker)
	{
		_activeChar = cha;
		_idTemplate = cha.getTemplate().idTemplate;
		_isAttackable = cha.isAutoAttackable(attacker); //(cha.getKarma() > 0);
		_rhand = cha.getTemplate().rhand;
		_lhand = cha.getTemplate().lhand;
		_isSummoned = cha.isShowSummonAnimation();
        _collisionHeight = _activeChar.getTemplate().collisionHeight;
        _collisionRadius = _activeChar.getTemplate().collisionRadius;
        _atkspdMul = _activeChar.getPAtkSpd()/_activeChar.getTemplate().basePAtkSpd;
        _movespdMul = _activeChar.getRunSpeed()/_activeChar.getTemplate().baseRunSpd;
        if (_atkspdMul<1) _atkspdMul = 1;
        if (_movespdMul<1) _movespdMul = 1;
        //if (cha.getTemplate().serverSideName || cha instanceof L2PetInstance)
        if (cha.getTemplate().serverSideName)
    	{
            _name = _activeChar.getName();
    	}
        else
        {
        	_name ="";
        }
        if (cha.getTemplate().serverSideTitle)
    	{
    		_title = cha.getTitle();
    	}
        else
        {
        	_title = "";
        }
        L2NpcCharData chardata = new L2NpcCharData();
        

        if (chardata.getIsChar() > 0)
        {
        	
            IsChar = true;
            armor = chardata.getArmor();
            pant = chardata.getPant();
            head = chardata.getHead();
            boot = chardata.getBoot();
            glove = chardata.getGlove();
            face = chardata.getFace();
            dhair = chardata.getDHair();
            hair = chardata.getHair();
            _lrhand = chardata.getLrhand();
            charrace = chardata.getCharRace();
            charclass = chardata.getCharClass();
            charface = chardata.getCharFace();
            charcolor = chardata.getCharColor();
            charhair = chardata.getCharHair();
            charhero = chardata.getCharHero();
            charsex = chardata.getCharSex();

            if (chardata.getEnchLvl()>0)
            {
                enchlvl = Math.min(127, chardata.getEnchLvl());
            }
            else enchlvl= 0; 
        }


        
        _x = _activeChar.getX();
		_y = _activeChar.getY();
		_z = _activeChar.getZ();
		_heading = _activeChar.getHeading();
		_mAtkSpd = _activeChar.getMAtkSpd();
		_pAtkSpd = _activeChar.getPAtkSpd();
		_runSpd = _activeChar.getRunSpeed();
		_walkSpd = _activeChar.getWalkSpeed();

		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
	}

	@Override
	protected final void writeImpl()
	{
        
        if (IsChar)
        {
        	writeC(0x03);		
			writeD(_x);
			writeD(_y);
			writeD(_z);
			writeD(_heading);
			writeD(_activeChar.getObjectId());
			writeS(_activeChar.getName());
			writeD(charrace);
			writeD(charsex);
			writeD(charclass);

			
			writeD(dhair);
			writeD(head);
			writeD(_rhand);
			writeD(_lhand);
			writeD(glove);
			writeD(armor);
			writeD(pant);
			writeD(boot);
			writeD(0);
			writeD(_lrhand);
			writeD(hair);
			writeD(face);
			
			// c6 new h's
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeD(augmentation);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			if(_lrhand>0)
			writeD(augmentation);
			else
			writeD(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			
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
			writeF(_activeChar.getMovementSpeedMultiplier()); // _activeChar.getProperMultiplier()
			writeF(_activeChar.getAttackSpeedMultiplier()); // _activeChar.getAttackSpeedMultiplier()
			writeF(_collisionRadius);
			writeF(_collisionHeight);
	
			writeD(charhair);
			writeD(charcolor);
			writeD(charface);
			
			writeS(_activeChar.getTitle());
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0);	// new in rev 417   siege-flags
			
			writeC(1);	// standing = 1  sitting = 0
			writeC(_activeChar.isRunning() ? 1 : 0);	// running = 1   walking = 0
			writeC(_activeChar.isInCombat() ? 1 : 0);
			writeC(_activeChar.isAlikeDead() ? 1 : 0);
			
			writeC(0);	// invisible = 1  visible =0
			writeC(0);	// 1 on strider   2 on wyvern   0 no mount
			writeC(0);   //  1 - sellshop
			
			writeH(0);
			writeH(0);
			
			writeC(0x00);	// find party members
			
	        writeD(_activeChar.getAbnormalEffect());
			writeC(0);                       //Changed by Thorgrim
			writeH(0); //Blue value for name (0 = white, 255 = pure blue)
			writeD(charclass);
			
			writeD(0);
			writeD((int) _activeChar.getCurrentCp());
	        writeC(enchlvl);
			
        	writeC(0x00); //team circle around feet 1= Blue, 2 = red
	        
			writeD(0); 
			writeC(0); // Symbol on char menu ctrl+I  
			writeC(charhero); // Hero Aura
			
			writeC(0); //0x01: Fishing Mode (Cant be undone by setting back to 0)
			writeD(0);  
			writeD(0);
			writeD(0);
			
	        writeD(0xFFFFFF);
	        
	        writeD(0x00); // ??
	        
	        writeD(0); 
	        writeD(0x00); // ??
	        
	        writeD(0x00);
	        
	        writeD(0x00); // ??
	        
        	writeD(0x00);
        }
        else
        {
        if (_activeChar instanceof L2Summon)
            if (((L2Summon)_activeChar).getOwner() != null
                    && ((L2Summon)_activeChar).getOwner().getAppearance().getInvisible())

                return;
		writeC(0x16);
		writeD(_activeChar.getObjectId());
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
		writeF(_collisionRadius);
		writeF(_collisionHeight);
		writeD(_rhand); // right hand weapon
		writeD(0);
		writeD(_lhand); // left hand weapon
		writeC(1);	// name above char 1=true ... ??
		writeC(_activeChar.isRunning() ? 1 : 0);
		writeC(_activeChar.isInCombat() ? 1 : 0);
		writeC(_activeChar.isAlikeDead() ? 1 : 0);
		writeC(_isSummoned ? 2 : 0); // invisible ?? 0=false  1=true   2=summoned (only works if model has a summon animation)
		writeS(_name);
		writeS(_title);
		writeD(0);
		writeD(0);
		writeD(0000);  // hmm karma ??

		writeD(_activeChar.getAbnormalEffect());  // C2
		writeD(0000);  // C2
		writeD(0000);  // C2
		writeD(0000);  // C2
		writeD(0000);  // C2
		writeC(0000);  // C2

		writeC(0x00);  // C3  team circle 1-blue, 2-red
		writeF(0x00);  // C4 i think it is collisionRadius a second time
		writeF(0x00);  // C4      "        collisionHeight     "
		writeD(0x00);  // C4
		writeD(0x00);  // C6

        }

	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__22_NPCINFO;
	}
}
