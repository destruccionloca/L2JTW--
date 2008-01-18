/*
 * $HeadURL: $
 *
 * $Author: $
 * $Date: $
 * $Revision: $
 *
 * 
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
package net.sf.l2j.gameserver.instancemanager;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Set;
import java.util.List;
import javolution.util.FastMap;
import javolution.util.FastList;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
//import net.sf.l2j.gameserver.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SepulcherMonsterInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.util.Rnd;

/**
 * This class ...
 * @version $Revision: $ $Date: $
 * @author  sandman
 */
public class FourSepulchersManager
{
    protected static Logger _log = Logger.getLogger(FourSepulchersManager.class.getName());

    private static FourSepulchersManager _Instance;

    private String _QuestId = "620_FourGoblets";
    private int _EntrancePass = 7075;
    private int _UsedEntrancePass = 7261;
    private final int _HallsKey = 7260;
    private int _OldBrooch = 7262;

    protected boolean _InEntryTime = false;
    protected boolean _InWarmUpTime = false;
    protected boolean _InAttackTime = false;
    protected boolean _InCoolDownTime = false;

    protected ScheduledFuture _ChangeCoolDownTimeTask = null;
    protected ScheduledFuture _ChangeEntryTimeTask = null; 
    protected ScheduledFuture _ChaneWarmUpTimeTask = null;
    protected ScheduledFuture _ChangeAttackTimeTask = null;
    protected ScheduledFuture _OnPartyAnnihilatedTask = null;
    
    
    
    protected static Map<Integer,Integer> _HallGateKeepers = new FastMap<Integer,Integer>();
    
    private int[][] _StartHallSpawn =
	{
		{181632,-85587,-7218},
		{179963,-88978,-7218},
		{173217,-86132,-7218},
		{175608,-82296,-7218}
	};
    
    protected static Map<Integer,int[]> _StartHallSpawns = new FastMap<Integer,int[]>();

    private int[][][] _ShadowSpawnLoc =
	{
		{
			{25339,191231,-85574,-7216,33380},
			{25349,189534,-88969,-7216,32768},
			{25346,173195,-76560,-7215,49277},
			{25342,175591,-72744,-7215,49317}
		},

		{
			{25342,191231,-85574,-7216,33380},
			{25339,189534,-88969,-7216,32768},
			{25349,173195,-76560,-7215,49277},
			{25346,175591,-72744,-7215,49317}
		},

		{
			{25346,191231,-85574,-7216,33380},
			{25342,189534,-88969,-7216,32768},
			{25339,173195,-76560,-7215,49277},
			{25349,175591,-72744,-7215,49317}
		},

		{
			{25349,191231,-85574,-7216,33380},
			{25346,189534,-88969,-7216,32768},
			{25342,173195,-76560,-7215,49277},
			{25339,175591,-72744,-7215,49317}
		},
	};
    
    protected Map<Integer,L2Spawn> _ShadowSpawns = new FastMap<Integer,L2Spawn>();
    
    protected static Map<Integer,Boolean> _HallInUse = new FastMap<Integer,Boolean>();
    
    protected Map<Integer,L2PcInstance> _Challengers = new FastMap<Integer,L2PcInstance>();
    
    protected Map<Integer,L2Spawn> _MysteriousBoxSpawns = new FastMap<Integer,L2Spawn>();
    
    protected List<L2Spawn> _PhysicalSpawns;
    protected Map<Integer,List> _PhysicalMonsters = new FastMap<Integer,List>();
    protected List<L2Spawn> _MagicalSpawns;
    protected Map<Integer,List> _MagicalMonsters = new FastMap<Integer,List>();

    protected List<L2Spawn> _DukeFinalSpawns;
    protected Map<Integer,List> _DukeFinalMobs = new FastMap<Integer,List>();
    protected Map<Integer,Boolean> _ArchonSpawned = new FastMap<Integer,Boolean>();

    protected List<L2Spawn> _EmperorsGraveSpawns;
    protected Map<Integer,List> _EmperorsGraveNpcs = new FastMap<Integer,List>();
    
    protected Map<Integer,List> _ViscountMobs =
    	new FastMap<Integer,List>();
    
    protected Map<Integer,List> _DukeMobs =
    	new FastMap<Integer,List>();
    
    protected Map<Integer,Integer> _KeyBoxNpc = new FastMap<Integer,Integer>();
    protected Map<Integer,L2Spawn> _KeyBoxSpawns = new FastMap<Integer,L2Spawn>();
    
    protected Map<Integer,Integer> _Victim = new FastMap<Integer,Integer>();
    protected Map<Integer,L2Spawn> _ExecutionerSpawns = new FastMap<Integer,L2Spawn>();
    
    protected List<L2NpcInstance> _AllMobs = new FastList<L2NpcInstance>();
    
    //private L2NpcInstance _ConquerorsSepulcherManager;
    //private L2NpcInstance _EmperorsSepulcherManager;
    //private L2NpcInstance _GreatSagesSepulcherManager;
    //private L2NpcInstance _JudgesSepulcherManager;

    public FourSepulchersManager()
    {
    }

    public static final FourSepulchersManager getInstance()
    {
        if (_Instance == null)
        {
            _Instance = new FourSepulchersManager();
        }
        return _Instance;
    }
    
    public void init()
    {
        _log.info("FourSepulchersManager:Init Four-Sepulchers Manager.");

        if(_ChangeCoolDownTimeTask != null) _ChangeCoolDownTimeTask.cancel(true);
        if(_ChangeEntryTimeTask != null) _ChangeEntryTimeTask.cancel(true);
        if(_ChaneWarmUpTimeTask != null) _ChaneWarmUpTimeTask.cancel(true);
        if(_ChangeAttackTimeTask != null) _ChangeAttackTimeTask.cancel(true);

        _ChangeCoolDownTimeTask = null;
        _ChangeEntryTimeTask = null; 
        _ChaneWarmUpTimeTask = null;
        _ChangeAttackTimeTask = null;
        
        _InEntryTime = false;
        _InWarmUpTime = false;
        _InAttackTime = false;
        _InCoolDownTime = false;

        initFixedInfo();
        
        LoadMysteriousBox();
        InitKeyBoxSpawns();
        LoadPhysicalMonsters();
        LoadMagicalMonsters();
        InitLocationShadowSpawns();
        InitExecutionerSpawns();
        LoadDukeMonsters();
        LoadEmperorsGraveMonsters();
        
        _ChangeCoolDownTimeTask =
            ThreadPoolManager.getInstance().scheduleGeneral(new ChangeCoolDownTime(),Config.FS_TIME_ATTACK * 60000);
        
    }

