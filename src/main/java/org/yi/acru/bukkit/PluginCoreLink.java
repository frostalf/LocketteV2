//
// This file is a component of PluginCore for Bukkit, and was written by Acru Jovian.
// Distributed under the The Non-Profit Open Software License version 3.0 (NPOSL-3.0)
// http://www.opensource.org/licenses/NOSL3.0
//


package org.yi.acru.bukkit;


//Imports.
//import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
//import org.anjocaido.groupmanager.GroupManager;
//import org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder;
import com.gmail.nossr50.mcMMO;
import com.griefcraft.lwc.LWCPlugin;
import com.massivecraft.factions.Factions;
import com.palmergames.bukkit.towny.Towny;
import net.milkbowl.vault.Vault;
import org.bukkit.plugin.Plugin;

public class PluginCoreLink{
	private static PluginCore plugin;
	
	public enum LinkType{
		NONE, GROUPS, PERMISSIONS, ZONES, ECONOMY,
		GROUPS_PERMISSIONS, GROUPS_ZONES, PERMISSIONS_ZONES, GROUPS_PERMISSIONS_ZONES,
		GroupManager, Permissions;
	}
	
	private boolean		linked, enabled;
	private LinkType	type;
	
	private Plugin		linkedPlugin;
	private Object		data;
	PluginCoreLink(PluginCore instance, Plugin target, LinkType handler){
		plugin = instance;
		
		linked = false;
		enabled = false;
		//found = false;
		
		linkedPlugin = target;
		type = handler;
		data = null;
	}
	
	
	public boolean isLinked(){return(linked);}
	protected void setLinked(boolean value){linked = value;}
	
	public boolean isEnabled(){
		if(plugin.pluginEnableOverride(getPluginName())) {
            return(false);
        }
		return(enabled);
	}
	
	protected void setEnabled(boolean value){enabled = value;}
	public LinkType getType(){return(type);}
	//protected void setType(LinkType value){type = value;}
	//public boolean isFound(){return(found);}
	//protected void setFound(boolean value){found = value;}
	
	
	// Plugin accessors.

	//public Plugin getPlugin(){return(plugin);}
	public String getPluginName(){
		if(linkedPlugin == null) {
            return(null);
        }
		return(linkedPlugin.getDescription().getName());
	}
	public String getPluginVersion(){
		if(linkedPlugin == null) {
            return(null);
        }
		return(linkedPlugin.getDescription().getVersion());
	}
	//protected void setPlugin(Plugin target){plugin = target;}
	protected Vault getGroupManager(){return((Vault) linkedPlugin);}
	protected Vault getPermissions(){return((Vault) linkedPlugin);}
	protected Vault getPermsBukkit(){return((Vault) linkedPlugin);}
	protected Towny getTowny(){return((Towny) linkedPlugin);}
	//protected SimpleClans getSimpleClans(){return((SimpleClans) linkedPlugin);}
	protected mcMMO getMcmmo(){return((mcMMO) linkedPlugin);}
	protected Factions getFactions(){return((Factions) linkedPlugin);}
	protected LWCPlugin getLWCPlugin(){return((LWCPlugin) linkedPlugin);}
	
	
	// Data accessors.
	
	protected void setData(Object value){data = value;}
	//protected WorldsHolder getWorldsHolder(){return((WorldsHolder) data);}
	protected Vault getPermissionHandler(){return((Vault) data);}
}

