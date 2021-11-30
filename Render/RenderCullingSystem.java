package Reika.Satisforestry.Render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

import Reika.DragonAPI.Instantiable.RayTracer;
import Reika.DragonAPI.Instantiable.Event.Client.ChunkWorldRenderEvent.ChunkWorldRenderWatcher;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Registry.SFBlocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCullingSystem implements ChunkWorldRenderWatcher {

	public static final RenderCullingSystem instance = new RenderCullingSystem();

	private final RayTracer ray = RayTracer.getVisualLOS();

	private final int[] offsets = new int[] {0, 8, 15};

	private RenderCullingSystem() {
		ray.addOpaqueBlock(SFBlocks.LEAVES.getBlockInstance());
	}

	@Override
	public boolean interceptChunkRender(WorldRenderer wr, int renderPass, int GLListID) {
		EntityPlayer ep = Minecraft.getMinecraft().thePlayer;
		int x = MathHelper.floor_double(ep.posX);
		int z = MathHelper.floor_double(ep.posZ);
		if (Satisforestry.isPinkForest(ep.worldObj, x, z)) {
			if (ep.posY < 74) { //lowest ground level in the biome
				if (wr.posY >= ep.posY+20)
					return true;
			}
			else {
				if (wr.posY <= 56)
					return true;
				//check far-dist canopy
				else if (!GuiScreen.isCtrlKeyDown() && this.shouldCullCanopy(wr, ep)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean shouldCullCanopy(WorldRenderer wr, EntityPlayer ep) {
		if (wr.posY < 172 || wr.posY <= ep.posY+40)
			return false;
		if (ep.getDistanceSq(wr.posX+8, ep.posY, wr.posZ+8) < 1024)
			return false;
		if (ep.getDistanceSq(wr.posX+8, wr.posY*0.25+0.75*ep.posY, wr.posZ+8) < 1024*Minecraft.getMinecraft().gameSettings.renderDistanceChunks)
			return false;
		//wr.posY >= 172 && wr.posY >= ep.posY+40 && ep.getDistanceSq(wr.posX+8, ep.posY, wr.posZ+8) >= 1024
		/*
		for (int dx : offsets) {
			for (int dy : offsets) {
				for (int dz : offsets) {
					ray.setOrigins(wr.posX+dx, wr.posY+dy, wr.posZ+dz, ep.posX, ep.posY, ep.posZ);
					if (ray.isClearLineOfSight(ep.worldObj))
						return false;
				}
			}
		}*/
		return true;
	}

	@Override
	public int chunkRenderSortIndex() {
		return 0;
	}

}