    protected void initFixedInfo()
    {
        _StartHallSpawns.clear();
        _StartHallSpawns.put(31921,_StartHallSpawn[0]);
        _StartHallSpawns.put(31922,_StartHallSpawn[1]);
        _StartHallSpawns.put(31923,_StartHallSpawn[2]);
        _StartHallSpawns.put(31924,_StartHallSpawn[3]);
        
        _HallGateKeepers.clear();
        _HallGateKeepers.put(31925, 25150012);
        _HallGateKeepers.put(31926, 25150013);
        _HallGateKeepers.put(31927, 25150014);
        _HallGateKeepers.put(31928, 25150015);
        _HallGateKeepers.put(31929, 25150016);
        _HallGateKeepers.put(31930, 25150002);
        _HallGateKeepers.put(31931, 25150003);
        _HallGateKeepers.put(31932, 25150004);
        _HallGateKeepers.put(31933, 25150005);
        _HallGateKeepers.put(31934, 25150006);
        _HallGateKeepers.put(31935, 25150032);
        _HallGateKeepers.put(31936, 25150033);
        _HallGateKeepers.put(31937, 25150034);
        _HallGateKeepers.put(31938, 25150035);
        _HallGateKeepers.put(31939, 25150036);
        _HallGateKeepers.put(31940, 25150022);
        _HallGateKeepers.put(31941, 25150023);
        _HallGateKeepers.put(31942, 25150024);
        _HallGateKeepers.put(31943, 25150025);
        _HallGateKeepers.put(31944, 25150026);
        
        _KeyBoxNpc.clear();
        _KeyBoxNpc.put(18120,31455);
        _KeyBoxNpc.put(18121,31455);
        _KeyBoxNpc.put(18122,31455);
        _KeyBoxNpc.put(18123,31455);
        _KeyBoxNpc.put(18124,31456);
        _KeyBoxNpc.put(18125,31456);
        _KeyBoxNpc.put(18126,31456);
        _KeyBoxNpc.put(18127,31456);
        _KeyBoxNpc.put(18128,31457);
        _KeyBoxNpc.put(18129,31457);
        _KeyBoxNpc.put(18130,31457);
        _KeyBoxNpc.put(18131,31457);
        _KeyBoxNpc.put(18149,31458);
        _KeyBoxNpc.put(18150,31459);
        _KeyBoxNpc.put(18151,31459);
        _KeyBoxNpc.put(18152,31459);
        _KeyBoxNpc.put(18153,31459);
        _KeyBoxNpc.put(18154,31460);
        _KeyBoxNpc.put(18155,31460);
        _KeyBoxNpc.put(18156,31460);
        _KeyBoxNpc.put(18157,31460);
        _KeyBoxNpc.put(18158,31461);
        _KeyBoxNpc.put(18159,31461);
        _KeyBoxNpc.put(18160,31461);
        _KeyBoxNpc.put(18161,31461);
        _KeyBoxNpc.put(18162,31462);
        _KeyBoxNpc.put(18163,31462);
        _KeyBoxNpc.put(18164,31462);
        _KeyBoxNpc.put(18165,31462);
        _KeyBoxNpc.put(18183,31463);
        _KeyBoxNpc.put(18184,31464);
        _KeyBoxNpc.put(18212,31465);
        _KeyBoxNpc.put(18213,31465);
        _KeyBoxNpc.put(18214,31465);
        _KeyBoxNpc.put(18215,31465);
        _KeyBoxNpc.put(18216,31466);
        _KeyBoxNpc.put(18217,31466);
        _KeyBoxNpc.put(18218,31466);
        _KeyBoxNpc.put(18219,31466);
     
        _Victim.clear();
        _Victim.put(18150,18158);
        _Victim.put(18151,18159);
        _Victim.put(18152,18160);
        _Victim.put(18153,18161);
        _Victim.put(18154,18162);
        _Victim.put(18155,18163);
        _Victim.put(18156,18164);
        _Victim.put(18157,18165);

    }

