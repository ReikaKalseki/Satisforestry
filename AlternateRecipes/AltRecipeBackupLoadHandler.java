/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.Satisforestry.AlternateRecipes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.DimensionManager;

import Reika.DragonAPI.Auxiliary.Trackers.PlayerHandler.PlayerTracker;
import Reika.DragonAPI.IO.ReikaFileReader;
import Reika.DragonAPI.Instantiable.Data.Maps.PlayerMap;
import Reika.DragonAPI.Instantiable.IO.NBTFile;
import Reika.DragonAPI.Libraries.ReikaPlayerAPI;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Config.AlternateRecipe;
import Reika.Satisforestry.Config.BiomeConfig;

import cpw.mods.fml.common.FMLLog;


public class AltRecipeBackupLoadHandler implements PlayerTracker {

	public static final AltRecipeBackupLoadHandler instance = new AltRecipeBackupLoadHandler();

	private String baseFilepath;

	private final HashMap<UUID, RecipeCache> cache = new HashMap();

	private final PlayerMap<NBTTagCompound> cachedBackup = new PlayerMap();

	private AltRecipeBackupLoadHandler() {

	}

	public void initLevelData(MinecraftServer instance) {
		baseFilepath = DimensionManager.getCurrentSaveRootDirectory()+"/Satisforestry_Data/RecipeCache/";
		cache.clear();
	}

	private final String getFilepath(EntityPlayer ep) {
		return this.getFilepath(ep.getUniqueID());
	}

	private final String getFilepath(UUID uid) {
		return baseFilepath+uid.toString()+".dat";
	}

	public void load() {
		File f = new File(baseFilepath);
		if (f.exists()) {
			ArrayList<File> li = ReikaFileReader.getAllFilesInFolder(f, ".dat");
			for (File in : li) {
				RecipeCache pc = RecipeCache.readFromFile(in);
				if (pc != null) {
					cache.put(pc.uid, pc);
				}
			}
		}
		else {
			f.getParentFile().mkdirs();
			f.mkdir();
		}
	}

	public void saveAll() {
		ReikaFileReader.emptyDirectory(new File(baseFilepath));
		for (RecipeCache pc : cache.values()) {
			File f = new File(this.getFilepath(pc.uid));
			pc.writeToFile(f);
		}
	}

	private void savePlayer(EntityPlayer ep) {
		RecipeCache pc = cache.get(ep.getUniqueID());
		if (pc != null) {
			File f = new File(this.getFilepath(ep.getUniqueID()));
			ReikaFileReader.clearFile(f);
			pc.writeToFile(f);
		}
	}

	@Override
	public void onPlayerLogin(EntityPlayer player) {
		this.updateRecipeCache(player);
	}

