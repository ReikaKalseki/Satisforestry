package Reika.Satisforestry.Config;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.google.common.base.Strings;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Instantiable.IO.CustomRecipeList;
import Reika.DragonAPI.Instantiable.IO.LuaBlock;
import Reika.DragonAPI.Libraries.ReikaPlayerAPI;
import Reika.DragonAPI.Libraries.ReikaRecipeHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaTimeHelper;
import Reika.DragonAPI.ModRegistry.PowerTypes;
import Reika.RotaryCraft.Auxiliary.RotaryAux;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.API.AltRecipe;
import Reika.Satisforestry.AlternateRecipes.AlternateRecipeManager;

import codechicken.nei.recipe.ShapedRecipeHandler;
import codechicken.nei.recipe.ShapelessRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler.CachedRecipe;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AlternateRecipe implements AltRecipe {

	public final String id;
	public String displayName;
	public final double spawnWeight;
	private final IRecipe recipe;

	public final PowerRequirement unlockPower;
	private final ItemStack unlockItem;

	public AlternateRecipe(String id, double wt, IRecipe recipe, ItemStack needItem, String powerType, long powerAmount, long ticksFor) {
		this.id = id;
		spawnWeight = wt;
		this.recipe = recipe;
		unlockItem = needItem;
		if (Strings.isNullOrEmpty(powerType)) {
			unlockPower = null;
		}
		else {
			PowerTypes type = PowerTypes.valueOf(powerType.toUpperCase(Locale.ENGLISH));
			if (type != PowerTypes.RF && type != PowerTypes.EU && type != PowerTypes.ROTARYCRAFT)
				throw new IllegalArgumentException("Unsupported power type "+type);
			unlockPower = new PowerRequirement(type, powerAmount, ticksFor);
		}

		this.addRecipe();
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

		this.addRecipe();
	}

	private void addRecipe() {
		Satisforestry.logger.log("Registering alternate recipe "+this);
		GameRegistry.addRecipe(this);
	}

	public ItemStack getRequiredItem() {
		return unlockItem != null ? unlockItem.copy() : null;
	}

	public boolean matchesItem(ItemStack is) {
		return ItemStack.areItemStacksEqual(is, unlockItem);
	}

	public String getDisplayName() {
		return Strings.isNullOrEmpty(displayName) ? recipe.getRecipeOutput().getDisplayName() : displayName;
	}

	public boolean giveToPlayer(EntityPlayerMP ep) {
		if (ep == null || ReikaPlayerAPI.isFake(ep)) {
			Satisforestry.logger.logError("Tried to give alt recipe '"+this+"' to null or fake player???");
			return false;
		}
		if (this.playerHas(ep.worldObj, ep.getUniqueID()))
			return false;
		AlternateRecipeManager.instance.setRecipeStatus(ep, this, true);
		return true;
	}

	public boolean playerHas(World world, UUID id) {
		EntityPlayer ep = world.func_152378_a(id);
		if (ep == null && world instanceof WorldServer) {
			ep = ReikaPlayerAPI.getFakePlayerByNameAndUUID((WorldServer)world, "AltRecipe Backup", id);
		}
		return ep != null && AlternateRecipeManager.instance.getPlayerRecipeData(ep).contains(this);
	}

	@Override
	public String toString() {
		return id+" = "+recipe.getRecipeOutput();
	}

	@Override
	public ItemStack getRecipeOutput() {
		return this.isCraftable() ? recipe.getRecipeOutput().copy() : null;
	}

	@Override
	public boolean matches(InventoryCrafting ic, World world) {
		if (!this.isCraftable())
			return false;
		EntityPlayer ep = this.getPlayer(world, ic);
		if (ep == null || !this.playerHas(world, ep.getUniqueID())) {
			return false;
		}
		return recipe.matches(ic, world);
	}

	private EntityPlayer getPlayer(World world, InventoryCrafting ic) {
		if (world == null)
			return null;
		if (world.isRemote) {
			return this.getClientPlayer();
		}
		List<ICrafting> li = ic.eventHandler.crafters;
		if (li.size() != 1 || (!(li.get(0) instanceof EntityPlayer))) {
			return null;
		}
		return (EntityPlayer)li.get(0);
	}

	@SideOnly(Side.CLIENT)
	private EntityPlayer getClientPlayer() {
		return Minecraft.getMinecraft().thePlayer;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting ic) {
		return recipe.getCraftingResult(ic);
	}

	@Override
	public int getRecipeSize() {
		return recipe.getRecipeSize();
	}

	public boolean usesItem(ItemStack ingredient) {
		return ReikaRecipeHelper.recipeContains(recipe, ingredient);
	}

	@Override
	public String getRequiredPowerDesc() {
		return unlockPower == null ? null : unlockPower.getDisplayString();
	}

	@Override
	public String getID() {
		return id;
	}

	public boolean isCraftable() {
		return !(recipe instanceof UncraftableAltRecipe);
	}

	@SideOnly(Side.CLIENT)
	public CachedRecipe createNEIDelegate() {
		if (recipe instanceof ShapedRecipes) {
			ShapedRecipeHandler sr = new ShapedRecipeHandler();
			return sr.new CachedShapedRecipe((ShapedRecipes)recipe);
		}
		else if (recipe instanceof ShapelessRecipes) {
			ShapelessRecipes sr = (ShapelessRecipes)recipe;
			ShapelessRecipeHandler srh = new ShapelessRecipeHandler();
			return srh.new CachedShapelessRecipe(sr.recipeItems, sr.getRecipeOutput()); //ew
		}
		else if (recipe instanceof ShapedOreRecipe) {
			return new ShapedRecipeHandler().forgeShapedRecipe((ShapedOreRecipe)recipe);
		}
		else if (recipe instanceof ShapelessOreRecipe) {
			return new ShapelessRecipeHandler().forgeShapelessRecipe((ShapelessOreRecipe)recipe);
		}
		else {
			return null;
		}
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
