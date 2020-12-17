package Reika.Satisforestry.Blocks;

import java.util.List;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import Reika.DragonAPI.Libraries.ReikaAABBHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.Satisforestry.SFBlocks;
import Reika.Satisforestry.Satisforestry;

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

		private String mobType;
		private Class mobClass;

		private AxisAlignedBB activeArea;

		public TileCaveSpawner() {
			this.setMobType(EntitySpider.class);
		}

		public void setMobType(Class<? extends EntityMob> c) {
			mobClass = c;
			mobType = (String)EntityList.classToStringMapping.get(c);
		}

		@Override
		public void updateEntity() {
			activeArea = ReikaAABBHelper.getBlockAABB(this).expand(activeRadius, 0, activeRadius).addCoord(0, 4, 0);
			boolean player = false;
			for (EntityPlayer ep : (List<EntityPlayer>)worldObj.playerEntities) {
				if (ep.boundingBox.intersectsWith(activeArea)) {
					player = true;
					break;
				}
			}
			if (!worldObj.isRemote && worldObj.rand.nextInt(5+respawnTime*respawnTime) == 0) {
				if (player) {
					List<EntityMob> li = worldObj.getEntitiesWithinAABB(mobClass, activeArea);
					//ReikaJavaLibrary.pConsole(li);
					if (li.size() < mobLimit) {
						this.trySpawnMob();
						//ReikaJavaLibrary.pConsole("Spawn @ "+xCoord+", "+yCoord+", "+zCoord);
					}
				}
			}
		}

		private boolean trySpawnMob() {
			double x = ReikaRandomHelper.getRandomBetween(activeArea.minX, activeArea.maxX);
			double z = ReikaRandomHelper.getRandomBetween(activeArea.minZ, activeArea.maxZ);
			EntityMob e = (EntityMob)EntityList.createEntityByName(mobType, worldObj);
			for (double y = yCoord; y <= activeArea.maxY; y += 0.5) {
				e.setLocationAndAngles(x, y, z, 0, 0);
				if (e.getCanSpawnHere()) {
					e.rotationYaw = worldObj.rand.nextFloat()*360;
					e.onSpawnWithEgg((IEntityLivingData)null);
					worldObj.spawnEntityInWorld(e);

					//this.worldObj.playAuxSFX(2004, xCoord, yCoord, zCoord, 0);

					e.spawnExplosionParticle();

					return true;
				}
			}
			return false;
		}

		@Override
		public void writeToNBT(NBTTagCompound NBT) {
			super.writeToNBT(NBT);

			NBT.setInteger("limit", mobLimit);
			NBT.setInteger("delay", respawnTime);
			NBT.setInteger("activeRadius", activeRadius);
			NBT.setInteger("spawnRadius", spawnRadius);

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

			mobType = NBT.getString("mob");
			try {
				mobClass = Class.forName(NBT.getString("type"));
			}
			catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

	}

}
