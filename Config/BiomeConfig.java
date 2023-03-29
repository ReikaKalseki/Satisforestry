/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.Satisforestry.Config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import com.google.common.base.Strings;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import Reika.DragonAPI.Exception.InstallationException;
import Reika.DragonAPI.Exception.RegistrationException;
import Reika.DragonAPI.Exception.UserErrorException;
import Reika.DragonAPI.IO.ReikaFileReader;
import Reika.DragonAPI.Instantiable.Data.KeyedItemStack;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Instantiable.IO.CustomRecipeList;
import Reika.DragonAPI.Instantiable.IO.LuaBlock;
import Reika.DragonAPI.Instantiable.IO.LuaBlock.LuaBlockDatabase;
import Reika.DragonAPI.Interfaces.Registry.OreType;
import Reika.DragonAPI.Libraries.ReikaEnchantmentHelper;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaOreHelper;
import Reika.DragonAPI.ModInteract.DeepInteract.SensitiveFluidRegistry;
import Reika.DragonAPI.ModInteract.DeepInteract.SensitiveItemRegistry;
import Reika.DragonAPI.ModRegistry.ModOreList;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.API.AltRecipe.UncraftableAltRecipe;
import Reika.Satisforestry.Biome.DecoratorPinkForest.OreClusterType;
import Reika.Satisforestry.Biome.DecoratorPinkForest.OreSpawnLocation;
import Reika.Satisforestry.Config.DoggoDrop.Checks;
import Reika.Satisforestry.Config.NodeResource.EffectTypes;
import Reika.Satisforestry.Config.NodeResource.Purity;
import Reika.Satisforestry.Registry.SFBlocks;
import Reika.Satisforestry.Registry.SFOptions;


public class BiomeConfig {

	public static final BiomeConfig instance = new BiomeConfig();

	public static final String COMPACTED_COAL_ID = "Compacted Coal";
	public static final String TURBOFUEL_ID = "Turbofuel";

	private LuaBlockDatabase oreData;
	private LuaBlockDatabase itemData;
	private LuaBlockDatabase fluidData;
	private LuaBlockDatabase doggoData;
	private LuaBlockDatabase recipeData;

	private int definitionCount;
	private int entryAttemptsCount;
	private int entryCount;

	private final HashMap<String, OreClusterType> oreEntries = new HashMap();
	private final HashMap<String, ResourceItem> resourceEntries = new HashMap();
	private final HashMap<String, ResourceFluid> fluidEntries = new HashMap();
	private final HashMap<String, AlternateRecipe> recipeEntries = new HashMap();
	private final Collection<DoggoDrop> doggoEntries = new ArrayList();
	private final HashSet<KeyedItemStack> doggoItems = new HashSet();
	private final HashMap<OreType, Integer> doggoOres = new HashMap();

