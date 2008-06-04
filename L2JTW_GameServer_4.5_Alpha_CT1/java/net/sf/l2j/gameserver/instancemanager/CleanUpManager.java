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
package net.sf.l2j.gameserver.instancemanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.entity.ClanHall;

/**
 * @author  TSL
 */
public class CleanUpManager
{
	private static Logger _log = Logger.getLogger(CleanUpManager.class.getName());
	
	private static CleanUpManager _instance;
	
	public static CleanUpManager getInstance()
	{
		if (_instance == null)
		{
			System.out.println("Initializing CleanUpManager");
			_instance = new CleanUpManager();
		}
		return _instance;
	}
	
	private CleanUpManager()
	{
		doCleanupCharacter();
		doCleanupClan();
		doCleanupClanHall();
	}
	
    public void doCleanupCharacter(){
        /*
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            ResultSet result;
            
            statement = con.prepareStatement("SELECT obj_id, account_name, char_name, lastaccess"
	            							 + " FROM characters"
											 + " WHERE lastaccess < (UNIX_TIMESTAMP()*1000 - 86400000*90)"
											 + " AND clanid = 0 AND accesslevel <= 0"
											 + " ORDER BY account_name, lastaccess");
			result = statement.executeQuery();
			while(result.next())
			{
				_log.info("[CHECK CHARADATA] DELETE CHARACTER(ID:"+result.getString("account_name")+", NAME:"+result.getString("char_name")+")");
				deleteCharByObjId(result.getInt("obj_id"));
			}
			result.close();
			statement.close();
        }
        catch (Exception e)
        {
            _log.warning("could not cleanup character:"+e);
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }*/
    }
	
    public void doCleanupClan(){
        /*
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            ResultSet result;
            
            statement = con.prepareStatement("SELECT clan_data.clan_id, clan_name, char_name, lastaccess"
											 + " FROM clan_data, characters"
											 + " WHERE clan_data.leader_id = characters.obj_id"
											 + " AND characters.lastaccess < (UNIX_TIMESTAMP()*1000 - 86400000*60)"
											 + " ORDER BY clan_data.clan_id");
			result = statement.executeQuery();
			while(result.next())
			{
        		_log.info("[CHECK CLANDATA] DELETE CLAN(NAME:"+result.getString("clan_name")+", LEADER:"+result.getString("char_name")+")!");
        		ClanTable.getInstance().destroyClan(Integer.parseInt(result.getString("clan_id")));
            }
			result.close();
			statement.close();
        }
        catch (Exception e)
        {
            _log.warning("could not cleanup clan:"+e);
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }*/
    }
	
    public void doCleanupClanHall(){
        /*
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            ResultSet result;
            
            statement = con.prepareStatement("SELECT clanhall.id, clan_data.clan_name"
            								 + " FROM clanhall, clan_data, characters"
											 + " WHERE clanhall.ownerid = clan_data.clan_id"
											 + " AND clan_data.leader_id = characters.obj_id"
											 + " AND characters.lastaccess < (UNIX_TIMESTAMP()*1000 - 86400000*14)"
											 + " ORDER BY clanhall.id");
            result = statement.executeQuery();
            while(result.next())
            {
            	ClanHall clanhall = ClanHallManager.getInstance().getClanHallById(result.getInt("id"));
            	_log.info("[CHECK CLANHALL] "+result.getString("clan_name")+" part with ClanHall("+clanhall.getName()+")!");
            	ClanHallManager.getInstance().setFree(result.getInt("id"));
            }
            statement.close();
        }
        catch (Exception e)
        {
            _log.warning("could not cleanup clanhall:"+e);
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }*/
    }
    
	public void deleteCharByObjId(int objid)
	{
	    if (objid < 0)
	        return;
        
	    java.sql.Connection con = null;
        
		try 
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement ;

        	statement = con.prepareStatement("DELETE FROM character_friends WHERE charId=? OR friendId=?");
			statement.setInt(1, objid);
			statement.setInt(2, objid);
			statement.execute();
			statement.close();
            
            statement = con.prepareStatement("DELETE FROM character_hennas WHERE charId=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
	
			statement = con.prepareStatement("DELETE FROM character_macroses WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_quests WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_recipebook WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
	
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
	
			statement = con.prepareStatement("DELETE FROM character_skills WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_subclasses WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
            
            statement = con.prepareStatement("DELETE FROM heroes WHERE charId=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
            
            statement = con.prepareStatement("DELETE FROM olympiad_nobles WHERE charId=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
            
            statement = con.prepareStatement("DELETE FROM seven_signs WHERE charId=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();

        	statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM augmentations WHERE item_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM items WHERE owner_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM merchant_lease WHERE player_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM characters WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Data error on deleting character: " + e);
		} 
		finally 
		{
			try { con.close(); } catch (Exception e) {}
		}
	}
}