	@Override
	public void onPlayerLogout(EntityPlayer player) {
		this.updateRecipeCache(player);
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player, int dimFrom, int dimTo) {
		this.updateRecipeCache(player);
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {
		this.updateRecipeCache(player);
	}

	public void clearRecipeCache(EntityPlayer ep) {
		cache.remove(ep.getUniqueID());
	}

	public void updateRecipeCache(EntityPlayer ep) {
		RecipeCache pc = cache.get(ep.getUniqueID());
		if (pc == null) {
			pc = new RecipeCache(ep);
			cache.put(ep.getUniqueID(), pc);
		}
		else {
			if (pc.hasMoreProgressionThan(ep)) { //recipe data lost?
				FMLLog.bigWarning("Satisforestry: Player %s just lost some of their recipe data!", ep.getCommandSenderName());
			}
			pc.update(ep);
		}
		this.savePlayer(ep);
	}

	public NBTTagCompound attemptToLoadBackup(EntityPlayer ep) {
		if (!ReikaPlayerAPI.isFake(ep))
			Satisforestry.logger.log("Attempting to load backup recipe data for "+ep);
		NBTTagCompound tag = cachedBackup.get(ep);
		if (tag == null) {
			File f = this.getBackupFile(ep);
			if (f.exists() && f.length() > 0) {
				try {
					tag = ReikaFileReader.readUncompressedNBT(f);
					cachedBackup.put(ep, tag);
				}
				catch (Exception e) {
					e.printStackTrace();
					Satisforestry.logger.logError("Could not read recipe data backup for "+ep.getCommandSenderName()+"!");
				}
			}
		}
		return tag;
	}

	public void updateBackup(EntityPlayer ep) {
		File f = this.getBackupFile(ep);
		if (!f.exists())
			f.getParentFile().mkdirs();
		if (f.exists())
			f.delete();
		try {
			f.createNewFile();
			cachedBackup.put(ep, ReikaPlayerAPI.getDeathPersistentNBT(ep));
			ReikaFileReader.writeUncompressedNBT(ReikaPlayerAPI.getDeathPersistentNBT(ep), f);
		}
		catch (IOException e) {
			e.printStackTrace();
			Satisforestry.logger.logError("Could not save recipe data backup for "+ep.getCommandSenderName()+"!");
		}
	}

	private File getBackupFile(EntityPlayer ep) {
		return new File(DimensionManager.getCurrentSaveRootDirectory()+"/Satisforestry_Data/RecipeBackup", ep.getUniqueID().toString()+".dat");
	}

	public static class RecipeCache {

		private final HashSet<AlternateRecipe> cache = new HashSet();
		private final UUID uid;

		private RecipeCache(EntityPlayer ep) {
			this(ep.getUniqueID());
			this.update(ep);
		}

		private boolean hasMoreProgressionThan(EntityPlayer ep) {
			for (AlternateRecipe p : new HashSet<AlternateRecipe>(cache)) { //CME protection
				if (!p.playerHas(ep.worldObj, ep.getUniqueID()))
					return true;
			}
			return false;
		}

		private RecipeCache(UUID id) {
			uid = id;
		}

		public void update(EntityPlayer ep) {
			cache.clear();
			cache.addAll(AlternateRecipeManager.instance.getRecipesFor(ep));
			//ReikaJavaLibrary.pConsole(cache);
		}

		public boolean containsProgress(AlternateRecipe p) {
			return cache.contains(p);
		}

		private static RecipeCache readFromFile(File f) {
			RecipeListFile pf = new RecipeListFile(f);
			try {
				pf.load();
				RecipeCache pc = new RecipeCache(pf.uid);
				for (String s : pf.entries) {
					AlternateRecipe p = BiomeConfig.instance.getAltRecipeByID(s);
					if (p == null) {
						Satisforestry.logger.logError("Player had a cached recipe that no longer exists: "+s);
						continue;
					}
					pc.cache.add(p);
				}
				return pc;
			}
			catch (Exception e) {
				Satisforestry.logger.logError("Could not load cached player recipe: "+f.getName());
				e.printStackTrace();
				return null;
			}
		}

		private void writeToFile(File f) {
			RecipeListFile pf = new RecipeListFile(f);
			pf.uid = uid;
			for (AlternateRecipe p : cache) {
				pf.entries.add(p.id);
			}
			try {
				pf.save();
			}
			catch (Exception e) {
				Satisforestry.logger.logError("Could not save cached player recipe: "+f.getName());
				e.printStackTrace();
			}
		}

		@Override
		public String toString() {
			return uid+": "+cache.toString();
		}

	}

	private static class RecipeListFile extends NBTFile {

		private UUID uid;
		private final HashSet<String> entries = new HashSet();

		private RecipeListFile(File f) {
			super(f);
			encryptData = true;
		}

		@Override
		protected void readHeader(NBTTagCompound header) {
			uid = UUID.fromString(header.getString("id"));
		}

		@Override
		protected void readData(NBTTagList li) {
			for (Object o : li.tagList) {
				String s = ((NBTTagCompound)o).getString("tag");
				entries.add(s);
			}
		}

		@Override
		protected void readExtraData(NBTTagCompound extra) {

		}

		@Override
		protected void writeHeader(NBTTagCompound header) {
			header.setString("id", uid.toString());
		}

		@Override
		protected void writeData(NBTTagList li) {
			for (String s : entries) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setString("tag", s);
				li.appendTag(tag);
			}
		}

		@Override
		protected NBTTagCompound writeExtraData() {
			return null;
		}

	}

}
