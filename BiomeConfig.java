/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.Satisforestry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import Reika.DragonAPI.Exception.InstallationException;
import Reika.DragonAPI.IO.ReikaFileReader;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Instantiable.IO.CustomRecipeList;
import Reika.DragonAPI.Instantiable.IO.LuaBlock;
import Reika.DragonAPI.Instantiable.IO.LuaBlock.LuaBlockDatabase;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.Satisforestry.DoggoDrop.Checks;
import Reika.Satisforestry.Biome.DecoratorPinkForest.OreClusterType;
import Reika.Satisforestry.Biome.DecoratorPinkForest.OreSpawnLocation;
import Reika.Satisforestry.Blocks.BlockResourceNode.Purity;


public class BiomeConfig {

	public static final BiomeConfig instance = new BiomeConfig();

	private LuaBlockDatabase oreData;
	private LuaBlockDatabase itemData;
	private LuaBlockDatabase doggoData;

	private int definitionCount;
	private int entryAttemptsCount;
	private int entryCount;

	private final HashMap<String, OreClusterType> oreEntries = new HashMap();
	private final HashMap<String, ResourceItem> resourceEntries = new HashMap();
	private final HashMap<String, DoggoDrop> doggoEntries = new HashMap();

	private BiomeConfig() {
		oreData = new LuaBlockDatabase();
		OreLuaBlock base = new OreLuaBlock("base", null, oreData);
		base.putData("type", "base_ores");
		base.putData("block", "minecraft:iron_ore");
		//base.putData("generate", "true");
		OreLuaBlock ores = new OreLuaBlock("blocks", base, oreData);
		OreLuaBlock spawns = new OreLuaBlock("spawnLocations", base, oreData);
		for (OreSpawnLocation s : OreSpawnLocation.values()) {
			OreLuaBlock sec = new OreLuaBlock(s.name(), spawns, oreData);
			sec.putData("sizeScale", 1F);
			sec.putData("maxSize", 4);
			sec.putData("spawnWeight", 10);
			sec.setComment("maxSize", "max cluster radius");
		}
		base.setComment("block", "single block type, mutually exclusive with 'blocks'");
		base.setComment("blocks", "optional, multiple block shorthand; mutually exclusive with 'block'");
		spawns.setComment(null, "where this type can spawn, valid locations: "+OreSpawnLocation.getNameList());
		oreData.addBlock("base", base);

		itemData = new LuaBlockDatabase();
		ResourceLuaBlock base2 = new ResourceLuaBlock("base", null, itemData);
		base2.putData("type", "base_resources");
		base2.putData("minCount", 1);
		base2.putData("maxCount", 1);
		base2.putData("spawnWeight", 10);
		base2.putData("renderColor", "0xffffff");
		//base.putData("generate", "true");
		ResourceLuaBlock levels = new ResourceLuaBlock("purityLevels", base2, itemData);
		for (Purity p : Purity.list) {
			levels.putData(p.name(), p == Purity.NORMAL ? 25 : 10);
		}
		ResourceLuaBlock items = new ResourceLuaBlock("outputItems", base2, itemData);
		LuaBlock item = new ResourceLuaBlock("{", items, itemData);
		item.putData("key", "minecraft:iron_ingot");
		item.putData("weight", 10);
		item.putData("minimumPurity", Purity.IMPURE.name());
		item = new ResourceLuaBlock("{", items, itemData);
		item.putData("key", "minecraft:gold_ingot");
		item.putData("weight", 6);
		item.putData("minimumPurity", Purity.NORMAL.name());
		item.setComment("minimumPurity", "minimum purity to allow this item");
		base2.setComment("minCount", "min yield per harvest cycle");
		base2.setComment("maxCount", "max yield per harvest cycle");
		levels.setComment(null, "purity level distribution");
		itemData.addBlock("base", base2);

		doggoData = new LuaBlockDatabase();
		DoggoLuaBlock base3 = new DoggoLuaBlock("base", null, doggoData);
		base3.putData("type", "base_doggo");
		//base.putData("generate", "true");
		DoggoLuaBlock drops = new DoggoLuaBlock("findableItems", base3, doggoData);

		DoggoDrop drop = new DoggoDrop("minecraft:diamond", 1, 1, 2);
		drop.addWeightFactor(Checks.MAXY, 16, 2);
		drop.addWeightFactor(Checks.MAXY, 24, 1.5F);
		drop.addWeightFactor(Checks.MAXY, 40, 1.2F);
		drop.addCondition(Checks.HEALTH, 0.75F);
		drop.addCondition(Checks.SKY, false);
		drop.createLuaBlock(drops, doggoData);

		drop = new DoggoDrop("minecraft:slimeball", 1, 2, 15);
		drop.addCondition(Checks.BIOME, 6);
		drop.addCondition(Checks.PEACEFUL, false);
		drop.createLuaBlock(drops, doggoData);

		drop = new DoggoDrop("minecraft:bone", 1, 4, 10);
		drop.addWeightFactor(Checks.NIGHT, true, 2.5F);
		drop.createLuaBlock(drops, doggoData);

		drop = new DoggoDrop("satisforestry:paleberry", 1, 16, 50);
		drop.createLuaBlock(drops, doggoData);

		doggoData.addBlock("base", base3);
	}

	/** Returns the number of entries that loaded! */
	public void loadConfigs() {
		this.reset();
		Satisforestry.logger.log("Loading configs.");
		String sg = this.getSaveFolder();
		File f = new File(sg); //parent dir
		if (f.exists()) {
			this.loadFiles(f);
			this.parseConfigs();

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
				this.createDefaultFiles(f);;
			}
			catch (Exception e) {
				e.printStackTrace();
				Satisforestry.logger.logError("Could not create default configs!");
			}
		}
		try {
			this.createBaseFile(f);
		}
		catch (IOException e) {
			e.printStackTrace();
			Satisforestry.logger.logError("Could not create base data file!");
		}

