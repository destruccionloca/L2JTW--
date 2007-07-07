/*
 * $Header: PlayerClass.java, 24/11/2005 12:56:01 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 24/11/2005 12:56:01 $
 * $Revision: 1 $
 * $Log: PlayerClass.java,v $
 * Revision 1  24/11/2005 12:56:01  luisantonioa
 * Added copyright notice
 *
 * 
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
package net.sf.l2j.gameserver.model.base;

import static net.sf.l2j.gameserver.model.base.ClassLevel.First;
import static net.sf.l2j.gameserver.model.base.ClassLevel.Fourth;
import static net.sf.l2j.gameserver.model.base.ClassLevel.Second;
import static net.sf.l2j.gameserver.model.base.ClassLevel.Third;
import static net.sf.l2j.gameserver.model.base.ClassType.Fighter;
import static net.sf.l2j.gameserver.model.base.ClassType.Mystic;
import static net.sf.l2j.gameserver.model.base.ClassType.Priest;
import static net.sf.l2j.gameserver.model.base.PlayerRace.DarkElf;
import static net.sf.l2j.gameserver.model.base.PlayerRace.Dwarf;
import static net.sf.l2j.gameserver.model.base.PlayerRace.Human;
import static net.sf.l2j.gameserver.model.base.PlayerRace.LightElf;
import static net.sf.l2j.gameserver.model.base.PlayerRace.Orc;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

/**
 * This class ...
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public enum PlayerClass
{
        �Ԥh    (Human, Fighter, First),
        ���h         (Human, Fighter, Second),
        �C���h       (Human, Fighter, Third),
        �ħL         (Human, Fighter, Third),
        �M�h     (Human, Fighter, Second),
        �t�M�h         (Human, Fighter, Third),
        ���M�h     (Human, Fighter, Third),
        �s��           (Human, Fighter, Second),
        �_���y�H  (Human, Fighter, Third),
        �N��         (Human, Fighter, Third),
        �H���k�v     (Human, Mystic, First),
        �Ův     (Human, Mystic, Second),
        �N�h        (Human, Mystic, Third),
        ���F�k�v     (Human, Mystic, Third),
        �k�]         (Human, Mystic, Third),
        ���v          (Human, Priest, Second),
        �D��          (Human, Priest, Third),
        ����         (Human, Priest, Third),
        
        ���F�Ԥh        (LightElf, Fighter, First),
        ���F�M�h         (LightElf, Fighter, Second),
        �t���M�h        (LightElf, Fighter, Third),
        �C�N�֤H         (LightElf, Fighter, Third),
        ���F���u          (LightElf, Fighter, Second),
        �j�a���        (LightElf, Fighter, Third),
        �Ȥ�C�L        (LightElf, Fighter, Third),
        ���F�k�v         (LightElf, Mystic, First),
        ���F�Ův         (LightElf, Mystic, Second),
        �G�N�֤H         (LightElf, Mystic, Third),
        ������   (LightElf, Mystic, Third),
        ����         (LightElf, Priest, Second),
        ����          (LightElf, Priest, Third),
        
        �·t���F�Ԥh    (DarkElf, Fighter, First),
        �h�A�M�h         (DarkElf, Fighter, Second),
        �u�Y�M�h      (DarkElf, Fighter, Third),
        �C�b�R��         (DarkElf, Fighter, Third),
        �t����            (DarkElf, Fighter, Second),
        �`�W���         (DarkElf, Fighter, Third),
        ��v�C�L       (DarkElf, Fighter, Third),
        �·t���F�k�v     (DarkElf, Mystic, First),
        �·t���F�Ův     (DarkElf, Mystic, Second),
        �g�G�N�h         (DarkElf, Mystic, Third),
        ��v�l��h     (DarkElf, Mystic, Third),
        �u�Y����      (DarkElf, Priest, Second),
        �u�Y����       (DarkElf, Priest, Third),
        
        �b�~�H�Ԥh  (Orc, Fighter, First),
        �b�~�H��ŧ��   (Orc, Fighter, Second),
        �}�a��   (Orc, Fighter, Third),
        �b�~�H�Z��     (Orc, Fighter, Second),
        �ɧg      (Orc, Fighter, Third),
        �b�~�H�k�v   (Orc, Mystic, First),
        �b�~�H����   (Orc, Mystic, Second),
        �Q�D    (Orc, Mystic, Third),
        �Ԩg    (Orc, Mystic, Third),
        
        �G�H�Ԥh      (Dwarf, Fighter, First),
        ������    (Dwarf, Fighter, Second),
        ����y�H        (Dwarf, Fighter, Third),
        �u�K      (Dwarf, Fighter, Second),
        �Ԫ��u�K            (Dwarf, Fighter, Third),
    


    

    dummyEntry1        (null, null, null),
    dummyEntry2        (null, null, null),
    dummyEntry3        (null, null, null),
    dummyEntry4        (null, null, null),
    dummyEntry5        (null, null, null),
    dummyEntry6        (null, null, null),
    dummyEntry7        (null, null, null),
    dummyEntry8        (null, null, null),
    dummyEntry9        (null, null, null),
    dummyEntry10       (null, null, null),
    dummyEntry11       (null, null, null),
    dummyEntry12       (null, null, null),
    dummyEntry13       (null, null, null),
    dummyEntry14       (null, null, null),
    dummyEntry15       (null, null, null),
    dummyEntry16       (null, null, null),
    dummyEntry17       (null, null, null),
    dummyEntry18       (null, null, null),
    dummyEntry19       (null, null, null),
    dummyEntry20       (null, null, null),
    dummyEntry21       (null, null, null),
    dummyEntry22       (null, null, null),
    dummyEntry23       (null, null, null),
    dummyEntry24       (null, null, null),
    dummyEntry25       (null, null, null),
    dummyEntry26       (null, null, null),
    dummyEntry27       (null, null, null),
    dummyEntry28       (null, null, null),
    dummyEntry29       (null, null, null),
    dummyEntry30       (null, null, null),

    /*
     * (3rd classes)
     */ 
    �M����           (Human, Fighter, Fourth),
    �r�N       (Human, Fighter, Fourth),
    �t���M�h     (Human, Fighter, Fourth),
    �Һ��M�h        (Human, Fighter, Fourth),
    �H��       (Human, Fighter, Fourth),
    �_�I�^��        (Human, Fighter, Fourth),
    �j�]�ɤh          (Human, Mystic, Fourth),
    ��N�h         (Human, Mystic, Fourth), 
    �����l�D        (Human, Mystic, Fourth),
    �Ͼ��D��          (Human, Mystic, Fourth),
    �L�t��        (Human, Mystic, Fourth),
     
    �쫽�t���M�h        (LightElf, Fighter, Fourth), 
    �쫽�u�C�֤H         (LightElf, Fighter, Fourth), 
    �e�����H         (LightElf, Fighter, Fourth), 
    ����b�F     (LightElf, Fighter, Fourth), 
    �쫽���N�֤H        (LightElf, Mystic, Fourth), 
    ������t��   (LightElf, Mystic, Fourth), 
    �쫽�t��          (LightElf, Mystic, Fourth), 

    �u�Y�߷��M�h   (DarkElf, Fighter, Fourth),
    �խ߻R��    (DarkElf, Fighter, Fourth),
    �y�v�y��       (DarkElf, Fighter, Fourth),
    �խ߽b�F     (DarkElf, Fighter, Fourth),
    ���ɨg�S��     (DarkElf, Mystic, Fourth),
    ��v��t��    (DarkElf, Mystic, Fourth),
    �u�Y�t��     (DarkElf, Mystic, Fourth),
     
    ���Z             (Orc, Fighter, Fourth),
    �d�ڶ𨽩v�v    (Orc, Fighter, Fourth),
    �g�D         (Orc, Mystic, Fourth),
    ����Ԩg         (Orc, Mystic, Fourth),

    �]�I�y�H     (Dwarf, Fighter, Fourth),
    ���K           (Dwarf, Fighter, Fourth);
    
    private PlayerRace _race;
    private ClassLevel _level;
    private ClassType _type;
    
    private static final Set<PlayerClass> mainSubclassSet;
    private static final Set<PlayerClass> neverSubclassed   = EnumSet.of(�Q�D, �Ԫ��u�K);

    private static final Set<PlayerClass> subclasseSet1     = EnumSet.of(���M�h, �t�M�h, �t���M�h, �u�Y�M�h);
    private static final Set<PlayerClass> subclasseSet2     = EnumSet.of(�_���y�H, �`�W���, �j�a���);
    private static final Set<PlayerClass> subclasseSet3     = EnumSet.of(�N��, �Ȥ�C�L, ��v�C�L);
    private static final Set<PlayerClass> subclasseSet4     = EnumSet.of(�k�], ������, ��v�l��h);
    private static final Set<PlayerClass> subclasseSet5     = EnumSet.of(�N�h, �G�N�֤H, �g�G�N�h);

    private static final EnumMap<PlayerClass, Set<PlayerClass>> subclassSetMap  = new EnumMap<PlayerClass, Set<PlayerClass>>(PlayerClass.class);
    
    static
    {
        Set<PlayerClass> subclasses = getSet(null, Third);
        subclasses.removeAll(neverSubclassed);
        
        mainSubclassSet = subclasses;
        
        subclassSetMap.put(���M�h,     subclasseSet1);
        subclassSetMap.put(�t�M�h,         subclasseSet1);
        subclassSetMap.put(�t���M�h,    subclasseSet1);
        subclassSetMap.put(�u�Y�M�h,  subclasseSet1);
        
        subclassSetMap.put(�_���y�H,  subclasseSet2);
        subclassSetMap.put(�`�W���,     subclasseSet2);
        subclassSetMap.put(�j�a���,    subclasseSet2);
        
        subclassSetMap.put(�N��,         subclasseSet3);
        subclassSetMap.put(�Ȥ�C�L,    subclasseSet3);
        subclassSetMap.put(��v�C�L,   subclasseSet3);
        
        subclassSetMap.put(�k�],             subclasseSet4);
        subclassSetMap.put(������,   subclasseSet4);
        subclassSetMap.put(��v�l��h,     subclasseSet4);
        
        subclassSetMap.put(�N�h,    subclasseSet5);
        subclassSetMap.put(�G�N�֤H, subclasseSet5);
        subclassSetMap.put(�g�G�N�h, subclasseSet5);
    }
    
    PlayerClass(PlayerRace pRace, ClassType pType, ClassLevel pLevel)
    {

        this._race = pRace;
        this._level = pLevel;
        this._type = pType;

    }
    
    public final Set<PlayerClass> getAvailableSubclasses()
    {
        Set<PlayerClass> subclasses = null;
        
        if (this._level == Third)
        {
            subclasses  = EnumSet.copyOf(mainSubclassSet);
            
            subclasses.removeAll(neverSubclassed);
            subclasses.remove(this);
            
            switch (this._race)
            {
                case LightElf:
                    subclasses.removeAll(getSet(DarkElf, Third));
                    break;
                case DarkElf:
                    subclasses.removeAll(getSet(LightElf, Third));
                    break;
            }
            
            Set<PlayerClass> unavailableClasses = subclassSetMap.get(this);
            
            if (unavailableClasses != null)
            {
                subclasses.removeAll(unavailableClasses);
            }
        }
        
        return subclasses;
    }
    
    public static final EnumSet<PlayerClass> getSet(PlayerRace race, ClassLevel level)
    {
        EnumSet<PlayerClass> allOf = EnumSet.noneOf(PlayerClass.class);
        
        for (PlayerClass playerClass : EnumSet.allOf(PlayerClass.class))
        {
            if (race == null || playerClass.isOfRace(race))
            {
                if (level == null || playerClass.isOfLevel(level))
                {
                    allOf.add(playerClass);
                }
            }
        }
        
        return allOf;
    }
    
    public final boolean isOfRace(PlayerRace pRace)
    {

        return this._race == pRace;

    }
    
    public final boolean isOfType(ClassType pType)
    {

        return this._type == pType;

    }
    
    public final boolean isOfLevel(ClassLevel pLevel)
    {

        return this._level == pLevel;

    }
    public final ClassLevel getLevel()
    {
        return _level;
    }
}