	private BiomeConfig() {
		{
			oreData = new LuaBlockDatabase();
			OreLuaBlock example = new OreLuaBlock("example", null, oreData);
			example.putData("type", "example_ores");
			example.putData("block", "minecraft:iron_ore");
			//example.putData("generate", "true");
			OreLuaBlock ores = new OreLuaBlock("blocks", example, oreData);
			OreLuaBlock spawns = new OreLuaBlock("spawnLocations", example, oreData);
			for (OreSpawnLocation s : OreSpawnLocation.values()) {
				OreLuaBlock sec = new OreLuaBlock(s.name(), spawns, oreData);
				sec.putData("sizeScale", 1F);
				sec.putData("maxSize", 4);
				sec.putData("spawnWeight", 10);
				sec.setComment("maxSize", "max cluster radius");
			}
			example.setComment("block", "single block type, mutually exclusive with 'blocks'");
			example.setComment("blocks", "optional, multiple block shorthand; mutually exclusive with 'block'");
			spawns.setComment(null, "where this type can spawn, valid locations: "+ReikaJavaLibrary.getEnumNameList(OreSpawnLocation.class));
			oreData.addBlock("example", example);
		}

		{
			itemData = new LuaBlockDatabase();
			ResourceItemLuaBlock example2 = new ResourceItemLuaBlock("example", null, itemData);
			example2.putData("type", "example_resources");
			example2.putData("spawnWeight", 10);
			example2.putData("renderColor", "0xffffff");
			//example2.putData("generate", "true");
			ResourceItemLuaBlock levels = new ResourceItemLuaBlock("purityLevels", example2, itemData);
			for (Purity p : Purity.list) {
				levels.putData(p.name(), p == Purity.NORMAL ? 25 : 10);
			}
			ResourceItemLuaBlock items = new ResourceItemLuaBlock("outputItems", example2, itemData);
			LuaBlock item = new ResourceItemLuaBlock("{", items, itemData);
			item.putData("key", "minecraft:iron_ingot");
			item.putData("weight", 10);
			item.putData("minCount", 1);
			item.putData("maxCount", 3);
			item.putData("minimumPurity", Purity.IMPURE.name());
			item = new ResourceItemLuaBlock("{", items, itemData);
			item.putData("key", "minecraft:gold_ingot");
			item.putData("weight", 6);
			item.putData("minCount", 1);
			item.putData("maxCount", 1);
			item.putData("manualWeightModifier", 0.3);
			item.putData("manualAmountModifier", 0.5);
			ResourceItemLuaBlock scales = new ResourceItemLuaBlock("weightModifiers", item, itemData);
			for (Purity p : Purity.list) {
				scales.putData(p.name(), (p.ordinal()+1)/2F);
			}
			scales = new ResourceItemLuaBlock("amountModifiers", item, itemData);
			for (Purity p : Purity.list) {
				scales.putData(p.name(), p == Purity.PURE ? 1F : 0.5F);
			}
			item.putData("minimumPurity", Purity.NORMAL.name());
			ResourceItemLuaBlock effects = new ResourceItemLuaBlock("effects", example2, itemData);
			item = new ResourceItemLuaBlock("{", effects, itemData);
			item.putData("effectType", "damage");
			item.putData("amount", 0.5F);
			item.putData("rate", 20);
			item.setComment("effectType", "type of effect, valid values: "+ReikaJavaLibrary.getEnumNameList(EffectTypes.class));
			item.setComment("rate", "ticks per hit");
			item = new ResourceItemLuaBlock("{", effects, itemData);
			item.putData("effectType", "potion");
			item.putData("potionID", Potion.weakness.id);
			item.putData("level", 1);
			example2.setComment("minCount", "min yield per harvest cycle");
			example2.setComment("maxCount", "max yield per harvest cycle");
			levels.setComment(null, "purity level distribution");
			effects.setComment(null, "optional, ambient AoE effects around the node");
			item.setComment("potionID", "weakness");
			itemData.addBlock("example", example2);
		}

		{
			fluidData = new LuaBlockDatabase();
			ResourceFluidLuaBlock example2b = new ResourceFluidLuaBlock("example", null, fluidData);
			example2b.putData("type", "example_fluids");
			example2b.putData("spawnWeight", 10);
			example2b.putData("renderColor", "0xFF8000");
			example2b.putData("maxSubnodes", 6);
			example2b.putData("glowAtNight", true);
			//example2b.putData("generate", "true");
			ResourceFluidLuaBlock levels2 = new ResourceFluidLuaBlock("purityLevels", example2b, fluidData);
			for (Purity p : Purity.list) {
				levels2.putData(p.name(), p == Purity.NORMAL ? 25 : 10);
			}
			ResourceFluidLuaBlock fluid = new ResourceFluidLuaBlock("outputFluids", example2b, fluidData);
			fluid.putData("key", "lava");
			fluid.putData("weight", 10);
			fluid.putData("minAmount", 100);
			fluid.putData("maxAmount", 500);
			ResourceFluidLuaBlock scales2 = new ResourceFluidLuaBlock("amountModifiers", fluid, fluidData);
			for (Purity p : Purity.list) {
				scales2.putData(p.name(), (p.ordinal()+1)*0.5F);
			}
			ResourceFluidLuaBlock effects2 = new ResourceFluidLuaBlock("effects", example2b, fluidData);
			fluid = new ResourceFluidLuaBlock("{", effects2, fluidData);
			fluid.putData("effectType", "damage");
			fluid.putData("amount", 2.5F);
			fluid.putData("rate", 20);
			fluid = new ResourceFluidLuaBlock("inputFluids", example2b, fluidData);
			fluid.putData("key", "water");
			fluid.putData("amount", 100);
			fluid.setComment("effectType", "type of effect, valid values: "+ReikaJavaLibrary.getEnumNameList(EffectTypes.class));
			fluid.setComment("rate", "ticks per hit");
			example2b.setComment("minCount", "min yield per harvest cycle");
			example2b.setComment("maxCount", "max yield per harvest cycle");
			levels2.setComment(null, "purity level distribution");
			effects2.setComment(null, "optional, ambient AoE effects2 around the node");
			fluidData.addBlock("example", example2b);
		}

		{
			doggoData = new LuaBlockDatabase();
			DoggoLuaBlock example3 = new DoggoLuaBlock("example", null, doggoData);
			example3.putData("type", "example_doggo");
			//example3.putData("generate", "true");
			DoggoLuaBlock drops = new DoggoLuaBlock("findableItems", example3, doggoData);

			DoggoDrop drop = new DoggoDrop(Items.diamond, 1, 1, 2);
			drop.addWeightFactor(Checks.MAXY, 16, 2);
			drop.addWeightFactor(Checks.MAXY, 24, 1.5);
			drop.addWeightFactor(Checks.MAXY, 40, 1.2);
			drop.addCondition(Checks.HEALTH, 0.75F);
			drop.addCondition(Checks.SKY, false);
			drop.createLuaBlock(drops, doggoData);

			drop = new DoggoDrop(Items.slime_ball, 1, 2, 15);
			drop.addCondition(Checks.BIOME, 6);
			drop.addCondition(Checks.PEACEFUL, false);
			drop.createLuaBlock(drops, doggoData);

			ItemStack is = new ItemStack(Items.bone);
			ReikaEnchantmentHelper.applyEnchantment(is, Enchantment.baneOfArthropods, 2);
			drop = new DoggoDrop(is, 1, 4, 10);
			drop.addWeightFactor(Checks.NIGHT, true, 2.5);
			drop.createLuaBlock(drops, doggoData);

			//drop = new DoggoDrop(Satisforestry.paleberry, 1, 16, 50);
			//drop.createLuaBlock(drops, doggoData);

			doggoData.addBlock("example", example3);
		}

		{
			recipeData = new LuaBlockDatabase();
			AltRecipeLuaBlock example3 = new AltRecipeLuaBlock("example", null, recipeData);
			example3.putData("type", "example_alt_recipe");
			example3.putData("spawnWeight", 40);
			//example3.putData("generate", "true");
			AltRecipeLuaBlock recipe = new AltRecipeLuaBlock("recipe", example3, recipeData);
			recipe.putData("shaped", true);
			recipe.putData("input_top", "null, minecraft:book, null");
			recipe.putData("input_middle", "minecraft:gold_ingot, minecraft:iron_block, minecraft:gold_ingot");
			recipe.putData("input_bottom", "null, minecraft:book, null");
			AltRecipeLuaBlock out = new AltRecipeLuaBlock("output", recipe, recipeData);
			out.putData("item", "minecraft:iron_pickaxe");
			AltRecipeLuaBlock nbt = new AltRecipeLuaBlock("nbt", out, recipeData);
			AltRecipeLuaBlock ench = new AltRecipeLuaBlock("ench", nbt, recipeData);
			AltRecipeLuaBlock ench2 = new AltRecipeLuaBlock("{", ench, recipeData);
			ench2.putData("id", 35);
			ench2.putData("lvl", 3);

			AltRecipeLuaBlock pwr = new AltRecipeLuaBlock("requiredPower", example3, recipeData);
			pwr.putData("format", "RF");
			pwr.putData("amount", 6000);
			pwr.putData("time", 30);
			pwr.putData("timeUnit", "seconds");

			AltRecipeLuaBlock req = new AltRecipeLuaBlock("requiredItem", example3, recipeData);
			req.putData("item", "minecraft:redstone*18");

			recipeData.addBlock("example", example3);
		}

		doggoOres.put(ReikaOreHelper.COAL, 20);
		doggoOres.put(ReikaOreHelper.IRON, 10);
		doggoOres.put(ReikaOreHelper.GOLD, 5);
		doggoOres.put(ReikaOreHelper.DIAMOND, 1);
		doggoOres.put(ReikaOreHelper.REDSTONE, 10);
		doggoOres.put(ReikaOreHelper.LAPIS, 3);
		doggoOres.put(ModOreList.COPPER, 15);
		doggoOres.put(ModOreList.TIN, 15);
		doggoOres.put(ModOreList.NICKEL, 5);
		doggoOres.put(ModOreList.SILVER, 5);
		doggoOres.put(ModOreList.ALUMINUM, 10);
		doggoOres.put(ModOreList.LEAD, 10);
		doggoOres.put(ModOreList.URANIUM, 2);
	}

