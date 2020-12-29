package Reika.Satisforestry.Biome;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.ModInteract.DeepInteract.ReikaMystcraftHelper;
import Reika.DragonAPI.ModInteract.DeepInteract.ReikaMystcraftHelper.BasicPages;
import Reika.Satisforestry.Satisforestry;

public class BiomeFootprint {

	private final HashSet<Coordinate> coords = new HashSet();
	private final HashSet<Coordinate> edgeCoords = new HashSet();

	private Vec3 center = Vec3.createVectorHelper(0, 0, 0);

	private int minX = Integer.MAX_VALUE;
	private int maxX = Integer.MIN_VALUE;
	private int minZ = Integer.MAX_VALUE;
	private int maxZ = Integer.MIN_VALUE;

	public BiomeFootprint() {

	}

	/* recursive version
	private void calculate(World world, int x, int z) {
		Coordinate loc = new Coordinate(x, 0, z);
		if (!coords.contains(loc) && Satisforestry.isPinkForest(world, x, z)) {
			coords.add(loc);
			center.xCoord += x+0.5;
			center.zCoord += z+0.5;
			this.calculate(world, x-1, z);
			this.calculate(world, x+1, z);
			this.calculate(world, x, z-1);
			this.calculate(world, x, z+1);
		}
	}*/

	public boolean calculate(World world, int x, int z) {
		if (ModList.MYSTCRAFT.isLoaded() && this.isSingleBiomeWorld(world))
			return false;
		HashSet<Coordinate> next = new HashSet();
		Coordinate loc = new Coordinate(x, 0, z);
		next.add(loc);
		while (!next.isEmpty()) {
			HashSet<Coordinate> newNext = new HashSet();
			for (Coordinate c : next) {
				if (!this.searchFrom(world, c, newNext)) { //c was not in the biome, its neighbors are edges
					for (Coordinate c2 : c.getAdjacentCoordinates())
						edgeCoords.add(c2.to2D());
				}
			}
			next = newNext;
		}
		edgeCoords.retainAll(coords);
		if (coords.isEmpty())
			return false;
		center.xCoord /= coords.size();
		center.zCoord /= coords.size();
		return true;
	}

	@ModDependent(ModList.MYSTCRAFT)
	private boolean isSingleBiomeWorld(World world) {
		return ReikaMystcraftHelper.isMystAge(world) && ReikaMystcraftHelper.isSymbolPresent(world, BasicPages.BiomeControllerSingle);
	}

	public int blockCount() {
		return coords.size();
	}

	public int sizeX() {
		return maxX-minX+1;
	}

	public int sizeZ() {
		return maxZ-minZ+1;
	}

	public int getArea() {
		return coords.size();
	}

	public Vec3 getCenter() {
		return Vec3.createVectorHelper(center.xCoord, center.yCoord, center.zCoord);
	}

	private boolean searchFrom(World world, Coordinate loc, HashSet<Coordinate> newNext) {
		int x = loc.xCoord;
		int z = loc.zCoord;
		boolean biome = Satisforestry.isPinkForest(world, x, z);
		if (!coords.contains(loc) && biome) {
			coords.add(loc);
			center.xCoord += x+0.5;
			center.zCoord += z+0.5;
			minX = Math.min(minX, loc.xCoord);
			minZ = Math.min(minZ, loc.zCoord);
			maxX = Math.max(maxX, loc.xCoord);
			maxZ = Math.max(maxZ, loc.zCoord);
			newNext.add(new Coordinate(x-1, 0, z));
			newNext.add(new Coordinate(x+1, 0, z));
			newNext.add(new Coordinate(x, 0, z-1));
			newNext.add(new Coordinate(x, 0, z+1));
		}
		return biome;
	}

	public Set<Coordinate> getCoords() {
		return Collections.unmodifiableSet(coords);
	}

	public Set<Coordinate> getEdges() {
		return Collections.unmodifiableSet(edgeCoords);
	}

	public double getAngleAt(Coordinate c, int searchRadius) { //linear regression https://i.imgur.com/La8ge8z.png
		ArrayList<Coordinate> li = new ArrayList();

		double sumX = c.xCoord;
		double sumY = c.zCoord;
		double sumX2 = c.xCoord*c.xCoord;
		double sumXY = c.xCoord*c.zCoord;

		for (int i = -searchRadius; i <= searchRadius; i++) {
			for (int k = -searchRadius; k <= searchRadius; k++) {
				Coordinate c2 = c.offset(i, 0, k);
				if (!c2.equals(c) && edgeCoords.contains(c2)) {
					li.add(c2);
					sumX += c2.xCoord;
					sumY += c2.zCoord;
					sumX2 += c2.xCoord*c2.xCoord;
					sumXY += c2.xCoord*c2.zCoord;
				}
			}
		}
		if (li.isEmpty()) { //cannot determine angle for isolated cell
			return 0;//Double.NaN;
		}

		li.add(c);

		double num = li.size()*sumXY-sumX*sumY;
		double denom = li.size()*sumX2-sumX*sumX;
		double slope = num/denom;

		/*
		ArrayList<Point> li2 = new ArrayList();
		for (Coordinate c2 : li) {
			li2.add(new Point(c2.xCoord-c.xCoord, c2.zCoord-c.zCoord));
		}
		ReikaJavaLibrary.pConsole(c+" > "+li2+" > "+slope+" > "+Math.toDegrees(Math.atan(slope)));
		 */

		return Math.toDegrees(Math.atan(slope));
	}

