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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2SkillLearn;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.PledgeSkillList;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class handles following admin commands:
 * - show_skills
 * - remove_skills
 * - skill_list
 * - skill_index
 * - add_skill
 * - remove_skill
 * - get_skills
 * - reset_skills
 * - give_all_skills
 * - remove_all_skills
 * - add_clan_skills
 *
 * @version $Revision: 1.2.4.7 $ $Date: 2005/04/11 10:06:02 $
 */
public class AdminSkill implements IAdminCommandHandler {
	private static Logger _log = Logger.getLogger(AdminSkill.class.getName());

	private static final String[] ADMIN_COMMANDS = {
		"admin_show_skills",
		"admin_remove_skills",
		"admin_skill_list",
		"admin_skill_index",
		"admin_add_skill",
		"admin_remove_skill",
		"admin_get_skills",
		"admin_reset_skills",
		"admin_give_all_skills",
		"admin_remove_all_skills",
		"admin_add_clan_skill"
	};

	private static L2Skill[] adminSkills;

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_show_skills"))
			showMainPage(activeChar);
		else if (command.startsWith("admin_remove_skills"))
		{
			try
			{
				String val = command.substring(20);
				removeSkillsPage(activeChar, Integer.parseInt(val));
			}
			catch (StringIndexOutOfBoundsException e){}
		}
		else if (command.startsWith("admin_skill_list"))
		{
			AdminHelpPage.showHelpPage(activeChar, "skills.htm");
		}
		else if (command.startsWith("admin_skill_index"))
		{
			try
			{
				String val = command.substring(18);
				AdminHelpPage.showHelpPage(activeChar, "skills/" + val + ".htm");
			}
			catch (StringIndexOutOfBoundsException e){}
		}
		else if (command.startsWith("admin_add_skill"))
		{
			try
			{
				String val = command.substring(15);
				adminAddSkill(activeChar, val);
			}

			catch (Exception e)
			{
				activeChar.sendMessage("使用方法: //add_skill <skill_id> <level>");

			}
		}
		else if (command.startsWith("admin_remove_skill"))
		{
			try
			{
				String id = command.substring(19);
				int idval = Integer.parseInt(id);
				adminRemoveSkill(activeChar, idval);
			}

			catch (Exception e)
			{
				activeChar.sendMessage("使用方法: //remove_skill <skill_id>");

			}
		}
		else if (command.equals("admin_get_skills"))
		{
			adminGetSkills(activeChar);
		}
		else if (command.equals("admin_reset_skills"))
		{
			adminResetSkills(activeChar);
		}
		else if (command.equals("admin_give_all_skills"))
		{
			adminGiveAllSkills(activeChar);
		}