	/** Returns the number of entries that loaded! */
	public void loadConfigs() {
		this.reset();
		Satisforestry.logger.log("Loading configs.");
		File f = this.getSaveFolder(); //parent dir
		if (f.exists()) {
			this.loadFiles(f);
			try {
				this.parseConfigs();
			}
			catch (Exception e) {
				if (e instanceof UserErrorException)
					throw new InstallationException(Satisforestry.instance, "Configs could not be loaded! Delete them and try again.", e);
				else
					throw new RegistrationException(Satisforestry.instance, "Configs could not be loaded! Delete them and try again.", e);
			}

			Satisforestry.logger.log("Configs loaded.");
		}
		else {
			try {
				f.mkdirs();
			}
			catch (Exception e) {
				e.printStackTrace();
				Satisforestry.logger.logError("Could not create biome config folder!");
			}
			try {
				this.createDefaultFiles(f);
			}
			catch (Exception e) {
				e.printStackTrace();
				Satisforestry.logger.logError("Could not create default configs!");
			}
		}
		try {
			this.createExampleFile(f);
		}
		catch (IOException e) {
			e.printStackTrace();
			Satisforestry.logger.logError("Could not create example data file!");
		}

		OreSpawnLocation.init();
	}

	private void createDefaultFiles(File folder) throws IOException {
		File f1 = new File(folder, "ores.lua");
		f1.createNewFile();
		ReikaFileReader.writeLinesToFile(f1, oreData.getBlock("example").writeToStrings(), true);

		File f2 = new File(folder, "resources.lua");
		f2.createNewFile();
		ReikaFileReader.writeLinesToFile(f2, itemData.getBlock("example").writeToStrings(), true);

		File f4 = new File(folder, "fluids.lua");
		f4.createNewFile();
		ReikaFileReader.writeLinesToFile(f4, fluidData.getBlock("example").writeToStrings(), true);

		File f3 = new File(folder, "doggo.lua");
		f3.createNewFile();
		ReikaFileReader.writeLinesToFile(f3, doggoData.getBlock("example").writeToStrings(), true);

		File f5 = new File(folder, "recipes.lua");
		f5.createNewFile();
		ReikaFileReader.writeLinesToFile(f5, recipeData.getBlock("example").writeToStrings(), true);
	}

