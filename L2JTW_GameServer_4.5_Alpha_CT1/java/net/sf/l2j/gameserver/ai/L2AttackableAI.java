/*
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
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FestivalMonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FriendlyMobInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GuardInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MinionInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RiftInvaderInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.gameserver.util.Util;

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
    private Future<?> _aiTask;

    /** The delay after wich the attacked is stopped */
    private int _attackTimeout;

    /** The L2Attackable aggro counter */
    private int _globalAggro;

    /** The flag used to indicate that a thinking action is in progress */
    private boolean _thinking; // to prevent recursive thinking
    
    /** For attack AI, analysis of mob and its targets */
    //private SelfAnalysis _selfAnalysis = new SelfAnalysis();
    //private TargetAnalysis _mostHatedAnalysis = new TargetAnalysis();
    //private TargetAnalysis _secondMostHatedAnalysis = new TargetAnalysis();
    
    
    private String clan;
    private String enemyClan;

    private int timepass = 0;
    private int chaostime = 0;
    private boolean iscasting = false;
    private L2NpcTemplate _skillrender;
    /**
     * Constructor of L2AttackableAI.<BR><BR>
     *
     * @param accessor The AI accessor of the L2Character
     *
     */
    public L2AttackableAI(L2Character.AIAccessor accessor)
    {
        super(accessor);
        _skillrender = NpcTable.getInstance().getTemplate(((L2NpcInstance)_actor).getTemplate().npcId);
        //_selfAnalysis.init();
        _attackTimeout = Integer.MAX_VALUE;
        _globalAggro = -10; // 10 seconds timeout of ATTACK after respawn
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

        if(target instanceof L2Attackable)
            if (((L2Attackable)_actor).getEnemyClan()!= "none")
            {
            	
            	enemyClan =  ((L2Attackable)_actor).getEnemyClan();
            	clan = ((L2Attackable)target).getClan();

            }
        // Check if the target isn't invulnerable
        if (target.isInvul())
        {
            // However EffectInvincible requires to check GMs specially
            if (target instanceof L2PcInstance && ((L2PcInstance)target).isGM())
                return false;
            if (target instanceof L2Summon && ((L2Summon)target).getOwner().isGM())
                return false;
        }

        // Check if the target isn't a Folk or a Door
        if (target instanceof L2FolkInstance || target instanceof L2DoorInstance) return false;

        // Check if the target isn't dead, is in the Aggro range and is at the same height
        if (target.isAlikeDead() || (target instanceof L2PcInstance && !me.isInsideRadius(target, me.getAggroRange(), false, false)) 
        		||Math.abs(_actor.getZ() - target.getZ()) > 300 || (target instanceof L2Summon && !me.isInsideRadius(target, me.getAggroRange(), false, false))) return false;

        // Check if the target is a L2PlayableInstance
        if (target instanceof L2PlayableInstance)
        {
        	// Check if the AI isn't a Raid Boss and the target isn't in silent move mode
            if (!(me instanceof L2RaidBossInstance) && ((L2PlayableInstance)target).isSilentMoving())
                return false;
        }
        
        // Check if the target is a L2PcInstance
        if (target instanceof L2PcInstance)
        {
            // Don't take the aggro if the GM has the access level below or equal to GM_DONT_TAKE_AGGRO
        	if (((L2PcInstance)target).isGM() && !((L2PcInstance)target).getAccessLevel().canTakeAggro())
                return false;

            // TODO: Ideally, autoattack condition should be called from the AI script.  In that case,
            // it should only implement the basic behaviors while the script will add more specific
            // behaviors (like varka/ketra alliance, etc).  Once implemented, remove specialized stuff
            // from this location.  (Fulminus)
            
            // Check if player is an ally (comparing mem addr)
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
            
            //if (_selfAnalysis.cannotMoveOnLand && !target.isInsideZone(L2Character.ZONE_WATER))
            //    return false;
        }

        // Check if the target is a L2Summon
        if (target instanceof L2Summon)
        {
        	L2PcInstance owner = ((L2Summon)target).getOwner();
        	if (owner != null)
        	{
        		// Don't take the aggro if the GM has the access level below or equal to GM_DONT_TAKE_AGGRO
                if (owner.isGM() && (owner.isInvul() || !owner.getAccessLevel().canTakeAggro()))
                    return false;
                // Check if player is an ally (comparing mem addr)
                if (me.getFactionId() == "varka" && owner.isAlliedWithVarka())
                    return false;
                if (me.getFactionId() == "ketra" && owner.isAlliedWithKetra())
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

            if(target instanceof L2Attackable)
            {
           	 if (((L2Attackable)_actor).getEnemyClan()== "none" || ((L2Attackable)target).getClan()== "none")
           		 return false;

           	 if(((L2Attackable)_actor).getEnemyClan().equals(((L2Attackable)target).getClan()))
               {
               	if(_actor.isInsideRadius(target, ((L2Attackable)_actor).getEnemyRange(), false, false))
               	{
               		return GeoData.getInstance().canSeeTarget(_actor, target);
               	}
               	else return false;
                                      
               }
               if  (((L2Attackable)_actor).getIsChaos() > 0 && me.isInsideRadius(target, ((L2Attackable)_actor).getIsChaos(), false, false) )
               {
               		if (((L2Attackable)_actor).getFactionId() == ((L2Attackable) target).getFactionId() && ((L2Attackable)_actor).getFactionId() != null)
               		{
               			return false;
               		}
                // Los Check
               	return GeoData.getInstance().canSeeTarget(me, target);
               }
            }
            if (target instanceof L2Attackable || target instanceof L2NpcInstance)
               return false;

            // depending on config, do not allow mobs to attack _new_ players in peacezones,
            // unless they are already following those players from outside the peacezone.
            if (!Config.ALT_MOB_AGRO_IN_PEACEZONE && target.isInsideZone(L2Character.ZONE_PEACE))
                return false;
            
            if (me.isChampion() && Config.L2JMOD_CHAMPION_PASSIVE)
            	return false;

            // Check if the actor is Aggressive
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

        // self and buffs
        /*
        if (_selfAnalysis.lastBuffTick+100 < GameTimeController.getGameTicks())
        {
          for (L2Skill sk : _selfAnalysis.buffSkills)
          {
        	if (_actor.getFirstEffect(sk.getId()) == null)
        	{
        		if (_actor.getCurrentMp() < sk.getMpConsume())
					continue;
        		if (_actor.isSkillDisabled(sk.getId()))
					continue;
        		// no clan buffs here?
        		if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_CLAN)
        			continue;
        		L2Object OldTarget = _actor.getTarget();
        		 _actor.setTarget(_actor);
        		clientStopMoving(null);
        		_accessor.doCast(sk);
        		// forcing long reuse delay so if cast get interrupted or there would be several buffs, doesn't cast again
        		_selfAnalysis.lastBuffTick = GameTimeController.getGameTicks(); 
        		_actor.setTarget(OldTarget);
            }
          }
        }
        */
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
                if (!(obj instanceof L2Character)) continue;
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

                /*
                 * Temporarily adding this commented code as a concept to be used eventually.
                 * However, the way it is written below will NOT work correctly.  The NPC
                 * should only notify Aggro Range Enter when someone enters the range from outside.
                 * Instead, the below code will keep notifying even while someone remains within 
                 * the range.  Perhaps we need a short knownlist of range = aggroRange for just
                 * people who are actively within the npc's aggro range?...(Fulminus) 
                // notify AI that a playable instance came within aggro range
                if ((obj instanceof L2PcInstance) || (obj instanceof L2Summon))
                {
                    if ( !((L2Character)obj).isAlikeDead()
                            && !npc.isInsideRadius(obj, npc.getAggroRange(), true, false) )
                    {
                    	L2PcInstance targetPlayer = (obj instanceof L2PcInstance)? (L2PcInstance) obj: ((L2Summon) obj).getOwner();
                        if (npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_AGGRO_RANGE_ENTER) !=null)
                        	for (Quest quest: npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_AGGRO_RANGE_ENTER))
                        		quest.notifyAggroRangeEnter(npc, targetPlayer, (obj instanceof L2Summon));
                    }
                }
                */
                // TODO: The AI Script ought to handle aggro behaviors in onSee.  Once implemented, aggro behaviors ought
                // to be removed from here.  (Fulminus)
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
            if (_actor.isConfused()) hated = getAttackTarget(); // effect handles selection
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

    	// Check if the mob should not return to spawn point
    	if (!npc.canReturnToSpawnPoint()) return;
        
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
            else if (Rnd.nextInt(RANDOM_WALK_RATE) == 0)
            {
            	iscasting = false;
            	if(_skillrender.hasBuffSkill())
                for (L2Skill sk : _skillrender._buffskills)
                {
					if(sk.getMpConsume()>=_actor.getCurrentMp()
							||_actor.isSkillDisabled(sk.getId())
							||(sk.isMagic()&&_actor.isMuted())
							||(!sk.isMagic()&&_actor.isPhysicalMuted()))
    				{
    					continue;
    				}
					Cast(sk);
    				if(iscasting)
    					return;
                }
            }
        }
        // Order to the L2MonsterInstance to random walk (1/100)
        else if (npc.getSpawn() != null && Rnd.nextInt(RANDOM_WALK_RATE) == 0)
        {
            int x1, y1, z1;
            int range = Config.MAX_DRIFT_RANGE;
            
        	iscasting = false;
        	if(_skillrender.hasBuffSkill())
            for (L2Skill sk : _skillrender._buffskills)
            {
				if(sk.getMpConsume()>=_actor.getCurrentMp()
						||_actor.isSkillDisabled(sk.getId())
						||(sk.isMagic()&&_actor.isMuted())
						||(!sk.isMagic()&&_actor.isPhysicalMuted()))
				{
					continue;
				}
				Cast(sk);
				if(iscasting)
					return;
            }
            
            // If NPC with random coord in territory
            if (npc.getSpawn().getLocx() == 0 && npc.getSpawn().getLocy() == 0)
            {
                // Calculate a destination point in the spawn area
                int p[] = Territory.getInstance().getRandomPoint(npc.getSpawn().getLocation());
                x1 = p[0];
                y1 = p[1];
                z1 = p[2];

                // Calculate the distance between the current position of the L2Character and the target (x,y)
                double distance2 = _actor.getPlanDistanceSq(x1, y1);

                if (distance2 > range * range)
                {
                    npc.setisReturningToSpawnPoint(true);
                	float delay = (float) Math.sqrt(distance2) / range;
                    x1 = _actor.getX() + (int) ((x1 - _actor.getX()) / delay);
                    y1 = _actor.getY() + (int) ((y1 - _actor.getY()) / delay);
                }
                
                // If NPC with random fixed coord, don't move (unless needs to return to spawnpoint)
                if (Territory.getInstance().getProcMax(npc.getSpawn().getLocation()) > 0 && !npc.isReturningToSpawnPoint())
                	return;
            }
            else
            {
                // If NPC with fixed coord
            	x1 = npc.getSpawn().getLocx() + Rnd.nextInt(range * 2) - range;
            	y1 = npc.getSpawn().getLocy() + Rnd.nextInt(range * 2) - range;
                z1 = npc.getZ();
            }

           
            moveTo(x1, y1, z1);
        }
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

        L2Character originalAttackTarget = null;
        
        if(getAttackTarget()!=null)
        	originalAttackTarget = getAttackTarget();

        // Handle all L2Object of its Faction inside the Faction Range
        if (((L2NpcInstance) _actor).getFactionId() != null && originalAttackTarget!=null)
        {
        	String faction_id = ((L2NpcInstance) _actor).getFactionId();

        	// Go through all L2Object that belong to its faction
        	for (L2Object obj : _actor.getKnownList().getKnownObjects().values())
        	{
        		if (obj instanceof L2NpcInstance)
        		{
        			L2NpcInstance npc = (L2NpcInstance) obj;
        			
        			if (faction_id != npc.getFactionId())
        				continue;

        			// Check if the L2Object is inside the Faction Range of the actor
        			if (_actor.isInsideRadius(npc, npc.getFactionRange()+npc.getTemplate().collisionRadius, true, false)
        				&& npc.getAI() != null
        				)
        			{
        				if (Math.abs(originalAttackTarget.getZ() - npc.getZ()) < 600
        						&& _actor.getAttackByList().contains(originalAttackTarget)
        						&& (npc.getAI()._intention == CtrlIntention.AI_INTENTION_IDLE
        								|| npc.getAI()._intention == CtrlIntention.AI_INTENTION_ACTIVE)
        						&& GeoData.getInstance().canSeeTarget(_actor, npc))
        				{
        					if (originalAttackTarget instanceof L2PcInstance
        							&& originalAttackTarget.isInParty()
        							&& originalAttackTarget.getParty().isInDimensionalRift())
        					{
        						byte riftType = originalAttackTarget.getParty().getDimensionalRift().getType();
        						byte riftRoom = originalAttackTarget.getParty().getDimensionalRift().getCurrentRoom();

        						if (_actor instanceof L2RiftInvaderInstance
        								&& !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(npc.getX(), npc.getY(), npc.getZ()))
        							continue;
        					}

        					// TODO: notifyEvent ought to be removed from here and added in the AI script, when implemented (Fulminus) 
        					// Notify the L2Object AI with EVT_AGGRESSION
        					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, originalAttackTarget, 1);
        					((L2Attackable)npc).addDamageHate(originalAttackTarget, 1, 1);
        					if ((originalAttackTarget instanceof L2PcInstance) || (originalAttackTarget instanceof L2Summon))
        					{
        						if (npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_FACTION_CALL) != null)
        						{
        							L2PcInstance player = (originalAttackTarget instanceof L2PcInstance)?
        								(L2PcInstance)originalAttackTarget: ((L2Summon) originalAttackTarget).getOwner();
        							for (Quest quest: npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_FACTION_CALL))
        							{
        								quest.notifyFactionCall(npc, (L2NpcInstance) _actor, player, (originalAttackTarget instanceof L2Summon));
        								((L2Attackable)npc).addDamageHate(originalAttackTarget, 1, 1);
        							}
        						}
        					}
        				}
        			}
        		}
            }
        }
        //Return when Attack been disable
        if(_actor.isAttackingDisabled())
        	return;
        //----------------------------------------------------------------
        
        //------------------------------------------------------------------------------
        //Initialize data
        double dist = 0;
        int dist2 = 0;
        int range = 0;
        L2Character MostHate = ((L2Attackable) _actor).getMostHated();
        L2Character ctarget = (L2Character)_actor.getTarget();
        try
        {
       		setAttackTarget(MostHate);
       		_actor.setTarget(MostHate);
            dist = Math.sqrt(_actor.getPlanDistanceSq(getAttackTarget().getX(), getAttackTarget().getY()));
            dist2= (int)dist;
            range = _actor.getPhysicalAttackRange() + _actor.getTemplate().collisionRadius + getAttackTarget().getTemplate().collisionRadius;
            if(getAttackTarget().isMoving())
            {
            	range = range + 50;
            	if(_actor.isMoving())
            		range = range + 50;
            }
        }
        catch (NullPointerException e)
        {
            setIntention(AI_INTENTION_ACTIVE);
            return;
        }
        /*
        if(_actor.getTarget() == null || this.getAttackTarget() == null || this.getAttackTarget().isDead() || ctarget == _actor)
        	AggroReconsider();
        */
        // Check if target is dead or if timeout is expired to stop this attack
        if (getAttackTarget() == null || getAttackTarget().isAlikeDead())
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
            return;
        }
        //------------------------------------------------------
    	// In case many mobs are trying to hit from same place, move a bit,
		// circling around the target
		if (!_actor.isRooted() && Rnd.nextInt(100) <= 33) // check it once per 3 seconds
		{
			int combinedCollision = _actor.getTemplate().collisionRadius + getAttackTarget().getTemplate().collisionRadius;
			int collision = _actor.getTemplate().collisionRadius;
			for (L2Object nearby : _actor.getKnownList().getKnownCharactersInRadius(collision))
			{
				if (nearby instanceof L2Attackable
				        && nearby != getAttackTarget())
				{
					int diffx = Rnd.get(combinedCollision, combinedCollision+40);
					if (Rnd.get(10)<5) diffx = -diffx;
					int diffy = Rnd.get(combinedCollision, combinedCollision+40);
					if (Rnd.get(10)<5) diffy = -diffy;
					moveTo(getAttackTarget().getX() + diffx, getAttackTarget().getY()
					        + diffy, getAttackTarget().getZ());
					return;
				}
			}
		}
        //Dodge if its needed
        if(!_actor.isRooted() && ((L2Attackable)_actor).getCanDodge()>0)
        if (Rnd.get(100) <= ((L2Attackable)_actor).getCanDodge())
        {
            // Micht: kepping this one otherwise we should do 2 sqrt
            double distance2 = _actor.getPlanDistanceSq(getAttackTarget().getX(), getAttackTarget().getY());
            if (Math.sqrt(distance2) <= (60+_actor.getTemplate().collisionRadius + getAttackTarget().getTemplate().collisionRadius))
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
                        return;
                //}
            }
        }

        //------------------------------------------------------------------------------
        // BOSS Target Reconsider
        if((_actor instanceof L2RaidBossInstance) || (_actor instanceof L2GrandBossInstance))
        {
        	chaostime++;
        	if(_actor instanceof L2RaidBossInstance && !((L2MonsterInstance)_actor).hasMinions())
        	{
        		//Target will not reconsider in 10 second
        		if(chaostime>10)
        		if(Rnd.get(100)<= 1-(_actor.getCurrentHp()*100/_actor.getMaxHp()))
        		{
        			AggroReconsider();
        			chaostime = 0;
        		}
        	}
        	else if(_actor instanceof L2RaidBossInstance && ((L2MonsterInstance)_actor).hasMinions())
        	{
        		//Target will not reconsider in 10 second
        		if(chaostime>10)
            		if(Rnd.get(100)<= 1-(_actor.getCurrentHp()*200/_actor.getMaxHp()))
            		{
            			AggroReconsider();
            			chaostime = 0;
            		}
        	}
        	else if(_actor instanceof L2GrandBossInstance)
        	{
        		//Target will not reconsider in 10 second
        		if(chaostime>10)
            		if(Rnd.get(100)<= 1-(_actor.getCurrentHp()*300/_actor.getMaxHp()))
            		{
            			AggroReconsider();
            			chaostime = 0;
            		}
        	}
        	
        }

        if(_skillrender.hasSkill())
        {
        //-------------------------------------------------------------------------------
        //Heal Condition
        if(_skillrender.hasHealSkill() && _skillrender._healskills !=null)
        {
        	double percentage = _actor.getCurrentHp()/_actor.getMaxHp()*100;
        	if(_actor instanceof L2MinionInstance)
        	{
        		L2Character leader = ((L2MinionInstance)_actor).getLeader();
        		if(leader!=null &&!leader.isDead()&& Rnd.get(100)>leader.getCurrentHp()/leader.getMaxHp()*100)
        		for(L2Skill sk:_skillrender._healskills)
        		{
        			if(sk.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF)
        				continue;
    				if((sk.getMpConsume()>=_actor.getCurrentMp()
    						|| _actor.isSkillDisabled(sk.getId())
    						||(sk.isMagic()&&_actor.isMuted())
    						||(!sk.isMagic()&&_actor.isPhysicalMuted())))
    				{
    					continue;
    				}
    				if(!Util.checkIfInRange((sk.getCastRange()+_actor.getTemplate().collisionRadius + leader.getTemplate().collisionRadius),_actor,leader,false) 
    						&& !isParty(sk) && !_actor.isMovementDisabled())
    				{
    					moveToPawn(leader,sk.getCastRange()
    							+_actor.getTemplate().collisionRadius + leader.getTemplate().collisionRadius);
    				}
    				if(GeoData.getInstance().canSeeTarget(_actor,leader))
    				{
    					clientStopMoving(null);
    					_actor.setTarget(leader);
    					clientStopMoving(null);
    					_actor.doCast(sk);
        				return;
    				}
        		}        	
        	}    
        	if(Rnd.get(100)<(100-percentage)/3)
        		for(L2Skill sk:_skillrender._healskills)
        		{
    				if((sk.getMpConsume()>=_actor.getCurrentMp()
    						|| _actor.isSkillDisabled(sk.getId())
    						||(sk.isMagic()&&_actor.isMuted()))
    						||(!sk.isMagic()&&_actor.isPhysicalMuted()))
    				{
    					continue;
    				}
    					clientStopMoving(null);
    					_actor.setTarget(_actor);
    					_actor.doCast(sk);
        				return;        				
        		}
    		for(L2Skill sk:_skillrender._healskills)
    		{
				if((sk.getMpConsume()>=_actor.getCurrentMp()
						|| _actor.isSkillDisabled(sk.getId())
						||(sk.isMagic()&&_actor.isMuted()))
						||(!sk.isMagic()&&_actor.isPhysicalMuted()))
				{
					continue;
				}
				if(sk.getTargetType() == L2Skill.SkillTargetType.TARGET_ONE)
				for(L2Character obj:  _actor.getKnownList().getKnownCharactersInRadius(sk.getCastRange()+_actor.getTemplate().collisionRadius))
    			{
					if(!(obj instanceof L2Attackable)||obj.isDead())
						continue;

					L2Attackable targets = ((L2Attackable)obj);
					if(((L2Attackable)_actor).getFactionId()!= targets.getFactionId()&&((L2Attackable)_actor).getFactionId()!=null)
						continue;
  		        	percentage = targets.getCurrentHp()/targets.getMaxHp()*100;
					if(Rnd.get(100)< (100-percentage)/10)
					{
	    				if(GeoData.getInstance().canSeeTarget(_actor,targets))
	    				{
	    					clientStopMoving(null);
	    					_actor.setTarget(obj);
	    					_actor.doCast(sk);
	        				return;
	    				}
					}    					
    			}
				if(isParty(sk))
				{

					clientStopMoving(null);
					_actor.doCast(sk);
    				return;    				
				}
    		}
        }
        //-------------------------------------------------------------------------------
        //Res Skill Condition
        if(_skillrender.hasResSkill())
        {
        	if(_actor instanceof L2MinionInstance)
        	{
        		L2Character leader = ((L2MinionInstance)_actor).getLeader();
        		if(leader!=null && leader.isDead())
        		for(L2Skill sk:_skillrender._resskills)
        		{
        			if(sk.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF)
        				continue;
    				if((sk.getMpConsume()>=_actor.getCurrentMp()
    						|| _actor.isSkillDisabled(sk.getId())
    						||(sk.isMagic()&&_actor.isMuted())
    						||(!sk.isMagic()&&_actor.isPhysicalMuted())))
    				{
    					continue;
    				}
    				if(!Util.checkIfInRange((sk.getCastRange()+_actor.getTemplate().collisionRadius + leader.getTemplate().collisionRadius),_actor,leader,false) 
    						&& !isParty(sk) && !_actor.isRooted())
    				{
    					moveToPawn(leader,sk.getCastRange()
    							+_actor.getTemplate().collisionRadius + leader.getTemplate().collisionRadius);
    				}
    				if(GeoData.getInstance().canSeeTarget(_actor,leader))
    				{
    					clientStopMoving(null);
    					_actor.setTarget(((L2MinionInstance)_actor).getLeader());
    					_actor.doCast(sk);
        				return;
    				}
        		}        	
        	}
    		for(L2Skill sk:_skillrender._resskills)
    		{
				if((sk.getMpConsume()>=_actor.getCurrentMp()
						|| _actor.isSkillDisabled(sk.getId())
						||(sk.isMagic()&&_actor.isMuted()))
						||(!sk.isMagic()&&_actor.isPhysicalMuted()))
				{
					continue;
				}
				if(sk.getTargetType() == L2Skill.SkillTargetType.TARGET_ONE)
				for(L2Character obj:  _actor.getKnownList().getKnownCharactersInRadius(sk.getCastRange()+_actor.getTemplate().collisionRadius))
    			{
					if(!(obj instanceof L2Attackable) || !obj.isDead())
						continue;

					L2Attackable targets = ((L2Attackable)obj);
					if(((L2Attackable)_actor).getFactionId()!= targets.getFactionId() && ((L2Attackable)_actor).getFactionId()!=null)
						continue;
					if(Rnd.get(100)< 10)
					{
	    				if(GeoData.getInstance().canSeeTarget(_actor,targets))
	    				{
	    					clientStopMoving(null);
	    					_actor.setTarget(obj);
	    					_actor.doCast(sk);
	        				return;
	    				}
					}    					
    			}
				if(isParty(sk))
				{
					clientStopMoving(null);
					L2Object target = getAttackTarget();
					_actor.setTarget(_actor);
					_actor.doCast(sk);
    				_actor.setTarget(target);
    				return;    				
				}
    		}
        }
        }

        //-------------------------------------------------------------------------------
        //Immobilize Condition
        if((_actor.isRooted() && (dist > range || getAttackTarget().isMoving()))
        		||(dist > range && getAttackTarget().isMoving()))
        {
        	MovementDisable();
        	return;
        }
        timepass = 0;
        iscasting = false;
        //--------------------------------------------------------------------------------
        //Skill Use 
        if(_skillrender.hasSkill())
        {
        if(Rnd.get(100)<=((L2NpcInstance)_actor).getSkillChance())
        {
            L2Skill skills;
            boolean check = true;
            skills = _skillrender._generalskills.get(Rnd.nextInt(_skillrender._generalskills.size()));
            if(skills.getMpConsume()>_actor.getCurrentMp()
					|| _actor.isSkillDisabled(skills.getId())
					|| (skills.isMagic()&&_actor.isMuted())
					|| (!skills.isMagic()&&_actor.isPhysicalMuted()))
			{
				check = false;
			}
            if(check)
            	Cast(skills);
			if(iscasting)
			{
				return;
			}
			
    		for(L2Skill sk:_skillrender._generalskills)
    		{
				if(sk.getMpConsume()>_actor.getCurrentMp()
						||_actor.isSkillDisabled(sk.getId())
						||(sk.isMagic()&&_actor.isMuted())
						||(!sk.isMagic()&&_actor.isPhysicalMuted()))
				{
					continue;
				}
				Cast(sk);
				if(iscasting)
				{
					return;
				}
				else continue;
    		}
        }
        
        //--------------------------------------------------------------------------------
        //Long/Short Range skill Usage
        if(((L2NpcInstance)_actor).hasLSkill()||((L2NpcInstance)_actor).hasSSkill())
        {
        	if(((L2NpcInstance)_actor).hasSSkill()&& dist2 <= 150 && Rnd.get(100) <= ((L2NpcInstance)_actor).getSSkillChance())
        	{
        		
        		SSkillRender();
        		if(_skillrender._Srangeskills!=null)
        		for(L2Skill sk:_skillrender._Srangeskills)
        		{
    				if(sk.getMpConsume()>=_actor.getCurrentMp()
    						||_actor.isSkillDisabled(sk.getId())
    						||(sk.isMagic()&&_actor.isMuted())
    						||(!sk.isMagic()&&_actor.isPhysicalMuted()))
    				{
    					continue;
    				}
    				Cast(sk);
    				if(iscasting)
    					return;
    				else continue;
        		}
        	}
        	if(((L2NpcInstance)_actor).hasLSkill()&& dist2 >= 300 && Rnd.get(100) <= ((L2NpcInstance)_actor).getSSkillChance())
        	{
        		
        		LSkillRender();
        		if(_skillrender._Lrangeskills!=null)
        		for(L2Skill sk:_skillrender._Lrangeskills)
        		{
    				if(sk.getMpConsume()>=_actor.getCurrentMp()
    						||_actor.isSkillDisabled(sk.getId())
    						||(sk.isMagic()&&_actor.isMuted())
    						||(!sk.isMagic()&&_actor.isPhysicalMuted()))
    				{
    					continue;
    				}
    				Cast(sk);
    				if(iscasting)
    					return;
    				else continue;
        		}
        	}
        	
        }
        }

        
      //--------------------------------------------------------------------------------
      // Starts Melee or Primary Skill
        if(dist2>range || !GeoData.getInstance().canSeeTarget(_actor, getAttackTarget()))
        {
    		if (_actor.isRooted())
    		{
    			TargetReconsider();
    			return;
    		}
    		else
    		{
                if(getAttackTarget().isMoving())
                	range -=100;if(range<5)range=5;
	        	moveToPawn(getAttackTarget(), range);
	        	return;
    		}
        }
        else
        {
        	Melee(((L2NpcInstance)_actor).getPrimaryAttack());
        }
        
    }
    
    private void Melee(int type)
    {
    	if(type!=0)
    	{
    		boolean check = true;
	    	switch(type)
	    	{
	    		case -1:
	    		{
	    			if(_skillrender._generalskills != null)
	    			for(L2Skill sk : _skillrender._generalskills)
	    			{

	    					if(sk.getMpConsume()>=_actor.getCurrentMp()
	    							||_actor.isSkillDisabled(sk.getId())
	    							||(sk.isMagic()&&_actor.isMuted())
	    							||(!sk.isMagic()&&_actor.isPhysicalMuted()))
	        				{
	        					continue;
	        				}
    						Cast(sk);
	        				if(iscasting)
	        					return;
	    				
	    			}
	    			break;
	    		}
	    		case 1:
	    		{
	    			if(_skillrender.hasAtkSkill())
	    			for(L2Skill sk : _skillrender._atkskills)
	    			{
	    				
	    					if(sk.getMpConsume()>=_actor.getCurrentMp()
	    							||_actor.isSkillDisabled(sk.getId())
	    							||(sk.isMagic()&&_actor.isMuted())
	    							||(!sk.isMagic()&&_actor.isPhysicalMuted()))
	        				{
	        					continue;
	        				}
    						Cast(sk);
	        				if(iscasting)
	        					return;
	    				
	    			}
	    			break;
	    		}
	    		default:
	    		{
	    			if(_skillrender._generalskills != null)
	    			for(L2Skill sk : _skillrender._generalskills)
	    			{
	    				if(sk.getId() == ((L2NpcInstance)_actor).getPrimaryAttack())
	    				{
	    					if(sk.getMpConsume()>=_actor.getCurrentMp()
	    							||_actor.isSkillDisabled(sk.getId())
	    							||(sk.isMagic()&&_actor.isMuted())
	    							||(!sk.isMagic()&&_actor.isPhysicalMuted()))
	        				{
	        					check = false;
	        				}
	    					if(check)
	    						Cast(sk);
	        				if(iscasting)
	        					return;
	    				}
	    			}
	    		}
	    		break;
	    	}
    	}

    	_accessor.doAttack(getAttackTarget());
    }
    private void Cast(L2Skill sk)
    {
    	if(sk == null)
    		return;
    	if(getAttackTarget() == null)
    	if(((L2Attackable)_actor).getMostHated()!=null)
    		setAttackTarget(((L2Attackable)_actor).getMostHated());
    	if(getAttackTarget()==null)
    		return;
    	double dist = Math.sqrt(_actor.getPlanDistanceSq(getAttackTarget().getX(), getAttackTarget().getY()));
	    double dist2= dist + getAttackTarget().getTemplate().collisionRadius;
	    double range = _actor.getPhysicalAttackRange() + _actor.getTemplate().collisionRadius + getAttackTarget().getTemplate().collisionRadius;
	    double srange = sk.getCastRange() + _actor.getTemplate().collisionRadius;
	    if(getAttackTarget().isMoving())
	    	dist2 = dist2 - 400;

    	boolean cancast = true;
    	switch(sk.getSkillType())
    	{
    		
    		case BUFF:
			case REFLECT:
				{
					L2Effect[] effects = _actor.getAllEffects();
					for (int i = 0; effects != null && i < effects.length; i++)
					{
						L2Effect effect = effects[i];
						if (effect.getSkill() == sk)
						{
							cancast = false;
							break;
						}
					}
					if(cancast)
					{
						clientStopMoving(null);
    					iscasting=true;
    					//L2Object target = getAttackTarget();
    					_actor.setTarget(_actor);
    					_actor.doCast(sk);
        				//_actor.setTarget(target);
        				return;
					}
					//----------------------------------------
					//If actor already have buff, start looking at others same faction mob to cast
					if(sk.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF)
						return;
					if(sk.getTargetType() == L2Skill.SkillTargetType.TARGET_ONE)
					{
						L2Character target = EffectTargetReconsider(sk,true);
						if (target != null)
						{
							clientStopMoving(null);
	    					iscasting=true;
	    					L2Object targets = getAttackTarget();
	    					_actor.setTarget(target);
	    					_actor.doCast(sk);
	        				_actor.setTarget(targets);
	        				return;
						}
					}
					if(canParty(sk))
					{
							clientStopMoving(null);
	    					iscasting=true;
	    					L2Object targets = getAttackTarget();
	    					_actor.setTarget(_actor);
	    					_actor.doCast(sk);
	        				_actor.setTarget(targets);
	        				return;
						
					}
					break;
				}
			case HEAL:
			case HOT:
			case HEAL_PERCENT:
			case HEAL_STATIC:
			case BALANCE_LIFE:
			case BALANCE:
				{
					double percentage = _actor.getCurrentHp()/_actor.getMaxHp()*100;
		        	if(_actor instanceof L2MinionInstance && sk.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF)
		        	{
		        		L2Character leader = ((L2MinionInstance)_actor).getLeader();
		        		if(leader!=null &&!leader.isDead()&& Rnd.get(100)>leader.getCurrentHp()/leader.getMaxHp()*100)
		        		{
		    				if(!Util.checkIfInRange((sk.getCastRange()+_actor.getTemplate().collisionRadius + leader.getTemplate().collisionRadius),_actor,leader,false) 
		    						&& !isParty(sk) && !_actor.isRooted())
		    				{
		    					moveToPawn(leader,sk.getCastRange()
		    							+_actor.getTemplate().collisionRadius + leader.getTemplate().collisionRadius);
		    				}
		    				if(GeoData.getInstance().canSeeTarget(_actor,leader))
		    				{
		    					clientStopMoving(null);
		    					iscasting=true;
		    					_actor.setTarget(leader);
		    					_actor.doCast(sk);
		        				return;
		    				}
		        		}        	
		        	}    
		        		if(Rnd.get(100)<(100-percentage)/3)
		        		{
		        				clientStopMoving(null);
		        				iscasting=true;
		    					_actor.setTarget(_actor);
		    					_actor.doCast(sk);
		        				return;        				
		        		}

		        		if(sk.getTargetType() == L2Skill.SkillTargetType.TARGET_ONE)
						for(L2Character obj:  _actor.getKnownList().getKnownCharactersInRadius(sk.getCastRange()+_actor.getTemplate().collisionRadius))
		    			{
							if(!(obj instanceof L2Attackable)||obj.isDead())
								continue;

							L2Attackable targets = ((L2Attackable)obj);
							if(((L2Attackable)_actor).getFactionId()!= targets.getFactionId()&&((L2Attackable)_actor).getFactionId()!=null)
								continue;
		  		        	percentage = targets.getCurrentHp()/targets.getMaxHp()*100;
							if(Rnd.get(100)< (100-percentage)/10)
							{
			    				if(GeoData.getInstance().canSeeTarget(_actor,targets))
			    				{
			    					clientStopMoving(null);
			    					iscasting=true;
			    					_actor.setTarget(obj);
			    					_actor.doCast(sk);
			        				return;
			    				}
							}    					
		    			}
						if(isParty(sk))
						{
							for(L2Character obj:  _actor.getKnownList().getKnownCharactersInRadius(sk.getSkillRadius()+_actor.getTemplate().collisionRadius))
			    			{
								if(!(obj instanceof L2Attackable))
			        			{
			        					continue;
			        			}
								L2NpcInstance targets = ((L2NpcInstance)obj);
			    				L2NpcInstance actors = ((L2NpcInstance)_actor);
			    				if(targets.getFactionId() == actors.getFactionId() && actors.getFactionId() !=null)
			    				{
									if(obj.getCurrentHp()<obj.getMaxHp() && Rnd.get(100)<=20)
									{
										clientStopMoving(null);
										iscasting=true;
										_actor.setTarget(_actor);
										_actor.doCast(sk);
					    				return;    	
									}
			    				}
			    			}
						}
						break;
				}
			case RESURRECT:
				{
						if(!isParty(sk))
						{
							if(_actor instanceof L2MinionInstance && sk.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF)
				        	{
				        		L2Character leader = ((L2MinionInstance)_actor).getLeader();
				        		if(leader!=null &&leader.isDead())
				    				if(!Util.checkIfInRange((sk.getCastRange()+_actor.getTemplate().collisionRadius + leader.getTemplate().collisionRadius),_actor,leader,false) 
				    						&& !isParty(sk) && !_actor.isRooted())
				    				{
				    					moveToPawn(leader,sk.getCastRange()
				    							+_actor.getTemplate().collisionRadius + leader.getTemplate().collisionRadius);
				    				}
				    				if(GeoData.getInstance().canSeeTarget(_actor,leader))
				    				{
				    					clientStopMoving(null);
				    					iscasting=true;
				    					_actor.setTarget(((L2MinionInstance)_actor).getLeader());
				    					_actor.doCast(sk);
				        				return;
				    				}
				        	}

							for(L2Character obj:  _actor.getKnownList().getKnownCharactersInRadius(sk.getCastRange()+_actor.getTemplate().collisionRadius))
			    			{
								if(!(obj instanceof L2Attackable) || !obj.isDead())
									continue;
	
								L2Attackable targets = ((L2Attackable)obj);
								if(((L2Attackable)_actor).getFactionId()!= targets.getFactionId() && ((L2Attackable)_actor).getFactionId()!=null)
									continue;
								if(Rnd.get(100)< 10)
								{
				    				if(GeoData.getInstance().canSeeTarget(_actor,targets))
				    				{
				    					clientStopMoving(null);
				    					iscasting=true;
				    					_actor.setTarget(obj);
				    					_actor.doCast(sk);
				        				return;
				    				}
								}    	
			    			}
						}
						else if(isParty(sk))
						{
							for(L2Character obj:  _actor.getKnownList().getKnownCharactersInRadius(sk.getSkillRadius()+_actor.getTemplate().collisionRadius))
			    			{
								if(!(obj instanceof L2Attackable))
			        			{
			        					continue;
			        			}
								L2NpcInstance targets = ((L2NpcInstance)obj);
			    				L2NpcInstance actors = ((L2NpcInstance)_actor);
			    				if(targets.getFactionId() == actors.getFactionId() && actors.getFactionId() !=null)
			    				{
									if(obj.getCurrentHp()<obj.getMaxHp() && Rnd.get(100)<=20)
									{
										clientStopMoving(null);
										iscasting=true;
										_actor.setTarget(_actor);
										_actor.doCast(sk);
					    				return;    	
									}
			    				}
			    			}
						}
						break;
				}
			case DEBUFF:
			case WEAKNESS:
			case POISON:
			case DOT:
			case MDOT:
			case BLEED:
				{
					if(GeoData.getInstance().canSeeTarget(_actor,getAttackTarget())
							&& !canAOE(sk) && !getAttackTarget().isDead() && dist2<=srange)
					{
						L2Effect[] effects = (getAttackTarget()).getAllEffects();
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
							iscasting=true;
							_actor.doCast(sk);
							return;
						}
					}
					else if(canAOE(sk))
					{
				    	if(sk.getTargetType() == SkillTargetType.TARGET_AURA 
				    			|| sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA
				    			|| sk.getTargetType() == SkillTargetType.TARGET_FRONT_AURA)
				    	{
				    		clientStopMoving(null);
							iscasting=true;
							//L2Object target = getAttackTarget();
							//_actor.setTarget(_actor);
							_actor.doCast(sk);
							//_actor.setTarget(target);
							return;
				    	}
				    	if((sk.getTargetType() == SkillTargetType.TARGET_AURA 
				    			|| sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AREA
				    			|| sk.getTargetType() == SkillTargetType.TARGET_FRONT_AREA				
				    			|| sk.getTargetType() == SkillTargetType.TARGET_MULTIFACE)
				    			&& GeoData.getInstance().canSeeTarget(_actor,getAttackTarget()) && !getAttackTarget().isDead() && dist2<=srange)
				    	{
				    		clientStopMoving(null);
							iscasting=true;
							_actor.doCast(sk);
							return;
				    	}
					}
					else if(sk.getTargetType() == SkillTargetType.TARGET_ONE)
					{
						L2Character target = EffectTargetReconsider(sk,false);
						if (target != null)
						{
							clientStopMoving(null);
							iscasting=true;
							_actor.doCast(sk);
							return;
						}
					}
					break;
				}
			case SLEEP:
				{
					if(sk.getTargetType() == SkillTargetType.TARGET_ONE)
					{
						
				        if(getAttackTarget()!=null && !getAttackTarget().isDead() && dist2<=srange)
				        {
				        	

				            
				            if(dist2>range || getAttackTarget().isMoving())
				            {
				            	L2Effect[] effects = (getAttackTarget()).getAllEffects();
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
									iscasting=true;
									//_actor.setTarget(getAttackTarget());
									_actor.doCast(sk);
									return;
								}
				            }
				        }


						L2Character target = EffectTargetReconsider(sk,false);
						if (target != null)
						{
							clientStopMoving(null);
							iscasting=true;
							_actor.doCast(sk);
							return;
						}
					}
					else if(canAOE(sk))
					{
				    	if(sk.getTargetType() == SkillTargetType.TARGET_AURA 
				    			|| sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA
				    			|| sk.getTargetType() == SkillTargetType.TARGET_FRONT_AURA)
				    	{
				    		clientStopMoving(null);
							iscasting=true;
							//L2Object target = getAttackTarget();
							//_actor.setTarget(_actor);
							_actor.doCast(sk);
							//_actor.setTarget(target);
							return;
				    	}
				    	if((sk.getTargetType() == SkillTargetType.TARGET_AREA 
				    			|| sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AREA
				    			|| sk.getTargetType() == SkillTargetType.TARGET_FRONT_AREA				
				    			|| sk.getTargetType() == SkillTargetType.TARGET_MULTIFACE)
			    			&& GeoData.getInstance().canSeeTarget(_actor,getAttackTarget()) && !getAttackTarget().isDead() && dist2<=srange)
				    	{
				    		clientStopMoving(null);
							iscasting=true;
							_actor.doCast(sk);
							return;
				    	}
					}
					break;
				}
			case ROOT:
			case STUN:
			case PARALYZE:
				{
					if(GeoData.getInstance().canSeeTarget(_actor,getAttackTarget()) && !canAOE(sk))
					{
						L2Effect[] effects = (getAttackTarget()).getAllEffects();
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
							_actor.doCast(sk);
							return;
						}
					}
					else if(canAOE(sk))
					{
				    	if(sk.getTargetType() == SkillTargetType.TARGET_AURA 
				    			|| sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA
				    			|| sk.getTargetType() == SkillTargetType.TARGET_FRONT_AURA)
				    	{
				    		clientStopMoving(null);
							iscasting=true;
							//L2Object target = getAttackTarget();
							//_actor.setTarget(_actor);
							_actor.doCast(sk);
							//_actor.setTarget(target);
							return;
				    	}
				    	else if((sk.getTargetType() == SkillTargetType.TARGET_AREA 
				    			|| sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AREA
				    			|| sk.getTargetType() == SkillTargetType.TARGET_FRONT_AREA				
				    			|| sk.getTargetType() == SkillTargetType.TARGET_MULTIFACE)
				    			&& GeoData.getInstance().canSeeTarget(_actor,getAttackTarget()) && !getAttackTarget().isDead() && dist2<=srange)
				    	{
				    		clientStopMoving(null);
							iscasting=true;
							_actor.doCast(sk);
							return;
				    	}
					}
					else if(sk.getTargetType() == SkillTargetType.TARGET_ONE)
					{
						L2Character target = EffectTargetReconsider(sk,false);
						if (target != null)
						{
							clientStopMoving(null);
							iscasting=true;
							_actor.doCast(sk);
							return;
						}
					}
					break;
				}
			case MUTE:
			case FEAR:
				{
					if(GeoData.getInstance().canSeeTarget(_actor,getAttackTarget()) && !canAOE(sk))
					{
						L2Effect[] effects = (getAttackTarget()).getAllEffects();
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
							_actor.doCast(sk);
							return;
						}
					}
					else if(canAOE(sk))
					{
				    	if(sk.getTargetType() == SkillTargetType.TARGET_AURA 
				    			|| sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA
				    			|| sk.getTargetType() == SkillTargetType.TARGET_FRONT_AURA)
				    	{
				    		clientStopMoving(null);
							iscasting=true;
							//L2Object target = getAttackTarget();
							//_actor.setTarget(_actor);
							_actor.doCast(sk);
							//_actor.setTarget(target);
							return;
				    	}
				    	if((sk.getTargetType() == SkillTargetType.TARGET_AURA 
				    			|| sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA
				    			|| sk.getTargetType() == SkillTargetType.TARGET_FRONT_AURA				
				    			|| sk.getTargetType() == SkillTargetType.TARGET_MULTIFACE)
			    			&& GeoData.getInstance().canSeeTarget(_actor,getAttackTarget()) && !getAttackTarget().isDead() && dist2<=srange)
				    	{
				    		clientStopMoving(null);
							iscasting=true;
							_actor.doCast(sk);
							return;
				    	}
					}
					else if(sk.getTargetType() == SkillTargetType.TARGET_ONE)
					{
						L2Character target = EffectTargetReconsider(sk,false);
						if (target != null)
						{
							clientStopMoving(null);
							iscasting=true;
							_actor.doCast(sk);
							return;
						}
					}
					break;
				}
			case CANCEL:
			case NEGATE:
				{
					if(sk.getTargetType() == SkillTargetType.TARGET_ONE)
					{
						L2Effect[] effects = (getAttackTarget()).getAllEffects();
						if(effects==null || effects.length == 0 || !GeoData.getInstance().canSeeTarget(_actor,getAttackTarget()) && !getAttackTarget().isDead() && dist2<=srange)
							cancast = false;
						if(cancast)
						{
							clientStopMoving(null);
							iscasting=true;
							//L2Object target = getAttackTarget();
							//_actor.setTarget(_actor);
							_actor.doCast(sk);
							//_actor.setTarget(target);
							return;
						}
						L2Character target = EffectTargetReconsider(sk,false);
						if (target != null)
						{
							clientStopMoving(null);
							iscasting=true;
							L2Object targets = getAttackTarget();
							_actor.setTarget(target);
							_actor.doCast(sk);
							_actor.setTarget(targets);
							return;
						}
					}
					else if(canAOE(sk))
					{
				    	if((sk.getTargetType() == SkillTargetType.TARGET_AURA 
				    			|| sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA
				    			|| sk.getTargetType() == SkillTargetType.TARGET_FRONT_AURA				
				    			|| sk.getTargetType() == SkillTargetType.TARGET_MULTIFACE)
			    			&& GeoData.getInstance().canSeeTarget(_actor,getAttackTarget()))
				    		
				    	{
				    		clientStopMoving(null);
							iscasting=true;
							//L2Object target = getAttackTarget();
							//_actor.setTarget(_actor);
							_actor.doCast(sk);
							//_actor.setTarget(target);
							return;
				    	}
				    	else if((sk.getTargetType() == SkillTargetType.TARGET_AREA 
				    			|| sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA
				    			|| sk.getTargetType() == SkillTargetType.TARGET_FRONT_AURA				
				    			|| sk.getTargetType() == SkillTargetType.TARGET_MULTIFACE)
			    			&& GeoData.getInstance().canSeeTarget(_actor,getAttackTarget()) && !getAttackTarget().isDead() && dist2<=srange)
				    	{
				    		clientStopMoving(null);
							iscasting=true;
							_actor.doCast(sk);
							return;
				    	}
					}
					break;
				}
			case PDAM:
			case MDAM:
			case BLOW:
			case DRAIN:
			case CHARGEDAM:
			case DEATHLINK:
			case CPDAM:
			case MANADAM:
				{
					if(!canAura(sk))
					{
						if(GeoData.getInstance().canSeeTarget(_actor,getAttackTarget()) && !getAttackTarget().isDead() && dist2<=srange)
						{
							clientStopMoving(null);
							iscasting=true;
							_actor.doCast(sk);
							return;
						}
						else
						{
							L2Character target = SkillTargetReconsider(sk);
							if (target != null)
							{
								clientStopMoving(null);
								iscasting=true;
								L2Object targets = getAttackTarget();
								_actor.setTarget(target);
								_actor.doCast(sk);
								_actor.setTarget(targets);
								return;
							}
						}
					}
					else
					{
						clientStopMoving(null);
						iscasting=true;
						_actor.doCast(sk);
						return;
					}
					break;
				}
			default :
				{
					if(!canAura(sk))
					{
						
						if(GeoData.getInstance().canSeeTarget(_actor,getAttackTarget()) && !getAttackTarget().isDead() && dist2<=srange)
						{
							clientStopMoving(null);
							iscasting=true;
							_actor.doCast(sk);
							return;
						}
						else
						{
							L2Character target = SkillTargetReconsider(sk);
							if (target != null)
							{
								clientStopMoving(null);
								iscasting=true;
								L2Object targets = getAttackTarget();
								_actor.setTarget(target);
								_actor.doCast(sk);
								_actor.setTarget(targets);
								return;
							}
						}
					}
					else
					{
						clientStopMoving(null);
						iscasting=true;
						//L2Object targets = getAttackTarget();
						//_actor.setTarget(_actor);
						_actor.doCast(sk);
						//_actor.setTarget(targets);
						return;
					}
					
				}
				break;
				

    				
    		
    	}
    	
    	return;
    }
    
    /**
     * This AI task will start when ACTOR cannot move and attack range larger than distance
     */
    private void MovementDisable()
    {
    	
        double dist = 0;
        double dist2 = 0;
        int range = 0;
        boolean cancast = true;
        try
        {
        	if(_actor.getTarget() == null)
            _actor.setTarget(getAttackTarget());
            dist = Math.sqrt(_actor.getPlanDistanceSq(getAttackTarget().getX(), getAttackTarget().getY()));
            dist2= dist;
            range = _actor.getPhysicalAttackRange() + _actor.getTemplate().collisionRadius + getAttackTarget().getTemplate().collisionRadius;
            if(getAttackTarget().isMoving())
            {
            		dist = dist -30;
            	if(_actor.isMoving())
            		dist = dist -50;
            }
        }
        catch (NullPointerException e)
        {
            setIntention(AI_INTENTION_ACTIVE);
            return;
        }
        
        //Check if activeChar has any skill
        if(_skillrender.hasSkill())
        {
	    	//-------------------------------------------------------------
	        //Try to stop the target or disable the target as priority
			if(_skillrender.hasImmobiliseSkill())
			{
				for(L2Skill sk:_skillrender._immobiliseskills)
				{
					if(sk.getMpConsume()>=_actor.getCurrentMp()
							|| _actor.isSkillDisabled(sk.getId())
							||(sk.getCastRange()+ _actor.getTemplate().collisionRadius + getAttackTarget().getTemplate().collisionRadius <= dist2 && !canAura(sk))
							||(sk.isMagic()&&_actor.isMuted())
							||(!sk.isMagic()&&_actor.isPhysicalMuted()))
					{
						continue;
					}
					if(!GeoData.getInstance().canSeeTarget(_actor,getAttackTarget()))
						continue;
					L2Effect[] effects = (getAttackTarget()).getAllEffects();
					for (int i = 0; effects != null && i < effects.length; i++)
					{
						L2Effect effect = effects[i];
						if (effect.getSkill() == sk)
						{
							cancast = false;
							break;
						}
					}
					if(cancast)
					{
						clientStopMoving(null);
						//L2Object target = getAttackTarget();
						//_actor.setTarget(_actor);
						_actor.doCast(sk);
	    				//_actor.setTarget(target);
						return;
					}
				}
			}
			//-------------------------------------------------------------
			//Same as Above, but with Mute/FEAR etc....
			if(_skillrender.hasCOTSkill())
			{
				for(L2Skill sk:_skillrender._cotskills)
				{
					if(sk.getMpConsume()>=_actor.getCurrentMp()
							|| _actor.isSkillDisabled(sk.getId())
							||(sk.getCastRange()+ _actor.getTemplate().collisionRadius + getAttackTarget().getTemplate().collisionRadius <= dist2 && !canAura(sk))
							||(sk.isMagic()&&_actor.isMuted())
							||(!sk.isMagic()&&_actor.isPhysicalMuted()))
					{
						continue;
					}
					if(!GeoData.getInstance().canSeeTarget(_actor,getAttackTarget()))
						continue;
					L2Effect[] effects = (getAttackTarget()).getAllEffects();
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
						//L2Object target = getAttackTarget();
						//_actor.setTarget(_actor);
						_actor.doCast(sk);
	    				//_actor.setTarget(target);
						return;
					}
				}
			}        		
			//-------------------------------------------------------------
			if(_skillrender.hasDebuffSkill()&&Rnd.get(10)<=2)
			{
				for(L2Skill sk:_skillrender._debuffskills)
				{
					if(sk.getMpConsume()>=_actor.getCurrentMp()
							|| _actor.isSkillDisabled(sk.getId())
							||(sk.getCastRange()+ _actor.getTemplate().collisionRadius + getAttackTarget().getTemplate().collisionRadius <= dist2 && !canAura(sk))
							||(sk.isMagic()&&_actor.isMuted())
							||(!sk.isMagic()&&_actor.isPhysicalMuted()))
					{
						continue;
					}
					if(!GeoData.getInstance().canSeeTarget(_actor,getAttackTarget()))
						continue;
					L2Effect[] effects = (getAttackTarget()).getAllEffects();
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
						//L2Object target = getAttackTarget();
						//_actor.setTarget(_actor);
						_actor.doCast(sk);
	    				//_actor.setTarget(target);
						return;
					}
				}
			}
			//-------------------------------------------------------------
			//Some side effect skill like CANCEL or NEGATE
			if(_skillrender.hasNegativeSkill()&&Rnd.get(10)==1)
			{
				for(L2Skill sk:_skillrender._negativeskills)
				{
					if(sk.getMpConsume()>=_actor.getCurrentMp()
							|| _actor.isSkillDisabled(sk.getId())
							||(sk.getCastRange()+ _actor.getTemplate().collisionRadius + getAttackTarget().getTemplate().collisionRadius <= dist2 && !canAura(sk))
							||(sk.isMagic()&&_actor.isMuted())
							||(!sk.isMagic()&&_actor.isPhysicalMuted()))
					{
						continue;
					}
					if(!GeoData.getInstance().canSeeTarget(_actor,getAttackTarget()))
						continue;
					L2Effect[] effects = (getAttackTarget()).getAllEffects();
					if(effects==null || effects.length == 0)
						continue;
					clientStopMoving(null);
					//L2Object target = getAttackTarget();
					//_actor.setTarget(_actor);
					_actor.doCast(sk);
					//_actor.setTarget(target);
					return;
				}
			}
			//-------------------------------------------------------------
			//Start ATK SKILL when nothing can be done
			if(_skillrender.hasAtkSkill())
			{
				for(L2Skill sk:_skillrender._atkskills)
				{
					if(sk.getMpConsume()>=_actor.getCurrentMp()
							|| _actor.isSkillDisabled(sk.getId())
							||(sk.getCastRange()+ _actor.getTemplate().collisionRadius + getAttackTarget().getTemplate().collisionRadius <= dist2 && !canAura(sk))
							||(sk.isMagic()&&_actor.isMuted())
							||(!sk.isMagic()&&_actor.isPhysicalMuted()))
					{
						continue;
					}
					if(!GeoData.getInstance().canSeeTarget(_actor,getAttackTarget()))
						continue;
					clientStopMoving(null);
					//L2Object target = getAttackTarget();
					//_actor.setTarget(_actor);
					_actor.doCast(sk);
					//_actor.setTarget(target);
					return;
				}
			}
			//-------------------------------------------------------------
			//if there is no ATK skill to use, then try Universal skill
			/*
			if(_skillrender.hasUniversalSkill())
			{
				for(L2Skill sk:_skillrender._universalskills)
				{
					if(sk.getMpConsume()>=_actor.getCurrentMp()
							|| _actor.isSkillDisabled(sk.getId())
							||(sk.getCastRange()+ _actor.getTemplate().collisionRadius + getAttackTarget().getTemplate().collisionRadius <= dist2 && !canAura(sk))
							||(sk.isMagic()&&_actor.isMuted())
							||(!sk.isMagic()&&_actor.isPhysicalMuted()))
					{
						continue;
					}
					if(!GeoData.getInstance().canSeeTarget(_actor,getAttackTarget()))
						continue;
					clientStopMoving(null);
					L2Object target = getAttackTarget();
					//_actor.setTarget(_actor);
					_actor.doCast(sk);
					//_actor.setTarget(target);
					return;
				}
			}
			*/
        }
		timepass = timepass + 1;
		if (_actor.isRooted())
		{
			timepass = 0;
			TargetReconsider();
			return;
		}
		else if(timepass>=5)
		{
			timepass = 0;
			AggroReconsider();
			return;
		}
		
        if(dist>range || !GeoData.getInstance().canSeeTarget(_actor, getAttackTarget()))
        {
            if(getAttackTarget().isMoving())
            	range -=100;if(range<5)range=5;
	        	moveToPawn(getAttackTarget(), range);
	        	return;
    		
        }


    	Melee(((L2NpcInstance)_actor).getPrimaryAttack());
    	return;
    }

    private L2Character EffectTargetReconsider(L2Skill sk, boolean positive)
    {	
    	if (sk == null)
    		return null;
    	if(sk.getSkillType() != L2Skill.SkillType.NEGATE || sk.getSkillType() != L2Skill.SkillType.CANCEL )
    	{
    		if(!positive)
	    	{
	    		boolean cancast = true;
	            double dist = 0;
	            double dist2 = 0;
	            int range = 0;
	        	L2Attackable actor = (L2Attackable)_actor;
	        	
	        	if(actor.getAttackByList()!=null)
	        	for(L2Character obj: actor.getAttackByList())
	        	{
	        		if(obj.isDead() || obj == null || !GeoData.getInstance().canSeeTarget(_actor,obj) || obj == getAttackTarget())
	        			continue;
	                try
	                {
	                    _actor.setTarget(getAttackTarget());
	                    dist = Math.sqrt(_actor.getPlanDistanceSq(obj.getX(), obj.getY()));
	                    dist2= dist;
	                    range = sk.getCastRange() + _actor.getTemplate().collisionRadius + obj.getTemplate().collisionRadius;
	                    if(obj.isMoving())
	                    	dist2 = dist2 - 70;
	                }
	                catch (NullPointerException e)
	                {
	                    continue;
	                }
	                if(dist2<=range)
	                {
						L2Effect[] effects = (getAttackTarget()).getAllEffects();
						for (int i = 0; effects != null && i < effects.length; i++)
						{
							L2Effect effect = effects[i];
							if (effect.getSkill() == sk)
							{
								cancast = false;
								break;
							}
						}
						if(cancast)
							return obj;
	                }
	        	}
	        	
	        	//----------------------------------------------------------------------
	        	//If there is nearby Target with aggro, start going on random target that is attackable
	         	 for (L2Character obj : _actor.getKnownList().getKnownCharactersInRadius(range))
	             {
	          		 if(obj.isDead() || obj == null || !GeoData.getInstance().canSeeTarget(_actor,obj))
	    				continue;
	                 try
	                 {
	                     _actor.setTarget(getAttackTarget());
	                     dist = Math.sqrt(_actor.getPlanDistanceSq(obj.getX(), obj.getY()));
	                     dist2= dist;
	                     range = sk.getCastRange() + _actor.getTemplate().collisionRadius + obj.getTemplate().collisionRadius;
	                     if(obj.isMoving())
	                     	dist2 = dist2 - 70;
	                 }
	                 catch (NullPointerException e)
	                 {
	                     continue;
	                 }
	          		 if(obj instanceof L2Attackable)
	          		 {
	        		     if(((L2Attackable)_actor).getEnemyClan()!=null)
	            			 if((((L2Attackable)_actor).getEnemyClan().equals(((L2Attackable)obj).getClan())) &&((L2Attackable)_actor).getEnemyClan() != "none")
	            			 {
	        	                 if(dist2<=range)
	        	                 {
	        	 					L2Effect[] effects = (getAttackTarget()).getAllEffects();
	        	 					for (int i = 0; effects != null && i < effects.length; i++)
	        	 					{
	        	 						L2Effect effect = effects[i];
	        	 						if (effect.getSkill() == sk)
	        	 						{
	        	 							cancast = false;
	        	 							break;
	        	 						}
	        	 					}
	        	 					if(cancast)
	        	 						return obj;
	        	                 }
	            			 }
	          		 }
	        		 if(obj instanceof L2PcInstance || obj instanceof L2Summon)
	        		 {
	
		                 if(dist2<=range)
		                 {
		 					L2Effect[] effects = (getAttackTarget()).getAllEffects();
		 					for (int i = 0; effects != null && i < effects.length; i++)
		 					{
		 						L2Effect effect = effects[i];
		 						if (effect.getSkill() == sk)
		 						{
		 							cancast = false;
		 							break;
		 						}
		 					}
		 					if(cancast)
		 						return obj;
		                 }
	        		 }
	             }
	        		 
	    	}
	    	else if (positive)
	    	{
	    		boolean cancast = true;
	            double dist = 0;
	            double dist2 = 0;
	            int range = 0;
	        	for(L2Character obj: _actor.getKnownList().getKnownCharactersInRadius(range))
	        	{
	        		if(!(obj instanceof L2Attackable) || obj.isDead() || obj == null || !GeoData.getInstance().canSeeTarget(_actor,obj))
	        			continue;
	        		
					L2Attackable targets = ((L2Attackable)obj);
					if(((L2Attackable)_actor).getFactionId()!= targets.getFactionId() && ((L2Attackable)_actor).getFactionId()!=null)
						continue;
					
	                try
	                {
	                    _actor.setTarget(getAttackTarget());
	                    dist = Math.sqrt(_actor.getPlanDistanceSq(obj.getX(), getAttackTarget().getY()));
	                    dist2= dist;
	                    range = sk.getCastRange() + _actor.getTemplate().collisionRadius + obj.getTemplate().collisionRadius;
	                    if(obj.isMoving())
	                    	dist2 = dist2 - 70;
	                }
	                catch (NullPointerException e)
	                {
	                    continue;
	                }
	                if(dist2<=range)
	                {
						L2Effect[] effects = (obj).getAllEffects();
						for (int i = 0; effects != null && i < effects.length; i++)
						{
							L2Effect effect = effects[i];
							if (effect.getSkill() == sk)
							{
								cancast = false;
								break;
							}
						}
						if(cancast)
							return obj;
	                }
	        	}
	    	}
	    	return null;
    	}
    	else
    	{
    		boolean cancast = false;
            double dist = 0;
            double dist2 = 0;
            int range = 0;
        	range = sk.getCastRange() + _actor.getTemplate().collisionRadius + getAttackTarget().getTemplate().collisionRadius;
        	 for (L2Character obj : _actor.getKnownList().getKnownCharactersInRadius(range))
             {
          		 if(obj.isDead() || obj == null || !GeoData.getInstance().canSeeTarget(_actor,obj))
    				continue;
                 try
                 {
                     _actor.setTarget(getAttackTarget());
                     dist = Math.sqrt(_actor.getPlanDistanceSq(obj.getX(), getAttackTarget().getY()));
                     dist2= dist;
                     range = sk.getCastRange() + _actor.getTemplate().collisionRadius + obj.getTemplate().collisionRadius;
                     if(obj.isMoving())
                     	dist2 = dist2 - 70;
                 }
                 catch (NullPointerException e)
                 {
                     continue;
                 }
          		 if(obj instanceof L2Attackable)
          		 {
        		     if(((L2Attackable)_actor).getEnemyClan()!=null)
            			 if((((L2Attackable)_actor).getEnemyClan().equals(((L2Attackable)obj).getClan())) &&((L2Attackable)_actor).getEnemyClan() != "none")
            			 {
        	                 if(dist2<=range)
        	                 {
        	 					L2Effect[] effects = (getAttackTarget()).getAllEffects();
        	 					if (effects.length >0)
        	 						cancast = true;
        	 					if(cancast)
        	 						return obj;
        	                 }
            			 }
          		 }
        		 if(obj instanceof L2PcInstance || obj instanceof L2Summon)
        		 {

	                 if(dist2<=range)
	                 {
	 					L2Effect[] effects = (getAttackTarget()).getAllEffects();
	 					if (effects.length >0)
	 						cancast = true;
	 					if(cancast)
	 						return obj;
	                 }
        		 }
             }
        	 return null;
    	}
    }
    private L2Character SkillTargetReconsider(L2Skill sk)
    {
        double dist = 0;
        double dist2 = 0;
        int range = 0;
    	L2Attackable actor = (L2Attackable)_actor;
    	
    	if(actor.getAttackByList()!=null)
    	for(L2Character obj: actor.getAttackByList())
    	{
    		if(obj == null || !GeoData.getInstance().canSeeTarget(_actor,obj) || obj.isDead())
				continue;
            try
            {
                _actor.setTarget(getAttackTarget());
                dist = Math.sqrt(_actor.getPlanDistanceSq(obj.getX(), getAttackTarget().getY()));
                dist2= dist;
                range = sk.getCastRange() + _actor.getTemplate().collisionRadius + getAttackTarget().getTemplate().collisionRadius;
                if(obj.isMoving())
                	dist2 = dist2 - 70;
            }
            catch (NullPointerException e)
            {
                continue;
            }
            if(dist2<=range)
            {
            	return obj;
            }
    	}
    	if(!(_actor instanceof L2GuardInstance))
      	 for (L2Character obj : _actor.getKnownList().getKnownCharactersInRadius(range))
         {
      		 if(obj == null || !GeoData.getInstance().canSeeTarget(_actor,obj))
				continue;
    		 if(obj instanceof L2PcInstance)
    		 {
             	return obj;
            	
    		 }
    		 if(obj instanceof L2Attackable)
    		 {
    		     if(((L2Attackable)_actor).getEnemyClan()!=null)
    			 if((((L2Attackable)_actor).getEnemyClan().equals(((L2Attackable)obj).getClan())) &&((L2Attackable)_actor).getEnemyClan() != "none")
    			 {
    	            	return obj;
    			 }
    		     if (((L2Attackable)_actor).getIsChaos() != 0)
    		     {
    		    	 if(((L2Attackable)obj).getFactionId() == ((L2Attackable)_actor).getFactionId() && ((L2Attackable)obj).getFactionId()!=null)
    		    		 continue;
    		    	 else
    		            return obj;
    		     }
    		 }
    		 if(obj instanceof L2Summon)
    		 {
    			 
             	return obj;
    		 }
    		 
    		 
         }
      	 return null;
    }
    private void TargetReconsider()
    {
        double dist = 0;
        double dist2 = 0;
        int range = 0;
    	L2Attackable actor = (L2Attackable)_actor;
    	L2Character MostHate = ((L2Attackable) _actor).getMostHated();
    	if(actor.getHateList()!=null)
    	for(L2Character obj: actor.getHateList())
    	{
    		if(obj == null || !GeoData.getInstance().canSeeTarget(_actor,obj) || obj.isDead() || obj!= MostHate || obj == _actor || obj == getAttackTarget())
				continue;
            try
            {
                dist = Math.sqrt(_actor.getPlanDistanceSq(obj.getX(), obj.getY()));
                dist2= dist;
                range = _actor.getPhysicalAttackRange() + _actor.getTemplate().collisionRadius + obj.getTemplate().collisionRadius;
                if(obj.isMoving())
                	dist2 = dist2 - 70;
            }
            catch (NullPointerException e)
            {
                continue;
            }
            
            if(dist2<=range)
            {
            	if(MostHate!=null)
            		actor.addDamageHate(obj,actor.getHating(MostHate),actor.getHating(MostHate));
            	else
            		actor.addDamageHate(obj,2000,2000);
            	_actor.setTarget(obj);
            	setAttackTarget(obj);
            	return;
            }
    	}
    	if(!(_actor instanceof L2GuardInstance))
      	 for (L2Character obj : _actor.getKnownList().getKnownCharactersInRadius(range))
         {
      		if(obj == null || !GeoData.getInstance().canSeeTarget(_actor,obj) || obj.isDead() || obj!= MostHate || obj == _actor || obj == getAttackTarget())
				continue;
    		 if(obj instanceof L2PcInstance)
    		 {
             	if(MostHate!=null)
            		actor.addDamageHate(obj,actor.getHating(MostHate),actor.getHating(MostHate));
            	else
            		actor.addDamageHate(obj,2000,2000);
            	_actor.setTarget(obj);
            	setAttackTarget(obj);
            	
    		 }
    		 if(obj instanceof L2Attackable)
    		 {
    		     if(((L2Attackable)_actor).getEnemyClan()!=null)
    			 if((((L2Attackable)_actor).getEnemyClan().equals(((L2Attackable)obj).getClan())) &&((L2Attackable)_actor).getEnemyClan() != "none")
    			 {
    	             	actor.addDamageHate(obj,0,actor.getHating(MostHate));
    	            	_actor.setTarget(obj);
    			 }
    		     if (((L2Attackable)_actor).getIsChaos() != 0)
    		     {
    		    	 if(((L2Attackable)obj).getFactionId() == ((L2Attackable)_actor).getFactionId() && ((L2Attackable)obj).getFactionId()!=null)
    		    		 continue;
    		    	 else
    		    	 {
    		            	if(MostHate!=null)
    		            		actor.addDamageHate(obj,actor.getHating(MostHate),actor.getHating(MostHate));
    		            	else
    		            		actor.addDamageHate(obj,2000,2000);
    		            	_actor.setTarget(obj);
    		            	setAttackTarget(obj);
    		    	 }
    		     }
    		 }
    		 if(obj instanceof L2Summon)
    		 {
    			 
             	if(MostHate!=null)
            		actor.addDamageHate(obj,actor.getHating(MostHate),actor.getHating(MostHate));
            	else
            		actor.addDamageHate(obj,2000,2000);
            	_actor.setTarget(obj);
            	setAttackTarget(obj);
    		 }
    		 
    		 
         }
    }
    
    
    private void AggroReconsider()
    {

    	L2Attackable actor = (L2Attackable)_actor;
    	L2Character MostHate = ((L2Attackable) _actor).getMostHated();
    	if(actor.getHateList()!=null)
    	for(L2Character obj: actor.getHateList())
    	{
    		if(obj == null || !GeoData.getInstance().canSeeTarget(_actor,obj) || obj.isDead()|| obj == getAttackTarget() || obj == actor)
				continue;
            try
            {
                _actor.setTarget(getAttackTarget());

            }
            catch (NullPointerException e)
            {
                continue;
            }
            	if(MostHate!=null)
            		actor.addDamageHate(obj,actor.getHating(MostHate),actor.getHating(MostHate));
            	else
            		actor.addDamageHate(obj,2000,2000);
            	_actor.setTarget(obj);
            	setAttackTarget(obj);
            	return;
            
    	}
    	if(!(_actor instanceof L2GuardInstance))
      	 for (L2Character obj : _actor.getKnownList().getKnownCharactersInRadius(1000))
         {
      		 if(obj == null || !GeoData.getInstance().canSeeTarget(_actor,obj) || obj.isDead() || obj!= MostHate || obj == _actor || obj == getAttackTarget())
				continue;
    		 if(obj instanceof L2PcInstance)
    		 {
             	if(MostHate!=null || !MostHate.isDead())
                	actor.addDamageHate(obj,actor.getHating(MostHate),actor.getHating(MostHate));
                else
                	actor.addDamageHate(obj,2000,2000);
            	_actor.setTarget(obj);
            	setAttackTarget(obj);
            	
    		 }
    		 if(obj instanceof L2Attackable)
    		 {
    		     if(((L2Attackable)_actor).getEnemyClan()!=null)
    			 if((((L2Attackable)_actor).getEnemyClan().equals(((L2Attackable)obj).getClan())) &&((L2Attackable)_actor).getEnemyClan() != "none")
    			 {
    	            	if(MostHate!=null)
    	                	actor.addDamageHate(obj,actor.getHating(MostHate),actor.getHating(MostHate));
    	                else
    	                	actor.addDamageHate(obj,2000,2000);
    	            	_actor.setTarget(obj);
    			 }
    		     if (((L2Attackable)_actor).getIsChaos() != 0)
    		     {
    		    	 if(((L2Attackable)obj).getFactionId() == ((L2Attackable)_actor).getFactionId() && ((L2Attackable)obj).getFactionId()!=null)
    		    		 continue;
    		    	 else
    		    	 {
    		            	if(MostHate!=null)
    		                	actor.addDamageHate(obj,actor.getHating(MostHate),actor.getHating(MostHate));
    		                else
    		                	actor.addDamageHate(obj,2000,2000);
    		    		 _actor.setTarget(obj);
    		    		 setAttackTarget(obj);
    		    		 
    		    	 }
    		     }
    		 }
    		 if(obj instanceof L2Summon)
    		 {
    			 
             	if(MostHate!=null)
            		actor.addDamageHate(obj,actor.getHating(MostHate),actor.getHating(MostHate));
            	else
            		actor.addDamageHate(obj,2000,2000);
            	_actor.setTarget(obj);
            	setAttackTarget(obj);
    		 }
    		 
    		 
         }
    }
    
    private void LSkillRender()
    { 
    	if(_skillrender._Lrangeskills==null)
    	_skillrender._Lrangeskills = ((L2NpcInstance)_actor).getLrangeSkill();
    }
    private void SSkillRender()
    { 
    	if(_skillrender._Srangeskills==null)
    	_skillrender._Srangeskills = ((L2NpcInstance)_actor).getSrangeSkill();
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
