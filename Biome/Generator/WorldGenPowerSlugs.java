package Reika.Satisforestry.Biome.Generator;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Instantiable.Math.Noise.VoronoiNoiseGenerator;
import Reika.DragonAPI.Libraries.World.ReikaChunkHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.DecoratorPinkForest;
import Reika.Satisforestry.Blocks.BlockPowerSlug;
import Reika.Satisforestry.Blocks.BlockPowerSlug.TilePowerSlug;
import Reika.Satisforestry.Entity.EntityEliteStinger;
import Reika.Satisforestry.Registry.SFBlocks;

public class WorldGenPowerSlugs {

	private final VoronoiNoiseGenerator[] noise = new VoronoiNoiseGenerator[3];

	public WorldGenPowerSlugs() {

	}

	public int generate(World world, Random rand, int chunkX, int chunkZ) {
		this.initNoise(world);
		int flags = 0;
		for (int i = 0; i < 3; i++) {
			if (!noise[i].chunkContainsCenter(chunkX, chunkZ))
				continue;
			ArrayList<Coordinate> li = ReikaChunkHelper.getChunkCoords(chunkX, chunkZ);
			while (!li.isEmpty()) {
				Coordinate c = li.remove(rand.nextInt(li.size()));
				if (!Satisforestry.isPinkForest(world, c.xCoord, c.zCoord))
					continue;
				int dy = DecoratorPinkForest.getTrueTopAt(world, c.xCoord, c.zCoord)+1;
				if (dy < 80)
					continue;
				if (this.isReplaceable(world, c.xCoord, dy, c.zCoord) && SFBlocks.SLUG.getBlockInstance().canBlockStay(world, c.zCoord, dy, c.zCoord)) {
					TilePowerSlug te = BlockPowerSlug.generatePowerSlugAt(world, c.xCoord, dy, c.zCoord, i);
					if (te != null) {
						switch(i) {
							case 0:
								te.setMobType(rand.nextInt(4) == 0 ? EntityCaveSpider.class : EntitySpider.class);
								break;
							case 1:
								te.setSingleStrongEnemy(rand.nextInt(3) == 0 ? EntityEliteStinger.class : EntitySpider.class, 3);
								break;
							case 2:
								te.setEnemyBoost(3);
								te.setMobType(EntityEliteStinger.class);
								break;
						}

						flags |= (1 << i);
						break;
					}
				}
			}
		}

		return flags;
	}

	private void initNoise(World world) {
		if (noise[0] == null || noise[0].seed != world.getSeed()) {
			noise[0] = new VoronoiNoiseGenerator(world.getSeed());
			noise[1] = new VoronoiNoiseGenerator(-2*world.getSeed()/3);
			noise[2] = new VoronoiNoiseGenerator(~world.getSeed()+622381);
			noise[0].setFrequency(1/150D);
			noise[1].setFrequency(1/250D);
			noise[2].setFrequency(1/400D);
			noise[0].randomFactor = 0.35;
			noise[1].randomFactor = 0.55;
			noise[2].randomFactor = 0.75;
		}
	}

	private boolean isReplaceable(World world, int x, int y, int z) {
		return ReikaWorldHelper.softBlocks(world, x, y, z);// || world.getBlock(x, y, z).isLeaves(world, x, y, z);
	}

}
