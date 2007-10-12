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

/*
 * This file still under construction...
 * -------------------------------------
 * All AI process will process through this class instead of directly go through to L2AttackableAI
 * 
 * 
 * 
 * */


package net.sf.l2j.gameserver.ai;

import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import java.util.concurrent.Future;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.GeoData;
import net.sf.l2j.gameserver.Territory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.actor.instance.L2ChestInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FestivalMonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FriendlyMobInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GuardInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MinionInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MinionInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2BossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PenaltyMonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.model.actor.instance.L2RiftInvaderInstance;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.taskmanager.DecayTaskManager;
import net.sf.l2j.gameserver.model.L2NpcChatData;
import net.sf.l2j.util.Rnd;


import net.sf.l2j.gameserver.clientpackets.Say2;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.CreatureSay;


public class MultiAlgorithmSystem extends L2CharacterAI implements Runnable
{
	
    //protected static final Logger _log = Logger.getLogger(L2AttackableAI.class.getName());

    private static final int RANDOM_WALK_RATE = 30; // confirmed
    // private static final int MAX_DRIFT_RANGE = 300;
    private static final int MAX_ATTACK_TIMEOUT = 300; // int ticks, i.e. 30 seconds

    /** The L2Attackable AI task executed every 1s (call onEvtThink method)*/
    private Future _aiTask;

    /** The delay after wich the attacked is stopped */
    private int _attackTimeout;

    /** The L2Attackable aggro counter */
    private int _globalAggro;

    /** The flag used to indicate that a thinking action is in progress */
    private boolean _thinking; // to prevent recursive thinking

    private int enemyRange;
    
    private boolean canchat;
    private int npcchat_delay = 0;
    private int radius=0;
    private static final int RANDOM_BUFF_RATE = 20;
    
    
    public void run()
    {
        // Launch actions corresponding to the Event Think
        onEvtThink();

    }
    public MultiAlgorithmSystem(L2Character.AIAccessor accessor)
    {
        super(accessor);

        _attackTimeout = Integer.MAX_VALUE;
        _globalAggro = -10; // 10 seconds timeout of ATTACK after respawn
        enemyRange = ((L2Attackable) _actor).getEnemyRange();
    }

