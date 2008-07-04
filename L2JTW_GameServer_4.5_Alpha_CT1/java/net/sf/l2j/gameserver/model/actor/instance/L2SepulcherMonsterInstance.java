/* This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.model.actor.instance;

import java.util.concurrent.Future;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.instancemanager.FourSepulchersManager;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.datatables.SkillTable;

/**
 *
 * @author  sandman
 */
public class L2SepulcherMonsterInstance extends L2MonsterInstance
{
    protected static Logger _log = Logger.getLogger(L2SepulcherMonsterInstance.class.getName());

        // 自分を出現させた謎の?のID
        public int MysteriousBoxId = 0;

        // ケープゴート用
        // 鍵の?を出現させるタスク
        protected Future _VictimSpawnKeyBoxTask = null;

        // 守護の石?用
        // 石化タスク
        protected Future _ChangeImmortalTask = null;
        // 無敵解?のタスク
        protected Future _ChangeMortalTask = null;

        // ?亡?のイベ?ト用
        protected Future _OnDeadEventTask = null;

        // コ?スト?クター
        public L2SepulcherMonsterInstance(int objectId, L2NpcTemplate template)
        {
                super(objectId, template);
        }

        public void onSpawn()
    {
                super.onSpawn();
                switch(getNpcId())
                {
                        // ケープゴートの場?
                        case 18150: // ケープゴート
                        case 18151: // ケープゴート
                        case 18152: // ケープゴート
                        case 18153: // ケープゴート
                        case 18154: // ケープゴート
                        case 18155: // ケープゴート
                        case 18156: // ケープゴート
                        case 18157: // ケープゴート
                                // 他の??スターのヘイトを稼ぐ

                                // ５分後に鍵の?を出現させるタスクを仕?む
                                if (_VictimSpawnKeyBoxTask != null)
                                        _VictimSpawnKeyBoxTask.cancel(true);
                                _VictimSpawnKeyBoxTask = ThreadPoolManager.getInstance().scheduleEffect(new VictimSpawnKeyBox(this), 300000);
                                break;

                        // 隅のお守りの場?
                        case 18196:     // 隅のお守り
                        case 18197:     // 隅のお守り
                        case 18198:     // 隅のお守り
                        case 18199:     // 隅のお守り
                        case 18200:     // 隅のお守り
                        case 18201:     // 隅のお守り
                        case 18202:     // 隅のお守り
                        case 18203:     // 隅のお守り
                        case 18204:     // 隅のお守り
                        case 18205:     // 隅のお守り
                        case 18206:     // 隅のお守り
                        case 18207:     // 隅のお守り
                        case 18208:     // 隅のお守り
                        case 18209:     // 隅のお守り
                        case 18210:     // 隅のお守り
                        case 18211:     // 隅のお守り
                                // デバフスキ?を発動させる
                                break;

                        // 守護の石?の場?
                        case 18231:     //守護の石?
                        case 18232:     //守護の石?
                        case 18233:     //守護の石?
                        case 18234:     //守護の石?
                        case 18235:     //守護の石?
                        case 18236:     //守護の石?
                        case 18237:     //守護の石?
                        case 18238:     //守護の石?
                        case 18239:     //守護の石?
                        case 18240:     //守護の石?
                        case 18241:     //守護の石?
                        case 18242:     //守護の石?
                        case 18243:     //守護の石?
                                // 無敵化し、??の効果（石化）を?出する
                                // 無敵化は出現と同?に行う
                                setIsInvul(true);

                                // 石化の効果は?る程度?間を置かないとなぜか解?されてしまう。
                                if (_ChangeImmortalTask != null)
                                        _ChangeImmortalTask.cancel(true);
                                _ChangeImmortalTask = ThreadPoolManager.getInstance().scheduleEffect(new ChangeImmortal(this), 1600);

                                break;
                }
    }

