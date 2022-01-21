package Reika.Satisforestry.Biome.Generator;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Libraries.ReikaDirectionHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.Satisforestry.Satisforestry;

public class PinkTreeGenerator extends PinkTreeGeneratorBase {

	public float heightScalar = 1;

	public PinkTreeGenerator(boolean force) {
		super(force, PinkTreeTypes.TREE);
	}

	public void setTrunkSize(int s) {
		trunkSize = s;
	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		if (!forceGen) {
			if (y < 96) //was 90
				return false;
		}
		int h = ReikaRandomHelper.getRandomBetween(10, 16, rand)-2;
		int hl = Math.min(h-4, ReikaRandomHelper.getRandomBetween(6, 9, rand));
		h *= heightScalar;
		hl *= heightScalar;
		this.resetHeight();
		leafDistanceLimit = rand.nextInt(isSaplingGrowth ? 2 : 3) == 0 ? 3 : 2;
		heightLimitLimit = h;
		branchSlope = ReikaRandomHelper.getRandomPlusMinus(0, BASE_SLOPE*1.5, rand);
		heightAttenuation = BASE_ATTENUATION;
		minHeight = hl;
		globalOffset[1] = Math.max(hl-4, 0);
		if (super.generate(world, rand, x, y, z)) {
			for (int i = 0; i < globalOffset[1]; i++) {
				world.setBlock(x, y+i, z, Satisforestry.log, 0, 2);
			}
			return true;
		}
		else {
			return false;
		}
	}

	private int getLogMetaForAngle(double ang) {
		ForgeDirection dir = ReikaDirectionHelper.getByHeading(ang);
		if (Math.abs(dir.offsetX) == 1)
			return 4;
		if (Math.abs(dir.offsetZ) == 1)
			return 8;
		return 0;
	}

	@Override
	protected float layerSize(int layer) {
		return super.layerSize(layer)*0.8F;
	}

	@Override
	protected float leafSize(int r) {
		return super.leafSize(r);
	}

	@Override
	protected int getDifficultyByHeight(int y, int dy, Random rand) {
		return this.getHeightFraction(y) >= (0.8+rand.nextDouble()*0.3) || dy >= ReikaRandomHelper.getRandomBetween(12, 20, rand) ? 1 : 0;
	}

	@Override
	protected int getSlugByHeight(int y, int dy, Random rand) {
		return rand.nextInt(3) == 0 && (this.getHeightFraction(y) >= 0.95+rand.nextDouble()*0.1 || dy >= ReikaRandomHelper.getRandomBetween(16, 24, rand)) ? 1 : 0;
	}

	@Override
	protected float getTrunkSlugChancePerBlock() {
		return 0.003F;
	}

	@Override
	protected float getTreeTopSlugChance() {
		return 0.01F;
	}

	@Override
	protected boolean canSpawnLeaftopMobs() {
		return false;
	}

	@Override
	protected float getBranchSlugChancePerBlock() {
		return 0;
	}

}
