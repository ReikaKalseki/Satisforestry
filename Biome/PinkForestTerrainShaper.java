package Reika.Satisforestry.Biome;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import Reika.DragonAPI.Instantiable.Worldgen.TerrainShaper;
import Reika.Satisforestry.Biome.BiomePinkForest.BiomeSection;

public class PinkForestTerrainShaper extends TerrainShaper {

	@Override
	protected void generateColumn(World world, int x, int z, Random rand, BiomeGenBase biome) {
		BiomePinkForest bp = (BiomePinkForest)biome;
		int up = bp.getUpthrust(world, x, z);
		int water = 0;
		BiomeSection sub = bp.getSubBiome(world, x, z);
		boolean thinDirt = false;
		if (sub == BiomeSection.STREAMS) {
			int delta = bp.getMiniCliffDelta(world, x, z);
			thinDirt |= delta > 0;
			up += delta;
		}
		else if (sub == BiomeSection.SWAMP) {
			int dep = bp.getSwampDepression(world, x, z);
			if (dep >= 2) {
				water = 0;//dep-1;
			}
			up -= dep;
		}
		int top = this.getTopNonAir(x, z);
		Block at = this.getBlock(x, top, z);
		if (at != Blocks.water) {
			this.shiftVertical(x, z, up, Blocks.stone, 0, false);
			double f = top >= 90 ? 1 : (top-60)/30D;
			double road = f*bp.getRoadFactor(world, x, z);
			int y = this.getTopNonAir(x, z);
			if (road > 0 && road >= 0.875 || rand.nextDouble() < road*0.6) { //was 0.875 and 0.75
				this.setBlock(x, y, z, Blocks.sand);
			}
			int dirtThickness = bp.getDirtThickness(world, x, z);
			if (thinDirt) {
				dirtThickness -= 2;
			}
			dirtThickness = Math.max(1, dirtThickness);
			for (int dt = 1; dt <= dirtThickness; dt++) {
				this.setBlock(x, y-dt, z, Blocks.dirt);
			}

			this.cleanColumn(world, x, z, biome);
		}
	}

	@Override
	protected boolean shouldClear() {
		return false;
	}

}
