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
* This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.model.base;

import static net.sf.l2j.gameserver.model.base.ClassLevel.First;
import static net.sf.l2j.gameserver.model.base.ClassLevel.Fourth;
import static net.sf.l2j.gameserver.model.base.ClassLevel.Second;
import static net.sf.l2j.gameserver.model.base.ClassLevel.Third;
import static net.sf.l2j.gameserver.model.base.ClassType.Fighter;
import static net.sf.l2j.gameserver.model.base.ClassType.Mystic;
import static net.sf.l2j.gameserver.model.base.ClassType.Priest;
import static net.sf.l2j.gameserver.model.base.Race.DarkElf;
import static net.sf.l2j.gameserver.model.base.Race.Dwarf;
import static net.sf.l2j.gameserver.model.base.Race.Elf;
import static net.sf.l2j.gameserver.model.base.Race.Human;
import static net.sf.l2j.gameserver.model.base.Race.Kamael;
import static net.sf.l2j.gameserver.model.base.Race.Orc;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public enum PlayerClass {
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
     
     ���F�Ԥh        (Elf, Fighter, First),
     ���F�M�h         (Elf, Fighter, Second),
     �t���M�h        (Elf, Fighter, Third),
     �C�N�֤H         (Elf, Fighter, Third),
     ���F���u          (Elf, Fighter, Second),
     �j�a���        (Elf, Fighter, Third),
     �Ȥ�C�L        (Elf, Fighter, Third),
     ���F�k�v         (Elf, Mystic, First),
     ���F�Ův         (Elf, Mystic, Second),
     �G�N�֤H         (Elf, Mystic, Third),
     ������   (Elf, Mystic, Third),
     ����         (Elf, Priest, Second),
     ����          (Elf, Priest, Third),
     
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
    dummyEntry1(null, null, null), dummyEntry2(null, null, null), dummyEntry3(null, null, null), dummyEntry4(
            null, null, null), dummyEntry5(null, null, null), dummyEntry6(null, null, null), dummyEntry7(
            null, null, null), dummyEntry8(null, null, null), dummyEntry9(null, null, null), dummyEntry10(
            null, null, null), dummyEntry11(null, null, null), dummyEntry12(null, null, null), dummyEntry13(
            null, null, null), dummyEntry14(null, null, null), dummyEntry15(null, null, null), dummyEntry16(
            null, null, null), dummyEntry17(null, null, null), dummyEntry18(null, null, null), dummyEntry19(
            null, null, null), dummyEntry20(null, null, null), dummyEntry21(null, null, null), dummyEntry22(
            null, null, null), dummyEntry23(null, null, null), dummyEntry24(null, null, null), dummyEntry25(
            null, null, null), dummyEntry26(null, null, null), dummyEntry27(null, null, null), dummyEntry28(
            null, null, null), dummyEntry29(null, null, null), dummyEntry30(null, null, null), 
    /*
     * (3rd classes)
     */
    duelist(Human, Fighter, Fourth), dreadnought(Human, Fighter, Fourth), phoenixKnight(Human, Fighter,
            Fourth), hellKnight(Human, Fighter, Fourth), sagittarius(Human, Fighter, Fourth), adventurer(
            Human, Fighter, Fourth), archmage(Human, Mystic, Fourth), soultaker(Human, Mystic, Fourth), arcanaLord(
            Human, Mystic, Fourth), cardinal(Human, Mystic, Fourth), hierophant(Human, Mystic, Fourth),

    evaTemplar(Elf, Fighter, Fourth), swordMuse(Elf, Fighter, Fourth), windRider(Elf,
            Fighter, Fourth), moonlightSentinel(Elf, Fighter, Fourth), mysticMuse(Elf, Mystic,
            Fourth), elementalMaster(Elf, Mystic, Fourth), evaSaint(Elf, Mystic, Fourth),

    shillienTemplar(DarkElf, Fighter, Fourth), spectralDancer(DarkElf, Fighter, Fourth), ghostHunter(
            DarkElf, Fighter, Fourth), ghostSentinel(DarkElf, Fighter, Fourth), stormScreamer(DarkElf,
            Mystic, Fourth), spectralMaster(DarkElf, Mystic, Fourth), shillienSaint(DarkElf, Mystic,
            Fourth),

    titan(Orc, Fighter, Fourth), grandKhauatari(Orc, Fighter, Fourth), dominator(Orc, Mystic, Fourth), doomcryer(
            Orc, Mystic, Fourth),

    fortuneSeeker(Dwarf, Fighter, Fourth), maestro(Dwarf, Fighter, Fourth),
    
    dummyEntry31(null, null, null),
    dummyEntry32(null, null, null),
    dummyEntry33(null, null, null),
    dummyEntry34(null, null, null),
    
    maleSoldier(Kamael, Fighter, First),
    femaleSoldier(Kamael, Fighter, First),
    trooper(Kamael, Fighter, Second),
    warder(Kamael, Fighter, Second),
    berserker(Kamael, Fighter, Third),
    maleSoulbreaker(Kamael, Fighter, Third),
    femaleSoulbreaker(Kamael, Fighter, Third),
    arbalester(Kamael, Fighter, Third),
    doombringer(Kamael, Fighter, Fourth),
    maleSoulhound(Kamael, Fighter, Fourth), 
    femaleSoulhound(Kamael, Fighter, Fourth),
    trickster(Kamael, Fighter, Fourth), 
    inspector(Kamael, Fighter, Third),
    judicator(Kamael, Fighter, Fourth);
    
    private Race _race;
    private ClassLevel _level;
    private ClassType _type;

    private static final Set<PlayerClass> mainSubclassSet;
    private static final Set<PlayerClass> neverSubclassed   = EnumSet.of(�Q�D, �Ԫ��u�K);

    private static final Set<PlayerClass> subclasseSet1     = EnumSet.of(���M�h, �t�M�h, �t���M�h, �u�Y�M�h);
    private static final Set<PlayerClass> subclasseSet2     = EnumSet.of(�_���y�H, �`�W���, �j�a���);
    private static final Set<PlayerClass> subclasseSet3     = EnumSet.of(�N��, �Ȥ�C�L, ��v�C�L);
    private static final Set<PlayerClass> subclasseSet4     = EnumSet.of(�k�], ������, ��v�l��h);
    private static final Set<PlayerClass> subclasseSet5     = EnumSet.of(�N�h, �G�N�֤H, �g�G�N�h);

    private static final EnumMap<PlayerClass, Set<PlayerClass>> subclassSetMap = new EnumMap<PlayerClass, Set<PlayerClass>>(PlayerClass.class);

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

    PlayerClass(Race pRace, ClassType pType, ClassLevel pLevel)
    {
        _race = pRace;
        _level = pLevel;
        _type = pType;
    }

    public final Set<PlayerClass> getAvailableSubclasses(L2PcInstance player)
    {
        Set<PlayerClass> subclasses = null;

        if (_level == Third)
        {
            if (player.getRace() != Kamael)
            {
                subclasses = EnumSet.copyOf(mainSubclassSet);

                subclasses.remove(this);

                switch (_race)
                {
                    case Elf:
                        subclasses.removeAll(getSet(DarkElf, Third));
                        break;
                    case DarkElf:
                        subclasses.removeAll(getSet(Elf, Third));
                        break;
                }
                
                subclasses.removeAll(getSet(Kamael,Third));

                Set<PlayerClass> unavailableClasses = subclassSetMap.get(this);

                if (unavailableClasses != null)
                    subclasses.removeAll(unavailableClasses);
                
            }
            else
            {
                subclasses = getSet(Kamael,Third);
                subclasses.remove(this);
                //Check sex, male can't subclass female and vice versa
                if (player.getAppearance().getSex())
                    subclasses.removeAll(EnumSet.of(maleSoulbreaker));
                else subclasses.removeAll(EnumSet.of(femaleSoulbreaker));
                if (player.getTotalSubClasses() < 2)
                    subclasses.removeAll(EnumSet.of(inspector));            
            }
        }
        return subclasses;
    }

    public static final EnumSet<PlayerClass> getSet(Race race, ClassLevel level)
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

    public final boolean isOfRace(Race pRace)
    {
        return _race == pRace;
    }

    public final boolean isOfType(ClassType pType)
    {
        return _type == pType;
    }

    public final boolean isOfLevel(ClassLevel pLevel)
    {
        return _level == pLevel;
    }
    public final ClassLevel getLevel()
    {
        return _level;
    }
}
