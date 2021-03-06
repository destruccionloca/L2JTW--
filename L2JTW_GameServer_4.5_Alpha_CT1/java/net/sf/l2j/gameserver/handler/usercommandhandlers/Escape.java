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
package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Broadcast;

/**
 *
 *
 */
public class Escape implements IUserCommandHandler
{
    private static final int[] COMMAND_IDS = { 52 };

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#useUserCommand(int, net.sf.l2j.gameserver.model.L2PcInstance)
     */
    public boolean useUserCommand(@SuppressWarnings("unused") int id, L2PcInstance activeChar)
    {
    	// Thanks nbd
    	if (!TvTEvent.onEscapeUse(activeChar.getObjectId()))
    	{
    		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
    		return false;
    	}

        if (activeChar.isCastingNow() || activeChar.isMovementDisabled() || activeChar.isMuted() || activeChar.isAlikeDead() ||
                activeChar.isInOlympiadMode())
            return false;

        int unstuckTimer = (activeChar.getAccessLevel().isGm()? 5000 : Config.UNSTUCK_INTERVAL*1000 );


        // 檢查玩家如果在黑暗祭典內
        if (activeChar.isFestivalParticipant())
        {
            //現在時刻無法嘗試「/逃脫」指令。請申請訴求。
    		activeChar.sendPacket(new SystemMessage(1043));
            return false;
        }

        // 檢查玩家如果在監獄
        if (activeChar.isInJail())
        {
            //現在時刻無法嘗試「/逃脫」指令。請申請訴求。
    		activeChar.sendPacket(new SystemMessage(1043));
            return false;
        }

/**  
        if (GrandBossManager.getInstance().getZone(activeChar) != null && !activeChar.isGM())
        {
            activeChar.sendMessage("You may not use an escape command in a Boss Zone.");
            return false;
        }
**/
        //不能確認是否處於無法移動的地形。5分鐘之後將逃脫到村莊。
		//activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_STUCK));

        if(activeChar.getAccessLevel().isGm())
        {
        	activeChar.sendMessage("使用快速脫逃: 估計5秒.");
        }
        else if(Config.UNSTUCK_INTERVAL > 100)
        {
        	activeChar.sendMessage("使用脫逃: 估計 " + unstuckTimer/60000 + " 分鐘.");
        }
        else activeChar.sendMessage("使用脫逃: 估計 " + unstuckTimer/1000 + " 秒.");

        activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        //SoE Animation section
        activeChar.setTarget(activeChar);
        activeChar.disableAllSkills();

        MagicSkillUse msk = new MagicSkillUse(activeChar, 1050, 1, unstuckTimer, 0);
        Broadcast.toSelfAndKnownPlayersInRadius(activeChar, msk, 810000/*900*/);
        SetupGauge sg = new SetupGauge(0, unstuckTimer);
        activeChar.sendPacket(sg);
        //End SoE Animation section

        EscapeFinalizer ef = new EscapeFinalizer(activeChar);
        // continue execution later
        activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, unstuckTimer));
        activeChar.setSkillCastEndTime(10+GameTimeController.getGameTicks()+unstuckTimer/GameTimeController.MILLIS_IN_TICK);

        return true;
    }

    static class EscapeFinalizer implements Runnable
    {
        private L2PcInstance _activeChar;

        EscapeFinalizer(L2PcInstance activeChar)
        {
            _activeChar = activeChar;
        }

        public void run()
        {
            if (_activeChar.isDead())
                return;

            _activeChar.setIsIn7sDungeon(false);

            _activeChar.enableAllSkills();

            try
            {
                _activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
            } catch (Throwable e) { if (Config.DEBUG) e.printStackTrace(); }
        }
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#getUserCommandList()
     */
    public int[] getUserCommandList()
    {
        return COMMAND_IDS;
    }
}
