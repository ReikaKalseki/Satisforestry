package Reika.Satisforestry.Biome.Biomewide;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import com.google.common.base.Strings;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.Immutable.WorldLocation;
import Reika.DragonAPI.Instantiable.Data.Maps.MultiMap;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.BiomeFootprint;
import Reika.Satisforestry.Blocks.PointSpawnBlock;
import Reika.Satisforestry.Blocks.PointSpawnBlock.PointSpawnTile;
import Reika.Satisforestry.Entity.SpawnPointEntity;

public final class PointSpawnSystem {

	private static final HashMap<String, SpawnPointDefinition> spawnerTypes = new HashMap();

	public static final PointSpawnSystem instance = new PointSpawnSystem();

	private static final String SPAWN_NBT_TAG = "PinkForestPointSpawn";
	private static final String KILLED_NBT_TAG = "playerKilled";
	private static final String HOSTILE_NBT_TAG = "alwaysHostile";

	private final LizardDoggoSpawner doggos;
	private final RoadGuardSpawner guards;

	private final HashSet<WorldLocation> locationsUsed = new HashSet();
	private final HashMap<Integer, MultiMap<Class<? extends EntityLiving>, SpawnPoint>> spawns = new HashMap();

	private PointSpawnSystem() {
		doggos = new LizardDoggoSpawner();
		guards = new RoadGuardSpawner();
	}

	public static void registerSpawnerType(SpawnPointDefinition c) {
		spawnerTypes.put(c.getID(), c);
	}

	public void createSpawnPoints(World world, int x, int z, BiomeFootprint bf, Random rand) {
		Collection<SpawnPoint> spawns = doggos.createDoggoSpawnPoints(world, bf, rand);
		Satisforestry.logger.log("Doggo spawn locations around "+x+", "+z+": "+spawns);
		this.addSpawnPoints(spawns);

		spawns = guards.createSpawnPoints(world, bf, rand);
		Satisforestry.logger.log("Road Guard spawn locations around "+x+", "+z+": "+spawns);
		this.addSpawnPoints(spawns);
	}

	public void addSpawnPoints(Collection<SpawnPoint> c) {
		for (SpawnPoint s : c) {
			this.addSpawnPoint(s);
		}
	}

	public void addSpawnPoint(SpawnPoint s) {
		if (Strings.isNullOrEmpty(s.id))
			throw new IllegalArgumentException("Untyped spawner "+s+"!");
		if (spawnerTypes.get(s.id) == null)
			throw new IllegalArgumentException("Unregistered spawner type "+s.getClass()+"!");
		if (s.location == null)
			throw new IllegalArgumentException("Spawnpoint "+s+" has a null location!");
		MultiMap<Class<? extends EntityLiving>, SpawnPoint> map = spawns.get(s.location.dimensionID);
		if (map == null) {
			map = new MultiMap();
			spawns.put(s.location.dimensionID, map);
		}
		map.addValue(s.mobClass, s);
		locationsUsed.add(s.location);
	}

	public void clear() {
		spawns.clear();
		locationsUsed.clear();
	}

	public void loadSpawnPoints(NBTTagList li) {
		this.loadSpawnPoints(li, null);
	}

	public void loadLegacyDoggoSpawns(NBTTagList li) {
		this.loadSpawnPoints(li, "doggo");
	}

	private void loadSpawnPoints(NBTTagList li, String typeOverride) {
		for (Object o : li.tagList) {
			NBTTagCompound tag = (NBTTagCompound)o;
			if (!Strings.isNullOrEmpty(typeOverride))
				tag.setString("spawnerType", typeOverride);
			SpawnPoint c = this.constructSpawn(tag);
			if (c == null)
				continue;
			c.readFromTag(tag);
			if (c.isDead())
				continue;
			this.addSpawnPoint(c);
		}
	}

	private SpawnPoint constructSpawn(NBTTagCompound tag) {
		WorldLocation loc = WorldLocation.readFromNBT("loc", tag);
		if (loc == null)
			return null;
		String type = tag.getString("spawnerType");
		SpawnPointDefinition c = spawnerTypes.get(type);
		if (c == null) {
			Satisforestry.logger.logError("Could not construct spawnpoint of unrecognized/null-mapped type '"+type+"': "+tag);
			return null;
		}
		SpawnPoint s = c.construct(loc);
		if (s != null)
			s.id = type;
		return s;
	}

