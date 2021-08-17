package Reika.Satisforestry.Blocks;

import java.util.List;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Libraries.ReikaAABBHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Registry.SFBlocks;
import Reika.Satisforestry.Registry.SFOptions;

public class BlockCaveSpawner extends BlockContainer {

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

	public static class TileCaveSpawner extends TileEntity {

		public int activeRadius = 6;
		public int spawnRadius = 6;
		public int mobLimit = 6;
		public int respawnTime = 0;

		private boolean hasSpawned;

		private String mobType;
		private Class mobClass;

		private AxisAlignedBB activeArea;
		private AxisAlignedBB checkArea;

		public TileCaveSpawner() {
			this.setMobType(EntitySpider.class);
		}

		public void setMobType(Class<? extends EntityMob> c) {
			mobClass = c;
			mobType = c == null ? null : (String)EntityList.classToStringMapping.get(c);
			this.markDirty();
		}

		@Override
		public void updateEntity() {
			if (worldObj.difficultySetting == EnumDifficulty.PEACEFUL)
				return;
			if (mobType == null)
				return;
			activeArea = ReikaAABBHelper.getBlockAABB(this).expand(activeRadius, 1, activeRadius).addCoord(0, 3, 0);
			checkArea = activeArea.expand(activeRadius*1.5+1, 2, activeRadius*1.5+1);
			boolean player = false;
			for (EntityPlayer ep : (List<EntityPlayer>)worldObj.playerEntities) {
				if (ep.boundingBox.intersectsWith(activeArea)) {
					player = true;
					break;
				}
			}
			if (!worldObj.isRemote && player && (!hasSpawned || worldObj.rand.nextInt(5+2*respawnTime*respawnTime/SFOptions.CAVEMOBS.getValue()) == 0)) {
				List<EntityMob> li = worldObj.getEntitiesWithinAABB(mobClass, checkArea);
				//ReikaJavaLibrary.pConsole(li);
				int lim = MathHelper.ceiling_double_int(mobLimit*SFOptions.CAVEMOBS.getValue());
				if (li.size() < lim) {
					int add = hasSpawned ? lim-li.size() : 1;
					hasSpawned = this.trySpawnMobs(add) >= Math.max(1, add/2);
					//ReikaJavaLibrary.pConsole("Spawn @ "+xCoord+", "+yCoord+", "+zCoord);
				}
			}
		}

		private int trySpawnMobs(int n) {
			int ret = 0;
			int i = 0;
			while (ret < n && i < n*3) {
				i++;
				double x = ReikaRandomHelper.getRandomBetween(activeArea.minX, activeArea.maxX);
				double z = ReikaRandomHelper.getRandomBetween(activeArea.minZ, activeArea.maxZ);
				EntityMob e = (EntityMob)EntityList.createEntityByName(mobType, worldObj);
				for (double y = yCoord; y <= activeArea.maxY; y += 0.5) {
					if (new Coordinate(x, y, z).isEmpty(worldObj)) {
						e.setLocationAndAngles(x, y, z, 0, 0);
						if (e.getCanSpawnHere()) {
							e.rotationYaw = worldObj.rand.nextFloat()*360;
							//e.onSpawnWithEgg((IEntityLivingData)null); no jockeys or potions
							this.onSpawnEntity(e);
							worldObj.spawnEntityInWorld(e);

							//this.worldObj.playAuxSFX(2004, xCoord, yCoord, zCoord, 0);

							e.spawnExplosionParticle();

							ret++;
							break;
						}
					}
				}
			}
			return ret;
		}

		protected void onSpawnEntity(EntityMob e) {
			e.getEntityData().setBoolean("pinkforestspawn", true);
		}

		@Override
		public void writeToNBT(NBTTagCompound NBT) {
			super.writeToNBT(NBT);

			NBT.setInteger("limit", mobLimit);
			NBT.setInteger("delay", respawnTime);
			NBT.setInteger("activeRadius", activeRadius);
			NBT.setInteger("spawnRadius", spawnRadius);

			if (this.preserveSpawnedFlag())
				NBT.setBoolean("spawned", hasSpawned);

			if (mobType != null) {
				NBT.setString("mob", mobType);
				NBT.setString("type", mobClass.getName());
			}
		}

		@Override
		public void readFromNBT(NBTTagCompound NBT) {
			super.readFromNBT(NBT);

			mobLimit = NBT.getInteger("limit");
			respawnTime = NBT.getInteger("delay");
			activeRadius = NBT.getInteger("activeRadius");
			spawnRadius = NBT.getInteger("spawnRadius");

			if (this.preserveSpawnedFlag())
				hasSpawned = NBT.getBoolean("spawned");

			mobType = NBT.hasKey("mob") ? NBT.getString("mob") : null;
			if (mobType != null) {
				try {
					mobClass = Class.forName(NBT.getString("type"));
				}
				catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		protected boolean preserveSpawnedFlag() {
			return true;
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