 private void AttackableAI()
 {

 	
 	
 	
 	 L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(((L2NpcInstance)_actor).getTemplate().npcId);
      //_log.warning("Chatdata Get:"+((L2NpcInstance)_actor).getTemplate().npcId);
     // _log.warning("TIME:"+GameTimeController.getGameTicks()+" NPCTIME:"+npcchat_delay);
      if (GameTimeController.getGameTicks() > npcchat_delay)
      for(L2NpcChatData chats: npcData.getChatData())
  	{
      	npcchat_delay=0;
      	if(Rnd.nextInt(100)>=chats.getChatChance())
      	{
      		_log.warning("fail");
      	continue;
      	}
      	
      	//condition check
      	//-------------------------------------------------
      	if (chats.getChatCondition1()==0)
      	{
      		canchat = true;
      	}
      	if (chats.getChatCondition1()==1)
      	{
      		double hppercent= _actor.getCurrentHp()/_actor.getMaxHp()*100;
  			if (hppercent<chats.getChatValue1())
  				canchat = true;
      	}
      	if (chats.getChatCondition1()==2)
      	{
      		double mppercent= _actor.getCurrentMp()/_actor.getMaxMp()*100;
  			if (mppercent<chats.getChatValue1())
  				canchat = true;
      	}
      	if (chats.getChatCondition1()==3)
      	{
      		
	        		if(_actor.getTarget() instanceof L2NpcInstance)
	        			canchat=true;
      	}
      	if (chats.getChatCondition1()==4)
      	{
      	
	        		if(_actor.getTarget() instanceof L2PcInstance)
	        			canchat=true;
      	}
      	_log.warning("check:"+canchat);
      	//condition where Dont chat
      	//-------------------------------------------------
      	if (chats.getChatCondition2()==1)
      	{
      		double hppercent= _actor.getCurrentHp()/_actor.getMaxHp()*100;
  			if (hppercent<chats.getChatValue2())
  				canchat = false;
      	}
      	if (chats.getChatCondition2()==2)
      	{
      		double mppercent= _actor.getCurrentMp()/_actor.getMaxMp()*100;
  			if (mppercent<chats.getChatValue2())
  				canchat = false;
      	}
      	if (chats.getChatCondition2()==3)
      	{
	        		if(_actor.getTarget() instanceof L2NpcInstance)
	        			canchat=false;
      	}
      	if (chats.getChatCondition2()==4)
      	{
	        		if(_actor.getTarget() instanceof L2PcInstance)
	        			canchat=false;
      	}
      	
      	
      	//-------------------------------------------------
  		if (canchat)
  		{
  			npcchat_delay=chats.getChatDelay()*10+GameTimeController.getGameTicks();
  			CreatureSay cs = new CreatureSay(_actor.getObjectId(), chats.getChatType(), ((L2NpcInstance)_actor).getTemplate().name, chats.getChatMemo());
  			 
  			if(chats.getChatType()==0)
  				radius=1000;
  			else if(chats.getChatType()==1||chats.getChatType()==10||chats.getChatType()==17)
  				radius=100000;
  			else
  				radius=1000;
  			
  			canchat=false;
  			for (L2Object obj : _actor.getKnownList().getKnownCharactersInRadius(radius))
               {
  				if(obj instanceof L2PcInstance)
  	            ((L2PcInstance)obj).sendPacket(cs);
               }
  	        break;
  		}

  	}
      
 	
 	
     if(_actor.isAttackingDisabled()) return;
     
     if (_attackTimeout < GameTimeController.getGameTicks())

     {
         // Check if the actor is running
         if (_actor.isRunning())
         {
             // Set the actor movement type to walk and send Server->Client packet ChangeMoveType to all others L2PcInstance
             _actor.setWalking();

             // Calculate a new attack timeout
             _attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
         }
     }

     // Check if target is dead or if timeout is expired to stop this attack
     if (getAttackTarget() == null || getAttackTarget().isAlikeDead()
         || _attackTimeout < GameTimeController.getGameTicks())
     {
         // Stop hating this target after the attack timeout or if target is dead
         if (getAttackTarget() != null)
         {
             L2Attackable npc = (L2Attackable) _actor;
             npc.stopHating(getAttackTarget());
         }

         // Set the AI Intention to AI_INTENTION_ACTIVE
         setIntention(AI_INTENTION_ACTIVE);

         _actor.setWalking();
     }
     else
     {


         // Call all L2Object of its Faction inside the Faction Range
         if (((L2NpcInstance) _actor).getFactionId() != null)
         {
             String faction_id = ((L2NpcInstance) _actor).getFactionId();

             // Go through all L2Object that belong to its faction
             for (L2Object obj : _actor.getKnownList().getKnownObjects().values())
             {
                 if (obj instanceof L2NpcInstance)
                 {
                     L2NpcInstance npc = (L2NpcInstance) obj;

                     if (npc == null || getAttackTarget() == null || faction_id != npc.getFactionId())
                         continue;

                     // Check if the L2Object is inside the Faction Range of the actor
                     if (_actor.isInsideRadius(npc, npc.getFactionRange(), true, false)
                         && GeoData.getInstance().canSeeTarget(_actor, npc)
                         && Math.abs(getAttackTarget().getZ() - npc.getZ()) < 600
                         && npc.getAI() != null
                         && _actor.getAttackByList().contains(getAttackTarget())
                         && (npc.getAI()._intention == CtrlIntention.AI_INTENTION_IDLE
                         || npc.getAI()._intention == CtrlIntention.AI_INTENTION_ACTIVE))
                     {
                         if (getAttackTarget() instanceof L2PcInstance
                             && getAttackTarget().isInParty()
                             && getAttackTarget().getParty().isInDimensionalRift())
                         {
                             byte riftType = getAttackTarget().getParty().getDimensionalRift().getType();
                             byte riftRoom = getAttackTarget().getParty().getDimensionalRift().getCurrentRoom();

                             if (_actor instanceof L2RiftInvaderInstance
                                 && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(npc.getX(), npc.getY(), npc.getZ()))
                                 continue;
                         }

                         // Notify the L2Object AI with EVT_AGGRESSION
                         npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, getAttackTarget(), 1);
                     }
                 }
             }
         }

         // Get all information needed to chose between physical or magical attack
         L2Skill[] skills = null;
         double dist2 = 0;
         int range = 0;

         try
         {
             _actor.setTarget(getAttackTarget());
             skills = _actor.getAllSkills();
             dist2 = Math.sqrt(_actor.getPlanDistanceSq(getAttackTarget().getX(), getAttackTarget().getY()));
             range = _actor.getPhysicalAttackRange();
         }
         catch (NullPointerException e)
         {
             //_log.warning("AttackableAI: Attack target is NULL.");
             setIntention(AI_INTENTION_ACTIVE);
             return;
         }

         L2Weapon weapon = _actor.getActiveWeaponItem();
         
         if ((weapon != null && weapon.getItemType() == L2WeaponType.BOW)|| ((L2Attackable)_actor).getCanDodge() == 1)
         {
             // Micht: kepping this one otherwise we should do 2 sqrt
             double distance2 = _actor.getPlanDistanceSq(getAttackTarget().getX(), getAttackTarget().getY());
             if (distance2 <= 3600)
             {
             	//Diasable the RND for increasing the performance
                 //int chance = 60;
                 //if (chance >= Rnd.get(100))
                 //{
                     int posX = _actor.getX();
                     int posY = _actor.getY();
                     int posZ = _actor.getZ();
                     /*      
                     if (Rnd.get(1)>0)
                     posX=((L2Attackable)_actor).getSpawn().getLocx()+Rnd.get(100);
                     else
                     posX=((L2Attackable)_actor).getSpawn().getLocx()-Rnd.get(100);

                     if (Rnd.get(1)>0)
                     posY=((L2Attackable)_actor).getSpawn().getLocy() + Rnd.get(100);
                     else
                     posY=((L2Attackable)_actor).getSpawn().getLocy()-Rnd.get(100);

                     setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(posX, posY, posZ, 0));
                     */
                     if (Rnd.get(1)>0)
                         posX=posX+Rnd.get(100);
                         else
                         posX=posX-Rnd.get(100);

                         if (Rnd.get(1)>0)
                         posY=posY + Rnd.get(100);
                         else
                         posY=posY - Rnd.get(100);
                         
                         setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(posX, posY, posZ, 0));
                 //}
             }
         }

         // Force mobs to attack anybody if confused
         L2Character hated;
         if (_actor.isConfused()) hated = getAttackTarget();
         else hated = ((L2Attackable) _actor).getMostHated();

         if (hated == null)
         {
             setIntention(AI_INTENTION_ACTIVE);
             return;
         }
         if (hated != getAttackTarget())
         {
             setAttackTarget(hated);
         }
         // We should calculate new distance cuz mob can have changed the target

