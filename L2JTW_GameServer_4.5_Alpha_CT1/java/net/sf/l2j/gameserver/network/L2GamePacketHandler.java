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
package net.sf.l2j.gameserver.network;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.clientpackets.*;
import net.sf.l2j.gameserver.network.L2GameClient.GameClientState;
import net.sf.l2j.util.Util;

import org.mmocore.network.HeaderInfo;
import org.mmocore.network.IClientFactory;
import org.mmocore.network.IMMOExecutor;
import org.mmocore.network.IPacketHandler;
import org.mmocore.network.MMOConnection;
import org.mmocore.network.ReceivablePacket;
import org.mmocore.network.TCPHeaderHandler;

/**
 * Stateful Packet Handler<BR>
 * The Stateful approach prevents the server from handling inconsistent packets, examples:<BR>
 * <li>Clients sends a MoveToLocation packet without having a character attached. (Potential errors handling the packet).</li>
 * <li>Clients sends a RequestAuthLogin being already authed. (Potential exploit).</li>
 * <BR><BR>
 * Note: If for a given exception a packet needs to be handled on more then one state, then it should be added to all these states.
 * @author  KenM
 */
public final class L2GamePacketHandler extends TCPHeaderHandler<L2GameClient> implements IPacketHandler<L2GameClient>, IClientFactory<L2GameClient>, IMMOExecutor<L2GameClient>
{
	/**
     * @param subHeaderHandler
     */
    public L2GamePacketHandler()
    {
        super(null);
    }

    private static final Logger _log = Logger.getLogger(L2GamePacketHandler.class.getName());

