package net.sf.l2j.gameserver.skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.StatsSet;

public class L2SkillSeed extends L2Skill {

    final int num_seeds;
    
    public L2SkillSeed(StatsSet set) {
        super(set);
        num_seeds = 2;
    }

public void useSkill(L2Character activeChar, L2Object[] targets) {
    int seed_of_fire  = 1285;
    int seed_of_water = 1286;
    int seed_of_wind  = 1287;
    
    if (activeChar.isAlikeDead())
        return;

    // Update Seeds Effects
    for (int i=0;i<targets.length; i++) {
        L2Character target = (L2Character)targets[i];
        if (target.isAlikeDead() && getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)
            continue;

        EffectSeed e_seed_of_fire = (EffectSeed) target.getEffect(seed_of_fire);
        if (e_seed_of_fire != null && this.getId() == seed_of_fire) {
            if (e_seed_of_fire.num_seeds < num_seeds)
            {
                e_seed_of_fire.num_seeds++;
                target.updateEffectIcons();
                SystemMessage sm = new SystemMessage(614);
                sm.addString("SYS");
                sm.addString("火的種子效果" + e_seed_of_fire.num_seeds + "階段");
                target.sendPacket(sm);
                e_seed_of_fire.rescheduleEffect();
             }
             return;
        }

        EffectSeed e_seed_of_water = (EffectSeed) target.getEffect(seed_of_water);
        if (e_seed_of_water != null && this.getId() == seed_of_water) {
            if (e_seed_of_water.num_seeds < num_seeds)
            {
                e_seed_of_water.num_seeds++;
                target.updateEffectIcons();
                SystemMessage sm = new SystemMessage(614);
                sm.addString("SYS");
                sm.addString("水的種子效果" + e_seed_of_water.num_seeds + "階段");
                target.sendPacket(sm);
                e_seed_of_water.rescheduleEffect();
             }
             return;
        }
        
        EffectSeed e_seed_of_wind = (EffectSeed) target.getEffect(seed_of_wind);
        if (e_seed_of_wind != null && this.getId() == seed_of_wind) {
            if (e_seed_of_wind.num_seeds < num_seeds)
            {
                e_seed_of_wind.num_seeds++;
                target.updateEffectIcons();
                SystemMessage sm = new SystemMessage(614);
                sm.addString("SYS");
                sm.addString("風的種子效果" + e_seed_of_wind.num_seeds + "階段");
                target.sendPacket(sm);
                e_seed_of_wind.rescheduleEffect();
             }
             return;
        }
        
        this.getEffects(activeChar, target);

        L2Effect[] effects = target.getAllEffects();
        for (int j=0;j<effects.length;j++) {
            if (effects[j].getEffectType()==L2Effect.EffectType.SEED) {
                EffectSeed e = (EffectSeed)effects[j];
                if (e.getInUse() || e.getSkill().getId()==this.getId()) {
                    e.rescheduleEffect();
                }
            }
        }
    }
}

}