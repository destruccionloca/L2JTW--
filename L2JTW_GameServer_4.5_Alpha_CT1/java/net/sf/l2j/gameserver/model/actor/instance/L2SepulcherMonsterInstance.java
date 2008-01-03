/* This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.model.actor.instance;

import java.util.concurrent.Future;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.instancemanager.FourSepulchersManager;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.datatables.SkillTable;

/**
 *
 * @author  sandman
 */
public class L2SepulcherMonsterInstance extends L2MonsterInstance
{
    protected static Logger _log = Logger.getLogger(L2SepulcherMonsterInstance.class.getName());
	

	public int MysteriousBoxId = 0;
	

	protected Future _VictimSpawnKeyBoxTask = null;


	protected Future _ChangeImmortalTask = null;

	protected Future _ChangeMortalTask = null;
	

	protected Future _OnDeadEventTask = null;
	

	public L2SepulcherMonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	public void onSpawn()
    {
		super.onSpawn();
		switch(getNpcId())
		{

			case 18150: 
			case 18151: 
			case 18152: 
			case 18153: 
			case 18154:
			case 18155: 
			case 18156:
			case 18157:



				if (_VictimSpawnKeyBoxTask != null)
					_VictimSpawnKeyBoxTask.cancel(true);
				_VictimSpawnKeyBoxTask = ThreadPoolManager.getInstance().scheduleEffect(new VictimSpawnKeyBox(this), 300000);
				break;


			case 18196:	
			case 18197:	
			case 18198:
			case 18199:	
			case 18200:
			case 18201:	
			case 18202:	
			case 18203:	
			case 18204:	
			case 18205:	
			case 18206:	
			case 18207:	
			case 18208:	
			case 18209:	
			case 18210:	
			case 18211:	
				
				break;
				

			case 18231:	
			case 18232:	
			case 18233:
			case 18234:	
			case 18235:
			case 18236:	
			case 18237:	
			case 18238:	
			case 18239:	
			case 18240:	
			case 18241:	
			case 18242:
			case 18243:	
		
		
				setIsInvul(true);

				
				if (_ChangeImmortalTask != null)
					_ChangeImmortalTask.cancel(true);
				_ChangeImmortalTask = ThreadPoolManager.getInstance().scheduleEffect(new ChangeImmortal(this), 1600);

				break;
		}
    }

    /**
     * @see net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance#doDie(net.sf.l2j.gameserver.model.L2Character)
     */
    public boolean doDie(L2Character killer) 
    {
        if (!super.doDie(killer))
            return false;

		switch(getNpcId())
		{

			case 18120:	
			case 18121:	
			case 18122:	
			case 18123:	
			case 18124:	
			case 18125:	
			case 18126:	
			case 18127:	
			case 18128:	
			case 18129:	
			case 18130:	
			case 18131:
			case 18149:	
			case 18158:	
			case 18159:	
			case 18160:	
			case 18161:
			case 18162:	
			case 18163:	
			case 18164:	
			case 18165:	
			case 18183:
			case 18184:	
			case 18212:	
			case 18213:	
			case 18214:	
			case 18215:
			case 18216:
			case 18217:	
			case 18218:	
			case 18219:	
				if(_OnDeadEventTask != null) _OnDeadEventTask.cancel(true);
				_OnDeadEventTask = ThreadPoolManager.getInstance().scheduleEffect(new OnDeadEvent(this), 3500);
				break;

			case 18150:
			case 18151:
			case 18152:
			case 18153:
			case 18154:
			case 18155:
			case 18156:
			case 18157:
		        if (_VictimSpawnKeyBoxTask != null)
		        {
		        	_VictimSpawnKeyBoxTask.cancel(true);
		        	_VictimSpawnKeyBoxTask = null;
		        }
				if(_OnDeadEventTask != null) _OnDeadEventTask.cancel(true);
				_OnDeadEventTask = ThreadPoolManager.getInstance().scheduleEffect(new OnDeadEvent(this), 3500);
				break;


			case 18141:
			case 18142:	
			case 18143:	
			case 18144:
			case 18145:	
			case 18146:
			case 18147:	
			case 18148:
				if(FourSepulchersManager.getInstance().IsViscountMobsAnnihilated(MysteriousBoxId))
				{
					if(_OnDeadEventTask != null) _OnDeadEventTask.cancel(true);
					_OnDeadEventTask = ThreadPoolManager.getInstance().scheduleEffect(new OnDeadEvent(this), 3500);
				}
				break;

		
			case 18220:	
			case 18221:	
			case 18222:	
			case 18223:	
			case 18224:	
			case 18225:	
			case 18226:	
			case 18227:	
			case 18228:	
			case 18229:
			case 18230:
			case 18231:	
			case 18232:	
			case 18233:	
			case 18234:	
			case 18235:	
			case 18236:	
			case 18237:
			case 18238:	
			case 18239:	
			case 18240:	
				if(FourSepulchersManager.getInstance().IsDukeMobsAnnihilated(MysteriousBoxId))
				{
					if(_OnDeadEventTask != null) _OnDeadEventTask.cancel(true);
					_OnDeadEventTask = ThreadPoolManager.getInstance().scheduleEffect(new OnDeadEvent(this), 3500);
				}
				break;



			case 25339:
			case 25342:
			case 25346:
			case 25349:
				GiveCup((L2PcInstance)killer);
				if(_OnDeadEventTask != null) _OnDeadEventTask.cancel(true);
				_OnDeadEventTask = ThreadPoolManager.getInstance().scheduleEffect(new OnDeadEvent(this), 8500);
				break;
		}
		return true;
    }

