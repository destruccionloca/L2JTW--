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
package net.sf.l2j.gameserver.communitybbs.Manager;

import java.util.StringTokenizer;

import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.ShowBoard;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class TopBBSManager extends BaseBBSManager
{

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.communitybbs.Manager.BaseBBSManager#parsecmd(java.lang.String, net.sf.l2j.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
        int cabal = SevenSigns.CABAL_NULL;
		// TEMP ======================================================================================
		if(command.equals("_bbsgetfav"))
		{
			String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/favorites.htm");
			if (content == null)
			{
				content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/favorites.htm' </center></body></html>";
			}
			separateAndSend(content,activeChar);
		}
		if(command.equals("_bbsmail"))
		{
			String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/mail.htm");
			if (content == null)
			{
				content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/mail.htm' </center></body></html>";
			}
			separateAndSend(content,activeChar);
		}
		if(command.equals("_bbsfriends"))
		{
			String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/friends.htm");
			if (content == null)
			{
				content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/friends.htm' </center></body></html>";
			}
			separateAndSend(content,activeChar);
		}
		if(command.equals("bbs_add_fav"))
		{
			String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/favorites.htm");
			if (content == null)
			{
				content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/favorites.htm' </center></body></html>";
			}
			separateAndSend(content,activeChar);
		}
		// TEMP ======================================================================================
		if(command.equals("_bbstop"))
		{
			String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/bbshome.htm");
			if (content == null)
			{
				content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/bbshome.htm' </center></body></html>";
			}
			separateAndSend(content,activeChar);
		}
		else if(command.equals("_bbshome"))
		{
			String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/bbshome.htm");
			if (content == null)
			{
				content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/bbshome.htm' </center></body></html>";
			}
			separateAndSend(content,activeChar);
		}
		else if (command.equals("_ssforum"))
		{
		    String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/ssforum.htm");
		    if (content == null)
		    {
			content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/ssforum.htm' </center></body></html>";
		    }
		    separateAndSend(content, activeChar);
		} 
		else if (command.equals("_bbsdawn"))
		{
	        if (cabal == SevenSigns.CABAL_DAWN)
	        {
	    	    String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/bbsdawn.htm");
	    	    if (content == null)
	    	    {
	    		content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/bbsdawn.htm' </center></body></html>";
	    	    }
	    	    separateAndSend(content, activeChar);
	        }
	        else
	        {
	        	activeChar.sendPacket(new SystemMessage(SystemMessageId.CAN_BE_USED_BY_DAWN));
	        }
		} 
		else if (command.equals("_bbsdusk"))
		{
	        if (cabal == SevenSigns.CABAL_DUSK)
	        {
	    	    String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/bbsdusk.htm");
	    	    if (content == null)
	    	    {
	    		content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/bbsdusk.htm' </center></body></html>";
	    	    }
	    	    separateAndSend(content, activeChar);
	        }
	        else
	        {
	        	activeChar.sendPacket(new SystemMessage(SystemMessageId.CAN_BE_USED_BY_DUSK));
	        }
		} 
		else if(command.startsWith("_bbstop;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			int idp = Integer.parseInt(st.nextToken());
			String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/"+idp+".htm");
			if (content == null)
			{
				content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/"+idp+".htm' </center></body></html>";
			}
			separateAndSend(content,activeChar);
		}
		else
		{
		//ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: "+command+" is not implemented yet</center><br><br></body></html>","101");
		ShowBoard sb = new ShowBoard("<html><body></body></html>","101");
		activeChar.sendPacket(sb);
		activeChar.sendPacket(new ShowBoard(null,"102"));
		activeChar.sendPacket(new ShowBoard(null,"103"));
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.communitybbs.Manager.BaseBBSManager#parsewrite(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, net.sf.l2j.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
		// TODO Auto-generated method stub

	}

	private static TopBBSManager _instance = new TopBBSManager();

	/**
	 * @return
	 */
	public static TopBBSManager getInstance()
	{
		return _instance;
	}

}