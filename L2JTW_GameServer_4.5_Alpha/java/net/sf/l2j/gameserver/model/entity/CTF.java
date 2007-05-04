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

/**
 * 
 * @author FBIagent
 * 
 */

package net.sf.l2j.gameserver.model.entity;

import java.lang.Boolean;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Future;

import javolution.text.TextBuilder;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class CTF
{   
    public static String _eventName = new String(),
                         _eventDesc = new String(),
                         _topTeam = new String(),
                         _joiningLocationName = new String();
    public static Vector<String> _teams = new Vector<String>(),
                                 _savePlayers = new Vector<String>(),
                                 _savePlayerTeams = new Vector<String>();
    public static Vector<L2PcInstance> _players = new Vector<L2PcInstance>(),
                                       _playersShuffle = new Vector<L2PcInstance>();
    public static Vector<Integer> _teamPlayersCount = new Vector<Integer>(),
                                  _teamPointsCount = new Vector<Integer>(),
                                  _teamColors = new Vector<Integer>(),
                                  _flagIds = new Vector<Integer>(),
                				  _flagsX = new Vector<Integer>(),
                				  _flagsY = new Vector<Integer>(),
                				  _flagsZ = new Vector<Integer>();
    public static Vector<L2Spawn> _flagSpawns = new Vector<L2Spawn>();
    public static Vector<Boolean> _flagsTaken = new Vector<Boolean>();
    public static boolean _joining = false,
                          _teleport = false,
                          _started = false,
                          _sitForced = false;
    public static L2Spawn _npcSpawn;
    public static int _npcId = 0,
                      _npcX = 0,
                      _npcY = 0,
                      _npcZ = 0,
                      _npcHeading = 0,
                      _rewardId = 0,
                      _rewardAmount = 0,
                      _topScore = 0;

    public static void setNpcPos(L2PcInstance activeChar)
    {
        _npcX = activeChar.getX();
        _npcY = activeChar.getY();
        _npcZ = activeChar.getZ();
        _npcHeading = activeChar.getHeading();
    }
    
    public static void addTeam(String teamName)
    {
        if (!checkTeamOk())
        {
            System.out.println("CTF Engine[addTeam(" + teamName + ")]: checkTeamOk() == false");
            return;
        }
        
        if (teamName.equals(" "))
            return;

        _teams.add(teamName);
        _teamPlayersCount.add(0);
        _teamPointsCount.add(0);
        _teamColors.add(0);
        _flagIds.add(0);
        _flagsX.add(0);
        _flagsY.add(0);
        _flagsZ.add(0);
        _flagSpawns.add(null);
        _flagsTaken.add(false);
    }
    
    public static void removeTeam(String teamName)
    {
        if (!checkTeamOk() || _teams.isEmpty())
        {
            System.out.println("CTF Engine[removeTeam(" + teamName + ")]: checkTeamOk() == false");
            return;
        }
        
        if (teamPlayersCount(teamName) > 0)
        {
            System.out.println("CTF Engine[removeTeam(" + teamName + ")]: teamPlayersCount(teamName) > 0");
            return;
        }
        
        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return;

        _flagsTaken.remove(index);
        _flagSpawns.remove(index);
        _flagsZ.remove(index);
        _flagsY.remove(index);
        _flagsX.remove(index);
        _flagIds.remove(index);
        _teamColors.remove(index);
        _teamPointsCount.remove(index);
        _teamPlayersCount.remove(index);
        _teams.remove(index);
    }
    
    public static void setTeamFlag(String teamName, int npcId)
    {
        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return;
        
        _flagIds.set(index, npcId);
    }
    
    public static void setTeamPos(String teamName, L2PcInstance activeChar)
    {
        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return;
        
        _flagsX.set(index, activeChar.getX());
        _flagsY.set(index, activeChar.getY());
        _flagsZ.set(index, activeChar.getZ());
    }
    
    public static void setTeamColor(String teamName, int color)
    {
        if (!checkTeamOk())
            return;

        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return;

        _teamColors.set(index, color);
    }
    
    public static boolean checkTeamOk()
    {
        if (_started || _teleport || _joining)
            return false;
        
        return true;
    }
    
    public static void startJoin()
    {
        if (!startJoinOk())
        {
            System.out.println("CTF Engine[startJoin()]: startJoinOk() == false");
            return;
        }
        
        _joining = true;
        spawnEventNpc();
        Announcements.getInstance().announceToAll(_eventName + "(CTF): 可在" + _joiningLocationName + "參加!");
    }
    
    private static boolean startJoinOk()
    {
        if (_started || _teleport || _joining || _teams.size() < 2 || _eventName.equals("") ||
            _joiningLocationName.equals("") || _eventDesc.equals("") || _npcId == 0 ||
            _npcX == 0 || _npcY == 0 || _npcZ == 0 || _rewardId == 0 || _rewardAmount == 0 ||
            _flagIds.contains(0) || _flagsX.contains(0) || _flagsY.contains(0) || _flagsZ.contains(0))
            return false;
        
        return true;
    }
    
    private static void spawnEventNpc()
    {
        L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(_npcId);

        try
        {
            _npcSpawn = new L2Spawn(tmpl);

            _npcSpawn.setLocx(_npcX);
            _npcSpawn.setLocy(_npcY);
            _npcSpawn.setLocz(_npcZ);
            _npcSpawn.setAmount(1);
            _npcSpawn.setHeading(_npcHeading);
            _npcSpawn.setRespawnDelay(1);

            SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);

            _npcSpawn.init();
            _npcSpawn.getLastSpawn().setCurrentHp(999999999);
            _npcSpawn.getLastSpawn().setTitle(_eventName);
            _npcSpawn.getLastSpawn()._isEventMobCTFJoiner = true;
            _npcSpawn.getLastSpawn().decayMe();
            _npcSpawn.getLastSpawn().spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());

            _npcSpawn.getLastSpawn().broadcastPacket(new MagicSkillUser(_npcSpawn.getLastSpawn(), _npcSpawn.getLastSpawn(), 1034, 1, 1, 1));
        }
        catch (Exception e)
        {
            System.out.println("CTF Engine[spawnEventNpc()]: exception: " + e);
        }
    }
    
    public static void teleportStart()
    {
        if (!startTeleportOk())
        {
        	System.out.println("CTF Engine[teleportStart()]: startTeleportOk() == false");
            return;
        }
        
        _joining = false;
        Announcements.getInstance().announceToAll(_eventName + "(CTF):20秒內傳送至各隊伍旗子!");

        if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
            shuffleTeams();
        
        setUserData();
        ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
                                                       {
                                                           public void run()
                                                           {
                                                               CTF.sit();
                                                               
                                                               for (L2PcInstance player : CTF._players)
                                                               {
                                                            	   if (player != null)
                                                            		   player.teleToLocation(_flagsX.get(_teams.indexOf(player._teamNameCTF)), _flagsY.get(_teams.indexOf(player._teamNameCTF)), _flagsZ.get(_teams.indexOf(player._teamNameCTF)));
                                                               }
                                                           }
                                                       }, 20000);
        
        spawnAllFlags();
        _teleport = true;
    }
    
    private static boolean startTeleportOk()
    {
    	if (!_joining)
            return false;

        if (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE"))
        {
            if (_teamPlayersCount.contains(0))
                return false;
        }
        else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
        {
            Vector<L2PcInstance> playersShuffleTemp = new Vector<L2PcInstance>();
            int loopCount = _playersShuffle.size();

            for (int i=0;i<loopCount;i++)
            {
                if (_playersShuffle.get(i) != null)
                    playersShuffleTemp.add(_playersShuffle.get(i));
            }
            
            _playersShuffle.clear();
            _playersShuffle = playersShuffleTemp; 
            playersShuffleTemp.clear();

            if (_playersShuffle.size() < _teams.size())
                return false;
        }
        
        return true;
    }
    
    public static void shuffleTeams()
    {
        int teamCount = 0,
            playersCount = 0;

        for (;;)
        {
            if (_playersShuffle.isEmpty())
                break;

            int playerToAddIndex = new Random().nextInt(_playersShuffle.size());
            
            _players.add(_playersShuffle.get(playerToAddIndex));
            _players.get(playersCount)._inEventCTF = true;
            _players.get(playersCount)._teamNameCTF = _teams.get(teamCount);
            _savePlayers.add(_players.get(playersCount).getName());
            _savePlayerTeams.add(_teams.get(teamCount));
            playersCount++;

            if (teamCount == _teams.size()-1)
                teamCount = 0;
            else
                teamCount++;
            
            _playersShuffle.remove(playerToAddIndex);
        }
    }
    
    public static void setUserData()
    {
        for (L2PcInstance player : _players)
        {
            player.setNameColor(_teamColors.get(_teams.indexOf(player._teamNameCTF)));
            player.setKarma(0);
            player.broadcastUserInfo();
        }
    }
    
    private static void spawnAllFlags()
    {
    	for (String team : _teams)
    	{
    		int index = _teams.indexOf(team);
    		L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(_flagIds.get(index));

    		try
    		{
    			_flagSpawns.set(index, new L2Spawn(tmpl));

    			_flagSpawns.get(index).setLocx(_flagsX.get(index));
    			_flagSpawns.get(index).setLocy(_flagsY.get(index));
    			_flagSpawns.get(index).setLocz(_flagsZ.get(index));
    			_flagSpawns.get(index).setAmount(1);
    			_flagSpawns.get(index).setHeading(0);
    			_flagSpawns.get(index).setRespawnDelay(1);

    			SpawnTable.getInstance().addNewSpawn(_flagSpawns.get(index), false);

    			_flagSpawns.get(index).init();
    			_flagSpawns.get(index).getLastSpawn().setCurrentHp(999999999);
    			_flagSpawns.get(index).getLastSpawn().setTitle(team);
    			_flagSpawns.get(index).getLastSpawn().decayMe();
    			_flagSpawns.get(index).getLastSpawn().spawnMe(_flagSpawns.get(index).getLastSpawn().getX(), _flagSpawns.get(index).getLastSpawn().getY(), _flagSpawns.get(index).getLastSpawn().getZ());
    		}
    		catch(Exception e)
    		{}
    	}
    }
    
    public static void startEvent()
    {
    	if (!_teleport)
        {
            System.out.println("CTF Engine[startEvent()]: start conditions wrong");
            return;
        }
        
        _teleport = false;
        sit();
        
        for (L2PcInstance player : _players)
        {
        	if (player != null)
        		player._posCheckerCTF = ThreadPoolManager.getInstance().scheduleGeneral(new posChecker(player), 0);
        }

        Announcements.getInstance().announceToAll(_eventName + "(CTF): 競賽開始!");
        _started = true;
    }
    
    public static void finishEvent(L2PcInstance activeChar)
    {
        if (!finishEventOk())
        {
            System.out.println("CTF Engine[finishEvent(" + activeChar.getName() + ")]: finishEventOk() == false");
            return;
        }

        _started = false;
        unspawnEventNpc();
        unspawnAllFlags();
        processTopTeam();

        if (_topScore == 0)
            Announcements.getInstance().announceToAll(_eventName + "(CTF): 宣佈平手!");
        else
        {
            Announcements.getInstance().announceToAll(_eventName + "(CTF): " + _topTeam + " 獲勝! 總分" + _topScore + "分.");
            rewardTeam(activeChar, _topTeam);
        }
        
        teleportFinish();
    }
    
    private static boolean finishEventOk()
    {
        if (!_started)
            return false;
        
        return true;
    }
    
    public static void processTopTeam()
    {
        for (String team : _teams)
        {
            if (teamPointsCount(team) > _topScore)
            {
                _topTeam = team;
                _topScore = teamPointsCount(team);
            }
        }
    }
    
    public static void rewardTeam(L2PcInstance activeChar, String teamName)
    {
        for (L2PcInstance player : _players)
        {
            if (player != null)
            {
                if (player._teamNameCTF.equals(teamName))
                {
                    PcInventory inv = player.getInventory();
                
                    if (ItemTable.getInstance().createDummyItem(_rewardId).isStackable())
                        inv.addItem("CTF Event: " + _eventName, _rewardId, _rewardAmount, player, activeChar.getTarget());
                    else
                    {
                        for (int i=0;i<=_rewardAmount-1;i++)
                            inv.addItem("CTF Event: " + _eventName, _rewardId, 1, player, activeChar.getTarget());
                    }
                
                    SystemMessage sm;

                    if (_rewardAmount > 1)
                    {
                        sm = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
                        sm.addItemName(_rewardId);
                        sm.addNumber(_rewardAmount);
                        player.sendPacket(sm);
                    }
                    else
                    {
                        sm = new SystemMessage(SystemMessage.EARNED_ITEM);
                        sm.addItemName(_rewardId);
                        player.sendPacket(sm);
                    }
                
                    StatusUpdate su = new StatusUpdate(player.getObjectId());
                    su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
                    player.sendPacket(su);

                    NpcHtmlMessage nhm = new NpcHtmlMessage(5);
                    TextBuilder replyMSG = new TextBuilder("");

                    replyMSG.append("<html><head><body>你所屬的隊伍在這場競賽中獲勝.獎品將自動轉入物品欄.</body></html>");

                    nhm.setHtml(replyMSG.toString());
                    player.sendPacket(nhm);
                }
            }
        }
    }

    public static void sit()
    {
        if (_sitForced)
            _sitForced = false;
        else
            _sitForced = true;
        
        for (L2PcInstance player : _players)
        {
            if (player != null)
            {
                if (_sitForced)
                {
                    player.stopMove(null, false);
                    player.abortAttack();
                    player.abortCast();
                    
                    if (!player.isSitting())
                        player.sitDown();
                }
                else
                {
                	if (player.isSitting())
                		player.standUp();
                }
            }
        }
    }
    
    private static void clean()
    {
        for (String team : _teams)
        {
            int index = _teams.indexOf(team);

            _teamPlayersCount.set(index, 0);
            _teamPointsCount.set(index, 0);
            _flagsTaken.set(index, false);
            _flagSpawns.set(index, null);
        }
        
        for (L2PcInstance player : _players)
        {
            player.setNameColor(player._originalNameColorCTF);
            player.setKarma(player._originalKarmaCTF);
            player.setTitle(player._originalTitleCTF);
            player.broadcastUserInfo();
            player._teamNameCTF = new String();
            player._inEventCTF = false;
            player._haveFlagCTF = false;
            player._posCheckerCTF.cancel(true);
            player._posCheckerCTF = null;
        }

        _topScore = 0;
        _topTeam = new String();
        _players = new Vector<L2PcInstance>();
        _playersShuffle = new Vector<L2PcInstance>();
        _savePlayers = new Vector<String>();
        _savePlayerTeams = new Vector<String>();
    }
    
    public static void unspawnEventNpc()
    {
        if (_npcSpawn == null)
            return;

        _npcSpawn.getLastSpawn().deleteMe();
        _npcSpawn.stopRespawn();
        SpawnTable.getInstance().deleteSpawn(_npcSpawn, true);
    }
    
    public static void unspawnAllFlags()
    {
    	for (String team : _teams)
    	{
    		int index = _teams.indexOf(team);

    		if (_flagSpawns.get(index) != null)
    		{
    			_flagSpawns.get(index).getLastSpawn().deleteMe();
    			_flagSpawns.get(index).stopRespawn();
    			SpawnTable.getInstance().deleteSpawn(_flagSpawns.get(index), true);
    		}
    	}
    }
    
    public static void teleportFinish()
    {
        Announcements.getInstance().announceToAll(_eventName + "(CTF): 將在20秒後傳回參賽地點!");

        ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
                                                       {
                                                            public void run()
                                                            {
                                                                for (L2PcInstance player : _players)
                                                                {
                                                                    if (player !=  null)
                                                                        player.teleToLocation(_npcX, _npcY, _npcZ);
                                                                }
                                                                
                                                                CTF.clean();
                                                            }
                                                       }, 20000);
    }
    
    private static void unspawnFlag(String teamName)
    {
    	int index = _teams.indexOf(teamName);

		_flagSpawns.get(index).getLastSpawn().deleteMe();
		_flagSpawns.get(index).stopRespawn();
		SpawnTable.getInstance().deleteSpawn(_flagSpawns.get(index), true);
    }
    
    public static void spawnFlag(String teamName)
    {
    	int index = _teams.indexOf(teamName);
		L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(_flagIds.get(index));

		try
		{
			_flagSpawns.set(index, new L2Spawn(tmpl));

			_flagSpawns.get(index).setLocx(_flagsX.get(index));
			_flagSpawns.get(index).setLocy(_flagsY.get(index));
			_flagSpawns.get(index).setLocz(_flagsZ.get(index));
			_flagSpawns.get(index).setAmount(1);
			_flagSpawns.get(index).setHeading(0);
			_flagSpawns.get(index).setRespawnDelay(1);

			SpawnTable.getInstance().addNewSpawn(_flagSpawns.get(index), false);

			_flagSpawns.get(index).init();
			_flagSpawns.get(index).getLastSpawn().setCurrentHp(999999999);
			_flagSpawns.get(index).getLastSpawn().setTitle(teamName);
			_flagSpawns.get(index).getLastSpawn().decayMe();
			_flagSpawns.get(index).getLastSpawn().spawnMe(_flagSpawns.get(index).getLastSpawn().getX(), _flagSpawns.get(index).getLastSpawn().getY(), _flagSpawns.get(index).getLastSpawn().getZ());
		}
		catch(Exception e)
		{}    	
    }
    
    public static void showEventHtml(L2PcInstance eventPlayer, String objectId)
    {
        try
        {
            NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

            TextBuilder replyMSG = new TextBuilder("<html><head><body>");
            replyMSG.append("<center>CTF [搶旗競賽]</center><br><br><br>");
            replyMSG.append("目前活動<br1>");
            replyMSG.append("    ... 名稱:&nbsp;<font color=\"00FF00\">" + _eventName + "</font><br1>");
            replyMSG.append("    ... 簡介:&nbsp;<font color=\"00FF00\">" + _eventDesc + "</font><br><br>");

            if (!_started && !_joining && !_teleport)
                replyMSG.append("<center>請等待管理員開放參加.</center>");
            else if (!_teleport && !_started && _joining)
            {
                if (_players.contains(eventPlayer) || _playersShuffle.contains(eventPlayer))
                {
                    if (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE"))
                        replyMSG.append("已經加入隊伍 <font color=\"LEVEL\">" + eventPlayer._teamNameCTF + "</font><br><br>");
                    else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
                        replyMSG.append("已經參與競賽!<br><br>");

                    replyMSG.append("<table border=\"0\"><tr>");
                    replyMSG.append("<td width=\"200\">請等待競賽開始或者脫離競賽</td>");
                    replyMSG.append("<td width=\"60\"><center><button value=\"脫離\" action=\"bypass -h npc_" + objectId + "_ctf_player_leave\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></td>");
                    replyMSG.append("<td width=\"100\"></td>");
                    replyMSG.append("</tr></table>");
                }
                else
                {
                    replyMSG.append("想參加這場競賽嗎?<br><br>");

                    if (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE"))
                    {
                        replyMSG.append("<center><table border=\"0\">");
                    
                        for (String team : _teams)
                        {
                            replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font>&nbsp;(" + teamPlayersCount(team) + " joined)</td>");
                            replyMSG.append("<td width=\"60\"><button value=\"加入\" action=\"bypass -h npc_" + objectId + "_ctf_player_join " + team + "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
                        }
                    
                        replyMSG.append("</table></center>");
                    }
                    else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
                    {
                        replyMSG.append("<center><table border=\"0\">");
                        
                        for (String team : _teams)
                            replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font></td>");
                    
                        replyMSG.append("</table></center><br>");
                        
                        replyMSG.append("<button value=\"參加\" action=\"bypass -h npc_" + objectId + "_ctf_player_join eventShuffle\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
                        replyMSG.append("隊伍將會自動隨機挑選!");
                    }
                }
            }
            else if (_started && !_joining && !_teleport)
                replyMSG.append("<center>CTF 搶旗競賽已經開始.</center>");
            
            replyMSG.append("</body></html>");
            adminReply.setHtml(replyMSG.toString());
            eventPlayer.sendPacket(adminReply);
        }
        catch (Exception e)
        {
            System.out.println("CTF Engine[showEventHtlm(" + eventPlayer.getName() + ", " + objectId + ")]: exception" + e.getMessage());
        }
    }
    
    public static synchronized void addPlayer(L2PcInstance player, String teamName)
    {
        if (!addPlayerOk(teamName))
        {
            player.sendMessage("\"" + teamName + "\" 隊人數過多 .");
            return;
        }

        if (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE"))
        {
            player._teamNameCTF = teamName;
            _players.add(player);
            setTeamPlayersCount(teamName, teamPlayersCount(teamName)+1);
            _savePlayers.add(player.getName());
            _savePlayerTeams.add(teamName);
        }
        else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
            _playersShuffle.add(player);
        
        player._originalTitleCTF = player.getTitle();
        player._originalNameColorCTF = player.getNameColor();
        player._originalKarmaCTF = player.getKarma();
        player._inEventCTF = true;
        player._posCheckerCTF = null;
    }
    
    public static boolean addPlayerOk(String teamName)
    {
        if (Config.CTF_EVEN_TEAMS.equals("NO"))
            return true;
        else if (Config.CTF_EVEN_TEAMS.equals("BALANCE"))
        {
            boolean allTeamsEqual = true;
            int countBefore = -1;
        
            for (int playersCount : _teamPlayersCount)
            {
                if (countBefore == -1)
                    countBefore = playersCount;
            
                if (countBefore != playersCount)
                {
                    allTeamsEqual = false;
                    break;
                }
            
                countBefore = playersCount;
            }
        
            if (allTeamsEqual)
                return true;

            countBefore = Integer.MAX_VALUE;
        
            for (int teamPlayerCount : _teamPlayersCount)
            {
                if (teamPlayerCount < countBefore)
                    countBefore = teamPlayerCount;
            }

            Vector<String> joinableTeams = new Vector<String>();
        
            for (String team : _teams)
            {
                if (teamPlayersCount(team) == countBefore)
                    joinableTeams.add(team);
            }
        
            if (joinableTeams.contains(teamName))
                return true;
        }
        else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
            return true;

        return false;
    }
    
    public static synchronized void addDisconnectedPlayer(L2PcInstance player)
    {
		if ((Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && (_teleport || _started)) || (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE")))
		{
	    	player._teamNameCTF = _savePlayerTeams.get(_savePlayers.indexOf(player.getName()));
			_players.add(player);
			player._originalTitleCTF = player.getTitle();
			player._originalNameColorCTF = player.getNameColor();
			player._originalKarmaCTF = player.getKarma();
			player._inEventCTF = true;
			player._posCheckerCTF = null;

			if (_teleport || _started)
			{
				player.setNameColor(_teamColors.get(_teams.indexOf(player._teamNameCTF)));
				player.setKarma(0);
				player.broadcastUserInfo();
				player.teleToLocation(_flagsX.get(_teams.indexOf(player._teamNameCTF)), _flagsY.get(_teams.indexOf(player._teamNameCTF)), _flagsZ.get(_teams.indexOf(player._teamNameCTF)));
				player._posCheckerCTF = ThreadPoolManager.getInstance().scheduleGeneral(new posChecker(player), 0);
			}
		}
    }
    
    public static synchronized void removePlayer(L2PcInstance player)
    {
        if (player != null)
        {
            if (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE"))
            {
                _players.remove(player);
                setTeamPlayersCount(player._teamNameCTF, teamPlayersCount(player._teamNameCTF)-1);
                player._inEventCTF = false;
            }
            else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
                _playersShuffle.remove(player);
        }
    }
    
    private static class posChecker implements Runnable
    {
    	private L2PcInstance _player;

    	posChecker(L2PcInstance player)
    	{
    		_player = player;
    	}
    	
    	private void processInFlagRange()
    	{	
    		for (String team : CTF._teams)
    		{
    			if (team.equals(_player._teamNameCTF))
    			{
    				int indexOwn = CTF._teams.indexOf(_player._teamNameCTF);
					
    				if (_player.getX() > CTF._flagsX.get(indexOwn)-100 && _player.getX() < CTF._flagsX.get(indexOwn)+100 &&
    					_player.getY() > CTF._flagsY.get(indexOwn)-100 && _player.getY() < CTF._flagsY.get(indexOwn)+100 &&
    					_player.getZ() > CTF._flagsZ.get(indexOwn)-100 && _player.getZ() < CTF._flagsZ.get(indexOwn)+100 &&
    					!CTF._flagsTaken.get(indexOwn) && _player._haveFlagCTF)
    				{
    					int indexEnemy = CTF._teams.indexOf(_player._teamNameHaveFlagCTF);

   						CTF._flagsTaken.set(indexEnemy, false);
   						CTF.spawnFlag(_player._teamNameHaveFlagCTF);
   						_player.setTitle(_player._originalTitleCTF);
   						_player.broadcastUserInfo();
   						_player._haveFlagCTF = false;
   						_teamPointsCount.set(indexOwn, teamPointsCount(team)+1);
   						Announcements.getInstance().announceToAll(_eventName + "(CTF): " + _player.getName() + "為" + _player._teamNameCTF + "得分.");
   					}
    			}
    			else
    			{
    				int indexEnemy = CTF._teams.indexOf(team);
    				
    				if ((_player.getX() > CTF._flagsX.get(indexEnemy)-100 && _player.getX() < CTF._flagsX.get(indexEnemy)+100) &&
    					(_player.getY() > CTF._flagsY.get(indexEnemy)-100 && _player.getY() < CTF._flagsY.get(indexEnemy)+100) &&
    					(_player.getZ() > CTF._flagsZ.get(indexEnemy)-100 && _player.getZ() < CTF._flagsZ.get(indexEnemy)+100) &&
    					!CTF._flagsTaken.get(indexEnemy) && !_player._haveFlagCTF && !_player.isDead())
    				{
    					CTF._flagsTaken.set(indexEnemy, true);
    					CTF.unspawnFlag(team);
    					_player._teamNameHaveFlagCTF = team;
    					_player.setTitle("旗子搶奪");
    					_player.broadcastUserInfo();
    					_player._haveFlagCTF = true;
    					Announcements.getInstance().announceToAll(_eventName + "(CTF): " + team + "隊的旗子被奪取!");
    					break;
    				}
    			}
			}
    	}
    	
    	private void restoreTakenFlag()
    	{
			Vector<Integer> teamsTakenFlag = new Vector<Integer>();

    		for (L2PcInstance player : CTF._players)
    		{
    			if (player != null && player._haveFlagCTF)    			
    				teamsTakenFlag.add(CTF._teams.indexOf(player._teamNameHaveFlagCTF));
    		}
    		
    		for (String team : CTF._teams)
    		{
    			int index = CTF._teams.indexOf(team);
    			
    			if (!teamsTakenFlag.contains(index))
    			{
    				if (CTF._flagsTaken.get(index))
    				{
    					CTF._flagsTaken.set(index, false);
    					CTF.spawnFlag(team);
    					Announcements.getInstance().announceToAll(CTF._eventName + "(CTF): " + team + "隊的旗子回歸.");
    				}
    			}
    		}
    	}
    	
    	public void run()
    	{
    		for (;;)
    		{
    			try
    			{
    				Thread.sleep(500);
    			}
    			catch (InterruptedException ie)
    			{}
    			
    			if (_player == null)
    			{
    				restoreTakenFlag();
    				break;
    			}
    			else
    				processInFlagRange();
    		}
    	}
    }
    
    public static void dumpData()
    {
        System.out.println("");
        System.out.println("");
        
        if (!_joining && !_teleport && !_started)
        {
        	System.out.println("<<---------------------------------->>");
        	System.out.println(">> CTF Engine infos dump (INACTIVE) <<");
        	System.out.println("<<--^----^^-----^----^^------^^----->>");
        }
        else if (_joining && !_teleport && !_started)
        {
        	System.out.println("<<--------------------------------->>");
        	System.out.println(">> CTF Engine infos dump (JOINING) <<");
        	System.out.println("<<--^----^^-----^----^^------^----->>");
        }
        else if (!_joining && _teleport && !_started)
        {
        	System.out.println("<<---------------------------------->>");
        	System.out.println(">> CTF Engine infos dump (TELEPORT) <<");
        	System.out.println("<<--^----^^-----^----^^------^^----->>");
        }
        else if (!_joining && !_teleport && _started)
        {
        	System.out.println("<<--------------------------------->>");
        	System.out.println(">> CTF Engine infos dump (STARTED) <<");
        	System.out.println("<<--^----^^-----^----^^------^----->>");
        }

        System.out.println("Name: " + _eventName);
        System.out.println("Desc: " + _eventDesc);
        System.out.println("Join location: " + _joiningLocationName);
        System.out.println("");
        System.out.println("##########################");
        System.out.println("# _teams(Vector<String>) #");
        System.out.println("##########################");
        
        for (String team : _teams)
            System.out.println(team);

        System.out.println("");
        System.out.println("#########################################");
        System.out.println("# _playersShuffle(Vector<L2PcInstance>) #");
        System.out.println("#########################################");
        
        for (L2PcInstance player : _playersShuffle)
        {
        	if (player != null)
        		System.out.println("Name: " + player.getName());
        }
        
        System.out.println("");
        System.out.println("##################################");
        System.out.println("# _players(Vector<L2PcInstance>) #");
        System.out.println("##################################");
        
        for (L2PcInstance player : _players)
        {
        	if (player != null)
        		System.out.println("Name: " + player.getName() + "    Team: " + player._teamNameCTF);
        }
        
        System.out.println("");
        System.out.println("#####################################################################");
        System.out.println("# _savePlayers(Vector<String>) and _savePlayerTeams(Vector<String>) #");
        System.out.println("#####################################################################");
        
        for (String player : _savePlayers)
            System.out.println("Name: " + player + "    Team: " + _savePlayerTeams.get(_savePlayers.indexOf(player)));
        
        System.out.println("");
        System.out.println("");
    }

    public static int teamPointsCount(String teamName)
    {
        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return -1;

        return _teamPointsCount.get(index);
    }
    
    public static void setTeamPointsCount(String teamName, int teamKillsCount)
    {
        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return;

        _teamPointsCount.set(index, teamKillsCount);
    }
    
    public static int teamPlayersCount(String teamName)
    {
        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return -1;

        return _teamPlayersCount.get(index);
    }
    
    public static void setTeamPlayersCount(String teamName, int teamPlayersCount)
    {
        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return;
        
        _teamPlayersCount.set(index, teamPlayersCount);
    }
}