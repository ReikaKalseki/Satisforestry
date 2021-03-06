package Reika.Satisforestry.Biome.Generator;

import java.util.List;
import java.util.Random;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Instantiable.Data.Immutable.DecimalPosition;
import Reika.DragonAPI.Instantiable.Math.Spline;
import Reika.DragonAPI.Instantiable.Math.Spline.BasicSplinePoint;
import Reika.DragonAPI.Instantiable.Math.Spline.SplineType;
import Reika.DragonAPI.Libraries.ReikaDirectionHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.Satisforestry.Satisforestry;

public class PinkTreeGenerator extends PinkTreeGeneratorBase {

	public PinkTreeGenerator(boolean force) {
		super(force, PinkTreeTypes.TREE);
	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		if (!forceGen) {
			if (y < 96) //was 90
				return false;
		}
		int h = ReikaRandomHelper.getRandomBetween(10, 16, rand)-2;
		int hl = Math.min(h-4, ReikaRandomHelper.getRandomBetween(6, 9, rand));
		this.resetHeight();
		leafDistanceLimit = rand.nextInt(3) == 0 ? 3 : 2;
		heightLimitLimit = h;
		branchSlope = ReikaRandomHelper.getRandomPlusMinus(0, BASE_SLOPE*1.5, rand);
		heightAttenuation = BASE_ATTENUATION;
		//minBranchHeight = hl*0+12;
		minHeight = hl;
		globalOffset[1] = Math.max(hl-4, 0);
		/*
		for (int i = 0; i <= h; i++) {
			if (!world.getBlock(x, y+i, z).isAir(world, x, y+i, z))
				return false;
		}
		for (int i = 0; i < h; i++) {
			world.setBlock(x, y+i, z, Satisforestry.log);

			/*
			if (i >= hl) {
				if (rand.nextDouble() < 0.2) {
					double ang = this.generateBranch(world, rand, x, y+i, z);
				}
			}*//*
		}
		int branches = ReikaRandomHelper.getRandomBetween(6, 18, rand);
		double ang = 360D/branches;
		for (int i = 0; i < branches; i++) {
			double a1 = ReikaRandomHelper.getRandomPlusMinus(ang*i, ang*0.33, rand);
			int dy = ReikaRandomHelper.getRandomBetween(hl, h, rand);
			this.generateBranch(world, rand, x, y+dy, z, a1);
		}
		return true;
			 */
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

	private void generateBranch(World world, Random rand, int x, int y, int z, double ang) {
		double a = Math.toRadians(ang);
		double vx = Math.cos(a);
		double vz = Math.sin(a);
		double len = ReikaRandomHelper.getRandomBetween(5, 10, rand);

		Spline s = new Spline(SplineType.UNIFORM);
		s.addPoint(new BasicSplinePoint(x+0.5, y+0.5, z+0.5));
		double d = 0;
		double left = len-d;
		while (left > 0) {
			double step = ReikaRandomHelper.getRandomBetween(0.1875, 0.5, rand)*left;
			if (left < 4) {
				step = left;
			}
			d += step;

			double df = d/len;
			double dd = 2.5*Math.min(1, 1.25*df*df);//1.5;
			double dx = ReikaRandomHelper.getRandomPlusMinus(0, dd, rand);
			double dy = ReikaRandomHelper.getRandomPlusMinus(0, dd, rand);
			double dz = ReikaRandomHelper.getRandomPlusMinus(0, dd, rand);
			s.addPoint(new BasicSplinePoint(s.getLast().offset(vx*step+dx, dy, vz*step+dz)));

			left = len-d;
		}

		List<DecimalPosition> li = s.get(32, false);

		/*
		for (int i = 0; i < li.size()-1; i++) {
			DecimalPosition from = li.get(i);
			DecimalPosition to = li.get(i+1);
			double dx = to.xCoord-from.xCoord;
			double dy = to.yCoord-from.yCoord;
			double dz = to.zCoord-from.zCoord;
			double dd = ReikaMathLibrary.py3d(dx, dy, dz);
			dx /= dd;
			dy /= dd;
			dz /= dd;
			for (int k = 0; k <= dd; k++) {
				int x0 = MathHelper.floor_double(dx*k);
				int y0 = MathHelper.floor_double(dy*k);
				int z0 = MathHelper.floor_double(dz*k);
				world.setBlock(x0, y0, z0, Satisforestry.log, this.getLogMetaForAngle(Math.toDegrees(Math.atan2(dz, dx))), 2);
			}
		}
		 */

		for (DecimalPosition pos : li) {
			pos.getCoordinate().setBlock(world, Satisforestry.log, 0, 2);
		}

		/*
		double vy = 0;
		double vang = rand.nextDouble()*4;
		double ay = rand.nextDouble()*0.15;
		double aang = rand.nextDouble()-0.5;
		double a2y = rand.nextDouble()*0.002;

		double cx = x+0.5;
		double cy = y+0.5;
		double cz = z+0.5;

		int n = 0;
		while (true) {
			n++;
			double a = Math.toRadians(ang);
			double vx = Math.cos(a);
			double vz = Math.sin(a);

			cx += vx;
			cy += vy;
			cz += vz;

			int dx = MathHelper.floor_double(cx);
			int dy = MathHelper.floor_double(cy);
			int dz = MathHelper.floor_double(cz);
			world.setBlock(dx, dy, dz, Satisforestry.log, this.getLogMetaForAngle(ang), 2); //meta 0 -> U/D; 4 -> W/E; 8 -> N/S

			ang += vang;
			vy += ay;

			vang += aang;
			ay += a2y;

			if (Math.abs(vy) >= 2 || ReikaMathLibrary.py3d(dx-x, 0, dz-z) > 12 || n > 40)
				return;
		}
		 */
	}

	private int getLogMetaForAngle(double ang) {
		ForgeDirection dir = ReikaDirectionHelper.getByHeading(ang);
		if (Math.abs(dir.offsetX) == 1)
			return 4;
		if (Math.abs(dir.offsetZ) == 1)
			return 8;
		return 0;
	}

	/*
	private double generateBranch(World world, Random rand, int x, int y, int z	) {

	}*/

	@Override
	protected float layerSize(int layer) {
		return super.layerSize(layer)*0.8F;
	}

	@Override
	protected float leafSize(int r) {
		return super.leafSize(r);
	}

}