	private void createExampleFile(File f) throws IOException {
		File out = new File(f, "example.lua");
		if (out.exists())
			out.delete();
		out.createNewFile();

		ArrayList<String> li = oreData.getBlock("example").writeToStrings();
		li.set(li.size()-1, li.get(li.size()-1)+",");
		li.addAll(itemData.getBlock("example").writeToStrings());
		li.set(li.size()-1, li.get(li.size()-1)+",");
		li.addAll(fluidData.getBlock("example").writeToStrings());
		li.set(li.size()-1, li.get(li.size()-1)+",");
		li.addAll(doggoData.getBlock("example").writeToStrings());
		li.set(li.size()-1, li.get(li.size()-1)+",");
		li.addAll(recipeData.getBlock("example").writeToStrings());

		ReikaFileReader.writeLinesToFile(out, li, true);
	}

	private void reset() {
		LuaBlock example = oreData.getBlock("example");
		LuaBlock example2 = itemData.getBlock("example");
		LuaBlock example3 = doggoData.getBlock("example");
		LuaBlock example4 = fluidData.getBlock("example");
		LuaBlock example5 = recipeData.getBlock("example");

		oreData = new LuaBlockDatabase();
		itemData = new LuaBlockDatabase();
		fluidData = new LuaBlockDatabase();
		doggoData = new LuaBlockDatabase();
		recipeData = new LuaBlockDatabase();

		oreEntries.clear();
		resourceEntries.clear();
		fluidEntries.clear();
		doggoEntries.clear();
		doggoItems.clear();
		recipeEntries.clear();

		definitionCount = 0;
		entryAttemptsCount = 0;
		entryCount = 0;

		oreData.addBlock("example", example);
		itemData.addBlock("example", example2);
		doggoData.addBlock("example", example3);
		fluidData.addBlock("example", example4);
		recipeData.addBlock("example", example5);
	}

	private void loadFiles(File parent) {
		File f1 = ReikaFileReader.getFileByNameAnyExt(parent, "ores");
		File f2 = ReikaFileReader.getFileByNameAnyExt(parent, "resources");
		File f4 = ReikaFileReader.getFileByNameAnyExt(parent, "fluids");
		File f3 = ReikaFileReader.getFileByNameAnyExt(parent, "doggo");
		File f5 = ReikaFileReader.getFileByNameAnyExt(parent, "recipes");
		if (f2 == null || !f2.exists()) {
			throw new InstallationException(Satisforestry.instance, "No resource config file found!");
		}
		itemData.loadFromFile(f2);

		if (f4 == null || !f4.exists()) {
			throw new InstallationException(Satisforestry.instance, "No fluid config file found!");
		}
		fluidData.loadFromFile(f4);

		if (f1.exists())
			oreData.loadFromFile(f1);
		else
			Satisforestry.logger.log("No ore config file found; no ore clusters will generate.");

		if (f3.exists())
			doggoData.loadFromFile(f3);
		else
			Satisforestry.logger.log("No doggo config file found; the only items doggos will find will be the ones hardcoded to Satisforestry.");

		if (f5.exists())
			recipeData.loadFromFile(f5);
		else
			Satisforestry.logger.log("No custom recipe config file found; your only alternate recipes will be the ones hardcoded to Satisforestry.");
	}