    private void LoadMysteriousBox()
    {
        java.sql.Connection con = null;
        
        _MysteriousBoxSpawns.clear();

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY id");
            statement.setInt(1, 0);
            ResultSet rset = statement.executeQuery();

            L2Spawn spawnDat;
            L2NpcTemplate template1;

            while (rset.next())
            {
                template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
                if (template1 != null)
                {
                	spawnDat = new L2Spawn(template1);
                	spawnDat.setAmount(rset.getInt("count"));
                	spawnDat.setLocx(rset.getInt("locx"));
                	spawnDat.setLocy(rset.getInt("locy"));
                	spawnDat.setLocz(rset.getInt("locz"));
                	spawnDat.setHeading(rset.getInt("heading"));
                	spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
                	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                	int keyNpcId = rset.getInt("key_npc_id");
                    _MysteriousBoxSpawns.put(keyNpcId,spawnDat);
                }
                else {
                    _log.warning("FourSepulchersManager.LoadMysteriousBox: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
                }
            }

            rset.close();
            statement.close();
            _log.info("FourSepulchersManager.LoadMysteriousBox: Loaded " + _MysteriousBoxSpawns.size() + " Mysterious-Box spawn locations.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warning("FourSepulchersManager.LoadMysteriousBox: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }

    private void InitKeyBoxSpawns()
    {
    	L2Spawn spawnDat;
        L2NpcTemplate template;

        for(int keyNpcId:_KeyBoxNpc.keySet())
        {
            try
            {
                template = NpcTable.getInstance().getTemplate(_KeyBoxNpc.get(keyNpcId));
                if (template != null)
                {
                	spawnDat = new L2Spawn(template);
                	spawnDat.setAmount(1);
                	spawnDat.setLocx(0);
                	spawnDat.setLocy(0);
                	spawnDat.setLocz(0);
                	spawnDat.setHeading(0);
                	spawnDat.setRespawnDelay(3600);
                	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                	_KeyBoxSpawns.put(keyNpcId, spawnDat);
                }
                else {
                    _log.warning("FourSepulchersManager.InitKeyBoxSpawns: Data missing in NPC table for ID: " + _KeyBoxNpc.get(keyNpcId) + ".");
                }
            }
            catch (Exception e)
            {
                _log.warning("FourSepulchersManager.InitKeyBoxSpawns: Spawn could not be initialized: " + e);
            }
        }
    }
    
    private void LoadPhysicalMonsters()
    {

    	_PhysicalMonsters.clear();

    	int loaded = 0;
    	java.sql.Connection con = null;
    	
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            
            PreparedStatement statement1 = con.prepareStatement("SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
            statement1.setInt(1, 1);
            ResultSet rset1 = statement1.executeQuery();
            while (rset1.next())
            {
            	int keyNpcId = rset1.getInt("key_npc_id");

                PreparedStatement statement2 = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
                statement2.setInt(1, keyNpcId);
                statement2.setInt(2, 1);
                ResultSet rset2 = statement2.executeQuery();

                L2Spawn spawnDat;
                L2NpcTemplate template1;

                _PhysicalSpawns = new FastList<L2Spawn>();

                while (rset2.next())
                {
                    template1 = NpcTable.getInstance().getTemplate(rset2.getInt("npc_templateid"));
                    if (template1 != null)
                    {
                    	spawnDat = new L2Spawn(template1);
                    	spawnDat.setAmount(rset2.getInt("count"));
                    	spawnDat.setLocx(rset2.getInt("locx"));
                    	spawnDat.setLocy(rset2.getInt("locy"));
                    	spawnDat.setLocz(rset2.getInt("locz"));
                    	spawnDat.setHeading(rset2.getInt("heading"));
                    	spawnDat.setRespawnDelay(rset2.getInt("respawn_delay"));
                    	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                    	_PhysicalSpawns.add(spawnDat);
                    	loaded++;
                    }
                    else {
                        _log.warning("FourSepulchersManager.LoadPhysicalMonsters: Data missing in NPC table for ID: " + rset2.getInt("npc_templateid") + ".");
                    }
                }

                rset2.close();
                statement2.close();
            	_PhysicalMonsters.put(keyNpcId,_PhysicalSpawns);
            }

            rset1.close();
            statement1.close();
            _log.info("FourSepulchersManager.LoadPhysicalMonsters: Loaded " + loaded + " Physical type monsters spawn locations.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warning("FourSepulchersManager.LoadPhysicalMonsters: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    private void LoadMagicalMonsters()
    {

    	_MagicalMonsters.clear();

    	int loaded = 0;
    	java.sql.Connection con = null;
    	
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            
            PreparedStatement statement1 = con.prepareStatement("SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
            statement1.setInt(1, 2);
            ResultSet rset1 = statement1.executeQuery();
            while (rset1.next())
            {

            	int keyNpcId = rset1.getInt("key_npc_id");

                PreparedStatement statement2 = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
                statement2.setInt(1, keyNpcId);
                statement2.setInt(2, 2);
                ResultSet rset2 = statement2.executeQuery();

                L2Spawn spawnDat;
                L2NpcTemplate template1;

                _MagicalSpawns = new FastList<L2Spawn>();            	

                while (rset2.next())
                {
                    template1 = NpcTable.getInstance().getTemplate(rset2.getInt("npc_templateid"));
                    if (template1 != null)
                    {
                    	spawnDat = new L2Spawn(template1);
                    	spawnDat.setAmount(rset2.getInt("count"));
                    	spawnDat.setLocx(rset2.getInt("locx"));
                    	spawnDat.setLocy(rset2.getInt("locy"));
                    	spawnDat.setLocz(rset2.getInt("locz"));
                    	spawnDat.setHeading(rset2.getInt("heading"));
                    	spawnDat.setRespawnDelay(rset2.getInt("respawn_delay"));
                    	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                    	_MagicalSpawns.add(spawnDat);
                    	loaded++;
                    }
                    else {
                        _log.warning("FourSepulchersManager.LoadMagicalMonsters: Data missing in NPC table for ID: " + rset2.getInt("npc_templateid") + ".");
                    }
                }

                rset2.close();
                statement2.close();
                _MagicalMonsters.put(keyNpcId,_MagicalSpawns);
            }

            rset1.close();
            statement1.close();
            _log.info("FourSepulchersManager.LoadMagicalMonsters: Loaded " + loaded + " Magical type monsters spawn locations.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warning("FourSepulchersManager.LoadMagicalMonsters: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    private void LoadDukeMonsters()
    {

    	_DukeFinalMobs.clear();
    	_ArchonSpawned.clear();

    	int loaded = 0;
    	java.sql.Connection con = null;
    	
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            
            PreparedStatement statement1 = con.prepareStatement("SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
            statement1.setInt(1, 5);
            ResultSet rset1 = statement1.executeQuery();
            while (rset1.next())
            {
            	int keyNpcId = rset1.getInt("key_npc_id");

                PreparedStatement statement2 = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
                statement2.setInt(1, keyNpcId);
                statement2.setInt(2, 5);
                ResultSet rset2 = statement2.executeQuery();

                L2Spawn spawnDat;
                L2NpcTemplate template1;

                _DukeFinalSpawns = new FastList<L2Spawn>();

                while (rset2.next())
                {
                    template1 = NpcTable.getInstance().getTemplate(rset2.getInt("npc_templateid"));
                    if (template1 != null)
                    {
                    	spawnDat = new L2Spawn(template1);
                    	spawnDat.setAmount(rset2.getInt("count"));
                    	spawnDat.setLocx(rset2.getInt("locx"));
                    	spawnDat.setLocy(rset2.getInt("locy"));
                    	spawnDat.setLocz(rset2.getInt("locz"));
                    	spawnDat.setHeading(rset2.getInt("heading"));
                    	spawnDat.setRespawnDelay(rset2.getInt("respawn_delay"));
                    	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                    	_DukeFinalSpawns.add(spawnDat);
                    	loaded++;
                    }
                    else {
                        _log.warning("FourSepulchersManager.LoadDukeMonsters: Data missing in NPC table for ID: " + rset2.getInt("npc_templateid") + ".");
                    }
                }

                rset2.close();
                statement2.close();
                _DukeFinalMobs.put(keyNpcId,_DukeFinalSpawns);
                _ArchonSpawned.put(keyNpcId, false);
            }

            rset1.close();
            statement1.close();
            _log.info("FourSepulchersManager.LoadDukeMonsters: Loaded " + loaded + " Church of duke monsters spawn locations.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warning("FourSepulchersManager.LoadDukeMonsters: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    private void LoadEmperorsGraveMonsters()
    {

    	_EmperorsGraveNpcs.clear();

    	int loaded = 0;
    	java.sql.Connection con = null;
    	
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            
            PreparedStatement statement1 = con.prepareStatement("SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
            statement1.setInt(1, 6);
            ResultSet rset1 = statement1.executeQuery();
            while (rset1.next())
            {
            	int keyNpcId = rset1.getInt("key_npc_id");

                PreparedStatement statement2 = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
                statement2.setInt(1, keyNpcId);
                statement2.setInt(2, 6);
                ResultSet rset2 = statement2.executeQuery();

                L2Spawn spawnDat;
                L2NpcTemplate template1;

                _EmperorsGraveSpawns = new FastList<L2Spawn>();

                while (rset2.next())
                {
                    template1 = NpcTable.getInstance().getTemplate(rset2.getInt("npc_templateid"));
                    if (template1 != null)
                    {
                    	spawnDat = new L2Spawn(template1);
                    	spawnDat.setAmount(rset2.getInt("count"));
                    	spawnDat.setLocx(rset2.getInt("locx"));
                    	spawnDat.setLocy(rset2.getInt("locy"));
                    	spawnDat.setLocz(rset2.getInt("locz"));
                    	spawnDat.setHeading(rset2.getInt("heading"));
                    	spawnDat.setRespawnDelay(rset2.getInt("respawn_delay"));
                    	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                    	_EmperorsGraveSpawns.add(spawnDat);
                    	loaded++;
                    }
                    else {
                        _log.warning("FourSepulchersManager.LoadEmperorsGraveMonsters: Data missing in NPC table for ID: " + rset2.getInt("npc_templateid") + ".");
                    }
                }

                rset2.close();
                statement2.close();
                _EmperorsGraveNpcs.put(keyNpcId,_EmperorsGraveSpawns);
            }

            rset1.close();
            statement1.close();
            _log.info("FourSepulchersManager.LoadEmperorsGraveMonsters: Loaded " + loaded + " Emperor's grave NPC spawn locations.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warning("FourSepulchersManager.LoadEmperorsGraveMonsters: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    protected void InitLocationShadowSpawns()
    {
    	int locNo = Rnd.get(4);
    	final int[] gateKeeper = {31929,31934,31939,31944};
    	
    	L2Spawn spawnDat;
        L2NpcTemplate template;

        _ShadowSpawns.clear();

        for(int i=0;i<=3;i++)
    	{
            template = NpcTable.getInstance().getTemplate(_ShadowSpawnLoc[locNo][i][0]);
            if (template != null)
            {
            	try
            	{
                	spawnDat = new L2Spawn(template);
                	spawnDat.setAmount(1);
                	spawnDat.setLocx(_ShadowSpawnLoc[locNo][i][1]);
                	spawnDat.setLocy(_ShadowSpawnLoc[locNo][i][2]);
                	spawnDat.setLocz(_ShadowSpawnLoc[locNo][i][3]);
                	spawnDat.setHeading(_ShadowSpawnLoc[locNo][i][4]);
                	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                	int keyNpcId = gateKeeper[i];
                	_ShadowSpawns.put(keyNpcId,spawnDat);
            	}
            	catch(Exception e)
            	{
            		_log.warning(e.getMessage());
            		e.printStackTrace();
            	}
            }
            else {
                _log.warning("FourSepulchersManager.InitLocationShadowSpawns: Data missing in NPC table for ID: " + _ShadowSpawnLoc[locNo][i][0] + ".");
            }
    	}
    }

    protected void InitExecutionerSpawns()
    {
    	L2Spawn spawnDat;
        L2NpcTemplate template;

        for(int keyNpcId:_Victim.keySet())
        {
            try
            {
                template = NpcTable.getInstance().getTemplate(_Victim.get(keyNpcId));
                if (template != null)
                {
                	spawnDat = new L2Spawn(template);
                	spawnDat.setAmount(1);
                	spawnDat.setLocx(0);
                	spawnDat.setLocy(0);
                	spawnDat.setLocz(0);
                	spawnDat.setHeading(0);
                	spawnDat.setRespawnDelay(3600);
                	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                	_ExecutionerSpawns.put(keyNpcId, spawnDat);
                }
                else {
                    _log.warning("FourSepulchersManager.InitExecutionerSpawns: Data missing in NPC table for ID: " + _Victim.get(keyNpcId) + ".");
                }
            }
            catch (Exception e)
            {
                _log.warning("FourSepulchersManager.InitExecutionerSpawns: Spawn could not be initialized: " + e);
            }
        }
    }
    
    public boolean IsEntryTime()
    {
    	return _InEntryTime;
    }
    
    public boolean IsAttackTime()
    {
    	return _InAttackTime;
    }
    
    public synchronized boolean IsEnableEntry(int npcId,L2PcInstance player)
    {

        if(!IsEntryTime())
        {
        	player.sendMessage("現在好像不是可以入場的時間，等一會兒再試試吧。");
        	return false;
        }
        
        else if(_HallInUse.get(npcId).booleanValue()) 
        {
        	player.sendMessage("其他隊伍正在進行中。下次可以入場的時侯再來挑戰吧。");
        	return false;
        }

        else if(Config.FS_PARTY_MEMBER_COUNT > 1)
        {

        	if(player.getParty() == null) 
            {
            	player.sendMessage("想起來了，隊員至少需要有4名才行。要再找隊員然後再來。");
            	return false;
            }
        		
        	if(!player.getParty().isLeader(player)) 
            {
            	player.sendMessage("恐怕要隊長把手放在石像上才行。我把手放上去也沒有反應。");
            	return false;
            }
        	
            if (player.getParty().getMemberCount() < Config.FS_PARTY_MEMBER_COUNT) 
            {
            	player.sendMessage("想起來了，隊員至少需要有4名才行。要再找隊員然後再來。");
            	return false;
            }

            else
            {
                for (L2PcInstance mem : player.getParty().getPartyMembers())
                {

                    if(mem.getQuestState(_QuestId).get("<state>") == null) 
                    {
                    	player.sendMessage("這麼一看，原來隊員中有不聽從無名的靈魂的囑托的。看來得讓隊員去和無名的靈魂談話，然後再回來。");
                    	return false;
                    }

                    if (mem.getInventory().getItemByItemId(_EntrancePass) == null)
                    {
                    	player.sendMessage("這麼一看，原來隊員中有沒帶陵墓通行證的。得給隊員陵墓通行證，或者讓他去弄來。");
                    	return false;
                    }

                    int invLimitCnt = mem.GetInventoryLimit();
                    int invItemCnt = mem.getInventory().getItems().length;
                    if((invItemCnt / invLimitCnt) >= 0.8)
                    {
                    	player.sendPacket(new SystemMessage(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT));
                    	return false;
                    }
                    
                    int invLimitWeight = mem.getMaxLoad();
                    int invWeight = mem.getInventory().getTotalWeight();
                    if((invWeight / invLimitWeight) >= 0.8)
                    {
                    	player.sendPacket(new SystemMessage(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT));
                    	return false;
                    }
                }
            }
        }
        else
        {

            //if(player.getQuestState(_QuestId).get("<state>") == null) return false;

            if(player.getInventory().getItemByItemId(_EntrancePass) == null) 
            {
            	player.sendMessage("這麼一看，原來你沒帶陵墓通行證。快去把陵墓通行證帶來吧。");
            	return false;
            }

            int invLimitCnt = player.GetInventoryLimit();
            int invItemCnt = player.getInventory().getItems().length;
            if((invItemCnt / invLimitCnt) >= 0.8)
            {
            	player.sendPacket(new SystemMessage(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT));
            	return false;
            }
            
            int invLimitWeight = player.getMaxLoad();
            int invWeight = player.getInventory().getTotalWeight();
            if((invWeight / invLimitWeight) >= 0.8)
            {
            	player.sendPacket(new SystemMessage(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT));
            	return false;
            }
        }

        return true;
    }
    
    public void Entry(int npcId, L2PcInstance player)
	{

		int[] Location = _StartHallSpawns.get(npcId);
		int driftx;
		int drifty;

		if (Config.FS_PARTY_MEMBER_COUNT > 1)
		{

			if (IsEnableEntry(npcId, player))
			{
				List<L2PcInstance> members = new FastList<L2PcInstance>();
				for (L2PcInstance mem : player.getParty().getPartyMembers())
				{
					if (!mem.isDead() && Util.checkIfInRange(700, player, mem, true))
					{
						members.add(mem);
					}
				}

				for (L2PcInstance mem : members)
				{

					driftx = Rnd.get(-80, 80);
					drifty = Rnd.get(-80, 80);
					mem.teleToLocation(Location[0] + driftx, Location[1] + drifty, Location[2]);

					mem.destroyItemByItemId("Quest", _EntrancePass, 1, mem,true);

                    if (mem.getInventory().getItemByItemId(_OldBrooch) == null)
                    {
						mem.addItem("Quest", _UsedEntrancePass, 1, mem, true);
                    }

					L2ItemInstance HallsKey = mem.getInventory().getItemByItemId(_HallsKey);
	                if(HallsKey != null)
	                {
	                	mem.destroyItemByItemId("Quest", _HallsKey, HallsKey.getCount(), mem, true);
	                }
				}

				_Challengers.remove(npcId);
				_Challengers.put(npcId, player);

				_HallInUse.remove(npcId);
				_HallInUse.put(npcId, true);
			}
			else
			{
            	//player.sendMessage("尚未達到所需要的條件。");
			}

		}
		else
		{

			if (IsEnableEntry(npcId, player))
			{

				driftx = Rnd.get(-80, 80);
				drifty = Rnd.get(-80, 80);
				player.teleToLocation(Location[0] + driftx, Location[1] + drifty, Location[2]);

				player.destroyItemByItemId("Quest", _EntrancePass, 1, player, true);

                if (player.getInventory().getItemByItemId(_OldBrooch) == null)
                {
                	player.addItem("Quest", _UsedEntrancePass, 1, player, true);
                }

				L2ItemInstance HallsKey = player.getInventory().getItemByItemId(_HallsKey);
                if(HallsKey != null)
                {
                	player.destroyItemByItemId("Quest", _HallsKey, HallsKey.getCount(), player, true);
                }

				_Challengers.remove(npcId);
				_Challengers.put(npcId, player);

				_HallInUse.remove(npcId);
				_HallInUse.put(npcId, true);
			}
			else
			{
            	//player.sendMessage("尚未達到所需要的條件。");
			}

		}
	}

    public void SpawnMysteriousBox(int npcId)
    {
    	if (!IsAttackTime()) return;
    	
    	L2Spawn spawnDat = _MysteriousBoxSpawns.get(npcId);
    	if(spawnDat != null)
    	{
        	_AllMobs.add(spawnDat.doSpawn());
        	spawnDat.stopRespawn();
    	}
    }

    public void SpawnMonster(int npcId)
    {
    	if (!IsAttackTime()) return;

    	FastList<L2Spawn> MonsterList;
    	List<L2SepulcherMonsterInstance> Mobs = new FastList<L2SepulcherMonsterInstance>();
    	L2Spawn KeyBoxMobSpawn;
    	
    	if(Rnd.get(2) == 0)
    	{
    		MonsterList = (FastList<L2Spawn>)_PhysicalMonsters.get(npcId);
    	}
    	else
    	{
    		MonsterList = (FastList<L2Spawn>)_MagicalMonsters.get(npcId);
    	}
    	
    	if(MonsterList != null)
    	{
    		boolean SpawnKeyBoxMob = false;
    		boolean SpawnedKeyBoxMob = false;
    		
        	for (L2Spawn spawnDat:MonsterList)
        	{
        		if(SpawnedKeyBoxMob)
        		{
        			SpawnKeyBoxMob = false;
        		}
        		else
        		{
            		switch(npcId)
            		{
            		    case 31469:
            		    case 31474:
            		    case 31479:
            		    case 31484:
            		    	if(Rnd.get(48) == 0)
            		    	{
            		    		SpawnKeyBoxMob = true;
            		    		_log.info("FourSepulchersManager.SpawnMonster: Set to spawn Church of Viscount Key Mob.");
            		    	}
            		    	break;
        		    	default:
        		    		SpawnKeyBoxMob = false;
            		}
        		}

        		L2SepulcherMonsterInstance mob = null;

        		if(SpawnKeyBoxMob)
        		{
                    try
                    {
            			L2NpcTemplate template = NpcTable.getInstance().getTemplate(18149);
                        if (template != null)
                        {
                        	KeyBoxMobSpawn = new L2Spawn(template);
                        	KeyBoxMobSpawn.setAmount(1);
                        	KeyBoxMobSpawn.setLocx(spawnDat.getLocx());
                        	KeyBoxMobSpawn.setLocy(spawnDat.getLocy());
                        	KeyBoxMobSpawn.setLocz(spawnDat.getLocz());
                        	KeyBoxMobSpawn.setHeading(spawnDat.getHeading());
                        	KeyBoxMobSpawn.setRespawnDelay(3600);
                        	SpawnTable.getInstance().addNewSpawn(KeyBoxMobSpawn, false);
                    		mob = (L2SepulcherMonsterInstance)KeyBoxMobSpawn.doSpawn();
                    		KeyBoxMobSpawn.stopRespawn();
                        }
                        else {
                            _log.warning("FourSepulchersManager.SpawnMonster: Data missing in NPC table for ID: 18149");
                        }
                    }
                    catch (Exception e)
                    {
                        _log.warning("FourSepulchersManager.SpawnMonster: Spawn could not be initialized: " + e);
                    }
        			
        			SpawnedKeyBoxMob = true;
        		}
        		else
        		{
            		mob = (L2SepulcherMonsterInstance)spawnDat.doSpawn();
                	spawnDat.stopRespawn();
        		}

        		if(mob != null)
        		{
            		mob.MysteriousBoxId = npcId;
            		switch(npcId)
            		{
            		    case 31469:
            		    case 31474:
            		    case 31479:
            		    case 31484:
            		    case 31472:
            		    case 31477:
            		    case 31482:
            		    case 31487:
            		    	Mobs.add(mob);
            		}
            		_AllMobs.add(mob);
        		}
        	}

        	switch(npcId)
    		{
    		    case 31469:
    		    case 31474:
    		    case 31479:
    		    case 31484:
    		    	_ViscountMobs.put(npcId, Mobs);
    		    	break;
    		    
    		    case 31472:
    		    case 31477:
    		    case 31482:
    		    case 31487:
    		    	_DukeMobs.put(npcId, Mobs);
    		    	break;
    		}
    	}
    }

    public synchronized boolean IsViscountMobsAnnihilated(int npcId)
    {
    	FastList<L2SepulcherMonsterInstance> Mobs =
    		(FastList<L2SepulcherMonsterInstance>)_ViscountMobs.get(npcId);
    	
    	if(Mobs == null) return true;
    	
    	for(L2SepulcherMonsterInstance mob:Mobs)
    	{
    		if(!mob.isDead()) return false;
    	}
    	
    	return true;
    }

    public synchronized boolean IsDukeMobsAnnihilated(int npcId)
    {
    	FastList<L2SepulcherMonsterInstance> Mobs =
    		(FastList<L2SepulcherMonsterInstance>)_DukeMobs.get(npcId);
    	
    	if(Mobs == null) return true;
    	
    	for(L2SepulcherMonsterInstance mob:Mobs)
    	{
    		if(!mob.isDead()) return false;
    	}
    	
    	return true;
    }

    public void SpawnKeyBox(L2NpcInstance activeChar)
    {
    	if (!IsAttackTime()) return;

    	L2Spawn	spawnDat = _KeyBoxSpawns.get(activeChar.getNpcId());

    	if(spawnDat != null)
    	{
        	spawnDat.setAmount(1);
        	spawnDat.setLocx(activeChar.getX());
        	spawnDat.setLocy(activeChar.getY());
        	spawnDat.setLocz(activeChar.getZ());
        	spawnDat.setHeading(activeChar.getHeading());
        	spawnDat.setRespawnDelay(3600);
        	_AllMobs.add(spawnDat.doSpawn());
        	spawnDat.stopRespawn();
    		
    	}
    }
    
    public void SpawnExecutionerOfHalisha(L2NpcInstance activeChar)
    {
    	if (!IsAttackTime()) return;

    	L2Spawn spawnDat = _ExecutionerSpawns.get(activeChar.getNpcId());

    	if(spawnDat != null)
    	{
    		spawnDat.setAmount(1);
        	spawnDat.setLocx(activeChar.getX());
        	spawnDat.setLocy(activeChar.getY());
        	spawnDat.setLocz(activeChar.getZ());
        	spawnDat.setHeading(activeChar.getHeading());
        	spawnDat.setRespawnDelay(3600);
        	_AllMobs.add(spawnDat.doSpawn());
        	spawnDat.stopRespawn();
    	}
    }

    public void SpawnArchonOfHalisha(int npcId)
    {
    	if (!IsAttackTime()) return;
    	
    	if(_ArchonSpawned.get(npcId)) return;

    	FastList<L2Spawn> MonsterList = (FastList<L2Spawn>)_DukeFinalMobs.get(npcId);
    	
    	if(MonsterList != null)
    	{
        	for (L2Spawn spawnDat:MonsterList)
        	{
        		L2SepulcherMonsterInstance mob = (L2SepulcherMonsterInstance)spawnDat.doSpawn();
            	spawnDat.stopRespawn();

        		if(mob != null)
        		{
            		mob.MysteriousBoxId = npcId;
            		_AllMobs.add(mob);
        		}
    		}
        	_ArchonSpawned.put(npcId, true);
    	}
    }

    public void SpawnEmperorsGraveNpc(int npcId)
    {
    	if (!IsAttackTime()) return;
    	
    	FastList<L2Spawn> MonsterList = (FastList<L2Spawn>)_EmperorsGraveNpcs.get(npcId);
    	
    	if(MonsterList != null)
    	{
        	for (L2Spawn spawnDat:MonsterList)
        	{
        		_AllMobs.add(spawnDat.doSpawn());
            	spawnDat.stopRespawn();
    		}
    	}
    }

    protected void LocationShadowSpawns()
    {
    	int locNo = Rnd.get(4);
    	_log.info("FourSepulchersManager.LocationShadowSpawns: Location index is " + locNo + ".");
    	final int[] gateKeeper = {31929,31934,31939,31944};
    	
    	L2Spawn spawnDat;

        for(int i=0;i<=3;i++)
    	{
        	int keyNpcId = gateKeeper[i];
        	spawnDat = _ShadowSpawns.get(keyNpcId);
        	spawnDat.setLocx(_ShadowSpawnLoc[locNo][i][1]);
        	spawnDat.setLocy(_ShadowSpawnLoc[locNo][i][2]);
        	spawnDat.setLocz(_ShadowSpawnLoc[locNo][i][3]);
        	spawnDat.setHeading(_ShadowSpawnLoc[locNo][i][4]);
        	_ShadowSpawns.put(keyNpcId,spawnDat);
    	}
    }

    public void SpawnShadow(int npcId)
    {
    	if (!IsAttackTime()) return;

    	L2Spawn spawnDat = _ShadowSpawns.get(npcId);
    	if(spawnDat != null)
    	{
    		L2SepulcherMonsterInstance mob = (L2SepulcherMonsterInstance)spawnDat.doSpawn();
        	spawnDat.stopRespawn();

    		if(mob != null)
    		{
        		mob.MysteriousBoxId = npcId;
        		_AllMobs.add(mob);
    		}
    	}
    }

    public void checkAnnihilated(L2PcInstance player)
    {
    	if(IsPartyAnnihilated(player))
    	{
    		_OnPartyAnnihilatedTask =
				ThreadPoolManager.getInstance().scheduleGeneral(new OnPartyAnnihilatedTask(player),5000);    			
    	}
    }
    
    public synchronized boolean IsPartyAnnihilated(L2PcInstance player)
    {
		if(player.getParty() != null)
		{
			for(L2PcInstance mem:player.getParty().getPartyMembers())
			{
				if(!mem.isDead())
				{
					return false;
				}
			}
			return true;
		}
		else
		{
			return true;
		}
    }
    
    public synchronized void OnPartyAnnihilated(L2PcInstance player)
    {
		if(player.getParty() != null)
		{
			for(L2PcInstance mem:player.getParty().getPartyMembers())
			{
				if(!mem.isDead()) break;
        		int driftX = Rnd.get(-80,80);
        		int driftY = Rnd.get(-80,80);
        		mem.teleToLocation(169589 + driftX,-90493 + driftY,-2914);
			}
		}
		else
		{
    		int driftX = Rnd.get(-80,80);
    		int driftY = Rnd.get(-80,80);
    		player.teleToLocation(169589 + driftX,-90493 + driftY,-2914);
    	}
    }
    
    public void DeleteAllMobs()
    {
    	_log.info("FourSepulchersManager.DeleteAllMobs: Try to delete " + _AllMobs.size() + " monsters.");

    	int delCnt = 0;
        for(L2NpcInstance mob : _AllMobs)
        {
			try
            {
				mob.getSpawn().stopRespawn();
				mob.deleteMe();
                delCnt++;
            }
            catch(Exception e)
            {
                _log.warning(e.getMessage());
            }
        }
        _AllMobs.clear();
    	_log.info("FourSepulchersManager.DeleteAllMobs: Deleted " + delCnt + " monsters.");
    }
    
    protected void CloseAllDoors()
    {
    	for(int doorId:_HallGateKeepers.values())
    	{
            try
            {
            	DoorTable.getInstance().getDoor(doorId).closeMe();
            }
            catch (Exception e)
            {
                _log.warning(e.getMessage());
            }
    	}
    }

    protected class ChaneEntryTime implements Runnable
    {
        public void run()
        {

            _log.info("FourSepulchersManager:In Entry Time");

            _InEntryTime = true;
            _InWarmUpTime = false;
            _InAttackTime = false;
            _InCoolDownTime = false;
            
            _ChaneWarmUpTimeTask =
                ThreadPoolManager.getInstance().scheduleGeneral(new ChaneWarmUpTime(),Config.FS_TIME_ENTRY * 60000);

            if(_ChangeEntryTimeTask != null)
            {
            	_ChangeEntryTimeTask.cancel(true);
            	_ChangeEntryTimeTask = null;
            }
        }
    }
    
    protected class ChaneWarmUpTime implements Runnable
    {
        public void run()
        {
            _log.info("FourSepulchersManager:In Warm-Up Time");

            _InEntryTime = true;
            _InWarmUpTime = false;
            _InAttackTime = false;
            _InCoolDownTime = false;
            
            _ChangeAttackTimeTask =
                ThreadPoolManager.getInstance().scheduleGeneral(new ChangeAttackTime(),Config.FS_TIME_WARMUP * 60000);

            if(_ChaneWarmUpTimeTask != null)
            {
            	_ChaneWarmUpTimeTask.cancel(true);
            	_ChaneWarmUpTimeTask = null;
            }
        }
    }
    
    protected class ChangeAttackTime implements Runnable
    {
        public void run()
        {

            _log.info("FourSepulchersManager:In Attack Time");

            _InEntryTime = false;
            _InWarmUpTime = false;
            _InAttackTime = true;
            _InCoolDownTime = false;
            
            LocationShadowSpawns();
            
            SpawnMysteriousBox(31921);
            SpawnMysteriousBox(31922);
            SpawnMysteriousBox(31923);
            SpawnMysteriousBox(31924);
            
            _ChangeCoolDownTimeTask =
                ThreadPoolManager.getInstance().scheduleGeneral(new ChangeCoolDownTime(),Config.FS_TIME_ATTACK * 60000);

            if(_ChangeAttackTimeTask != null)
            {
            	_ChangeAttackTimeTask.cancel(true);
            	_ChangeAttackTimeTask = null;
            }
        }
    }

    protected class ChangeCoolDownTime implements Runnable
    {
        
        public void run()
        {

            _log.info("FourSepulchersManager:In Cool-Down Time");

            _InEntryTime = false;
            _InWarmUpTime = false;
            _InAttackTime = false;
            _InCoolDownTime = true;

            for(L2PcInstance player :L2World.getInstance().getAllPlayers())
            {
            	if (BossZoneManager.getInstance().checkIfInZone("FourSepulcher", player) &&
            		(player.getZ() >= -7250 && player.getZ() <= -6841) &&
            		!player.isGM())
            	{
            		int driftX = Rnd.get(-80,80);
            		int driftY = Rnd.get(-80,80);
            		player.teleToLocation(169589 + driftX,-90493 + driftY,-2914);
            	}
            }
            
            DeleteAllMobs();

            CloseAllDoors();
            
            _HallInUse.clear();
            _HallInUse.put(31921,false);
            _HallInUse.put(31922,false);
            _HallInUse.put(31923,false);
            _HallInUse.put(31924,false);

            if(_ArchonSpawned.size() != 0)
            {
                Set<Integer> npcIdSet = _ArchonSpawned.keySet();
                for(int npcId:npcIdSet)
                {
                	_ArchonSpawned.put(npcId, false);
                }
            }
            
            _ChangeEntryTimeTask = 
                ThreadPoolManager.getInstance().scheduleGeneral(new ChaneEntryTime(),Config.FS_TIME_COOLDOWN * 60000);

            if(_ChangeCoolDownTimeTask != null)
            {
            	_ChangeCoolDownTimeTask.cancel(true);
            	_ChangeCoolDownTimeTask = null;
            }
        }
    }

	private class OnPartyAnnihilatedTask implements Runnable
	{
		L2PcInstance _player;
		
		public OnPartyAnnihilatedTask(L2PcInstance player)
		{
			_player = player;
		}
		
		public void run()
		{
			OnPartyAnnihilated(_player);

            if(_OnPartyAnnihilatedTask != null)
            {
            	_OnPartyAnnihilatedTask.cancel(true);
            	_OnPartyAnnihilatedTask = null;
            }
			
		}
	}
/* ================================================================================================
            //入場時段備用訊息 In Entry Time (55分鐘的時候) 
            //sendMessageToAll("征服者陵墓管理員", "從現在起可以進入陵墓內。");
            //sendMessageToAll("征服者陵墓管理員", "若將手放在四大陵墓的石像上，就可以進入陵墓內。");
            //sendMessageToAll("統治者陵墓管理員", "從現在起可以進入陵墓內。");
            //sendMessageToAll("統治者陵墓管理員", "若將手放在四大陵墓的石像上，就可以進入陵墓內。");
            //sendMessageToAll("大賢者陵墓管理員", "從現在起可以進入陵墓內。");
            //sendMessageToAll("大賢者陵墓管理員", "若將手放在四大陵墓的石像上，就可以進入陵墓內。");
            //sendMessageToAll("審判者陵墓管理員", "從現在起可以進入陵墓內。");
            //sendMessageToAll("審判者陵墓管理員", "若將手放在四大陵墓的石像上，就可以進入陵墓內。");

            //攻擊時段備用訊息 In Attack Time
            //sendMessageToAll("征服者陵墓管理員", "現在已經過了5分鐘。");
            //sendMessageToAll("征服者陵墓管理員", "現在已經過了10分鐘。");
            //sendMessageToAll("征服者陵墓管理員", "現在已經過了15分鐘。");
            //sendMessageToAll("征服者陵墓管理員", "現在已經過了20分鐘。");
            //sendMessageToAll("征服者陵墓管理員", "現在已經過了25分鐘。");
            //sendMessageToAll("征服者陵墓管理員", "現在已經過了30分鐘。");
            //sendMessageToAll("征服者陵墓管理員", "現在已經過了35分鐘。");
            //sendMessageToAll("征服者陵墓管理員", "現在已經過了40分鐘。");
            //sendMessageToAll("征服者陵墓管理員", "現在已經過了45分鐘。");

            //結束時段備用訊息 Cool-Down Time (50分鐘的時候) 
            //sendMessageToAll("征服者陵墓管理員", "挑戰已結束，稍後將自動進行傳送");

/* ================================================================================================
	
/*
// addon ==========================================================================================
	public void sendMessageToAll(String senderName, String message)
    {
        if (
        		_ConquerorsSepulcherManager == null //|| 
        		//_EmperorsSepulcherManager == null || 
        		//_GreatSagesSepulcherManager == null || 
        		//_JudgesSepulcherManager == null 
           )
            return;

        CreatureSay cs = new CreatureSay(_ConquerorsSepulcherManager.getObjectId(), 1, senderName, message);
        _ConquerorsSepulcherManager.broadcastPacket(cs);

        //cs = new CreatureSay(_EmperorsSepulcherManager.getObjectId(), 1, senderName, message);
        //_EmperorsSepulcherManager.broadcastPacket(cs);

        //cs = new CreatureSay(_GreatSagesSepulcherManager.getObjectId(), 1, senderName, message);
        //_GreatSagesSepulcherManager.broadcastPacket(cs);

        //cs = new CreatureSay(_JudgesSepulcherManager.getObjectId(), 1, senderName, message);
        //_JudgesSepulcherManager.broadcastPacket(cs);

    }
    public void npcSpawned(L2NpcInstance npc)
    {
        if (npc == null)
            return;

        int npcId = npc.getNpcId();

        // If the spawned NPC ID matches the ones we need, assign their instances.
        if (npcId == 31921)
        {
            if (Config.DEBUG)
                _log.config("ConquerorsSepulcherManager: Instance found for NPC ID 31921 (" + npc.getObjectId() + ").");

            _ConquerorsSepulcherManager = npc;
        }
        if (npcId == 31922)
        {
            if (Config.DEBUG)
                _log.config("EmperorsSepulcherManager: Instance found for NPC ID 31922 (" + npc.getObjectId() + ").");

            _EmperorsSepulcherManager = npc;
        }

        if (npcId == 31923)
        {
            if (Config.DEBUG)
                _log.config("GreatSagesSepulcherManager: Instance found for NPC ID 31923 (" + npc.getObjectId() + ").");

            _GreatSagesSepulcherManager = npc;
        }

        if (npcId == 31924)
        {
            if (Config.DEBUG)
                _log.config("JudgesSepulcherManager: Instance found for NPC ID 31924 (" + npc.getObjectId() + ").");

            _JudgesSepulcherManager = npc;
        }
    }
	// addon end=======================================================================================
*/

}
