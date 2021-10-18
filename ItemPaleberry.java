package Reika.Satisforestry;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class ItemPaleberry extends ItemFood {

	private IIcon drupelet;

	public ItemPaleberry() {
		super(4, 1F, false);
		this.setMaxStackSize(32);
		this.setAlwaysEdible();
		this.setCreativeTab(Satisforestry.tabCreative);
	}

	@Override
	protected void onFoodEaten(ItemStack is, World world, EntityPlayer ep) {
		if (is.getItemDamage() == 0)
			ep.heal(2); //heal one heart
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item i, CreativeTabs cr, List li) {
		li.add(new ItemStack(this));
		li.add(new ItemStack(this, 1, 1));
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected String getIconString() {
		return "satisforestry:paleberry";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int dmg) {
		return dmg == 0 ? super.getIconFromDamage(dmg) : drupelet;
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return stack.getItemDamage() == 0 ? super.getItemStackLimit(stack) : 64;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ico) {
		super.registerIcons(ico);
		drupelet = ico.registerIcon("satisforestry:paleberry-drop");
	}

	@Override
	public String getUnlocalizedName(ItemStack is) {
		return super.getUnlocalizedName(is)+"."+is.getItemDamage();
	}

	@Override
	public int func_150905_g(ItemStack is) {
		return is.getItemDamage() == 0 ? super.func_150905_g(is) : 0;
	}

	@Override
	public float func_150906_h(ItemStack is) {
		return is.getItemDamage() == 0 ? super.func_150906_h(is) : 0.1F;
	}

}