	private void parseConfigs() {
		LuaBlock root = oreData.getRootBlock();
		for (LuaBlock b : root.getChildren()) {
			try {
				definitionCount++;
				String type = b.getString("type");
				oreData.addBlock(type, b);
				this.parseOreEntry(type, b);
			}
			catch (Exception e) {
				Satisforestry.logger.logError("Could not parse config section "+b.getString("type")+": ");
				ReikaJavaLibrary.pConsole(b);
				ReikaJavaLibrary.pConsole("----------------------Cause------------------------");
				e.printStackTrace();
			}
		}
		Satisforestry.logger.log("All ore config entries parsed; files contained "+definitionCount+" definitions, for a total of "+entryAttemptsCount+" entries, of which "+entryCount+" loaded.");

		definitionCount = 0;
		entryAttemptsCount = 0;
		entryCount = 0;

		root = itemData.getRootBlock();
		for (LuaBlock b : root.getChildren()) {
			try {
				definitionCount++;
				String type = b.getString("type");
				itemData.addBlock(type, b);
				this.parseResourceEntry(type, b);
			}
			catch (Exception e) {
				Satisforestry.logger.logError("Could not parse config section "+b.getString("type")+": ");
				ReikaJavaLibrary.pConsole(b);
				ReikaJavaLibrary.pConsole("----------------------Cause------------------------");
				e.printStackTrace();
			}
		}
		Satisforestry.logger.log("All resource config entries parsed; files contained "+definitionCount+" definitions, for a total of "+entryAttemptsCount+" entries, of which "+entryCount+" loaded.");
		if (resourceEntries.isEmpty()) {
			throw new InstallationException(Satisforestry.instance, "No resource entries were loaded; at least one must be defined!");
		}

		definitionCount = 0;
		entryAttemptsCount = 0;
		entryCount = 0;

		root = fluidData.getRootBlock();
		for (LuaBlock b : root.getChildren()) {
			try {
				definitionCount++;
				String type = b.getString("type");
				fluidData.addBlock(type, b);
				this.parseFluidEntry(type, b);
			}
			catch (Exception e) {
				Satisforestry.logger.logError("Could not parse config section "+b.getString("type")+": ");
				ReikaJavaLibrary.pConsole(b);
				ReikaJavaLibrary.pConsole("----------------------Cause------------------------");
				e.printStackTrace();
			}
		}
		Satisforestry.logger.log("All fluid config entries parsed; files contained "+definitionCount+" definitions, for a total of "+entryAttemptsCount+" entries, of which "+entryCount+" loaded.");
		if (fluidEntries.isEmpty()) {
			throw new InstallationException(Satisforestry.instance, "No fluid entries were loaded; at least one must be defined!");
		}

		definitionCount = 0;
		entryAttemptsCount = 0;
		entryCount = 0;

		root = doggoData.getRootBlock();
		for (LuaBlock b : root.getChildren()) {
			try {
				definitionCount++;
				String type = b.getString("type");
				doggoData.addBlock(type, b);
				this.parseDoggoEntry(type, b);
			}
			catch (Exception e) {
				Satisforestry.logger.logError("Could not parse config section "+b.getString("type")+": ");
				ReikaJavaLibrary.pConsole(b);
				ReikaJavaLibrary.pConsole("----------------------Cause------------------------");
				e.printStackTrace();
			}
		}
		Satisforestry.logger.log("All doggo-item config entries parsed; files contained "+definitionCount+" definitions, for a total of "+entryAttemptsCount+" entries, of which "+entryCount+" loaded.");

		definitionCount = 0;
		entryAttemptsCount = 0;
		entryCount = 0;

		root = recipeData.getRootBlock();
		for (LuaBlock b : root.getChildren()) {
			try {
				definitionCount++;
				String type = b.getString("type");
				recipeData.addBlock(type, b);
				this.parseAltRecipeEntry(type, b);
			}
			catch (Exception e) {
				Satisforestry.logger.logError("Could not parse config section "+b.getString("type")+": ");
				ReikaJavaLibrary.pConsole(b);
				ReikaJavaLibrary.pConsole("----------------------Cause------------------------");
				e.printStackTrace();
			}
		}
		Satisforestry.logger.log("All alternate recipe config entries parsed; files contained "+definitionCount+" definitions, for a total of "+entryAttemptsCount+" entries, of which "+entryCount+" loaded.");
		this.addHardcodedEntries();
	}

