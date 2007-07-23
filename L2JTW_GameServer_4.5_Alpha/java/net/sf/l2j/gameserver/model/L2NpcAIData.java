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
	
	
    private int _primary_attack;
    private int _skill_chance;
    private int _canMove;
    private int _soulshot;
    private int _spiritshot;
    private int _ischaos;
    private String _enemyClan;
    private int _enemyRange;
    private int _baseShldRate;
    private int _baseShldDef;


	private FastList<L2NpcAIData> npcAIdata;
	
	public void NpcData()
	{	
		npcAIdata = new FastList<L2NpcAIData>(0);
		
	}

	public FastList<L2NpcAIData> getChatData()
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
    
    public void setIsChaos (int ischaos)
    {
    	_ischaos = ischaos;
    	
    }
    
    public void setEnemyClan (String enemyClan)
    {
    	
    	_enemyClan = enemyClan;
    	
    }
    
    public void setEnemyRange (int enemyRange)
    {
    	
    	_enemyRange = enemyRange;
    	
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
    
    public int getIsChaos ()
    {
    	return _ischaos;
    	
    }
    
    public String getEnemyClan ()
    {
    	
    	return _enemyClan;
    	
    }
    
    public int getEnemyRange ()
    {
    	
    	return _enemyRange;
    	
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
    
    
	
	

}