		else if (command.equals("admin_remove_all_skills"))
		{

			if (activeChar.getTarget() instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance)activeChar.getTarget();
				for (L2Skill skill : player.getAllSkills())
					player.removeSkill(skill);
				//activeChar.sendMessage("移除" + player.getName() + "所有技能.");
		        player.sendMessage("已移除全部技能。");
				player.sendSkillList(); 
			}

		}
		else if (command.startsWith("admin_add_clan_skill"))
		{
			try
			{
				String[] val = command.split(" ");
				adminAddClanSkill(activeChar, Integer.parseInt(val[1]),Integer.parseInt(val[2]));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("使用方法: //add_clan_skill <skill_id> <level>");
			}
		}
		return true;
	}

	/**
	 * This function will give all the skills that the target can learn at his/her level
	 * @param activeChar: the gm char
	 */
	private void adminGiveAllSkills(L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
			player = (L2PcInstance)target;
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		boolean countUnlearnable = true;
		int unLearnable = 0;
		int skillCounter = 0;
		L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player, player.getClassId());
		while(skills.length > unLearnable)
		{
			for (L2SkillLearn s : skills)
			{
				L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				if (sk == null || !sk.getCanLearn(player.getClassId()))
				{
					if(countUnlearnable)
						unLearnable++;
					continue;
				}
				if(player.getSkillLevel(sk.getId()) == -1)
					skillCounter++;
				player.addSkill(sk, true);
			}
			countUnlearnable = false;
			skills = SkillTreeTable.getInstance().getAvailableSkills(player, player.getClassId());
		}
		//Notify player and admin
        player.sendMessage("已增加" + skillCounter + "個技能。");
        //activeChar.sendMessage("增加" + skillCounter + "個技能給" + player.getName());
		player.sendSkillList();
	}

	public String[] getAdminCommandList() {
		return ADMIN_COMMANDS;
	}

	private void removeSkillsPage(L2PcInstance activeChar, int page)
	{	//TODO: Externalize HTML
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
			player = (L2PcInstance)target;

		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return;
		}

		L2Skill[] skills = player.getAllSkills();

		int MaxSkillsPerPage = 10;
		int MaxPages = skills.length / MaxSkillsPerPage;
		if (skills.length > MaxSkillsPerPage * MaxPages)
			MaxPages++;

		if (page>MaxPages)
			page = MaxPages;

		int SkillsStart = MaxSkillsPerPage*page;
		int SkillsEnd = skills.length;
		if (SkillsEnd - SkillsStart > MaxSkillsPerPage)
			SkillsEnd = SkillsStart + MaxSkillsPerPage;

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		TextBuilder replyMSG = new TextBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");

		replyMSG.append("<td width=40><button value=\"首頁\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		replyMSG.append("<td width=180><center>角色選單</center></td>");
		replyMSG.append("<td width=40><button value=\"返回\" action=\"bypass -h admin_show_skills\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");

		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>編輯 <font color=\"LEVEL\">" + player.getName() + "</font></center>");
		replyMSG.append("<br><table width=300><tr><td>等級：" + player.getLevel() + " " + player.getTemplate().className + "</td></tr></table>");
		replyMSG.append("<br><table width=300><tr><td>注意：修改將會造成破壞遊戲平衡</td></tr>");
		replyMSG.append("<tr><td></td></tr></table>");
		replyMSG.append("<br><center>選取想要移除的技能：</center>");
		replyMSG.append("<br>");
		String pages = "<center><table width=270><tr>";
		for (int x=0; x<MaxPages; x++)
		{
			int pagenr = x + 1;
			pages += "<td><a action=\"bypass -h admin_remove_skills " + x + "\">第" + pagenr + "頁</a></td>";
		}
		pages += "</tr></table></center>";
		replyMSG.append(pages);
		replyMSG.append("<br><table width=270>");
		replyMSG.append("<tr><td width=80>名稱：</td><td width=60>等級：</td><td width=40>Id：</td></tr>");
		for (int i = SkillsStart; i < SkillsEnd; i++)
			replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_remove_skill "+skills[i].getId()+"\">"+skills[i].getName()+"</a></td><td width=60>"+skills[i].getLevel()+"</td><td width=40>"+skills[i].getId()+"</td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<br><center><table>");
		replyMSG.append("要移除的技能 ID：");
		replyMSG.append("<tr><td>Id: </td>");
		replyMSG.append("<td><edit var=\"id_to_remove\" width=110></td></tr>");
		replyMSG.append("</table></center>");

		replyMSG.append("<center><button value=\"移除技能\" action=\"bypass -h admin_remove_skill $id_to_remove\" width=110 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>");
		replyMSG.append("<br><center><button value=\"返回\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>");

		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void showMainPage(L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
			player = (L2PcInstance)target;

		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));

			return;
		}
