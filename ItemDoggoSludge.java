package Reika.Satisforestry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class ItemDoggoSludge extends ItemFood {

	public ItemDoggoSludge() {
		super(1, 0, false);
		this.setMaxStackSize(8);
		this.setAlwaysEdible();
		this.setCreativeTab(Satisforestry.tabCreative);
	}

	@Override
	protected void onFoodEaten(ItemStack is, World world, EntityPlayer ep) {
		ep.addPotionEffect(new PotionEffect(Potion.hunger.id, 200, 2));
		ep.addPotionEffect(new PotionEffect(Potion.confusion.id, 100, 2));
	}

	@Override
	public void onUpdate(ItemStack is, World world, Entity e, int slot, boolean held) {
		if (e instanceof EntityLivingBase)
			((EntityLivingBase)e).addPotionEffect(new PotionEffect(Potion.confusion.id, 20, held ? 1 : 0));
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected String getIconString() {
		return "satisforestry:sludge2";
	}

}
