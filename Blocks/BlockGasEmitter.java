package Reika.Satisforestry.Blocks;

import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.Extras.IconPrefabs;
import Reika.DragonAPI.Instantiable.Effects.EntityFloatingSeedsFX;
import Reika.DragonAPI.Libraries.ReikaAABBHelper;
import Reika.DragonAPI.Libraries.IO.ReikaColorAPI;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.Satisforestry.SFBlocks;
import Reika.Satisforestry.Satisforestry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockGasEmitter extends BlockContainer {

	public BlockGasEmitter(Material mat) {
		super(mat);
		this.setCreativeTab(Satisforestry.tabCreative);
		this.setResistance(60000);
	}

	@Override
	public float getBlockHardness(World world, int x, int y, int z) {
		return world.getBlockMetadata(x, y, z) == 0 ? 4 : -1;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileGasVent();
	}

	@Override
	public IIcon getIcon(IBlockAccess iba, int x, int y, int z, int s) {
		if (s == 1)
			return blockIcon;
		SFBlocks sf = iba.getBlockMetadata(x, y, z) == 0 ? SFBlocks.TERRAIN : SFBlocks.CAVESHIELD;
		int meta = 0;
		return sf.getBlockInstance().getIcon(s, meta);
	}

	@Override
	public void registerBlockIcons(IIconRegister ico) {
		blockIcon = ico.registerIcon("satisforestry:gasvent");
	}

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return Item.getItemFromBlock(Blocks.stone);
	}

	@Override
	public boolean canSilkHarvest(World world, EntityPlayer player, int x, int y, int z, int metadata) {
		return true;
	}

	public static class TileGasVent extends TileEntity {

		public int activeRadius = 4;
		public int activeHeight = 4;
		public int yOffset = 0;

		private AxisAlignedBB activeArea;

		@Override
		public void updateEntity() {
			activeArea = ReikaAABBHelper.getBlockAABB(this).expand(activeRadius, 0, activeRadius).addCoord(0, activeHeight, 0).offset(0, yOffset, 0);
			if (worldObj.isRemote) {
				this.doFX();
			}
			else {
				List<EntityPlayer> li = worldObj.getEntitiesWithinAABB(EntityPlayer.class, activeArea);
				for (EntityPlayer e : li) {
					e.addPotionEffect(new PotionEffect(Potion.poison.id, 40, 1));
				}
			}
		}

		@SideOnly(Side.CLIENT)
		private void doFX() {
			EntityPlayer ep = Minecraft.getMinecraft().thePlayer;
			double dd = Math.sqrt(this.getDistanceFrom(ep.posX, ep.posY, ep.posZ));
			int n = 1;
			n += Math.max(0, (dd-32)/16D);
			if (dd <= 256 && DragonAPICore.rand.nextInt(n) == 0) {
				double px = ReikaRandomHelper.getRandomBetween(activeArea.minX, activeArea.maxX);
				double py = ReikaRandomHelper.getRandomBetween(activeArea.minY, activeArea.maxY);
				double pz = ReikaRandomHelper.getRandomBetween(activeArea.minZ, activeArea.maxZ);
				int c = 0xBAFF21;//0xC9FF21;
				EntityFloatingSeedsFX fx = new EntityFloatingSeedsFX(worldObj, px, py, pz, 0, 90, IconPrefabs.FADE.getIcon());
				fx.setRapidExpand().setAlphaFading().forceIgnoreLimits();
				fx.angleVelocity *= 1.85;
				fx.particleVelocity *= 0.3;
				fx.freedom *= 1.5;
				fx.setColliding();
				fx.setColor(c);
				Minecraft.getMinecraft().effectRenderer.addEffect(fx);

				EntityFloatingSeedsFX fx2 = new EntityFloatingSeedsFX(worldObj, px, py, pz, 0, 90, IconPrefabs.FADE_GENTLE.getIcon());
				fx2.setRapidExpand().setAlphaFading().forceIgnoreLimits();
				fx2.setScale((float)ReikaRandomHelper.getRandomBetween(12D, 25D));
				fx2.setColor(ReikaColorAPI.getColorWithBrightnessMultiplier(c, 0.25F));
				fx2.setColliding();
				fx2.lockTo(fx);
				Minecraft.getMinecraft().effectRenderer.addEffect(fx2);
			}
		}

		@Override
		public void writeToNBT(NBTTagCompound NBT) {
			super.writeToNBT(NBT);

			NBT.setInteger("offset", yOffset);
			NBT.setInteger("radius", activeRadius);
			NBT.setInteger("height", activeHeight);
		}

		@Override
		public void readFromNBT(NBTTagCompound NBT) {
			super.readFromNBT(NBT);

			activeRadius = NBT.getInteger("radius");
			activeHeight = NBT.getInteger("height");
			yOffset = NBT.getInteger("offset");
		}

		@Override
		public Packet getDescriptionPacket() {
			NBTTagCompound NBT = new NBTTagCompound();
			this.writeToNBT(NBT);
			S35PacketUpdateTileEntity pack = new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, NBT);
			return pack;
		}

		@Override
		public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity p)  {
			this.readFromNBT(p.field_148860_e);
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}

	}

}