	public void saveSpawnPoints(NBTTagList li) {
		for (MultiMap<Class<? extends EntityLiving>, SpawnPoint> map : spawns.values()) {
			for (Collection<SpawnPoint> c : map.values()) {
				for (SpawnPoint loc : c) {
					if (loc.isDead())
						continue;
					NBTTagCompound tag = new NBTTagCompound();
					loc.writeToTag(tag);
					if (Strings.isNullOrEmpty(loc.id)) {
						Satisforestry.logger.logError("Could not save spawnpoint of unrecognized/null-mapped type '"+loc.id+"': "+loc);
						continue;
					}
					tag.setString("spawnerType", loc.id);
					li.appendTag(tag);
				}
			}
		}
	}

	public Collection<SpawnPoint> getWorldSpawns(World world) {
		MultiMap<Class<? extends EntityLiving>, SpawnPoint> map = spawns.get(world.provider.dimensionId);
		return map != null ? Collections.unmodifiableCollection(map.allValues(false)) : new ArrayList();
	}

	public Collection<SpawnPoint> getSpawns(World world, Class<? extends EntityLiving> c) {
		MultiMap<Class<? extends EntityLiving>, SpawnPoint> map = spawns.get(world.provider.dimensionId);
		return map != null ? Collections.unmodifiableCollection(map.get(c)) : new ArrayList();
	}

	public SpawnPoint getSpawnAt(WorldLocation loc, Class<? extends EntityLiving> c) {
		if (loc == null)
			return null;
		if (loc.getBlock() instanceof PointSpawnBlock) {
			TileEntity tile = loc.getWorld().getTileEntity(loc.xCoord, loc.yCoord, loc.zCoord);
			return tile instanceof PointSpawnTile ? ((PointSpawnTile)tile).getSpawner() : null;
		}
		MultiMap<Class<? extends EntityLiving>, SpawnPoint> map = spawns.get(loc.dimensionID);
		if (map == null)
			return null;
		for (SpawnPoint spawn : map.get(c)) {
			if (spawn.getLocation().equals(loc)) {
				return spawn;
			}
		}
		return null;
	}

	public SpawnPoint getSpawn(EntityLiving e) {
		return this.getSpawnAt(this.getSpawnLocation(e), e.getClass());
	}

	private WorldLocation getSpawnLocation(EntityLiving e) {
		if (e instanceof SpawnPointEntity) {
			return ((SpawnPointEntity)e).getSpawn();
		}
		else {
			NBTTagCompound tag = e.getEntityData().getCompoundTag(SPAWN_NBT_TAG);
			if (tag.hasNoTags())
				return null;
			return WorldLocation.readFromNBT("location", tag);
		}
	}

	public void onEntityRemoved(EntityLiving e) {
		if (e.worldObj == null || e.worldObj.isRemote)
			return;
		SpawnPoint spawn = this.getSpawn(e);
		if (spawn != null) {
			spawn.removeEntity(e);
		}
	}

	public static void tagEntityAsKilled(EntityLiving e) {
		setTag(e, KILLED_NBT_TAG, true);
	}

	private static void setTag(EntityLiving e, String key, int value) {
		NBTTagCompound tag = e.getEntityData().getCompoundTag(SPAWN_NBT_TAG);
		tag.setInteger(key, value);
		e.getEntityData().setTag(SPAWN_NBT_TAG, tag);
	}

	private static void setTag(EntityLiving e, String key, boolean flag) {
		NBTTagCompound tag = e.getEntityData().getCompoundTag(SPAWN_NBT_TAG);
		tag.setBoolean(key, flag);
		e.getEntityData().setTag(SPAWN_NBT_TAG, tag);
	}

	private static boolean hasTag(EntityLiving e, String key) {
		return e.getEntityData().getCompoundTag(SPAWN_NBT_TAG).getBoolean(key);
	}

	private static int getTag(EntityLiving e, String key) {
		return e.getEntityData().getCompoundTag(SPAWN_NBT_TAG).getInteger(key);
	}

	public boolean isAlwaysHostile(EntityLiving e) {
		return this.hasTag(e, HOSTILE_NBT_TAG);
	}

