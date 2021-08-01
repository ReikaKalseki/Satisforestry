package Reika.Satisforestry.API;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

/** Implement this to use code-based custom node proximity effects. */
public interface NodeEffectCallback {

	/** The descriptive comment added to any generated LuaBlock entries. Use null for none, not empty string. */
	String getComment();

	/** Apply the effect. This is called every tick - serverside only - for every player in the AoE of the node TE. */
	public void apply(TileEntity node, EntityPlayer ep);

}
