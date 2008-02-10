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
	 戰士    (Human, Fighter, First),
     鬥士         (Human, Fighter, Second),
     劍鬥士       (Human, Fighter, Third),
     傭兵         (Human, Fighter, Third),
     騎士     (Human, Fighter, Second),
     聖騎士         (Human, Fighter, Third),
     闇騎士     (Human, Fighter, Third),
     盜賊           (Human, Fighter, Second),
     寶藏獵人  (Human, Fighter, Third),
     鷹眼         (Human, Fighter, Third),
     人類法師     (Human, Mystic, First),
     巫師     (Human, Mystic, Second),
     術士        (Human, Mystic, Third),
     死靈法師     (Human, Mystic, Third),
     法魔         (Human, Mystic, Third),
     牧師          (Human, Priest, Second),
     主教          (Human, Priest, Third),
     先知         (Human, Priest, Third),
     
     精靈戰士        (Elf, Fighter, First),
     精靈騎士         (Elf, Fighter, Second),
     聖殿騎士        (Elf, Fighter, Third),
     劍術詩人         (Elf, Fighter, Third),
     精靈巡守          (Elf, Fighter, Second),
     大地行者        (Elf, Fighter, Third),
     銀月遊俠        (Elf, Fighter, Third),
     精靈法師         (Elf, Mystic, First),
     精靈巫師         (Elf, Mystic, Second),
     咒術詩人         (Elf, Mystic, Third),
     元素使   (Elf, Mystic, Third),
     神使         (Elf, Priest, Second),
     長老          (Elf, Priest, Third),
     
     黑暗精靈戰士    (DarkElf, Fighter, First),
     沼澤騎士         (DarkElf, Fighter, Second),
     席琳騎士      (DarkElf, Fighter, Third),
     劍刃舞者         (DarkElf, Fighter, Third),
     暗殺者            (DarkElf, Fighter, Second),
     深淵行者         (DarkElf, Fighter, Third),
     闇影遊俠       (DarkElf, Fighter, Third),
     黑暗精靈法師     (DarkElf, Mystic, First),
     黑暗精靈巫師     (DarkElf, Mystic, Second),
     狂咒術士         (DarkElf, Mystic, Third),
     闇影召喚士     (DarkElf, Mystic, Third),
     席琳神使      (DarkElf, Priest, Second),
     席琳長老       (DarkElf, Priest, Third),
     
     半獸人戰士  (Orc, Fighter, First),
     半獸人突襲者   (Orc, Fighter, Second),
     破壞者   (Orc, Fighter, Third),
     半獸人武者     (Orc, Fighter, Second),
     暴君      (Orc, Fighter, Third),
     半獸人法師   (Orc, Mystic, First),
     半獸人巫醫   (Orc, Mystic, Second),
     霸主    (Orc, Mystic, Third),
     戰狂    (Orc, Mystic, Third),
     
     矮人戰士      (Dwarf, Fighter, First),
     收集者    (Dwarf, Fighter, Second),
     賞金獵人        (Dwarf, Fighter, Third),
     工匠      (Dwarf, Fighter, Second),
     戰爭工匠            (Dwarf, Fighter, Third),
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
    private static final Set<PlayerClass> neverSubclassed   = EnumSet.of(霸主, 戰爭工匠);

    private static final Set<PlayerClass> subclasseSet1     = EnumSet.of(闇騎士, 聖騎士, 聖殿騎士, 席琳騎士);
    private static final Set<PlayerClass> subclasseSet2     = EnumSet.of(寶藏獵人, 深淵行者, 大地行者);
    private static final Set<PlayerClass> subclasseSet3     = EnumSet.of(鷹眼, 銀月遊俠, 闇影遊俠);
    private static final Set<PlayerClass> subclasseSet4     = EnumSet.of(法魔, 元素使, 闇影召喚士);
    private static final Set<PlayerClass> subclasseSet5     = EnumSet.of(術士, 咒術詩人, 狂咒術士);

    private static final EnumMap<PlayerClass, Set<PlayerClass>> subclassSetMap = new EnumMap<PlayerClass, Set<PlayerClass>>(PlayerClass.class);

    static
    {
        Set<PlayerClass> subclasses = getSet(null, Third);
        subclasses.removeAll(neverSubclassed);

        mainSubclassSet = subclasses;

        subclassSetMap.put(闇騎士,     subclasseSet1);
        subclassSetMap.put(聖騎士,         subclasseSet1);
        subclassSetMap.put(聖殿騎士,    subclasseSet1);
        subclassSetMap.put(席琳騎士,  subclasseSet1);
        
        subclassSetMap.put(寶藏獵人,  subclasseSet2);
        subclassSetMap.put(深淵行者,     subclasseSet2);
        subclassSetMap.put(大地行者,    subclasseSet2);
        
        subclassSetMap.put(鷹眼,         subclasseSet3);
        subclassSetMap.put(銀月遊俠,    subclasseSet3);
        subclassSetMap.put(闇影遊俠,   subclasseSet3);
        
        subclassSetMap.put(法魔,             subclasseSet4);
        subclassSetMap.put(元素使,   subclasseSet4);
        subclassSetMap.put(闇影召喚士,     subclasseSet4);
        
        subclassSetMap.put(術士,    subclasseSet5);
        subclassSetMap.put(咒術詩人, subclasseSet5);
        subclassSetMap.put(狂咒術士, subclasseSet5);
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