	public void tick(EntityPlayer ep) {
		World world = ep.worldObj;
		if (world == null || world.isRemote)
			return;
		long time = world.getTotalWorldTime();
		if (time%10 == 0 && Satisforestry.isPinkForest(ep.worldObj, MathHelper.floor_double(ep.posX), MathHelper.floor_double(ep.posZ))) {
			MultiMap<Class<? extends EntityLiving>, SpawnPoint> map = spawns.get(world.provider.dimensionId);
			if (map == null)
				return;
			for (Collection<SpawnPoint> c : map.values()) {
				for (SpawnPoint loc : c) {
					//ReikaJavaLibrary.pConsole(loc);
					loc.tick(ep.worldObj, ep);
				}
			}
		}
	}

	public void removeSpawner(SpawnPoint p) {
		MultiMap<Class<? extends EntityLiving>, SpawnPoint> map = spawns.get(p.getDimension());
		if (map == null)
			return;
		map.removeValue(p);
		p.isDead = true;
	}

	public static abstract class SpawnPoint {

		private final WorldLocation location;

		private int numberToSpawn;
		private double activationRadius;
		private Class<? extends EntityLiving> mobClass;
		private String mobType;
		private int emptyTimeout = -1;

		private int existingCount = 0;
		private int playerKilled = 0;

		private long lastTick;
		private long emptyTicks;
		private boolean isDead;

		private String id;
		/*
		protected SpawnPoint(World world, Coordinate c) {
			this(new WorldLocation(world, c));
		}
		 */
		protected SpawnPoint(WorldLocation loc) {
			location = loc;
		}

		protected WorldLocation getLocation() {
			return location;
		}

		protected int getDimension() {
			return this.getLocation().dimensionID;
		}

		public void setSpawnParameters(Class<? extends EntityLiving> c, int n, double r) {
			mobClass = c;
			mobType = (String)EntityList.classToStringMapping.get(c);
			numberToSpawn = n;
			activationRadius = r;
		}

		@Override
		public final String toString() {
			return this.getClass()+" @ "+this.getLocation().toString()+this.getInfoString()+" ["+mobType+" x"+numberToSpawn+"]";
		}

		protected String getInfoString() {
			return "";
		}

		@Override
		public final int hashCode() {
			return this.getLocation().hashCode();
		}

		@Override
		public final boolean equals(Object o) {
			return o instanceof SpawnPoint && this.getLocation().equals(((SpawnPoint)o).getLocation());
		}

		public final boolean isDead() {
			return isDead;
		}

		public void writeToTag(NBTTagCompound ret) {
			if (location != null)
				location.writeToNBT("loc", ret);
			ret.setDouble("radius", activationRadius);
			ret.setInteger("count", numberToSpawn);
			ret.setInteger("exists", existingCount);
			ret.setInteger("killed", playerKilled);
			ret.setInteger("timeout", emptyTimeout);
			ret.setLong("tick", lastTick);
			ret.setLong("empty", emptyTicks);
			ret.setBoolean("dead", isDead);
			ret.setString("mob", mobType);
			ret.setString("type", mobClass.getName());
		}

		public void readFromTag(NBTTagCompound NBT) {
			activationRadius = NBT.getDouble("radius");
			numberToSpawn = NBT.getInteger("count");
			existingCount = NBT.getInteger("exists");
			playerKilled = NBT.getInteger("killed");
			emptyTimeout = NBT.getInteger("timeout");
			lastTick = NBT.getLong("tick");
			emptyTicks = NBT.getLong("empty");
			isDead = NBT.getBoolean("dead");
			mobType = NBT.getString("mob");
			try {
				mobClass = (Class<? extends EntityLiving>)Class.forName(NBT.getString("type"));
			}
			catch (ClassNotFoundException e) {
				e.printStackTrace();
				mobClass = (Class<? extends EntityLiving>)EntityList.stringToClassMapping.get(mobType);
			}
		}

