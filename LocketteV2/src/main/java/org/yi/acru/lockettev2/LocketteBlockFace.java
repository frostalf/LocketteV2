
package org.yi.acru.lockettev2;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
//import org.yi.acru.lockettev2.*;
/**
 *
 * @author Frostalf
 */
public class LocketteBlockFace {

    public static Lockette plugin;
    public LocketteBlockFace(Lockette instance){
        plugin = instance;
    }
    //public LocketteBlockFace(LocketteBlockFace instance){
    //    plugin = instance;
    //}
    
    	public static Block getSignAttachedBlock(Block block){
		if(block.getType() != Material.WALL_SIGN) {
            return(null);
        }
		
		
		int	face = block.getData() & 0x7;
		
		if(face == 5) {
            return(block.getRelative(BlockFace.NORTH));
        }
		if(face == 3) {
            return(block.getRelative(BlockFace.EAST));
        }
		if(face == 4) {
            return(block.getRelative(BlockFace.SOUTH));
        }
		if(face == 2) {
            return(block.getRelative(BlockFace.WEST));
        }
		
		return(null);
	}
	
	
	public static Block getTrapDoorAttachedBlock(Block block){
		if(block.getType() != Material.TRAP_DOOR) {
            return(null);
        }
		
		
		int	face = block.getData() & 0x3;
		
		if(face == 3) {
            return(block.getRelative(BlockFace.NORTH));
        }
		if(face == 1) {
            return(block.getRelative(BlockFace.EAST));
        }
		if(face == 2) {
            return(block.getRelative(BlockFace.SOUTH));
        }
		if(face == 0) {
            return(block.getRelative(BlockFace.WEST));
        }
		
		return(null);
	}
	
	
	public static BlockFace getPistonFacing(Block block){
		Material type = block.getType();
		
		if((type != Material.PISTON_BASE) && (type != Material.PISTON_STICKY_BASE) && (type != Material.PISTON_EXTENSION)){
			return(BlockFace.SELF);
		}
		
		
		int			face = block.getData() & 0x7;
		
		switch(face){
			case 0: return(BlockFace.DOWN);
			case 1: return(BlockFace.UP);
			case 2: return(BlockFace.EAST);
			case 3: return(BlockFace.WEST);
			case 4: return(BlockFace.NORTH);
			case 5: return(BlockFace.SOUTH);
		}
		
		return(BlockFace.SELF);
	}
}
