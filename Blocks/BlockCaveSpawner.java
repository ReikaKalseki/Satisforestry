package Reika.Satisforestry.Blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.Immutable.WorldLocation;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.Biomewide.PointSpawnSystem;
import Reika.Satisforestry.Biome.Biomewide.PointSpawnSystem.SpawnPoint;
import Reika.Satisforestry.Registry.SFBlocks;

public class BlockCaveSpawner extends BlockContainer implements PointSpawnBlock {

	public BlockCaveSpawner(Material mat) {
		super(mat);
		this.setCreativeTab(Satisforestry.tabCreative);
		this.setResistance(60000);
		this.setBlockUnbreakable();
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileCaveSpawner();
	}

	@Override
	public IIcon getIcon(IBlockAccess iba, int x, int y, int z, int s) {
		return s == 1 ? blockIcon : SFBlocks.CAVESHIELD.getBlockInstance().getIcon(iba, x, y, z, s);
	}

	@Override
	public void registerBlockIcons(IIconRegister ico) {
		blockIcon = ico.registerIcon("satisforestry:cavespawner");
	}

	private static class TileSpawnPoint extends SpawnPoint {

		private TileCaveSpawner tile;

		protected TileSpawnPoint(TileCaveSpawner t) {
			super(null);
			tile = t;
		}

		@Override
		protected WorldLocation getLocation() {
			return new WorldLocation(tile);
		}

		@Override
		protected int getDimension() {
			return tile.worldObj.provider.dimensionId;
		}

		private void tick(World world) {
			this.tick(world, null);
		}

		@Override
		protected EntityLiving getSpawn(World world, int cx, int cy, int cz) {
			double x = ReikaRandomHelper.getRandomBetween(cx+0.5-tile.spawnRadius, cx+0.5+tile.spawnRadius);
			double z = ReikaRandomHelper.getRandomBetween(cz+0.5-tile.spawnRadius, cz+0.5+tile.spawnRadius);
			EntityMob e = (EntityMob)this.constructEntity(world);
			e.setLocationAndAngles(x, cy, z, 0, 0);
			return e;
		}

		@Override
		protected void onEntitySpawned(EntityLiving e) {
			tile.onSpawnEntity((EntityMob)e);
		}

		@Override
		protected void delete() {

		}

		@Override
		protected boolean isEmptyTimeoutActive(World world) {
			return tile.isEmptyTimeoutActive(world);
		}

		@Override
		protected boolean denyPassivation() {
			return tile.denyPassivation();
		}

		static {
			PointSpawnSystem.registerSpawnerType("tile", TileSpawnPoint.class);
		}

	}

	public static class TileCaveSpawner extends TileEntity implements PointSpawnTile {

		private TileSpawnPoint spawner;

		private double spawnRadius = 6;

		public TileCaveSpawner() {
			this.setSpawnParameters(EntitySpider.class, 6, 6, 6);
		}

		public final SpawnPoint getSpawner() {
			return spawner;
		}

		public final void setSpawnParameters(int n, double ar, double sr) {
			this.setSpawnParameters(this.getSpawnType(), n, ar, sr);
		}

		public final void setSpawnParameters(Class<? extends EntityMob> c, int n, double ar, double sr) {
			if (spawner == null && c != null && n > 0) {
				this.createSpawner();
			}
			spawnRadius = sr;
			if (c == null || n <= 0)
				spawner = null;
			else
				spawner.setSpawnParameters(c, n, ar);
			this.markDirty();
		}

		public final void setInertTimeout(int ticks) {
			if (spawner != null) {
				spawner.setEmptyTimeout(ticks);
				this.markDirty();
			}
		}

		protected boolean isEmptyTimeoutActive(World world) {
			return true;
		}

		private void createSpawner() {
			spawner = new TileSpawnPoint(this);
		}

		private final Class<? extends EntityMob> getSpawnType() {
			return spawner != null ? (Class<? extends EntityMob>)spawner.getSpawnType() : null;
		}

		protected boolean denyPassivation() {
			return false;
		}

		@Override
		public void updateEntity() {
			if (worldObj.difficultySetting == EnumDifficulty.PEACEFUL)
				return;
			if (spawner == null)
				return;
			spawner.tick(worldObj);
		}

		protected void onSpawnEntity(EntityMob e) {
			e.getEntityData().setBoolean("pinkforestspawned", true);
		}

		@Override
		public void writeToNBT(NBTTagCompound NBT) {
			super.writeToNBT(NBT);

			if (spawner != null) {
				NBTTagCompound tag = new NBTTagCompound();
				spawner.writeToTag(tag);
				NBT.setTag("spawner", tag);
			}

			NBT.setDouble("spawnRadius", spawnRadius);
		}

		@Override
		public void readFromNBT(NBTTagCompound NBT) {
			super.readFromNBT(NBT);

			if (NBT.hasKey("spawner")) {
				NBTTagCompound tag = NBT.getCompoundTag("spawner");
				TileSpawnPoint pt = new TileSpawnPoint(this);
				pt.readFromTag(tag);
			}

			spawnRadius = NBT.getDouble("spawnRadius");
		}

		@Override
		public final Packet getDescriptionPacket() {
			NBTTagCompound NBT = new NBTTagCompound();
			this.writeToNBT(NBT);
			S35PacketUpdateTileEntity pack = new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, NBT);
			return pack;
		}

		@Override
		public final void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity p)  {
			this.readFromNBT(p.field_148860_e);
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}

	}

}