	// implementation
	public ReceivablePacket<L2GameClient> handlePacket(ByteBuffer buf, L2GameClient client)
	{
		int opcode = buf.get() & 0xFF;

		ReceivablePacket<L2GameClient> msg = null;
		GameClientState state = client.getState();
		//_log.warning("STATE:" +client.getState());
		if(client.getState() == state.CONNECTED)
		switch(opcode)
		{		
				case 0x2b:
					client.setClientType(true);
					break;
				case 0x48: 
					client.setClientType(false);
					break;
		}
		if(client.PTSClient)
		switch (state)
		{
			case CONNECTED:
                switch (opcode)
                {
                    case 0x00:
                        _log.warning("Client "+client.toString()+" is trying to connect using Interlude Client");
                        break;
                    case 0x0e:
                        msg = new ProtocolVersion();
                        break;
                    case 0x2b:
                        msg = new AuthLogin();
                        break;
                    default:
                        printDebug(opcode, buf, state, client);
                        break;
                }
				break;
			case AUTHED:
				switch (opcode)
				{
					case 0x00:
						msg = new Logout();
						break;
                    case 0x0c:
                        msg = new CharacterCreate();
                        break;
                    case 0x0d:
                        msg = new CharacterDelete();
                        break;
                    case 0x12:
                        msg = new CharacterSelect();
                        break;
                    case 0x13:
                        msg = new NewCharacter();
                        break;
                    case 0x67:
                        msg = new RequestPledgeCrest();
                        break;
					case 0x7b:
						msg = new CharacterRestore();
						break;
					case 0xd0:
						int id2 = -1;
		            	if (buf.remaining() >= 2)
		            	{
		            		id2 = buf.getShort() & 0xffff;
		            	}
		            	else
		            	{
		            		_log.warning("Client: "+client.toString()+" sent a 0xd0 without the second opcode.");
		            		break;
		            	}
		            	
		            	// single packet
		            	if (id2 == 0x39)
		            	{
		            		msg = new RequestGotoLobby();
		            	}
		            	else
		            	{
		            		this.printDebugDoubleOpcode(opcode, id2, buf, state, client);
		            	}
		            	
						break;
					default:
						this.printDebug(opcode, buf, state, client);
						break;
				}
				break;
			case IN_GAME:
				switch(opcode)
				{
                    case 0x00:
                        msg = new Logout();
                        break;
                    case 0x01:
                        msg = new AttackRequest();
                        break;
                    case 0x03:
                        msg = new RequestStartPledgeWar();
                        break;
                    case 0x04:
                        msg = new RequestReplyStartPledgeWar();
                        break;
                    case 0x05:
                        msg = new RequestStopPledgeWar();
                        break;
                    case 0x06:
                        msg = new RequestReplyStopPledgeWar();
                        break;
                    case 0x07:
                        msg = new RequestSurrenderPledgeWar();
                        break;
                    case 0x08:
                        msg = new RequestReplySurrenderPledgeWar();
                        break;
                    case 0x09:
                        msg = new RequestSetPledgeCrest();
                        break;
                    case 0x0b:
                        msg = new RequestGiveNickName();
                        break;
					case 0x0f:
						msg = new MoveBackwardToLocation();
						break;
					case 0x11:
						msg = new EnterWorld();
						break;
					case 0x12:
					    break;
                    case 0x14:
                        msg = new RequestItemList();
                        break;
                    case 0x16:
                        msg = new RequestUnEquipItem(); // confirm
                        break;
                    case 0x17:
                        msg = new RequestDropItem();
                        break;
                    case 0x19:
                        msg = new UseItem();
                        break;
                    case 0x1a:
                        msg = new TradeRequest();
                        break;
                    case 0x1b:
                        msg = new AddTradeItem();
                        break;
                    case 0x1c:
                        msg = new TradeDone();
                        break;
					case 0x1f:
						msg = new Action();
						break;
                    case 0x22:
                        msg = new RequestLinkHtml();
                        break;
                    case 0x23:
                        msg = new RequestBypassToServer();
                        break;
                    case 0x24:
                        msg = new RequestBBSwrite();
                        break;
                    case 0x26:
                        msg = new RequestJoinPledge();
                        break;
                    case 0x27:
                        msg = new RequestAnswerJoinPledge();
                        break;
                    case 0x28:
                        msg = new RequestWithdrawalPledge();
                        break;
                    case 0x29:
                        msg = new RequestOustPledgeMember();
                        break;
                    case 0x2c:
                        msg = new RequestGetItemFromPet();
                        break;
                    case 0x2e:
                        msg = new RequestAllyInfo();
                        break;
                    case 0x2f:
                        msg = new RequestCrystallizeItem();
                        break;
                    case 0x30: // t1 ??
                        msg = new RequestPrivateStoreManageSell();
                        break;
                    case 0x31: // t1 ??
                        msg = new SetPrivateStoreListSell();
                        break;
//                  case 0x32:
//                      msg = new RequestPrivateStoreManageCancel(data, _client);
//                      break;
					case 0x34:
						msg = new RequestSocialAction();
						break;
					case 0x35:
						msg = new ChangeMoveType2();
						break;
					case 0x36:
						msg = new ChangeWaitType2();
						break;
					case 0x37:
						msg = new RequestSellItem();
						break;
                    case 0x39:
                        msg = new RequestMagicSkillUse();
                        break;
                    case 0x3a:
                        msg = new Appearing();  //  (after death)
                        break;
                    case 0x3b:
                        if (Config.ALLOW_WAREHOUSE)
                            msg = new SendWareHouseDepositList();
                        break;
                    case 0x3c:
                        msg = new SendWareHouseWithDrawList();
                        break;
                    case 0x3d:
                        msg = new RequestShortCutReg();
                        break;
                    case 0x3f:
                        msg = new RequestShortCutDel();
                        break;
					case 0x40: // t1 ??
						msg = new RequestBuyItem();
						break;
					case 0x42:
						msg = new RequestJoinParty();
						break;
					case 0x43:
						msg = new RequestAnswerJoinParty();
						break;
					case 0x44:
						msg = new RequestWithDrawalParty();
						break;
					case 0x45:
						msg = new RequestOustPartyMember();
						break;
					case 0x46:
						// RequestDismissParty
						break;
					case 0x47:
						msg = new CannotMoveAnymore();
						break;
					case 0x48:
						msg = new RequestTargetCanceld();
						break;
					case 0x49:
						msg = new Say2();
						break;
					case 0x4d:
						msg = new RequestPledgeMemberList();
						break;
					case 0x4f:
						msg = new DummyPacket();
						break;
					case 0x50:
						msg = new RequestSkillList();
						break;
					case 0x52:
						msg = new MoveWithDelta(); 
						break;
					case 0x53:
						msg = new RequestGetOnVehicle();  
						break;
					case 0x54:
						msg = new RequestGetOffVehicle();
						break;
					case 0x55:
						msg = new AnswerTradeRequest();
						break;
					case 0x56:
						msg = new RequestActionUse();
						break;
					case 0x57:
						msg = new RequestRestart();
						break;
					case 0x58:
						msg = new RequestSiegeInfo();   
						break;
					case 0x59: // t1 ??
						msg = new ValidatePosition();
						break;
//					case 0x5a:
//						// RequestSEKCustom
//						break;
//						THESE ARE NOW TEMPORARY DISABLED
					case 0x5b:
						//new StartRotating();
						break;
					case 0x5c:
						//new FinishRotating();
						break;
					case 0x5e:
						msg = new RequestShowBoard();
						break;
					case 0x5f:
						msg = new RequestEnchantItem();
						break;
					case 0x60:
						msg = new RequestDestroyItem();
						break;
                    case 0x62:
                        msg = new RequestQuestList();
                        break;
                    case 0x63:  
                        msg = new RequestQuestAbort();  
                        break;              
                    case 0x65:
                        msg = new RequestPledgeInfo();
                        break;
                    case 0x66:
                        msg = new RequestPledgeExtendedInfo();   
                        break;
                    case 0x67:
                        msg = new RequestPledgeCrest();
                        break;
                    case 0x6f:
                        msg = new RequestHennaEquip();
                        break;
                    case 0x73: // send when talking to trainer npc, to show list of available skills
                        msg = new RequestAquireSkillInfo();//  --> [s] 0xa4;
                        break;
					case 0x74:
						msg = new SendBypassBuildCmd();
						break;
					case 0x75:
						msg = new RequestMoveToLocationInVehicle();
						break;
					case 0x76:
						msg = new CannotMoveAnymoreInVehicle();
						break;
					case 0x77:
						msg = new RequestFriendInvite();
						break;
					case 0x78:
						msg = new RequestAnswerFriendInvite();
						break;
					case 0x79:
						msg = new RequestFriendList();
						break;
					case 0x7a:
						msg = new RequestFriendDel();
						break;
					case 0x7c: // send when a skill to be learned is selected
						msg = new RequestAquireSkill();
						break;
					case 0x7d:
						msg = new RequestRestartPoint();
						break;
					case 0x7e:
						msg = new RequestGMCommand();
						break;
                    case 0x7f:
                        msg = new RequestPartyMatchConfig();
                        break;
					case 0x80:
						msg = new RequestPartyMatchList();
						break;
					case 0x81:
						msg = new RequestPartyMatchDetail();
						break;
//                      case 0x82:
//                      // RequestPrivateStoreList
//                      break;
                    case 0x83:
                        msg = new RequestPrivateStoreBuy();
                        break;
                    case 0x85:
                        msg = new RequestTutorialLinkHtml();
                        break;
                    case 0x86:
                       	msg = new RequestTutorialPassCmdToServer();
                      	break;
                    case 0x87:
                        msg = new RequestTutorialQuestionMark();
                        break;
                    case 0x88:
                       	msg = new RequestTutorialClientEvent();
                      	break;
                    case 0x89:
                        msg = new RequestPetition();
                        break;
                    case 0x8a:
                        msg = new RequestPetitionCancel();
                        break;
                    case 0x8b:
                        msg = new RequestGmList();
                        break;
                    case 0x8c:
                        msg = new RequestJoinAlly();
                        break;
                    case 0x8d:
                        msg = new RequestAnswerJoinAlly();
                        break;
                    case 0x8e:
                        msg = new AllyLeave();
                        break;
                    case 0x8f:
                        msg = new AllyDismiss();
                        break;
                    case 0x90:
                        msg = new RequestDismissAlly();
                        break;
                    case 0x91:
                        msg = new RequestSetAllyCrest();
                        break;
                    case 0x92:
                        msg = new RequestAllyCrest();
                        break;
                    case 0x93:
                        msg = new RequestChangePetName();
                        break;
                    case 0x94:
                        msg = new RequestPetUseItem();
                        break;
                    case 0x95:
                        msg = new RequestGiveItemToPet();
                        break;
					case 0x96:
						msg = new RequestPrivateStoreQuitSell();
						break;
					case 0x97:
						msg = new SetPrivateStoreMsgSell();
						break;
					case 0x98:
						msg = new RequestPetGetItem();
						break;
					case 0x99:
						msg = new RequestPrivateStoreManageBuy();
						break;
					case 0x9a:
						msg = new SetPrivateStoreListBuy ();
						break;
//					case 0x92:
//						// RequestPrivateStoreBuyManageCancel
//						break;
					case 0x9c:
						msg = new RequestPrivateStoreQuitBuy();
						break;
					case 0x9d:
						msg = new SetPrivateStoreMsgBuy();
						break;
//					case 0x95:
//						// RequestPrivateStoreBuyList
//						break;
					case 0x9f:
						msg = new RequestPrivateStoreSell();
						break;
//					case 0x97:
//						// SendTimeCheckPacket
//						break;
//					case 0x98:
//						// RequestStartAllianceWar
//						break;
//					case 0x99:
//						// ReplyStartAllianceWar
//						break;
//					case 0x9a:
//						// RequestStopAllianceWar
//						break;
//					case 0x9b:
//		 				// ReplyStopAllianceWar
//						break;
//					case 0x9c:
//						// RequestSurrenderAllianceWar
//						break;
					case 0xa6:
						// RequestSkillCoolTime
						/*if (Config.DEBUG)
							_log.info("Request Skill Cool Time .. ignored");
						msg = null;*/
						break;
					case 0xa7:
						msg = new RequestPackageSendableItemList();
						break;
					case 0xa8:
						msg = new RequestPackageSend();
						break;
					case 0xa9:
						msg = new RequestBlock();
						break;
					case 0xaa:
						// RequestCastleSiegeInfo
						break;
					case 0xab:
		                msg = new RequestSiegeAttackerList();
		                break;
					case 0xac:
		                msg = new RequestSiegeDefenderList();
		                break;
					case 0xad:
		                msg = new RequestJoinSiege();
		                break;
					case 0xae:
					    msg = new RequestConfirmSiegeWaitingList();
					    break;
//					case 0xaf:
//						// RequestSetCastleSiegeTime
//						break;
					case 0xb0:
						msg = new MultiSellChoose();
						break;
//					case 0xb1:
//						// NetPing
//						break;
		            case 0xb3:
		            	msg = new RequestUserCommand();
		                break;
		            case 0xb4:
		            	msg = new SnoopQuit();
		            	break;
		            case 0xb5:  // we still need this packet to handle BACK button of craft dialog
		                msg = new RequestRecipeBookOpen();
		                break;
		            case 0xb6:
		                msg = new RequestRecipeBookDestroy();
		                break;
					case 0xb7:
						msg = new RequestRecipeItemMakeInfo();
					    break;
					case 0xb8:
						msg = new RequestRecipeItemMakeSelf();
					    break;
					//case 0xb9:
					//	msg = new RequestRecipeShopManageList(data, client);
					//    break;
		            case 0xba:
		            	msg = new RequestRecipeShopMessageSet();
		                break;
					case 0xbb:
						msg = new RequestRecipeShopListSet();
					    break;
					case 0xbc:
						msg = new RequestRecipeShopManageQuit();
					    break;
					case 0xbe:
						msg = new RequestRecipeShopMakeInfo();
					    break;
					case 0xbf:
						msg = new RequestRecipeShopMakeItem();
					    break;
					case 0xc0:
						msg = new RequestRecipeShopManagePrev();
					    break;
					case 0xc1:
						msg = new ObserverReturn();
					    break;
					case 0xc2:
						msg = new RequestEvaluate();
					    break;
					case 0xc3:
					    msg = new RequestHennaList();
					    break;
					case 0xc4:
						msg = new RequestHennaItemInfo();
						break;
					case 0xcc:
						// Clan Privileges
						msg = new RequestPledgePower();
						break;
		            case 0xcd:
		                msg = new RequestMakeMacro();
		                break;
		            case 0xce:
						msg = new RequestDeleteMacro();
						break;
					// Manor
		            case 0xcf:
		            	msg = new RequestBuyProcure();
		            	break;
		            case 0xc5:
		            	msg = new RequestBuySeed();
		            	break;
		            case 0xc6:
						msg = new DlgAnswer();
						break;
		            case 0xc7:
						msg = new RequestWearItem();
						break;
		            case 0xc8:
		            	msg = new RequestSSQStatus();
		            	break;
		            case 0xcb:
		                msg = new GameGuardReply();
		                break;
		            case 0x6b:
		            	msg = new RequestSendFriendMsg();
		            	break;
		            case 0x6c:
		                msg = new RequestShowMiniMap();
		                break;
		            case 0x6d: // MSN dialogs so that you dont see them in the console.
						break;
		            case 0x6e: //record video
		            	msg = new RequestRecordInfo();
		                break;

		            case 0xd0:
		            	int id2 = -1;
		            	if (buf.remaining() >= 2)
		            	{
		            		id2 = buf.getShort() & 0xffff;
		            	}
		            	else
		            	{
		            		_log.warning("Client: "+client.toString()+" sent a 0xd0 without the second opcode.");
		            		break;
		            	}

		                switch (id2)
		                {
                            case 0x01:
                                msg = new RequestManorList();
                                break;
                            case 0x02:
                                msg = new RequestProcureCropList();
                                break;
                            case 0x03:
                                msg = new RequestSetSeed();
                                break;
                            case 0x04:
                                msg = new RequestSetCrop();
                                break;
                            case 0x05:
                                msg = new RequestWriteHeroWords();
                                break;
                            case 0x06:
                                msg = new RequestExAskJoinMPCC();
                                break;
                            case 0x07:
                                msg = new RequestExAcceptJoinMPCC();
                                break;
                            case 0x08:
                                msg = new RequestExOustFromMPCC();
                                break;
		                	case 0x09:
		                		msg = new RequestOustFromPartyRoom();
		                		break;
		                	case 0x0a:
		                		msg = new RequestDismissPartyRoom();
		                		break;
		                	case 0x0b:
		                		msg = new RequestWithdrawPartyRoom();
		                		break;
		                    case 0x0c:
		                        msg = new RequestChangePartyLeader();
		                        break;
		                    case 0x0d:
		                        msg = new RequestAutoSoulShot();
		                        break;
		                    case 0x0e:
		                        msg = new RequestExEnchantSkillInfo();
		                        break;
		                    case 0x0f:
		                    	msg = new RequestExEnchantSkill();
		                    	break;
		                    case 0x10:
		                    	msg = new RequestExPledgeCrestLarge();
		                    	break;
		                    case 0x11:
		                    	msg = new RequestExSetPledgeCrestLarge();
		                    	break;
                            case 0x12:
                                msg = new RequestPledgeSetAcademyMaster();
                                break;
                            case 0x13:
                                msg = new RequestPledgePowerGradeList();
                                break;
                            case 0x14:
                                msg = new RequestPledgeMemberPowerInfo();
                                break;
                            case 0x15:
                                msg = new RequestPledgeSetMemberPowerGrade();
                                break;
                            case 0x16:
                                msg = new RequestPledgeMemberInfo();
                                break;
                            case 0x17:
                                msg = new RequestPledgeWarList();
                                break;
                            case 0x18:
                                msg = new RequestExFishRanking();
                                break;
                            case 0x19:
                                msg = new RequestPCCafeCouponUse();
                                break;
                            case 0x1b:
                                msg = new RequestDuelStart();
                                break;
                            case 0x1c:
                                msg = new RequestDuelAnswerStart();
                                break;
                            case 0x1e:
                                msg = new RequestExRqItemLink();
                                break;
                            case 0x21:
                                msg = new RequestKeyMapping(); 
                                break;
                            case 0x22:
                                // TODO implement me (just disabling warnings for this packet)
                                break;
                            case 0x24:
                                msg = new RequestSaveInventoryOrder();
                                break;
                            case 0x25:
                                msg = new RequestExitPartyMatchingWaitingRoom();
                                break;
                            case 0x26:
                                msg = new RequestConfirmTargetItem();
                                break;
                            case 0x27:
                                msg = new RequestConfirmRefinerItem();
                                break;
                            case 0x28:
                                msg = new RequestConfirmGemStone();
                                break;
		                    case 0x29:
		                    	msg = new RequestOlympiadObserverEnd();
		                    	break;
                            case 0x2a:
                                msg = new RequestCursedWeaponList();
                                break;
                            case 0x2b:
                                msg = new RequestCursedWeaponLocation();
                                break;
                            case 0x2c:
                                msg = new RequestPledgeReorganizeMember();
                                break;
                            case 0x2e:
                                msg = new RequestExMPCCShowPartyMembersInfo();
                                break;
		                    case 0x2f:
		                    	msg = new RequestOlympiadMatchList();
		                    	break;
		                    case 0x30:
		                        msg = new RequestAskJoinPartyRoom();
		                        break;
		                    case 0x31:
		                        msg = new AnswerJoinPartyRoom();
		                        break;
		                    case 0x32:
		                        msg = new RequestListPartyMatchingWaitingRoom();
		                        break;
                            case 0x33:
                                msg = new RequestExEnchantSkillSafe();
                                break;
                            case 0x34:
                                msg = new RequestExEnchantSkillUntrain();
                                break;
                            case 0x35:
                                msg = new RequestExEnchantSkillRouteChange();
                                break;
                            case 0x36:
                                msg = new ExGetOnAirShip();
                                break;
                            case 0x3f:
                                msg = new RequestAllCastleInfo();
                                break;
                            case 0x40:
                                msg = new RequestAllFortressInfo();
                                break;
                            case 0x41:
                                msg = new RequestAllAgitInfo();
                                break;
                            case 0x42:
                                msg = new RequestFortressSiegeInfo();
                                break;
		                    case 0x43:
		                        msg = new RequestGetBossRecord();
		                        break;
		                    case 0x44:
		                    	msg = new RequestRefine();
		                    	break;
		                    case 0x45:
		                    	msg = new RequestConfirmCancelItem();
		                    	break;
		                    case 0x46:
		                    	msg = new RequestRefineCancel();
		                    	break;
		                    case 0x47:
		                    	msg = new RequestExMagicSkillUseGround();
		                    	break;
		                    case 0x48:
		                    	msg = new RequestDuelSurrender();
		                    	break;
		                    case 0x49:
		                    	msg = new RequestExEnchantSkillInfoDetail();
		                    	break;
		                    default: 
		                     	this.printDebugDoubleOpcode(opcode, id2, buf, state, client);
		                    	break;
		                }
						break;
		            /*case 0xee:
						msg = new RequestChangePartyLeader(data, _client);
						break;*/
					default:
						this.printDebug(opcode, buf, state, client);
						break;
				}
				break;
		}
		else
		      switch (state)
		      {
		      case CONNECTED:
		         switch (opcode)
		         {
		         case 0:
		            break;
		         case 0xe:
		            msg = new ProtocolVersion();
		            break;
		         case 0x48:
		            msg = new AuthLogin();
		            break;
		         default:
		            printDebug(opcode, buf, state, client);
		         break;
		         }
		         break;
		      case AUTHED:
		         switch(opcode)
		         {
		         case 0xaa:
		            msg = new Logout();
		            break ;
		         case 0x95:
		            msg = new CharacterCreate();
		            break ;
		         case 0x10:
		            msg = new CharacterDelete();
		            break ;
		         case 0x12:
		            msg = new CharacterSelect();
		            break ;
		         case 0xc4:
		            msg = new NewCharacter();
		            break ;
		         case 0x62:
		            msg = new CharacterRestore();
		            break ;
		         case 0x69:
		                msg = new RequestPledgeCrest();
		                break;
		            case 0xbe:
		            int idx = -1;
		            if(buf.remaining() >= 2)
		            {
		               idx = buf.getShort() & 0xffff;
		            }
		            else
		            {
		               break ;
		            }
		            // single packet
		            if (idx == 0x41)
		            {
		               msg = new RequestGotoLobby();
		            }
		            else
		            {
		               this.printDebugDoubleOpcode(opcode, idx, buf, state, client);
		            }
		            
		            break;
		         default:
		            printDebug(opcode, buf, state, client);
		         break;
		         }
		         break;
		      case IN_GAME:
		         switch(opcode)
		         {
		         case 0xaa: //v
		            msg = new Logout();
		            break ;
		            
		         case 0x86: //v
		            msg = new AttackRequest();
		            break ;
		            
		         case 0x9c://????v
		            msg = new RequestStartPledgeWar();
		            break;
		            
		            //case 0x0e://?? ??? ?? ??
		            //   msg = new RequestReplyStartPledgeWar();
		            //   break;
		            
		         case 0x82://?? ??v
		            msg = new RequestStopPledgeWar();
		            break;
		            
		            //case 0x06://?? ??? ?? ??
		            //   msg = new RequestReplyStopPledgeWar();
		            //   break;
		            
		            //case 0xb2:
		              //  msg = new RequestRemainTime();
		              //  break;
		               
		         case 0x12://v
		            msg = new GameGuardReply();
		            break;
		            
		            //case 0x0a://?? ??
		            //   msg = new RequestReplySurrenderPledgeWar();
		            //   break;
		            
		         case 0x63://?? ????v
		            msg = new RequestGiveNickName();
		            break;
		            
		         case 0x43://v
		            msg = new MoveBackwardToLocation();
		            break ;
		            
		         case 0xbd: //??v
		            msg = new EnterWorld();
		            break ;
		            
		         case 0xbf: //??? ???v
		            msg = new RequestItemList();
		            break ;
		            
		         case 0x6e: //??? ?? ?? v
		            msg = new RequestUnEquipItem();
		            break ;
		            
		         case 0x19: //??? ??v
		            msg = new RequestDropItem();
		            break ;
		            
		         case 0xa6: //??? ??v
		            msg = new UseItem();
		            break ;
		            
		         case 0x61: //?? ??v
		            msg = new TradeRequest();
		            break ;
		            
		         case 0x90://?? ??? ??v
		            msg = new AddTradeItem();
		            break;
		            
		         case 0x8a://?? ??v
		            msg = new TradeDone();
		            break;
		            
		         case 0x91: //v
		            msg = new Action();
		            break ;
		            
		         case 0x2b: //v
		            msg = new RequestBypassToServer();
		            break ;
		            
		         case 0x71://v
		            msg = new RequestBBSwrite();
		            break ;
		            
		         case 0xb5://?? ??v
		            msg = new RequestJoinPledge();
		            break;
		            
		         case 0x3e://?? ?? ??v
		            msg = new RequestAnswerJoinPledge();
		            break;
		            
		         case 0x87://?? ??v
		            msg = new RequestWithdrawalPledge();
		            break;
		            
		         case 0xc6://??? ??v
		            msg = new RequestOustPledgeMember();
		            break;
		            
		         case 0x07://????? ??? ????v
		            msg = new RequestGetItemFromPet();
		            break;
		            
		         case 0x46://???v
		            msg = new RequestCrystallizeItem();
		            break;
		            
		            //case 0x30: //?????????????????
		            //   msg = new RequestPrivateStoreManageSell();
		            //   break;
		            //case 0x96://?????????????????
		            //   msg = new RequestPrivateStoreManageBuy();
		            //   break;
		            
		           case 0x2c://??? ??v
		            msg = new SetPrivateStoreListSell();
		            break ;
		            
		         case 0x7f://??v
		            msg = new RequestSocialAction();
		            break ;
		            
		            //case 0x85://?????????????????
		            //   msg = new ChangeMoveType2();
		            //   break;
		            //case 0x36://?????????????????
		            //   msg = new ChangeWaitType2();
		            //   break;
		         //case 0x8d://?????????????????
		         //   msg = new MoveWithDelta();
		         //   break;
		            
		         case 0x89: //??? ??v
		            msg = new RequestSellItem();
		            break ;
		            
		         case 0x38://?? ??v
		            msg = new RequestSkillList();
		            break;
		            
		         case 0x05: //?? ??v
		            msg = new RequestMagicSkillUse();
		            break ;
		            
		         case 0x4d: //????, ?????v
		            msg = new Appearing();
		            break ;
		            
		         case 0xb2: //?? ???v
		            msg = new SendWareHouseDepositList();
		            break ;
		            
		         case 0x1b://?? ??v
		            msg = new SendWareHouseWithDrawList();
		            break ;
		            
		         case 0xac: //?? ?? v
		            msg = new RequestShortCutReg();
		            break ;
		            
		         case 0x99: //?? ??v
		            msg = new RequestShortCutDel();
		            break ;
		            
		         case 0x3a: //??? ??v
		            msg = new RequestBuyItem();
		            break ;
		            
		         case 0x60: //?? ??v
		            msg = new RequestJoinParty();
		            break ;
		            
		         case 0x11://?? ?? ??v
		            msg = new RequestAnswerJoinParty();
		            break;
		            
		         case 0x17: //?? ??v
		            msg = new RequestWithDrawalParty();
		            break ;
		            
		         case 0xc5: //??? ??v
		            msg = new RequestOustPartyMember();
		            break ;
		            
		         case 0x8e://?
		             msg = new CannotMoveAnymore();
		            break;
		            
		         case 0xcd: //v
		            msg = new RequestTargetCanceld();
		            break ;
		            
		         case 0x2d://v
		            msg = new Say2();
		            break ;
		            
		         case 0x5d://??? ???v
		            msg = new RequestPledgeMemberList();
		            break;
		            
		         case 0xb0: //?? ??(?? ??)v
		            msg = new RequestSkillList();
		            break ;
		         
		         case 0xa5://???v
		            msg = new RequestGetOnVehicle();
		            break;
		            
		         case 0x3d://????v
		            msg = new RequestGetOffVehicle();
		            break;
		            
		         case 0x55://?? ??v
		            msg = new AnswerTradeRequest();
		            break;
		            
		         case 0xb9: //?? ??v
		            msg = new RequestActionUse();
		            break ;
		            
		         case 0x04: //????v
		            msg = new RequestRestart();
		            break ;
		            
		            //case 0x34://?? ??
		            //msg = new RequestSiegeInfo();
		            //break;
		            
		         case 0x80: //??? ??,????v
		            msg = new ValidatePosition();
		            break ;
		            
		         case 0x9f: //??? ????v
		            msg = new StartRotating();
		            break ;
		            
		         case 0x35://??? ????v
		            msg = new FinishRotating();
		            break ;
		            
		         case 0x5e: //???v
		            msg = new RequestShowBoard();
		            break ;
		            
		         case 0x9b://??? ??v
		            msg = new RequestEnchantItem();
		            break;
		            
		         case 0x37://??? ??v
		            msg = new RequestDestroyItem();
		            break ;
		            
		         case 0x62://??? ???v
		            msg = new RequestQuestList();
		            break;
		            
		         case 0x65: //??? ??v
		            msg = new RequestQuestAbort();
		            break ;
		            
		         case 0x88://?? ??v
		            msg = new RequestPledgeInfo();
		            break;
		            
		         case 0x5a://?? ??v - /???? ??? ??
		            msg = new RequestAllyInfo();
		            break;
		            
		            //case 0x66://?????????????????
		            //   msg = new RequestPledgeExtendedInfo();
		            //   break;
		            
		         case 0xa8://?? ?? ??v
		            msg = new RequestSetPledgeCrest();
		            break;
		            
		         case 0x69://?? ??v
		            msg = new RequestPledgeCrest();
		            break;
		            
		         case 0x54://?? ?? ??v
		            msg = new RequestSetAllyCrest();
		            break;
		            
		         case 0x24://?? ??v
		            msg = new RequestAllyCrest();
		            break;
		            
		         case 0x97://?? ??? ??v
		            msg = new RequestSendFriendMsg();
		            break;
		            
		         case 0x64: //???v
		            msg = new RequestShowMiniMap();
		            break ;
		            
		         case 0x83: //???? v
		            msg = new RequestRecordInfo();
		            break ;
		            
		         case 0x5b://?? ??v
		            msg = new RequestHennaEquip();
		            break;
		            
		            case 0xc8://?? - ????v
		                msg = new RequestHennaList();
		                break;
		               
		            case 0x09://?? ?v
		                msg = new RequestHennaItemInfo();
		                break;
		               
		            case 0x4f: //?? ??v
		            msg = new RequestAquireSkillInfo();
		            break ;
		            
		         case 0xa2: //v
		            msg = new SendBypassBuildCmd();
		            break ;
		            
		         case 0x0e://?? ??v
		            msg = new RequestMoveToLocationInVehicle();
		            break;
		            
		         case 0x4e://???? ???? ??v
		            msg = new CannotMoveAnymoreInVehicle();
		            break;
		            
		         case 0xa3://????v
		            msg = new RequestFriendInvite();
		            break;
		            
		         case 0xc3://?? ?? ??v
		            msg = new RequestAnswerFriendInvite();
		            break;
		            
		         case 0xb4://?? ???v
		            msg = new RequestFriendList();
		            break;
		            
		         case 0x4c://??v
		            msg = new RequestFriendDel();
		            break;
		            
		         case 0x39: //?? ???v
		            msg = new RequestAquireSkill();
		            break ;
		            
		         case 0xb6://???? ???v
		            msg = new RequestRestartPoint();
		            break;
		            
		         case 0x2f: //?? ???v
		            msg = new RequestGMCommand();
		            break ;
		            
		            // case 0x82://?? ??
		            //   msg = new RequestPrivateStoreList
		            //   break;
		            
		         case 0x03://???? ????v
		            msg = new RequestPrivateStoreBuy();
		            break;
		            
		         case 0x7e://
		            msg = new RequestTutorialLinkHtml();
		            break;
		         case 0xbb://
		            msg = new RequestTutorialPassCmdToServer();
		            break;
		         case 0x8f://????
		            msg = new RequestTutorialQuestionMark();
		            break;
		            
		         case 0x45://??v
		            msg = new RequestPetition();
		            break ;
		            
		         case 0x25://?? ?? v
		            msg = new RequestPetitionCancel();
		            break ;
		            
		         case 0x6c: //?????v
		            msg = new RequestGmList();
		            break ;
		            
		         case 0x2e://?? ??v
		            msg = new RequestJoinAlly();
		            break;
		            
		         case 0x44://?? ?? ??v
		            msg = new RequestAnswerJoinAlly();
		            break;
		            
		         case 0x4a://?? ??v
		            msg = new AllyLeave();
		            break;
		            
		         case 0xa4://?? ??v
		            msg = new AllyDismiss();
		            break;
		            
		         case 0x76://?? ??v
		            msg = new RequestDismissAlly();
		            break;
		            
		         case 0x98://? ?? ???v
		            msg = new RequestChangePetName();
		            break;
		            
		         case 0x32://?? ?? ?? ???
		            msg = new RequestPetUseItem();
		            break;
		            
		         case 0x6d://??? ??? ?? v
		            msg = new RequestGiveItemToPet();
		            break;
		            
		         //case 0x8d://??
		         //   msg = new RequestLastMotion();
		         //   break;
		            
		         case 0x26: //v
		            msg = new RequestPrivateStoreQuitSell();
		            break ;
		            
		         case 0x93: //v
		            msg = new SetPrivateStoreMsgSell();
		            break ;
		            
		         case 0x30://v
		            msg = new RequestPetGetItem();
		            break;
		         
		         case 0x08: //v
		            msg = new SetPrivateStoreListBuy();
		            break ;
		            
		         case 0x9e: //v
		            msg = new RequestPrivateStoreQuitBuy();
		            break ;
		            
		         case 0xcb: //v
		            msg = new SetPrivateStoreMsgBuy();
		            break ;
		            
		         case 0x01://v
		            msg = new RequestPrivateStoreSell();
		            break;
		            
		         case 0x81: //v
		            //msg = new RequestSkillCoolTime();
		            break ;
		            
		         case 0xce://???v
		            msg = new RequestPackageSendableItemList();
		            break;
		            
		         case 0x21://???v
		            msg = new RequestPackageSend();
		            break;
		            
		         case 0x9a://?? v
		            msg = new RequestBlock();
		            break;
		            
		         case 0x34://?? ?? - ??? (??)v
		            msg = new RequestSiegeAttackerList();
		            break;
		            
		         case 0x75://?? ?? - ??? (??) v
		            msg = new RequestSiegeDefenderList();
		            break;
		            
		         case 0x6f://?? ??v
		            msg = new RequestJoinSiege();
		            break;
		            
		          case 0x85://?? ?? ???
		            msg = new RequestConfirmSiegeWaitingList();
		            break;
		            
		         case 0xc2://??? ?? v
		            msg = new MultiSellChoose();
		            break;
		            
		         case 0x8b: //?? ???v
		            msg = new RequestUserCommand();
		            break ;
		            
		         case 0x18://?? ??v
		            msg = new SnoopQuit();
		            break;
		            
		          case 0xae://???v
		            msg = new RequestRecipeBookOpen();
		            break;
		            
		         case 0x1e://???v
		            msg = new RequestRecipeBookDestroy();
		            break;
		            
		         case 0xab://???v
		            msg = new RequestRecipeItemMakeInfo();
		            break;
		            
		         case 0x14://???v
		            msg = new RequestRecipeItemMakeSelf();
		            break;
		            
		         case 0x5c://???v
		            msg = new RequestRecipeShopMessageSet();
		            break ;
		            
		         case 0x7c://???v
		            msg = new RequestRecipeShopListSet();
		            break ;
		            
		         case 0x5f: //???v
		            msg = new RequestRecipeShopManageQuit();
		            break ;
		            
		         case 0x52://?? ?? ??v
		            msg = new RequestRecipeShopMakeInfo();
		            break;
		            
		         case 0x06://?? ??? ??v
		            msg = new RequestRecipeShopMakeItem();
		            break;
		            
		         case 0x67://???v
		              msg = new RequestRecipeShopManagePrev();
		              break;
		            
		         case 0xa7://?? ??v
		            msg = new ObserverReturn();
		            break;
		            
		         case 0x0d: //v
		            msg = new RequestEvaluate();
		            break ;
		            
		         case 0x92://?? ??
		            msg = new RequestBuySeed();
		            break;
		            
		         case 0xcf://?? ?? ??? ??v
		            msg = new DlgAnswer();
		            break;
		            
		         case 0x31://????v
		            msg = new RequestWearItem();
		            break;
		            
		         case 0x42://v
		            msg = new RequestSSQStatus();
		            break;
		            
		         case 0x13: //v
		            msg = new GameGuardReply();
		            break ;
		            
		         case 0x78://?? ??v
		            msg = new RequestPledgePower();
		            break;
		            
		            //               case 0x5a:
		            //                  // RequestSEKCustom
		            //                  break;
		            //                  THESE ARE NOW TEMPORARY DISABLED
		            //               case 0x97:
		            //                  // SendTimeCheckPacket
		            //                  break;
		            //               case 0x98:
		            //                  // RequestStartAllianceWar
		            //                  break;
		            //               case 0x99:
		            //                  // ReplyStartAllianceWar
		            //                  break;
		            //               case 0x9a:
		            //                  // RequestStopAllianceWar
		            //                  break;
		            //               case 0x9b:
		            //                   // ReplyStopAllianceWar
		            //                  break;
		            //               case 0x9c:
		            //                  // RequestSurrenderAllianceWar
		            //                  break;
		            //               case 0xaf:
		            //                  // RequestSetCastleSiegeTime
		            //                  break;
		            // RequestSkillCoolTime
		            /*if (Config.DEBUG)
		               _log.info("Request Skill Cool Time .. ignored");
		            msg = null;*/
		            
		         /** @param ??? ?? ?? **/
		         case 0xba: //??? ??? v
		            msg = new RequestMakeMacro();
		            break ;
		         case 0x7d: //??? ?? v
		            msg = new RequestDeleteMacro();
		            break ;
		            
		         //case 0x92://?? ??
		         //   msg = new RequestBuyProcure();
		         //   break;
		            
		         /** @param ?? ?? ?? ?? ?? **/
		         case 0xca: ////?? ?? ???v
		            msg = new RequestPartyMatchConfig();
		            break ;
		         case 0x22://?? ?? ???v
		            msg = new RequestPartyMatchList();
		            break;
		         //case 0xca://?? ?? ???
		         //   msg = new RequestPartyMatchDetail();
		         //   break;
		            
		         case 0xbe:
		            int id2 = -1;
		            if(buf.remaining() >= 2)
		            {
		               id2 = buf.getShort() & 0xffff;
		            }
		            else
		            {
		               
		               break ;
		            }
		            switch(id2)
		            {
		            
		            /** ?? ??? ?? ?? **/
		            case 0x44://????? ?? ???
		               msg = new RequestManorList();
		               break;
		            //case 0x45://?? ???
		            //   msg = new RequestProcureCropList();
		            //   break;
		            case 0x00://?? ????v
		               msg = new RequestSetSeed();
		               break;
		            case 0x1b://?? ????v
		               msg = new RequestSetCrop();
		               break;
		            case 0x05://?? ?? ? v
		               msg = new RequestWriteHeroWords();
		               break;

		            /** ?? ??? ?? ?? **/
		            case 0x18://?? ??? ??v
		               msg = new RequestExEnchantSkillInfo();
		               break;
		            case 0x07://?? ???v
		               msg = new RequestExEnchantSkill();
		                  break;
		            case 0x1c://??? ?? ???v
		               msg = new RequestExEnchantSkillSafe();
		               break;
		            case 0x0b://???? ?? ???v
		               msg = new RequestExEnchantSkillUntrain();
		               break;
		            case 0x39://?? ??? ?? ???v
		               msg = new RequestExEnchantSkillRouteChange();
		               break;
		            case 0x1d://???? ?? ??? ?? v
		               msg = new RequestExEnchantSkillInfoDetail();
		               break;
		               
		            /** ?? ? ?? ?? **/
		            case 0x4a://???? ??v
		               msg = new RequestExPledgeCrestLarge();
		               break;
		            case 0x2f://???? ??/????v
		               msg = new RequestExSetPledgeCrestLarge();
		               break;
		            case 0x1a://??? ??v
		               msg = new RequestPledgeSetAcademyMaster();
		               break;
		            case 0x48://?? ???v
		               msg = new RequestPledgePowerGradeList();
		               break;
		            case 0x2b://?? ??v
		               msg = new RequestPledgeMemberPowerInfo();
		               break;
		            case 0x33://?? ??v
		               msg = new RequestPledgeSetMemberPowerGrade();
		               break;
		            case 0x23://??? ??v
		               msg = new RequestPledgeMemberInfo();
		               break;
		            case 0x03://?? ??v - ?? ???v
		               msg = new RequestPledgeWarList();
		               break;
		            case 0x40://?? ??v
		               msg = new RequestPledgeReorganizeMember();
		               break;
		            
		            /** ?? ?? ?? **/
		            case 0x35: //?? ??v
		               msg = new RequestDuelStart();
		               break;
		            case 0x2a://?? ??v
		               msg = new RequestDuelAnswerStart();
		               break;
		            case 0x32: //v
		               msg = new RequestDuelSurrender();
		               break;
		               
		            /** ????? **/
		            case 0x4c:///Rq
		               msg = new RequestExRqItemLink();
		               break;
		            case 0x3a: //v
		               msg = new RequestKeyMapping();
		               break;
		            case 0x20: //v
		               //msg = new RequestSaveKeyMapping();
		               break;
		            case 0x0e: //v
		               msg = new RequestSaveInventoryOrder();
		               break;
		               //case 0x18:
		               //   msg = new RequestExFishRanking();
		               //   break;
		            case 0x1e://PC? ??? ??v
		               msg = new RequestPCCafeCouponUse();
		               break;
		            case 0x17://?? ?? ??v
		               msg = new RequestAutoSoulShot();
		               break;
		               
		            /** ?? ?? ?? ?? ?? **/
		            case 0x28: //???? ?? ???v
		               msg = new RequestCursedWeaponList();
		               break;
		            case 0x0c://???? ?? ????v
		               msg = new RequestCursedWeaponLocation();
		               break;

		            /** ?? ?? ?? **/
		            case 0x16://?? ??v
		               msg = new RequestExAskJoinMPCC();
		               break;
		               //case 0x0a://???? ?? ?
		               //   msg = new RequestExAcceptJoinMPCC();
		               //   break;
		               //case 0x08://?? ?? ?
		               //   msg = new RequestExOustFromMPCC();
		               //   break;
		               //case 0x2f://?? ? (????)  ??
		               //msg = new RequestExMPCCShowPartyMembersInfo();
		               //break;
		               
		               
		            /** ????? ?? ?? **/
		               //case 0x2f:
		               //   msg = new RequestOlympiadMatchList();
		               //   break;
		               //case 0x?:
		               //   msg = new RequestOlympiadObserverEnd();
		               //   break;
		               
		            /** ?? ?? ?? **/
		               //case 0x30://???? ??(???)
		               //   msg = new RequestAskJoinPartyRoom();
		               //   break;
		               //case 0x31://(???)
		               //   msg = new AnswerJoinPartyRoom();
		               //   break;
		               //case 0x03://(???)
		               //   msg = new RequestOustFromPartyRoom();
		               //   break;
		               //case 0x0a://(???)
		               //   msg = new RequestDismissPartyRoom();
		               //   break;
		               //case 0x0b://(???)
		               //   msg = new RequestWithdrawPartyRoom();
		               //   break;
		            case 0x3c://??? ??v
		               msg = new RequestChangePartyLeader();
		               break;
		               
		               
		            /** ?? ?? ?? ?? **/
		            case 0xd://???? ??v
		               msg = new RequestListPartyMatchingWaitingRoom();
		               break;
		            case 0x2://???? ???v
		               msg = new RequestExitPartyMatchingWaitingRoom();
		               break;

		            /** ? ?? ?? ?? **/
		            case 0x3b:
		               msg = new ExGetOnAirShip();
		               break;
		               
		            /** ? ?? ??? ?? **/
		            case 0x3f://v
		               msg = new RequestAllCastleInfo();
		               break;
		            case 0x12: //v
		               msg = new RequestAllFortressInfo();
		               break;
		            case 0x37: //v
		               msg = new RequestAllAgitInfo();
		               break;
		            case 0x49://v
		               msg = new RequestFortressSiegeInfo();
		               break;
		            case 0x46: //v
		               msg = new RequestGetBossRecord();
		               break;
		               
		            /** ?? ??(??) ?? **/
		            case 0x10://?? ?? (??)v
		               msg = new RequestExMagicSkillUseGround();
		               break;
		               
		            /** ?? ?? ?? **/
		            case 0x06://?? ? ???v
		               msg = new RequestConfirmTargetItem();
		               break;
		            case 0x19://??v
		               msg = new RequestConfirmRefinerItem();
		               break;
		            case 0x2e://???v
		               msg = new RequestConfirmGemStone();
		               break;
		            case 0x27://??? ?? v
		               msg = new RequestRefine();
		               break;
		            case 0x13://?? ??????v
		               msg = new RequestConfirmCancelItem();
		               break;
		            case 0x29://?? ??v
		               msg = new RequestRefineCancel();
		               break;
		               
		            /** ?? ??? ?? **/
		            //case 0x22:
		            //   msg = new RequestAttriEnchantItem();
		            //   break;
		               
		            default:
		               this.printDebugDoubleOpcode(opcode, id2, buf, state, client);
		            break;
		            }
		            break;
		            
		         default:
		            this.printDebug(opcode, buf, state, client);
		         break;
		         }
		         break;
		      } 
		return msg;
	}

