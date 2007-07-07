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
package net.sf.l2j.gameserver;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.clientpackets.Say2;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.script.DateRange;
import net.sf.l2j.gameserver.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.5.2.1.2.7 $ $Date: 2005/03/29 23:15:14 $
 */
public class Announcements
{
	private static Logger _log = Logger.getLogger(Announcements.class.getName());
	
	private static Announcements _instance;
	private List<String> _announcements = new FastList<String>();

	private List<String> _announcecycle = new FastList<String>();

	private List<List<Object>> _eventAnnouncements = new FastList<List<Object>>();

	private int _announcecyclesize = 0;


	public Announcements()
	{
		loadAnnouncements();
		loadAnnouncCycle();
	}
	
	public static Announcements getInstance()
	{
		if (_instance == null)
		{
			_instance = new Announcements();
		}
		
		return _instance;
	}
	
	/** The task launching the function doAnnouncCycle() */
	class AnnouncTask implements Runnable
	{	
		public void run()
		{		
			try
			{	
				System.out.print("第"+(_announcecyclesize+1));
				showAnnounceCycle(_announcecycle.get(_announcecyclesize));//輪迴式公告發布
				_announcecyclesize++;
				if(_announcecyclesize>=_announcecycle.size())//公告折返回第1則
					_announcecyclesize = 0;
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "", e);
			}
		}
	}
	
    public void doAnnouncCycle()
    {
    	AnnouncTask rs = new AnnouncTask();//建立執行緒
    	ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(rs, 60000, 1200000);//使用低階排程運作
    }
	
	public void loadAnnouncements()
	{
		_announcements.clear();
		File file = new File(Config.DATAPACK_ROOT, "data/announcements.txt");
		if (file.exists())
		{
			readFromDisk(file);
		}
		else
		{
			_log.config("data/announcements.txt 檔案不存在");
		}
	}
	
	public void loadAnnouncCycle()//讀取循環公告
	{
		_announcecycle.clear();
		File file = new File(Config.DATAPACK_ROOT, "data/announcecycle.txt");
		if (file.exists())
		{
			readFromDiskmulti(file);
			doAnnouncCycle();//若有載入檔案即開始運作循環公告執行緒
		}
		else
		{
			_log.config("data/announcecycle.txt 檔案不存在");
		}
	}
	
    /** 顯示所有公告內容 */
	public void showAnnouncements(L2PcInstance activeChar)
	{
		for (int i = 0; i < _announcements.size(); i++)//修改為限定公告行數設置 by game
		{
			CreatureSay cs = new CreatureSay(0, Say2.ANNOUNCEMENT, activeChar.getName(), _announcements.get(i));
			activeChar.sendPacket(cs);
		}
		
		for (int i = 0; i < _eventAnnouncements.size(); i++)
		{
		    List<Object> entry   = _eventAnnouncements.get(i);
            
            DateRange validDateRange  = (DateRange)entry.get(0);
            String[] msg              = (String[])entry.get(1);
		    Date currentDate          = new Date();
		    
		    if (!validDateRange.isValid() || validDateRange.isWithinRange(currentDate))
		    {
                SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
                for (int j=0; j<msg.length; j++)
                {
                    sm.addString(msg[j]);
                }
                activeChar.sendPacket(sm);
		    }
		    
		}
	}
	
	/** 
	* 顯示單項公告內容 
	* by game
	*/
	public void showOneAnnouncement(L2PcInstance activeChar,int line)
	{
		CreatureSay cs = new CreatureSay(0, Say2.ANNOUNCEMENT, activeChar.getName(), _announcements.get(line).toString());
		activeChar.sendPacket(cs);
	}
	
	/** 
	* 顯示循環公告內容 
	* by game
	*/
	public void showAnnounceCycle(String str)
	{
		announceToAll(str);
		System.out.println("公告事項: "+str);
	}
	
	public void addEventAnnouncement(DateRange validDateRange, String[] msg)
	{
	    List<Object> entry = new FastList<Object>();
	    entry.add(validDateRange);
	    entry.add(msg);
	    _eventAnnouncements.add(entry);
	}
	
    /** 開啟公告發布功能頁*/
	public void listAnnouncements(L2PcInstance activeChar)
	{		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
        TextBuilder replyMSG = new TextBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"主頁\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=180><center>發布公告頁</center></td>");
		replyMSG.append("<td width=40><button value=\"返回\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>加入或發布一則公告:</center>");
		replyMSG.append("<center><multiedit var=\"new_announcement\" width=240 height=30></center><br>");
		replyMSG.append("<center><table><tr><td>");
		replyMSG.append("<button value=\"增加公告\" action=\"bypass -h admin_add_announcement $new_announcement\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
		replyMSG.append("<button value=\"即時公告\" action=\"bypass -h admin_announce_menu $new_announcement\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
		replyMSG.append("<button value=\"重讀公告\" action=\"bypass -h admin_reload_announcements\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
		replyMSG.append("<button value=\"發佈公告\" action=\"bypass -h admin_announce_announcements\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</td></tr></table></center>");
		replyMSG.append("<br>");
		for (int i = 0; i < _announcements.size(); i++)
		{

			replyMSG.append("<table width=260><tr><td>");
			replyMSG.append(_announcements.get(i).toString());
			replyMSG.append("</td></tr><tr><td>");
			replyMSG.append("<button value=\"單項公告\" action=\"bypass -h admin_one_announcement " + i + "\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			replyMSG.append("&nbsp;&nbsp;&nbsp;<td><button value=\"刪除\" action=\"bypass -h admin_del_announcement " + i + "\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
			replyMSG.append("</td></tr></table>");

		}
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	public void addAnnouncement(String text)
	{
		_announcements.add(text);
		saveToDisk();
	}
	
	public void delAnnouncement(int line)
	{
		_announcements.remove(line);
		saveToDisk();
	}
	
	private void readFromDisk(File file)
	{
		LineNumberReader lnr = null;
		try
		{
			int i=0;
			String line = null;
			lnr = new LineNumberReader(new FileReader(file));
			while ( (line = lnr.readLine()) != null)
			{
				StringTokenizer st = new StringTokenizer(line,"\n\r");
				if (st.hasMoreTokens())
				{	
					String announcement = st.nextToken();
					_announcements.add(announcement);
					
					i++;
				}
			}
			
			_log.config("Announcements: Loaded " + i + " Announcements.");
		}
		catch (IOException e1)
		{
			_log.log(Level.SEVERE, "Error reading announcements", e1);
		}
		finally
		{
			try
			{
				lnr.close();
			}
			catch (Exception e2)
			{
				// nothing
			}
		}
	}
	
	private void readFromDiskmulti(File file)
	{ //循環公告多型法讀取應用
		LineNumberReader lnr = null;
		try
		{
			int i=0;
			String line = null;
			lnr = new LineNumberReader(new FileReader(file));
			while ( (line = lnr.readLine()) != null)
			{
				StringTokenizer st = new StringTokenizer(line,"\n\r");
				if (st.hasMoreTokens())
				{	
					String announcement = st.nextToken();
					_announcecycle.add(announcement);
					
					i++;
				}
			}
			
			_log.config("Announcecycle: Loaded " + i + " Announcecycle.");
		}
		catch (IOException e1)
		{
			_log.log(Level.SEVERE, "Error reading announcecycle", e1);
		}
		finally
		{
			try
			{
				lnr.close();
			}
			catch (Exception e2)
			{
				// nothing
			}
		}
	}
	
	private void saveToDisk()
	{
		File file = new File("data/announcements.txt");
		FileWriter save = null; 

		try
		{
			save = new FileWriter(file);
			for (int i = 0; i < _announcements.size(); i++)
			{
				save.write(_announcements.get(i));
				save.write("\r\n");
			}
			save.flush();
			save.close();
			save = null;
		}
		catch (IOException e)
		{
			_log.warning("saving the announcements file has failed: " + e);
		}
	}
	
	public void announceToAll(String text) {
		CreatureSay cs = new CreatureSay(0, Say2.ANNOUNCEMENT, "", text);
		
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			player.sendPacket(cs);
		}
	}
	public void announceToAll(SystemMessage sm) {
		
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			player.sendPacket(sm);
		}
	}

	// 管理員處理公告內容方法
	public void handleAnnounce(String command, int lengthToTrim)
	{
		try
		{
			// 公告字串給伺服器內每個人
			String text = command.substring(lengthToTrim);
			Announcements.getInstance().announceToAll(text);
		}
		
		// No body cares!
		catch (StringIndexOutOfBoundsException e)
		{
			//無訊息.. 忽略
		}
	}
}
