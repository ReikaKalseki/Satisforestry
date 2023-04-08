package Reika.Satisforestry.API;

import java.util.UUID;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import Reika.Satisforestry.API.SFAPI.AltRecipeHandler;

/** Implemented by alternate recipes. This extends {@link IRecipe} and acts as a read-only view onto the internal recipe. */
public interface AltRecipe extends IRecipe {

	public String getID();
	public String getDisplayName();

	/** Whether the recipe has been unlocked by a given player. */
	public boolean playerHas(World world, UUID ep);

	/** The required unlock item, if any. */
	public ItemStack getRequiredItem();

	/** A description of the required unlock power, if any. Will be <b>null</b> and not empty string if none. */
	public String getRequiredPowerDesc();

	/** A recipe helper function, useful in NEI. */
	public boolean usesItem(ItemStack ingredient);

	/** Whether this recipe is craftable as a normal crafting recipe. False for flag-only recipes (see {@link AltRecipeHandler.addAltRecipe} and {@link UncraftableAltRecipe}) */
	public boolean isCraftable();

	public static interface UncraftableAltRecipe extends IRecipe {

	}

	/** A subclass of UncraftableAltRecipe which can still use the native NEI handling. */
	public static abstract class UncraftableAltRecipeWithNEI implements UncraftableAltRecipe {

		/** A standard 3x3 grid. Supply nulls for empty slots. Items may be Item, Block, ItemStack, ItemStack[], or List of ItemStack. */
		public abstract Object[] getDisplayInputs();
		public abstract boolean usesItem(ItemStack is);
		public abstract boolean crafts(ItemStack is);
		/** A brief description of the recipe, eg which machine it is crafted in. */
		public abstract String getDescription();

		@Override
		public final boolean matches(InventoryCrafting is, World world) {
			return false;
		}
		@Override
		public final ItemStack getCraftingResult(InventoryCrafting ic) {
			return this.getRecipeOutput();
		}
		@Override
		public final int getRecipeSize() {
			return this.getDisplayInputs().length;
		}

	}

	public static final UncraftableAltRecipe defaultDummyRecipe = new UncraftableAltRecipe() {
		@Override
		public boolean matches(InventoryCrafting is, World world) {
			return false;
		}
		@Override
		public ItemStack getCraftingResult(InventoryCrafting ic) {
			return this.getRecipeOutput();
		}
		@Override
		public int getRecipeSize() {
			return 0;
		}
		@Override
		public ItemStack getRecipeOutput() {
			return null;
		}
	};

}