    public boolean doDie(L2Character killer) 
    {
        if (!super.doDie(killer))
            return false;
                // ?んだ場?のイベ?トが?る??スターはここで??
                switch(getNpcId())
                {
                        // 鍵の?を出現させる??スター
                        case 18120:     //ハ?シ?の?官
                        case 18121:     //ハ?シ?の?官
                        case 18122:     //ハ?シ?の?官
                        case 18123:     //ハ?シ?の?官
                        case 18124:     //ハ?シ?の?官
                        case 18125:     //ハ?シ?の?官
                        case 18126:     //ハ?シ?の?官
                        case 18127:     //ハ?シ?の?官
                        case 18128:     //ハ?シ?の?官
                        case 18129:     //ハ?シ?の?官
                        case 18130:     //ハ?シ?の?官
                        case 18131:     //ハ?シ?の?官
                        case 18149:     //墓地の甲?
                        case 18158:     //ハ?シ?の執行者
                        case 18159:     //ハ?シ?の執行者
                        case 18160:     //ハ?シ?の執行者
                        case 18161:     //ハ?シ?の執行者
                        case 18162:     //ハ?シ?の執行者
                        case 18163:     //ハ?シ?の執行者
                        case 18164:     //ハ?シ?の執行者
                        case 18165:     //ハ?シ?の執行者
                        case 18183:     //ハ?シ?の監督官
                        case 18184:     //ハ?シ?の監督官
                        case 18212:     //ハ?シ? アドミニスト?ーター
                        case 18213:     //ハ?シ? アドミニスト?ーター
                        case 18214:     //ハ?シ? アドミニスト?ーター
                        case 18215:     //ハ?シ? アドミニスト?ーター
                        case 18216:     //ハ?シ? アドミニスト?ーター
                        case 18217:     //ハ?シ? アドミニスト?ーター
                        case 18218:     //ハ?シ? アドミニスト?ーター
                        case 18219:     //ハ?シ? アドミニスト?ーター
                                if(_OnDeadEventTask != null) _OnDeadEventTask.cancel(true);
                                _OnDeadEventTask = ThreadPoolManager.getInstance().scheduleEffect(new OnDeadEvent(this), 3500);
                                break;

                        // ケープゴートは殺されるとハ?シ?の執行者を出現させる
                        case 18150:     //ケープゴート
                        case 18151:     //ケープゴート
                        case 18152:     //ケープゴート
                        case 18153:     //ケープゴート
                        case 18154:     //ケープゴート
                        case 18155:     //ケープゴート
                        case 18156:     //ケープゴート
                        case 18157:     //ケープゴート
                        if (_VictimSpawnKeyBoxTask != null)
                        {
                                _VictimSpawnKeyBoxTask.cancel(true);
                                _VictimSpawnKeyBoxTask = null;
                        }
                                if(_OnDeadEventTask != null) _OnDeadEventTask.cancel(true);
                                _OnDeadEventTask = ThreadPoolManager.getInstance().scheduleEffect(new OnDeadEvent(this), 3500);
                                break;

                        // ?屋の殲滅状態を監?して、全滅状態ならイベ?トを発生させる??スター
                        // 子爵の会堂
                        case 18141:     //墓地の甲?
                        case 18142:     //墓地の甲?
                        case 18143:     //墓地の甲?
                        case 18144:     //墓地の甲?
                        case 18145:     //墓地の甲?
                        case 18146:     //墓地の甲?
                        case 18147:     //墓地の甲?
                        case 18148:     //墓地の甲?
                                if(FourSepulchersManager.getInstance().IsViscountMobsAnnihilated(MysteriousBoxId))
                                {
                                        if(_OnDeadEventTask != null) _OnDeadEventTask.cancel(true);
                                        _OnDeadEventTask = ThreadPoolManager.getInstance().scheduleEffect(new OnDeadEvent(this), 3500);
                                }
                                break;

                        // ?屋の殲滅状態を監?して、全滅状態ならイベ?トを発生させる??スター
                        // 公爵の会堂
                        case 18220:     //闇の巫?
                        case 18221:     //闇の巫?
                        case 18222:     //闇の暗殺者
                        case 18223:     //闇の暗殺者
                        case 18224:     //闇の暗殺者
                        case 18225:     //闇の暗殺者
                        case 18226:     //闇の史伝者
                        case 18227:     //闇の史伝者
                        case 18228:     //闇の史伝者
                        case 18229:     //闇の史伝者
                        case 18230:     //皇帝の印章
                        case 18231:     //守護の石?
                        case 18232:     //守護の石?
                        case 18233:     //守護の石?
                        case 18234:     //守護の石?
                        case 18235:     //守護の石?
                        case 18236:     //守護の石?
                        case 18237:     //守護の石?
                        case 18238:     //守護の石?
                        case 18239:     //守護の石?
                        case 18240:     //守護の石?
                                if(FourSepulchersManager.getInstance().IsDukeMobsAnnihilated(MysteriousBoxId))
                                {
                                        if(_OnDeadEventTask != null) _OnDeadEventTask.cancel(true);
                                        _OnDeadEventTask = ThreadPoolManager.getInstance().scheduleEffect(new OnDeadEvent(this), 3500);
                                }
                                break;

                        // ハ?シ?シ?ドーは討伐隊に杯を渡す
                        // ハ?シ?シ?ドーはウィゴットの幽霊を出現させる
                        case 25339:
                        case 25342:
                        case 25346:
                        case 25349:
                                GiveCup((L2PcInstance)killer);
                                if(_OnDeadEventTask != null) _OnDeadEventTask.cancel(true);
                                _OnDeadEventTask = ThreadPoolManager.getInstance().scheduleEffect(new OnDeadEvent(this), 8500);
                                break;
                }
                return true;
    }

