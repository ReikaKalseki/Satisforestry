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

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import Reika.DragonAPI.IO.ReikaFileReader;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Instantiable.IO.CustomRecipeList;
import Reika.DragonAPI.Instantiable.IO.LuaBlock;
import Reika.DragonAPI.Instantiable.IO.LuaBlock.LuaBlockDatabase;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;


public class BiomeConfig {

	public static final BiomeConfig instance = new BiomeConfig();

	private LuaBlockDatabase oreData;
	private LuaBlockDatabase itemData;

	private int definitionCount;
	private int entryAttemptsCount;
	private int entryCount;

	private final HashMap<String, OreClusterType> oreEntries = new HashMap();
	private final HashMap<String, ResourceItem> resourceEntries = new HashMap();

	private BiomeConfig() {
		oreData = new LuaBlockDatabase();
		OreLuaBlock base = new OreLuaBlock("base", null, oreData);
		base.putData("type", "base");
		base.putData("sizeScale", "1");
		base.putData("maxSize", "4");
		base.putData("ringSpawn", "true");
		base.putData("spawnWeight", "10");
		base.putData("block", "some_mod:some_ore");
		//base.putData("generate", "true");
		OreLuaBlock ores = new OreLuaBlock("blocks", base, oreData);
		oreData.addBlock("base", base);

		itemData = new LuaBlockDatabase();
		ResourceLuaBlock base2 = new ResourceLuaBlock("base", null, itemData);
		base2.putData("type", "base");
		base2.putData("minCount", "1");
		base2.putData("maxCount", "1");
		base2.putData("spawnWeight", "10");
		base2.putData("item", "some_item_identifier");
		//base.putData("generate", "true");
		ResourceLuaBlock items = new ResourceLuaBlock("items", base2, itemData);
		itemData.addBlock("base", base2);
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
				File f1 = new File(f, "ores.lua");
				File f2 = new File(f, "resources.lua");
				f1.createNewFile();
				f2.createNewFile();
			}
			catch (Exception e) {
				e.printStackTrace();
				Satisforestry.logger.logError("Could not create ore config folder!");
			}
		}
		try {
			this.createBaseFile(f);
		}
		catch (IOException e) {
			e.printStackTrace();
			Satisforestry.logger.logError("Could not create base data file!");
		}
	}

	private void createBaseFile(File f) throws IOException {
		File out = new File(f, "base.lua");
		if (out.exists())
			out.delete();
		out.createNewFile();
		ArrayList<String> li = oreData.getBlock("base").writeToStrings();
		li.set(li.size()-1, li.get(li.size()-1)+",");
		li.addAll(itemData.getBlock("base").writeToStrings());
		ReikaFileReader.writeLinesToFile(out, li, true);
	}

	private void reset() {
		LuaBlock base = oreData.getBlock("base");
		LuaBlock base2 = itemData.getBlock("base");
		oreData = new LuaBlockDatabase();
		itemData = new LuaBlockDatabase();

		oreEntries.clear();

		definitionCount = 0;
		entryAttemptsCount = 0;
		entryCount = 0;

		oreData.addBlock("base", base);
		itemData.addBlock("base", base2);
	}

	private void loadFiles(File parent) {
		File f = new File(parent, "ores.lua");
		File f2 = new File(parent, "resources.lua");
		if (f.exists())
			oreData.loadFromFile(f);
		if (f2.exists())
			itemData.loadFromFile(f2);
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
	}

	private void parseOreEntry(String type, LuaBlock b) throws NumberFormatException, IllegalArgumentException, IllegalStateException {
		ArrayList<String> blocks = new ArrayList();

		LuaBlock set = b.getChild("blocks");
		if (set != null) {
			for (String s : set.getDataValues()) {
				blocks.add(s);
			}
		}
		else {
			blocks.add(b.getString("block"));
		}

		for (String s : blocks) {
			entryAttemptsCount++;
			BlockKey bk = this.parseBlockKey(s);
			if (bk == null) {
				Satisforestry.logger.logError("Could not load block type '"+s+"' for ore type '"+type+"'; skipping.");
				continue;
			}
			String id = type+"_"+s;
			OreClusterType ore = new OreClusterType(id, bk, b.getInt("spawnWeight"));
			ore.sizeScale = (float)b.getDouble("sizeScale");
			ore.maxDepth = b.getInt("maxSize");
			ore.canSpawnInMainRing = b.getBoolean("ringSpawn");
			oreEntries.put(type, ore);
			Satisforestry.logger.log("Registered ore type '"+type+"' with block '"+bk);
			entryCount++;
		}
	}

	private void parseResourceEntry(String type, LuaBlock b) throws NumberFormatException, IllegalArgumentException, IllegalStateException {
		ArrayList<LuaBlock> items = new ArrayList();

		LuaBlock set = b.getChild("items");
		if (set != null) {
			for (LuaBlock s : set.getChildren()) {
				items.add(s);
			}
		}
		else {
			items.add(b.getChild("item"));
		}

		for (LuaBlock s : items) {
			entryAttemptsCount++;
			ItemStack is = CustomRecipeList.parseItemString(s.getString("key"), s.getChild("nbt"), true);
			if (is == null) {
				Satisforestry.logger.logError("Could not load item type '"+s+"' for resource type '"+type+"'; skipping.");
				continue;
			}
			String id = type+"_"+s;
			ResourceItem ore = new ResourceItem(id, is, b.getInt("spawnWeight"));
			ore.minCount = b.getInt("minCount");
			ore.maxCount = b.getInt("maxCount");
			resourceEntries.put(type, ore);
			Satisforestry.logger.log("Registered resource type '"+type+"' with item '"+CustomRecipeList.fullID(is));
			entryCount++;
		}
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

	private static class OreLuaBlock extends LuaBlock {

		protected OreLuaBlock(String n, LuaBlock lb, LuaBlockDatabase db) {
			super(n, lb, db);

			requiredElements.add("inherit");
			requiredElements.add("name");
			requiredElements.add("spawnWeight");
		}

	}

	private static class ResourceLuaBlock extends LuaBlock {

		protected ResourceLuaBlock(String n, LuaBlock lb, LuaBlockDatabase db) {
			super(n, lb, db);

			requiredElements.add("inherit");
			requiredElements.add("name");
			requiredElements.add("spawnWeight");
		}

	}

}
