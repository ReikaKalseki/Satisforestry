package Reika.Satisforestry.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.ChromatiCraft.API.Interfaces.CrystalEffectBoostArmor;
import Reika.DragonAPI.Instantiable.MetadataItemBlock;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.Satisforestry.UpgradeHandler;
import Reika.Satisforestry.Blocks.BlockPowerSlug.TilePowerSlug;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class ItemBlockPowerSlug extends MetadataItemBlock implements CrystalEffectBoostArmor {

	private static final UUID moveSpeedBonusID = UUID.fromString("31bbbe31-4838-44ca-b6a7-2777dcc87375");
	private static final UUID unknownBonusID = UUID.fromString("05e61bc2-1be8-415a-9045-f8b6f25bdbc4");
	private static final UUID unknownBonusID2 = UUID.fromString("622a4a6a-31f4-448d-92bc-fc96c003828d");

	public ItemBlockPowerSlug(Block b) {
		super(b);
	}

	@Override
	public int getMetadata(int meta) {
		return meta;//%3+3;
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
		boolean flag = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
		if (flag) {
			TilePowerSlug te = (TilePowerSlug)world.getTileEntity(x, y, z);
			te.setNoSpawns();
			te.setDirection(ForgeDirection.VALID_DIRECTIONS[side].getOpposite());
			te.angle = world.rand.nextFloat()*360;
		}
		return flag;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack is, EntityPlayer ep, List li, boolean vb) {
		ArrayList<ItemStack> li2 = UpgradeHandler.instance.getSlugEquivalents(is.getItemDamage()%3);
		if (!li2.isEmpty()) {
			li.add("Usable as upgrades:");
			for (ItemStack is2 : li2) {
				li.add("  "+is2.getDisplayName()+" ("+ReikaItemHelper.getRegistrantMod(is2)+")");
			}
		}
	}

	@Override
	public boolean isValidArmor(ItemStack stack, int type, Entity e) {
		return type == 0 && e instanceof EntityPlayer;
	}

	@Override
	public void onArmorTick(World world, EntityPlayer ep, ItemStack is) {
		//int boost = is.getItemDamage()%3;
		//int dur = 20*ReikaMathLibrary.intpow2(10, boost)*(1+boost); //1s, 20s, 2.5min
		//this.addPotion(ep, Potion.moveSpeed, boost, dur);
		//this.addPotion(ep, Potion.damageBoost, boost, dur);
		//this.addPotion(ep, Potion.digSpeed, boost, dur);
		addBonuses(ep, is);
	}

	private void addPotion(EntityPlayer ep, Potion p, int boost, int dur) {
		PotionEffect pot = ep.getActivePotionEffect(p);
		if (pot == null || pot.getDuration() < dur || pot.getAmplifier() < boost) {
			ep.addPotionEffect(new PotionEffect(p.id, dur, boost, true));
		}
	}

	@Override
	public float getPower(ItemStack is) {
		return is.getItemDamage()%3+1;
	}

	public static void addBonuses(EntityPlayer ep, ItemStack is) {
		IAttributeInstance speed = ep.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
		AttributeModifier am = speed.getModifier(moveSpeedBonusID);
		int tier = is.getItemDamage()%3+1;
		if (am == null) {
			am = new AttributeModifier(moveSpeedBonusID, "slugMoveSpeed", 0.1667F, 2);
		}
		speed.removeModifier(am);
		speed.applyModifier(am);
	}
	/*
	    for (iterator1 = this.getModifiersByOperation(0).iterator(); iterator1.hasNext(); d0 += attributemodifier.getAmount())
        for (iterator1 = this.getModifiersByOperation(1).iterator(); iterator1.hasNext(); d1 += d0 * attributemodifier1.getAmount())
        for (iterator1 = this.getModifiersByOperation(2).iterator(); iterator1.hasNext(); d1 *= 1.0D + attributemodifier1.getAmount())
	 */

	public static void removeBonuses(EntityPlayer ep) {
		removeModifier(ep, SharedMonsterAttributes.movementSpeed, moveSpeedBonusID);
	}

	private static void removeModifier(EntityPlayer ep, IAttribute attr, UUID id) {
		IAttributeInstance ia = ep.getEntityAttribute(attr);
		AttributeModifier am = ia.getModifier(id);
		ia.removeModifier(am);
	}

}
