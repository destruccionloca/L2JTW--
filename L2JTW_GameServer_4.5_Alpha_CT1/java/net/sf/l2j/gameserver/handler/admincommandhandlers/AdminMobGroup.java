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

import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.MobGroup;
import net.sf.l2j.gameserver.model.MobGroupTable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.Broadcast;

/**
 * @author littlecrow
 * Admin commands handler for controllable mobs
 */
public class AdminMobGroup implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = { "admin_mobmenu", "admin_mobgroup_list",
		"admin_mobgroup_create", "admin_mobgroup_remove", "admin_mobgroup_delete",
		"admin_mobgroup_spawn", "admin_mobgroup_unspawn", "admin_mobgroup_kill",
		"admin_mobgroup_idle", "admin_mobgroup_attack", "admin_mobgroup_rnd",
		"admin_mobgroup_return", "admin_mobgroup_follow", "admin_mobgroup_casting",
		"admin_mobgroup_nomove" , "admin_mobgroup_attackgrp", "admin_mobgroup_invul"};

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_mobmenu"))
		{
			showMainPage(activeChar,command);
			return true;
		}
		else if (command.equals("admin_mobgroup_list"))
			showGroupList(activeChar);
		else if (command.startsWith("admin_mobgroup_create"))
			createGroup(command, activeChar);
		else if (command.startsWith("admin_mobgroup_delete") ||
				command.startsWith("admin_mobgroup_remove"))
			removeGroup(command, activeChar);
		else if (command.startsWith("admin_mobgroup_spawn"))
			spawnGroup(command, activeChar);
		else if (command.startsWith("admin_mobgroup_unspawn"))
			unspawnGroup(command, activeChar);
		else if (command.startsWith("admin_mobgroup_kill"))
			killGroup(command, activeChar);
		else if (command.startsWith("admin_mobgroup_attackgrp"))
			attackGrp(command, activeChar);
		else if (command.startsWith("admin_mobgroup_attack"))
		{
			if (activeChar.getTarget() instanceof L2Character)
			{
				L2Character target = (L2Character) activeChar.getTarget();
				attack(command, activeChar, target);
			}
		}
		else if (command.startsWith("admin_mobgroup_rnd"))
			setNormal(command, activeChar);
		else if (command.startsWith("admin_mobgroup_idle"))
			idle(command, activeChar);
		else if (command.startsWith("admin_mobgroup_return"))
			returnToChar(command, activeChar);
		else if (command.startsWith("admin_mobgroup_follow"))
			follow(command, activeChar, activeChar);
		else if (command.startsWith("admin_mobgroup_casting"))
			setCasting(command, activeChar);
		else if (command.startsWith("admin_mobgroup_nomove"))
			noMove(command, activeChar);
		else if (command.startsWith("admin_mobgroup_invul"))
			invul(command, activeChar);
		else if (command.startsWith("admin_mobgroup_teleport"))
			teleportGroup(command, activeChar);
		showMainPage(activeChar,command);
		return true;
	}

	/**
	 * @param activeChar
	 */
	private void showMainPage(L2PcInstance activeChar, String command)
	{
		String filename = "mobgroup.htm";
		AdminHelpPage.showHelpPage(activeChar, filename);
	}

	private void returnToChar(String command, L2PcInstance activeChar)
	{
		int groupId;
		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (Exception e)
		{
			activeChar.sendMessage("Incorrect command arguments.");
			return;
		}
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null)
		{
			activeChar.sendMessage("Invalid group specified.");
			return;
		}
		group.returnGroup(activeChar);
	}

	private void idle(String command, L2PcInstance activeChar)
	{
		int groupId;

		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
		} 

        catch (Exception e) 
        {
            activeChar.sendMessage("指令錯誤。");

			return;
		}
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null)
		{
			activeChar.sendMessage("Invalid group specified.");
			return;
		}
		group.setIdleMode();
	}

	private void setNormal(String command, L2PcInstance activeChar)
	{
		int groupId;
		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
		}

        catch (Exception e) {
            activeChar.sendMessage("指令錯誤。");

			return;
		}
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null)
		{
			activeChar.sendMessage("Invalid group specified.");
			return;
		}
		group.setAttackRandom();
	}

	private void attack(String command, L2PcInstance activeChar, L2Character target)
	{
		int groupId;
		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
		} 

        catch (Exception e) {
            activeChar.sendMessage("指令錯誤。");


			return;
		}
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null)
		{
			activeChar.sendMessage("指令錯誤。");
			return;
		}
		group.setAttackTarget(target);
	}

	private void follow(String command, L2PcInstance activeChar, L2Character target)
	{
		int groupId;
		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
		} 

        catch (Exception e) {
            activeChar.sendMessage("指令錯誤。");


			return;
		}
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null)
		{
			activeChar.sendMessage("指令錯誤。");
			return;
		}
		group.setFollowMode(target);
	}

	private void createGroup(String command, L2PcInstance activeChar)
	{
		int groupId;
		int templateId;
		int mobCount;

		try {
			String[] cmdParams = command.split(" ");

			groupId = Integer.parseInt(cmdParams[1]);
			templateId = Integer.parseInt(cmdParams[2]);
			mobCount = Integer.parseInt(cmdParams[3]);
		}

		catch (Exception e) {
			activeChar.sendMessage("使用方法: //mobgroup_create <group> <npcid> <count>");

			return;
		}

		if (MobGroupTable.getInstance().getGroup(groupId) != null)
		{
			activeChar.sendMessage("群組編號 " + groupId + " 已存在。");
			return;
		}

		L2NpcTemplate template = NpcTable.getInstance().getTemplate(templateId);

		if (template == null)
		{
			activeChar.sendMessage("NPC 編號錯誤。");
			return;
		}

		MobGroup group = new MobGroup(groupId, template, mobCount);
		MobGroupTable.getInstance().addGroup(groupId, group);

		activeChar.sendMessage("群組 " + groupId + " 建立成功。");
	}


	private void removeGroup(String command, L2PcInstance activeChar)
	{
		int groupId;


		try {
			groupId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (Exception e) {
			activeChar.sendMessage("使用方法: //mobgroup_remove <groupId>");
			return;
		}

		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);

		if (group == null)
		{
			activeChar.sendMessage("指令錯誤。");
			return;
		}

		doAnimation(activeChar);
		group.unspawnGroup();

		if (MobGroupTable.getInstance().removeGroup(groupId))
			activeChar.sendMessage("群組 " + groupId + " 移除。");
	}

	private void spawnGroup(String command, L2PcInstance activeChar)
	{
		int groupId;
		boolean topos = false;
		int posx = 0;
		int posy = 0;
		int posz = 0;

		try {
			String[] cmdParams = command.split(" ");
			groupId = Integer.parseInt(cmdParams[1]);

			try { // we try to get a position
				posx = Integer.parseInt(cmdParams[2]);
				posy = Integer.parseInt(cmdParams[3]);
				posz = Integer.parseInt(cmdParams[4]);
				topos = true;
			}
			catch (Exception e) {
				// no position given
			}
		}
		catch (Exception e) {
			activeChar.sendMessage("使用方法: //mobgroup_spawn <group> [ x y z ]");
			return;
		}

		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);

		if (group == null)
		{
			activeChar.sendMessage("Invalid group specified.");
			return;
		}

		doAnimation(activeChar);

		if (topos)
			group.spawnGroup(posx, posy, posz);
		else
			group.spawnGroup(activeChar);

		activeChar.sendMessage("Mob group " + groupId + " spawned.");
	}

	private void unspawnGroup(String command, L2PcInstance activeChar)
	{
		int groupId;

		try {
			groupId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (Exception e) {
			activeChar.sendMessage("使用方法: //mobgroup_unspawn <groupId>");
			return;
		}

		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);

		if (group == null)
		{
			activeChar.sendMessage("指令錯誤。");
			return;
		}

		doAnimation(activeChar);
		group.unspawnGroup();

		activeChar.sendMessage("群組 " + groupId + " 移除。");
	}

	private void killGroup(String command, L2PcInstance activeChar)
	{
		int groupId;

		try {
			groupId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (Exception e) {
			activeChar.sendMessage("使用方法: //mobgroup_kill <groupId>");
			return;
		}

		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);

		if (group == null)
		{
			activeChar.sendMessage("指令錯誤。");
			return;
		}

		doAnimation(activeChar);
		group.killGroup(activeChar);
	}

	private void setCasting(String command, L2PcInstance activeChar)
	{
		int groupId;

		try {
			groupId = Integer.parseInt(command.split(" ")[1]);
		}

		catch (Exception e) {
			activeChar.sendMessage("使用方法: //mobgroup_casting <groupId>");
			return;
		}

		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);

		if (group == null)
		{
			activeChar.sendMessage("指令錯誤。");
			return;
		}

		group.setCastMode();
	}

	private void noMove(String command, L2PcInstance activeChar)
	{
		int groupId;
		String enabled;

		try {
			groupId = Integer.parseInt(command.split(" ")[1]);
			enabled = command.split(" ")[2];
		}

		catch (Exception e) {
			activeChar.sendMessage("使用方法: //mobgroup_nomove <groupId> <on|off>");

			return;
		}

		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);

		if (group == null)
		{
			activeChar.sendMessage("指令錯誤。");
			return;
		}

		if (enabled.equalsIgnoreCase("on") || enabled.equalsIgnoreCase("true"))
			group.setNoMoveMode(true);
		else if (enabled.equalsIgnoreCase("off") || enabled.equalsIgnoreCase("false"))
			group.setNoMoveMode(false);
		else

			activeChar.sendMessage("指令錯誤。");

	}

	private void doAnimation(L2PcInstance activeChar)
	{
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, 1008, 1, 4000, 0), 2250000/*1500*/);
		activeChar.sendPacket(new SetupGauge(0, 4000));
	}

	private void attackGrp(String command, L2PcInstance activeChar)
	{
		int groupId;
		int othGroupId;

		try {
			groupId = Integer.parseInt(command.split(" ")[1]);
			othGroupId = Integer.parseInt(command.split(" ")[2]);
		}

		catch (Exception e) {

			activeChar.sendMessage("使用方法: //mobgroup_attackgrp <groupId> <TargetGroupId>");


			return;
		}

		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);

		if (group == null)
        {
            activeChar.sendMessage("群組錯誤。");

			return;
		}

		MobGroup othGroup = MobGroupTable.getInstance().getGroup(othGroupId);

		if (othGroup == null)
        {
            activeChar.sendMessage("群組錯誤。");
			return;
		}

		group.setAttackGroup(othGroup);
	}

	private void invul(String command, L2PcInstance activeChar)
	{
		int groupId;
		String enabled;

		try {
			groupId = Integer.parseInt(command.split(" ")[1]);
			enabled = command.split(" ")[2];
		}
		catch (Exception e) {
			activeChar.sendMessage("使用方法: //mobgroup_invul <groupId> <on|off>");
			return;
		}

		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);

		if (group == null)
        {
            activeChar.sendMessage("群組錯誤。");
			return;
		}

		if (enabled.equalsIgnoreCase("on") || enabled.equalsIgnoreCase("true"))
			group.setInvul(true);
		else if (enabled.equalsIgnoreCase("off") || enabled.equalsIgnoreCase("false"))
			group.setInvul(false);
		else
            activeChar.sendMessage("指令錯誤。");
	}

	private void teleportGroup(String command, L2PcInstance activeChar)
	{
		int groupId;
		String targetPlayerStr = null;
		L2PcInstance targetPlayer = null;


		try {
			groupId = Integer.parseInt(command.split(" ")[1]);
			targetPlayerStr = command.split(" ")[2];

			if (targetPlayerStr != null)
				targetPlayer = L2World.getInstance().getPlayer(targetPlayerStr);

			if (targetPlayer == null)
				targetPlayer = activeChar;
		}
		catch (Exception e) {
			activeChar.sendMessage("使用方法: //mobgroup_teleport <groupId> [playerName]");
			return;
		}

		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);

		if (group == null)
		{
			activeChar.sendMessage("指令錯誤。");
			return;
		}

		group.teleportGroup(activeChar);
	}

	private void showGroupList(L2PcInstance activeChar)
	{
		MobGroup[] mobGroupList = MobGroupTable.getInstance().getGroups();

		activeChar.sendMessage("======= <怪物群組> =======");

		for (MobGroup mobGroup : mobGroupList)
			activeChar.sendMessage(mobGroup.getGroupId() + ": " + mobGroup.getActiveMobCount() + " 生存，最大值為 " +  mobGroup.getMaxMobCount() + 
					"。編號為 " + mobGroup.getTemplate().npcId + " (" + mobGroup.getStatus() + ")");

		activeChar.sendPacket(new SystemMessage(SystemMessageId.FRIEND_LIST_FOOTER));

	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}