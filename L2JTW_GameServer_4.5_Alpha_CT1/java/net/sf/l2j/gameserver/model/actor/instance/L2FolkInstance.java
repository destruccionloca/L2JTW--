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
package net.sf.l2j.gameserver.model.actor.instance;

import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.model.L2EnchantSkillLearn;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2SkillLearn;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.AcquireSkillList;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.ExEnchantSkillList;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.ExEnchantSkillList.EnchantSkillType;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.datatables.CharTemplateTable;

public class L2FolkInstance extends L2NpcInstance
{
	private final ClassId[] _classesToTeach;

	public L2FolkInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		_classesToTeach = template.getTeachInfo();
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		player.setLastFolkNPC(this);
		super.onAction(player);
	}

	/**
	 * this displays SkillList to the player.
	 * @param player
	 */
	public void showSkillList(L2PcInstance player, ClassId classId)
	{
		if (Config.DEBUG)
			_log.fine("SkillList activated on: "+getObjectId());

		int npcId = getTemplate().npcId;

		if (_classesToTeach == null)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            TextBuilder sb = new TextBuilder();
            sb.append("<html><body>");
            sb.append("我無法教你任何東西。<br>請告知管理員修正。<br>NpcId："+npcId+"，你的職業："+player.getClassId().getId()+"<br>");
            sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;

		}

		if (!getTemplate().canTeach(classId))
        {
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            TextBuilder sb = new TextBuilder();
            sb.append("<html><body>我無法教你任何東西。<br>你必須尋找你所屬職業的教師。</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;

		}

		L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player, classId);
		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.skillType.Usual);
		int counts = 0;

		for (L2SkillLearn s: skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());

			if (sk == null || !sk.getCanLearn(player.getClassId()) || !sk.canTeachBy(npcId))
				continue;

			int cost = SkillTreeTable.getInstance().getSkillCost(player, sk);
			counts++;

			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
		}

		if (counts == 0)
		{
		    int minlevel = SkillTreeTable.getInstance().getMinLevelForNewSkill(player, classId);
		    if (minlevel > 0)
		    {
		        SystemMessage sm = new SystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN);
		        sm.addNumber(minlevel);
		        player.sendPacket(sm);

		    }

		    else
		    {

		        SystemMessage sm = new SystemMessage(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
		        player.sendPacket(sm);

		    }
		}
		else
		{
            player.sendPacket(asl);
		}


		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	/**
     * this displays EnchantSkillList to the player.
     * @param player
     */
    public void showEnchantSkillList(L2PcInstance player, boolean isSafeEnchant)
    {
        if (Config.DEBUG)
        {
            _log.fine("EnchantSkillList activated on: "+getObjectId());
        }
        
        int npcId = getTemplate().npcId;

        if (_classesToTeach == null)
        {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            TextBuilder sb = new TextBuilder();
            sb.append("<html><body>");
            sb.append("我無法教你任何東西。<br>請告知管理員修正。<br>NpcId："+npcId+"，你的職業："+player.getClassId().getId()+"<br>");
            sb.append("</body></html>");
            html.setHtml(sb.toString());
            player.sendPacket(html);

            return;
        }

        if (!getTemplate().canTeach(player.getClassId()))
        {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            TextBuilder sb = new TextBuilder();
            sb.append("<html><body>我無法教你任何東西。<br>你必須尋找你所屬職業的教師。</body></html>");
            html.setHtml(sb.toString());
            player.sendPacket(html);

            return;
        }
        
        if (player.getClassId().getId() < 88 ||(player.getClassId().getId() >= 123 && player.getClassId().getId() < 132 )||player.getClassId().getId() == 135)
        {
        	NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            TextBuilder sb = new TextBuilder();
            sb.append("<html><body>昇華技能：<br>完成3次轉職的角色才可以昇華技能。</body></html>");
            html.setHtml(sb.toString());
            player.sendPacket(html);

            return;
        }
        int playerLevel = player.getLevel();
        
        if (playerLevel >= 76)
        {
            ExEnchantSkillList esl = new ExEnchantSkillList(isSafeEnchant ? EnchantSkillType.SAFE : EnchantSkillType.NORMAL);
            L2Skill[] charSkills = player.getAllSkills();
            int counts = 0;
            for  (L2Skill skill : charSkills)
            {
                L2EnchantSkillLearn enchantLearn = SkillTreeTable.getInstance().getSkillEnchantmentForSkill(skill);
                if (enchantLearn != null)
                {
                    esl.addSkill(skill.getId(), skill.getLevel());
                    counts++;
                }
            }
            
            if (counts == 0)
            {
                player.sendPacket(new SystemMessage(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
            }
            else
            {
                player.sendPacket(esl);
            }
        }
        else
        {
            player.sendPacket(new SystemMessage(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
        }
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }
    
    /**
     * Show the list of enchanted skills for changing enchantment route
     * 
     * @param player
     * @param classId
     */
    public void showEnchantChangeSkillList(L2PcInstance player)
    {
        if (Config.DEBUG)
        {
            _log.fine("Enchanted Skill List activated on: "+getObjectId());
        }
        
        int npcId = getTemplate().npcId;

        if (_classesToTeach == null)
        {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            TextBuilder sb = new TextBuilder();
            sb.append("<html><body>");
            sb.append("I cannot teach you. My class list is empty.<br> Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:"+npcId+", Your classId:"+player.getClassId().getId()+"<br>");
            sb.append("</body></html>");
            html.setHtml(sb.toString());
            player.sendPacket(html);

            return;
        }

        if (!getTemplate().canTeach(player.getClassId()))
        {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            TextBuilder sb = new TextBuilder();
            sb.append("<html><body>");
            sb.append("I cannot teach you any skills.<br> You must find your current class teachers.");
            sb.append("</body></html>");
            html.setHtml(sb.toString());
            player.sendPacket(html);

            return;
        }
        
        if (player.getClassId().getId() < 88 ||(player.getClassId().getId() >= 123 && player.getClassId().getId() < 132 )||player.getClassId().getId() == 135)
        {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            TextBuilder sb = new TextBuilder();
            sb.append("<html><body>");
            sb.append("You must have 3rd class change quest completed.");
            sb.append("</body></html>");
            html.setHtml(sb.toString());
            player.sendPacket(html);

            return;
        }
        int playerLevel = player.getLevel();
        
        if (playerLevel >= 76)
        {
            ExEnchantSkillList esl = new ExEnchantSkillList(EnchantSkillType.CHANGE_ROUTE);
            L2Skill[] charSkills = player.getAllSkills();
            
            for  (L2Skill skill : charSkills)
            {
                // is enchanted?
                if (skill.getLevel() > 100)
                {
                    esl.addSkill(skill.getId(), skill.getLevel());
                }
            }
            
            player.sendPacket(esl);
        }
        else
        {
            player.sendPacket(new SystemMessage(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
        }
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }
    
    /**
     * Show the list of enchanted skills for untraining
     * 
     * @param player
     * @param classId
     */
    public void showEnchantUntrainSkillList(L2PcInstance player, ClassId classId)
    {
        if (Config.DEBUG)
        {
            _log.fine("Enchanted Skill List activated on: "+getObjectId());
        }
        
        int npcId = getTemplate().npcId;

        if (_classesToTeach == null)
        {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            TextBuilder sb = new TextBuilder();
            sb.append("<html><body>");
            sb.append("I cannot teach you. My class list is empty.<br> Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:"+npcId+", Your classId:"+player.getClassId().getId()+"<br>");
            sb.append("</body></html>");
            html.setHtml(sb.toString());
            player.sendPacket(html);

            return;
        }

        if (!getTemplate().canTeach(classId))
        {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            TextBuilder sb = new TextBuilder();
            sb.append("<html><body>");
            sb.append("I cannot teach you any skills.<br> You must find your current class teachers.");
            sb.append("</body></html>");
            html.setHtml(sb.toString());
            player.sendPacket(html);

            return;
        }
        
        if (player.getClassId().getId() < 88 ||(player.getClassId().getId() >= 123 && player.getClassId().getId() < 132 )||player.getClassId().getId() == 135)
        {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            TextBuilder sb = new TextBuilder();
            sb.append("<html><body>");
            sb.append("You must have 3rd class change quest completed.");
            sb.append("</body></html>");
            html.setHtml(sb.toString());
            player.sendPacket(html);

            return;
        }
        int playerLevel = player.getLevel();
        
        if (playerLevel >= 76)
        {
            ExEnchantSkillList esl = new ExEnchantSkillList(EnchantSkillType.UNTRAIN);
            L2Skill[] charSkills = player.getAllSkills();
            
            for  (L2Skill skill : charSkills)
            {
                // is enchanted?
                if (skill.getLevel() > 100)
                {
                    esl.addSkill(skill.getId(), skill.getLevel());
                }
            }
            
            player.sendPacket(esl);
        }
        else
        {
            player.sendPacket(new SystemMessage(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
        }
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (player.isTransformed())
        	return;
		
		if (command.startsWith("SkillList"))
		{
			if (Config.ALT_GAME_SKILL_LEARN)
			{
				String id = command.substring(9).trim();

				if (id.length() != 0)
                {
					player.setSkillLearningClassId(ClassId.values()[Integer.parseInt(id)]);
					showSkillList(player, ClassId.values()[Integer.parseInt(id)]);
				}
                else
                {
					boolean own_class = false;

					if (_classesToTeach != null)
                    {
						for (ClassId cid : _classesToTeach)
                        {
							if (cid.equalsOrChildOf(player.getClassId()))
                            {
								own_class = true;
								break;
							}
						}
					}


					String text =
						"<html>"+
						"<body>"+
						"<center>技能學習︰</center>"+
						"<br>";



					if (!own_class)
                    {
						String charType = player.getClassId().isMage() ? "fighter" : "mage";
						text +=

								"所屬職業技能是最簡單學習的，<br>\n"+
	                            "相同種族的其他職業技能則會有點困難，<br>\n"+
	                            "其他種族的技能將會更難學習，<br>\n"+
	                            "當然，你也可以學習"+charType+"的技能，但是它們是最難學習的！"+
	                            "<br>"+
	                            "<br>";

					}

					// make a list of classes
					if (_classesToTeach != null)
                    {

						int count = 0;
						ClassId classCheck = player.getClassId();

						while ((count == 0) && (classCheck != null))
						{
						    for (ClassId cid : _classesToTeach)
						    {
						        if (cid.level() != classCheck.level())
						            continue;

						        if (SkillTreeTable.getInstance().getAvailableSkills(player, cid).length == 0)
						            continue;

						        String className = CharTemplateTable.getClassNameById(cid.getId());
						        text += "<a action=\"bypass -h npc_%objectId%_SkillList "+cid.getId()+"\">學習"+cid+"技能</a><br>\n";

						        count++;
						    }
						    classCheck = classCheck.getParent();
						}
						classCheck = null;

                    }
                    else
                    {
                        text += "No Skills.<br>";
                    }

					text +=
						"</body></html>";

					insertObjectIdAndShowChatWindow(player, text);
					player.sendPacket( ActionFailed.STATIC_PACKET );
				}
			}
            else
            {
				player.setSkillLearningClassId(player.getClassId());
				showSkillList(player, player.getClassId());
			}
		}
		else if (command.startsWith("EnchantSkillList"))
        {
            this.showEnchantSkillList(player, false);
        }
        else if (command.startsWith("SafeEnchantSkillList"))
        {
            this.showEnchantSkillList(player, true);
        }
        else if (command.startsWith("ChangeEnchantSkillList"))
        {
            this.showEnchantChangeSkillList(player);
        }
        else if (command.startsWith("UntrainEnchantSkillList"))
        {
            this.showEnchantUntrainSkillList(player, player.getClassId());
        }
		else
		{
			// this class dont know any other commands, let forward
			// the command to the parent class

			super.onBypassFeedback(player, command);
		}
	}
}