	private void addHardcodedEntries() {
		for (int i = 0; i < 3; i++)
			doggoEntries.add(new DoggoDrop(SFBlocks.SLUG.getStackOfMetadata(i), 1, 1, 9/ReikaMathLibrary.intpow2(3, i)));
		doggoEntries.add(new DoggoDrop(Satisforestry.paleberry, 1, 6, 25));
		doggoEntries.add(new DoggoDrop(Items.stick, 1, 8, 20));
		doggoEntries.add(new DoggoDrop(Items.rotten_flesh, 1, 12, 15));
		doggoEntries.add(new DoggoDrop(Satisforestry.sludge, 1, 1, 10));

		for (Entry<OreType, Integer> e : doggoOres.entrySet()) {
			OreType ore = e.getKey();
			if (ore.existsInGame()) {
				int amtMax = 1;
				switch(ore.getRarity()) {
					case EVERYWHERE:
						amtMax = 24;
						break;
					case COMMON:
						amtMax = 16;
						break;
					case AVERAGE:
						amtMax = 8;
						break;
					case SCATTERED:
						amtMax = 4;
						break;
					case SCARCE:
						amtMax = 2;
						break;
					case RARE:
						amtMax = 1;
						break;
				}
				doggoEntries.add(new DoggoDrop(ore.getFirstOreBlock(), 1, amtMax, e.getValue()));
			}
		}

		String sulf = ModOreList.SULFUR.getProductOreDictName();
		if (ReikaItemHelper.oreItemExists(sulf)) {
			Object coal = ReikaItemHelper.oreItemExists("dustCoal") ? "dustCoal" : Items.coal;
			ItemStack in = ReikaItemHelper.lookupItem(SFOptions.COMPACTCOALITEM.getString());
			String pwr = SFOptions.COMPACTCOALPOWER.getString();
			String[] parts = pwr.split(";");
			ItemStack out = new ItemStack(Satisforestry.compactedCoal, 2, 1);
			if (parts.length != 3 || Strings.isNullOrEmpty(parts[0])) {
				this.addAlternateRecipe(COMPACTED_COAL_ID, 50, new ShapelessOreRecipe(out, coal, coal, sulf, sulf), in, null, 0, 0);
			}
			else {
				try {
					this.addAlternateRecipe(COMPACTED_COAL_ID, 50, new ShapelessOreRecipe(out, coal, coal, sulf, sulf), in, parts[0], Long.parseLong(parts[1]), Long.parseLong(parts[2]));
				}
				catch (Exception e) {
					throw new InstallationException(Satisforestry.instance, "Invalid compacted coal alternate recipe parameters specified", e);
				}
			}
		}

		if (Satisforestry.turbofuel != null) { //the recipe will not be craftable without another mod to add it from their side
			ItemStack in = ReikaItemHelper.lookupItem(SFOptions.TURBOFUELITEM.getString());
			IRecipe dummy = new UncraftableAltRecipe() {
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
			String pwr = SFOptions.TURBOFUELPOWER.getString();
			String[] parts = pwr.split(";");
			if (parts.length != 3 || Strings.isNullOrEmpty(parts[0])) {
				this.addAlternateRecipe(TURBOFUEL_ID, 25, dummy, in, null, 0, 0);
			}
			else {
				try {
					this.addAlternateRecipe(TURBOFUEL_ID, 25, dummy, in, parts[0], Long.parseLong(parts[1]), Long.parseLong(parts[2]));
				}
				catch (Exception e) {
					throw new InstallationException(Satisforestry.instance, "Invalid compacted coal alternate recipe parameters specified", e);
				}
			}
		}
	}

	public AlternateRecipe addAlternateRecipe(String id, int wt, IRecipe rec, ItemStack need, String powerType, long powerAmount, long ticksFor) {
		if (recipeEntries.containsKey(id))
			throw new IllegalArgumentException("Recipe ID '"+id+"' is already in use: "+recipeEntries.get(id));
		AlternateRecipe alt = new AlternateRecipe(id, wt, rec, need, powerType, powerAmount, ticksFor);
		recipeEntries.put(id, alt);
		return alt;
	}

	private void parseOreEntry(String type, LuaBlock b) throws NumberFormatException, IllegalArgumentException, IllegalStateException {
		ArrayList<String> blocks = new ArrayList();
		HashMap<OreSpawnLocation, LuaBlock> sections = new HashMap();

		String bsk = b.getString("block");
		if (!LuaBlock.isErrorCode(bsk))
			blocks.add(bsk);

		LuaBlock set = b.getChild("blocks");
		if (set != null) {
			blocks.addAll(set.getDataValues());
		}

		if (blocks.isEmpty())
			throw new IllegalArgumentException("No blocks specified");

		LuaBlock spawn = b.getChild("spawnLocations");
		if (spawn == null)
			throw new IllegalArgumentException("No spawn locations specified");
		for (OreSpawnLocation s : OreSpawnLocation.values()) {
			LuaBlock lb = spawn.getChild(s.name());
			if (lb != null) {
				sections.put(s, lb);
			}
		}
		if (sections.isEmpty())
			throw new IllegalArgumentException("No spawn locations specified");

		entryAttemptsCount += blocks.size()*sections.size();

		ArrayList<BlockKey> types = new ArrayList();
		for (String s : blocks) {
			BlockKey bk = null;
			try {
				bk = this.parseBlockKey(s);
			}
			catch (Exception e) {
				Satisforestry.logger.logError("Threw exception parsing block ID '"+s+"':");
				e.printStackTrace();
			}
			if (bk == null) {
				Satisforestry.logger.logError("Could not load block type '"+s+"' for ore type '"+type+"'; skipping.");
				continue;
			}
			else {
				types.add(bk);
			}
		}
		for (Entry<OreSpawnLocation, LuaBlock> e : sections.entrySet()) {
			OreSpawnLocation cs = e.getKey();
			LuaBlock data = e.getValue();
			String id = type+"_"+cs.name();
			OreClusterType ore = new OreClusterType(id, cs, data.getInt("spawnWeight"), types);
			if (data.containsKey("sizeScale"))
				ore.sizeScale = (float)data.getDouble("sizeScale");
			if (data.containsKey("maxSize"))
				ore.maxDepth = data.getInt("maxSize");
			oreEntries.put(id, ore);
			Satisforestry.logger.log("Registered ore type '"+type+"' with "+types.size()+" blocks '"+types+"' for area "+cs+", wt="+ore.spawnWeight+" s="+ore.sizeScale+" d="+ore.maxDepth);
			entryCount++;
		}
	}

	private void parseResourceEntry(String type, LuaBlock b) throws NumberFormatException, IllegalArgumentException, IllegalStateException {
		ArrayList<LuaBlock> items = new ArrayList();

		LuaBlock set = b.getChild("outputItems");
		if (set == null)
			throw new IllegalArgumentException("No items specified");
		for (LuaBlock s : set.getChildren()) {
			items.add(s);
		}
		if (items.isEmpty())
			throw new IllegalArgumentException("No items specified");

		LuaBlock purities = b.getChild("purityLevels");
		if (purities == null)
			throw new IllegalArgumentException("No purity levels specified");
		HashMap<String, Object> map = purities.asHashMap();
		if (map.isEmpty())
			throw new IllegalArgumentException("No purity levels specified");

		ResourceItem ore = new ResourceItem(type, b.getString("displayName"), b.getInt("spawnWeight"), b.getInt("renderColor"), map);
		if (b.containsKey("speedFactor"))
			ore.speedFactor = (float)b.getDouble("speedFactor");
		if (ore.speedFactor <= 0)
			throw new IllegalArgumentException("Invalid speed factor");

		for (LuaBlock s : items) {
			entryAttemptsCount++;
			String sk = s.getString("key");
			ItemStack is = CustomRecipeList.parseItemString(sk, s.getChild("nbt"), true);
			if (is == null) {
				Satisforestry.logger.logError("Could not load item type '"+sk+"' for resource type '"+type+"' - no item found. Skipping.");
				continue;
			}
			if (SensitiveItemRegistry.instance.contains(is)) {
				Satisforestry.logger.logError("Could not load item type '"+sk+"' for resource type '"+type+"' - is not allowed. Skipping.");
				continue;
			}
			int weight = s.getInt("weight");
			float man = s.containsKey("manualModifier") ? (float)s.getDouble("manualModifier") : 1; //TODO unimplemented
			int min = s.getInt("minCount");
			int max = s.getInt("maxCount");
			if (max <= 0)
				throw new IllegalArgumentException("Invalid drop count");
			if (max < min)
				throw new IllegalArgumentException("Min count is greater than max count");
			Purity p = Purity.valueOf(s.getString("minimumPurity"));
			while (p != null) {
				ore.addItem(p, is, weight, min, max, s);
				p = p.higher();
			}
		}

		if (ore.hasNoItems())
			throw new IllegalArgumentException("Resource type found no items for any of its definitions");

		LuaBlock effects = b.getChild("effects");
		if (effects != null) {
			for (LuaBlock lb : effects.getChildren()) {
				ore.addEffect(lb);
			}
		}

		resourceEntries.put(type, ore);
		Satisforestry.logger.log("Registered resource type '"+type+"': "+ore.toString().replaceAll("\\\\n", " "));
		entryCount++;
	}

	private void parseFluidEntry(String type, LuaBlock b) throws NumberFormatException, IllegalArgumentException, IllegalStateException {
		ArrayList<LuaBlock> fluids = new ArrayList();

		LuaBlock fluid = b.getChild("outputFluids");
		if (fluid == null)
			throw new IllegalArgumentException("No fluid specified");

		LuaBlock purities = b.getChild("purityLevels");
		if (purities == null)
			throw new IllegalArgumentException("No purity levels specified");
		HashMap<String, Object> map = purities.asHashMap();
		if (map.isEmpty())
			throw new IllegalArgumentException("No purity levels specified");

		int nodes = b.getInt("maxSubnodes");
		if (nodes < 1)
			throw new IllegalArgumentException("Too low limit for subnodes");
		if (nodes > 8)
			throw new IllegalArgumentException("Too high limit for subnodes");
		LuaBlock in = b.getChild("inputFluids");
		FluidStack fs = in == null ? null : new FluidStack(FluidRegistry.getFluid(in.getString("key")), in.getInt("amount"));
		ResourceFluid ore = new ResourceFluid(type, b.getInt("spawnWeight"), b.getInt("renderColor"), nodes, b.getBoolean("glowAtNight"), fs, b.getInt("rounding"), map);

		entryAttemptsCount++;
		String sk = fluid.getString("key");
		Fluid is = FluidRegistry.getFluid(sk);
		if (is == null)
			throw new IllegalArgumentException("Could not load fluid type '"+sk+"' for resource type '"+type+"' - no fluid found.");
		if (SensitiveFluidRegistry.instance.contains(is))
			throw new IllegalArgumentException("Could not load fluid type '"+sk+"' for resource type '"+type+"' - is not allowed.");
		int min = fluid.getInt("minAmount");
		int max = fluid.getInt("maxAmount");
		if (max <= 0)
			throw new IllegalArgumentException("Invalid yield amount");
		if (max < min)
			throw new IllegalArgumentException("Min amount is greater than max amount");
		for (Purity p : Purity.list)
			ore.addItem(p, is, 1, min, max, fluid);

		if (ore.hasNoItems())
			throw new IllegalArgumentException("Resource type found no fluids for any of its definitions");

		LuaBlock effects = b.getChild("effects");
		if (effects != null) {
			for (LuaBlock lb : effects.getChildren()) {
				ore.addEffect(lb);
			}
		}

		fluidEntries.put(type, ore);
		Satisforestry.logger.log("Registered resource type '"+type+"': "+ore.toString().replaceAll("\\\\n", " "));
		entryCount++;
	}

	private void parseDoggoEntry(String type, LuaBlock b) throws NumberFormatException, IllegalArgumentException, IllegalStateException {
		ArrayList<LuaBlock> items = new ArrayList();

		LuaBlock set = b.getChild("findableItems");
		if (set == null)
			throw new IllegalArgumentException("No items specified");
		for (LuaBlock s : set.getChildren()) {
			items.add(s);
		}
		if (items.isEmpty())
			throw new IllegalArgumentException("No items specified");

		for (LuaBlock s : items) {
			entryAttemptsCount++;
			ItemStack is = CustomRecipeList.parseItemString(s.getString("key"), s.getChild("nbt"), true);
			if (is == null) {
				Satisforestry.logger.logError("Could not load item type '"+s+"' for doggo drop '"+type+"'; skipping.");
				continue;
			}
			KeyedItemStack ks = new KeyedItemStack(is).setIgnoreMetadata(false).setIgnoreNBT(false).setSized(false).setSimpleHash(true);
			if (doggoItems.contains(ks)) {
				Satisforestry.logger.logError("Doggo drop for item '"+CustomRecipeList.fullID(is)+"' already loaded. Skipping duplicate in '"+type+"'.");
				continue;
			}
			doggoItems.add(ks);
			int weight = s.getInt("weight");
			int min = s.getInt("minCount");
			int max = s.getInt("maxCount");

			DoggoDrop drop = new DoggoDrop(is, min, max, weight);
			LuaBlock limit = s.getChild("limits");
			if (limit != null) {
				for (LuaBlock in : limit.getChildren()) {
					drop.addCondition(in);
				}
			}
			LuaBlock modifiers = s.getChild("weightFactors");
			if (modifiers != null) {
				for (LuaBlock in : modifiers.getChildren()) {
					drop.addWeightFactor(in);
				}
			}

			doggoEntries.add(drop);
			Satisforestry.logger.log("Registered doggo drop type '"+type+"': "+drop.toString().replaceAll("\\\\n", " "));
			entryCount++;
		}
	}

	private void parseAltRecipeEntry(String type, LuaBlock b) throws NumberFormatException, IllegalArgumentException, IllegalStateException {
		entryAttemptsCount++;
		if (recipeEntries.containsKey(type))
			throw new IllegalArgumentException("Recipe ID '"+type+"' is already in use: "+recipeEntries.get(type));
		AlternateRecipe rec = new AlternateRecipe(type, b.getInt("spawnWeight"), b.getChild("recipe"), b.getChild("requiredItem"), b.getChild("requiredPower"));
		rec.displayName = b.getString("displayName");
		recipeEntries.put(rec.id, rec);
		Satisforestry.logger.log("Registered alternate recipe '"+type+"': "+rec.toString().replaceAll("\\\\n", " "));
		entryCount++;
	}

	private Collection<BlockKey> parseBlocks(LuaBlock b) {
		Collection<BlockKey> blocks = new HashSet();
		Block block = Block.getBlockFromName(b.getString("block"));
		if (block != null) {
			LuaBlock metas = b.getChild("metadata"); //do not use inherit, use direct call so will return null if unspecified
			Collection<Integer> c = new HashSet();
			if (metas != null) {
				for (String val : metas.getDataValues()) {
					int m = Integer.parseInt(val);
					c.add(m);
				}
			}
			else {
				c.add(-1);
			}
			for (int m : c) {
				blocks.add(new BlockKey(block, m));
			}
		}
		return blocks;
	}

	private BlockKey parseBlockKey(String s) {
		String[] parts = s.split(":");
		if (parts.length < 2)
			throw new IllegalArgumentException("Malformed Block Name/Namespace: "+s);
		int meta = 0;
		if (parts.length == 3)
			meta = Integer.parseInt(parts[2]);
		Block b = Block.getBlockFromName(parts[0]+":"+parts[1]);
		return b != null ? new BlockKey(b, meta) : null;
	}

	private final File getSaveFolder() {
		return new File(Satisforestry.config.getConfigFolder(), "Satisforestry_Files");
	}

	public Collection<OreClusterType> getOreTypes() {
		return oreEntries.values();
	}

	public Collection<AlternateRecipe> getAlternateRecipes() {
		return recipeEntries.values();
	}

	public Collection<ResourceItem> getResourceDrops() {
		return resourceEntries.values();
	}

	public Collection<ResourceFluid> getFluidDrops() {
		return fluidEntries.values();
	}

	public ResourceItem getResourceByID(String s) {
		return resourceEntries.get(s);
	}

	public ResourceFluid getFluidByID(String s) {
		return fluidEntries.get(s);
	}

	public AlternateRecipe getAltRecipeByID(String s) {
		return recipeEntries.get(s);
	}

	public Collection<DoggoDrop> getDoggoDrops() {
		return doggoEntries;
	}

	private static class OreLuaBlock extends LuaBlock {

		protected OreLuaBlock(String n, LuaBlock lb, LuaBlockDatabase db) {
			super(n, lb, db);

			requiredElements.add("inherit");
			requiredElements.add("name");
			requiredElements.add("spawnWeight");

			requiredElements.add("spawnLocations");
		}

	}

	private static class ResourceLuaBlock extends LuaBlock {

		protected ResourceLuaBlock(String n, LuaBlock lb, LuaBlockDatabase db) {
			super(n, lb, db);

			requiredElements.add("inherit");
			requiredElements.add("name");
			requiredElements.add("spawnWeight");

			requiredElements.add("purityLevels");
		}

	}

	private static class ResourceItemLuaBlock extends ResourceLuaBlock {

		protected ResourceItemLuaBlock(String n, LuaBlock lb, LuaBlockDatabase db) {
			super(n, lb, db);

			requiredElements.add("outputItems");
		}

	}

	private static class ResourceFluidLuaBlock extends ResourceLuaBlock {

		protected ResourceFluidLuaBlock(String n, LuaBlock lb, LuaBlockDatabase db) {
			super(n, lb, db);

			requiredElements.add("outputFluids");
		}

	}

	static class DoggoLuaBlock extends LuaBlock {

		protected DoggoLuaBlock(String n, LuaBlock lb, LuaBlockDatabase db) {
			super(n, lb, db);

			requiredElements.add("inherit");
			requiredElements.add("name");
			requiredElements.add("spawnWeight");

			requiredElements.add("findableItems");
		}

	}

	static class AltRecipeLuaBlock extends LuaBlock {

		protected AltRecipeLuaBlock(String n, LuaBlock lb, LuaBlockDatabase db) {
			super(n, lb, db);

			requiredElements.add("name");
			requiredElements.add("spawnWeight");
			requiredElements.add("recipe");
		}

	}

}
