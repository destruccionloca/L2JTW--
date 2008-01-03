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
/*
 * 
 * 
 * Author: ShanSoft
 * By L2JTW
 * 
 * 
 */


// Chat Condition
public class L2NpcChatData
{

	private int _chattype;
	private int _chance;
	private int _delay;
	private int _chatcondition1;
	private int _chatcondition2;
	private int _chatvalue1;
	private int _chatvalue2;
	private String _chatmemo = null;
	private FastList<L2NpcChatData> chatdata;
	
	public void ChatData()
	{	
		chatdata = new FastList<L2NpcChatData>(0);
	}

	public FastList<L2NpcChatData> getChatData()
	{
		return chatdata;
	}	
    public void addChatData(L2NpcChatData chattable)
	{
    	
    	chatdata.add(chattable);
    	
	}

	
	
	
	public int getChatType()
	{
		return _chattype;
	}
		
	public int getChatChance()
	{
		return _chance;
	}
	public int getChatDelay()
	{
		return _delay;
	}
	public int getChatValue1()
	{
		return _chatvalue1;
	}
	public int getChatValue2()
	{
		return _chatvalue2;
	}
	public int getChatCondition1()
	{
		return _chatcondition1;
	}
	public int getChatCondition2()
	{
		return _chatcondition2;
	}
	public String getChatMemo()
	{
		return _chatmemo;
	}
	

	

	public void setChatType(int chat_type)
	{
		_chattype = chat_type;
	}
	
	public void setChatChance(int chance)
	{
		_chance = chance;
	}
	public void setChatDelay(int delay)
	{
		_delay = delay;
	}
	
	public void setChatCondition1(int chatcondition1)
	{
		_chatcondition1 = chatcondition1;
	}
	
	public void setChatValue1(int chatvalue1)
	{
		_chatvalue1 = chatvalue1;
	}
	
	public void setChatCondition2(int chatcondition2)
	{
		_chatcondition2 = chatcondition2;
	}
	
	public void setChatValue2(int chatvalue2)
	{
		_chatvalue2 = chatvalue2;
	}

	public void setChatMemo(String chatmemo)
	{
		_chatmemo = chatmemo;
	}
	

    public String toString()
    {
        String out = "NpcChat: " + getChatType() + " ChatValue: " + getChatValue1() + 
        	" Memo: " + getChatMemo();
      
        
        return out;
    }
    

}
