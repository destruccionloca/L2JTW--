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

/**
 * This class manages AI of L2Attackable.<BR><BR>
 *
 */
public class L2AttackableAI extends L2CharacterAI implements Runnable
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

    /**
     * Constructor of L2AttackableAI.<BR><BR>
     *
     * @param accessor The AI accessor of the L2Character
     *
     */
    public L2AttackableAI(L2Character.AIAccessor accessor)
    {
        super(accessor);

        _attackTimeout = Integer.MAX_VALUE;
        _globalAggro = -10; // 10 seconds timeout of ATTACK after respawn
        enemyRange = ((L2Attackable) _actor).getEnemyRange();
    }

    public void run()
    {
        // Launch actions corresponding to the Event Think
        onEvtThink();

    }

    /**
     * Return True if the target is autoattackable (depends on the actor type).<BR><BR>
     *
     * <B><U> Actor is a L2GuardInstance</U> :</B><BR><BR>
     * <li>The target isn't a Folk or a Door</li>
     * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
     * <li>The target is in the actor Aggro range and is at the same height</li>
     * <li>The L2PcInstance target has karma (=PK)</li>
     * <li>The L2MonsterInstance target is aggressive</li><BR><BR>
     *
     * <B><U> Actor is a L2SiegeGuardInstance</U> :</B><BR><BR>
     * <li>The target isn't a Folk or a Door</li>
     * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
     * <li>The target is in the actor Aggro range and is at the same height</li>
     * <li>A siege is in progress</li>
     * <li>The L2PcInstance target isn't a Defender</li><BR><BR>
     *
     * <B><U> Actor is a L2FriendlyMobInstance</U> :</B><BR><BR>
     * <li>The target isn't a Folk, a Door or another L2NpcInstance</li>
     * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
     * <li>The target is in the actor Aggro range and is at the same height</li>
     * <li>The L2PcInstance target has karma (=PK)</li><BR><BR>
     *
     * <B><U> Actor is a L2MonsterInstance</U> :</B><BR><BR>
     * <li>The target isn't a Folk, a Door or another L2NpcInstance</li>
     * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
     * <li>The target is in the actor Aggro range and is at the same height</li>
     * <li>The actor is Aggressive</li><BR><BR>
     *
     * @param target The targeted L2Object
     *
     */
    private boolean autoAttackCondition(L2Character target)
    {

    	if (target == null || !(_actor instanceof L2Attackable)) return false;

        L2Attackable me = (L2Attackable) _actor;
        
        // Check if the target isn't invulnerable
        if (target.isInvul())
            return false;

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
        		
	        		if(target instanceof L2NpcInstance)
	        			canchat=true;
        	}
        	if (chats.getChatCondition1()==4)
        	{
        	
	        		if(target instanceof L2PcInstance)
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
	        		if(target instanceof L2NpcInstance)
	        			canchat=false;
        	}
        	if (chats.getChatCondition2()==4)
        	{
	        		if(target instanceof L2PcInstance)
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
        
        // Check if the target isn't a Folk or a Door
        if (target instanceof L2FolkInstance || target instanceof L2DoorInstance) return false;

        // Check if the target isn't dead, is in the Aggro range and is at the same height


        
        if(target instanceof L2Attackable)
        {
        if((!me.isInsideRadius(target, me.getAggroRange(), false, false) 
            || Math.abs(_actor.getZ() - target.getZ()) > 300) && ((L2Attackable)_actor).getEnemyRange() == 0 && ((L2Attackable)_actor).getEnemyClan() == null && ((L2Attackable)_actor).getIsChaos()==0) 
            return false;

        }
        else
        {
         if (target.isAlikeDead() 
            || !me.isInsideRadius(target, me.getAggroRange(), false, false) 

            || Math.abs(_actor.getZ() - target.getZ()) > 300) return false;


        }

        // Los Check Here
        if(!GeoData.getInstance().canSeeTarget(me, target))
        	return false;


        // Check if the target isn't invulnerable
        if (target.isInvul())
            return false;

    	// Check if the target is a L2PcInstance
        if (target instanceof L2PcInstance)
        {
        	// Don't take the aggro if the GM has the access level below or equal to GM_DONT_TAKE_AGGRO
            if (((L2PcInstance)target).isGM() && ((L2PcInstance)target).getAccessLevel() <= Config.GM_DONT_TAKE_AGGRO)
                return false;

            // Check if the AI isn't a Raid Boss and the target isn't in silent move mode
            if (!(me instanceof L2RaidBossInstance) && ((L2PcInstance)target).isSilentMoving())
                return false;

            // Check if player is an ally //TODO! [Nemesiss] it should be rather boolean or smth like that
            // Comparing String isnt good idea!
            if (me.getFactionId() == "varka" && ((L2PcInstance)target).isAlliedWithVarka())
                return false;
            if (me.getFactionId() == "ketra" && ((L2PcInstance)target).isAlliedWithKetra())
                return false;
            // check if the target is within the grace period for JUST getting up from fake death
            if (((L2PcInstance)target).isRecentFakeDeath())
                return false;
            
            if (target.isInParty() && target.getParty().isInDimensionalRift())
            {
                byte riftType = target.getParty().getDimensionalRift().getType();
                byte riftRoom = target.getParty().getDimensionalRift().getCurrentRoom();
                
                if (me instanceof L2RiftInvaderInstance 
                        && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(me.getX(), me.getY(), me.getZ()))
                    return false;
            }
        }

        // Check if the actor is a L2GuardInstance
        if (_actor instanceof L2GuardInstance)
        {

            // Check if the L2PcInstance target has karma (=PK)
            if (target instanceof L2PcInstance && ((L2PcInstance) target).getKarma() > 0)
                // Los Check
                return GeoData.getInstance().canSeeTarget(me, target);

            //if (target instanceof L2Summon)
            //    return ((L2Summon)target).getKarma() > 0;

            // Check if the L2MonsterInstance target is aggressive
            if (target instanceof L2MonsterInstance)
                return (((L2MonsterInstance) target).isAggressive() && GeoData.getInstance().canSeeTarget(me, target));

            return false;
        }
        else if (_actor instanceof L2FriendlyMobInstance)
        { // the actor is a L2FriendlyMobInstance

            // Check if the target isn't another L2NpcInstance
            if (target instanceof L2NpcInstance) return false;

            // Check if the L2PcInstance target has karma (=PK)

            if (target instanceof L2PcInstance && ((L2PcInstance) target).getKarma() > 0)
                // Los Check
               return GeoData.getInstance().canSeeTarget(me, target);
            else
                return false;

        }
        else
        { //The actor is a L2MonsterInstance

            // Check if the target isn't another L2NpcInstance
            //if (target instanceof L2NpcInstance) return false;

            // depending on config, do not allow mobs to attack _new_ players in peacezones, 
            // unless they are already following those players from outside the peacezone. 
            if (!Config.ALT_MOB_AGRO_IN_PEACEZONE && target.isInsideZone(L2Character.ZONE_PEACE)) 
                return false;
            
            // Check if the actor is Aggressive

         if(!(target instanceof L2PcInstance) && target instanceof L2Attackable)
            if (((L2Attackable)_actor).getEnemyClan() == ((L2Attackable) target).getFactionId() && ((L2Attackable)_actor).getEnemyClan() != null && ((L2Attackable) target).getFactionId() !=null)
            {
            	
                // Los Check
            	return GeoData.getInstance().canSeeTarget(me, target);
                
                
            }
            if  (((L2Attackable)_actor).getIsChaos() != 0)
            {
            	if (((L2Attackable)_actor).getFactionId() == ((L2Attackable) target).getFactionId() && ((L2Attackable)_actor).getFactionId() != null)
            	{
            		return false;
            	}
                // Los Check
            	return GeoData.getInstance().canSeeTarget(me, target);
            }
            
         if (target instanceof L2Attackable || target instanceof L2NpcInstance)
            return false;
         
         return (me.isAggressive() && GeoData.getInstance().canSeeTarget(me, target));
         

        }
       
    }
    public void startAITask()
    {
        // If not idle - create an AI task (schedule onEvtThink repeatedly)
        if (_aiTask == null)
        {
            _aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000, 1000);
        }
    }

    public void stopAITask()
    {
        if (_aiTask != null)
        {
            _aiTask.cancel(false);
            _aiTask = null;
        }
    }

    @Override
	protected void onEvtDead()
    {
        stopAITask();
        super.onEvtDead();
    }

    /**
     * Set the Intention of this L2CharacterAI and create an  AI Task executed every 1s (call onEvtThink method) for this L2Attackable.<BR><BR>
     *
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : If actor _knowPlayer isn't EMPTY, AI_INTENTION_IDLE will be change in AI_INTENTION_ACTIVE</B></FONT><BR><BR>
     *
     * @param intention The new Intention to set to the AI
     * @param arg0 The first parameter of the Intention
     * @param arg1 The second parameter of the Intention
     *
     */
    @Override
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
    {
        if (intention == AI_INTENTION_IDLE || intention == AI_INTENTION_ACTIVE)
        {
            // Check if actor is not dead
            if (!_actor.isAlikeDead())
            {
                L2Attackable npc = (L2Attackable) _actor;

                // If its _knownPlayer isn't empty set the Intention to AI_INTENTION_ACTIVE
                if (npc.getKnownList().getKnownPlayers().size() > 0) intention = AI_INTENTION_ACTIVE;
            }

            if (intention == AI_INTENTION_IDLE)
            {
                // Set the Intention of this L2AttackableAI to AI_INTENTION_IDLE
                super.changeIntention(AI_INTENTION_IDLE, null, null);

                // Stop AI task and detach AI from NPC
                if (_aiTask != null)
                {
                    _aiTask.cancel(true);
                    _aiTask = null;
                }

                // Cancel the AI
                _accessor.detachAI();

                return;
            }
        }

        // Set the Intention of this L2AttackableAI to intention
        super.changeIntention(intention, arg0, arg1);

        // If not idle - create an AI task (schedule onEvtThink repeatedly)
        startAITask();
    }

    /**
     * Manage the Attack Intention : Stop current Attack (if necessary), Calculate attack timeout, Start a new Attack and Launch Think Event.<BR><BR>
     *
     * @param target The L2Character to attack
     *
     */
    @Override
	protected void onIntentionAttack(L2Character target)
    {
        // Calculate the attack timeout
        _attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();

        // Manage the Attack Intention : Stop current Attack (if necessary), Start a new Attack and Launch Think Event
        super.onIntentionAttack(target);
    }

    /**
     * Manage AI standard thinks of a L2Attackable (called by onEvtThink).<BR><BR>
     *
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Update every 1s the _globalAggro counter to come close to 0</li>
     * <li>If the actor is Aggressive and can attack, add all autoAttackable L2Character in its Aggro Range to its _aggroList, chose a target and order to attack it</li>
     * <li>If the actor is a L2GuardInstance that can't attack, order to it to return to its home location</li>
     * <li>If the actor is a L2MonsterInstance that can't attack, order to it to random walk (1/100)</li><BR><BR>
     *
     */
    private void thinkActive()
    {
        L2Attackable npc = (L2Attackable) _actor;

        // Update every 1s the _globalAggro counter to come close to 0
        if (_globalAggro != 0)
        {
            if (_globalAggro < 0) _globalAggro++;
            else _globalAggro--;
        }

        // Add all autoAttackable L2Character in L2Attackable Aggro Range to its _aggroList with 0 damage and 1 hate
        // A L2Attackable isn't aggressive during 10s after its spawn because _globalAggro is set to -10
        if (_globalAggro >= 0)
        {
            // Get all visible objects inside its Aggro Range
            //L2Object[] objects = L2World.getInstance().getVisibleObjects(_actor, ((L2NpcInstance)_actor).getAggroRange());
            // Go through visible objects
            for (L2Object obj : npc.getKnownList().getKnownObjects().values())
            {
                if (obj == null || !(obj instanceof L2Character)) continue;
                L2Character target = (L2Character) obj;

                /*
                 * Check to see if this is a festival mob spawn.
                 * If it is, then check to see if the aggro trigger
                 * is a festival participant...if so, move to attack it.
                 */
                if ((_actor instanceof L2FestivalMonsterInstance) && obj instanceof L2PcInstance)
                {
                    L2PcInstance targetPlayer = (L2PcInstance) obj;

                    if (!(targetPlayer.isFestivalParticipant())) continue;
                }

                // For each L2Character check if the target is autoattackable
                if (autoAttackCondition(target)) // check aggression
                {
                    // Get the hate level of the L2Attackable against this L2Character target contained in _aggroList
                    int hating = npc.getHating(target);

                    // Add the attacker to the L2Attackable _aggroList with 0 damage and 1 hate
                    if (hating == 0) npc.addDamageHate(target, 0, 1);
                }
            }

            // Chose a target from its aggroList
            L2Character hated;
            if (_actor.isConfused()) hated = getAttackTarget(); // Force mobs to attak anybody if confused
            else hated = npc.getMostHated();

            // Order to the L2Attackable to attack the target
            if (hated != null)
            {
                // Get the hate level of the L2Attackable against this L2Character target contained in _aggroList
                int aggro = npc.getHating(hated);

                if (aggro + _globalAggro > 0)
                {
                    // Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
                    if (!_actor.isRunning()) _actor.setRunning();

                    // Set the AI Intention to AI_INTENTION_ATTACK
                    setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated);
                }

                return;
            }

        }

        // Check if the actor is a L2GuardInstance
        if (_actor instanceof L2GuardInstance)
        {
            // Order to the L2GuardInstance to return to its home location because there's no target to attack
            ((L2GuardInstance) _actor).returnHome();
        }

        // If this is a festival monster, then it remains in the same location.
        if (_actor instanceof L2FestivalMonsterInstance) return;

        // Minions following leader
        if (_actor instanceof L2MinionInstance && ((L2MinionInstance)_actor).getLeader() != null)
        {
            int offset;

            if (_actor.isRaid()) offset = 500; // for Raids - need correction
            else offset = 200; // for normal minions - need correction :)

            if(((L2MinionInstance)_actor).getLeader().isRunning())	_actor.setRunning();
            else _actor.setWalking();

            if (_actor.getPlanDistanceSq(((L2MinionInstance)_actor).getLeader()) > offset*offset)
            {
                int x1, y1, z1;
                x1 = ((L2MinionInstance)_actor).getLeader().getX() + Rnd.nextInt( (offset - 30) * 2 ) - ( offset - 30 );
                y1 = ((L2MinionInstance)_actor).getLeader().getY() + Rnd.nextInt( (offset - 30) * 2 ) - ( offset - 30 );
                z1 = ((L2MinionInstance)_actor).getLeader().getZ();
                // Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
                moveTo(x1, y1, z1);
                return;
            }
        }
        // Order to the L2MonsterInstance to random walk (1/100)
        else if (npc.getSpawn() != null && Rnd.nextInt(RANDOM_WALK_RATE) == 0)
        {
            int x1, y1, z1;

            // If NPC with random coord in territory
            if (npc.getSpawn().getLocx() == 0 && npc.getSpawn().getLocy() == 0)
            {
                // If NPC with random fixed coord, don't move
                if (Territory.getInstance().getProcMax(npc.getSpawn().getLocation()) > 0) return;

                // Calculate a destination point in the spawn area
                int p[] = Territory.getInstance().getRandomPoint(npc.getSpawn().getLocation());
                x1 = p[0];
                y1 = p[1];
                z1 = p[2];

                // Calculate the distance between the current position of the L2Character and the target (x,y)
                double distance2 = _actor.getPlanDistanceSq(x1, y1);

                if (distance2 > Config.MAX_DRIFT_RANGE * Config.MAX_DRIFT_RANGE)
                {
                    float delay = (float) Math.sqrt(distance2) / Config.MAX_DRIFT_RANGE;
                    x1 = _actor.getX() + (int) ((x1 - _actor.getX()) / delay);
                    y1 = _actor.getY() + (int) ((y1 - _actor.getY()) / delay);
                }

            }
            else
            {
                // If NPC with fixed coord
                x1 = npc.getSpawn().getLocx() + Rnd.nextInt(Config.MAX_DRIFT_RANGE * 2)
                    - Config.MAX_DRIFT_RANGE;
                y1 = npc.getSpawn().getLocy() + Rnd.nextInt(Config.MAX_DRIFT_RANGE * 2)
                    - Config.MAX_DRIFT_RANGE;
                z1 = npc.getZ();
            }


            //_log.config("Curent pos ("+getX()+", "+getY()+"), moving to ("+x1+", "+y1+").");
            // Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
            moveTo(x1, y1, z1);
        }

        
        
        //----------------------------------------------------------------------------
        // IDLE BUFF SYSTEM
      
        
        if (npc.getSpawn() != null && Rnd.nextInt(RANDOM_BUFF_RATE) == 0)
        {
            L2Skill[] skills = null;
            skills = _actor.getAllSkills();
            for (L2Skill sk : skills)
            {
                if (sk.getSkillType() == L2Skill.SkillType.BUFF && ((_actor instanceof L2Attackable)||(_actor instanceof L2MinionInstance)))
                {                                                                     
                // monsters should first look for a possible target to heal or buff
               
                
                    for (L2Object obj : _actor.getKnownList().getKnownCharactersInRadius(sk.getCastRange()))// should be looking for faction_id though
                    {

                        if((obj instanceof L2Attackable|| obj instanceof L2MinionInstance)&&!GeoData.getInstance().canSeeTarget(_actor, obj))
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
                                    // GeoData Los Check

                                    clientStopMoving(null);
                                    _actor.setTarget(obj);
                                    _accessor.doCast(sk);
                                    return;
                                }
                            }
                            }
                        if ((((L2Attackable) _actor).getFactionId() == ((L2Attackable) obj).getFactionId())&&!GeoData.getInstance().canSeeTarget(_actor, obj))
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

		
            	} 
           
            
            }
            

        
        
        
        return;

    }

    /**
     * Manage AI attack thinks of a L2Attackable (called by onEvtThink).<BR><BR>
     *
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Update the attack timeout if actor is running</li>
     * <li>If target is dead or timeout is expired, stop this attack and set the Intention to AI_INTENTION_ACTIVE</li>
     * <li>Call all L2Object of its Faction inside the Faction Range</li>
     * <li>Chose a target and order to attack it with magic skill or physical attack</li><BR><BR>
     *
     * TODO: Manage casting rules to healer mobs (like Ant Nurses)
     *
     */
    private void thinkAttack()
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

            if(_actor.isAttackingDisabled()) return;

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
                    moveToPawn(getAttackTarget(), range+((L2Attackable) _actor).getTemplate().collisionRadius);
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
            	                	moveToPawn(hated, range+((L2Attackable) _actor).getTemplate().collisionRadius);
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
            	moveToPawn(hated, range+((L2Attackable) _actor).getTemplate().collisionRadius);
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
                	moveToPawn(hated, range+((L2Attackable) _actor).getTemplate().collisionRadius);
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
                moveToPawn(getAttackTarget(), range+((L2Attackable) _actor).getTemplate().collisionRadius);
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
        moveToPawn(getAttackTarget(), attackrange+((L2Attackable) _actor).getTemplate().collisionRadius);
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
    /**
     * Manage AI thinking actions of a L2Attackable.<BR><BR>
     */
    @Override
	protected void onEvtThink()
    {
        // Check if the actor can't use skills and if a thinking action isn't already in progress
        if (_thinking || _actor.isAllSkillsDisabled()) return;

        // Start thinking action
        _thinking = true;

        try
        {
            // Manage AI thinks of a L2Attackable
            if (getIntention() == AI_INTENTION_ACTIVE) thinkActive();
            else if (getIntention() == AI_INTENTION_ATTACK) thinkAttack();
        }
        finally
        {
            // Stop thinking action
            _thinking = false;
        }
    }

    /**
     * Launch actions corresponding to the Event Attacked.<BR><BR>
     *
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Init the attack : Calculate the attack timeout, Set the _globalAggro to 0, Add the attacker to the actor _aggroList</li>
     * <li>Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance</li>
     * <li>Set the Intention to AI_INTENTION_ATTACK</li><BR><BR>
     *
     * @param attacker The L2Character that attacks the actor
     *
     */
    @Override
	protected void onEvtAttacked(L2Character attacker)
    {
    	//if (_actor instanceof L2ChestInstance && !((L2ChestInstance)_actor).isInteracted())
    	//{
    		//((L2ChestInstance)_actor).deleteMe();
    		//((L2ChestInstance)_actor).getSpawn().startRespawn();
    		//return;
    	//}

        // Calculate the attack timeout
        _attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();

        // Set the _globalAggro to 0 to permit attack even just after spawn
        if (_globalAggro < 0) _globalAggro = 0;

        // Add the attacker to the _aggroList of the actor
        ((L2Attackable) _actor).addDamageHate(attacker, 0, 1);

        // Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
        if (!_actor.isRunning()) _actor.setRunning();

        // Set the Intention to AI_INTENTION_ATTACK
        if (getIntention() != AI_INTENTION_ATTACK)
        {
            setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
        }
        else if (((L2Attackable) _actor).getMostHated() != getAttackTarget())
        {
            setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
        }

        super.onEvtAttacked(attacker);
    }

    /**
     * Launch actions corresponding to the Event Aggression.<BR><BR>
     *
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Add the target to the actor _aggroList or update hate if already present </li>
     * <li>Set the actor Intention to AI_INTENTION_ATTACK (if actor is L2GuardInstance check if it isn't too far from its home location)</li><BR><BR>
     *
     * @param attacker The L2Character that attacks
     * @param aggro The value of hate to add to the actor against the target
     *
     */
    @Override
	protected void onEvtAggression(L2Character target, int aggro)
    {
        L2Attackable me = (L2Attackable) _actor;

    	if (target != null)
        {
            // Add the target to the actor _aggroList or update hate if already present
            me.addDamageHate(target, 0, aggro);

            // Set the actor AI Intention to AI_INTENTION_ATTACK
            if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
            {
                // Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
                if (!_actor.isRunning()) _actor.setRunning();

                setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
            }
        }
    }
    
    @Override
	protected void onIntentionActive()
    {
        // Cancel attack timeout
        _attackTimeout = Integer.MAX_VALUE;
    	super.onIntentionActive();
    }
    
    public void setGlobalAggro(int value)
    {
    	_globalAggro = value;
    }
}
