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

public class L2NpcCharData
{
	
	private int _ischar;
	private int _charclass;
	private int _charrace;
	private int _charface;
	private int _charhair;
	private int _charcolor;
	private int _charsex;
	private int _charhero;
	private int _lrhand;
	private int _armor;
	private int _pant;
	private int _head;
	private int _boot;
	private int _glove;
	private int _hair;
	private int _dhair;
	private int _face;
	private int _enchlvl;
	
	private FastList<L2NpcCharData> npcCharData;
	
	
	
	public void NpcCharData()
	{	
		npcCharData = new FastList<L2NpcCharData>(0);
	}

	public FastList<L2NpcCharData> getChatData()
	{
		return npcCharData;
	}	
    public void addNpcCharData(L2NpcCharData npcdatatable)
	{
    	
    	npcCharData.add(npcdatatable);
    	
	}

    
    //--------------------------------------------------------------------------------------------------------------
    //Setting....
    //--------------------------------------------------------------------------------------------------------------
	
    public void setIsChar(int ischar)
	{
		_ischar = ischar;
	}
    public void setCharClass(int charclass)
	{
		_charclass = charclass;
	}
    public void setCharRace(int charrace)
	{
		_charrace = charrace;
	}
    public void setCharFace(int charface)
	{
		_charface = charface;
	}
    public void setCharHair(int charhair)
	{
		_charhair = charhair;
	}
    public void setCharColor(int charcolor)
	{
		_charcolor = charcolor;
	}
    public void setCharSex(int charsex)
	{
		_charsex = charsex;
	}
    public void setCharHero(int charhero)
	{
		_charhero = charhero;
	}
    //Gears
    public void setArmor(int armor)
	{
		_armor = armor;
	}
    public void setPant(int pant)
	{
		_pant = pant;
	}
    public void setHead(int head)
	{
		_head = head;
	}
    public void setLrhand(int lrhand)
	{
		_lrhand = lrhand ;
	}
    public void setGlove(int glove)
	{
		_glove = glove;
	}
    public void setBoot(int boot)
	{
		_boot = boot;
	}
    public void setHair(int hair)
	{
		_hair = hair;
	}
    public void setDHair(int dhair)
	{
		_dhair = dhair ;
	}
    public void setFace(int face)
	{
		_face = face ;
	}
    public void setEnchLvl(int enchlvl)
	{
		_enchlvl = enchlvl ;
	}
    
    
    //--------------------------------------------------------------------------------------------------------------
    //Data Recall....
    //--------------------------------------------------------------------------------------------------------------
	
    public int getIsChar()
	{
		return _ischar;
	}
    public int getCharClass()
	{
		return _charclass;
	}
    public int getCharRace()
	{
		return _charrace;
	}
    public int getCharFace()
	{
		return _charface;
	}
    public int getCharHair()
	{
		return _charhair;
	}
    public int getCharColor()
	{
		return _charcolor;
	}
    public int getCharSex()
	{
		return _charsex;
	}
    public int getCharHero()
	{
		return _charhero;
	}
    //Gears
    public int getArmor()
	{
		return _armor;
	}
    public int getPant()
	{
		return _pant;
	}
    public int getHead()
	{
		return _head;
	}
    public int getLrhand()
	{
		return _lrhand;
	}
    public int getGlove()
	{
		return _glove;
	}
    public int getBoot()
	{
		return _boot;
	}
    public int getHair()
	{
		return _hair;
	}
    public int getDHair()
	{
		return _dhair;
	}
    public int getFace()
	{
		return _face;
	}
    
    public int getEnchLvl()
	{
		return _enchlvl;
	}
    
    
    
    
    
    
    
    
}