    public void deleteMe()
    {
        if (_VictimSpawnKeyBoxTask != null)
        {
                _VictimSpawnKeyBoxTask.cancel(true);
                _VictimSpawnKeyBoxTask = null;
        }
                if(_OnDeadEventTask != null)
                {
                        _OnDeadEventTask.cancel(true);
                        _OnDeadEventTask = null;
                }

                super.deleteMe();
    }

        public boolean isRaid()
        {
                switch(getNpcId())
                {
                        // ハ?シ?シ?ドーは?イドボス扱い
                        case 25339:
                        case 25342:
                        case 25346:
                        case 25349:
                                return true;
                        default:
                                return false;
                }
        }

        // ハ?シ?シ?ドーは討伐隊に杯を渡す
        protected void GiveCup(L2PcInstance player)
        {
            // クエスト「４つの杯」
            String QuestId = "620_FourGoblets";
            int CupId = 0;
            int OldBrooch = 7262;       // 古いブ?ーチ


        // どの杯か?定する
                switch(getNpcId())
                {
                        case 25339:
                                CupId = 7256;   // ア?クティアの杯
                                break;
                        case 25342:
                                CupId = 7257;   // ティシ?スの杯
                                break;
                        case 25346:
                                CupId = 7258;   // ?カ?の杯
                                break;
                        case 25349:
                                CupId = 7259;   // ??グ?の杯
                                break;
                }

                // 討伐隊がパーティーの場?
                if(player.getParty() != null)
                {
            for (L2PcInstance mem : player.getParty().getPartyMembers())
            {
                // パーティ??バー全員がクエスト「４つの杯」を受けていること
                if(mem.getQuestState(QuestId) == null) return;
                else
                {
                                        // 古いブ?ーチを?っていなければ杯をイ?ベ?ト?に入れる
                    if (mem.getInventory().getItemByItemId(OldBrooch) == null)
                    {
                                        mem.addItem("Quest", CupId, 1, mem, true);
                    }
                }
            }
                }
            // 討伐隊がソ?の場?
                else
                {
            if(player.getQuestState(QuestId) == null) return;
            else
            {
                                // 古いブ?ーチを?っていなければ杯をイ?ベ?ト?に入れる
                if (player.getInventory().getItemByItemId(OldBrooch) == null)
                {
                        player.addItem("Quest", CupId, 1, player, true);
                }
            }
                }
        }

        // ケープゴートが鍵の?を出現させるタスク
        protected class VictimSpawnKeyBox implements Runnable
        {
                L2SepulcherMonsterInstance _activeChar;

                public VictimSpawnKeyBox(L2SepulcherMonsterInstance activeChar)
                {
                        _activeChar = activeChar;
                }

        public void run()
        {
                // ?んでいる場?は何もしない
                if(_activeChar.isDead()) return;

                // 姿が消えている場?も何もしない
                if(!_activeChar.isVisible()) return;

                // 鍵の?を出現させる
                        FourSepulchersManager.getInstance().SpawnKeyBox(_activeChar);
        }
        }

        // ?亡後のイベ?トを発生させるタスク
        protected class OnDeadEvent implements Runnable
        {
                L2SepulcherMonsterInstance _activeChar;

                public OnDeadEvent(L2SepulcherMonsterInstance activeChar)
                {
                        _activeChar = activeChar;
                }

