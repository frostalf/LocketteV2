//
// This file is a component of Lockette for Bukkit, and was written by Acru Jovian.
// Distributed under the The Non-Profit Open Software License version 3.0 (NPOSL-3.0)
// http://www.opensource.org/licenses/NOSL3.0
//
package org.yi.acru.lockettev2;

// Imports.
import java.text.MessageFormat;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.plugin.PluginManager;

public class LocketteBlockListener implements Listener {
    
    private static Lockette plugin;
    // Facings are reversed as we are attaching signs to blocks.
    static byte faceList[] = {5, 3, 4, 2};     // SOUTH, WEST, NORTH, EAST

    static {
        if (BlockFace.NORTH.getModX() != -1) {
            // Post CraftBukkit 2502
            faceList[0] = 3; // SOUTH
            faceList[1] = 4; // WEST
            faceList[2] = 2; // NORTH
            faceList[3] = 5; // EAST
        }
    }
    final int materialList[] = {Material.CHEST.getId(), Material.DISPENSER.getId(), Material.FURNACE.getId(), Material.BURNING_FURNACE.getId(), Material.BREWING_STAND.getId(), Material.TRAP_DOOR.getId(), Material.WOODEN_DOOR.getId(), Material.IRON_DOOR_BLOCK.getId(), Material.FENCE_GATE.getId()};
    final int materialListFurnaces[] = {Material.FURNACE.getId(), Material.BURNING_FURNACE.getId()};
    final int materialListDoors[] = {Material.WOODEN_DOOR.getId(), Material.IRON_DOOR_BLOCK.getId(), Material.FENCE_GATE.getId()};
    final int materialListBad[] = {50, 63, 64, 65, 68, 71, 75, 76, 96};//,12,13,18,46// sand, gravel, leaves, tnt

    public LocketteBlockListener(Lockette instance) {

        plugin = instance;
    }

    protected void registerEvents() {

        PluginManager pm = plugin.getServer().getPluginManager();

        pm.registerEvents(this, plugin);
    }

    //********************************************************************************************************************
    // Start of event section
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material type = block.getType();

        if (event.isCancelled()) {
            if (type != Material.WOODEN_DOOR) {
                return;
            }
        }

        // Someone is breaking a block, lets see if they are allowed.

