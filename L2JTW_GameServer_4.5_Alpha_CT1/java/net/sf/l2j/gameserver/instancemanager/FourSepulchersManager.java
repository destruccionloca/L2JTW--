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
 * ４大霊廟マネージ?ー
 * @version $Revision: $ $Date: $
 * @author  sandman
 */
public class FourSepulchersManager
{
    // ?ガー
    protected static Logger _log = Logger.getLogger(FourSepulchersManager.class.getName());

    // イ?スタ?ス
    private static FourSepulchersManager _Instance;

    // クエスト「４つの杯」
    private String _QuestId = "620_FourGoblets";
    // 霊廟の通行証
    private int _EntrancePass = 7075;
    private int _UsedEntrancePass = 7261;
    // 会堂の鍵
    private final int _HallsKey = 7260;
	// 古いブ?ーチ
    private int _OldBrooch = 7262;

    // 状態遷移管?
    // 参加受付?間かどうか？
    protected boolean _InEntryTime = false;
    // ウォー?アップかどうか？
    protected boolean _InWarmUpTime = false;
    // 霊廟攻略?間かどうか？
    protected boolean _InAttackTime = false;
    // クー?ダウ??かどうか？
    protected boolean _InCoolDownTime = false;

    // 状態遷移用タスク
    protected ScheduledFuture<?> _ChangeCoolDownTimeTask = null;
    protected ScheduledFuture<?> _ChangeEntryTimeTask = null;
    protected ScheduledFuture<?> _ChaneWarmUpTimeTask = null;
    protected ScheduledFuture<?> _ChangeAttackTimeTask = null;
    protected ScheduledFuture<?> _OnPartyAnnihilatedTask = null;



    // 会堂門番とドアの組み?わせ(<門番のID、ドアのID>)
    protected static Map<Integer,Integer> _HallGateKeepers = new FastMap<Integer,Integer>();

    // 男爵の会堂テ?ポート座標[x,y,z]
    private int[][] _StartHallSpawn =
	{
		{181632,-85587,-7218},     // 征?者の霊廟
		{179963,-88978,-7218},     // 支配者の霊廟
		{173217,-86132,-7218},     // 大賢者の霊廟
		{175608,-82296,-7218}      // 審判者の霊廟
	};

    // 霊廟管?人と男爵の会堂テ?ポート座標の組み?わせ(<管?人のID、座標[x,y,z]>)
    protected static Map<Integer,int[]> _StartHallSpawns = new FastMap<Integer,int[]>();

    // ハ?シ?シ?ドーの出現座標[出現パター?][霊廟][npcId,x,y,z,heading]
    private int[][][] _ShadowSpawnLoc =
	{
		// 出現パター?１
		{
			{25339,191231,-85574,-7216,33380},	// 征?者
			{25349,189534,-88969,-7216,32768},	// 支配者
			{25346,173195,-76560,-7215,49277},	// 大賢者
			{25342,175591,-72744,-7215,49317}	// 審判者
		},
		// 出現パター?２
		{
			{25342,191231,-85574,-7216,33380},	// 征?者
			{25339,189534,-88969,-7216,32768},	// 支配者
			{25349,173195,-76560,-7215,49277},	// 大賢者　
			{25346,175591,-72744,-7215,49317}	// 審判者
		},
		// 出現パター?３
		{
			{25346,191231,-85574,-7216,33380},	// 征?者
			{25342,189534,-88969,-7216,32768},	// 支配者
			{25339,173195,-76560,-7215,49277},	// 大賢者
			{25349,175591,-72744,-7215,49317}	// 審判者
		},
		// 出現パター?４
		{
			{25349,191231,-85574,-7216,33380},	// 征?者
			{25346,189534,-88969,-7216,32768},	// 支配者
			{25342,173195,-76560,-7215,49277},	// 大賢者
			{25339,175591,-72744,-7215,49317}	// 審判者
		},
	};

    // 公爵の門番とハ?シ?シ?ドーの組み?わせ
    protected Map<Integer,L2Spawn> _ShadowSpawns = new FastMap<Integer,L2Spawn>();

    // 霊廟管?人と霊廟の?用状態の組み?わせ(<管?人のID、?用状態(true = 誰かが使っている)>)
    protected static Map<Integer,Boolean> _HallInUse = new FastMap<Integer,Boolean>();

    // 霊廟管?人と挑戦者パーティ?ーダーの組み?わせ
    protected Map<Integer,L2PcInstance> _Challengers = new FastMap<Integer,L2PcInstance>();

    // 管?人・会堂門番と謎の?のspawnデータの組み?わせ
    protected Map<Integer,L2Spawn> _MysteriousBoxSpawns = new FastMap<Integer,L2Spawn>();

