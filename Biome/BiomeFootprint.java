package Reika.Satisforestry.Biome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

	public Set<Coordinate> getEdges	() {
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

}
