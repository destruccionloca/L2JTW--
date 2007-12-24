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
package net.sf.l2j.gameserver.model;

import java.util.Arrays;

import javolution.util.FastList;

/**
 *
 * 
 * 
 * Author: ShanSoft
 * By L2JTW
 * 
 * 
 */


// This Data is for NPC Attributes and AI relate stuffs...
// Still need to finish...Update later...

public class L2NpcAIData
{
	
	//Basic AI
    private int _primary_attack;
    private int _skill_chance;
    private int _canMove;
    private int _soulshot;
    private int _spiritshot;
    private int _soulshotchance;
    private int _spiritshotchance;
    private int _ischaos;
    private String _clan;
    private String _enemyClan;
    private int _enemyRange;
    private int _baseShldRate;
    private int _baseShldDef;
    private int _dodge;
    //Skill AI
    private int _buffskill;
    private int _debuffskill;
    private int _atkskill;
    private int _rootskill;
    private int _sleepskill;
    private int _effectskill;
    private int _specialskill; // It will be use later on..
    
    private int _castchance;
    private int _skillcondition; // use later on
    

	private FastList<L2NpcAIData> npcAIdata;
	
	public void NpcData()
	{	
		npcAIdata = new FastList<L2NpcAIData>(0);
		
	}

	public FastList<L2NpcAIData> getAIData()
	{
		return npcAIdata;
	}	
    public void addNpcData(L2NpcAIData npcdatatable)
	{
    	
    	npcAIdata.add(npcdatatable);
    	
	}
    
    
    
    
    
    
    
    
    //--------------------------------------------------------------------------------------------------------------
    //Setting....
    //--------------------------------------------------------------------------------------------------------------
    public void setPrimaryAttack (int primaryattack)
    {
    	
    	_primary_attack = primaryattack;
    	
    }
	
    public void setSkillChance (int skill_chance)
    {
    	
    	_skill_chance = skill_chance ;
    	
    }
    
    public void setCanMove (int canMove)
    {
    	
    	_canMove = canMove ;
    	
    }
    
    public void setSoulShot (int soulshot)
    {
    	
    	_soulshot = soulshot;
    	
    }
    
    public void setSpiritShot (int spiritshot)
    {
    	
    	_spiritshot = spiritshot;
    	
    }
    
    public void setSoulShotChance (int soulshotchance)
    {
    	
    	_soulshotchance = soulshotchance;
    	
    }
    
    public void setSpiritShotChance (int spiritshotchance)
    {
    	
    	_spiritshotchance = spiritshotchance;
    	
    }
    
    public void setIsChaos (int ischaos)
    {
    	_ischaos = ischaos;
    	
    }
    public void setClan (String clan)
    {
    	
    	_clan = clan;
    	
    }
    
    public void setEnemyClan (String enemyClan)
    {
    	
    	_enemyClan = enemyClan;
    	
    }
    
    public void setEnemyRange (int enemyRange)
    {
    	
    	_enemyRange = enemyRange;
    	
    }
    public void setDodge (int dodge)
    {
    	_dodge = dodge;
    }
    
    /*
    
    public void setBaseShldRate (int baseShldRate)
    {
    	
    	_baseShldRate = baseShldRate;
    	
    }
    
    public void setBaseShldDef (int baseShldDef)
    {
    	
    	_baseShldDef = baseShldDef;
    	
    }
    */
    
    public void setBuffSkill (int buff)
    {
    	_buffskill = buff;
    }
    public void setDebuffSkill (int debuff)
    {
    	_debuffskill = debuff;
    }
    public void setEffectSkill (int effect)
    {
    	_effectskill = effect;
    }
    public void setRootSkill (int root)
    {
    	_rootskill = root;
    }
    public void setSleepSkill (int sleep)
    {
    	_sleepskill = sleep;
    }
    public void setAtkSkill (int atk)
    {
    	_atkskill = atk;
    }
    public void setSpecialSkill(int special)
    {
    	_specialskill = special;
    }
    public void setCastChance(int chance)
    {
    	_castchance = chance;
    }
    
    //--------------------------------------------------------------------------------------------------------------
    //Data Recall....
    //--------------------------------------------------------------------------------------------------------------
    public int getPrimaryAttack ()
    {
    	
    	return _primary_attack;
    	
    }
	
    public int getSkillChance ()
    {
    	
    	return _skill_chance;
    	
    }
    
    public int getCanMove ()
    {
    	
    	return _canMove;
    	
    }
    
    public int getSoulShot ()
    {
    	
    	return _soulshot;
    	
    }
    
    public int getSpiritShot ()
    {
    	
    	return _spiritshot;
    	
    }
    
    public int getSoulShotChance()
    {
    	
    	return _soulshotchance;
    	
    }
    
    public int getSpiritShotChance()
    {
    	
    	return _spiritshotchance;
    	
    }
    public int getIsChaos ()
    {
    	return _ischaos;
    	
    }
    public String getClan()
    {
    	
    	return _clan;
    	
    }
    public String getEnemyClan ()
    {
    	
    	return _enemyClan;
    	
    }
    
    public int getEnemyRange ()
    {
    	
    	return _enemyRange;
    	
    }
    
    public int getDodge()
    {
    	
    	return _dodge;
    	
    }
    
    /*
    
    public int getBaseShldRate ()
    {
    	
    	return _baseShldRate;
    	
    }
    
    public int getBaseShldDef ()
    {
    	
    	return _baseShldDef;
    	
    }
    */
    
    
    public int getBuffSkill ()
    {
    	return _buffskill;
    }
    public int getDebuffSkill ()
    {
    	return _debuffskill;
    }
    public int getEffectSkill ()
    {
    	return _effectskill;
    }
    public int getRootSkill ()
    {
    	return _rootskill;
    }
    public int getSleepSkill ()
    {
    	return _sleepskill;
    }
    public int getAtkSkill ()
    {
    	return _atkskill;
    }
    public int getSpecialSkill()
    {
    	return _specialskill;
    }
    public int getCastChance()
    {
    	return _castchance;
    }
	

}