    // 謎の?と出現する??スターのspawnデータの組み?わせ（物?系）
    protected List<L2Spawn> _PhysicalSpawns;
    protected Map<Integer,List<L2Spawn>> _PhysicalMonsters = new FastMap<Integer,List<L2Spawn>>();
    // 謎の?と出現する??スターのspawnデータの組み?わせ（?法系）
    protected List<L2Spawn> _MagicalSpawns;
    protected Map<Integer,List<L2Spawn>> _MagicalMonsters = new FastMap<Integer,List<L2Spawn>>();
    // 公爵の会堂の謎の?と、殲滅後に再出現する??スターの組み?わせ
    protected List<L2Spawn> _DukeFinalSpawns;
    protected Map<Integer,List<L2Spawn>> _DukeFinalMobs = new FastMap<Integer,List<L2Spawn>>();
    protected Map<Integer,Boolean> _ArchonSpawned = new FastMap<Integer,Boolean>();
    // ハ?シ?シ?ドー討伐後に皇帝の墓?で出現するNPCの組み?わせ
    protected List<L2Spawn> _EmperorsGraveSpawns;
    protected Map<Integer,List<L2Spawn>> _EmperorsGraveNpcs = new FastMap<Integer,List<L2Spawn>>();

    // 子爵の会堂の謎の?と、出現した??スターの組み?わせ
    protected Map<Integer,List<L2SepulcherMonsterInstance>> _ViscountMobs =
    	new FastMap<Integer,List<L2SepulcherMonsterInstance>>();

    // 公爵の会堂の謎の?と、出現した??スターの組み?わせ
    protected Map<Integer,List<L2SepulcherMonsterInstance>> _DukeMobs =
    	new FastMap<Integer,List<L2SepulcherMonsterInstance>>();

    // 鍵の?を出現させる??スターと鍵の?の組み?わせ<??スターのNpcId,鍵の?のNpcId>
    protected Map<Integer,Integer> _KeyBoxNpc = new FastMap<Integer,Integer>();
    protected Map<Integer,L2Spawn> _KeyBoxSpawns = new FastMap<Integer,L2Spawn>();

    // ケープゴートとハ?シ?の執行者のspawnデータの組み?わせ
    protected Map<Integer,Integer> _Victim = new FastMap<Integer,Integer>();
    protected Map<Integer,L2Spawn> _ExecutionerSpawns = new FastMap<Integer,L2Spawn>();

    // 霊廟攻略?間に霊廟内に出現した全ての??スター
    protected List<L2NpcInstance> _AllMobs = new FastList<L2NpcInstance>();

    // コ?スト?クター
    public FourSepulchersManager()
    {
    }

    // イ?スタ?ス関?
    public static final FourSepulchersManager getInstance()
    {
        if (_Instance == null)
        {
            _Instance = new FourSepulchersManager();
        }
        return _Instance;
    }

    // ?期化
    public void init()
    {
        _log.info("FourSepulchersManager:Init Four-Sepulchers Manager.");

        // タスクをすべてキ??セ?
        if(_ChangeCoolDownTimeTask != null) _ChangeCoolDownTimeTask.cancel(true);
        if(_ChangeEntryTimeTask != null) _ChangeEntryTimeTask.cancel(true);
        if(_ChaneWarmUpTimeTask != null) _ChaneWarmUpTimeTask.cancel(true);
        if(_ChangeAttackTimeTask != null) _ChangeAttackTimeTask.cancel(true);

        _ChangeCoolDownTimeTask = null;
        _ChangeEntryTimeTask = null;
        _ChaneWarmUpTimeTask = null;
        _ChangeAttackTimeTask = null;

        // フ?グを変更する(最?のクー?ダウ?が始まるまではどの?間帯でもない)
        _InEntryTime = false;
        _InWarmUpTime = false;
        _InAttackTime = false;
        _InCoolDownTime = false;

        // 固定情報を?期化
        initFixedInfo();

        // ??スター配置の?期化
        LoadMysteriousBox();
        InitKeyBoxSpawns();
        LoadPhysicalMonsters();
        LoadMagicalMonsters();
        InitLocationShadowSpawns();
        InitExecutionerSpawns();
        LoadDukeMonsters();
        LoadEmperorsGraveMonsters();

        // 最?のクー?ダウ??間に移行するタスクを仕?む
        _ChangeCoolDownTimeTask =
            ThreadPoolManager.getInstance().scheduleGeneral(new ChangeCoolDownTime(),Config.FS_TIME_ATTACK * 60000);

    }

