package Reika.Satisforestry.API;

import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import Reika.Satisforestry.API.SFAPI.AltRecipeHandler;

/** Implemented by alternate recipes. This extends {@link IRecipe} and acts as a read-only view onto the internal recipe. */
public interface AltRecipe extends IRecipe {

	public String getID();

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

}
