package net.sf.l2j.gameserver.skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.StatsSet;

public class L2SkillChargeAtk extends L2Skill {

	final int num_charges;
	
	public L2SkillChargeAtk(StatsSet set) 
    {
		super(set);
		num_charges = set.getInteger("num_charges", getLevel());
	}

	public void useSkill(L2Character caster, @SuppressWarnings("unused") L2Object[] targets) {
		if (caster.isAlikeDead())
			return;
		
		boolean ss = false;
        


        for(int index = 0;index < targets.length;index++)
        {
            L2ItemInstance weapon = caster.getActiveWeaponInstance();
                    L2Character target = (L2Character)targets[index];
                    if (target.isAlikeDead())
          {
                        continue;
          }

            // TODO: should we use dual or not?
            // because if so, damage are lowered but we dont do anything special with dual then
            // like in doAttackHitByDual which in fact does the calcPhysDam call twice
            
            //boolean dual  = caster.isUsingDualWeapon();
           
            boolean shld = Formulas.getInstance().calcShldUse(caster, target);
            boolean dual = caster.isUsingDualWeapon();
            //boolean crit = Formulas.getInstance().calcCrit(caster.getCriticalHit(target, this));
            boolean soul = (weapon != null && weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT);

            int damage = (int) Formulas.getInstance().calcPhysDam(caster, target, this, shld, false, dual, soul);
            if (damage > 0)
            {
                
                target.reduceCurrentHp(damage, caster);
                SystemMessage sm = new SystemMessage(SystemMessage.YOU_DID_S1_DMG);
                sm.addNumber((int)damage);
                caster.sendPacket(sm);
                
                if (soul && weapon!= null)
                    weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
            }
            else
            {
                SystemMessage sm = new SystemMessage(SystemMessage.MISSED_TARGET);
                caster.sendPacket(sm);
            }
        }
        
		EffectCharge effect = (EffectCharge) caster.getEffect(4271);

		if (effect != null) {
			if (effect.num_charges < num_charges)
			{
				effect.num_charges++;
				caster.updateEffectIcons();
                SystemMessage sm = new SystemMessage(323);
                sm.addNumber(effect.num_charges);
                caster.sendPacket(sm);
			}
			else
            {
                SystemMessage sm = new SystemMessage(324);
                caster.sendPacket(sm);
            }
            return;
		}
		this.getEffects(caster, caster);
	}
	
}