    // 固定情報の?期化
    protected void initFixedInfo()
    {
        // 霊廟管?人と男爵の会堂テ?ポート座標の組み?わせを作成
        _StartHallSpawns.clear();
        _StartHallSpawns.put(31921,_StartHallSpawn[0]);    // 征?者の霊廟
        _StartHallSpawns.put(31922,_StartHallSpawn[1]);    // 支配者の霊廟
        _StartHallSpawns.put(31923,_StartHallSpawn[2]);    // 大賢者の霊廟
        _StartHallSpawns.put(31924,_StartHallSpawn[3]);    // 審判者の霊廟

        // 会堂門番とドアの組み?わせを作成
        _HallGateKeepers.clear();
        _HallGateKeepers.put(31925, 25150012);  // 征?者の霊廟：男爵の会堂門番
        _HallGateKeepers.put(31926, 25150013);  // 征?者の霊廟：子爵の会堂門番
        _HallGateKeepers.put(31927, 25150014);  // 征?者の霊廟：?爵の会堂門番
        _HallGateKeepers.put(31928, 25150015);  // 征?者の霊廟：侯爵の会堂門番
        _HallGateKeepers.put(31929, 25150016);  // 征?者の霊廟：公爵の会堂門番
        _HallGateKeepers.put(31930, 25150002);  // 支配者の霊廟：男爵の会堂門番
        _HallGateKeepers.put(31931, 25150003);  // 支配者の霊廟：子爵の会堂門番
        _HallGateKeepers.put(31932, 25150004);  // 支配者の霊廟：?爵の会堂門番
        _HallGateKeepers.put(31933, 25150005);  // 支配者の霊廟：侯爵の会堂門番
        _HallGateKeepers.put(31934, 25150006);  // 支配者の霊廟：公爵の会堂門番
        _HallGateKeepers.put(31935, 25150032);  // 大賢者の霊廟：男爵の会堂門番
        _HallGateKeepers.put(31936, 25150033);  // 大賢者の霊廟：子爵の会堂門番
        _HallGateKeepers.put(31937, 25150034);  // 大賢者の霊廟：?爵の会堂門番
        _HallGateKeepers.put(31938, 25150035);  // 大賢者の霊廟：侯爵の会堂門番
        _HallGateKeepers.put(31939, 25150036);  // 大賢者の霊廟：公爵の会堂門番
        _HallGateKeepers.put(31940, 25150022);  // 審判者の霊廟：男爵の会堂門番
        _HallGateKeepers.put(31941, 25150023);  // 審判者の霊廟：子爵の会堂門番
        _HallGateKeepers.put(31942, 25150024);  // 審判者の霊廟：?爵の会堂門番
        _HallGateKeepers.put(31943, 25150025);  // 審判者の霊廟：侯爵の会堂門番
        _HallGateKeepers.put(31944, 25150026);  // 審判者の霊廟：公爵の会堂門番

        // 鍵の?を出現させる??スターと鍵の?の組み?わせ
        _KeyBoxNpc.clear();
        _KeyBoxNpc.put(18120,31455);	//ハ?シ?の?官,鍵の?
        _KeyBoxNpc.put(18121,31455);	//ハ?シ?の?官,鍵の?
        _KeyBoxNpc.put(18122,31455);	//ハ?シ?の?官,鍵の?
        _KeyBoxNpc.put(18123,31455);	//ハ?シ?の?官,鍵の?
        _KeyBoxNpc.put(18124,31456);	//ハ?シ?の?官,鍵の?
        _KeyBoxNpc.put(18125,31456);	//ハ?シ?の?官,鍵の?
        _KeyBoxNpc.put(18126,31456);	//ハ?シ?の?官,鍵の?
        _KeyBoxNpc.put(18127,31456);	//ハ?シ?の?官,鍵の?
        _KeyBoxNpc.put(18128,31457);	//ハ?シ?の?官,鍵の?
        _KeyBoxNpc.put(18129,31457);	//ハ?シ?の?官,鍵の?
        _KeyBoxNpc.put(18130,31457);	//ハ?シ?の?官,鍵の?
        _KeyBoxNpc.put(18131,31457);	//ハ?シ?の?官,鍵の?
        _KeyBoxNpc.put(18149,31458);	//墓地の甲?,鍵の?
        _KeyBoxNpc.put(18150,31459);	//ケープゴート,鍵の?
        _KeyBoxNpc.put(18151,31459);	//ケープゴート,鍵の?
        _KeyBoxNpc.put(18152,31459);	//ケープゴート,鍵の?
        _KeyBoxNpc.put(18153,31459);	//ケープゴート,鍵の?
        _KeyBoxNpc.put(18154,31460);	//ケープゴート,鍵の?
        _KeyBoxNpc.put(18155,31460);	//ケープゴート,鍵の?
        _KeyBoxNpc.put(18156,31460);	//ケープゴート,鍵の?
        _KeyBoxNpc.put(18157,31460);	//ケープゴート,鍵の?
        _KeyBoxNpc.put(18158,31461);	//ハ?シ?の執行者,鍵の?
        _KeyBoxNpc.put(18159,31461);	//ハ?シ?の執行者,鍵の?
        _KeyBoxNpc.put(18160,31461);	//ハ?シ?の執行者,鍵の?
        _KeyBoxNpc.put(18161,31461);	//ハ?シ?の執行者,鍵の?
        _KeyBoxNpc.put(18162,31462);	//ハ?シ?の執行者,鍵の?
        _KeyBoxNpc.put(18163,31462);	//ハ?シ?の執行者,鍵の?
        _KeyBoxNpc.put(18164,31462);	//ハ?シ?の執行者,鍵の?
        _KeyBoxNpc.put(18165,31462);	//ハ?シ?の執行者,鍵の?
        _KeyBoxNpc.put(18183,31463);	//ハ?シ?の監督官,鍵の?
        _KeyBoxNpc.put(18184,31464);	//ハ?シ?の監督官,鍵の?
        _KeyBoxNpc.put(18212,31465);	//ハ?シ? アドミニスト?ーター,鍵の?
        _KeyBoxNpc.put(18213,31465);	//ハ?シ? アドミニスト?ーター,鍵の?
        _KeyBoxNpc.put(18214,31465);	//ハ?シ? アドミニスト?ーター,鍵の?
        _KeyBoxNpc.put(18215,31465);	//ハ?シ? アドミニスト?ーター,鍵の?
        _KeyBoxNpc.put(18216,31466);	//ハ?シ? アドミニスト?ーター,鍵の?
        _KeyBoxNpc.put(18217,31466);	//ハ?シ? アドミニスト?ーター,鍵の?
        _KeyBoxNpc.put(18218,31466);	//ハ?シ? アドミニスト?ーター,鍵の?
        _KeyBoxNpc.put(18219,31466);	//ハ?シ? アドミニスト?ーター,鍵の?

        // ケープコードとハ?シ?の執行者の組み?わせ
        _Victim.clear();
        _Victim.put(18150,18158);	// ケープゴート,ハ?シ?の執行者
        _Victim.put(18151,18159);	// ケープゴート,ハ?シ?の執行者
        _Victim.put(18152,18160);	// ケープゴート,ハ?シ?の執行者
        _Victim.put(18153,18161);	// ケープゴート,ハ?シ?の執行者
        _Victim.put(18154,18162);	// ケープゴート,ハ?シ?の執行者
        _Victim.put(18155,18163);	// ケープゴート,ハ?シ?の執行者
        _Victim.put(18156,18164);	// ケープゴート,ハ?シ?の執行者
        _Victim.put(18157,18165);	// ケープゴート,ハ?シ?の執行者

    }

