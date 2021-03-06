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
package net.sf.l2j.gameserver.handler.skillhandlers;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.lib.Log;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.effects.EffectCharge;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.lib.Rnd;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.7.2.16 $ $Date: 2005/04/06 16:13:49 $
 */

public class Pdam implements ISkillHandler
{

    // all the items ids that this handler knowns
    private static Logger _log = Logger.getLogger(Pdam.class.getName());

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
     */
    private static SkillType[] _skillIds = {SkillType.PDAM, SkillType.DEATHLINK_PDAM
    /* SkillType.CHARGEDAM */
    };
    public static boolean RageType;
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
     */
    public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
    {
        if (activeChar.isAlikeDead()) return;

        int damage = 0;

        if (Config.DEBUG)
            if (Config.DEBUG) _log.fine("Begin Skill processing in Pdam.java " + skill.getSkillType());
        
        
           
        
        for (int index = 0; index < targets.length; index++)

        {
            L2Character target = (L2Character) targets[index];
            if(target.reflectSkill(skill))
            	target = activeChar;
            L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
            if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance
                && target.isAlikeDead() && target.isFakeDeath())
            {
                target.stopFakeDeath(null);
            }
            else if (target.isAlikeDead()) continue;


            boolean dual = activeChar.isUsingDualWeapon();
            boolean shld = Formulas.getInstance().calcShldUse(activeChar, target);
            
            boolean soul = (weapon != null && weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT);

            if (!L2Skill.CRIT_ATTACK) damage = 0;
            //_log.warning("CRIT ATTACK="+L2Skill.CRIT_ATTACK);
            if (L2Skill.CRIT_ATTACK)
            {
            	
                    damage = (int) Formulas.getInstance().calcPhysDam(activeChar, target, skill, shld, true, dual, soul);
            }
            else if (skill.getCondition() != 16 && skill.getCondition() != 17)
            	damage = (int) Formulas.getInstance().calcPhysDam(activeChar, target, skill, shld, false, dual, soul);
            
            if (skill.getSkillType() == SkillType.DEATHLINK_PDAM)   
            {   
                 double cur = 2*activeChar.getCurrentHp();   
                 double max = activeChar.getMaxHp();   
                 if (cur<max)   
                      damage *= Math.sqrt(max/cur);   
                 else damage *= Math.pow(max/cur, 4);   
            }
            if (damage > 5000 && activeChar instanceof L2PcInstance)
            {
                String name = "";
                if (target instanceof L2RaidBossInstance) name = "RaidBoss ";
                if (target instanceof L2NpcInstance)
                    name += target.getName() + "(" + ((L2NpcInstance) target).getTemplate().npcId
                        + ")";
                if (target instanceof L2PcInstance)
                    name = target.getName() + "(" + target.getObjectId() + ") ";
                name += target.getLevel() + " lvl";
                Log.add(activeChar.getName() + "(" + activeChar.getObjectId() + ") "
                    + activeChar.getLevel() + " lvl did damage " + damage + " with skill "
                    + skill.getName() + "(" + skill.getId() + ") to " + name, "damage_pdam");
            }

            if (soul && weapon != null) weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
            
            

            if (damage > 0)
            {
                if (soul && weapon != null) weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
                
                if (skill.isInstantKill())  
                  {  
                    int Ikill = activeChar.getLevel() - target.getLevel();
                    double CritC = Math.sqrt(activeChar.getLevel() - target.getLevel()) + activeChar.getDEX();
                    if (Rnd.get(100) <= CritC)
                    {
                        damage = damage*2;
                        if (activeChar instanceof L2PcInstance)
                        {

                            SystemMessage sm = new SystemMessage(614);
                            sm.addString("SYS");
                            sm.addString("�ޯ�P�R����");
                            activeChar.sendPacket(sm);
                        }
                       
                    }
                    if (Ikill < 1)
                        Ikill = 1;
                    
                            if(Rnd.get(100) <= Ikill)  
                             {  
                               activeChar.sendPacket(new SystemMessage(1668));  
                                           
                              if (target instanceof L2PcInstance)  
                             {  
                             //hp += skill.getPower()/*+(Math.sqrt(cLev)*cLev)+cLev*/; 
                             target.setCurrentCp(0); 
                             StatusUpdate sump = new StatusUpdate(target.getObjectId()); 
                             sump.addAttribute(StatusUpdate.CUR_CP, (int)target.getCurrentCp()); 
                             target.sendPacket(sump);  
                              }   
                              else if (!target.isRaid())  
                              {  
                                  target.reduceCurrentHp(target.getCurrentHp()-1, activeChar);  
                              }   
                              else if (!(target instanceof L2PcInstance))  
                              {  
                             
                              }  
                              } 
                                   
                       }               
                if (activeChar instanceof L2PcInstance)
                {
                    if (L2Skill.CRIT_ATTACK) activeChar.sendPacket(new SystemMessage(SystemMessage.CRITICAL_HIT));
                    
                    SystemMessage sm = new SystemMessage(SystemMessage.YOU_DID_S1_DMG);
                    sm.addNumber(damage);
                    activeChar.sendPacket(sm);
                }
                if (skill.hasEffects())
                {
                    // activate attacked effects, if any
                    target.stopEffect(skill.getId());
                    if (target.getEffect(skill.getId()) != null)
                        target.removeEffect(target.getEffect(skill.getId()));
                    if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, false, false, false) || skill.willHit())
                    {
                        
                        if (skill.getChargeNum()>0)
                        {
                            L2Skill charge = SkillTable.getInstance().getInfo(4271, skill.getChargeNum());
                            charge.getEffects(activeChar, activeChar);
                        }
                        else
                        {
                        skill.getEffects(activeChar, target);
                        
                        SystemMessage sm = new SystemMessage(SystemMessage.YOU_FEEL_S1_EFFECT);
                        sm.addSkillName(skill.getId());
                        target.sendPacket(sm);
                        }
                    }
                    else
                    {
                        SystemMessage sm = new SystemMessage(SystemMessage.S1_WAS_UNAFFECTED_BY_S2);
                        sm.addString(target.getName());
                        sm.addSkillName(skill.getDisplayId());
                        activeChar.sendPacket(sm);
                    }
                }

