package Reika.Satisforestry.Biome.Biomewide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Instantiable.Data.Immutable.DecimalPosition;
import Reika.DragonAPI.Instantiable.Data.Immutable.WorldLocation;
import Reika.DragonAPI.Instantiable.Math.Spline;
import Reika.DragonAPI.Instantiable.Math.Spline.BasicSplinePoint;
import Reika.DragonAPI.Instantiable.Math.Spline.SplineType;
import Reika.DragonAPI.Libraries.ReikaNBTHelper.NBTTypes;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.World.ReikaBlockHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.Biome.BiomeFootprint;
import Reika.Satisforestry.Biome.BiomeFootprint.EdgeProfile;
import Reika.Satisforestry.Biome.DecoratorPinkForest;

public class MantaGenerator {

	public static final MantaGenerator instance = new MantaGenerator();

	private static final double ANGLE_STEP = 6;//2.5;//10;
	private static final double ANGLE_FUZZ = 2;
	private static final double MAX_RISE_STEP = 14;//20;//5;
	private static final double MAX_DROP_STEP = 10;

	private MantaGenerator() {

	}

	public MantaPath generateLoopAround(WorldLocation loc) {
		Spline s = new Spline(SplineType.CHORDAL);
		for (double a = 0; a < 360; a += 22.5) {
			double r = ReikaRandomHelper.getRandomBetween(20D, 40D);
			double ra = Math.toRadians(ReikaRandomHelper.getRandomPlusMinus(a, 8D));
			double dx = loc.xCoord+0.5+r*Math.cos(ra);
			double dz = loc.zCoord+0.5+r*Math.sin(ra);
			s.addPoint(new BasicSplinePoint(dx, loc.yCoord+ReikaRandomHelper.getRandomBetween(3, 6), dz));
		}
		return new MantaPath(loc, s.get(128, true));
	}

	public MantaPath generatePathAroundBiome(World world, BiomeFootprint bf, Random rand) {
		Vec3 cc = bf.getCenter();
		WorldLocation ctr = new WorldLocation(world, new Coordinate(cc));
		Spline s = new Spline(SplineType.CHORDAL);
		double lastY = -1;
		Coordinate lastEdge = null;
		double lastOut = -1;
		double df = ANGLE_STEP/10D;
		int misses = 0;
		EdgeProfile prof = bf.generateEdgeProfile(ANGLE_FUZZ);
		prof.sort(new Comparator<Coordinate>() {

			@Override
			public int compare(Coordinate o1, Coordinate o2) {
				return -Double.compare(o1.getDistanceTo(cc.xCoord, o1.yCoord, cc.zCoord), o2.getDistanceTo(cc.xCoord, o2.yCoord, cc.zCoord));
			}

		});
		for (double a = 0; a < 360; a += ANGLE_STEP) {
			double a2 = ReikaRandomHelper.getRandomPlusMinus(a, ANGLE_FUZZ, rand);
			double out = ReikaRandomHelper.getRandomBetween(18, 40, rand); //was 8/24
			if (lastOut >= 0) {
				out = MathHelper.clamp_double(out, lastOut-5*df, lastOut+5*df);
			}
			lastOut = out;
			Coordinate edge = prof.getFirstEdge(a2); //farthest after sorting
			if (edge == null) {
				misses++;
				if (misses >= 5)
					return null;
				else
					continue;
			}
			edge = edge.to2D();
			double dx = edge.xCoord-cc.xCoord;
			double dz = edge.zCoord-cc.zCoord;
			double ddd = ReikaMathLibrary.py3d(dx, 0, dz);
			dx /= ddd;
			dz /= ddd;
			edge = new Coordinate(MathHelper.floor_double(edge.xCoord+dx*out), 0, MathHelper.floor_double(edge.zCoord+dz*out));
			misses = 0;
			if (lastEdge != null && edge.getDistanceTo(lastEdge) > 12) {
				//continue;
			}
			Block at = null;
			int top = -1;
			for (int i = -1; i <= 1; i++) {
				for (int k = -1; k <= 1; k++) {
					int top2 = ReikaWorldHelper.getTopNonAirBlock(world, edge.xCoord+i, edge.zCoord+k, true);
					if (top2 > top) {
						top = top2;
						at = world.getBlock(edge.xCoord+i, top, edge.zCoord+k);
					}
				}
			}
			boolean liq = ReikaBlockHelper.isLiquid(at);
			double min = liq ? 4 : 25; //was 6/25, then 6/40, then 6/25 again
			double max = liq ? 12 : 90; //was 18/90
			if (at == Blocks.sand) {
				min = 12;
				max = 48;
			}
			double dy = top+ReikaRandomHelper.getRandomBetween(min, max, rand);
			//ReikaJavaLibrary.pConsole(lastY+" - "+edge.getBiome(world).biomeName+" @ "+edge+" > "+top+" = "+at+" >> "+dy+" / "+(lastY+MAX_RISE_STEP*df));
			double spiral = 0;
			double minY = lastY-MAX_DROP_STEP*df;
			if (lastY >= 0) {
				if (lastY-dy >= 18 && s.length() > 10 && rand.nextInt(5) > 0) { //was 100%
					spiral = lastY-dy;
					spiral *= ReikaRandomHelper.getRandomBetween(0.75, 1, rand);
				}
				else {
					dy = MathHelper.clamp_double(dy, minY, lastY+MAX_RISE_STEP*df);
				}
			}
			lastEdge = edge;
			ArrayList<DecimalPosition> spiralPath = new ArrayList();
			if (spiral > 0) {
				int spiralDir = rand.nextBoolean() ? 1 : -1;
				DecimalPosition prev = s.getLast();
				double vx = edge.xCoord-s.getLast().xCoord;
				double vz = edge.zCoord-s.getLast().zCoord;
				double dd = ReikaMathLibrary.py3d(vx, 0, vz);
				vx /= dd;
				vz /= dd;
				double vx2 = -vz;
				double vz2 = vx;
				double rs = ReikaRandomHelper.getRandomBetween(15D, 25D, rand)*spiralDir; //was 8/15
				spiralPath.add(new DecimalPosition(edge.xCoord+0.5, lastY, edge.zCoord+0.5));
				spiralPath.add(new DecimalPosition(edge.xCoord+0.5+vx*rs, lastY-spiral/4D, edge.zCoord+0.5+vz*rs));
				spiralPath.add(new DecimalPosition(edge.xCoord+0.5+vx*rs+vx2*rs, lastY-spiral/2D, edge.zCoord+0.5+vz*rs+vz2*rs));
				spiralPath.add(new DecimalPosition(edge.xCoord+0.5+vx2*rs, lastY-spiral*3/4D, edge.zCoord+0.5+vz2*rs));
			}
			boolean flag = true;
			for (DecimalPosition pos : spiralPath) {
				if (!pos.isEmpty(world)) {
					flag = false;
				}
			}
			if (flag) {
				for (DecimalPosition pos : spiralPath) {
					s.addPoint(new BasicSplinePoint(pos));
				}
			}
			else {
				dy = MathHelper.clamp_double(dy, minY, lastY+MAX_RISE_STEP*df);
			}
			s.addPoint(new BasicSplinePoint(edge.xCoord+0.5, dy, edge.zCoord+0.5));
			lastY = dy;
		}
		return new MantaPath(ctr, s.get(64, true));
	}

