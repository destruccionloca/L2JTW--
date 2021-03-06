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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.clientpackets.Say2;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.serverpackets.ShowBoard;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.L2GameClient;

public class CommunityBoard
{
	private static Logger _log = Logger.getLogger(CommunityBoard.class.getName());
	private static CommunityBoard _instance;
	
	public static CommunityBoard getInstance()
	{
		if (_instance == null)
		{
			_instance = new CommunityBoard();
		}
		
		return _instance;
	}
	
	public void handleCommands(L2GameClient client, String command)
	{
		L2PcInstance activeChar = client.getActiveChar();
        if(activeChar == null)
            return;
		System.out.println("[CommunityBoard] CommunityBoard page:"+command);
		if (command.startsWith("bbs_")||command.startsWith("_bbs")||command.equals("_friendlist_0_")||command.equals("_maillist_0_1_0_"))
		{
			StringBuffer htmlCode = new StringBuffer("<html><body><br>");

			if (command.startsWith("bbs_default") && (Config.SHOW_STATUS_COMMUNITYBOARD || activeChar.isGM()))
			{
				int startIndex;
				String arg = command.substring("bbs_default".length()).trim();
				if (arg.equals(""))
					startIndex = 0;
				else
				{
					try {
						startIndex = Integer.parseInt(arg);
						if (startIndex < 0)
							throw new Exception();
						if (startIndex > L2World.getInstance().getAllPlayers().size()-1)
							startIndex = 0;
					} catch (Exception e) {
						_log.warning("Bad argument was passed to bbs_default. "+arg);
						return;
					}
				}
				System.out.println("[CommunityBoard] CommunityBoard page:"+command+" "+startIndex);
				onBBSDefault(htmlCode, startIndex, activeChar);
			}

			/**
			 * &$412;類別 &$413;標題 &$414;數目 &$415;管理者 &$416;編號 &$417;作者 &$418;貼上日期 &$419;閱覽數
			 */
			else if (command.equals("_bbshome"))
			{
				htmlCode.append("<center><br><br1><br1>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=50></td>");
				htmlCode.append("<td align=center width=560 height=30 align=left><img src=\"l2ui.bbs_lineage2\" width=128 height=16 ></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td WIDTH=60 align=center>&$412;</td>");
				htmlCode.append("<td WIDTH=410 align=center>&$413;</td>");
				htmlCode.append("<td WIDTH=80 align=center>&$414;</td>");
				htmlCode.append("<td WIDTH=110 align=center>&$415;</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<br>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=60 align=right valign=top><img src=\"l2ui.bbs_folder\" width=32 height=32></td>");//書籤 bbs_init_1_1
				htmlCode.append("<td FIXWIDTH=410 align=left valign=top><a action=\"bypass bbs_init_1_1\">書籤</a><br1><font color=\"AAAAAA\">書籤 文件夾</font></td>");
				htmlCode.append("<td FIXWIDTH=80 align=center valign=top>0</td>");
				htmlCode.append("<td FIXWIDTH=110 align=center valign=top>管理者</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=610><img src=\"l2ui.squaregray\" width=\"610\" height=\"1\"></td>");//分隔線
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<br>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=60 align=right valign=top><img src=\"l2ui.bbs_folder\" width=32 height=32></td>");//七個封印交流區
				htmlCode.append("<td FIXWIDTH=410 align=left valign=top><a action=\"bypass bbs_init_1_2\">七個封印交流區</a><br1><font color=\"AAAAAA\">七個封印 雙方陣營分類討論區</font></td>");
				htmlCode.append("<td FIXWIDTH=80 align=center valign=top>0</td>");
				htmlCode.append("<td FIXWIDTH=110 align=center valign=top>管理者</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=610><img src=\"l2ui.squaregray\" width=\"610\" height=\"1\"></td>");//分隔線
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<br>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=60 align=right valign=top><img src=\"l2ui.bbs_folder\" width=32 height=32></td>");//天堂 II 活動快報
				htmlCode.append("<td FIXWIDTH=410 align=left valign=top><a action=\"bypass bbs_init_1_3\">天堂 II 活動快報</a><br1><font color=\"AAAAAA\">天堂 II 活動快報</font></td>");
				htmlCode.append("<td FIXWIDTH=80 align=center valign=top>0</td>");
				htmlCode.append("<td FIXWIDTH=110 align=center valign=top>管理者</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=610><img src=\"l2ui.squaregray\" width=\"610\" height=\"1\"></td>");//分隔線
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<br>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=60 align=right valign=top><img src=\"l2ui.bbs_folder\" width=32 height=32></td>");
				htmlCode.append("<td FIXWIDTH=410 align=left valign=top><a action=\"bypass bbs_init_1_4\">地區 社群</a><br1><font color=\"AAAAAA\">天堂 II 地區社群</font></td>");
				htmlCode.append("<td FIXWIDTH=80 align=center valign=top>0</td>");
				htmlCode.append("<td FIXWIDTH=110 align=center valign=top>管理者</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=610><img src=\"l2ui.squaregray\" width=\"610\" height=\"1\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<br><br></center>");
			}
			
			else if (command.equals("bbs_init_1_1") || command.equals("_bbsgetfav"))//書籤頁
			{
				htmlCode.append("<center><br><br1><br1>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass bbs_init_1_1\">書籤 </a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=50>&nbsp;</td>");
				htmlCode.append("<td FIXWIDTH=350 align=center>&$413;</td>");//標題
				htmlCode.append("<td FIXWIDTH=150 align=center>&$418;</td>");//貼上日期
				htmlCode.append("<td FIXWIDTH=60 align=center><img src=\"l2ui.mini_logo\" width=5 height=1></td>");//??
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0><tr>");
				htmlCode.append("<td width=610><img src=\"sek.cbui355\" width=\"610\" height=\"1\"><br><img src=\"sek.cbui355\" width=\"610\" height=\"1\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</center>");
			}
			
			else if (command.equals("bbs_init_1_2"))//七個封印交流區
			{
				htmlCode.append("<center><br><br1><br1>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass _bbshome\">HOME</a>&nbsp;&gt;&nbsp;<a action=\"bypass bbs_init_1_2\">七個封印交流區</a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td WIDTH=60 align=center>&$412;</td>");//類別
				htmlCode.append("<td WIDTH=410 align=center>&$413;</td>");//標題
				htmlCode.append("<td WIDTH=80 align=center>&$414;</td>");//數目
				htmlCode.append("<td WIDTH=110 align=center>&$415;</td>");//管理者
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<br>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2><tr>");
				htmlCode.append("<td FIXWIDTH=60 align=right valign=top><img src=\"l2ui.bbs_board\" width=32 height=32></td>");
				htmlCode.append("<td FIXWIDTH=410 align=left valign=top><a action=\"bypass bbs_init_1_2_1\">七個封印交流區 黎明的君主們 佈告欄(0)</a><br1><font color=\"AAAAAA\">七個封印 限競爭期間內可使用之 黎明的君主 專用佈告欄</font></td>");
				htmlCode.append("<td FIXWIDTH=80 align=center valign=top>1</td>");
				htmlCode.append("<td FIXWIDTH=110 align=center valign=top>管理者</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=610><img src=\"l2ui.squaregray\" width=\"610\" height=\"1\"></td>");//分隔線
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<br>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=60 align=right valign=top><img src=\"l2ui.bbs_board\" width=32 height=32></td>");
				htmlCode.append("<td FIXWIDTH=410 align=left valign=top><a action=\"bypass bbs_init_1_2_2\">七個封印交流區 黃昏的革命軍 佈告欄(0)</a><br1><font color=\"AAAAAA\">七個封印 限競爭期間內可使用之 黃昏的革命軍 專用佈告欄</font></td>");
				htmlCode.append("<td FIXWIDTH=80 align=center valign=top>1</td>");
				htmlCode.append("<td FIXWIDTH=110 align=center valign=top>管理者</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=610><img src=\"l2ui.squaregray\" width=\"610\" height=\"1\"></td>");//分隔線
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br><br></center>");
			}
			
			else if (command.equals("bbs_init_1_3"))//天堂 II 活動快報
			{
				htmlCode.append("<center><br><br1><br1>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass _bbshome\">HOME</a>&nbsp;&gt;&nbsp;<a action=\"bypass bbs_init_1_3\">天堂 II 活動快報</a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td WIDTH=60 align=center>&$412;</td>");//類別
				htmlCode.append("<td WIDTH=410 align=center>&$413;</td>");//標題
				htmlCode.append("<td WIDTH=80 align=center>&$414;</td>");//數目
				htmlCode.append("<td WIDTH=110 align=center>&$415;</td>");//管理者
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<br>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2><tr>");
				htmlCode.append("<td FIXWIDTH=60 align=right valign=top><img src=\"l2ui.bbs_board\" width=32 height=32></td>");
				htmlCode.append("<td FIXWIDTH=410 align=left valign=top><a action=\"bypass bbs_init_1_3_1\">更新情報 (0)</a><br1><font color=\"AAAAAA\">天堂 II 更新情報</font></td>");
				htmlCode.append("<td FIXWIDTH=80 align=center valign=top>1</td>");
				htmlCode.append("<td FIXWIDTH=110 align=center valign=top>管理者</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=610><img src=\"l2ui.squaregray\" width=\"610\" height=\"1\"></td>");//分隔線
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<br>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=60 align=right valign=top><img src=\"l2ui.bbs_board\" width=32 height=32></td>");
				htmlCode.append("<td FIXWIDTH=410 align=left valign=top><a action=\"bypass bbs_init_1_3_2\">活動 (0)</a><br1><font color=\"AAAAAA\">天堂 II 活動</font></td>");
				htmlCode.append("<td FIXWIDTH=80 align=center valign=top>1</td>");
				htmlCode.append("<td FIXWIDTH=110 align=center valign=top>管理者</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=610><img src=\"l2ui.squaregray\" width=\"610\" height=\"1\"></td>");//分隔線
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				/*htmlCode.append("<br>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=60 align=right valign=top><img src=\"l2ui.bbs_board\" width=32 height=32></td>");
				htmlCode.append("<td FIXWIDTH=410 align=left valign=top><a action=\"bypass bbs_init_1_3_3\">更新資訊 (0)</a><br1><font color=\"AAAAAA\">目錄手札</font></td>");
				htmlCode.append("<td FIXWIDTH=80 align=center valign=top>1</td>");
				htmlCode.append("<td FIXWIDTH=110 align=center valign=top>管理者</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");*/
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=610><img src=\"l2ui..squaregray\" width=\"610\" height=\"1\"></td>");//分隔線
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<br><br></center>");
			}
			
			else if (command.equals("bbs_init_1_3_1"))//天堂 II 更新情報
			{
				htmlCode.append("<center><br><br1><br1>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass _bbshome\">HOME</a>&nbsp;&gt;&nbsp;<a action=\"bypass bbs_init_1_3\">天堂 II 活動快報</a>&nbsp;&gt;&nbsp;<a action=\"bypass bbs_init_1_3_1\">天堂 II 更新情報</a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=60 align=center>&$416;</td>");//編號
				htmlCode.append("<td FIXWIDTH=305 align=center>&$413;</td>");//標題
				htmlCode.append("<td FIXWIDTH=120 align=center>&$417;</td>");//作者
				htmlCode.append("<td FIXWIDTH=70 align=center>&$418;</td>");//貼上日期
				htmlCode.append("<td FIXWIDTH=55 align=center>&$419;</td>");//閱覽數
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<br>");
				
				htmlCode.append("<table width=610 cellspace=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=50><button value=\"&$422;\" action=\"bypass _bbslist_5_1_0\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");//觀看目錄 按鈕
				htmlCode.append("<td width=510 align=center>");
				
				htmlCode.append("<table cellspacing=2 cellpadding=0 border=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
				htmlCode.append("<td>1</td>");
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("</td>");
				htmlCode.append("<td align=right><button value=\"&$421;\" action=\"bypass _bbser2_5_1_0\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td>");//寫新文章 按鈕
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><img src=\"l2ui.mini_logo\" width=5 height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
				htmlCode.append("<td align=center>");
				
				htmlCode.append("<table border=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><combobox width=65 var=keyword list=\"Title;Writer\"></td>");//Title
				htmlCode.append("<td><edit var=\"Search\" width=130 height=11 length=\"16\"></td>");
				htmlCode.append("<td><button value=\"&$420;\" action=\"Write 5 5 0 Search keyword keyword\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");////查詢 按鈕
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<br><br><br></center>");
			}
			
			else if (command.equals("bbs_init_1_3_2"))//天堂 II 活動
			{
				htmlCode.append("<center><br><br1><br1>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass _bbshome\">HOME</a>&nbsp;&gt;&nbsp;<a action=\"bypass bbs_init_1_3\">天堂 II 活動快報</a>&nbsp;&gt;&nbsp;<a action=\"bypass bbs_init_1_3_2\">天堂 II 活動</a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0 width=610  bgcolor=333333>");
				htmlCode.append("<tr>");
				htmlCode.append("<td height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td fixWIDTH=5></td>");
				htmlCode.append("<td fixWIDTH=90 align=left valign=top>[&$429;]</td>");
//				 action
				htmlCode.append("<td fixWIDTH=380 align=left valign=top><a action=\"bypass bbs_init_1_3_2_1\">歡迎來到天堂II的世界</a></td>");
				htmlCode.append("<td fixWIDTH=75 align=right valign=top>&$418; :</td>");
				htmlCode.append("<td fixWIDTH=55 align=right valign=top>05-05-10</td>");
				htmlCode.append("<td fixWIDTH=5></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=60 align=center>&$416;</td>");
				htmlCode.append("<td FIXWIDTH=305 align=center>&$413;</td>");
				htmlCode.append("<td FIXWIDTH=120 align=center>&$417;</td>");
				htmlCode.append("<td FIXWIDTH=70 align=center>&$418;</td>");
				htmlCode.append("<td FIXWIDTH=55 align=center>&$419;</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table width=610 cellspace=0 cellpadding=0>");
				htmlCode.append("<tr>");
//				 action
				htmlCode.append("<td width=50><button value=\"&$422;\" action=\"bypass _bbslist_4_1_0\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("<td width=510 align=center>");
				htmlCode.append("<table cellspacing=2 cellpadding=0 border=0>");
				htmlCode.append("<tr>");
//				 action
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
				htmlCode.append("<td>1</td>");
//				 action
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
//				 action
				htmlCode.append("<td align=right><button value = \"&$421;\" action=\"bypass _bbser2_4_1_0\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><img src=\"l2ui.mini_logo\" width=5 height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td> ");
				htmlCode.append("<td align=center>");
				htmlCode.append("<table border=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><combobox width=65 var=keyword list=\"主題;作者\"></td>");
				htmlCode.append("<td><edit var=\"Search\" width=130 height=11 length=\"16\"></td>");
//				 action
				htmlCode.append("<td><button value=\"&$420;\" action=\"Write 5 4 0 Search keyword keyword\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br><br><br></center>");
			}
			
			else if (command.equals("bbs_init_1_3_2_1"))//歡迎來到天堂II的世界
			{
				htmlCode.append("<br><br1><br1>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
//				 action
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass _bbshome\">HOME</a>&nbsp;&gt; <a action=\"bypass bbs_init_1_3\">天堂 II 活動快報</a>&nbsp;&gt; <a action=\"bypass bbs_init_1_3_2\">天堂 II 活動</a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<center>");
				htmlCode.append("<table width=610 border=0 cellspacing=0 cellpadding=0 bgcolor=333333>");
				htmlCode.append("<tr>");
				htmlCode.append("<td height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td fixWIDTH=55 align=center valign=top></td>");
				htmlCode.append("<td fixWIDTH=380 valign=top>歡迎來到天堂II的世界</td>");
				htmlCode.append("<td fixwidth=5></td><td><font color=\"AAAAAA\">&$418; :</font></td>");
				htmlCode.append("<td><font color=\"AAAAAA\">2005-05-10 11:31:50</font></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td fixwidth=5></td>");
				htmlCode.append("<td FIXWIDTH=600 align=left></td>");
				htmlCode.append("<td fixwidth=5></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<img src=\"L2UI.squaregray\" width=\"610\" height=\"1\">");
				htmlCode.append("<br><br><br></center>");
			}

			/*else if (command.equals("bbs_init_1_3_3"))
			{
				htmlCode.append("<center><br><br1><br1>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
//				 action
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass _bbshome\"> 首頁 </a>&nbsp;&gt; <a action=\"bypass bbs_init_1_3\"> 天堂II新聞 </a>&nbsp;&gt; <a action=\"bypass bbs_init_1_3_3\"> 更新資訊 </a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=60 align=center>&$416;</td>");
				htmlCode.append("<td FIXWIDTH=305 align=center>&$413;</td>");
				htmlCode.append("<td FIXWIDTH=120 align=center>&$417;</td>");
				htmlCode.append("<td FIXWIDTH=70 align=center>&$418;</td>");
				htmlCode.append("<td FIXWIDTH=55 align=center>&$419;</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table width=610 cellspace=0 cellpadding=0>");
				htmlCode.append("<tr>");
//				 action
				htmlCode.append("<td width=50><button value=\"&$422;\" action=\"bypass _bbslist_6_1_0\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("<td width=510 align=center>");
				htmlCode.append("<table cellspacing=2 cellpadding=0 border=0>");
				htmlCode.append("<tr>");
//				 action
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
				htmlCode.append("<td>1</td>");
//				 action
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
//				 action
				htmlCode.append("<td align=right><button value=\"&$421;\" action=\"bypass _bbser2_6_1_0\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><img src=\"l2ui.mini_logo\" width=5 height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
				htmlCode.append("<td align=center><table border=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><combobox width=65 var=keyword list=\"主題;作者\"></td>");
				htmlCode.append("<td><edit var=\"Search\" width=130 height=11 length=\"16\"></td>");
//				 action
				htmlCode.append("<td><button value=\"&$420;\" action=\"Write 5 6 0 Search keyword keyword\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br><br><br></center>");
			}*/

			else if (command.equals("bbs_default") || command.equals("_bbsloc"))//領地連結
			{
				/*htmlCode.append("<table border=0><tr><td FIXWIDTH=15></td><td align=center width=610 height=30><img src=\"l2ui.bbs_lineage2\" width=128 height=16 ></td></tr><tr><td FIXWIDTH=15></td><td>");

				Collection<L2PcInstance> players = L2World.getInstance().getAllPlayers();
				htmlCode.append("<table border=0>");
				
				//時間計算常數設置
				int t = GameTimeController.getInstance().getGameTime();
				int h = t/60;
				int m = t%60;
				SimpleDateFormat format = new SimpleDateFormat("H:mm");
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, h);
				cal.set(Calendar.MINUTE, m);
				
				htmlCode.append("<tr><td>遊戲時間: "+format.format(cal.getTime())+"</td></tr>");
				htmlCode.append("<tr><td>經驗倍率: "+Config.RATE_XP+"</td></tr>");
				htmlCode.append("<tr><td>技能點倍率: "+Config.RATE_SP+"</td></tr>");
				htmlCode.append("<tr><td>金錢倍率: "+Config.RATE_DROP_ADENA+"</td></tr>");
				htmlCode.append("<tr><td>道具掉落倍率: "+Config.RATE_DROP_ITEMS+"</td></tr>");
				htmlCode.append("<tr><td>狩獵倍率: "+Config.RATE_DROP_SPOIL+"</td></tr>");
				htmlCode.append("<tr><td>經驗指數: "+Config.ALT_GAME_EXPONENT_XP+"</td></tr>");
				htmlCode.append("<tr><td>技能點指數: "+Config.ALT_GAME_EXPONENT_SP+"</td></tr>");
				htmlCode.append("<tr><td><img src=\"sek.cbui355\" width=610 height=1><br></td></tr>");
				
				htmlCode.append("<tr><td>在線玩家: "+players.size()+"人</td></tr><tr><td><table border=0><tr>");
				int n = 1;
				for (L2PcInstance player : players)
				{
					if (n == 5)
					{
						htmlCode.append("</tr><tr>");
						n = 0;
					}
					n++;
				}
				htmlCode.append("</tr></table></td></tr></table>");
				htmlCode.append("</td></tr></table>");*/
			}
			
			else if (command.equals("bbs_init_1_4"))//地區 社群
			{
				htmlCode.append("<center><br><br1><br1>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
//				 action
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass _bbshome\"> 首頁 </a>&nbsp;&gt;&nbsp;<a action=\"bypass bbs_init_1_3\"> 血盟交流 </a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0 width=610 bgcolor=434343>");
				htmlCode.append("<tr>");
				htmlCode.append("<td height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td fixWIDTH=5></td>");
//				 action
				htmlCode.append("<td fixWIDTH=600><a action=\"bypass _clbbsclan_0\">[血盟情報]</a>&nbsp;&nbsp;</td>");
				htmlCode.append("<td fixWIDTH=5></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=5></td>");
				htmlCode.append("<td FIXWIDTH=200 align=center>血盟名稱</td>");
				htmlCode.append("<td FIXWIDTH=200 align=center>血盟盟主</td>");
				htmlCode.append("<td FIXWIDTH=100 align=center>血盟等級</td>");
				htmlCode.append("<td FIXWIDTH=100 align=center>血盟成員</td>");
				htmlCode.append("<td FIXWIDTH=5></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<img src=\"L2UI.Squareblank\" width=\"1\" height=\"5\"><img src=\"L2UI.SquareBlank\" width=\"610\" height=\"3\">");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0 width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=5></td>");
//				 action
				htmlCode.append("<td FIXWIDTH=200 align=center><a action=\"bypass _clbbsclan_1847\">天堂</a></td>");
				htmlCode.append("<td FIXWIDTH=200 align=center>測試員</td>");
				htmlCode.append("<td FIXWIDTH=100 align=center>1</td>");
				htmlCode.append("<td FIXWIDTH=100 align=center>1</td>");
				htmlCode.append("<td FIXWIDTH=5></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td height=5></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<img src=\"L2UI.SquareBlank\" width=\"610\" height=\"3\"><img src=\"L2UI.SquareGray\" width=\"610\" height=\"1\"><img src=\"L2UI.SquareBlank\" width=\"610\" height=\"3\">");
				htmlCode.append("<table cellpadding=0 cellspacing=2 border=0>");
				htmlCode.append("<tr>");
//				 action
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16></td>");
				htmlCode.append("<td>1</td>");
				htmlCode.append("<td><a action=\"bypass bbs_init_1_4\"> 2 </a></td>");
				htmlCode.append("<td><a action=\"bypass bbs_init_1_4\"> 3 </a></td>");
				htmlCode.append("<td><a action=\"bypass bbs_init_1_4\"> 4 </a></td>");
				htmlCode.append("<td><a action=\"bypass bbs_init_1_4\"> 5 </a></td>");
				htmlCode.append("<td><a action=\"bypass bbs_init_1_4\"> 6 </a></td>");
				htmlCode.append("<td><a action=\"bypass bbs_init_1_4\"> 7 </a></td>");
				htmlCode.append("<td><a action=\"bypass bbs_init_1_4\"> 8 </a></td>");
				htmlCode.append("<td><a action=\"bypass bbs_init_1_4\"> 9 </a></td>");
				htmlCode.append("<td><a action=\"bypass bbs_init_1_4\"> 10 </a></td>");
//				 action
				htmlCode.append("<td><button action=\"bypass _clbbslist_2_\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=610><img src=\"sek.cbui141\" width=\"610\" height=\"1\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br><br></center>");
			}
			
			else if (command.equals("_bbsclan"))//血盟連結
			{
				htmlCode.append("<center><br><br1><br1>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass _bbsclan\">血盟社群系統</a>&nbsp;&gt;&nbsp;<a action=\"bypass _bbsclan\">血盟 HOME</a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0 width=610 bgcolor=555555><tr><td width=5></td><td height=10 width=605></td></tr><tr><td></td><td height=20>");
				htmlCode.append("<a action=\"bypass bbs_clanannounce\">[血盟公告]</a>&nbsp;&nbsp;<a action=\"bypass bbs_clandis\">[血盟自由討論區]</a>&nbsp;&nbsp;<a action=\"bypass bbs_clanmanager\">[血盟管理]</a></td></tr><tr><td></td><td height=10></td></tr></table><br>");

				L2Clan clan = activeChar.getClan();
				String AllyName = "無";
				if(clan==null)
				{
					htmlCode.append("<table border=0 width=610>");
					htmlCode.append("<tr><td width=100>血盟名稱    </td><td>無 </td><td width=300> </td></tr>");
					htmlCode.append("<tr><td width=100>血盟等級    </td><td>無 </td><td width=300> </td></tr>");
					htmlCode.append("<tr><td width=100>血盟人數</td><td>無 </td><td width=300> </td></tr>");
					htmlCode.append("<tr><td width=100>血盟盟主    </td><td>無 </td><td width=300> </td></tr>");
					htmlCode.append("<tr><td width=100>管理者        </td><td>無 </td><td width=300> </td></tr>");
					htmlCode.append("<tr><td width=100>所屬同盟    </td><td>無 </td><td width=300> </td></tr>");
					htmlCode.append("</table>");
				}else
				{
					if(clan.getAllyName()!=null)AllyName = clan.getAllyName();
					htmlCode.append("<table border=0 width=610>");
					htmlCode.append("<tr><td width=150>血盟名稱    </td><td>"+clan.getName()+"        </td><td width=300> </td></tr>");
					htmlCode.append("<tr><td width=150>血盟等級    </td><td>"+clan.getLevel()+"       </td><td width=300> </td></tr>");
					htmlCode.append("<tr><td width=150>血盟人數</td><td>"+clan.getMembersCount()+"</td><td width=300></td></tr>");
					htmlCode.append("<tr><td width=150>血盟盟主    </td><td>"+clan.getLeaderName()+"  </td><td width=300> </td></tr>");
					htmlCode.append("<tr><td width=150>管理者        </td><td>"+clan.getLeaderName()+"  </td><td width=300> </td></tr>");
					htmlCode.append("<tr><td width=150>所屬同盟    </td><td>"+AllyName+"              </td><td width=300> </td></tr>");
					htmlCode.append("</table>");
					
					htmlCode.append("<table border=0 width=610>");
					htmlCode.append("<tr><td width=610></td><td FIXWIDTH=150>封號</td><td FIXWIDTH=150>名字</td></tr>");
					htmlCode.append("<tr><td width=610></td><td FIXWIDTH=150>血盟盟主</td><td FIXWIDTH=150></td></tr>");
					String title = "";
					//盟主名字
					if (!clan.getClanMember(clan.getLeaderName()).getTitle().equals(""))
					{
						title = "["+clan.getClanMember(clan.getLeaderName()).getTitle()+"]";
					}
					String name = clan.getLeaderName();
					if (clan.getClanMember(clan.getLeaderName()).isOnline())
					{
						name = "<a action=\"bypass bbs_player_info "+clan.getLeaderName()+"\">"+clan.getLeaderName()+"</a>";
					}
					htmlCode.append("<tr><td></td><td>"+title+"</td><td>"+name+"</td></tr>");
					htmlCode.append("<tr><td width=610></td><td FIXWIDTH=150>血盟成員</td><td FIXWIDTH=150></td></tr>");
					//成員名字
					L2ClanMember[] members = clan.getMembers();
					for (int i = 0; i < members.length; i++)
					{
						if (members[i].getName() != clan.getLeaderName())
						{
							title = "";
							if (!members[i].getTitle().equals(""))
							{
								title = "["+members[i].getTitle()+"]";
							}
							name = members[i].getName();
							if (members[i].isOnline())
							{
								name = "<a action=\"bypass bbs_player_info "+members[i].getName()+"\">"+members[i].getName()+"</a>";
							}
							htmlCode.append("<tr><td></td><td>"+title+"</td><td>"+name+"</td></tr>");
						}
					}
					htmlCode.append("</table>");
				}
				htmlCode.append("<br><br><br><br>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0 width=610 bgcolor=555555><tr><td width=5></td><td width=605></td></tr><tr><td></td><td>");
				htmlCode.append("</td></tr><tr><td></td><td height=5></td></tr></table><br>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=5></td>");
				htmlCode.append("<td FIXWIDTH=400 align=center>&$413;</td>");
				htmlCode.append("<td FIXWIDTH=80 align=center>&$417;</td>");
				htmlCode.append("<td FIXWIDTH=60 align=center>&$418;</td>");
				htmlCode.append("<td FIXWIDTH=60 align=center>&$419;</td>");
				htmlCode.append("<td FIXWIDTH=5></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<br><br></center>");
			}
			
			else if (command.equals("bbs_clanannounce"))//血盟公告事項
			{
				htmlCode.append("<br><br1><br1>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass _bbshome\">HOME</a>&nbsp;&gt;&nbsp;<a action=\"bypass _bbsclan\">血盟社群系統</a>&nbsp;&gt;&nbsp;<a action=\"bypass _bbsclan\">血盟 HOME</a>&nbsp;&gt;&nbsp;<a action=\"bypass bbs_clanannounce\">公告事項</a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<br><br></center>");
			}
			
			else if (command.equals("bbs_clandis"))//血盟自由討論區
			{
				htmlCode.append("<br><br1><br1>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass _bbshome\">HOME</a>&nbsp;&gt;&nbsp;<a action=\"bypass _bbsclan\">血盟社群系統</a>&nbsp;&gt;&nbsp;<a action=\"bypass _bbsclan\">血盟 HOME</a>&nbsp;&gt;&nbsp;<a action=\"bypass bbs_clandis\">自由討論區</a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<br><br></center>");
			}
			
			else if (command.equals("bbs_clanmanager"))//血盟管理
			{
				htmlCode.append("<br><br1><br1>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass _bbshome\">HOME</a>&nbsp;&gt;&nbsp;<a action=\"bypass _bbsclan\">血盟社群系統</a>&nbsp;&gt;&nbsp;<a action=\"bypass _bbsclan\">血盟 HOME</a>&nbsp;&gt;&nbsp;<a action=\"bypass bbs_clanmanager\">血盟管理</a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<br><br></center>");
			}

			else if (command.equals("_friendlist_0_"))//好友管理
			{
				htmlCode.append("<center><br><br1><br1>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass _friendlist_0_\">好友管理</a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0 width=610 bgcolor=555555><tr><td width=5></td><td height=10 width=605></td></tr><tr><td></td><td height=20>");
				htmlCode.append("<a action=\"bypass \">[好友名單]</a>&nbsp;&nbsp;<a action = \"bypass \">[隔絕名單]</a></td></tr><tr><td></td><td height=10></td></tr></table><br>");
				htmlCode.append("好友名單");
				htmlCode.append("<img src=\"L2UI.SquareGray\" width=\"610\" height=\"1\"><br>");//分隔線
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0 width=610><tr><td height=10>");
        	    java.sql.Connection con = null;
        	    try
        	    {
            		con = L2DatabaseFactory.getInstance().getConnection();
            		PreparedStatement statement = con.prepareStatement("select char_id,friend_name from character_friends where char_id=? or friend_name=?");
            		statement.setInt(1, activeChar.getObjectId());
            		statement.setString(2, activeChar.getName());
            		ResultSet rset = statement.executeQuery();
            		while (rset.next())
                    {
            		    int objectId = rset.getInt("char_id");
            		    String friendName = rset.getString("friend_name");
            		    L2PcInstance friend;
            		    if (activeChar.getObjectId() != objectId)
                        {
                			friend = (L2PcInstance) L2World.getInstance().findObject(objectId);
                			if (friend != null)
                			    friendName = friend.getName();
                			else 
                            {
                			    PreparedStatement statement2 = con.prepareStatement("select char_name from characters where obj_id=?");
                			    statement2.setInt(1, objectId);
                			    ResultSet rset2 = statement2.executeQuery();
                			    if (rset2.next())
                				friendName=rset2.getString("char_name");
                			    statement2.close();
                			}
            		    }
                        else
                            friend = L2World.getInstance().getPlayer(friendName);
            		    if (friend == null)
                        {
                			htmlCode.append("<a action=\"bypass bbs_player_info \">"+friendName+"</a>(Off)&nbsp;");
                			//(Currently: Offline)
            		    }
                        else
                        {
                			//(Currently: Online)
                			htmlCode.append("<a action=\"bypass bbs_player_info \">"+friendName+"</a>(On)&nbsp;");
            		    }
            		}
            		rset.close();
            		statement.close();
        	    }
        	    catch (Exception e)
        	    {
        	        _log.warning("Error in friendlist: "+e);
        	    }
        	    finally
        	    {
        	        try {con.close();} catch (Exception e) {}
        	    }

				htmlCode.append("</td></tr></table><img src=\"L2UI.SquareGray\" width=\"610\" height=\"1\"><br><br>");
				htmlCode.append("<font color=\"999999\">選擇名單</font>");
				htmlCode.append("<img src=\"L2UI.SquareGray\" width=\"610\" height=\"1\"><br>");//分隔線
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0 width=610><tr><td></td></tr></table><br>");
				htmlCode.append("<img src=\"L2UI.SquareGray\" width=\"610\" height=\"1\"><br>");//分隔線
				htmlCode.append("<table fixwidth=610 border=0 cellspacing=0 cellpadding=0><tr><td width=400></td>");
				htmlCode.append("<td align=right width=70><button value=\"刪除\" action=\"bypass frienddelete\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("<td align=right width=70><button value=\"全部刪除\" action=\"bypass frienddeleteall\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("<td align=right width=70><button value=\"新郵件\" action=\"bypass friendmail\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<br><br><br></center>");
			}
			
			else if (command.equals("_bbsmemo"))//備忘錄
			{
				htmlCode.append("<center><br><br1><br1>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass _bbshome\"> 首頁 </a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">備忘錄</a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=888888 width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=5></td>");
				htmlCode.append("<td FIXWIDTH=415 align=center>&$413;</td>");//標題
				htmlCode.append("<td FIXWIDTH=120 align=center></td>");
				htmlCode.append("<td FIXWIDTH=70 align=center>&$418;</td>");//貼上日期
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("<br>");
				
				htmlCode.append("<table width=610 cellspace=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=50><button value=\"&$422;\" action=\"bypass _mmlist_1_\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("<td width=510 align=center>");
				
				htmlCode.append("<table border=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16></td>");
				htmlCode.append("<td>1</td>");
				htmlCode.append("<td><button action=\"bypass _mmlist_2_\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				
				htmlCode.append("</td>");
				htmlCode.append("<td align=right><button value=\"&$421;\" action=\"bypass _mmcrea\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");//觀看目錄
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><img src=\"l2ui.mini_logo\" width=5 height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
				htmlCode.append("<td align=center><table border=0><tr><td></td>");
				htmlCode.append("<td><edit var=\"Search\" width=130 height=11></td>");
				htmlCode.append("<td><button value=\"&$420;\" action=\"Write 5 -2 0 Search _ _\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");//寫新文章
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br><br><br></center>");
			}

			else if (command.equals("_maillist_0_1_0_")) // 郵件
			{
				htmlCode.append("<center><br><br1><br1>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
//				 action
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass _bbshome\"> 首頁 </a>&nbsp;&gt;<a action=\"bypass bbs_mail\">&$917;</a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0 width=610 bgcolor=555555>");
				htmlCode.append("<tr>");
				htmlCode.append("<td WIDTH=5></td>");
				htmlCode.append("<td height=10 WIDTH=605></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
//				 action
				htmlCode.append("<td height=20><a action=\"bypass bbs_mail\">[&amp;$917;]</a>(0)&nbsp; <a action=\"bypass bbs_init_2_1\">[&amp;$918;]</a>(0)&nbsp; <a action=\"bypass bbs_init_2_2\">[&amp;$919;]</a>(0)&nbsp; <a action=\"bypass bbs_init_2_3\">[&amp;$920;]</a>(0)</td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
				htmlCode.append("<td height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=5 align=center></td>");
				htmlCode.append("<td FIXWIDTH=120 align=center>&$911;</td>");
				htmlCode.append("<td FIXWIDTH=410>&amp;$413;</td>");
				htmlCode.append("<td FIXWIDTH=70 align=center>&$910;</td>");
				htmlCode.append("<td FIXWIDTH=5 align=center></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table width=610 cellspace=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=50></td>");
				htmlCode.append("<td width=510 align=center>");
//				 action
				htmlCode.append("<table cellspacing=2 cellpadding=0 border=0><tr><td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
				htmlCode.append("<td>1</td>");
//				 action
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
//				 action
				htmlCode.append("<td align=right><button value=\"&$421;\" action=\"bypass bbs_init_2_4_1\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><img src=\"l2ui.mini_logo\" width=5 height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
				htmlCode.append("<td align=center><table border=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><combobox width=65 var=SearchTarget list=\"Writer;Title\"></td>");
				htmlCode.append("<td><edit var=\"Keyword\" width=130 height=11 length=\"16\"></td>");
//				 action
				htmlCode.append("<td><button value=\"&$420;\" action=\"Write _mailsearch_0__ 0 0 SearchTarget SearchTarget Keyword\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br><br><br></center>");
			}

			else if (command.equals("bbs_init_2_1")) // 郵件送出
			{
				htmlCode.append("<center><br><br1><br1>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
//				 action
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass _bbshome\"> 首頁 </a>&nbsp;&gt;<a action=\"bypass bbs_init_2_1\">&$918;</a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0 width=610 bgcolor=555555>");
				htmlCode.append("<tr>");
				htmlCode.append("<td WIDTH=5></td>");
				htmlCode.append("<td height=10 WIDTH=605></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
//				 action
				htmlCode.append("<td height=20><a action=\"bypass bbs_mail\">[&amp;$917;]</a>(0)&nbsp; <a action=\"bypass bbs_init_2_1\">[&amp;$918;]</a>(0)&nbsp; <a action=\"bypass bbs_init_2_2\">[&amp;$919;]</a>(0)&nbsp; <a action=\"bypass bbs_init_2_3\">[&amp;$920;]</a>(0)</td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
				htmlCode.append("<td height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=5 align=center></td>");
				htmlCode.append("<td FIXWIDTH=120 align=center>&$909;</td>");
				htmlCode.append("<td FIXWIDTH=410>&amp;$413;</td>");
				htmlCode.append("<td FIXWIDTH=70 align=center>&$910;</td>");
				htmlCode.append("<td FIXWIDTH=5 align=center></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table width=610 cellspace=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=50></td>");
				htmlCode.append("<td width=510 align=center>");
				htmlCode.append("<table cellspacing=2 cellpadding=0 border=0>");
				htmlCode.append("<tr>");
//				 action
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16></td>");
				htmlCode.append("<td>1</td>");
//				 action
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
//				 action
				htmlCode.append("<td align=right><button value=\"&$421;\" action=\"bypass bbs_init_2_4_2\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><img src=\"l2ui.mini_logo\" width=5 height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
				htmlCode.append("<td align=center>");
				htmlCode.append("<table border=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><combobox width=65 var=SearchTarget list=\"Writer;Title\"></td>");
				htmlCode.append("<td><edit var=\"Keyword\" width=130 height=11 length=\"16\"></td>");
//				 action
				htmlCode.append("<td><button value=\"&$420;\" action=\"Write _mailsearch_0__ 0 0 SearchTarget SearchTarget Keyword\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br><br><br></center>");
			}

			else if (command.equals("bbs_init_2_2")) // 郵件操作
			{
				htmlCode.append("<center><br><br1><br1>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
//				 action
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass _bbshome\"> 首頁 </a>&nbsp;&gt;<a action=\"bypass bbs_init_2_2\">&$919;</a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0 width=610 bgcolor=555555>");
				htmlCode.append("<tr>");
				htmlCode.append("<td WIDTH=5></td>");
				htmlCode.append("<td height=10 WIDTH=605></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
//				 action
				htmlCode.append("<td height=20><a action=\"bypass bbs_mail\">[&amp;$917;]</a>(0)&nbsp; <a action=\"bypass bbs_init_2_1\">[&amp;$918;]</a>(0)&nbsp; <a action=\"bypass bbs_init_2_2\">[&amp;$919;]</a>(0)&nbsp; <a action=\"bypass bbs_init_2_3\">[&amp;$920;]</a>(0)</td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
				htmlCode.append("<td height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=5 align=center></td>");
				htmlCode.append("<td FIXWIDTH=120 align=center>&$911;</td>");
				htmlCode.append("<td FIXWIDTH=410>&amp;$413;</td>");
				htmlCode.append("<td FIXWIDTH=70 align=center>&$910;</td>");
				htmlCode.append("<td FIXWIDTH=5 align=center></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table width=610 cellspace=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=50></td>");
				htmlCode.append("<td width=510 align=center>");
//				 action
				htmlCode.append("<table cellspacing=2 cellpadding=0 border=0><tr><td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
				htmlCode.append("<td>1</td>");
//				 action
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
//				 action
				htmlCode.append("<td align=right><button value=\"&$421;\" action=\"bypass bbs_init_2_4_3\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><img src=\"l2ui.mini_logo\" width=5 height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
				htmlCode.append("<td align=center><table border=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><combobox width=65 var=SearchTarget list=\"作者;主題\"></td>");
				htmlCode.append("<td><edit var=\"Keyword\" width=130 height=11 length=\"16\"></td>");
//				 action
				htmlCode.append("<td><button value=\"&$420;\" action=\"Write _mailsearch_0__ 0 0 SearchTarget SearchTarget Keyword\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br><br><br></center>");
			}

			else if (command.equals("bbs_init_2_3")) // temporary mail archive
			{
				htmlCode.append("<center><br><br1><br1>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
//				 action
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass _bbshome\"> 首頁 </a>&nbsp;&gt;<a action=\"bypass bbs_init_2_3\">&$920;</a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0 width=610 bgcolor=555555>");
				htmlCode.append("<tr>");
				htmlCode.append("<td WIDTH=5></td>");
				htmlCode.append("<td height=10 WIDTH=605></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
//				 action
				htmlCode.append("<td height=20><a action=\"bypass bbs_mail\">[&amp;$917;]</a>(0)&nbsp; <a action=\"bypass bbs_init_2_1\">[&amp;$918;]</a>(0)&nbsp; <a action=\"bypass bbs_init_2_2\">[&amp;$919;]</a>(0)&nbsp; <a action=\"bypass bbs_init_2_3\">[&amp;$920;]</a>(0)</td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
				htmlCode.append("<td height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=5 align=center></td>");
				htmlCode.append("<td FIXWIDTH=120 align=center>&$909;</td>");
				htmlCode.append("<td FIXWIDTH=410>&amp;$413;</td>");
				htmlCode.append("<td FIXWIDTH=70 align=center>&$910;</td>");
				htmlCode.append("<td FIXWIDTH=5 align=center></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table width=610 cellspace=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=50></td>");
				htmlCode.append("<td width=510 align=center>");
				htmlCode.append("<table cellspacing=2 cellpadding=0 border=0>");
				htmlCode.append("<tr>");
//				 action
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16></td>");
				htmlCode.append("<td>1</td>");
//				 action
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
//				 action
				htmlCode.append("<td align=right><button value=\"&$421;\" action=\"bypass bbs_init_2_4_4\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><img src=\"l2ui.mini_logo\" width=5 height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
				htmlCode.append("<td align=center>");
				htmlCode.append("<table border=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><combobox width=65 var=SearchTarget list=\"Writer;Title\"></td>");
				htmlCode.append("<td><edit var=\"Keyword\" width=130 height=11 length=\"16\"></td>");
//				 action
				htmlCode.append("<td><button value=\"&$420;\" action=\"Write _mailsearch_0__ 0 0 SearchTarget SearchTarget Keyword\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br><br><br></center>");
			}

			else if (command.startsWith("bbs_init_2_4")) // 新郵件
			{
				htmlCode.append("<br><br1><br1>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
				
				if (command.equals("bbs_init_2_4_1"))
				{
					htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass _bbshome\"> 首頁 </a>&nbsp;&gt;<a action=\"bypass bbs_mail\">&$917;</a></td>");
				}
				
				else if (command.equals("bbs_init_2_4_2"))
				{
					htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass _bbshome\"> 首頁 </a>&nbsp;&gt;<a action=\"bypass bbs_init_2_1\">&$918;</a></td>");
				}
				
				else if (command.equals("bbs_init_2_4_3"))
				{
					htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass _bbshome\"> 首頁 </a>&nbsp;&gt;<a action=\"bypass bbs_init_2_2\">&$919;</a></td>");
				}
				
				else if (command.equals("bbs_init_2_4_4"))
				{
					htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass _bbshome\"> 首頁 </a>&nbsp;&gt;<a action=\"bypass bbs_init_2_3\">&$920;</a></td>");
				}
				
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=610><img src=\"L2UI.SquareGray\" width=\"610\" height=\"1\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table fixwidth=610 border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><img src=\"l2ui.mini_logo\" width=5 height=20></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=5></td>");
				htmlCode.append("<td align=center FIXWIDTH=50 height=29>To:</td>");
				htmlCode.append("<td FIXWIDTH=550><edit var=\"ToList\" width=550 height=13 length=\"128\"></td>");
				htmlCode.append("<td width=5></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
				htmlCode.append("<td align=center height=29>&amp;$413;</td>");
				htmlCode.append("<td><edit var=\"Title\" width=550 height=13 length=\"128\"></td>");
				htmlCode.append("<td></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table fixwidth=610 border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=5></td>");
				htmlCode.append("<td align=center FIXWIDTH=50 height=29 valign=top>&amp;$427;</td>");
				htmlCode.append("<td FIXWIDTH=550><MultiEdit var=\"Content\" width=550 height=313></td>");
				htmlCode.append("<td width=5></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=5 height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table fixwidth=610 border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=5></td>");
				htmlCode.append("<td align=center FIXWIDTH=50 height=29>&nbsp;</td>");
				htmlCode.append("<td align=center FIXWIDTH=70><button value=\"往前\" action=\"Write _mailsend_0_0_3_1_0__ 0 0 ToList Title Content\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("<td align=center FIXWIDTH=70><button value=\"&$141;\" action=\"bypass _maillist_3_1_0_\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("<td align=center FIXWIDTH=70><button value=\"儲存\" action=\"Write _mailsave_0_0_3_1_0__ 0 0 ToList Title Content\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("<td align=center FIXWIDTH=340>&nbsp;</td>");
				htmlCode.append("<td width=5></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
			}
			
			else if (command.startsWith("bbs_player_info"))//玩家資訊
			{
				htmlCode.append("<table border=0><tr><td FIXWIDTH=15></td><td align=center>天堂2社群版<img src=\"sek.cbui355\" width=610 height=1></td></tr><tr><td FIXWIDTH=15></td><td>");

				String name = command.substring(16);
				L2PcInstance player = L2World.getInstance().getPlayer(name);
				
				if (player != null)
				{
				    /*String sex = "男";
				    if (player.getSex() == 1)
				    {
				        sex = "女";
				    }
				    */
				    String levelApprox = "新手";
				    if (player.getLevel() >= 60)
				        levelApprox = "高級";
				    else if (player.getLevel() >= 40)
				        levelApprox = "中級";
				    else if (player.getLevel() >= 20)
				        levelApprox = "初級";
				    htmlCode.append("<table border=0><tr><td>"+player.getName()+" ("+player.getTemplate().className+"):</td></tr>");
				    htmlCode.append("<tr><td>等級: "+levelApprox+"</td></tr>");
				    htmlCode.append("<tr><td><br></td></tr>");
				    
				    if (activeChar != null && (activeChar.isGM() || player.getObjectId() == activeChar.getObjectId()
				            || Config.SHOW_LEVEL_COMMUNITYBOARD))
				    {
				        long nextLevelExp = 0;
				        long nextLevelExpNeeded = 0;
				        if (player.getLevel() < 75)
				        {
				            nextLevelExp = Experience.LEVEL[player.getLevel() + 1];
				            nextLevelExpNeeded = nextLevelExp-player.getExp();
				        }
				        
				        htmlCode.append("<tr><td>等級: "+player.getLevel()+"</td></tr>");
				        htmlCode.append("<tr><td>經驗: "+player.getExp()+"/"+nextLevelExp+"</td></tr>");
				        htmlCode.append("<tr><td>下次升級經驗: "+nextLevelExpNeeded+"</td></tr>");
				        htmlCode.append("<tr><td><br></td></tr>");
				    }
				    
				    int uptime = (int)player.getUptime()/1000;
				    int h = uptime/3600;
				    int m = (uptime-(h*3600))/60;
				    int s = ((uptime-(h*3600))-(m*60));
				    
				    htmlCode.append("<tr><td>上線時間: "+h+"時 "+m+"分 "+s+"秒</td></tr>");
				    htmlCode.append("<tr><td><br></td></tr>");
				    
				    if (player.getClan() != null)
				    {
				        htmlCode.append("<tr><td>血盟: "+player.getClan().getName()+"</td></tr>");
				        htmlCode.append("<tr><td><br></td></tr>");
				    }
				    
				    htmlCode.append("<tr><td><multiedit var=\"pm\" width=240 height=40>");
				    htmlCode.append("<button value=\"送出密語\" action=\"bypass bbs_player_pm "+player.getName()+" $pm\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
                    htmlCode.append("<tr><td><br><button value=\"返回\" action=\"bypass bbs_default\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");

                    if(activeChar.isGM())
                    {
                        /** admin manage button */
                        htmlCode.append("<br><br>======= 角色管理 =======");
                        htmlCode.append("<table><tr><td>");
                        htmlCode.append("<button value=\"狀態\" action=\"bypass -h admin_character_list " +player.getName()+ "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
                        htmlCode.append("<button value=\"移動\" action=\"bypass -h admin_teleportto " +player.getName()+ "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
                        htmlCode.append("<button value=\"召喚\" action=\"bypass -h admin_recall " +player.getName()+ "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
                        htmlCode.append("</td></tr><tr></tr><tr><td>");
                        htmlCode.append("<button value=\"復活\" action=\"bypass -h admin_res " +player.getName()+ "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
                        htmlCode.append("<button value=\"治癒\" action=\"bypass -h admin_heal " +player.getName()+ "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
                        htmlCode.append("<button value=\"回歸\" action=\"bypass -h admin_sendhome " +player.getName()+ "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
                        htmlCode.append("</td></tr></table>");
                    }
                    htmlCode.append("</td></tr></table>");
				}
			}
			
            //密語
			else if (command.startsWith("bbs_player_pm"))
			{
				htmlCode.append("<table border=0><tr><td FIXWIDTH=15></td><td align=center>天堂2社群版<img src=\"sek.cbui355\" width=610 height=1></td></tr><tr><td FIXWIDTH=15></td><td>");

				try
				{
					String val = command.substring(14);
					StringTokenizer st = new StringTokenizer(val);
					String name = st.nextToken();
					String message = val.substring(name.length()+1);
					L2PcInstance reciever = L2World.getInstance().getPlayer(name);
					CreatureSay cs = new CreatureSay(activeChar.getObjectId(), Say2.TELL, activeChar.getName(), message);
					if(reciever!=null && !reciever.getMessageRefusal())
					{
						reciever.sendPacket(cs);
						activeChar.sendPacket(cs);
						htmlCode.append("訊息已經送出<br><button value=\"返回\" action=\"bypass bbs_player_info "+reciever.getName()+"\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
					}
                    else
                    {
                        SystemMessage sm = new SystemMessage(SystemMessage.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);        
                        activeChar.sendPacket(sm);
                    }
				}
				catch (StringIndexOutOfBoundsException e)
				{
					// ignore
				}
				htmlCode.append("</td></tr></table>");
			}

			else
			{
				String strcommand=command;
				if(command.equals("_bbshome")){
					strcommand = "首頁";
				}
				else if(command.equals("_bbsgetfav")){
					strcommand = "書籤";
				}
				else if(command.equals("_bbsloc")){
					strcommand = "領地連結";
				}
				else if(command.equals("_bbsclan")){
					strcommand = "血盟";
				}
				else if(command.equals("_bbsmemo")){
					strcommand = "備註";
				}
				else if(command.equals("_maillist_0_1_0_")){
					strcommand = "郵件";
				}
				else if(command.equals("_friendlist_0_")){
					strcommand = "好友管理";
				}
				else if(command.equals("bbs_add_fav")){
					strcommand = "加入書籤";
				}
				else{
					strcommand = "預設";
				}
				htmlCode.append("<br><center>版面: "+ strcommand +" 尚未開發完成</center>");
			}
			
			htmlCode.append("</body></html>");
			
			ShowBoard sb = new ShowBoard(activeChar,htmlCode.toString());
			activeChar.sendPacket(sb);
		}
	}

    //討論版預設頁
	private void onBBSDefault(StringBuffer htmlCode, int startIndex, L2PcInstance activeChar) 
	{
		if (activeChar != null)
        {
			htmlCode.append("<br><br1><br1>");
			htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
			htmlCode.append("<tr>");
			htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
			htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass _bbshome\">HOME</a>&nbsp;&gt;&nbsp;<a action=\"bypass bbs_default\">伺服器資訊</a></td>");
			htmlCode.append("</tr>");
			htmlCode.append("</table>");
			
            Collection<L2PcInstance> players = L2World.getInstance().getAllPlayers();
            String tdClose = "</td>";
            String tdOpen = "<td align=left valign=top>";
            String trClose = "</tr>";
            String trOpen = "<tr>";
            String colSpacer = "<td FIXWIDTH=15></td>";

            htmlCode.append("<table>");

            SimpleDateFormat format = new SimpleDateFormat("H:mm");
            Calendar cal = Calendar.getInstance();
            int t = GameTimeController.getInstance().getGameTime();

            htmlCode.append(trOpen);
            htmlCode.append(tdOpen + "伺服器時間: " + format.format(cal.getTime()) + tdClose);
            htmlCode.append(colSpacer);
            cal.set(Calendar.HOUR_OF_DAY, t / 60);
            cal.set(Calendar.MINUTE, t % 60);
            htmlCode.append(tdOpen + "遊戲時間: " + format.format(cal.getTime()) + tdClose);
            htmlCode.append(colSpacer);
           // htmlCode.append(tdOpen + "啟動時間: " + GameServer.DateTimeServerStarted.getTime() + tdClose);
            htmlCode.append(trClose);
/*
            htmlCode.append(trOpen);
            htmlCode.append(tdOpen + "經驗倍率: " + Config.RATE_XP + tdClose);
            htmlCode.append(colSpacer);
            htmlCode.append(tdOpen + "組隊經驗倍率: " + Config.RATE_PARTY_XP + tdClose);
            htmlCode.append(colSpacer);
            htmlCode.append(tdOpen + "經驗值指數: " + Config.ALT_GAME_EXPONENT_XP + tdClose);
            htmlCode.append(trClose);

            htmlCode.append(trOpen);
            htmlCode.append(tdOpen + "技能點倍率: " + Config.RATE_SP + tdClose);
            htmlCode.append(colSpacer);
            htmlCode.append(tdOpen + "組隊技能點倍率: " + Config.RATE_PARTY_SP + tdClose);
            htmlCode.append(colSpacer);
            htmlCode.append(tdOpen + "技能點指數: " + Config.ALT_GAME_EXPONENT_SP + tdClose);
            htmlCode.append(trClose);

            htmlCode.append(trOpen);
            htmlCode.append(tdOpen + "掉寶倍率: " + Config.RATE_DROP_ITEMS + tdClose);
            htmlCode.append(colSpacer);
            htmlCode.append(tdOpen + "回收倍率: " + Config.RATE_DROP_SPOIL + tdClose);
            htmlCode.append(colSpacer);
            htmlCode.append(tdOpen + "金錢倍率: " + Config.RATE_DROP_ADENA + tdClose);
            htmlCode.append(trClose);
*/
            htmlCode.append("</table>");
            htmlCode.append("<table>");
            htmlCode.append(trOpen);
            htmlCode.append("<td><img src=\"sek.cbui355\" width=625 height=1><br></td>");
            htmlCode.append(trClose);

            if (activeChar.isGM())
            {
                htmlCode.append(trOpen);
                htmlCode.append(tdOpen + "物件數量" +L2World.getInstance().getAllVisibleObjects().size()
                    + "</td>");
                htmlCode.append(trClose);
            }

            htmlCode.append(trOpen);
            htmlCode.append(tdOpen+ "在線玩家:"+ players.size() + "人</td>");
            htmlCode.append(trClose);
            htmlCode.append("</table>");

            int i;
            htmlCode.append("<table border=0>");
            htmlCode.append("<tr><td><table border=0>");
            Iterator<L2PcInstance> iterator = players.iterator();

            int cell = 0;
            for (i = 0; i < startIndex + Config.NAME_PAGE_SIZE_COMMUNITYBOARD; i++)
            {
                if (i > players.size() - 1 || !iterator.hasNext()) break;

                L2PcInstance player = iterator.next();  // Get the current record
                if (i < startIndex) continue;           // If not at start index
                if ((player == null) || (player.getInvisible() == 1 && player != activeChar && !activeChar.isGM()))
                {
                    i--;                                // Don't count the current loop
                    continue;                           // Go to next
                }

                cell++;

                if (cell == 1) htmlCode.append(trOpen);

                htmlCode.append("<td align=left valign=top FIXWIDTH=75><a action=\"bypass bbs_player_info "
                    + player.getName() + "\">");

                if (player.isGM()) htmlCode.append("<font color=\"LEVEL\">" + player.getName()
                    + "</font>");
                else htmlCode.append(player.getName());

                htmlCode.append("</a></td>");

                if (cell < Config.NAME_PER_ROW_COMMUNITYBOARD) htmlCode.append(colSpacer);

                if (cell == Config.NAME_PER_ROW_COMMUNITYBOARD)
                {
                    cell = 0;
                    htmlCode.append(trClose);
                }
            }
            if (cell > 0 && cell < Config.NAME_PER_ROW_COMMUNITYBOARD) htmlCode.append(trClose);
            htmlCode.append("</table></td></tr>");

            if (players.size() > Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
            {
                htmlCode.append("<tr><td align=center valign=top>顯示 " + (startIndex + 1) + " - "
                    + i + " 玩家</td></tr>");
                htmlCode.append("<tr><td align=center valign=top><table border=0 width=610>");
                htmlCode.append("<tr>");
                if (startIndex == 0) htmlCode.append("<td><button value=\"上一頁\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
                else htmlCode.append("<td><button value=\"上一頁\" action=\"bypass bbs_default "
                    + (startIndex - Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
                    + "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
                htmlCode.append("<td FIXWIDTH=10></td>");
                if (players.size() <= startIndex + Config.NAME_PAGE_SIZE_COMMUNITYBOARD) htmlCode.append("<td><button value=\"下一頁\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
                else htmlCode.append("<td><button value=\"下一頁\" action=\"bypass bbs_default "
                    + (startIndex + Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
                    + "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
                htmlCode.append("</tr>");
                htmlCode.append("</table></td></tr>");
            }

            htmlCode.append("</table>");
        }
	}

	/**
	 * @param client
	 * @param url
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 */
	public void handleWriteCommands(L2GameClient client, String url, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		// TODO Auto-generated method stub
		
	}
}