		OreSpawnLocation.init();
	}

	private void createDefaultFiles(File folder) throws IOException {
		File f1 = new File(folder, "ores.lua");
		f1.createNewFile();
		ReikaFileReader.writeLinesToFile(f1, oreData.getBlock("base").writeToStrings(), true);

		File f2 = new File(folder, "resources.lua");
		f2.createNewFile();
		ReikaFileReader.writeLinesToFile(f2, itemData.getBlock("base").writeToStrings(), true);

		File f3 = new File(folder, "doggo.lua");
		f3.createNewFile();
		ReikaFileReader.writeLinesToFile(f3, doggoData.getBlock("base").writeToStrings(), true);
	}

	private void createBaseFile(File f) throws IOException {
		File out = new File(f, "base.lua");
		if (out.exists())
			out.delete();
		out.createNewFile();
		ArrayList<String> li = oreData.getBlock("base").writeToStrings();
		li.set(li.size()-1, li.get(li.size()-1)+",");
		li.addAll(itemData.getBlock("base").writeToStrings());
		li.set(li.size()-1, li.get(li.size()-1)+",");
		li.addAll(doggoData.getBlock("base").writeToStrings());
		ReikaFileReader.writeLinesToFile(out, li, true);
	}

	private void reset() {
		LuaBlock base = oreData.getBlock("base");
		LuaBlock base2 = itemData.getBlock("base");
		LuaBlock base3 = doggoData.getBlock("base");

		oreData = new LuaBlockDatabase();
		itemData = new LuaBlockDatabase();
		doggoData = new LuaBlockDatabase();

		oreEntries.clear();
		resourceEntries.clear();
		doggoEntries.clear();

		definitionCount = 0;
		entryAttemptsCount = 0;
		entryCount = 0;

		oreData.addBlock("base", base);
		itemData.addBlock("base", base2);
		doggoData.addBlock("base", base3);
	}

	private void loadFiles(File parent) {
		File f1 = ReikaFileReader.getFileByNameAnyExt(parent, "ores");
		File f2 = ReikaFileReader.getFileByNameAnyExt(parent, "resources");
		File f3 = ReikaFileReader.getFileByNameAnyExt(parent, "doggo");
		if (f2 == null || !f2.exists()) {
			throw new InstallationException(Satisforestry.instance, "No resource config file found!");
		}
		itemData.loadFromFile(f2);

		if (f1.exists())
			oreData.loadFromFile(f1);
		else
			Satisforestry.logger.log("No ore config file found; no ore clusters will generate.");

		if (f3.exists())
			doggoData.loadFromFile(f3);
		else
			Satisforestry.logger.log("No doggo config file found; doggos will not find items.");
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
	}

	private void parseOreEntry(String type, LuaBlock b) throws NumberFormatException, IllegalArgumentException, IllegalStateException {
		ArrayList<String> blocks = new ArrayList();
		HashMap<OreSpawnLocation, LuaBlock> sections = new HashMap();

		LuaBlock set = b.getChild("blocks");
		if (set != null) {
			for (String s : set.getDataValues()) {
				blocks.add(s);
			}
		}
		else {
			blocks.add(b.getString("block"));
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

		for (String s : blocks) {
			BlockKey bk = this.parseBlockKey(s);
			if (bk == null) {
				Satisforestry.logger.logError("Could not load block type '"+s+"' for ore type '"+type+"'; skipping.");
				continue;
			}
			for (Entry<OreSpawnLocation, LuaBlock> e : sections.entrySet()) {
				OreSpawnLocation cs = e.getKey();
				LuaBlock data = e.getValue();
				String id = type+"_"+s+"_"+cs.name();
				OreClusterType ore = new OreClusterType(id, bk, cs, b.getInt("spawnWeight"));
				ore.sizeScale = (float)data.getDouble("sizeScale");
				ore.maxDepth = data.getInt("maxSize");
				oreEntries.put(id, ore);
				Satisforestry.logger.log("Registered ore type '"+type+"' with block '"+bk+" for area "+cs);
				entryCount++;
			}
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

		ResourceItem ore = new ResourceItem(type, b.getInt("spawnWeight"), b.getInt("renderColor"), map);
		ore.minCount = b.getInt("minCount");
		ore.maxCount = b.getInt("maxCount");

		for (LuaBlock s : items) {
			entryAttemptsCount++;
			ItemStack is = CustomRecipeList.parseItemString(s.getString("key"), s.getChild("nbt"), true);
			if (is == null) {
				Satisforestry.logger.logError("Could not load item type '"+s+"' for resource type '"+type+"'; skipping.");
				continue;
			}
			int weight = s.getInt("weight");
			Purity p = Purity.valueOf(s.getString("minimumPurity"));
			while (p != null) {
				ore.addItem(p, is, weight);
				p = p.higher();
			}
		}

		resourceEntries.put(type, ore);
		Satisforestry.logger.log("Registered resource type '"+type+"': "+ore.toString().replaceAll("\\\\n", " "));
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

	private final String getSaveFolder() {
		return Satisforestry.config.getConfigFolder().getAbsolutePath()+"/Satisforestry_Files/";
	}

	public Collection<OreClusterType> getOreTypes() {
		return oreEntries.values();
	}

	public Collection<ResourceItem> getResourceDrops() {
		return resourceEntries.values();
	}

	public ResourceItem getResourceByID(String s) {
		return resourceEntries.get(s);
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
			requiredElements.add("outputItems");
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

}