		protected void tick(World world, EntityPlayer ep) {
			if (isDead)
				return;
			long time = world.getTotalWorldTime();
			if (time == lastTick)
				return;
			lastTick = time;
			WorldLocation loc = this.getLocation();
			if (loc == null)
				return;
			if (playerKilled >= numberToSpawn && !this.isClearingPermanent() && this.isEmptyTimeoutActive(world)) {
				emptyTicks++;
				if (emptyTicks >= emptyTimeout) {
					playerKilled = 0;
					emptyTicks = 0;
				}
			}
			if (mobClass == null)
				return;
			if (EntityMob.class.isAssignableFrom(mobClass) && world.difficultySetting == EnumDifficulty.PEACEFUL)
				return;
			if (ep == null)
				ep = world.getClosestPlayer(loc.xCoord+0.5, loc.yCoord+0.5, loc.zCoord+0.5, activationRadius);
			if (ep == null)
				return;
			int amt = numberToSpawn-existingCount-playerKilled;
			if (amt > 0 && ep.getDistanceSq(loc.xCoord+0.5, loc.yCoord+0.5, loc.zCoord+0.5) <= activationRadius*activationRadius) {
				int last = existingCount;
				for (int i = 0; i < amt; i++) {
					if (this.attemptSpawn(world, loc)) {
						existingCount++;
					}
				}
				if (existingCount != last && location != null)
					BiomewideFeatureGenerator.instance.save(world);
			}
		}

		protected boolean isEmptyTimeoutActive(World world) {
			return false;
		}

		public final void setEmptyTimeout(int ticks) {
			emptyTimeout = ticks;
		}

		private final void removeEntity(EntityLiving e) {
			//ReikaJavaLibrary.pConsole("Removed "+e+" from "+this);
			existingCount--;
			if (this.canBeCleared()) {
				if (hasTag(e, KILLED_NBT_TAG))
					playerKilled++;
				if (playerKilled >= numberToSpawn && this.isClearingPermanent())
					this.delete();
			}
			if (location != null)
				BiomewideFeatureGenerator.instance.save(e.worldObj);
		}

		protected boolean canBeCleared() {
			return true;
		}

		protected boolean isClearingPermanent() {
			return emptyTimeout < 0;
		}

		protected void delete() {
			instance.removeSpawner(this);
		}

		protected final EntityLiving getRandomPlacedEntity(double r, World world, int cx, int cy, int cz) {
			EntityLiving e = this.constructEntity(world);
			if (e != null) {
				double minX = cx+0.5-r;
				double maxX = cx+0.5+r;
				double minZ = cz+0.5-r;
				double maxZ = cz+0.5+r;
				double x = ReikaRandomHelper.getRandomBetween(minX, maxX);
				double z = ReikaRandomHelper.getRandomBetween(minZ, maxZ);
				e.setLocationAndAngles(x, cy+1+world.rand.nextDouble()*1.5, z, 0, 0);
			}
			return e;
		}

		protected final EntityLiving constructEntity(World world) {
			return (EntityLiving)EntityList.createEntityByName(mobType, world);
		}

		protected abstract EntityLiving getSpawn(World world, int cx, int cy, int cz);

		public final Class<? extends EntityLiving> getSpawnType() {
			return mobClass;
		}

		private boolean attemptSpawn(World world, WorldLocation loc) {
			EntityLiving e = this.getSpawn(world, loc.xCoord, loc.yCoord, loc.zCoord);
			if (e != null) {
				int i = 0;
				while (!e.getCanSpawnHere() && i < 5) {
					e.setLocationAndAngles(e.posX, e.posY+0.5, e.posZ, e.rotationYaw, e.rotationPitch);
					i++;
				}
				if (e.getCanSpawnHere()) {
					e.rotationYaw = world.rand.nextFloat()*360;
					this.setSpawnCallback(e, loc);
					//e.onSpawnWithEgg((IEntityLivingData)null); no jockeys or potions
					world.spawnEntityInWorld(e);
					e.spawnExplosionParticle();
					//this.worldObj.playAuxSFX(2004, xCoord, yCoord, zCoord, 0);
					if (this.denyPassivation()) {
						setTag(e, HOSTILE_NBT_TAG, true);
					}
					//ReikaJavaLibrary.pConsole("Spawned "+e+" @ "+this+", has "+existingCount+"/"+numberToSpawn);
					this.onEntitySpawned(e);
					return true;
				}
			}
			return false;
		}

		protected boolean denyPassivation() {
			return false;
		}

		protected void onEntitySpawned(EntityLiving e) {

		}

		private void setSpawnCallback(EntityLiving e, WorldLocation loc) {
			if (e instanceof SpawnPointEntity) {
				((SpawnPointEntity)e).setSpawn(loc);
			}
			else {
				NBTTagCompound tag = new NBTTagCompound();
				loc.writeToNBT("location", tag);
				e.getEntityData().setTag(SPAWN_NBT_TAG, tag);
			}
		}

	}

	public static interface SpawnPointDefinition {

		public SpawnPoint construct(WorldLocation loc);
		public String getID();

	}

}
