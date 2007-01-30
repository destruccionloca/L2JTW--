package net.sf.l2j.gameserver.skills;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class EffectSlam extends L2Effect {

	
	public EffectSlam(Env env, EffectTemplate template) {
		super(env, template);
	}


	public EffectType getEffectType() {
		return L2Effect.EffectType.SLAM;
	}

	public void onStart() {
		getEffected().startSlam();
        L2ItemInstance weaponInst = getEffected().getActiveWeaponInstance();
        //TODO:unequip the weapon and update inv.
        if (getEffected() instanceof L2PcInstance)
        getEffected().setTarget(null);
        getEffected().breakAttack();
        getEffected().breakCast();
        getEffected().startSlam();
        onActionTime();
	}
	
	public boolean onActionTime() {
        
		getEffected().stopSlam(this);
		return false;
	}


	public void onExit() {
		getEffected().stopSlam(this);
	}



}