    // 謎の?のspawnデータの読み?み
    private void LoadMysteriousBox()
    {
        java.sql.Connection con = null;

        // 謎の?の出現座標情報をク?ア
        _MysteriousBoxSpawns.clear();

        // 謎の?の出現座標情報を作成
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

    // 鍵の?のspawnデータの作成
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

    // 物?タイプのspawnデータの読み?み
    private void LoadPhysicalMonsters()
    {

    	// ??スター出現?ストを?期化
    	_PhysicalMonsters.clear();

    	// 読み?まれた出現?ストの?
    	int loaded = 0;
    	java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();

            // 出現のきっかけとなるNPCの?ープ
            PreparedStatement statement1 = con.prepareStatement("SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
            statement1.setInt(1, 1);
            ResultSet rset1 = statement1.executeQuery();
            while (rset1.next())
            {
            	// 出現する??スターの座標?スト作成の?ープ
            	int keyNpcId = rset1.getInt("key_npc_id");

                PreparedStatement statement2 = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
                statement2.setInt(1, keyNpcId);
                statement2.setInt(2, 1);
                ResultSet rset2 = statement2.executeQuery();

                L2Spawn spawnDat;
                L2NpcTemplate template1;

                // ??スター?ストを?期化
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

    // ?法タイプのspawnデータの読み?み
    private void LoadMagicalMonsters()
    {

    	// ??スター出現?ストを?期化
    	_MagicalMonsters.clear();

    	// 読み?まれた出現?ストの?
    	int loaded = 0;
    	java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();

            // 出現のきっかけとなるNPCの?ープ
            PreparedStatement statement1 = con.prepareStatement("SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
            statement1.setInt(1, 2);
            ResultSet rset1 = statement1.executeQuery();
            while (rset1.next())
            {
            	// 出現する??スターの座標?スト作成の?ープ
            	int keyNpcId = rset1.getInt("key_npc_id");

                PreparedStatement statement2 = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
                statement2.setInt(1, keyNpcId);
                statement2.setInt(2, 2);
                ResultSet rset2 = statement2.executeQuery();

                L2Spawn spawnDat;
                L2NpcTemplate template1;

                // ??スター?ストを?期化
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

    // 公爵の会堂の殲滅後に再出現する??スターのspawnデータの読み?み
    private void LoadDukeMonsters()
    {

    	// ??スター出現?ストを?期化
    	_DukeFinalMobs.clear();
    	_ArchonSpawned.clear();

    	// 読み?まれた出現?ストの?
    	int loaded = 0;
    	java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();

            // 出現のきっかけとなるNPCの?ープ
            PreparedStatement statement1 = con.prepareStatement("SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
            statement1.setInt(1, 5);
            ResultSet rset1 = statement1.executeQuery();
            while (rset1.next())
            {
            	// 出現する??スターの座標?スト作成の?ープ
            	int keyNpcId = rset1.getInt("key_npc_id");

                PreparedStatement statement2 = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
                statement2.setInt(1, keyNpcId);
                statement2.setInt(2, 5);
                ResultSet rset2 = statement2.executeQuery();

                L2Spawn spawnDat;
                L2NpcTemplate template1;

                // ??スター?ストを?期化
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

    // ウィゴットの幽霊とハ?シ?の宝?のspawnデータの読み?み
    private void LoadEmperorsGraveMonsters()
    {

    	// ??スター出現?ストを?期化
    	_EmperorsGraveNpcs.clear();

    	// 読み?まれた出現?ストの?
    	int loaded = 0;
    	java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();

            // 出現のきっかけとなるNPCの?ープ
            PreparedStatement statement1 = con.prepareStatement("SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
            statement1.setInt(1, 6);
            ResultSet rset1 = statement1.executeQuery();
            while (rset1.next())
            {
            	// 出現する??スターの座標?スト作成の?ープ
            	int keyNpcId = rset1.getInt("key_npc_id");

                PreparedStatement statement2 = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
                statement2.setInt(1, keyNpcId);
                statement2.setInt(2, 6);
                ResultSet rset2 = statement2.executeQuery();

                L2Spawn spawnDat;
                L2NpcTemplate template1;

                // ??スター?ストを?期化
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

    // ハ?シ?シ?ドーのspawnデータの作成
    protected void InitLocationShadowSpawns()
    {
    	int locNo = Rnd.get(4);
    	final int[] gateKeeper = {31929,31934,31939,31944};

    	L2Spawn spawnDat;
        L2NpcTemplate template;

        // 出現座標情報をク?ア
        _ShadowSpawns.clear();

        // 今回の出現座標情報を作成
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

    // ハ?シ?の執行者のspawnデータの作成
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

    // 受付?間かチェック
    public boolean IsEntryTime()
    {
    	return _InEntryTime;
    }

    // 霊廟攻略?間かチェック
    public boolean IsAttackTime()
    {
    	return _InAttackTime;
    }

    // 霊廟の申し?みが可能かどうかをチェック
    public synchronized boolean IsEnableEntry(int npcId,L2PcInstance player)
    {
        // 受付?間外ならば受け付けない
        if(!IsEntryTime()) return false;

        // 既に他のプ?イ?ーが申し?んでいる場?は受け付けない
        else if(_HallInUse.get(npcId).booleanValue()) return false;

        // 参加人?の確認
        else if(Config.FS_PARTY_MEMBER_COUNT > 1)    // Configでソ?での挑戦が?されていないば?い
        {
        	// プ?イ?ーはパーティを組んでいること
        	if(player.getParty() == null) return false;

        	// プ?イ?ーはパーティ?ーダーで?ること
        	if(!player.getParty().isLeader(player)) return false;

            // プ?イ?ーは規定人?以上でパーティを組んでいること
            if (player.getParty().getMemberCount() < Config.FS_PARTY_MEMBER_COUNT) return false;

            else
            {
                for (L2PcInstance mem : player.getParty().getPartyMembers())
                {
                    // パーティ??バー全員がクエスト「４つの杯」を受けていること
                    if(mem.getQuestState(_QuestId).get("<state>") == null) return false;
                    // パーティ??バー全員が「霊廟の通行証」を?っていること
                    if (mem.getInventory().getItemByItemId(_EntrancePass) == null) return false;

                    // イ?ベ?ト?内のアイテ??が80%未?で?ること
                    int invLimitCnt = mem.getInventoryLimit();
                    int invItemCnt = mem.getInventory().getItems().length;
                    if((invItemCnt / invLimitCnt) >= 0.8) return false;

                    // イ?ベ?ト?の重量が80%未?で?ること
                    int invLimitWeight = mem.getMaxLoad();
                    int invWeight = mem.getInventory().getTotalWeight();
                    if((invWeight / invLimitWeight) >= 0.8) return false;
                }
            }
        }
        else    // ソ?の場?
        {
            // 挑戦者はクエスト「４つの杯」を受けていること
            if(player.getQuestState(_QuestId).get("<state>") == null) return false;
            // 挑戦者は「霊廟の通行証」を?っていること
            if(player.getInventory().getItemByItemId(_EntrancePass) == null) return false;

            // イ?ベ?ト?内のアイテ??が80%未?で?ること
            int invLimitCnt = player.getInventoryLimit();
            int invItemCnt = player.getInventory().getItems().length;
            if((invItemCnt / invLimitCnt) >= 0.8) return false;

            // イ?ベ?ト?の重量が80%未?で?ること
            int invLimitWeight = player.getMaxLoad();
            int invWeight = player.getInventory().getTotalWeight();
            if((invWeight / invLimitWeight) >= 0.8) return false;
        }

        // すべての判定を通過すれば?用可能
        return true;
    }

    // 挑戦者パーティを登録し、男爵の会堂へテ?ポートさせる
    public void Entry(int npcId, L2PcInstance player)
	{
		// 挑戦者パーティを男爵の会堂へテ?ポート
		// テ?ポート座標の取得
		int[] Location = _StartHallSpawns.get(npcId);
		int driftx;
		int drifty;

		// テ?ポート
		if (Config.FS_PARTY_MEMBER_COUNT > 1) // Configでソ?での挑戦が?されていないば?い
		{
			// 霊廟の申し?み条?を再確認
			if (IsEnableEntry(npcId, player))
			{
				List<L2PcInstance> members = new FastList<L2PcInstance>(); // テ?ポート可能な??バーの?スト
				for (L2PcInstance mem : player.getParty().getPartyMembers())
				{
					// ??バーが霊廟の通行証を?っていて、生きていて、パーティ?ーダーの認識範囲内にいれば、テ?ポートさせる
					if (!mem.isDead() && Util.checkIfInRange(700, player, mem, true))
					{
						members.add(mem);
					}
				}

				for (L2PcInstance mem : members)
				{
					// 男爵の会堂へテ?ポートする
					driftx = Rnd.get(-80, 80);
					drifty = Rnd.get(-80, 80);
					mem.teleToLocation(Location[0] + driftx, Location[1] + drifty, Location[2]);
					// 霊廟の通行証をイ?ベ?ト?から消す
					mem.destroyItemByItemId("Quest", _EntrancePass, 1, mem,true);
					// 古いブ?ーチを?っていなければ使用済み霊廟の通行証をイ?ベ?ト?に入れる
                    if (mem.getInventory().getItemByItemId(_OldBrooch) == null)
                    {
						mem.addItem("Quest", _UsedEntrancePass, 1, mem, true);
                    }

					// 不正防止のため、会堂の鍵を全て消す
					L2ItemInstance HallsKey = mem.getInventory().getItemByItemId(_HallsKey);
	                if(HallsKey != null)
	                {
	                    // 会堂の鍵をイ?ベ?ト?から消す
	                	mem.destroyItemByItemId("Quest", _HallsKey, HallsKey.getCount(), mem, true);
	                }
				}

				// 霊廟管?人にパーティ?ーダーを関係付ける
				_Challengers.remove(npcId);
				_Challengers.put(npcId, player);

				// 霊廟の?用状況を更新する
				_HallInUse.remove(npcId);
				_HallInUse.put(npcId, true);
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("条?を?たしていないため、入場が?否されました。");
				player.sendPacket(sm);
			}
		}
		else
		// ソ?の場?
		{
			// 霊廟の申し?み条?を再確認
			if (IsEnableEntry(npcId, player))
			{
				// 男爵の会堂へテ?ポートする
				driftx = Rnd.get(-80, 80);
				drifty = Rnd.get(-80, 80);
				player.teleToLocation(Location[0] + driftx, Location[1] + drifty, Location[2]);
				// 霊廟の通行証をイ?ベ?ト?から消す
				player.destroyItemByItemId("Quest", _EntrancePass, 1, player, true);
				// 古いブ?ーチを?っていなければ使用済み霊廟の通行証をイ?ベ?ト?に入れる
                if (player.getInventory().getItemByItemId(_OldBrooch) == null)
                {
                	player.addItem("Quest", _UsedEntrancePass, 1, player, true);
                }

				// 不正防止のため、会堂の鍵を全て消す
				L2ItemInstance HallsKey = player.getInventory().getItemByItemId(_HallsKey);
                if(HallsKey != null)
                {
                    // 会堂の鍵をイ?ベ?ト?から消す
                	player.destroyItemByItemId("Quest", _HallsKey, HallsKey.getCount(), player, true);
                }

                // 霊廟管?人にパーティ?ーダーを関係付ける
				_Challengers.remove(npcId);
				_Challengers.put(npcId, player);

				// 霊廟の?用状況を更新する
				_HallInUse.remove(npcId);
				_HallInUse.put(npcId, true);
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("条?を?たしていないため、入場が?否されました。");
				player.sendPacket(sm);
			}
		}
	}

    // 謎の?を出現させる
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

    // ??スターを出現させる
    public void SpawnMonster(int npcId)
    {
    	if (!IsAttackTime()) return;

    	FastList<L2Spawn> MonsterList;
    	List<L2SepulcherMonsterInstance> Mobs = new FastList<L2SepulcherMonsterInstance>();
    	L2Spawn KeyBoxMobSpawn;

    	// 物?系、?法系のどちらかを?める
    	if(Rnd.get(2) == 0)	// 物?系
    	{
    		MonsterList = (FastList<L2Spawn>)_PhysicalMonsters.get(npcId);
    	}
    	else				// ?法系
    	{
    		MonsterList = (FastList<L2Spawn>)_MagicalMonsters.get(npcId);
    	}

    	// ??スターを出現させる
    	if(MonsterList != null)
    	{
    		boolean SpawnKeyBoxMob = false;
    		boolean SpawnedKeyBoxMob = false;

        	for (L2Spawn spawnDat:MonsterList)
        	{
        		// 子爵の会堂で鍵の?を出現させる墓地の甲?を出現させるか判定する。
        		if(SpawnedKeyBoxMob)
        		{
        			SpawnKeyBoxMob = false;
        		}
        		else
        		{
            		switch(npcId)
            		{
            		    // 子爵の会堂の謎の?
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

        		// 鍵の?を出現させる墓地の甲?を出現させる??
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
            		    // 子爵の会堂の謎の?
            		    case 31469:
            		    case 31474:
            		    case 31479:
            		    case 31484:
            		    // 公爵の会堂の謎の?
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
    		    // 子爵の会堂の謎の?
    		    case 31469:
    		    case 31474:
    		    case 31479:
    		    case 31484:
    		    	_ViscountMobs.put(npcId, Mobs);
    		    	break;

    		    // 公爵の会堂の謎の?
    		    case 31472:
    		    case 31477:
    		    case 31482:
    		    case 31487:
    		    	_DukeMobs.put(npcId, Mobs);
    		    	break;
    		}
    	}
    }

    // 子爵の会堂の??スターの殲滅を確認する
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

    // 公爵の会堂の??スターの殲滅を確認する
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

    // 鍵の?を出現させる
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

    // ハ?シ?の執行者を出現させる
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

    // ハ?シ?アドミニスト?ーターを出現させる
    public void SpawnArchonOfHalisha(int npcId)
    {
    	if (!IsAttackTime()) return;

    	// 既に出現している場?は無?
    	if(_ArchonSpawned.get(npcId)) return;

    	FastList<L2Spawn> MonsterList = (FastList<L2Spawn>)_DukeFinalMobs.get(npcId);

    	// ??スターを出現させる
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

    // ウィゴットの幽霊とハ?シ?の宝?を出現させる
    public void SpawnEmperorsGraveNpc(int npcId)
    {
    	if (!IsAttackTime()) return;

    	FastList<L2Spawn> MonsterList = (FastList<L2Spawn>)_EmperorsGraveNpcs.get(npcId);

    	// ??スターを出現させる
    	if(MonsterList != null)
    	{
        	for (L2Spawn spawnDat:MonsterList)
        	{
        		_AllMobs.add(spawnDat.doSpawn());
            	spawnDat.stopRespawn();
    		}
    	}
    }

    // ハ?シ?シ?ドーの出現座標を?定する。
    protected void LocationShadowSpawns()
    {
    	int locNo = Rnd.get(4);
    	_log.info("FourSepulchersManager.LocationShadowSpawns: Location index is " + locNo + ".");
    	final int[] gateKeeper = {31929,31934,31939,31944};

    	L2Spawn spawnDat;

        // 今回の出現座標情報を作成
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

    // ハ?シ?シ?ドーを出現させる
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

    // パーティが全滅したかを確認
    public void checkAnnihilated(L2PcInstance player)
    {
    	// 全滅した場?は５秒後に?礼者の?院へ飛ばす
    	if(IsPartyAnnihilated(player))
    	{
    		_OnPartyAnnihilatedTask =
				ThreadPoolManager.getInstance().scheduleGeneral(new OnPartyAnnihilatedTask(player),5000);
    	}
    }

    // パーティが全滅したかを確認
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

    // パーティが全滅した場?の??、?礼者の?院へ飛ばす
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

    // 霊廟内の??スターを消す
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

    // すべての会堂のドアを閉じる
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

    // 参加受付?間に入る
    protected class ChaneEntryTime implements Runnable
    {
        public void run()
        {
            _log.info("FourSepulchersManager:In Entry Time");
            // フ?グを変更する
            _InEntryTime = true;
            _InWarmUpTime = false;
            _InAttackTime = false;
            _InCoolDownTime = false;

            // ウォー?アップ?間に移行するタスクを仕?んでおく
            _ChaneWarmUpTimeTask =
                ThreadPoolManager.getInstance().scheduleGeneral(new ChaneWarmUpTime(),Config.FS_TIME_ENTRY * 60000);

            // 実行されたタスクのク?ー?アップ
            if(_ChangeEntryTimeTask != null)
            {
            	_ChangeEntryTimeTask.cancel(true);
            	_ChangeEntryTimeTask = null;
            }
        }
    }

    // ウォー?アップ?間に入る
    protected class ChaneWarmUpTime implements Runnable
    {
        public void run()
        {
            _log.info("FourSepulchersManager:In Warm-Up Time");
            // フ?グを変更する
            _InEntryTime = true;
            _InWarmUpTime = false;
            _InAttackTime = false;
            _InCoolDownTime = false;

            // 霊廟攻略?間に移行するタスクを仕?んでおく
            _ChangeAttackTimeTask =
                ThreadPoolManager.getInstance().scheduleGeneral(new ChangeAttackTime(),Config.FS_TIME_WARMUP * 60000);

            // 実行されたタスクのク?ー?アップ
            if(_ChaneWarmUpTimeTask != null)
            {
            	_ChaneWarmUpTimeTask.cancel(true);
            	_ChaneWarmUpTimeTask = null;
            }
        }
    }

    // 霊廟攻略?間に入る
    protected class ChangeAttackTime implements Runnable
    {
        public void run()
        {
            _log.info("FourSepulchersManager:In Attack Time");
            // フ?グを変更する
            _InEntryTime = false;
            _InWarmUpTime = false;
            _InAttackTime = true;
            _InCoolDownTime = false;

            // ハ?シ?シ?ドーの出現座標を?定する
            LocationShadowSpawns();

            // 男爵の会堂の謎の?を出現させる
            SpawnMysteriousBox(31921);
            SpawnMysteriousBox(31922);
            SpawnMysteriousBox(31923);
            SpawnMysteriousBox(31924);

            // クー?ダウ?タイ?に移行するタスクを仕?んでおく
            _ChangeCoolDownTimeTask =
                ThreadPoolManager.getInstance().scheduleGeneral(new ChangeCoolDownTime(),Config.FS_TIME_ATTACK * 60000);

            // 実行されたタスクのク?ー?アップ
            if(_ChangeAttackTimeTask != null)
            {
            	_ChangeAttackTimeTask.cancel(true);
            	_ChangeAttackTimeTask = null;
            }
        }
    }

    // クー?ダウ?タイ?に入る
    protected class ChangeCoolDownTime implements Runnable
    {

        public void run()
        {
            _log.info("FourSepulchersManager:In Cool-Down Time");
            // フ?グを変更する
            _InEntryTime = false;
            _InWarmUpTime = false;
            _InAttackTime = false;
            _InCoolDownTime = true;

            // 攻略?のPCを?礼者の?院へ飛ばす
            for(L2PcInstance player :L2World.getInstance().getAllPlayers())
            {
            	if (CustomZoneManager.getInstance().checkIfInZone("FourSepulcher", player) &&
            		(player.getZ() >= -7250 && player.getZ() <= -6841) &&
            		!player.isGM())
            	{
            		int driftX = Rnd.get(-80,80);
            		int driftY = Rnd.get(-80,80);
            		player.teleToLocation(169589 + driftX,-90493 + driftY,-2914);
            	}
            }

            // 霊廟内の??スター類を消す
            DeleteAllMobs();

            // 開いている可能性の?る会堂の扉をすべて閉じる
            CloseAllDoors();

            // 霊廟の?用状態をク?ア
            _HallInUse.clear();
            _HallInUse.put(31921,false);    // 征?者の霊廟
            _HallInUse.put(31922,false);    // 支配者の霊廟
            _HallInUse.put(31923,false);    // 大賢者の霊廟
            _HallInUse.put(31924,false);    // 審判者の霊廟

            // 公爵の会堂のハ?シ?アドミニスト?ーターの出現状態をク?ア
            if(_ArchonSpawned.size() != 0)
            {
                Set<Integer> npcIdSet = _ArchonSpawned.keySet();
                for(int npcId:npcIdSet)
                {
                	_ArchonSpawned.put(npcId, false);
                }
            }

            // 参加受付?間に移行するタスクを仕?んでおく
            _ChangeEntryTimeTask =
                ThreadPoolManager.getInstance().scheduleGeneral(new ChaneEntryTime(),Config.FS_TIME_COOLDOWN * 60000);

            // 実行されたタスクのク?ー?アップ
            if(_ChangeCoolDownTimeTask != null)
            {
            	_ChangeCoolDownTimeTask.cancel(true);
            	_ChangeCoolDownTimeTask = null;
            }
        }
    }

    // 4大霊廟で?んだ場?、パーティが全滅していれば?礼者の?院へ飛ばす
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
            // 実行されたタスクのク?ー?アップ
            if(_OnPartyAnnihilatedTask != null)
            {
            	_OnPartyAnnihilatedTask.cancel(true);
            	_OnPartyAnnihilatedTask = null;
            }

		}
	}
}