        public void run()
        {
                switch(_activeChar.getNpcId())
                {
                        // 鍵の?を出現させる??スター
                        case 18120:     //ハ?シ?の?官
                        case 18121:     //ハ?シ?の?官
                        case 18122:     //ハ?シ?の?官
                        case 18123:     //ハ?シ?の?官
                        case 18124:     //ハ?シ?の?官
                        case 18125:     //ハ?シ?の?官
                        case 18126:     //ハ?シ?の?官
                        case 18127:     //ハ?シ?の?官
                        case 18128:     //ハ?シ?の?官
                        case 18129:     //ハ?シ?の?官
                        case 18130:     //ハ?シ?の?官
                        case 18131:     //ハ?シ?の?官
                        case 18149:     //墓地の甲?
                        case 18158:     //ハ?シ?の執行者
                        case 18159:     //ハ?シ?の執行者
                        case 18160:     //ハ?シ?の執行者
                        case 18161:     //ハ?シ?の執行者
                        case 18162:     //ハ?シ?の執行者
                        case 18163:     //ハ?シ?の執行者
                        case 18164:     //ハ?シ?の執行者
                        case 18165:     //ハ?シ?の執行者
                        case 18183:     //ハ?シ?の監督官
                        case 18184:     //ハ?シ?の監督官
                        case 18212:     //ハ?シ? アドミニスト?ーター
                        case 18213:     //ハ?シ? アドミニスト?ーター
                        case 18214:     //ハ?シ? アドミニスト?ーター
                        case 18215:     //ハ?シ? アドミニスト?ーター
                        case 18216:     //ハ?シ? アドミニスト?ーター
                        case 18217:     //ハ?シ? アドミニスト?ーター
                        case 18218:     //ハ?シ? アドミニスト?ーター
                        case 18219:     //ハ?シ? アドミニスト?ーター
                                FourSepulchersManager.getInstance().SpawnKeyBox(_activeChar);
                                break;

                        // ケープゴートは殺されるとハ?シ?の執行者を出現させる
                        case 18150:     //ケープゴート
                        case 18151:     //ケープゴート
                        case 18152:     //ケープゴート
                        case 18153:     //ケープゴート
                        case 18154:     //ケープゴート
                        case 18155:     //ケープゴート
                        case 18156:     //ケープゴート
                        case 18157:     //ケープゴート
                                FourSepulchersManager.getInstance().SpawnExecutionerOfHalisha(_activeChar);
                                break;

                        // ?屋の殲滅状態を監?して、全滅状態ならイベ?トを発生させる??スター
                        // 子爵の会堂
                        case 18141:     //墓地の甲?
                        case 18142:     //墓地の甲?
                        case 18143:     //墓地の甲?
                        case 18144:     //墓地の甲?
                        case 18145:     //墓地の甲?
                        case 18146:     //墓地の甲?
                        case 18147:     //墓地の甲?
                        case 18148:     //墓地の甲?
                                FourSepulchersManager.getInstance().SpawnMonster(_activeChar.MysteriousBoxId);
                                break;

                        // ?屋の殲滅状態を監?して、全滅状態ならイベ?トを発生させる??スター
                        // 公爵の会堂
                        case 18220:     //闇の巫?
                        case 18221:     //闇の巫?
                        case 18222:     //闇の暗殺者
                        case 18223:     //闇の暗殺者
                        case 18224:     //闇の暗殺者
                        case 18225:     //闇の暗殺者
                        case 18226:     //闇の史伝者
                        case 18227:     //闇の史伝者
                        case 18228:     //闇の史伝者
                        case 18229:     //闇の史伝者
                        case 18230:     //皇帝の印章
                        case 18231:     //守護の石?
                        case 18232:     //守護の石?
                        case 18233:     //守護の石?
                        case 18234:     //守護の石?
                        case 18235:     //守護の石?
                        case 18236:     //守護の石?
                        case 18237:     //守護の石?
                        case 18238:     //守護の石?
                        case 18239:     //守護の石?
                        case 18240:     //守護の石?
                                        FourSepulchersManager.getInstance().SpawnArchonOfHalisha(_activeChar.MysteriousBoxId);
                                break;

                        // ハ?シ?シ?ドーはウィゴットの幽霊を出現させる
                        case 25339:
                        case 25342:
                        case 25346:
                        case 25349:
                                        FourSepulchersManager.getInstance().SpawnEmperorsGraveNpc(_activeChar.MysteriousBoxId);
                                break;
                }
        }
        }

        // 守護の石?が石化するタスク
        protected class ChangeImmortal implements Runnable
        {
                L2SepulcherMonsterInstance activeChar;
                public ChangeImmortal(L2SepulcherMonsterInstance mob)
                {
                        activeChar = mob;
                }

                public void run()
                {
                        L2Skill fp = SkillTable.getInstance().getInfo(4616, 1);
                        fp.getEffects(activeChar, activeChar);

                        // 無敵解?タスクの仕?み
                        if (_ChangeMortalTask != null)
                                _ChangeMortalTask.cancel(true);
                        _ChangeMortalTask = ThreadPoolManager.getInstance().scheduleEffect(new ChangeMortal(), fp.getBuffDuration());
                }
        }

        // 守護の石?が無敵を解?するタスク
        protected class ChangeMortal implements Runnable
        {
                public ChangeMortal()
                {
                }

                public void run()
                {
                        setIsInvul(false);
                }
        }
}
