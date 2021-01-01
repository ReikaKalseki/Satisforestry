package Reika.Satisforestry.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;

import Reika.DragonAPI.Instantiable.Data.WeightedRandom;
import Reika.DragonAPI.Instantiable.IO.LuaBlock;
import Reika.Satisforestry.Blocks.BlockResourceNode.Purity;

public class ResourceItem {

	public final String id;
	public final int color;
	public final int spawnWeight;

	private final WeightedRandom<Purity> levels = new WeightedRandom();
	private final HashMap<Purity, WeightedRandom<ItemStack>> items = new HashMap();
	private final ArrayList<NodeEffect> effects = new ArrayList();

	public int minCount = 1;
	public int maxCount = 1;

	public ResourceItem(String s, int w, int c, HashMap<String, Object> map) {
		id = s;
		spawnWeight = w;
		color = c;
		for (Entry<String, Object> e : map.entrySet()) {
			levels.addEntry(Purity.valueOf(e.getKey()), (Integer)e.getValue());
		}
	}

	public void addItem(Purity p, ItemStack is, int weight) {
		WeightedRandom<ItemStack> wr = items.get(p);
		if (wr == null) {
			wr = new WeightedRandom();
			items.put(p, wr);
		}
		wr.addEntry(is.copy(), weight);
	}

	public void addEffect(LuaBlock b) {
		String key = b.getString("effectType");
		EffectTypes t = EffectTypes.getByKey(key);
		if (t == null)
			throw new IllegalArgumentException("Invalid effect type '"+key+"'");
		NodeEffect e = new NodeEffect(t, b.asHashMap());
		effects.add(e);
	}

	public ItemStack getRandomItem(Purity p) {
		WeightedRandom<ItemStack> wr = items.get(p);
		return wr == null ? null : wr.getRandomEntry();
	}

	public Purity getRandomPurity(Random rand) {
		levels.setRNG(rand);
		return levels.getRandomEntry();
	}

	public Collection<NodeEffect> getEffects() {
		return Collections.unmodifiableCollection(effects);
	}

	@Override
	public String toString() {
		return "W="+spawnWeight+", C="+Integer.toHexString(color)+", L="+levels.toString()+", I="+items.toString();
	}

	public static class NodeEffect {

		public final EffectTypes type;
		private final HashMap<String, Object> data;

		private NodeEffect(EffectTypes e, HashMap<String, Object> map) {
			type = e;
			data = map;
		}

		public void apply(EntityPlayer ep) {
			type.apply(ep, data);
		}

	}

	public static enum EffectTypes {
		DAMAGE("damage"),
		POTION("potion"),
		;

		public final String key;
		public final String comment;

		private static final HashMap<String, EffectTypes> keyMap = new HashMap();

		private EffectTypes(String s) {
			this(s, null);
		}

		private EffectTypes(String s, String c) {
			key = s;
			comment = c;
		}

		public void apply(EntityPlayer ep, HashMap<String, Object> data) {
			switch(this) {
				case DAMAGE:
					if (ep.ticksExisted%(int)data.get("rate") == 0) {
						float amt = ((Double)data.get("amount")).floatValue();
						if (amt > 0)
							ep.attackEntityFrom(DamageSource.generic, amt);
						else if (amt < 0)
							ep.heal(amt);
					}
					break;
				case POTION:
					Potion p = Potion.potionTypes[(int)data.get("potionID")];
					if (!ep.isPotionActive(p))
						ep.addPotionEffect(new PotionEffect(p.id, 20, (int)data.get("level")-1));
					break;
			}
		}

		static {
			for (EffectTypes c : values()) {
				keyMap.put(c.key, c);
			}
		}

		public static EffectTypes getByKey(String s) {
			return keyMap.get(s);
		}

		public static String getNameList() {
			StringBuilder sb = new StringBuilder();
			EffectTypes[] list = values();
			for (int i = 0; i < list.length; i++) {
				EffectTypes loc = list[i];
				sb.append(loc.name());
				if (i < list.length-1)
					sb.append(", ");
			}
			return sb.toString();
		}
	}

}