         dist2 = Math.sqrt(_actor.getPlanDistanceSq(getAttackTarget().getX(), getAttackTarget().getY()));
         
         
         if (hated.isMoving()) range += 100;  

         //---------------------------------------------------------------------
         // Prevent Stack
         /*
         
         for (L2Object nearby : _actor.getKnownList().getKnownCharactersInRadius(40))

         {

         {
         	
         	if(nearby instanceof L2Character)
         	{
         		_log.warning("AttackableAI: Too many nearby subject...move near..in Radius");
                 int posX = _actor.getX();
                 int posY = _actor.getY();
                 int posZ = _actor.getZ();
         	
         		if (Rnd.get(2)>1)
                     posX=hated.getX()+Rnd.get(50);
                     else
                     posX=hated.getX()-Rnd.get(50);



                     if (Rnd.get(2)>1)
                     posY=hated.getY() + Rnd.get(50);
                     else
                     posY=hated.getY()-Rnd.get(50);



                     moveTo(posX, posY, posZ);
                     return;
         	}

         }

    		 
         }
         */
         //----------------------------------------------------------------------------------
         // AI Active - Process
         // AI System
         
         
         // Primary Attack - Skill
         if (((L2Attackable) _actor).getPrimaryAttack()>0)

         {

         	//Skill Chance
         	if (Rnd.nextInt(100)<=((L2Attackable) _actor).getSkillChance())
         	{
         		onSkillCast(_actor);
         		return;
         	}
         	
         	if(dist2 >= range)
         	{
         		if (_actor.isMovementDisabled())
         		{
         			TargetReconsider(_actor);
         			return;
         		}


                 if (hated.isMoving()) range -= 100; if (range < 5) range = 5; 
     			//_accessor.moveTo(hated.getX(), hated.getY(), hated.getZ());	
                 moveToPawn(getAttackTarget(), range);
                 return;
         		
         		
         	}
         	
         	if(((L2Attackable) _actor).getPrimaryAttack()==1)
         	{
         		onSkillCast(_actor);
         		return;
         	}
         	else
         	{
         		for (L2Skill sk : skills)

                 {


         			if (((L2Attackable) _actor).getPrimaryAttack()==sk.getId())
         			{
         				
         				
         				
         				 if (sk.getSkillType() == L2Skill.SkillType.RESURRECT && ((_actor instanceof L2Attackable)))
         	                {
         	                    
         	                    if(_actor instanceof L2MinionInstance)
         	                    {
         	                      
         	                        if(((L2MinionInstance) _actor).getLeader().isDead()&&GeoData.getInstance().canSeeTarget(_actor, ((L2MinionInstance) _actor).getLeader()))
         	                        
         	                            {
         	                            DecayTaskManager.getInstance().cancelDecayTask((((L2MinionInstance) _actor).getLeader()));
 	                                    clientStopMoving(null);
 	                                    _actor.setTarget(((L2MinionInstance) _actor).getLeader());
 	                                    _accessor.doCast(sk);
 	                                    return;
         	                            }
         	                       
         	                        
         	                    }
         	                    
         	                    
         	                    
         	                    for (L2Object obj : _actor.getKnownList().getKnownCharactersInRadius(sk.getCastRange()))// should be looking for faction_id though
         	                    {
         	                      
         	                    if (((obj instanceof L2Attackable && ((L2Attackable) obj).isDead() && obj != _actor)
         	                            ||(obj instanceof L2MinionInstance && ((L2MinionInstance) obj).isDead()))&&GeoData.getInstance().canSeeTarget(_actor, obj))
         	                    {

         	                        
         	                        if (obj instanceof L2MinionInstance)
         	                        {
         	                            if((((L2MinionInstance) _actor).getNpcId() == ((L2MinionInstance) obj).getNpcId()
         	                                 ||(((L2MinionInstance) _actor).getFactionId() == ((L2MinionInstance) obj).getFactionId()))&&((L2MinionInstance) obj).isDead())
         	                            {
         	                                DecayTaskManager.getInstance().cancelDecayTask((L2Attackable)obj);
     	                                    clientStopMoving(null);
     	                                    _actor.setTarget(obj);
     	                                    _accessor.doCast(sk);
     	                                    return;
         	                            }
         	                            
         	                        }
         	                        if ((((L2Attackable) _actor).getFactionId() == ((L2Attackable) obj).getFactionId()) && ((L2Attackable) obj).isDead())                            
         	                        {
         	                            DecayTaskManager.getInstance().cancelDecayTask((L2Attackable)obj);
 	                                    clientStopMoving(null);
 	                                    _actor.setTarget(obj);
 	                                    _accessor.doCast(sk);
 	                                    return;
         	                        
         	                        }
         	                    }
         	                    }
         	                                       
         	                    return;
         	                    
         	                    
         	                }
         	                
         	                if (sk.getSkillType() == L2Skill.SkillType.HEAL && ((_actor instanceof L2Attackable) || (_actor instanceof L2MinionInstance)))
         	                {
         	                   
         	                       
         	                        if(_actor instanceof L2MinionInstance)
         	                        {
         	                          
         	                            if((((L2MinionInstance) _actor).getLeader().getCurrentHp()<(((L2MinionInstance) _actor).getLeader().getMaxHp())
         	                                    && !((L2MinionInstance) _actor).getLeader().isDead() && Rnd.get(100)<=66)&&GeoData.getInstance().canSeeTarget(_actor, ((L2MinionInstance) _actor).getLeader()))
         	                            
         	                                {
     	                                    clientStopMoving(null);
     	                                    _actor.setTarget(((L2MinionInstance) _actor).getLeader());
     	                                    _accessor.doCast(sk);
     	                                    return;
         	                                }
         	                            
         	                            
         	                        }
         	                    for (L2Object obj : _actor.getKnownList().getKnownCharactersInRadius(sk.getCastRange()))// should be looking for faction_id though
         	                    {
         	                      
         	                    if (((obj instanceof L2Attackable && !((L2Attackable) obj).isDead() && obj != _actor)
         	                            ||(obj instanceof L2MinionInstance && !((L2MinionInstance) obj).isDead()))&&GeoData.getInstance().canSeeTarget(_actor, obj))
         	                    {

         	                        
         	                        if (obj instanceof L2MinionInstance)
         	                        {
         	                            if(((L2Attackable) _actor).getNpcId() == ((L2Attackable) obj).getNpcId()
         	                                 ||(((L2Attackable) _actor).getFactionId() == ((L2Attackable) obj).getFactionId())   )
         	                            if (((L2MinionInstance) _actor).getCurrentHp() > (((L2Attackable) obj).getCurrentHp()))
         	                            {
     	                                    clientStopMoving(null);
     	                                    _actor.setTarget(obj);
     	                                    _accessor.doCast(sk);
     	                                    return;
         	                               
         	                            }
         	                        }
         	                        if ((((L2Attackable) _actor).getFactionId() == ((L2Attackable) obj).getFactionId()))                            
         	                        {
         	                            //AI should always try to save the dying one.......
         	                           

         	                        if(((L2Attackable) _actor).getCurrentHp() > (((L2Attackable) obj).getCurrentHp()))
         	                        {
 	                                    clientStopMoving(null);
 	                                    _actor.setTarget(obj);
 	                                    _accessor.doCast(sk);
 	                                    return;
         	                        }
         	                        }
         	                    }
         	                    }
         	                    if  (((L2Attackable) _actor).getCurrentHp() < (((L2Attackable) _actor).getMaxHp()*0.8))
         	                    {
         	                       
	                                    clientStopMoving(null);
	                                    _actor.setTarget(_actor);
	                                    _accessor.doCast(sk);
	                                    return;
         	                    }
         	                    
         	                    return;
         	                
         	                }
         	                
         	                if (sk.getSkillType() == L2Skill.SkillType.BUFF && ((_actor instanceof L2Attackable)||(_actor instanceof L2MinionInstance)))
         	                    {                                                                     
         	                    // monsters should first look for a possible target to heal or buff
         	                   
         	                    
         	                        for (L2Object obj : _actor.getKnownList().getKnownCharactersInRadius(sk.getCastRange()))// should be looking for faction_id though
         	                        {
         	                            
         	                            if((obj instanceof L2Attackable|| obj instanceof L2MinionInstance)&&GeoData.getInstance().canSeeTarget(_actor, obj))
         	                            {
         	                                if (!((L2Attackable)obj).isDead())
         	                                {
         	                                if(obj instanceof L2MinionInstance&& _actor instanceof L2MinionInstance)
         	                                {
         	                                if(((L2MinionInstance) _actor).getLeader() == obj 
         	                                        || ((L2MinionInstance) _actor).getNpcId() == ((L2MinionInstance) obj).getNpcId()
         	                                        || ((L2MinionInstance) _actor).getFactionId() == ((L2MinionInstance) obj).getFactionId())
         	                                {
         	                                    if (Rnd.get(100) >= 50)
         	                                    {
         	                                        if (sk.getSkillType() == L2Skill.SkillType.BUFF)
         	                                        {
         	                                            L2Effect[] effects = ((L2Attackable)obj).getAllEffects();
         	                                            for (int i = 0; effects != null && i < effects.length; i++)
         	                                            {
         	                                                L2Effect effect = effects[i];
         	                                                if (effect.getSkill() == sk)
         	                                                {
         	                                                    return;
         	                                                }
         	                                            }
         	                                        }
             	                                    clientStopMoving(null);
             	                                    _actor.setTarget(obj);
             	                                    _accessor.doCast(sk);
             	                                    return;
         	                                    }
         	                                }
         	                                }
         	                            if (((L2Attackable) _actor).getFactionId() == ((L2Attackable) obj).getFactionId()&&GeoData.getInstance().canSeeTarget(_actor, obj))
         	                            {
         	                                if (Rnd.get(100) >= 50)
         	                                {
         	                                    if (sk.getSkillType() == L2Skill.SkillType.BUFF)
         	                                    {
         	                                        L2Effect[] effects = ((L2Attackable)obj).getAllEffects();
         	                                        for (int i = 0; effects != null && i < effects.length; i++)
         	                                        {
         	                                            L2Effect effect = effects[i];
         	                                            if (effect.getSkill() == sk)
         	                                            {
         	                                                return;
         	                                            }
         	                                        }
         	                                    }
         	                                    clientStopMoving(null);
         	                                    _actor.setTarget(obj);
         	                                    _accessor.doCast(sk);
         	                                    return;
         	                                }
         	                            }
         	                            }
         	                            }
         	                                
         	                        }
         	                        if (sk.getSkillType() == L2Skill.SkillType.BUFF)
         	                        {
         	                            L2Effect[] effects = ((L2Attackable)_actor).getAllEffects();
         	                            for (int i = 0; effects != null && i < effects.length; i++)
         	                            {
         	                                L2Effect effect = effects[i];
         	                                if (effect.getSkill() == sk)
         	                                {
         	                                    return;
         	                                }
         	                            }
         	                        }
         	                         clientStopMoving(null);
         	                        _actor.setTarget(_actor);
         	                        _accessor.doCast(sk);
         	                    }
         	                if(!GeoData.getInstance().canSeeTarget(_actor, hated))
         	                {
         	                	moveToPawn(hated, range);
         	                	return;
         	                }
                      clientStopMoving(null);
                     _actor.setTarget(hated);
                     _accessor.doCast(sk);                       
                     return;
         				
         			}
         			else
         			onSkillCast(_actor);
         			return;


                 }
         	}


         	
         }
         
         
         //-------------------------------------------------------------
         // Melee ...Etc.... 
         