	private void printDebug(int opcode, ByteBuffer buf, GameClientState state, L2GameClient client)
	{
		int size = buf.remaining();
     	_log.warning("Unknown Packet: "+Integer.toHexString(opcode)+" on State: "+state.name()+" Client: "+client.toString());
     	byte[] array = new byte[size];
     	buf.get(array);
     	_log.warning(Util.printData(array, size));
	}

	private void printDebugDoubleOpcode(int opcode, int id2, ByteBuffer buf, GameClientState state, L2GameClient client)
	{
		int size = buf.remaining();
     	_log.warning("Unknown Packet: "+Integer.toHexString(opcode)+":" + Integer.toHexString(id2)+" on State: "+state.name()+" Client: "+client.toString());
     	byte[] array = new byte[size];
     	buf.get(array);
     	_log.warning(Util.printData(array, size));
	}

	// impl
	public L2GameClient create(MMOConnection<L2GameClient> con)
	{
		return new L2GameClient(con);
	}

	public void execute(ReceivablePacket<L2GameClient> rp)
	{
		try
		{
			if (rp.getClient().getState() == GameClientState.IN_GAME)
			{
				ThreadPoolManager.getInstance().executePacket(rp);
			}
			else
			{
				ThreadPoolManager.getInstance().executeIOPacket(rp);
			}
		}
		catch (RejectedExecutionException e)
		{
			// if the server is shutdown we ignore
			if (!ThreadPoolManager.getInstance().isShutdown())
			{
				_log.severe("Failed executing: "+rp.getClass().getSimpleName()+" for Client: "+rp.getClient().toString());
			}
		}
	}
    
    /**
     * @see org.mmocore.network.TCPHeaderHandler#handleHeader(java.nio.channels.SelectionKey, java.nio.ByteBuffer)
     */
    @SuppressWarnings("unchecked")
    @Override
    public HeaderInfo handleHeader(SelectionKey key, ByteBuffer buf)
    {
        if (buf.remaining() >= 2)
        {
            int dataPending = (buf.getShort() & 0xffff) - 2;
            L2GameClient client = ((MMOConnection<L2GameClient>) key.attachment()).getClient(); 
            return this.getHeaderInfoReturn().set(0, dataPending, false, client);
        }
        else
        {
            L2GameClient client = ((MMOConnection<L2GameClient>) key.attachment()).getClient(); 
            return this.getHeaderInfoReturn().set(2 - buf.remaining(), 0, false, client);
        }
    }
}