/*
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);		

		TextBuilder replyMSG = new TextBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"首頁\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=180><center>人物選擇</center></td>");
		replyMSG.append("<td width=40><button value=\"返回\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
        replyMSG.append("<center>玩家名稱 <font color=\"LEVEL\">" + player.getName() + "</font></center>");
        replyMSG.append("<br><table width=270><tr><td>等級: " + player.getLevel() + " " + player.getTemplate().className + "</td></tr></table>");
		replyMSG.append("<br><table width=270><tr><td>注意: 修改玩家資料可能會導致遊戲出錯或者引起更多問題</td></tr>");
		replyMSG.append("<tr><td></td></tr></table>");
		replyMSG.append("<br><center><table>");
		replyMSG.append("<tr><td><button value=\"增加技能\" action=\"bypass -h admin_skill_list\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td><button value=\"取得技能\" action=\"bypass -h admin_get_skills\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");			
		replyMSG.append("<tr><td><button value=\"刪除技能\" action=\"bypass -h admin_remove_skills 0\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td><button value=\"技能重制\" action=\"bypass -h admin_reset_skills\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
        replyMSG.append("<tr><td><button value=\"所有技能\" action=\"bypass -h admin_give_all_skills\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("</table></center>");		
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
*/
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/charskills.htm");
		adminReply.replace("%name%",player.getName());
		adminReply.replace("%level%",String.valueOf(player.getLevel()));
		adminReply.replace("%class%",player.getTemplate().className);

		activeChar.sendPacket(adminReply);
	}

	private void adminGetSkills(L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
			player = (L2PcInstance)target;

		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));

			return;
		}
		if (player.getName().equals(activeChar.getName()))

			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_ON_YOURSELF));

		else
		{
			L2Skill[] skills = player.getAllSkills();
			adminSkills = activeChar.getAllSkills();
			for (int i=0;i<adminSkills.length;i++)
				activeChar.removeSkill(adminSkills[i]);
			for (int i=0;i<skills.length;i++)
				activeChar.addSkill(skills[i], true);

			activeChar.sendMessage("取得 "+player.getName()+" 所有技能。");

			activeChar.sendSkillList(); 
		}
		showMainPage(activeChar);
	}

	private void adminResetSkills(L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
			player = (L2PcInstance)target;
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		if (adminSkills==null)

			activeChar.sendMessage("無法執行。");
		else
		{
			L2Skill[] skills = player.getAllSkills();
			for (int i=0;i<skills.length;i++)
				player.removeSkill(skills[i]);
			for (int i=0;i<activeChar.getAllSkills().length;i++)
				player.addSkill(activeChar.getAllSkills()[i], true);
			for (int i=0;i<skills.length;i++)
				activeChar.removeSkill(skills[i]);
			for (int i=0;i<adminSkills.length;i++)
				activeChar.addSkill(adminSkills[i], true);

			//player.sendMessage("[GM]"+activeChar.getName()+" 重新整理該人物的技能");
			activeChar.sendMessage("技能恢復。");

			adminSkills=null;
			activeChar.sendSkillList();
		}
		showMainPage(activeChar);
	}

	private void adminAddSkill(L2PcInstance activeChar, String val)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
			player = (L2PcInstance)target;
		else
		{
			showMainPage(activeChar);
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		StringTokenizer st = new StringTokenizer(val);
		if (st.countTokens()!=2)
		{
			showMainPage(activeChar);
		}
		else
		{
			L2Skill skill=null;
			try
			{
				String id = st.nextToken();
				String level = st.nextToken();
				int idval = Integer.parseInt(id);
				int levelval = Integer.parseInt(level);
				skill = SkillTable.getInstance().getInfo(idval,levelval);
			}
			catch (Exception e) {}
			if (skill != null)
			{
				String name = skill.getName();
				player.sendMessage("已增加 "+name+" 技能。");
				player.addSkill(skill, true);
				//Admin information
				//activeChar.sendMessage("增加 "+name+" 技能給人物 "+player.getName()+".");
				if (Config.DEBUG)
					_log.fine("[GM]"+activeChar.getName()+" gave skill "+name+" to "+player.getName()+".");
				activeChar.sendSkillList(); 
			}
			else
				activeChar.sendMessage("資料錯誤。");
			showMainPage(activeChar); //Back to start
		}
	}

	private void adminRemoveSkill(L2PcInstance activeChar, int idval)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
			player = (L2PcInstance)target;
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		L2Skill skill = SkillTable.getInstance().getInfo(idval,player.getSkillLevel(idval));
		if (skill != null)
		{

			String skillname = skill.getName();
			player.sendMessage("已移除 "+skillname+" 技能。");
			player.removeSkill(skill);

			//Admin information	

			//activeChar.sendMessage("移除技能 "+skillname+" 從人物 "+player.getName()+".");

			if (Config.DEBUG)
				_log.fine("[GM]"+activeChar.getName()+" removed skill "+skillname+" from "+player.getName()+".");
			activeChar.sendSkillList(); 
		}
		else
			activeChar.sendMessage("資料錯誤。");
		removeSkillsPage(activeChar, 0); //Back to previous page	

	}

	private void adminAddClanSkill(L2PcInstance activeChar, int id, int level)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
			player = (L2PcInstance)target;

		else
		{
			showMainPage(activeChar);
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));

			return;
		}
		if (!player.isClanLeader())
		{

			activeChar.sendPacket(new SystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER).addString(player.getName()));
			showMainPage(activeChar);
			return;

		}
		if ((id < 370)|| (id > 391) || (level<1) || (level>3))
		{

			activeChar.sendMessage("使用方法: //add_clan_skill <skill_id> <level>");
			showMainPage(activeChar);
			return;

		}
		else
		{
			L2Skill skill = SkillTable.getInstance().getInfo(id,level);
			if (skill != null)
			{
				String skillname = skill.getName();
				SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_SKILL_S1_ADDED);
				sm.addSkillName(skill);
				player.sendPacket(sm);
				player.getClan().broadcastToOnlineMembers(sm);
				player.getClan().addNewSkill(skill);
				activeChar.sendMessage("增加血盟技能︰ "+skillname+" 給予血盟 "+player.getClan().getName()+"。");
				
				activeChar.getClan().broadcastToOnlineMembers(new PledgeSkillList(activeChar.getClan()));  
				for(L2PcInstance member: activeChar.getClan().getOnlineMembers(0))  
				{
					member.sendSkillList();  
				}

				showMainPage(activeChar);
				return;
			}
			else
			{
				activeChar.sendMessage("資料錯誤。");
				return;
			}

		}
	}

}