         //_log.warning("AttackableAI: MELEE CHECK Range:"+range+" Dist:"+dist2); 
         
     	if (Rnd.nextInt(100)<=((L2Attackable) _actor).getSkillChance())
     	{
     		//_log.warning("AttackableAI: Skill Chance True");
     		onSkillCast(_actor);
     		return;
     	}
     	else if (hated.isMoving() && range+40 >= dist2)
     	{
     		
         //-------------------------------------------------------
     	//Geo Los Check
         if(!GeoData.getInstance().canSeeTarget(_actor, hated))
         {
         	moveToPawn(hated, range);
         	return;
         }
         //-------------------------------------------------------
         
     		//_log.warning("AttackableAI: Chasing Attack");      		
     		setAttackTarget(hated);
     		_accessor.doAttack(hated);     	 		
     		
     		
     	}
     	else if (range+20 >= dist2)
     	{
     		
             //-------------------------------------------------------
         	//Geo Los Check
             if(!GeoData.getInstance().canSeeTarget(_actor, hated))
             {
             	moveToPawn(hated, range);
             	return;
             }
             //-------------------------------------------------------
     		//_log.warning("AttackableAI: Normal Attack");      		
     		setAttackTarget(hated);
     		_accessor.doAttack(hated);     	 		
     		
     		
     	}
     	else if(dist2 >= range)
     	{
     		if (_actor.isMovementDisabled())
     		{
     			TargetReconsider(_actor);
     			return;
     		}
             if (hated.isMoving()) range -= 100; if (range < 5) range = 5; 
 			//_accessor.moveTo(hated.getX(), hated.getY(), hated.getZ());	
             moveToPawn(getAttackTarget(), range);
             return;
     		
     		
     	}
         
         
         

     }
 }
 
 //-------------------------------------------------------------------------------
 //Skill Cast AI System
 public void onSkillCast(L2Character caster)
 {
 	
 	
 	 // Get all information needed to chose between physical or magical attack
     L2Skill[] skills = null;
     double distTarget = 0;
     int attackrange = 0;
     boolean targetInRange = false;
     L2Character MostHate = ((L2Attackable) _actor).getMostHated();
     
     
     try
     {
         skills = caster.getAllSkills();
         distTarget = Math.sqrt(caster.getPlanDistanceSq(MostHate.getX(), MostHate.getY()));
         attackrange = _actor.getPhysicalAttackRange();
     }
     catch (NullPointerException e)
     {
         //_log.warning("AttackableAI: Attack target is NULL.");
         setIntention(AI_INTENTION_ACTIVE);
         return;
     }
 	//-----------------------------------------------------------------
     //Skill List Activate
    // _log.warning("AttackableAI: onSkillCast");
     if (skills != null)
 	for (L2Skill sk : skills)
     {
 		//_log.warning("AttackableAI: CheckSkill");
 		// If the skill is magic.. and caster is muted...then search for next skill...
 		if (sk.isMagic()&& _actor.isMuted())
 			continue;
 		//_log.warning("AttackableAI: MuteCheck");
 		// If skill is not prepare..which is not enough mp nor Skill cannot reuse...

 		if (_actor.isSkillDisabled(sk.getId())
         || _actor.getCurrentMp() <= _actor.getStat().getMpConsume(sk)
         || sk.isPassive())
 		{
     		//_log.warning(""+_actor.isSkillDisabled(sk.getId()));
     		//_log.warning(""+_actor.getCurrentMp()+" "+_actor.getStat().getMpConsume(sk));
     		//_log.warning(""+sk.isPassive());
 			continue;
 		}
 		if (sk.getCastRange()>=distTarget)
 		{
 			targetInRange=true;
 		}
 		//--------------------------------------------------------------------
 		//Positive Skill 
 		//_log.warning("AttackableAI: Skill Pass through");
			 if (sk.getSkillType() == L2Skill.SkillType.RESURRECT && ((_actor instanceof L2Attackable)))
          {
              
              if(_actor instanceof L2MinionInstance)
              {
                
                  if(((L2MinionInstance) _actor).getLeader().isDead() && GeoData.getInstance().canSeeTarget(_actor, ((L2MinionInstance) _actor).getLeader()))
                  
                      {
                      DecayTaskManager.getInstance().cancelDecayTask((((L2MinionInstance) _actor).getLeader()));
                      clientStopMoving(null);
                      _actor.setTarget(((L2MinionInstance) _actor).getLeader());
                      _accessor.doCast(sk);
                      return;
                      }
                 
                  
              }
              
              
              
              for (L2Object obj : _actor.getKnownList().getKnownCharactersInRadius(sk.getCastRange()))// should be looking for faction_id though
              {
                
              if (((obj instanceof L2Attackable && ((L2Attackable) obj).isDead() && obj != _actor)
                      ||(obj instanceof L2MinionInstance && ((L2MinionInstance) obj).isDead()))&&GeoData.getInstance().canSeeTarget(_actor, obj))
              {

                  
                  if (obj instanceof L2MinionInstance)
                  {
                      if((((L2MinionInstance) _actor).getNpcId() == ((L2MinionInstance) obj).getNpcId()
                           ||(((L2MinionInstance) _actor).getFactionId() == ((L2MinionInstance) obj).getFactionId()))&&((L2MinionInstance) obj).isDead())
                      {
                          DecayTaskManager.getInstance().cancelDecayTask((L2Attackable)obj);
                          clientStopMoving(null);
                          _actor.setTarget(obj);
                          _accessor.doCast(sk);
                          return;
                      }
                      
                  }
                  if ((((L2Attackable) _actor).getFactionId() == ((L2Attackable) obj).getFactionId()) && ((L2Attackable) obj).isDead())                            
                  {
                      DecayTaskManager.getInstance().cancelDecayTask((L2Attackable)obj);
                      clientStopMoving(null);
                      _actor.setTarget(obj);
                      _accessor.doCast(sk);
                      return;
                  
                  }
              }
              }
                                 
              continue;
              
              
          }
			 
          if (sk.getSkillType() == L2Skill.SkillType.HEAL && ((_actor instanceof L2Attackable) || (_actor instanceof L2MinionInstance)))
          {

                 
                  if(_actor instanceof L2MinionInstance)
                  {
                    
                      if(((L2MinionInstance) _actor).getLeader().getCurrentHp()<(((L2MinionInstance) _actor).getLeader().getMaxHp())
                              && !((L2MinionInstance) _actor).getLeader().isDead() && Rnd.get(100)<=66 
                              &&GeoData.getInstance().canSeeTarget(_actor, ((L2MinionInstance) _actor).getLeader()))
                      
                          {
                          clientStopMoving(null);
                          _actor.setTarget(((L2MinionInstance) _actor).getLeader());
                          _accessor.doCast(sk);
                          return;
                          }
                      
                      
                  }
              for (L2Object obj : _actor.getKnownList().getKnownCharactersInRadius(sk.getCastRange()))// should be looking for faction_id though
              {
                
              if (((obj instanceof L2Attackable && !((L2Attackable) obj).isDead() && obj != _actor)
                      ||(obj instanceof L2MinionInstance && !((L2MinionInstance) obj).isDead()))&&GeoData.getInstance().canSeeTarget(_actor, obj))
              {

                  
                  if (obj instanceof L2MinionInstance)
                  {
                      if(((L2Attackable) _actor).getNpcId() == ((L2Attackable) obj).getNpcId()
                           ||(((L2Attackable) _actor).getFactionId() == ((L2Attackable) obj).getFactionId())   )
                      if (((L2MinionInstance) _actor).getCurrentHp() > (((L2Attackable) obj).getCurrentHp()))
                      {
                          clientStopMoving(null);
                          _actor.setTarget(obj);
                          _accessor.doCast(sk);
                          return;
                         
                      }
                  }
                  if ((((L2Attackable) _actor).getFactionId() == ((L2Attackable) obj).getFactionId()))                            
                  {
                      //AI should always try to save the dying one.......
                     

                  if(((L2Attackable) _actor).getCurrentHp() > (((L2Attackable) obj).getCurrentHp()))
                  {
                      clientStopMoving(null);
                      _actor.setTarget(obj);
                      _accessor.doCast(sk);
                      return;
                  }
                  }
              }
              }
              if  (((L2Attackable) _actor).getCurrentHp() < (((L2Attackable) _actor).getMaxHp()*0.8))
              {
                 
                  clientStopMoving(null);
                  _actor.setTarget(_actor);
                  _accessor.doCast(sk);
                  return;
              }
              
              continue;
          
          }
          
          if (sk.getSkillType() == L2Skill.SkillType.BUFF && ((_actor instanceof L2Attackable)||(_actor instanceof L2MinionInstance)))
              {                                                                     
              // monsters should first look for a possible target to heal or buff
             
         	     boolean canbuff=true;
                  for (L2Object obj : _actor.getKnownList().getKnownCharactersInRadius(sk.getCastRange()))// should be looking for faction_id though
                  {
                      
                      if((obj instanceof L2Attackable|| obj instanceof L2MinionInstance) &&GeoData.getInstance().canSeeTarget(_actor, obj))
                      {
                          if (!((L2Attackable)obj).isDead())
                          {
                          if(obj instanceof L2MinionInstance&& _actor instanceof L2MinionInstance)
                          {
                          if(((L2MinionInstance) _actor).getLeader() == obj 
                                  || ((L2MinionInstance) _actor).getNpcId() == ((L2MinionInstance) obj).getNpcId()
                                  || ((L2MinionInstance) _actor).getFactionId() == ((L2MinionInstance) obj).getFactionId())
                          {
                              if (Rnd.get(100) >= 50)
                              {
                                  if (sk.getSkillType() == L2Skill.SkillType.BUFF)
                                  {
                                      L2Effect[] effects = ((L2Attackable)obj).getAllEffects();
                                      for (int i = 0; effects != null && i < effects.length; i++)
                                      {
                                          L2Effect effect = effects[i];
                                          if (effect.getSkill() == sk)
                                          {
                                         	 break;
                                          }
                                      }
                                  }
	                                    clientStopMoving(null);
	                                    _actor.setTarget(obj);
	                                    _accessor.doCast(sk);
	                                    return;
                              }
                          }
                          }
                      if (((L2Attackable) _actor).getFactionId() == ((L2Attackable) obj).getFactionId())
                      {
                          if (Rnd.get(100) >= 50)
                          {
                              if (sk.getSkillType() == L2Skill.SkillType.BUFF)
                              {
                                  L2Effect[] effects = ((L2Attackable)obj).getAllEffects();
                                  for (int i = 0; effects != null && i < effects.length; i++)
                                  {
                                      L2Effect effect = effects[i];
                                      if (effect.getSkill() == sk)
                                      {
                                     	 break;
                                      }
                                  }
                              }
                              clientStopMoving(null);
                              _actor.setTarget(obj);
                              _accessor.doCast(sk);
                              return;
                          }
                      }
                      }
                      }
                          
                  }
                  if (sk.getSkillType() == L2Skill.SkillType.BUFF)
                  {
                      L2Effect[] effects = ((L2Attackable)_actor).getAllEffects();
                      for (int i = 0; effects != null && i < effects.length; i++)
                      {
                          L2Effect effect = effects[i];
                          if (effect.getSkill() == sk)
                          {
                         	 canbuff = false;
                         	 break;
                          }
                      }
                  }
                  if (canbuff)
                  {
                   clientStopMoving(null);
                  _actor.setTarget(_actor);
                  _accessor.doCast(sk);
                  return;
                  }
                  else continue;
              }
 		
 	//---------------------------------------------------------------------------------
     //Debuff
          
          if (((sk.getSkillType() == L2Skill.SkillType.DEBUFF||sk.getSkillType() == L2Skill.SkillType.MUTE)
         		 && ((sk.getCastRange()>= distTarget || (MostHate.isMoving() && sk.getCastRange()*3>= distTarget))
         				 || !(sk.getTargetType() == SkillTargetType.TARGET_ONE)))&&GeoData.getInstance().canSeeTarget(_actor,MostHate))
          {
         	// _log.warning("1");
         	 boolean candebuff=true;
              L2Effect[] effects = (MostHate).getAllEffects();
              if(sk.getTargetType() == SkillTargetType.TARGET_ONE)
              for (int i = 0; effects != null && i < effects.length; i++)
              {
                  L2Effect effect = effects[i];
                  if (effect.getSkill() == sk)
                  {
                 	 candebuff=false;
                 	 break;
                  }
              }
             // _log.warning("2");
              if(candebuff)
              {
             //	 _log.warning("3");
              clientStopMoving(null);
              _actor.setTarget(MostHate);
              _accessor.doCast(sk);
              return;
              }
              else continue;
          }
 	
      //--------------------------------------------------------------------------------
      // Sleep Type
      // Only cast when target is moving or out of attack range or random...probably =_=
          if (((sk.getSkillType() == L2Skill.SkillType.SLEEP)&& ((sk.getCastRange()>= distTarget || (MostHate.isMoving() && sk.getCastRange()*3>= distTarget))
 				 || !(sk.getTargetType() == SkillTargetType.TARGET_ONE)))&&GeoData.getInstance().canSeeTarget(_actor, MostHate))
          {
         	 boolean cancast=true;
         	 if(MostHate.isMoving()||caster.getPhysicalAttackRange()<distTarget || Rnd.nextInt(5)>1)
         	 {
                   clientStopMoving(null);
                  _actor.setTarget(MostHate);
                  _accessor.doCast(sk);
         	 }
         	 else
         	 for (L2Object obj : _actor.getKnownList().getKnownCharactersInRadius(sk.getCastRange()))
              {
         		 
         		 
         		 if(((L2Attackable)caster).getHating((L2Character)obj)>0 && obj != caster)
         		 {
         			 L2Effect[] effects = ((L2Character)obj).getAllEffects();
                      for (int i = 0; effects != null && i < effects.length; i++)
                      {
                          L2Effect effect = effects[i];
                          if (effect.getSkill() == sk)
                          {
                         	 cancast=false;
                         	 break;
                          }
                      }
                      if(cancast)
                      {
                      clientStopMoving(null);
                      _actor.setTarget(obj);
                      _accessor.doCast(sk);
                      return;
                      }
                      else break;
         		 }
         		 
         		 
              }
         	 continue;
         	 
         	 
         	 
         	 
         	 
         	 
          }
          
        //------------------------------------------------------------------------
        //Paralyze Root Stun Bleed Dot Poison Skill Type
          
          if (((sk.getSkillType() == L2Skill.SkillType.PARALYZE 
         		 || sk.getSkillType() == L2Skill.SkillType.ROOT
         		 || sk.getSkillType() == L2Skill.SkillType.STUN
         		 || sk.getSkillType() == L2Skill.SkillType.BLEED
         		 || sk.getSkillType() == L2Skill.SkillType.POISON
         		 || sk.getSkillType() == L2Skill.SkillType.DOT)
         		 && ((sk.getCastRange()>= distTarget || (MostHate.isMoving() && sk.getCastRange()*3>= distTarget))
         				 || !(sk.getTargetType() == SkillTargetType.TARGET_ONE)))&&GeoData.getInstance().canSeeTarget(_actor, MostHate))
          {
         	 boolean cancast=true;
         	 
 			 L2Effect[] effects = (MostHate).getAllEffects();
              for (int i = 0; effects != null && i < effects.length; i++)
              {
                  L2Effect effect = effects[i];
                  if (effect.getSkill() == sk)
                  {
                 	 cancast=false;
                 	 break;
                  }
              }
              if(cancast)
              {
              clientStopMoving(null);
              _actor.setTarget(MostHate);
              _accessor.doCast(sk);
              return;
              }
              else
              
             	 for (L2Object obj : _actor.getKnownList().getKnownCharactersInRadius(sk.getCastRange()))
                  {
             		 
             		 
             		 if(((L2Attackable)caster).getHating((L2Character)obj)>0 && obj != caster)
             		 {
             			 L2Effect[] Teffects = ((L2Character)obj).getAllEffects();
                          for (int i = 0; Teffects != null && i < effects.length; i++)
                          {
                              L2Effect Geffect = effects[i];
                              if (Geffect.getSkill() == sk)
                              {
                             	 cancast=false;
                             	 break;
                              }
                          }
                          if(cancast)
                          {
                          clientStopMoving(null);
                          _actor.setTarget(obj);
                          _accessor.doCast(sk);
                          return;
                          }
                          else break;
             		 }
             		 
             		 
                  }
              
         	 
         	
         	 continue;
         	 
         	 
         	 
         	 
         	 
          }
 		
 		//---------------------------------------------------------------------
         // Rest of the Skill that can be use...
         
 		if(((sk.getCastRange()>= distTarget)&& ((sk.getCastRange()>= distTarget || (MostHate.isMoving() && sk.getCastRange()*3>= distTarget))
				 || !(sk.getTargetType() == SkillTargetType.TARGET_ONE)))&&GeoData.getInstance().canSeeTarget(_actor, MostHate))
 		{
             clientStopMoving(null);
             _actor.setTarget(MostHate);
             _accessor.doCast(sk);
             return;
 		}
 		
 		
 		
 		
 
 		
 		
     }
 	
     if (_actor.isMovementDisabled())
     {
 	if (!targetInRange)
 	{
 		TargetReset();
 	    return;
 	}
     }
     else if (attackrange<distTarget)
     {
     if (MostHate.isMoving()) attackrange -= 100; if (attackrange < 5) attackrange = 5; 
		//_accessor.moveTo(MostHate.getX(), MostHate.getY(), MostHate.getZ());	
     moveToPawn(getAttackTarget(), attackrange);
     return;
     }
 	return;
 }
 
 public void TargetReconsider(L2Character attacker)
 {
     L2Skill[] skills = null;
     boolean targetInRange=false;
     boolean cancast=true;
     L2Character MostHate = ((L2Attackable) _actor).getMostHated();

     double distTarget=0;
     
     try
     {
         skills = attacker.getAllSkills();
         distTarget = Math.sqrt(attacker.getPlanDistanceSq(MostHate.getX(), MostHate.getY()));
     }
     catch (NullPointerException e)
     {
         //_log.warning("AttackableAI: Attack target is NULL.");
         setIntention(AI_INTENTION_ACTIVE);
         return;
     }
     if(skills!=null)
     for(L2Skill sk:skills)
     {
 		if (sk.isMagic()&& attacker.isMuted())
 			continue;
 		
 		if(!GeoData.getInstance().canSeeTarget(_actor, MostHate))
 			return;
 		// If skill is not prepare..which is not enough mp nor Skill cannot reuse...
 		if (!_actor.isSkillDisabled(sk.getId())
         || _actor.getCurrentMp() >= _actor.getStat().getMpConsume(sk)
         || !sk.isPassive())
 		{
 			continue;
 		}
 		if (sk.getCastRange()>=distTarget)
 		{
 			targetInRange=true;
 		}
     	if((((sk.getSkillType()==L2Skill.SkillType.ROOT
     			||sk.getSkillType()==L2Skill.SkillType.SLEEP
     			||sk.getSkillType()==L2Skill.SkillType.PARALYZE
     			||sk.getSkillType()==L2Skill.SkillType.STUN
     			||sk.getSkillType()==L2Skill.SkillType.MUTE)&& ((sk.getCastRange()>= distTarget || (MostHate.isMoving() && sk.getCastRange()*3>= distTarget))
        				 || !(sk.getTargetType() == SkillTargetType.TARGET_ONE)))))
     	{
			 L2Effect[] effects = (MostHate).getAllEffects();
          for (int i = 0; effects != null && i < effects.length; i++)
          {
              L2Effect effect = effects[i];
              if (effect.getSkill() == sk)
              {
             	 cancast=false;
             	 break;
              }
          }
          if(cancast)
          {
          clientStopMoving(null);
          _actor.setTarget(MostHate);
          _accessor.doCast(sk);
          return;
          }
     	}
     }
     if (cancast)
     {
     	onSkillCast(_actor);
     	return;
     }
     else
     	TargetReset();
 	
 }

 public void TargetReset()
 {
	 for (L2Object obj : _actor.getKnownList().getKnownCharactersInRadius(_actor.getPhysicalAttackRange()))
  {
		 
		 if(obj instanceof L2Character)
		 {
		 //if(((L2Attackable)_actor).getHating((L2Character)obj)>0 && obj != _actor)
		 //{
			 
          ((L2Attackable)_actor).addDamageHate(((L2Character)obj), 5000,5000);
          //((L2Attackable)_actor).addBufferHate();
		 //}
		 }
		 if(obj instanceof L2Attackable)
		 {
			 if((((L2Attackable)_actor).getEnemyClan() == ((L2Attackable)obj).getFactionId()) &&((L2Attackable)_actor).getEnemyClan() != null)
			 {
				 ((L2Attackable)_actor).addDamageHate(((L2Attackable)obj), 5000,5000);
			 }
		 }
		 if(obj instanceof L2Summon)
		 {
			 
				 ((L2Attackable)_actor).addDamageHate(((L2Summon)obj), 5000,5000);

		 }
		 
		 
  }
	 return;
 }
	
	
}