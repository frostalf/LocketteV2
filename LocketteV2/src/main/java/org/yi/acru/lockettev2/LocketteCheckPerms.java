
package org.yi.acru.lockettev2;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
/**
 *
 * @author Frostalf
 */
public class LocketteCheckPerms extends JavaPlugin {
    public static Permission permission = null;
    public boolean setupPermissions(){
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if(permissionProvider != null){
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    public boolean hasPermission(Permission permission, Player player, String permissionNode){
        return(hasPermission(permission, getServer().getPlayer(playerName), permissionNode ));
    }
    
    
}
