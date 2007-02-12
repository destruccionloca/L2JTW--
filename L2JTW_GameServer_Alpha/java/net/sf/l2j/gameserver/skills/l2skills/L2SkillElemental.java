package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.gameserver.skills.effects.EffectSeed;

public class L2SkillElemental extends L2Skill
{

    private final int num_seeds;
    private final int[] seeds;
    private final int[] seed_id;
    private final boolean seed_any;

    public L2SkillElemental(StatsSet set)
    {
        super(set);

        num_seeds = set.getInteger("num_seeds", 2);
        seeds = new int[3];
        seeds[0] = set.getInteger("seed1", 0);
        seeds[1] = set.getInteger("seed2", 0);
        seeds[2] = set.getInteger("seed3", 0);
        seed_id = new int[3];
        seed_id[0] = 1285;
        seed_id[1] = 1286;
        seed_id[2] = 1287;


        if (set.getInteger("seed_any", 0) == 1) seed_any = true;
        else seed_any = false;
    }

    public boolean checkCondition(L2Character activeChar)
    {
        if (activeChar instanceof L2PcInstance)
        {
            L2PcInstance player = (L2PcInstance) activeChar;
            {
                boolean charged = true;
                if (!seed_any)
                {
                    int num_seedes = 0;
                    for (int i = 0; i < seeds.length; i++)
                    {
                        if (seeds[i] != 0)
                        {
                            EffectSeed e = (EffectSeed) player.getEffect(seeds[i]);
                            if (e == null || !e.getInUse()) {
                                charged = false;
                                break;
                            }
                            num_seedes += e.num_seeds;
                            e.exit();  
                        }
                    }
                    if (num_seedes < this.num_seeds) 
                        charged = false;
                }
                else
                {
                    charged = false;
                    {
                        int num_seedes = 0;
                        for (int i = 0; i < seed_id.length; i++)
                        {
                            EffectSeed e = (EffectSeed) player.getEffect(seed_id[i]);
                            if (e != null && e.getInUse())
                            {
                                e.exit();
                                num_seedes += e.num_seeds;
                            }
                        }
                        if (num_seedes >= this.num_seeds) 
                            charged = true;
                    }
                }
                if (!charged)
                {
                    SystemMessage sm = new SystemMessage(614);
                    sm.addString("SYS");
                    sm.addString("你並無庸有施展所需要的元素.");
                    activeChar.sendPacket(sm);
                    return false;
                }
                
            }

        }
        return super.checkCondition(activeChar, false);
        

    }

    public void useSkill(L2Character activeChar, L2Object[] targets)
    {
        if (activeChar.isAlikeDead())
            return;
        
        boolean sps = false;
        boolean bss = false;
        
        boolean  spsUsed = false;
        
        L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
        
                if (weaponInst != null)
        {
            if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT) 
            {
                bss = true;
            }
            else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT) 
            {
                sps = true;
            }
        }

        

        for (int index = 0; index < targets.length; index++)
        {
            L2Character target = (L2Character) targets[index];
            if (target.isAlikeDead()) continue;

            boolean mcrit = Formulas.getInstance().calcMCrit(activeChar.getMCriticalHit(target, this));

            int damage = (int) Formulas.getInstance().calcMagicDam(activeChar, target, this, sps, bss,
                                                                   mcrit);

            spsUsed = true;
            target.reduceCurrentHp(damage, activeChar);

            if (activeChar instanceof L2PcInstance)
            {
                SystemMessage sm = new SystemMessage(SystemMessage.YOU_DID_S1_DMG);
                sm.addNumber(damage);
                activeChar.sendPacket(sm);
            }

            // activate attacked effects, if any
            target.stopEffect(this.getId());
            if (target.getEffect(this.getId()) != null)
                target.removeEffect(target.getEffect(this.getId()));
            this.getEffects(activeChar, target);
        }
        
        if (weaponInst != null)
        {
            if((sps || bss) && spsUsed)
                weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
        }
    }
}