	public static class MantaPath {

		public final WorldLocation biomeCenter;
		private final List<DecimalPosition> path;

		private MantaPath(WorldLocation loc, List<DecimalPosition> li) {
			biomeCenter = loc;
			path = li;
		}

		public void clearBlocks(World world) {
			HashSet<Coordinate> clear = new HashSet();
			int r = 4;
			for (DecimalPosition p : path) {
				for (int i = -r; i <= r; i++) {
					for (int j = -r; j <= r; j++) {
						for (int k = -r; k <= r; k++) {
							if (ReikaMathLibrary.py3d(i, j, k) <= r+0.5) {
								Coordinate c = new Coordinate(p.xCoord+i, p.yCoord+j, p.zCoord+k);
								clear.add(c);
							}
						}
					}
				}
			}
			for (Coordinate c : clear) {
				if (DecoratorPinkForest.isTerrain(world, c.xCoord, c.yCoord, c.zCoord)) {
					c.setBlock(world, Blocks.air);
				}
			}
		}

		@Override
		public String toString() {
			return biomeCenter+" @ "+path.toString();
		}

		public List<DecimalPosition> getSpline() {
			return Collections.unmodifiableList(path);
		}

		public static MantaPath readFromNBT(NBTTagCompound tag) {
			List<DecimalPosition> li0 = new ArrayList();
			NBTTagList li = tag.getTagList("path", NBTTypes.COMPOUND.ID);
			for (Object o : li.tagList) {
				NBTTagCompound at = (NBTTagCompound)o;
				li0.add(DecimalPosition.readTag(at));
			}
			WorldLocation ctr = WorldLocation.readTag(tag.getCompoundTag("center"));
			return new MantaPath(ctr, li0);
		}

		public NBTTagCompound writeToNBT() {
			NBTTagCompound ret = new NBTTagCompound();
			ret.setTag("center", biomeCenter.writeToTag());
			NBTTagList li = new NBTTagList();
			for (DecimalPosition p : path) {
				li.appendTag(p.writeToTag());
			}
			ret.setTag("path", li);
			return ret;
		}

	}

}