                 // Success of lethal effect
                int chance;
                if(!target.isRaid() && ( chance = Rnd.get(100)) < skill.getLethalChance1())
                {
                	// 1st lethal effect activate (cp to 1 or if target is npc then hp to 50%)
                	if(chance >= skill.getLethalChance2())
                	{
              		   if (target instanceof L2PcInstance) 
             		   {
               				L2PcInstance player = (L2PcInstance)target;
            				if (!player.isInvul())
            				{
            					player.setCurrentCp(1); // Set CP to 1
                           		player.reduceCurrentHp(damage, activeChar);
            				}
            		   }
             		   else if (target instanceof L2MonsterInstance) // If is a monster remove first damage and after 50% of current hp
             		   {
             			  target.reduceCurrentHp(damage, activeChar);
             			  target.reduceCurrentHp(target.getCurrentHp()/2, activeChar);
             		   }
                	}
                	else //2nd lethal effect activate (cp,hp to 1 or if target is npc then hp to 1)
                	{
                      	 // If is a monster damage is (CurrentHp - 1) so HP = 1
                        if (target instanceof L2NpcInstance)
                            target.reduceCurrentHp(target.getCurrentHp()-1, activeChar);
            			else if (target instanceof L2PcInstance) // If is a active player set his HP and CP to 1
            			{
            				L2PcInstance player = (L2PcInstance)target;
            				if (!player.isInvul())
            				{
            					player.setCurrentHp(1);
            					player.setCurrentCp(1);
            				}
            			}
                	}
                    // Lethal Strike was succefful!
                    activeChar.sendPacket(new SystemMessage(1668));
                }
                else
                {
                	// Make damage directly to HP
                	if(skill.getDmgDirectlyToHP())
                	{
        				if(target instanceof L2PcInstance)
        				{
        					L2PcInstance player = (L2PcInstance)target;
	                		if (!player.isInvul())
	        				{
	                	       if (damage >= player.getCurrentHp()) 
	                	       {
	                	    	   player.setCurrentHp(0);
	                	    	   player.doDie(activeChar);
	                	       }
	                	       else 
	                		      player.setCurrentHp(player.getCurrentHp() - damage);
	        				}
	                		
	                		SystemMessage smsg = new SystemMessage(SystemMessage.S1_GAVE_YOU_S2_DMG);
	                		smsg.addString(activeChar.getName());
	                		smsg.addNumber(damage);
	                		player.sendPacket(smsg);
	                		
        				}
        				else
        					target.reduceCurrentHp(damage, activeChar);
                	}
                	else
                	{
                		target.reduceCurrentHp(damage, activeChar);
                	}
                }

            }

            else // No - damage
            {
            	activeChar.sendPacket(new SystemMessage(SystemMessage.ATTACK_FAILED));
            }
            
            if (skill.getId() == 345 || skill.getId() == 346) // Sonic Rage or Raging Force
            {
                EffectCharge effect = (EffectCharge)activeChar.getEffect(L2Effect.EffectType.CHARGE);
                if (effect != null) 
                {
                    int effectcharge = effect.getLevel();
                    if (effectcharge < 7)
                    {
                        effectcharge++;
                        effect.addNumCharges(1);
                        activeChar.updateEffectIcons();
                        SystemMessage sm = new SystemMessage(SystemMessage.FORCE_INCREASED_TO_S1);
                        sm.addNumber(effectcharge);
                        activeChar.sendPacket(sm);
                    }
                    else
                    {
                        SystemMessage sm = new SystemMessage(SystemMessage.FORCE_MAXLEVEL_REACHED);
                        activeChar.sendPacket(sm);
                    }
                }
                else
                {
                    if (skill.getId() == 345) // Sonic Rage
                    {
                        L2Skill dummy = SkillTable.getInstance().getInfo(8, 7); // Lv7 Sonic Focus
                        dummy.getEffects(activeChar, activeChar);
                    }
                    else if (skill.getId() == 346) // Raging Force
                    {
                        L2Skill dummy = SkillTable.getInstance().getInfo(50, 7); // Lv7 Focused Force
                        dummy.getEffects(activeChar, activeChar);
                    }
                }
            }            
            //self Effect :]
            L2Effect effect = activeChar.getEffect(skill.getId());        
            if (effect != null && effect.isSelfEffect())        
            {            
            	//Replace old effect with new one.            
            	effect.exit();        
            }        
            skill.getEffectsSelf(activeChar);
        }
        
        if (skill.isSuicideAttack())
        {
        	activeChar.doDie(null);
        	activeChar.setCurrentHp(0);
        }
        L2Skill.CRIT_ATTACK =false;
    }

    public SkillType[] getSkillIds()
    {
        return _skillIds;
    }

}
