package Reika.Satisforestry.Biome.Biomewide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.layer.GenLayer;

import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Instantiable.Data.Immutable.DecimalPosition;
import Reika.DragonAPI.Instantiable.Data.Immutable.WorldLocation;
import Reika.DragonAPI.Instantiable.Math.Spline;
import Reika.DragonAPI.Instantiable.Math.Spline.BasicSplinePoint;
import Reika.DragonAPI.Instantiable.Math.Spline.SplineType;
import Reika.DragonAPI.Libraries.ReikaNBTHelper.NBTTypes;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.World.ReikaBlockHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.Biome.BiomeFootprint;

public class MantaGenerator {

	public static final MantaGenerator instance = new MantaGenerator();

	private static final double ANGLE_STEP = 2.5;//10;
	private static final double ANGLE_FUZZ = 1;
	private static final double MAX_RISE_STEP = 5;
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
		double lastOut = -1;
		double df = ANGLE_STEP/10D;
		int biomeSize = GenLayer.getModdedBiomeSize(world.getWorldInfo().getTerrainType(), (byte)(world.getWorldInfo().getTerrainType() == WorldType.LARGE_BIOMES ? 6 : 4));
		for (double a = 0; a < 360; a += ANGLE_STEP) {
			double a2 = ReikaRandomHelper.getRandomPlusMinus(a, ANGLE_FUZZ, rand);
			double out = ReikaRandomHelper.getRandomBetween(8, 24, rand);
			if (lastOut >= 0) {
				out = MathHelper.clamp_double(out, lastOut-5*df, lastOut+5*df);
			}
			lastOut = out;
			Coordinate edge = bf.getEdgeAt(a2, 512*biomeSize, out); //was 1024, then 2048,then 1024*size
			if (edge == null)
				return null;
			int top = ReikaWorldHelper.getTopNonAirBlock(world, edge.xCoord, edge.zCoord, true);
			Block at = world.getBlock(edge.xCoord, top, edge.zCoord);
			boolean liq = ReikaBlockHelper.isLiquid(at);
			double min = liq ? 6 : 25;
			double max = liq ? 18 : 90;
			if (at == Blocks.sand) {
				min = 12;
				max = 48;
			}
			double dy = top+ReikaRandomHelper.getRandomBetween(min, max, rand);
			if (lastY >= 0) {
				dy = MathHelper.clamp_double(dy, lastY-MAX_DROP_STEP*df, lastY+MAX_RISE_STEP*df);
			}
			lastY = dy;
			s.addPoint(new BasicSplinePoint(edge.xCoord+0.5, dy, edge.zCoord+0.5));
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