        if (type == Material.WALL_SIGN) {
            if (type == Material.AIR) {
                // Fix for mcMMO error.
                block.setData((byte) 5);
            }

            Sign sign = (Sign) block.getState();
            String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();

            if (text.equals("[private]") || text.equalsIgnoreCase(Lockette.altPrivate)) {
                int length = player.getName().length();

                if (length > 15) {
                    length = 15;
                }

                // Check owner.
                if (sign.getLine(1).replaceAll("(?i)\u00A7[0-F]", "").equals(player.getName().substring(0, length))) {
                    //Block		checkBlock = Lockette.getSignAttachedBlock(block);
                    //if(checkBlock == null) checkBlock = block;

                    //if((checkBlock.getTypeId() != Material.WOODEN_DOOR.getId()) && (checkBlock.getTypeId() != Material.IRON_DOOR_BLOCK.getId())){
                    Lockette.log.info(MessageFormat.format("[{0}] {1} has released a container.", plugin.getDescription().getName(), player.getName()));
                    //}
                    //else Lockette.log.info("[" + plugin.getDescription().getName() + "] " + player.getName() + " has released a door.");

                    plugin.localizedMessage(player, null, "msg-owner-release");
                    return;
                }

                // At this point, check admin.

                if (Lockette.adminBreak) {
                    boolean snoop = false;

                    if (plugin.hasPermission(block.getWorld(), player, "lockette.admin.break")) {
                        snoop = true;
                    }

                    if (snoop) {
                        Lockette.log.info(MessageFormat.format("[{0}] (Admin) {1} has broken open a container owned by {2}!", plugin.getDescription().getName(), player.getName(), sign.getLine(1)));

                        plugin.localizedMessage(player, Lockette.broadcastBreakTarget, "msg-admin-release", sign.getLine(1));
                        return;
                    }
                }

                event.setCancelled(true);
                sign.update();

                plugin.localizedMessage(player, null, "msg-user-release-owned", sign.getLine(1));
            }
            else if (text.equals("[more users]") || text.equalsIgnoreCase(Lockette.altMoreUsers)) {
                Block checkBlock = LocketteBlockFace.getSignAttachedBlock(block);
                if (checkBlock == null) {
                    return;
                }

                Block signBlock = Lockette.findBlockOwner(checkBlock);
                if (signBlock == null) {
                    return;
                }

                Sign sign2 = (Sign) signBlock.getState();
                int length = player.getName().length();

                if (length > 15) {
                    length = 15;
                }

                if (sign2.getLine(1).replaceAll("(?i)\u00A7[0-F]", "").equals(player.getName().substring(0, length))) {
                    plugin.localizedMessage(player, null, "msg-owner-remove");
                    return;
                }

                event.setCancelled(true);
                sign.update();

                plugin.localizedMessage(player, null, "msg-user-remove-owned", sign2.getLine(1));
            }
        }
        else {
            Block signBlock = Lockette.findBlockOwner(block);

            if (signBlock == null) {
                return;
            }

            Sign sign = (Sign) signBlock.getState();
            int length = player.getName().length();

            if (length > 15) {
                length = 15;
            }

            // Check owner.
            if (sign.getLine(1).replaceAll("(?i)\u00A7[0-F]", "").equals(player.getName().substring(0, length))) {
                if (Lockette.findBlockOwnerBreak(block) != null) {
                    // This block has the sign attached.  (Or the the door above the block.)

                    Lockette.log.info("[" + plugin.getDescription().getName() + "] " + player.getName() + " has released a container.");
                }
                else {
                    // Partial release for chest/doors, the sign may now be invalid for doors, but is always valid for chests.

                    if ((type == Material.WOODEN_DOOR) || (type == Material.IRON_DOOR_BLOCK)) {
                        // Check for invalid signs somehow?
                        // But valid signs can be collided anyways... so probably doesn't matter.  (Unless this is prevented too.)
                    }
                }
                return;
            }

            event.setCancelled(true);
            //if(!Lockette.enhancedEvents){
            //	// Fix for broken doors in build xxx-560.
            //	if(type == Material.WOODEN_DOOR.getId()) Lockette.toggleSingleDoor(block);
            //}

            plugin.localizedMessage(player, null, "msg-user-break-owned", sign.getLine(1));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {

        Block block = event.getBlock();
        // Check the block list for any protected blocks, and cancel the event if any are found.

        Block checkBlock;
        List<Block> blockList = event.getBlocks();
        int x, count = blockList.size();

        for (x = 0; x < count; ++x) {
            checkBlock = blockList.get(x);

            if (Lockette.isProtected(checkBlock)) {
                event.setCancelled(true);
                return;
            }
        }

        // The above misses doors at the end of the chain, in the space the blocks are being pushed into.

        checkBlock = block.getRelative(LocketteBlockFace.getPistonFacing(block), event.getLength() + 1);
        Block checkBlock1 = block.getRelative(LocketteBlockFace.getPistonFacing(block), event.getLength() + 2).getRelative(BlockFace.UP);
        Material type = block.getRelative(LocketteBlockFace.getPistonFacing(block)).getType();
        //added this check with pistons so you could not push any rail near the chest unless you own it
        if ((type == Material.ACTIVATOR_RAIL) 
                || (type == Material.DETECTOR_RAIL) 
                || (type == Material.POWERED_RAIL) 
                || (type == Material.RAILS) 
                || (type == Material.HOPPER_MINECART)){
        if (Lockette.isProtected(checkBlock1)) {
            event.setCancelled(true);
            return;
        }            
        }
                if (Lockette.isProtected(checkBlock)) {
            event.setCancelled(true);
        }

    }
//added a check with all rails involved in trying to move a rail close enough to chest. unless you own it.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {

        if (!(event.isSticky())) {
            return;
        }

        Block block = event.getBlock();
        Block checkBlock = block.getRelative(LocketteBlockFace.getPistonFacing(block), 2);
        Block checkBlock1 = block.getRelative(BlockFace.UP);
        Material type = checkBlock.getType();

        // Skip those mats that cannot be pulled.

        if (type == Material.CHEST) {
            return;
        }
        if (type == Material.DISPENSER) {
            return;
        }
        if (type == Material.FURNACE) {
            return;
        }
        if (type == Material.BURNING_FURNACE) {
            return;
        }
        if (type == Material.WOODEN_DOOR) {
            return;
        }
        if (type == Material.IRON_DOOR_BLOCK) {
            return;
        }
        if (type == Material.TRAPPED_CHEST) {
            return;
        }
        if (type == Material.BREWING_STAND){
            return;
        }
        //if(type == Material.TRAP_DOOR.getId()) don't return

        if((type == Material.ACTIVATOR_RAIL) 
                || (type == Material.DETECTOR_RAIL) 
                || (type == Material.POWERED_RAIL) 
                || (type == Material.RAILS) 
                || (type == Material.HOPPER_MINECART)){
            
        if (Lockette.isProtected(checkBlock1)) {
            event.setCancelled(true);
            return;
        }
        }
                if (Lockette.isProtected(checkBlock)) {
            event.setCancelled(true);
        }
    }
    // This new event is for when players try to push the minecart under the chest. When the minecarthopper dissappears it does not remove items from chest.
    @EventHandler(priority =  EventPriority.HIGHEST)
    public void onCartHopperMove(VehicleMoveEvent event){
        //Don't
        if((event.getVehicle().getType() != EntityType.MINECART_HOPPER) || (event.getVehicle().isOnGround() != true)){
            return;
        }
        Location chest = event.getTo().add(0, 1, 0);
        Block checkBlock = chest.getBlock();
        Material type = checkBlock.getType();
        if((type == Material.CHEST) 
                || (type == Material.DISPENSER) 
                || (type == Material.FURNACE) 
                || (type == Material.WOODEN_DOOR) 
                || (type == Material.BURNING_FURNACE) 
                || (type == Material.IRON_DOOR_BLOCK)
                || (type == Material.TRAPPED_CHEST)){
            if (Lockette.isProtected(checkBlock)) {
            event.getVehicle().remove();
        }            
        }
        
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {

        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        Material type = block.getType();
        Block against = event.getBlockAgainst();
        Block checkBlock;
        Block signBlock;

        // Check if someone accidentally put any block on an owned sign.

        if (against.getType() == Material.WALL_SIGN) {
            // Only cancel it for our signs.
            Sign sign = (Sign) against.getState();
            String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();

            if (text.equals("[private]") 
                    || text.equalsIgnoreCase(Lockette.altPrivate) 
                    || text.equals("[more users]") 
                    || text.equalsIgnoreCase(Lockette.altMoreUsers)) {
                event.setCancelled(true);
                return;
            }
        }

        // Check the placing of a door by a door here.
        // Though it is usually an item, not a block?  Is this still needed?

        if ((type == Material.WOODEN_DOOR) 
                || (type == Material.IRON_DOOR_BLOCK) 
                || (type == Material.TRAP_DOOR) 
                || (type == Material.FENCE_GATE)) {
            //player.sendMessage(ChatColor.DARK_PURPLE + "Lockette: Door block block has been placed");

            if (canBuildDoor(block, against, player)) {
                return;
            }

            event.setCancelled(true);

            plugin.localizedMessage(player, null, "msg-user-conflict-door");
            return;
        }

        if (Lockette.directPlacement) {
            if (type == Material.WALL_SIGN) {
                checkBlock = LocketteBlockFace.getSignAttachedBlock(block);

                if (checkBlock == null) {
                    return;
                }

                type = checkBlock.getType();

                if ((type == Material.CHEST) 
                        || (type == Material.DISPENSER) 
                        || (type == Material.FURNACE) 
                        || (type == Material.BURNING_FURNACE) 
                        || (type == Material.BREWING_STAND)
                        || (type == Material.TRAPPED_CHEST)
                        || Lockette.isInList(type, Lockette.customBlockList)) {

                    Sign sign = (Sign) block.getState();
                    int length = player.getName().length();

                    if (length > 15) {
                        length = 15;
                    }

                    if (Lockette.isProtected(checkBlock)) {
                        // Add a users sign only if owner.
                        if (Lockette.isOwner(checkBlock, player.getName())) {
                            sign.setLine(0, Lockette.altMoreUsers);
                            sign.setLine(1, Lockette.altEveryone);
                            sign.setLine(2, "");
                            sign.setLine(3, "");
                            sign.update(true);

                            plugin.localizedMessage(player, null, "msg-owner-adduser");
                        }
                        else {
                            event.setCancelled(true);
                        }
                    }
                    else {
                        // Check for permission first.

                        if (!checkPermissions(player, block, checkBlock)) {
                            event.setCancelled(true);

                            plugin.localizedMessage(player, null, "msg-error-permission");
                            return;
                        }

                        sign.setLine(0, Lockette.altPrivate);
                        sign.setLine(1, player.getName().substring(0, length));
                        sign.setLine(2, "");
                        sign.setLine(3, "");
                        sign.update(true);

                        Lockette.log.info("[" + plugin.getDescription().getName() + "] " + player.getName() + " has claimed a container.");

                        plugin.localizedMessage(player, null, "msg-owner-claim");
                    }
                }

                return;
            }
        }

        // The rest is for chests and hoppers only.		

        if (type == Material.CHEST) {

            // Count nearby chests to find illegal sized chests.

            int chests = Lockette.findChestCountNear(block);

            if (chests > 1) {
                event.setCancelled(true);

                plugin.localizedMessage(player, null, "msg-user-illegal");
                return;
            }

            signBlock = Lockette.findBlockOwner(block);

            if (signBlock != null) {
                // Expanding a private chest, see if its allowed.

                Sign sign = (Sign) signBlock.getState();
                int length = player.getName().length();

                if (length > 15) {
                    length = 15;
                }

                // Check owner.
                if (sign.getLine(1).replaceAll("(?i)\u00A7[0-F]", "").equals(player.getName().substring(0, length))) {
                    return;
                }

                // If we got here, then not allowed.

                event.setCancelled(true);

                plugin.localizedMessage(player, null, "msg-user-resize-owned", sign.getLine(1));
            }
            else {
                // Only send one helpful message per user per session.

                if (plugin.playerList.get(player.getName()) == null) {
                    // Associate the user with a non-null block, and print a helpful message.
                    plugin.playerList.put(player.getName(), block);
                    plugin.localizedMessage(player, null, "msg-help-chest");
                }
            }
        }

        // Hoppers from here.

        if ((type == Material.HOPPER)) {

            checkBlock = block.getRelative(BlockFace.UP);
            type = checkBlock.getType();

            if ((type == Material.CHEST) 
                    || (type == Material.DISPENSER) 
                    || (type == Material.FURNACE) 
                    || (type == Material.BURNING_FURNACE) 
                    || (type == Material.BREWING_STAND) 
                    || Lockette.isInList(type, Lockette.customBlockList)) {

                if (!validateOwner(checkBlock, player)) {

                    event.setCancelled(true);

                    plugin.localizedMessage(player, null, "msg-user-denied");
                    return;
                }
            }

            checkBlock = block.getRelative(BlockFace.DOWN);
            type = checkBlock.getType();

            if ((type == Material.CHEST) 
                    || (type == Material.DISPENSER) 
                    || (type == Material.FURNACE) 
                    || (type == Material.BURNING_FURNACE) 
                    || (type == Material.BREWING_STAND) 
                    || Lockette.isInList(type, Lockette.customBlockList)) {

                if (!validateOwner(checkBlock, player)) {

                    event.setCancelled(true);

                    plugin.localizedMessage(player, null, "msg-user-denied");
                }
            }
        }
        // Way to stop minecart hoppers
        if ((type == Material.POWERED_RAIL) 
                || (type == Material.RAILS) 
                || (type == Material.ACTIVATOR_RAIL) 
                || (type == Material.DETECTOR_RAIL)){
            checkBlock = block.getRelative(BlockFace.UP);
            Block checkBlock1 = checkBlock.getRelative(BlockFace.EAST);
            Block checkBlock2 = checkBlock.getRelative(BlockFace.WEST);
            Block checkBlock3 = checkBlock.getRelative(BlockFace.NORTH);
            Block checkBlock4 = checkBlock.getRelative(BlockFace.SOUTH);
            Material type1 = checkBlock1.getType();
            Material type2 = checkBlock2.getType();
            Material type3 = checkBlock3.getType();
            Material type4 = checkBlock4.getType();
            type = checkBlock.getType();

            if ((type == Material.CHEST) 
                    || (type == Material.DISPENSER) 
                    || (type == Material.FURNACE) 
                    || (type == Material.BURNING_FURNACE) 
                    || (type == Material.BREWING_STAND) 
                    || (type == Material.TRAPPED_CHEST)
                    || Lockette.isInList(type, Lockette.customBlockList)) {

                if (!validateOwner(checkBlock, player)) {

                    event.setCancelled(true);

                    plugin.localizedMessage(player, null, "msg-user-denied");
                    return;
                }
            }
                        if ((type1 == Material.CHEST) 
                                || (type1 == Material.DISPENSER) 
                                || (type1 == Material.FURNACE) 
                                || (type1 == Material.BURNING_FURNACE) 
                                || (type1 == Material.TRAPPED_CHEST)
                                || (type1 == Material.BREWING_STAND) 
                                || Lockette.isInList(type1, Lockette.customBlockList)) {

                if (!validateOwner(checkBlock1, player)) {

                    event.setCancelled(true);

                    plugin.localizedMessage(player, null, "msg-user-denied");
                    return;
                }
            }
                        if ((type2 == Material.CHEST) 
                                || (type2 == Material.DISPENSER) 
                                || (type2 == Material.FURNACE) 
                                || (type2 == Material.BURNING_FURNACE) 
                                || (type2 == Material.BREWING_STAND) 
                                || (type2 == Material.TRAPPED_CHEST)
                                || Lockette.isInList(type2, Lockette.customBlockList)) {

                if (!validateOwner(checkBlock2, player)) {

                    event.setCancelled(true);

                    plugin.localizedMessage(player, null, "msg-user-denied");
                    return;
                }
            }
                      if ((type3 == Material.CHEST) 
                              || (type3 == Material.DISPENSER) 
                              || (type3 == Material.FURNACE) 
                              || (type3 == Material.BURNING_FURNACE) 
                              || (type3 == Material.BREWING_STAND) 
                              || (type3 == Material.TRAPPED_CHEST)
                              || Lockette.isInList(type3, Lockette.customBlockList)) {

                if (!validateOwner(checkBlock3, player)) {

                    event.setCancelled(true);

                    plugin.localizedMessage(player, null, "msg-user-denied");
                    return;
                }
            }
                      if ((type4 == Material.CHEST) 
                              || (type4 == Material.DISPENSER) 
                              || (type4 == Material.FURNACE) 
                              || (type4 == Material.BURNING_FURNACE) 
                              || (type4 == Material.BREWING_STAND) 
                              || (type4 == Material.TRAPPED_CHEST)
                              || Lockette.isInList(type4, Lockette.customBlockList)) {

                if (!validateOwner(checkBlock4, player)) {

                    event.setCancelled(true);

                    plugin.localizedMessage(player, null, "msg-user-denied");
                }
            }
        }
    }
/*
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event){
            
        if (event.isCancelled()) {
            return;
        }
        int carthopper = event.getMaterial().getId();
        Player player = event.getPlayer();
        int blockx = (int) event.getClickedBlock().getLocation().getX();
        int blocky = (int) event.getClickedBlock().getLocation().getY();
        int blockz = (int) event.getClickedBlock().getLocation().getZ();
        Block checkblock = event.getClickedBlock().getLocation().getBlock().getRelative(blockx, blocky + 2, blockz);
        int type = checkblock.getTypeId();
        if(carthopper == Material.HOPPER_MINECART.getId()){

            if(((type == Material.CHEST.getId()) || (type == Material.DISPENSER.getId()) || (type == Material.FURNACE.getId()) || (type == Material.BURNING_FURNACE.getId()) || (type == Material.BREWING_STAND.getId()))){
      
                if (!validateOwner(checkblock, player)) {

                    event.setCancelled(true);

                    plugin.localizedMessage(player, null, "msg-user-denied");
                }
            }
        }
    }
  */
    /**
     * Check permissions and external sources to see if we are allowed to place
     * a private sign here
     *
     * @return true if permitted
     */
    private boolean checkPermissions(Player player, Block block, Block checkBlock) {

        int type = checkBlock.getTypeId();

        if (plugin.usingExternalZones()) {
            if (!plugin.canBuild(player, block)) {

                plugin.localizedMessage(player, null, "msg-error-zone", PluginCore.lastZoneDeny());
                return false;
            }

            if (!plugin.canBuild(player, checkBlock)) {

                plugin.localizedMessage(player, null, "msg-error-zone", PluginCore.lastZoneDeny());
                return false;
            }
        }

        if (plugin.usingExternalPermissions()) {
            boolean create = false;

            if (plugin.hasPermission(block.getWorld(), player, "lockette.create.all")) {
                create = true;
            }
            else if (type == Material.CHEST.getId()) {
                if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.chest")) {
                    create = true;
                }
            }
            else if ((type == Material.FURNACE.getId()) || (type == Material.BURNING_FURNACE.getId())) {
                if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.furnace")) {
                    create = true;
                }
            }
            else if (type == Material.DISPENSER.getId()) {
                if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.dispenser")) {
                    create = true;
                }
            }
            else if (type == Material.BREWING_STAND.getId()) {
                if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.brewingstand")) {
                    create = true;
                }
            }
            else if (Lockette.isInList(type, Lockette.customBlockList)) {
                if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.custom")) {
                    create = true;
                }
            }

            return create;
        }

        return true;
    }

    /**
     * Check for a private sign and check we are the owner of this block.
     *
     * @param block
     * @param player
     * @return true if no owner or we are the owner named on the private sign.
     */
    private boolean validateOwner(Block block, Player player) {

        Block signBlock = Lockette.findBlockOwner(block);

        // No sign block so has no owner.
        if (signBlock == null) {
            return true;
        }

        Sign sign = (Sign) signBlock.getState();
        int length = player.getName().length();

        if (length > 15) {
            length = 15;
        }

        // Check owner.
        if (sign.getLine(1).replaceAll("(?i)\u00A7[0-F]", "").equals(player.getName().substring(0, length))) {
            return true;
        }

        // Owner doesn't match so deny.
        return false;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {

        Block block = event.getBlock();
        Material type = block.getType();
        boolean doCheck = false;

        if (Lockette.protectTrapDoors) {
            if (type == Material.TRAP_DOOR) {
                doCheck = true;
            }
        }

        if (Lockette.protectDoors) {
            if ((type == Material.WOODEN_DOOR) 
                    || (type == Material.IRON_DOOR_BLOCK) 
                    || (type == Material.FENCE_GATE)) {
                doCheck = true;
            }
        }

        if (doCheck) {
            // Lets see if everyone is allowed to activate.
            Block signBlock = Lockette.findBlockOwner(block);

            if (signBlock == null) {
                return;
            }

            // Check main three users.

            Sign sign = (Sign) signBlock.getState();
            String line;
            int y;

            for (y = 1; y <= 3; ++y) {
                if (!sign.getLine(y).isEmpty()) {
                    line = sign.getLine(y).replaceAll("(?i)\u00A7[0-F]", "");

                    if (line.equalsIgnoreCase("[Everyone]") || line.equalsIgnoreCase(Lockette.altEveryone)) {
                        return;
                    }
                }
            }

            // Check for more users.

            List<Block> list = Lockette.findBlockUsers(block, signBlock);
            int x, count = list.size();

            for (x = 0; x < count; ++x) {
                sign = (Sign) list.get(x).getState();

                for (y = 1; y <= 3; ++y) {
                    if (!sign.getLine(y).isEmpty()) {
                        line = sign.getLine(y).replaceAll("(?i)\u00A7[0-F]", "");

                        if (line.equalsIgnoreCase("[Everyone]") || line.equalsIgnoreCase(Lockette.altEveryone)) {
                            return;
                        }
                    }
                }
            }

            // Don't have permission.

            event.setNewCurrent(event.getOldCurrent());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        //if(event.isCancelled()) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        boolean typeWallSign = (block.getType() == Material.WALL_SIGN);
        boolean typeSignPost = (block.getType() == Material.SIGN_POST);

        // But also need this along with stuff in PrefixListener

        if (typeWallSign) {
            Sign sign = (Sign) block.getState();
            String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "");

            if (text.equalsIgnoreCase("[Private]") 
                    || text.equalsIgnoreCase(Lockette.altPrivate) 
                    || text.equalsIgnoreCase("[More Users]") 
                    || text.equalsIgnoreCase(Lockette.altMoreUsers)) {
                if (event.isCancelled()) {
                    return;
                }
                //event.setCancelled(true);
                //return;
            }
        }
        else if (typeSignPost) {
        }
        else {
            // Not a sign, wtf!
            event.setCancelled(true);
            return;
        }

        // Check for a new [Private] or [More Users] sign.

        String text = event.getLine(0).replaceAll("(?i)\u00A7[0-F]", "");

        if (text.equalsIgnoreCase("[Private]") || text.equalsIgnoreCase(Lockette.altPrivate)) {
            //Player		player = event.getPlayer();
            //Block		block = event.getBlock();
            //boolean		typeWallSign = (block.getTypeId() == Material.WALL_SIGN.getId());
            boolean doChests = true, doFurnaces = true, doDispensers = true;
            boolean doBrewingStands = true, doCustoms = true;
            boolean doTrapDoors = true, doDoors = true;

            // Check for permission first.

            if (plugin.usingExternalZones()) {
                if (!plugin.canBuild(player, block)) {
                    event.setLine(0, "[?]");

                    plugin.localizedMessage(player, null, "msg-error-zone", PluginCore.lastZoneDeny());
                    return;
                }
            }

            if (plugin.usingExternalPermissions()) {
                boolean create = false;

                doChests = false;
                doFurnaces = false;
                doDispensers = false;
                doBrewingStands = false;
                doCustoms = false;
                doTrapDoors = false;
                doDoors = false;

                if (plugin.hasPermission(block.getWorld(), player, "lockette.create.all")) {
                    create = true;
                    doChests = true;
                    doFurnaces = true;
                    doDispensers = true;
                    doBrewingStands = true;
                    doCustoms = true;
                    doTrapDoors = true;
                    doDoors = true;
                }
                else {
                    if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.chest")) {
                        create = true;
                        doChests = true;
                    }
                    if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.furnace")) {
                        create = true;
                        doFurnaces = true;
                    }
                    if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.dispenser")) {
                        create = true;
                        doDispensers = true;
                    }
                    if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.brewingstand")) {
                        create = true;
                        doBrewingStands = true;
                    }
                    if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.custom")) {
                        create = true;
                        doCustoms = true;
                    }
                    if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.trapdoor")) {
                        create = true;
                        doTrapDoors = true;
                    }
                    if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.door")) {
                        create = true;
                        doDoors = true;
                    }
                }

                if (!create) {
                    event.setLine(0, "[?]");

                    plugin.localizedMessage(player, null, "msg-error-permission");
                    return;
                }
            }

            int x;
            Block checkBlock[] = new Block[4];
            byte face = 0;
            int type = 0;
            boolean conflict = false;
            boolean deny = false;
            boolean zonedeny = false;

            // Check wall sign attached block for trap doors.

            if (Lockette.protectTrapDoors) {
                if (typeWallSign) {
                    checkBlock[3] = LocketteBlockFace.getSignAttachedBlock(block);
                    if (checkBlock[3] != null) {
                        if (!isInList(checkBlock[3].getTypeId(), materialListBad)) {
                            checkBlock[0] = checkBlock[3].getRelative(BlockFace.NORTH);
                            checkBlock[1] = checkBlock[3].getRelative(BlockFace.EAST);
                            checkBlock[2] = checkBlock[3].getRelative(BlockFace.SOUTH);
                            checkBlock[3] = checkBlock[3].getRelative(BlockFace.WEST);
                            for (x = 0; x < 4; ++x) {
                                if (checkBlock[x].getType() == Material.TRAP_DOOR) {
                                    if (Lockette.findBlockOwner(checkBlock[x], block, true) == null) {
                                        if (!doTrapDoors) {
                                            deny = true;
                                        }
                                        else {
                                            face = block.getData();
                                            type = 4;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Check wall sign attached block for doors, above and below.

            if (Lockette.protectDoors) {
                if (typeWallSign) {
                    checkBlock[0] = LocketteBlockFace.getSignAttachedBlock(block);
                    if (checkBlock[0] != null) {
                        if (!isInList(checkBlock[0].getTypeId(), materialListBad)) {
                            checkBlock[1] = checkBlock[0].getRelative(BlockFace.UP);
                            checkBlock[2] = checkBlock[0].getRelative(BlockFace.DOWN);
                            if (isInList(checkBlock[1].getTypeId(), materialListDoors)) {
                                if (Lockette.findBlockOwner(checkBlock[1], block, true) == null) {
                                    if (isInList(checkBlock[2].getTypeId(), materialListDoors)) {
                                        if (Lockette.findBlockOwner(checkBlock[2], block, true) == null) {
                                            if (!doDoors) {
                                                deny = true;
                                            }
                                            else {
                                                face = block.getData();
                                                type = 5;
                                            }
                                        }
                                        else {
                                            conflict = true;
                                        }
                                    }
                                    else {
                                        if (!doDoors) {
                                            deny = true;
                                        }
                                        else {
                                            face = block.getData();
                                            type = 5;
                                        }
                                    }
                                }
                                else {
                                    conflict = true;
                                }
                            }
                            else if (isInList(checkBlock[2].getTypeId(), materialListDoors)) {
                                if (Lockette.findBlockOwner(checkBlock[2], block, true) == null) {
                                    if (!doDoors) {
                                        deny = true;
                                    }
                                    else {
                                        face = block.getData();
                                        type = 5;
                                    }
                                }
                                else {
                                    conflict = true;
                                }
                            }
                        }
                    }
                }
            }

            // Reset trapdoor face if there is a conflict with a door.
            if (conflict == true) {
                face = 0;
                type = 0;
            }

            if (face == 0) {
                int lastType;

                // Check for chests first, dispensers second, furnaces third.

                checkBlock[0] = block.getRelative(BlockFace.NORTH);
                checkBlock[1] = block.getRelative(BlockFace.EAST);
                checkBlock[2] = block.getRelative(BlockFace.SOUTH);
                checkBlock[3] = block.getRelative(BlockFace.WEST);

                for (x = 0; x < 4; ++x) {
                    if (plugin.usingExternalZones()) {
                        if (!plugin.canBuild(player, checkBlock[x])) {
                            zonedeny = true;
                            continue;
                        }
                    }

                    // Check if allowed by type.
                    if (checkBlock[x].getType() == Material.CHEST) {
                        if (!doChests) {
                            deny = true;
                            continue;
                        }
                        lastType = 1;
                    }
                    else if (isInList(checkBlock[x].getTypeId(), materialListFurnaces)) {
                        if (!doFurnaces) {
                            deny = true;
                            continue;
                        }
                        lastType = 2;
                    }
                    else if (checkBlock[x].getType() == Material.DISPENSER) {
                        if (!doDispensers) {
                            deny = true;
                            continue;
                        }
                        lastType = 3;
                    }
                    else if (checkBlock[x].getType() == Material.BREWING_STAND) {
                        if (!doBrewingStands) {
                            deny = true;
                            continue;
                        }
                        lastType = 6;
                    }
                    else if (Lockette.isInList(checkBlock[x].getTypeId(), Lockette.customBlockList)) {
                        if (!doCustoms) {
                            deny = true;
                            continue;
                        }
                        lastType = 7;
                    }
                    else if (checkBlock[x].getType() == Material.TRAP_DOOR) {
                        if (!Lockette.protectTrapDoors) {
                            continue;
                        }
                        if (!doTrapDoors) {
                            deny = true;
                            continue;
                        }
                        lastType = 4;
                    }
                    else if (isInList(checkBlock[x].getTypeId(), materialListDoors)) {
                        if (!Lockette.protectDoors) {
                            continue;
                        }
                        if (!doDoors) {
                            deny = true;
                            continue;
                        }
                        lastType = 5;
                    }
                    else {
                        continue;
                    }

                    // Allowed, lets see if it is claimed.
                    if (Lockette.findBlockOwner(checkBlock[x], block, true) == null) {
                        face = faceList[x];
                        type = lastType;
                        break;
                    }
                    // For when the last type is a door, and it is conflicting.
                    else {
                        if (Lockette.protectTrapDoors) {
                            if (doTrapDoors) {
                                if (checkBlock[x].getType() == Material.TRAP_DOOR) {
                                    conflict = true;
                                }
                            }
                        }
                        if (Lockette.protectDoors) {
                            if (doDoors) {
                                if (isInList(checkBlock[x].getTypeId(), materialListDoors)) {
                                    conflict = true;
                                }
                            }
                        }
                    }
                }
            }

            // None found, send a message.

            if (face == 0) {
                event.setLine(0, "[?]");

                if (conflict) {
                    plugin.localizedMessage(player, null, "msg-error-claim-conflict");
                }
                else if (zonedeny) {
                    plugin.localizedMessage(player, null, "msg-error-zone", PluginCore.lastZoneDeny());
                }
                else if (deny) {
                    plugin.localizedMessage(player, null, "msg-error-permission");
                }
                else {
                    plugin.localizedMessage(player, null, "msg-error-claim");
                }
                return;
            }

            // Claim it...

            boolean anyone = true;
            int length = player.getName().length();

            if (event.getLine(1).isEmpty()) {
                anyone = false;
            }
            if (length > 15) {
                length = 15;
            }

            // In case some other plugin messed with the cancel state.
            event.setCancelled(false);

            if (anyone) {
                // Check if allowed by type.
                if (type == 1) {	// Chest
                    if (!plugin.hasPermission(block.getWorld(), player, "lockette.admin.create.chest")) {
                        anyone = false;
                    }
                }
                else if (type == 2) {	// Furnace
                    if (!plugin.hasPermission(block.getWorld(), player, "lockette.admin.create.furnace")) {
                        anyone = false;
                    }
                }
                else if (type == 3) {	// Dispenser
                    if (!plugin.hasPermission(block.getWorld(), player, "lockette.admin.create.dispenser")) {
                        anyone = false;
                    }
                }
                else if (type == 6) {	// Brewing Stand
                    if (!plugin.hasPermission(block.getWorld(), player, "lockette.admin.create.brewingstand")) {
                        anyone = false;
                    }
                }
                else if (type == 7) {	// Custom
                    if (!plugin.hasPermission(block.getWorld(), player, "lockette.admin.create.custom")) {
                        anyone = false;
                    }
                }
                else if (type == 4) {	// Trap Door
                    if (!plugin.hasPermission(block.getWorld(), player, "lockette.admin.create.trapdoor")) {
                        anyone = false;
                    }
                }
                else if (type == 5) {	// Door
                    if (!plugin.hasPermission(block.getWorld(), player, "lockette.admin.create.door")) {
                        anyone = false;
                    }
                }
                else {
                    anyone = false;
                }
            }

            if (!anyone) {
                event.setLine(1, player.getName().substring(0, length));
            }

            if (!typeWallSign) {
                // Set to wall type.
                block.setType(Material.WALL_SIGN);
                block.setData(face);

                // Re-set the text.
                Sign sign = (Sign) block.getState();

                sign.setLine(0, event.getLine(0));
                sign.setLine(1, event.getLine(1));
                sign.setLine(2, event.getLine(2));
                sign.setLine(3, event.getLine(3));
                sign.update(true);
            }
            else {
                block.setData(face);
            }

            // All done!

            if (anyone) {
                Lockette.log.info("[" + plugin.getDescription().getName() + "] (Admin) " + player.getName() + " has claimed a container for " + event.getLine(1) + ".");

                if (!plugin.playerOnline(event.getLine(1))) {
                    plugin.localizedMessage(player, null, "msg-admin-claim-error", event.getLine(1));
                }
                else {
                    plugin.localizedMessage(player, null, "msg-admin-claim", event.getLine(1));
                }
            }
            else {
                Lockette.log.info("[" + plugin.getDescription().getName() + "] " + player.getName() + " has claimed a container.");

                plugin.localizedMessage(player, null, "msg-owner-claim");
            }
        }
        else if (text.equalsIgnoreCase("[More Users]") || text.equalsIgnoreCase(Lockette.altMoreUsers)) {
            //Player		player = event.getPlayer();
            //Block		block = event.getBlock();
            //boolean		typeWallSign = (block.getTypeId() == Material.WALL_SIGN.getId());

            int x;
            Block checkBlock[] = new Block[4];
            Block signBlock = null;
            Sign sign = null;
            byte face = 0;
            //int			type = 0;

            int length = player.getName().length();

            if (length > 15) {
                length = 15;
            }

            // Check wall sign attached block for owner.

            if (Lockette.protectDoors || Lockette.protectTrapDoors) {
                if (typeWallSign) {
                    checkBlock[0] = LocketteBlockFace.getSignAttachedBlock(block);
                    if (checkBlock[0] != null) {
                        if (!isInList(checkBlock[0].getTypeId(), materialListBad)) {
                            signBlock = Lockette.findBlockOwner(checkBlock[0]);

                            if (signBlock != null) {
                                sign = (Sign) signBlock.getState();

                                // Check owner.
                                if (sign.getLine(1).replaceAll("(?i)\u00A7[0-F]", "").equals(player.getName().substring(0, length))) {
                                    face = block.getData();
                                }
                            }
                        }
                    }
                }
            }

            if (face == 0) {
                // Check for chests first, dispensers second, furnaces third.

                checkBlock[0] = block.getRelative(BlockFace.NORTH);
                checkBlock[1] = block.getRelative(BlockFace.EAST);
                checkBlock[2] = block.getRelative(BlockFace.SOUTH);
                checkBlock[3] = block.getRelative(BlockFace.WEST);

                for (x = 0; x < 4; ++x) {
                    if (!isInList(checkBlock[x].getTypeId(), materialList)) {
                        continue;
                    }

                    if (!Lockette.protectTrapDoors) {
                        if (checkBlock[x].getType() == Material.TRAP_DOOR) {
                            continue;
                        }
                    }

                    if (!Lockette.protectDoors) {
                        if (isInList(checkBlock[x].getTypeId(), materialListDoors)) {
                            continue;
                        }
                    }

                    signBlock = Lockette.findBlockOwner(checkBlock[x]);

                    if (signBlock != null) {
                        sign = (Sign) signBlock.getState();

                        // Check owner.
                        if (sign.getLine(1).replaceAll("(?i)\u00A7[0-F]", "").equals(player.getName().substring(0, length))) {
                            face = faceList[x];
                            //type = y;
                            break;
                        }
                    }
                }
            }

            // None found, send a message.

            if (face == 0) {
                event.setLine(0, "[?]");
                if (sign != null) {
                    plugin.localizedMessage(player, null, "msg-error-adduser-owned", sign.getLine(1));
                }
                else {
                    plugin.localizedMessage(player, null, "msg-error-adduser");
                }
                return;
            }

            // Add the users sign.

            // In case some other plugin messed with the cancel state.
            event.setCancelled(false);
            if (!typeWallSign) {
                // Set to wall type.
                block.setType(Material.WALL_SIGN);
                block.setData(face);

                // Re-set the text.
                //Sign		
                sign = (Sign) block.getState();

                sign.setLine(0, event.getLine(0));
                sign.setLine(1, event.getLine(1));
                sign.setLine(2, event.getLine(2));
                sign.setLine(3, event.getLine(3));
                sign.update(true);

            }
            else {
                block.setData(face);
            }

            // All done!

            plugin.localizedMessage(player, null, "msg-owner-adduser");
        }
    }

    //********************************************************************************************************************
    // Start of utility section
    // Returns true if it should be allowed, false if it should be canceled.
    private static boolean canBuildDoor(Block block, Block against, Player player) {

        Block checkBlock;
        //Sign		sign;
        //int			length = player.getName().length();

        //if(length > 15) length = 15;

        // Check block below for doors or block to side for trapdoors.

        if (!Lockette.isOwner(against, player.getName())) {
            return (false);
        }

        if (Lockette.protectTrapDoors) {
            if (block.getType() == Material.TRAP_DOOR) {
                //if(!Lockette.isOwner(Lockette.getTrapDoorAttachedBlock(block), player.getName())) return(false);
                //if(!Lockette.isOwner(block, player.getName())) return(false); // Failed as block data is bad, same as above.
                //if(!Lockette.isOwner(against, player.getName())) return(false);
                return (true);
            }
        }

        // Check block above door.

        if (!Lockette.isOwner(against.getRelative(BlockFace.UP, 3), player.getName())) {
            return (false);
        }

        // Check neighboring doors.

        checkBlock = block.getRelative(BlockFace.NORTH);
        if (checkBlock.getType() == block.getType()) {
            if (!Lockette.isOwner(checkBlock, player.getName())) {
                return (false);
            }
        }

        checkBlock = block.getRelative(BlockFace.EAST);
        if (checkBlock.getType() == block.getType()) {
            if (!Lockette.isOwner(checkBlock, player.getName())) {
                return (false);
            }
        }

        checkBlock = block.getRelative(BlockFace.SOUTH);
        if (checkBlock.getType() == block.getType()) {
            if (!Lockette.isOwner(checkBlock, player.getName())) {
                return (false);
            }
        }

        checkBlock = block.getRelative(BlockFace.WEST);
        if (checkBlock.getType() == block.getType()) {
            if (!Lockette.isOwner(checkBlock, player.getName())) {
                return (false);
            }
        }

        return (true);
    }

    private boolean isInList(int target, int[] list) {

        if (list == null) {
            return (false);
        }
        for (int x = 0; x < list.length; ++x) {
            if (target == list[x]) {
                return (true);
            }
        }
        return (false);
    }
}
