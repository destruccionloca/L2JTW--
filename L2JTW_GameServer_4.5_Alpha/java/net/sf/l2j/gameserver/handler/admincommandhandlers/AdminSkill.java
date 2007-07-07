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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2SkillLearn;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
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
			"admin_remove_all_skills"
			};
	private static final int REQUIRED_LEVEL = Config.GM_CHAR_EDIT;
	private static final int REQUIRED_LEVEL2 = Config.GM_CHAR_EDIT_OTHER;
	
	private static L2Skill[] adminSkills;

	public boolean useAdminCommand(String command, L2PcInstance activeChar) {
        if (!Config.ALT_PRIVILEGES_ADMIN)
            if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
		
		String target = (activeChar.getTarget() != null?activeChar.getTarget().getName():"no-target");
        GMAudit.auditGMAction(activeChar.getName(), command, target, "");

        if (command.equals("admin_show_skills"))
		{
			showSkillsPage(activeChar);
		}
        else if (command.startsWith("admin_remove_skills"))
		{
        	try {
        		String val = command.substring(20);
        		removeSkillsPage(activeChar, Integer.parseInt(val));
        	}
			catch (StringIndexOutOfBoundsException e)
			{ }
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
            catch (StringIndexOutOfBoundsException e)
            { }
		}
		else if (command.startsWith("admin_add_skill"))
		{
			try
			{
				String val = command.substring(15); 
			    if (activeChar == activeChar.getTarget() || activeChar.getAccessLevel() >= REQUIRED_LEVEL2)
				adminAddSkill(activeChar, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{	//Case of empty character name
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("SYS");
				sm.addString("增加技能發生錯誤");
				activeChar.sendPacket(sm);
			}			
		}
		else if (command.startsWith("admin_remove_skill"))
		{
			try
			{
				String id = command.substring(19);
				int idval = Integer.parseInt(id);
			     if (activeChar == activeChar.getTarget() || activeChar.getAccessLevel() >= REQUIRED_LEVEL2)
				adminRemoveSkill(activeChar, idval);
			}
			catch (StringIndexOutOfBoundsException e)
			{	//Case of empty character name
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("SYS");
				sm.addString("移除技能發生錯誤");
				activeChar.sendPacket(sm);
			}			
		}
		else if (command.equals("admin_get_skills"))
		{
			adminGetSkills(activeChar);
		}
		else if (command.equals("admin_reset_skills"))
		{
		     if (activeChar == activeChar.getTarget() || activeChar.getAccessLevel() >= REQUIRED_LEVEL2)
			adminResetSkills(activeChar);
		}
        else if (command.equals("admin_give_all_skills"))
        {
	  if (activeChar == activeChar.getTarget() || activeChar.getAccessLevel() >= REQUIRED_LEVEL2)
            adminGiveAllSkills(activeChar);
        }
		
		else if (command.equals("admin_remove_all_skills"))
		{
	        if (activeChar.getTarget() instanceof L2PcInstance)
	        {
		        L2PcInstance player = (L2PcInstance)activeChar.getTarget();
	        	for (L2Skill skill : player.getAllSkills())
	        		player.removeSkill(skill);
		        activeChar.sendMessage("移除" + player.getName() + "所有技能.");
		        player.sendMessage("管理員將您的技能全部移除");
	        }
		}
		return true;
	}
	
	/**
     * This function will give all the skills that the gm target can have at its level
     * to the traget
     * @param activeChar: the gm char
     */
    private void adminGiveAllSkills(L2PcInstance activeChar)
    {
        L2Object target = activeChar.getTarget();
        L2PcInstance player = null;
        if (target instanceof L2PcInstance) {
            player = (L2PcInstance)target;
        } else {            
            SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
            sm.addString("SYS");
            sm.addString("目標錯誤");
            activeChar.sendPacket(sm);
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
                {
                    skillCounter++;
                }
                player.addSkill(sk, true);
            }
            countUnlearnable = false;
            skills = SkillTreeTable.getInstance().getAvailableSkills(player, player.getClassId());
        }
        //Notify player and admin
        player.sendMessage("管理員增加" + skillCounter + "個技能到技能表內.");
        activeChar.sendMessage("增加" + skillCounter + "個技能給" + player.getName());
    }

    public String[] getAdminCommandList() {
		return ADMIN_COMMANDS;
	}
	
	private boolean checkLevel(int level) {
		return (level >= REQUIRED_LEVEL);
	}
	
	//ok
	private void removeSkillsPage(L2PcInstance activeChar, int page)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance) {
			player = (L2PcInstance)target;
		} else {
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("SYS");
			sm.addString("目標錯誤");
			activeChar.sendPacket(sm);
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
		replyMSG.append("<td width=40><button value=\"首頁\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=180><center人物選擇</center></td>");
		replyMSG.append("<td width=40><button value=\"返回\" action=\"bypass -h admin_show_skills\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>玩家名稱 <font color=\"LEVEL\">" + player.getName() + "</font></center>");
		replyMSG.append("<br><table width=270><tr><td>等級: " + player.getLevel() + " " + player.getTemplate().className + "</td></tr></table>");
		replyMSG.append("<br><table width=270><tr><td>注意: 修改玩家資料可能會導致遊戲出錯或者引起更多問題</td></tr>");
		replyMSG.append("<tr><td></td></tr></table>");
		replyMSG.append("<br><center>請選擇技能移除:</center>");
		replyMSG.append("<br>");
		String pages = "<center><table width=270><tr>";
		for (int x=0; x<MaxPages; x++)
		{
			int pagenr = x + 1;
			pages += "<td><a action=\"bypass -h admin_remove_skills " + x + "\">頁 " + pagenr + "</a></td>";
		}
		pages += "</tr></table></center>";
		replyMSG.append(pages);
		replyMSG.append("<br><table width=270>");		
		replyMSG.append("<tr><td width=80>名稱:</td><td width=60>等級:</td><td width=40>編號:</td></tr>");
		
		for (int i = SkillsStart; i < SkillsEnd; i++)
		{
			replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_remove_skill "+skills[i].getId()+"\">"+skills[i].getName()+"</a></td><td width=60>"+skills[i].getLevel()+"</td><td width=40>"+skills[i].getId()+"</td></tr>");
		}
		replyMSG.append("</table>");
		replyMSG.append("<br><center><table>");
		replyMSG.append("移除特殊技能:");
		replyMSG.append("<tr><td>Id: </td>");
		replyMSG.append("<td><edit var=\"id_to_remove\" width=110></td></tr>");
		replyMSG.append("</table></center>");		
		replyMSG.append("<center><button value=\"移除技能\" action=\"bypass -h admin_remove_skill $id_to_remove\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("<br><center><button value=\"返回\" action=\"bypass -h admin_current_player\" width=40 height=15></center>");
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	//ok
	private void showSkillsPage(L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance) {
			player = (L2PcInstance)target;
		} else {
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("SYS");
			sm.addString("錯誤目標");
			activeChar.sendPacket(sm);
			return;
		}
		
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
		activeChar.sendPacket(adminReply);
	}
	
	private void adminGetSkills(L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance) {
			player = (L2PcInstance)target;
		} else {			
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("SYS");
			sm.addString("錯誤目標");
			activeChar.sendPacket(sm);
			return;
		}

		if (player.getName().equals(activeChar.getName()))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("SYS");
			sm.addString("錯誤目標");
			player.sendPacket(sm);
		}
		else
		{
			L2Skill[] skills = player.getAllSkills();
			adminSkills = activeChar.getAllSkills();
			for (int i=0;i<adminSkills.length;i++)
			{
				activeChar.removeSkill(adminSkills[i]);
			}
			for (int i=0;i<skills.length;i++)
			{
				activeChar.addSkill(skills[i], true);
			}
			SystemMessage smA = new SystemMessage(SystemMessageId.S1_S2);
			smA.addString("SYS");
			smA.addString("取得 "+player.getName()+" 所有技能.");
			activeChar.sendPacket(smA);
		}
		showSkillsPage(activeChar);
	}
	
	private void adminResetSkills(L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance) {
			player = (L2PcInstance)target;
		} else {
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("SYS");
			sm.addString("目標錯誤");
			activeChar.sendPacket(sm);
			return;
		}

		if (adminSkills==null)
		{
			SystemMessage smA = new SystemMessage(SystemMessageId.S1_S2);
			smA.addString("SYS");
			smA.addString("無法執行");
			activeChar.sendPacket(smA);
		}
		else
		{
			L2Skill[] skills = player.getAllSkills();
			for (int i=0;i<skills.length;i++)
			{
				player.removeSkill(skills[i]);
			}
			for (int i=0;i<activeChar.getAllSkills().length;i++)
			{
				player.addSkill(activeChar.getAllSkills()[i], true);
			}
			for (int i=0;i<skills.length;i++)
			{
				activeChar.removeSkill(skills[i]);
			}
			for (int i=0;i<adminSkills.length;i++)
			{
				activeChar.addSkill(adminSkills[i], true);
			}
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("[GM]");
			sm.addString("重新整理該人物的技能");
			player.sendPacket(sm);
			SystemMessage smA = new SystemMessage(SystemMessageId.S1_S2);
			smA.addString("SYS");
			smA.addString("技能恢復");
			activeChar.sendPacket(smA);
			adminSkills=null;
		}
		showSkillsPage(activeChar);
	}
	
	private void adminAddSkill(L2PcInstance activeChar, String val)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance) {
			player = (L2PcInstance)target;
		} else {
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("SYS");
			sm.addString("錯誤目標");
			activeChar.sendPacket(sm);
			return;
		}

		StringTokenizer st = new StringTokenizer(val);
		if (st.countTokens()!=2)
		{
			showSkillsPage(activeChar);
		}
		else
		{
		String id = st.nextToken();
		String level = st.nextToken();		
		int idval = Integer.parseInt(id);
		int levelval = Integer.parseInt(level);
		
		L2Skill skill = SkillTable.getInstance().getInfo(idval,levelval);		
		
		if (skill != null)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("SYS");
			sm.addString("管理員增加"+skill.getName()+"到該人物的技能表.");
			player.sendPacket(sm);
			
			player.addSkill(skill, true);
			
			//Admin information	
			SystemMessage smA = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("SYS");
			smA.addString("增加"+skill.getName()+"給"+player.getName()+".");
			
			activeChar.sendPacket(smA);
			if (Config.DEBUG)
				_log.fine("[GM]"+activeChar.getName()+"gave the skill "+skill.getName()+
						" to "+player.getName()+".");
		}
		else
		{
			SystemMessage smA = new SystemMessage(SystemMessageId.S1_S2);
			smA.addString("SYS");
			smA.addString("錯誤資料");
		}		
		showSkillsPage(activeChar); //Back to start
		}
	}
	
	private void adminRemoveSkill(L2PcInstance activeChar, int idval)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance) {
			player = (L2PcInstance)target;
		} else {
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("SYS");
			sm.addString("錯誤目標");
			activeChar.sendPacket(sm);
			return;
		}
				
		L2Skill skill = SkillTable.getInstance().getInfo(idval,player.getSkillLevel(idval));
				
		if (skill != null)
		{
		SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
		sm.addString("SYS");
		sm.addString("管理員移除"+skill.getName()+"此技能.");
		player.sendPacket(sm);
				
		player.removeSkill(skill);
		
		//Admin information	
		SystemMessage smA = new SystemMessage(SystemMessageId.S1_S2);
		smA.addString("SYS");
		smA.addString("移除"+skill.getName()+"從"+player.getName()+".");
		
		activeChar.sendPacket(smA);
		if (Config.DEBUG)
			_log.fine("[GM]"+activeChar.getName()+"removed the skill "+skill.getName()+
					" from "+player.getName()+".");
		}
		else
		{
			SystemMessage smA = new SystemMessage(SystemMessageId.S1_S2);
			smA.addString("SYS");
			smA.addString("錯誤資料");
		}
		removeSkillsPage(activeChar, 0); //Back to start	
	}

	public void showSkill(L2PcInstance activeChar, String val)
	{		
		int skillid = Integer.parseInt(val);
		L2Skill skill = SkillTable.getInstance().getInfo(skillid, 1);

		if (skill != null)
		{
			if (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF)
			{
				activeChar.setTarget(activeChar);
		
				MagicSkillUser msk = new MagicSkillUser(activeChar, skillid, 1, skill.getSkillTime() , skill.getReuseDelay());
				activeChar.broadcastPacket(msk);
				if (Config.DEBUG) _log.fine("showing self skill, id: "+skill.getId()+" named: "+skill.getName());
			}
			else if (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_ONE)
			{
				if (Config.DEBUG) _log.fine("showing ATTACK skill, id: "+skill.getId()+" named: "+skill.getName());				
			}
		}
		else
		{
			if (Config.DEBUG) _log.fine("no such skill id: "+skillid);
			ActionFailed af = new ActionFailed();
			activeChar.broadcastPacket(af);
		}
	}
}
