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

        // �������o�����������?��ID
        public int MysteriousBoxId = 0;

        // �P�[�v�S�[�g�p
        // ����?���o��������^�X�N
        protected Future _VictimSpawnKeyBoxTask = null;

        // ���̐�?�p
        // �Ή��^�X�N
        protected Future _ChangeImmortalTask = null;
        // ���G��?�̃^�X�N
        protected Future _ChangeMortalTask = null;

        // ?�S?�̃C�x?�g�p
        protected Future _OnDeadEventTask = null;

        // �R?�X�g?�N�^�[
        public L2SepulcherMonsterInstance(int objectId, L2NpcTemplate template)
        {
                super(objectId, template);
        }

        public void onSpawn()
    {
                super.onSpawn();
                switch(getNpcId())
                {
                        // �P�[�v�S�[�g�̏�?
                        case 18150: // �P�[�v�S�[�g
                        case 18151: // �P�[�v�S�[�g
                        case 18152: // �P�[�v�S�[�g
                        case 18153: // �P�[�v�S�[�g
                        case 18154: // �P�[�v�S�[�g
                        case 18155: // �P�[�v�S�[�g
                        case 18156: // �P�[�v�S�[�g
                        case 18157: // �P�[�v�S�[�g
                                // ����??�X�^�[�̃w�C�g���҂�

                                // �T����Ɍ���?���o��������^�X�N���d?��
                                if (_VictimSpawnKeyBoxTask != null)
                                        _VictimSpawnKeyBoxTask.cancel(true);
                                _VictimSpawnKeyBoxTask = ThreadPoolManager.getInstance().scheduleEffect(new VictimSpawnKeyBox(this), 300000);
                                break;

                        // ���̂����̏�?
                        case 18196:     // ���̂����
                        case 18197:     // ���̂����
                        case 18198:     // ���̂����
                        case 18199:     // ���̂����
                        case 18200:     // ���̂����
                        case 18201:     // ���̂����
                        case 18202:     // ���̂����
                        case 18203:     // ���̂����
                        case 18204:     // ���̂����
                        case 18205:     // ���̂����
                        case 18206:     // ���̂����
                        case 18207:     // ���̂����
                        case 18208:     // ���̂����
                        case 18209:     // ���̂����
                        case 18210:     // ���̂����
                        case 18211:     // ���̂����
                                // �f�o�t�X�L?�𔭓�������
                                break;

                        // ���̐�?�̏�?
                        case 18231:     //���̐�?
                        case 18232:     //���̐�?
                        case 18233:     //���̐�?
                        case 18234:     //���̐�?
                        case 18235:     //���̐�?
                        case 18236:     //���̐�?
                        case 18237:     //���̐�?
                        case 18238:     //���̐�?
                        case 18239:     //���̐�?
                        case 18240:     //���̐�?
                        case 18241:     //���̐�?
                        case 18242:     //���̐�?
                        case 18243:     //���̐�?
                                // ���G�����A??�̌��ʁi�Ή��j��?�o����
                                // ���G���͏o���Ɠ�?�ɍs��
                                setIsInvul(true);

                                // �Ή��̌��ʂ�?����x?�Ԃ�u���Ȃ��ƂȂ�����?����Ă��܂��B
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
                // ?�񂾏�?�̃C�x?�g��?��??�X�^�[�͂�����??
                switch(getNpcId())
                {
                        // ����?���o��������??�X�^�[
                        case 18120:     //�n?�V?��?��
                        case 18121:     //�n?�V?��?��
                        case 18122:     //�n?�V?��?��
                        case 18123:     //�n?�V?��?��
                        case 18124:     //�n?�V?��?��
                        case 18125:     //�n?�V?��?��
                        case 18126:     //�n?�V?��?��
                        case 18127:     //�n?�V?��?��
                        case 18128:     //�n?�V?��?��
                        case 18129:     //�n?�V?��?��
                        case 18130:     //�n?�V?��?��
                        case 18131:     //�n?�V?��?��
                        case 18149:     //��n�̍b?
                        case 18158:     //�n?�V?�̎��s��
                        case 18159:     //�n?�V?�̎��s��
                        case 18160:     //�n?�V?�̎��s��
                        case 18161:     //�n?�V?�̎��s��
                        case 18162:     //�n?�V?�̎��s��
                        case 18163:     //�n?�V?�̎��s��
                        case 18164:     //�n?�V?�̎��s��
                        case 18165:     //�n?�V?�̎��s��
                        case 18183:     //�n?�V?�̊ē�
                        case 18184:     //�n?�V?�̊ē�
                        case 18212:     //�n?�V? �A�h�~�j�X�g?�[�^�[
                        case 18213:     //�n?�V? �A�h�~�j�X�g?�[�^�[
                        case 18214:     //�n?�V? �A�h�~�j�X�g?�[�^�[
                        case 18215:     //�n?�V? �A�h�~�j�X�g?�[�^�[
                        case 18216:     //�n?�V? �A�h�~�j�X�g?�[�^�[
                        case 18217:     //�n?�V? �A�h�~�j�X�g?�[�^�[
                        case 18218:     //�n?�V? �A�h�~�j�X�g?�[�^�[
                        case 18219:     //�n?�V? �A�h�~�j�X�g?�[�^�[
                                if(_OnDeadEventTask != null) _OnDeadEventTask.cancel(true);
                                _OnDeadEventTask = ThreadPoolManager.getInstance().scheduleEffect(new OnDeadEvent(this), 3500);
                                break;

                        // �P�[�v�S�[�g�͎E�����ƃn?�V?�̎��s�҂��o��������
                        case 18150:     //�P�[�v�S�[�g
                        case 18151:     //�P�[�v�S�[�g
                        case 18152:     //�P�[�v�S�[�g
                        case 18153:     //�P�[�v�S�[�g
                        case 18154:     //�P�[�v�S�[�g
                        case 18155:     //�P�[�v�S�[�g
                        case 18156:     //�P�[�v�S�[�g
                        case 18157:     //�P�[�v�S�[�g
                        if (_VictimSpawnKeyBoxTask != null)
                        {
                                _VictimSpawnKeyBoxTask.cancel(true);
                                _VictimSpawnKeyBoxTask = null;
                        }
                                if(_OnDeadEventTask != null) _OnDeadEventTask.cancel(true);
                                _OnDeadEventTask = ThreadPoolManager.getInstance().scheduleEffect(new OnDeadEvent(this), 3500);
                                break;

                        // ?���̟r�ŏ�Ԃ���?���āA�S�ŏ�ԂȂ�C�x?�g�𔭐�������??�X�^�[
                        // �q�݂̉
                        case 18141:     //��n�̍b?
                        case 18142:     //��n�̍b?
                        case 18143:     //��n�̍b?
                        case 18144:     //��n�̍b?
                        case 18145:     //��n�̍b?
                        case 18146:     //��n�̍b?
                        case 18147:     //��n�̍b?
                        case 18148:     //��n�̍b?
                                if(FourSepulchersManager.getInstance().IsViscountMobsAnnihilated(MysteriousBoxId))
                                {
                                        if(_OnDeadEventTask != null) _OnDeadEventTask.cancel(true);
                                        _OnDeadEventTask = ThreadPoolManager.getInstance().scheduleEffect(new OnDeadEvent(this), 3500);
                                }
                                break;

                        // ?���̟r�ŏ�Ԃ���?���āA�S�ŏ�ԂȂ�C�x?�g�𔭐�������??�X�^�[
                        // ���݂̉
                        case 18220:     //�ł̛�?
                        case 18221:     //�ł̛�?
                        case 18222:     //�ł̈ÎE��
                        case 18223:     //�ł̈ÎE��
                        case 18224:     //�ł̈ÎE��
                        case 18225:     //�ł̈ÎE��
                        case 18226:     //�ł̎j�`��
                        case 18227:     //�ł̎j�`��
                        case 18228:     //�ł̎j�`��
                        case 18229:     //�ł̎j�`��
                        case 18230:     //�c��̈��
                        case 18231:     //���̐�?
                        case 18232:     //���̐�?
                        case 18233:     //���̐�?
                        case 18234:     //���̐�?
                        case 18235:     //���̐�?
                        case 18236:     //���̐�?
                        case 18237:     //���̐�?
                        case 18238:     //���̐�?
                        case 18239:     //���̐�?
                        case 18240:     //���̐�?
                                if(FourSepulchersManager.getInstance().IsDukeMobsAnnihilated(MysteriousBoxId))
                                {
                                        if(_OnDeadEventTask != null) _OnDeadEventTask.cancel(true);
                                        _OnDeadEventTask = ThreadPoolManager.getInstance().scheduleEffect(new OnDeadEvent(this), 3500);
                                }
                                break;

                        // �n?�V?�V?�h�[�͓������ɔt��n��
                        // �n?�V?�V?�h�[�̓E�B�S�b�g�̗H����o��������
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
                        // �n?�V?�V?�h�[��?�C�h�{�X����
                        case 25339:
                        case 25342:
                        case 25346:
                        case 25349:
                                return true;
                        default:
                                return false;
                }
        }

        // �n?�V?�V?�h�[�͓������ɔt��n��
        protected void GiveCup(L2PcInstance player)
        {
            // �N�G�X�g�u�S�̔t�v
            String QuestId = "620_FourGoblets";
            int CupId = 0;
            int OldBrooch = 7262;       // �Â��u?�[�`


        // �ǂ̔t��?�肷��
                switch(getNpcId())
                {
                        case 25339:
                                CupId = 7256;   // �A?�N�e�B�A�̔t
                                break;
                        case 25342:
                                CupId = 7257;   // �e�B�V?�X�̔t
                                break;
                        case 25346:
                                CupId = 7258;   // ?�J?�̔t
                                break;
                        case 25349:
                                CupId = 7259;   // ??�O?�̔t
                                break;
                }

                // ���������p�[�e�B�[�̏�?
                if(player.getParty() != null)
                {
            for (L2PcInstance mem : player.getParty().getPartyMembers())
            {
                // �p�[�e�B??�o�[�S�����N�G�X�g�u�S�̔t�v���󂯂Ă��邱��
                if(mem.getQuestState(QuestId) == null) return;
                else
                {
                                        // �Â��u?�[�`��?���Ă��Ȃ���Δt���C?�x?�g?�ɓ����
                    if (mem.getInventory().getItemByItemId(OldBrooch) == null)
                    {
                                        mem.addItem("Quest", CupId, 1, mem, true);
                    }
                }
            }
                }
            // ���������\?�̏�?
                else
                {
            if(player.getQuestState(QuestId) == null) return;
            else
            {
                                // �Â��u?�[�`��?���Ă��Ȃ���Δt���C?�x?�g?�ɓ����
                if (player.getInventory().getItemByItemId(OldBrooch) == null)
                {
                        player.addItem("Quest", CupId, 1, player, true);
                }
            }
                }
        }

        // �P�[�v�S�[�g������?���o��������^�X�N
        protected class VictimSpawnKeyBox implements Runnable
        {
                L2SepulcherMonsterInstance _activeChar;

                public VictimSpawnKeyBox(L2SepulcherMonsterInstance activeChar)
                {
                        _activeChar = activeChar;
                }

        public void run()
        {
                // ?��ł����?�͉������Ȃ�
                if(_activeChar.isDead()) return;

                // �p�������Ă����?���������Ȃ�
                if(!_activeChar.isVisible()) return;

                // ����?���o��������
                        FourSepulchersManager.getInstance().SpawnKeyBox(_activeChar);
        }
        }

        // ?�S��̃C�x?�g�𔭐�������^�X�N
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
                        // ����?���o��������??�X�^�[
                        case 18120:     //�n?�V?��?��
                        case 18121:     //�n?�V?��?��
                        case 18122:     //�n?�V?��?��
                        case 18123:     //�n?�V?��?��
                        case 18124:     //�n?�V?��?��
                        case 18125:     //�n?�V?��?��
                        case 18126:     //�n?�V?��?��
                        case 18127:     //�n?�V?��?��
                        case 18128:     //�n?�V?��?��
                        case 18129:     //�n?�V?��?��
                        case 18130:     //�n?�V?��?��
                        case 18131:     //�n?�V?��?��
                        case 18149:     //��n�̍b?
                        case 18158:     //�n?�V?�̎��s��
                        case 18159:     //�n?�V?�̎��s��
                        case 18160:     //�n?�V?�̎��s��
                        case 18161:     //�n?�V?�̎��s��
                        case 18162:     //�n?�V?�̎��s��
                        case 18163:     //�n?�V?�̎��s��
                        case 18164:     //�n?�V?�̎��s��
                        case 18165:     //�n?�V?�̎��s��
                        case 18183:     //�n?�V?�̊ē�
                        case 18184:     //�n?�V?�̊ē�
                        case 18212:     //�n?�V? �A�h�~�j�X�g?�[�^�[
                        case 18213:     //�n?�V? �A�h�~�j�X�g?�[�^�[
                        case 18214:     //�n?�V? �A�h�~�j�X�g?�[�^�[
                        case 18215:     //�n?�V? �A�h�~�j�X�g?�[�^�[
                        case 18216:     //�n?�V? �A�h�~�j�X�g?�[�^�[
                        case 18217:     //�n?�V? �A�h�~�j�X�g?�[�^�[
                        case 18218:     //�n?�V? �A�h�~�j�X�g?�[�^�[
                        case 18219:     //�n?�V? �A�h�~�j�X�g?�[�^�[
                                FourSepulchersManager.getInstance().SpawnKeyBox(_activeChar);
                                break;

                        // �P�[�v�S�[�g�͎E�����ƃn?�V?�̎��s�҂��o��������
                        case 18150:     //�P�[�v�S�[�g
                        case 18151:     //�P�[�v�S�[�g
                        case 18152:     //�P�[�v�S�[�g
                        case 18153:     //�P�[�v�S�[�g
                        case 18154:     //�P�[�v�S�[�g
                        case 18155:     //�P�[�v�S�[�g
                        case 18156:     //�P�[�v�S�[�g
                        case 18157:     //�P�[�v�S�[�g
                                FourSepulchersManager.getInstance().SpawnExecutionerOfHalisha(_activeChar);
                                break;

                        // ?���̟r�ŏ�Ԃ���?���āA�S�ŏ�ԂȂ�C�x?�g�𔭐�������??�X�^�[
                        // �q�݂̉
                        case 18141:     //��n�̍b?
                        case 18142:     //��n�̍b?
                        case 18143:     //��n�̍b?
                        case 18144:     //��n�̍b?
                        case 18145:     //��n�̍b?
                        case 18146:     //��n�̍b?
                        case 18147:     //��n�̍b?
                        case 18148:     //��n�̍b?
                                FourSepulchersManager.getInstance().SpawnMonster(_activeChar.MysteriousBoxId);
                                break;

                        // ?���̟r�ŏ�Ԃ���?���āA�S�ŏ�ԂȂ�C�x?�g�𔭐�������??�X�^�[
                        // ���݂̉
                        case 18220:     //�ł̛�?
                        case 18221:     //�ł̛�?
                        case 18222:     //�ł̈ÎE��
                        case 18223:     //�ł̈ÎE��
                        case 18224:     //�ł̈ÎE��
                        case 18225:     //�ł̈ÎE��
                        case 18226:     //�ł̎j�`��
                        case 18227:     //�ł̎j�`��
                        case 18228:     //�ł̎j�`��
                        case 18229:     //�ł̎j�`��
                        case 18230:     //�c��̈��
                        case 18231:     //���̐�?
                        case 18232:     //���̐�?
                        case 18233:     //���̐�?
                        case 18234:     //���̐�?
                        case 18235:     //���̐�?
                        case 18236:     //���̐�?
                        case 18237:     //���̐�?
                        case 18238:     //���̐�?
                        case 18239:     //���̐�?
                        case 18240:     //���̐�?
                                        FourSepulchersManager.getInstance().SpawnArchonOfHalisha(_activeChar.MysteriousBoxId);
                                break;

                        // �n?�V?�V?�h�[�̓E�B�S�b�g�̗H����o��������
                        case 25339:
                        case 25342:
                        case 25346:
                        case 25349:
                                        FourSepulchersManager.getInstance().SpawnEmperorsGraveNpc(_activeChar.MysteriousBoxId);
                                break;
                }
        }
        }

        // ���̐�?���Ή�����^�X�N
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

                        // ���G��?�^�X�N�̎d?��
                        if (_ChangeMortalTask != null)
                                _ChangeMortalTask.cancel(true);
                        _ChangeMortalTask = ThreadPoolManager.getInstance().scheduleEffect(new ChangeMortal(), fp.getBuffDuration());
                }
        }

        // ���̐�?�����G����?����^�X�N
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
