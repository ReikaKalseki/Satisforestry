package Reika.Satisforestry.Biome.Biomewide;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
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
import Reika.Satisforestry.Biome.BiomeFootprint;

public class MantaGenerator {

	public static final MantaGenerator instance = new MantaGenerator();

	private static final double ANGLE_STEP = 10;
	private static final double ANGLE_FUZZ = 4;

	private MantaGenerator() {

	}

	public MantaPath generatePathAroundBiome(World world, BiomeFootprint bf, Random rand) {
		Vec3 cc = bf.getCenter();
		WorldLocation ctr = new WorldLocation(world, new Coordinate(cc));
		Spline s = new Spline(SplineType.CHORDAL);
		int lastY = -1;
		int biomeSize = GenLayer.getModdedBiomeSize(world.getWorldInfo().getTerrainType(), (byte)(world.getWorldInfo().getTerrainType() == WorldType.LARGE_BIOMES ? 6 : 4));
		for (double a = 0; a < 360; a += ANGLE_STEP) {
			double a2 = ReikaRandomHelper.getRandomPlusMinus(a, ANGLE_FUZZ, rand);
			double out = ReikaRandomHelper.getRandomBetween(8, 24, rand);
			Coordinate edge = bf.getEdgeAt(a2, 1024*biomeSize, out); //was 1024, then 2048
			if (edge == null)
				return null;
			int top = world.getTopSolidOrLiquidBlock(edge.xCoord, edge.zCoord)-1;
			while (ReikaBlockHelper.isLiquid(world.getBlock(edge.xCoord, top+1, edge.zCoord))) {
				top++;
			}
			Block at = world.getBlock(edge.xCoord, top, edge.zCoord);
			boolean liq = ReikaBlockHelper.isLiquid(at);
			int min = liq ? 5 : 25;
			int max = liq ? 30 : 90;
			int dy = top+ReikaRandomHelper.getRandomBetween(min, max, rand);
			if (lastY >= 0) {
				dy = MathHelper.clamp_int(dy, lastY-20, lastY+20);
			}
			lastY = dy;
			s.addPoint(new BasicSplinePoint(edge.xCoord+0.5, dy, edge.zCoord+0.5));
		}
		return new MantaPath(ctr, s.get(32, true));
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
