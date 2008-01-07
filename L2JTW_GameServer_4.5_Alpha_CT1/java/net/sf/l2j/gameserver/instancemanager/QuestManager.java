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

import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastMap;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.scripting.ScriptManager;

public class QuestManager extends ScriptManager<Quest>
{
    protected static final Logger _log = Logger.getLogger(QuestManager.class.getName());

    // =========================================================
    private static QuestManager _instance;
    public static final QuestManager getInstance()
    {
        if (_instance == null)
        {
    		System.out.println("Initializing QuestManager");
            _instance = new QuestManager();
        }
        return _instance;
    }
    // =========================================================


    // =========================================================
    // Data Field
    private Map<String, Quest> _quests = new FastMap<String, Quest>();

    // =========================================================
    // Constructor
    public QuestManager()
    {
    }

    // =========================================================
    // Method - Public
    public final boolean reload(String questFolder)
    {
    	Quest q = getQuest(questFolder);
    	if (q == null)
    	{
            return false;
    	}
    	return q.reload();
    }
    
    /**
     * Reloads a the quest given by questId.<BR>
     * <B>NOTICE: Will only work if the quest name is equal the quest folder name</B>
     * @param questId The id of the quest to be reloaded
     * @return true if reload was succesful, false otherwise
     */
    public final boolean reload(int questId)
    {
    	Quest q = this.getQuest(questId);
    	if (q == null)
    	{
    		return false;
    	}
    	return q.reload();
    }
    
    public final void report()
    {
        _log.info("Loaded: " + getQuests().size() + " quests");
    }
    
    public final void save()
    {
    	for (Quest q: getQuests().values())
        {
    		q.saveGlobalData();
        }
    }

    // =========================================================
    // Property - Public
    public final Quest getQuest(String name)
    {
		return getQuests().get(name);
    }

    public final Quest getQuest(int questId)
    {
    	for (Quest q: getQuests().values())
    	{
    		if (q.getQuestIntId() == questId)
    			return q;
    	}
    	return null;
    }
    

    public final void addQuest(Quest newQuest)
    {
        if (newQuest == null)
        {
            throw new IllegalArgumentException("Quest argument cannot be null");
        }
    	Quest old = this.getQuests().put(newQuest.getName(), newQuest);
        if (old != null)
        {
            _log.info("Replaced: ("+old.getName()+") with a new version ("+newQuest.getName()+")");
        }
    }
    
    public final boolean removeQuest(Quest q)
    {
        return this.getQuests().remove(q.getName()) != null;
    }
    
    public final FastMap<String, Quest> getQuests()
    {
        if (_quests == null) _quests = new FastMap<String, Quest>();
        return (FastMap<String, Quest>) _quests;
    }

    /**
     * @see net.sf.l2j.gameserver.scripting.ScriptManager#getAllManagedScripts()
     */
    public Iterable<Quest> getAllManagedScripts()
    {
        return _quests.values();
    }

    /**
     * @see net.sf.l2j.gameserver.scripting.ScriptManager#unload(net.sf.l2j.gameserver.scripting.ManagedScript)
     */
    public boolean unload(Quest ms)
    {
        ms.saveGlobalData();
        return this.removeQuest(ms);
    }

    /**
     * @see net.sf.l2j.gameserver.scripting.ScriptManager#getScriptManagerName()
     */
    @Override
    public String getScriptManagerName()
    {
        return "QuestManager";
    }
}