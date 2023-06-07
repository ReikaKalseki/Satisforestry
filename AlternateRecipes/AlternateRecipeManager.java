package Reika.Satisforestry.AlternateRecipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumChatFormatting;

import Reika.DragonAPI.Auxiliary.Trackers.TickScheduler;
import Reika.DragonAPI.Instantiable.Event.ScheduledTickEvent;
import Reika.DragonAPI.Instantiable.Event.ScheduledTickEvent.ScheduledStringPacket;
import Reika.DragonAPI.Instantiable.IO.PacketTarget;
import Reika.DragonAPI.Libraries.ReikaNBTHelper;
import Reika.DragonAPI.Libraries.ReikaNBTHelper.NBTTypes;
import Reika.DragonAPI.Libraries.ReikaPlayerAPI;
import Reika.DragonAPI.Libraries.IO.ReikaChatHelper;
import Reika.Satisforestry.SFPacketHandler.SFPackets;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.API.AltRecipe;
import Reika.Satisforestry.API.SFAPI.AltRecipeHandler;
import Reika.Satisforestry.Config.AlternateRecipe;
import Reika.Satisforestry.Config.BiomeConfig;

public class AlternateRecipeManager implements AltRecipeHandler {

	public static final String MAIN_NBT_TAG = "Satisforestry_Recipes";

	public static final AlternateRecipeManager instance = new AlternateRecipeManager();

	private final HashMap<String, AlternateRecipe> apiAlternates = new HashMap();

	private AlternateRecipeManager() {

	}

	public void setRecipeStatus(EntityPlayerMP ep, AlternateRecipe s, boolean set, boolean notify) {
		if (set && notify) {
			ReikaChatHelper.sendChatToAllOnServer(EnumChatFormatting.GOLD+ep.getCommandSenderName()+EnumChatFormatting.RESET+" just unlocked "+EnumChatFormatting.LIGHT_PURPLE+s.getDisplayName());
			TickScheduler.instance.scheduleEvent(new ScheduledTickEvent(new ScheduledStringPacket(Satisforestry.packetChannel, SFPackets.ALTRECIPEUNLOCK.ordinal(), new PacketTarget.PlayerTarget(ep), s.getID())), 15);
		}
		NBTTagList li = this.getNBTList(ep);
		NBTBase tag = new NBTTagString(s.id);
		boolean flag = false;
		if (set) {
			if (!li.tagList.contains(tag)) {
				flag = true;
				li.appendTag(tag);
			}
		}
		else {
			if (li.tagList.contains(tag)) {
				flag = true;
				li.tagList.remove(tag);
			}
		}
		if (flag) {
			this.getRootNBTTag(ep).setTag(MAIN_NBT_TAG, li);
			ReikaPlayerAPI.syncCustomData(ep);
			AltRecipeBackupLoadHandler.instance.updateRecipeCache(ep);
			AltRecipeBackupLoadHandler.instance.updateBackup(ep);
		}
	}

	public Collection<AlternateRecipe> getPlayerRecipeData(EntityPlayer ep) {
		NBTTagList li = this.getNBTList(ep);
		Collection<AlternateRecipe> c = new HashSet();
		Iterator<NBTTagString> it = li.tagList.iterator();
		while (it.hasNext()) {
			String val = it.next().func_150285_a_();
			AlternateRecipe p = BiomeConfig.instance.getAltRecipeByID(val);
			if (p == null) {
				Satisforestry.logger.logError("Could not load alternate recipe from NBT String "+val+"; was it removed?");
				it.remove();
			}
			else {
				c.add(p);
			}
		}
		return c;
	}

	private NBTTagList getNBTList(EntityPlayer ep) {
		NBTTagCompound nbt = this.getRootNBTTag(ep);
		if (nbt == null) {
			Satisforestry.logger.logError("Looking for recipe data on player "+ep.getCommandSenderName()+", with no NBT?!");
			return new NBTTagList();
		}
		if (!nbt.hasKey(MAIN_NBT_TAG))
			nbt.setTag(MAIN_NBT_TAG, new NBTTagList());
		NBTTagList li = nbt.getTagList(MAIN_NBT_TAG, NBTTypes.STRING.ID);
		return li;
	}

	public NBTTagCompound getRootNBTTag(EntityPlayer ep) {
		NBTTagCompound tag = ReikaPlayerAPI.isFake(ep) ? null : ReikaPlayerAPI.getDeathPersistentNBT(ep);
		if (tag == null || tag.hasNoTags()) {
			NBTTagCompound repl = AltRecipeBackupLoadHandler.instance.attemptToLoadBackup(ep);
			if (repl != null) {
				if (tag == null)
					tag = new NBTTagCompound();
				ReikaNBTHelper.copyNBT(repl, tag);
			}
		}
		return tag;
	}

	public Collection<AlternateRecipe> getRecipesFor(EntityPlayer ep) {
		Collection<AlternateRecipe> c = this.getPlayerRecipeData(ep);
		return c != null ? Collections.unmodifiableCollection(c) : new ArrayList();
	}

	@Override
	public AltRecipe getRecipeByID(String id) {
		return BiomeConfig.instance.getAltRecipeByID(id);
	}

	@Override
	public Set<String> getRecipeIDs() {
		HashSet<String> set = new HashSet();
		for (AlternateRecipe r : BiomeConfig.instance.getAlternateRecipes()) {
			set.add(r.id);
		}
		return set;
	}

	@Override
	public AltRecipe addAltRecipe(String id, String displayName, int spawnWeight, IRecipe recipe, ItemStack requiredUnlock, String unlockPowerType, long powerAmount, long ticksFor) {
		if (recipe == null)
			throw new IllegalArgumentException("Could not accept alt recipe '"+id+"': Recipe may not be null");
		AlternateRecipe added = BiomeConfig.instance.addAlternateRecipe(id, displayName, spawnWeight, recipe, requiredUnlock, unlockPowerType, powerAmount, ticksFor);
		apiAlternates.put(added.id, added);
		return added;
	}

	public void refreshAPIAlternates() {
		for (AlternateRecipe ar : apiAlternates.values()) {
			BiomeConfig.instance.readAlternateRecipe(ar);
		}
	}

	@Override
	public String getCompactedCoalID() {
		return BiomeConfig.COMPACTED_COAL_ID;
	}

	@Override
	public String getTurbofuelID() {
		return BiomeConfig.TURBOFUEL_ID;
	}

}