	public boolean isConcave() {
		return !coords.contains(new Coordinate(center));
	}

	public EdgeProfile generateEdgeProfile(double angleStep) {
		EdgeProfile ret = new EdgeProfile(angleStep);
		ret.populate();
		return ret;
	}

	public Coordinate getEdgeAt(double ang, double maxr) {
		return this.getEdgeAt(ang, maxr, 0);
	}

	public Coordinate getEdgeAt(double ang, double maxr, double outset) {
		Vec3 pos = Vec3.createVectorHelper(center.xCoord, center.yCoord, center.zCoord);
		double angr = Math.toRadians(ang);
		double dx = Math.cos(angr);
		double dz = Math.sin(angr);
		double dd = 0.5;
		//int n = this.isConcave() ? 2 : 1;
		//int n2 = 0;
		int dAtEdge = -1;
		boolean wasIn = this.isConcave() ? false : true;
		Coordinate ret = null;
		for (int d = 0; d <= maxr/dd; d++) {
			pos.xCoord += dx*dd;
			pos.zCoord += dz*dd;
			Coordinate at = new Coordinate(pos.xCoord, 0, pos.zCoord);
			boolean in = coords.contains(at);
			if (wasIn && !in) {
				dAtEdge = d;
				if (outset == 0) {
					ret = at;
				}
				else {
					pos.xCoord += dx*outset;
					pos.zCoord += dz*outset;
					ret = new Coordinate(pos.xCoord, 0, pos.zCoord);
				}
				//ReikaJavaLibrary.pConsole(ang+": found edge @ "+d);
			}
			wasIn = in;
			if (ret != null && dAtEdge >= 0 && d-dAtEdge > 25+outset*2) {
				//ReikaJavaLibrary.pConsole(ang+": break @ "+d);
				break;
			}
		}
		//ReikaJavaLibrary.pConsole(ang+": return "+ret);
		return ret;
	}

	public void exportToImage(File folder) {
		folder.mkdirs();
		File f = new File(folder, "biomefootprint "+MathHelper.floor_double(center.xCoord)+", "+MathHelper.floor_double(center.zCoord)+".png");
		int r = 20;
		int x0 = minX-r;
		int x1 = maxX+r;
		int z0 = minZ-r;
		int z1 = maxZ+r;
		BufferedImage buf = new BufferedImage(x1-x0+1, z1-z0+1, BufferedImage.TYPE_INT_ARGB);
		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				int i = x-x0;
				int k = z-z0;
				Coordinate pos = new Coordinate(x, 0, z);
				int clr = 0xffffff;
				if (edgeCoords.contains(pos))
					clr = 0xff0000;
				else if (coords.contains(pos))
					clr = 0x22aaff;
				buf.setRGB(i, k, 0xff000000 | clr);
			}
		}
		try {
			ImageIO.write(buf, "png", f);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public class EdgeProfile {

		public final double angleStep;

		private final ArrayList<Coordinate>[] edges;

		private EdgeProfile(double step) {
			angleStep = step;
			int n = MathHelper.ceiling_double_int(360D/step);
			edges = new ArrayList[n];
			for (int i = 0; i < n; i++) {
				edges[i] = new ArrayList();
			}
		}

		private void populate() {
			for (Coordinate c : edgeCoords) {
				double dy = c.zCoord-center.zCoord;
				double dx = c.xCoord-center.xCoord;
				double ang = Math.toDegrees(Math.atan2(dy, dx));
				edges[this.getIndex(ang)].add(c);
			}
		}

		public List<Coordinate> getEdgesAtAngle(double ang) {
			return Collections.unmodifiableList(edges[this.getIndex(ang)]);
		}

		public void sort(Comparator<Coordinate> c) {
			for (int i = 0; i < edges.length; i++) {
				Collections.sort(edges[i], c);
			}
		}

		public Coordinate getFirstEdge(double ang) {
			ArrayList<Coordinate> li = edges[this.getIndex(ang)];
			return li.isEmpty() ? null : li.get(0);
		}

		private int getIndex(double angle) {
			angle = ((angle%360)+360)%360;
			double base = angle-angle%angleStep;
			return (int)(base/angleStep);
		}

	}

}