    public void deleteMe()
    {
        if (_VictimSpawnKeyBoxTask != null)
        {
        	_VictimSpawnKeyBoxTask.cancel(true);
        	_VictimSpawnKeyBoxTask = null;
        }
		if(_OnDeadEventTask != null)
		{
			_OnDeadEventTask.cancel(true);
			_OnDeadEventTask = null;
		}

		super.deleteMe();
    }

	public boolean isRaid()
	{
		switch(getNpcId())
		{

			case 25339:
			case 25342:
			case 25346:
			case 25349:
				return true;
			default:
				return false;
		}
	}
	

	protected void GiveCup(L2PcInstance player)
	{

	    String QuestId = "620_FourGoblets";
	    int CupId = 0;
	    int OldBrooch = 7262;	

	    

		switch(getNpcId())
		{
			case 25339:
				CupId = 7256;
				break;
			case 25342:
				CupId = 7257;	
				break;
			case 25346:
				CupId = 7258;	
				break;
			case 25349:
				CupId = 7259;
				break;
		}
		

		if(player.getParty() != null)
		{
            for (L2PcInstance mem : player.getParty().getPartyMembers())
            {

                if(mem.getQuestState(QuestId).get("<state>") == null) return;
                else
                {

                    if (mem.getInventory().getItemByItemId(OldBrooch) == null)
                    {
    					mem.addItem("Quest", CupId, 1, mem, true);
                    }
                }
            }
		}
	   
		else
		{
            if(player.getQuestState(QuestId).get("<state>") == null) return;
            else
            {
			
                if (player.getInventory().getItemByItemId(OldBrooch) == null)
                {
                	player.addItem("Quest", CupId, 1, player, true);
                }
            }
		}
	}


	protected class VictimSpawnKeyBox implements Runnable
	{
		L2SepulcherMonsterInstance _activeChar;
		
		public VictimSpawnKeyBox(L2SepulcherMonsterInstance activeChar)
		{
			_activeChar = activeChar;
		}
		
        public void run()
        {
        	
        	if(_activeChar.isDead()) return;
        	
        	
        	if(!_activeChar.isVisible()) return;

        	
			FourSepulchersManager.getInstance().SpawnKeyBox(_activeChar);
        }
	}

	
	protected class OnDeadEvent implements Runnable
	{
		L2SepulcherMonsterInstance _activeChar;
		
		public OnDeadEvent(L2SepulcherMonsterInstance activeChar)
		{
			_activeChar = activeChar;
		}
		
        public void run()
        {
    		switch(_activeChar.getNpcId())
    		{
    			
    			case 18120:	
    			case 18121:	
    			case 18122:	
    			case 18123:
    			case 18124:	
    			case 18125:
    			case 18126:	
    			case 18127:
    			case 18128:	
    			case 18129:	
    			case 18130:	
    			case 18131:
    			case 18149:
    			case 18158:
    			case 18159:	
    			case 18160:
    			case 18161:
    			case 18162:
    			case 18163:	
    			case 18164:
    			case 18165:	
    			case 18183:
    			case 18184:	
    			case 18212:
    			case 18213:
    			case 18214:
    			case 18215:
    			case 18216:
    			case 18217:
    			case 18218:
    			case 18219:	
    				FourSepulchersManager.getInstance().SpawnKeyBox(_activeChar);
    				break;

    		
    			case 18150:	
    			case 18151:
    			case 18152:	
    			case 18153:
    			case 18154:	
    			case 18155:	
    			case 18156:
    			case 18157:
    				FourSepulchersManager.getInstance().SpawnExecutionerOfHalisha(_activeChar);
    				break;

    			
    			case 18141:	
    			case 18142:	
    			case 18143:	
    			case 18144:	
    			case 18145:
    			case 18146:
    			case 18147:
    			case 18148:
    				FourSepulchersManager.getInstance().SpawnMonster(_activeChar.MysteriousBoxId);
    				break;

    		
    			case 18220:
    			case 18221:
    			case 18222:
    			case 18223:	
    			case 18224:	
    			case 18225:	
    			case 18226:	
    			case 18227:	//
    			case 18228:	//
    			case 18229:	//
    			case 18230:	//çcíÈÇÃàÛèÕ
    			case 18231:	//
    			case 18232:	//
    			case 18233:	//
    			case 18234:	//
    			case 18235:	//
    			case 18236:	//
    			case 18237:	//
    			case 18238:	//
    			case 18239:	
    			case 18240:	//
   					FourSepulchersManager.getInstance().SpawnArchonOfHalisha(_activeChar.MysteriousBoxId);
    				break;

    		
    			case 25339:
    			case 25342:
    			case 25346:
    			case 25349:
   					FourSepulchersManager.getInstance().SpawnEmperorsGraveNpc(_activeChar.MysteriousBoxId);
    				break;
    		}
        }
	}
	

	protected class ChangeImmortal implements Runnable
	{
		L2SepulcherMonsterInstance activeChar;
		public ChangeImmortal(L2SepulcherMonsterInstance mob)
		{
			activeChar = mob;
		}
		
		public void run()
		{
			L2Skill fp = SkillTable.getInstance().getInfo(4616, 1);
			fp.getEffects(activeChar, activeChar);


			if (_ChangeMortalTask != null)
				_ChangeMortalTask.cancel(true);
			_ChangeMortalTask = ThreadPoolManager.getInstance().scheduleEffect(new ChangeMortal(), fp.getBuffDuration());
		}
	}


	protected class ChangeMortal implements Runnable
	{
		public ChangeMortal()
		{
		}
		
		public void run()
		{
			setIsInvul(false);
		}
	}
}
