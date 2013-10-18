
//
//This file is a component of Lockette for Bukkit, and was written by Acru Jovian.
//Distributed under the The Non-Profit Open Software License version 3.0 (NPOSL-3.0)
//http://www.opensource.org/licenses/NOSL3.0
//
package org.yi.acru.lockettev2;

//~--- non-JDK imports --------------------------------------------------------

import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.plugin.PluginManager;

public class LocketteWorldListener implements Listener {
    private static Lockette plugin;

    public LocketteWorldListener(Lockette instance) {
        plugin = instance;
    }

    protected void registerEvents() {
        PluginManager pm = plugin.getServer().getPluginManager();

        pm.registerEvents(this, plugin);
    }

    // ********************************************************************************************************************
    // Start of event section
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onStructureGrow(StructureGrowEvent event) {
        if (event.isCancelled()) {
            return;
        }

        List<BlockState> blockList = event.getBlocks();
        int              x,
                         count     = blockList.size();
        Block            block;

        /*
         * // Structure debug routine, all I touch turns to glass.
         *
         * if(count >= 0){
         * //Lockette.log.info("[Lockette] All I touch turns to glass. (" + count + ")");
         * plugin.getServer().broadcastMessage("[Lockette] All I touch turns to glass. (" + count + ")");
         *
         * for(x = 0; x < count; ++x){
         * block = blockList.get(x).getBlock();
         * block.setTypeId(20);
         * }
         *
         * event.setCancelled(true);
         * return;
         * }
         */

        // Check the block list for any protected blocks, and cancel the event if any are found.
        for (x = 0; x < count; ++x) {
            block = blockList.get(x).getBlock();

            if (Lockette.isProtected(block)) {
                event.setCancelled(true);

                return;
            }

            if (Lockette.explosionProtectionAll) {
                if ((block.getType() == Material.CHEST) 
                        || (block.getType() == Material.DISPENSER)
                        || (block.getType() == Material.FURNACE)
                        || (block.getType() == Material.BURNING_FURNACE)
                        || (block.getType() == Material.BREWING_STAND)
                        || (block.getType() == Material.HOPPER)
                        || (block.getType() == Material.DROPPER)) {
                    event.setCancelled(true);

                    return;
                }
            }
        }
    }
}