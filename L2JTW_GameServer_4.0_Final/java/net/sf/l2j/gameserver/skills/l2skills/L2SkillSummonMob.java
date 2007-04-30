 package net.sf.l2j.gameserver.skills.l2skills;


import java.util.List;
import java.util.Random;

import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;

public class L2SkillSummonMob extends L2Skill {

	private int     npcId;
    private static final Random _rnd = new Random();
    private List<L2NpcInstance> _mobs;
    private final int     power;

	public L2SkillSummonMob(StatsSet set) 
    {
		super(set);
		
		npcId      = set.getInteger("npcId", 0);
        power     = set.getInteger("power",5);
	}


	
 public void useSkill(L2Character caster, L2Object[] targets) 
 {
     try
     {
      for (int i = 0; i < this.getPower(); i++) 
     {
        L2NpcTemplate npcTemplate = null;
        npcTemplate = NpcTable.getInstance().getTemplate(npcId);
        L2Spawn npcSpawn = new L2Spawn(npcTemplate);
				
        int point = _rnd.nextInt(100);
        int signX = (_rnd.nextInt(2) == 0) ? -1 : 1;
        int signY = (_rnd.nextInt(2) == 0) ? -1 : 1;
        
        npcSpawn.setLocx(caster.getX()+(point*signX));
        npcSpawn.setLocy(caster.getY()+(point*signY));
        npcSpawn.setLocz(caster.getZ());
        npcSpawn.stopRespawn();
        
        SpawnTable.getInstance().addNewSpawn(npcSpawn, false);
        _mobs.add(npcSpawn.doSpawn());
      }
     }
     catch (Exception e) 
     {
     _log.warning("RaidEngine: Error while spawning undead: " + e);
     }
        
        

	}

}
