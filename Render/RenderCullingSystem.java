package Reika.Satisforestry.Render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Event.Client.ChunkWorldRenderEvent.ChunkWorldRenderWatcher;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.Biomewide.BiomewideFeatureGenerator;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCullingSystem implements ChunkWorldRenderWatcher {

	public static final RenderCullingSystem instance = new RenderCullingSystem();

	private final int[] offsets = new int[] {0, 8, 15};

	private RenderCullingSystem() {

	}

	@Override
	public boolean interceptChunkRender(WorldRenderer wr, int renderPass, int GLListID) {
		EntityPlayer ep = Minecraft.getMinecraft().thePlayer;
		int x = MathHelper.floor_double(ep.posX);
		int z = MathHelper.floor_double(ep.posZ);
		if (Satisforestry.isPinkForest(ep.worldObj, x, z)) {
			if (BiomewideFeatureGenerator.instance.isInCave(ep.worldObj, x, MathHelper.floor_double(ep.posY), z)) {
				if (wr.posY >= ep.posY+20)
					return true;
			}
			else if (this.intersectsCave(ep.worldObj, wr) && ep.getDistanceSq(wr.posX+8.5, wr.posY+8.5, wr.posZ+8.5) >= 1024) {
				return true;
			}
		}
		return false;
	}

	private boolean intersectsCave(World world, WorldRenderer wr) {
		for (int dx : offsets) {
			for (int dy : offsets) {
				for (int dz : offsets) {
					if (BiomewideFeatureGenerator.instance.isInCave(world, wr.posX+dx, wr.posY+dy, wr.posZ+dz))
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public int chunkRenderSortIndex() {
		return 0;
	}

}
