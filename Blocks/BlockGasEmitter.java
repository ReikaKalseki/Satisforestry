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
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import Reika.ChromatiCraft.Registry.ChromaIcons;
import Reika.ChromatiCraft.Render.Particle.EntityFloatingSeedsFX;
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
		return world.getBlockMetadata(x, y, z) == 1 ? 4 : -1;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileGasVent(4, meta == 0 ? 0 : -4);
	}

	@Override
	public IIcon getIcon(IBlockAccess iba, int x, int y, int z, int s) {
		return s == 1 ? blockIcon : SFBlocks.CAVESHIELD.getBlockInstance().getIcon(iba, x, y, z, s);
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

	public class TileGasVent extends TileEntity {

		public int activeRadius = 4;
		public int yOffset = 0;

		private AxisAlignedBB activeArea;

		public TileGasVent() {
			this(4, 0);
		}

		public TileGasVent(int r, int y) {
			activeRadius = r;
			yOffset = y;
		}

		@Override
		public void updateEntity() {
			activeArea = ReikaAABBHelper.getBlockAABB(this).expand(activeRadius, 0, activeRadius).addCoord(0, 4, 0).offset(0, yOffset, 0);
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
			double px = ReikaRandomHelper.getRandomBetween(activeArea.minX, activeArea.maxX);
			double py = ReikaRandomHelper.getRandomBetween(activeArea.minY, activeArea.maxY);
			double pz = ReikaRandomHelper.getRandomBetween(activeArea.minZ, activeArea.maxZ);
			int c = 0xBAFF21;//0xC9FF21;
			EntityFloatingSeedsFX fx = new EntityFloatingSeedsFX(worldObj, px, py, pz, 0, 90);
			fx.setRapidExpand().setAlphaFading();
			fx.angleVelocity *= 1.85;
			fx.particleVelocity *= 0.3;
			fx.freedom *= 1.5;
			fx.setIcon(ChromaIcons.FADE);
			fx.setColor(c);
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);

			EntityFloatingSeedsFX fx2 = new EntityFloatingSeedsFX(worldObj, px, py, pz, 0, 90);
			fx2.setRapidExpand().setAlphaFading();
			fx2.setIcon(ChromaIcons.FADE_GENTLE);
			fx2.setScale((float)ReikaRandomHelper.getRandomBetween(12D, 25D));
			fx2.setColor(ReikaColorAPI.getColorWithBrightnessMultiplier(c, 0.25F));
			fx2.lockTo(fx);
			Minecraft.getMinecraft().effectRenderer.addEffect(fx2);
		}

		@Override
		public void writeToNBT(NBTTagCompound NBT) {
			super.writeToNBT(NBT);

			NBT.setInteger("offset", yOffset);
			NBT.setInteger("radius", activeRadius);
		}

		@Override
		public void readFromNBT(NBTTagCompound NBT) {
			super.readFromNBT(NBT);

			activeRadius = NBT.getInteger("radius");
			yOffset = NBT.getInteger("offset");
		}

	}

}
