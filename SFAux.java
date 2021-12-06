
package Reika.Satisforestry;

import java.util.Map;
import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

import Reika.DragonAPI.ModList;
import Reika.DragonAPI.Auxiliary.WorldGenInterceptionRegistry.BlockSetData;
import Reika.DragonAPI.Auxiliary.WorldGenInterceptionRegistry.BlockSetWatcher;
import Reika.DragonAPI.Auxiliary.WorldGenInterceptionRegistry.IWGWatcher;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Libraries.World.ReikaChunkHelper;
import Reika.DragonAPI.ModInteract.ItemHandlers.ThaumItemHelper;
import Reika.DragonAPI.ModInteract.ItemHandlers.TinkerBlockHandler;
import Reika.DragonAPI.ModRegistry.InterfaceCache;
import Reika.Satisforestry.Biome.BiomePinkForest;
import Reika.Satisforestry.Registry.SFBlocks;

import cpw.mods.fml.common.IWorldGenerator;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.nodes.NodeType;

public class SFAux {

	public static final IWGWatcher slimeIslandBlocker = new IWGWatcher() {

		@Override
		public boolean canIWGRun(IWorldGenerator gen, Random random, int cx, int cz, World world, IChunkProvider generator, IChunkProvider loader) {
			if (ReikaChunkHelper.chunkContainsBiomeType(world, cx, cz, BiomePinkForest.class)) {
				return BiomePinkForest.canRunGenerator(gen);
			}
			return true;
		}

	};

	public static final BlockSetWatcher populationWatcher = new BlockSetWatcher() {

		@Override
		public void onChunkGeneration(World world, Map<Coordinate, BlockSetData> set) {
			for (Coordinate c : set.keySet()) {
				if (Satisforestry.isPinkForest(world, c.xCoord, c.zCoord)) {
					BlockSetData dat = set.get(c);
					if (dat.newBlock == ThaumItemHelper.BlockEntry.TOTEM.getBlock()) {
						dat.revert(world);
					}
					else if (dat.newBlock == Blocks.obsidian && c.yCoord >= 100) {
						dat.revert(world);
					}
					else if (ModList.TINKERER.isLoaded() && TinkerBlockHandler.getInstance().isSlimeIslandBlock(dat.newBlock, dat.newMetadata)) {
						dat.revert(world);
					}
					else if (InterfaceCache.NODE.instanceOf(dat.getTileEntity(world))) {
						TileEntity te = dat.getTileEntity(world);
						INode n = (INode)te;
						n.setNodeType(NodeType.NORMAL);
					}
				}
			}
		}

	};

	public static int getSlugHelmetTier(EntityLivingBase ep) {
		ItemStack helm = ep.getEquipmentInSlot(4);
		if (helm == null)
			return 0;
		if (SFBlocks.SLUG.matchWith(helm)) {
			return helm.getItemDamage()%3+1;
		}
		if (helm.stackTagCompound != null && helm.stackTagCompound.hasKey("slugUpgrade")) {
			return helm.stackTagCompound.getInteger("slugUpgrade");
		}
		return 0;
	}

}
