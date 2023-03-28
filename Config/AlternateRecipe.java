package Reika.Satisforestry.Config;

import java.util.Locale;

import com.google.common.base.Strings;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Instantiable.IO.CustomRecipeList;
import Reika.DragonAPI.Instantiable.IO.LuaBlock;
import Reika.DragonAPI.Libraries.MathSci.ReikaTimeHelper;
import Reika.DragonAPI.ModRegistry.PowerTypes;
import Reika.RotaryCraft.Auxiliary.RotaryAux;

public class AlternateRecipe {

	public final String id;
	public String displayName;
	public final double spawnWeight;
	private final IRecipe recipe;

	public final PowerRequirement unlockPower;
	private final ItemStack unlockItem;

	public AlternateRecipe(String id, double wt, IRecipe recipe, ItemStack needItem, PowerRequirement power) {
		this.id = id;
		spawnWeight = wt;
		this.recipe = recipe;
		unlockItem = needItem;
		unlockPower = power;
	}

	public AlternateRecipe(String id, double wt, LuaBlock recipe, LuaBlock needItem, LuaBlock power) {
		this.id = id;
		spawnWeight = wt;

		LuaBlock output = recipe.getChild("output");
		if (output == null)
			throw new IllegalArgumentException("No recipe specified!");
		this.recipe = CustomRecipeList.parseCraftingRecipe(recipe, CustomRecipeList.parseItemString(output.getString("item"), output.getChild("nbt"), false));

		if (power != null) {
			PowerTypes type = PowerTypes.valueOf(power.getString("format").toUpperCase(Locale.ENGLISH));
			if (type != PowerTypes.RF && type != PowerTypes.EU && type != PowerTypes.ROTARYCRAFT)
				throw new IllegalArgumentException("Unsupported power type "+type);
			long amt = power.getLong("amount");
			long time = power.getLong("time");
			ReikaTimeHelper unit = power.containsKey("timeUnit") ? ReikaTimeHelper.valueOf(power.getString("timeUnit").toUpperCase(Locale.ENGLISH)) : ReikaTimeHelper.SECOND;
			unlockPower = new PowerRequirement(type, amt, time*unit.getDuration());
		}
		else {
			unlockPower = null;
		}

		unlockItem = needItem == null ? null : CustomRecipeList.parseItemString(needItem.getString("item"), needItem.getChild("nbt"), true);
	}

	public ItemStack getRequiredItem() {
		return unlockItem != null ? unlockItem.copy() : null;
	}

	public boolean matchesItem(ItemStack is) {
		return unlockItem == null || (is != null && ItemStack.areItemStacksEqual(is, unlockItem));
	}

	public boolean matchesRecipe(InventoryCrafting con, World world) {
		return recipe.matches(con, world);
	}

	public String getDisplayName() {
		return Strings.isNullOrEmpty(displayName) ? recipe.getRecipeOutput().getDisplayName() : displayName;
	}

	public void giveToPlayer(EntityPlayer ep) {
		//TODO
	}

	public boolean playerHas(EntityPlayer ep) {
		//TODO
		return false;
	}

	@Override
	public String toString() {
		return id+" = "+recipe.getRecipeOutput();
	}

	public ItemStack getOutput() {
		return recipe.getRecipeOutput().copy();
	}

	public static class PowerRequirement {

		public final PowerTypes type;
		public final long amount;
		public final long ticksToHold;

		private PowerRequirement(PowerTypes p, long amt, long ticks) {
			type = p;
			amount = amt;
			ticksToHold = ticks;
		}

		public String getDisplayString() {
			switch(type) {
				case RF:
				case EU:
					return amount+" "+type.getDisplayName()+"/t for "+ticksToHold/20+"s";
				case ROTARYCRAFT:
					return this.formatRCPower(amount)+" for "+ticksToHold/20+"s";
				default:
					return "";
			}
		}

		@ModDependent(ModList.ROTARYCRAFT)
		private String formatRCPower(long pwr) {
			return RotaryAux.formatPower(pwr);
		}

	}

}
