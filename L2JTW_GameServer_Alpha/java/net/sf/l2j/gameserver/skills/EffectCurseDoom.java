package net.sf.l2j.gameserver.skills;

import net.sf.l2j.gameserver.model.L2Effect;

public class EffectCurseDoom extends L2Effect {

    
    public EffectCurseDoom(Env env, EffectTemplate template) {
        super(env, template);
    }


    public EffectType getEffectType() {
        return L2Effect.EffectType.CURSE_DOOM;
    }

    public void onStart()
    {
        getEffected().startMuted();
        getEffected().disableAllSkills();
        onActionTime();
    }
    public void onExit()
    {
        getEffected().enableAllSkills();
    }
    public boolean onActionTime()
    {
       return true;
    }
 }
