package Reika.Satisforestry.Config;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.item.ItemStack;

import Reika.DragonAPI.Instantiable.Data.WeightedRandom;
import Reika.Satisforestry.Blocks.BlockResourceNode.Purity;

public class ResourceItem {

	public final String id;
	public final int color;
	public final int spawnWeight;

	private final WeightedRandom<Purity> levels = new WeightedRandom();
	private final HashMap<Purity, WeightedRandom<ItemStack>> items = new HashMap();

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

	public ItemStack getRandomItem(Purity p) {
		WeightedRandom<ItemStack> wr = items.get(p);
		return wr == null ? null : wr.getRandomEntry();
	}

	public Purity getRandomPurity(Random rand) {
		levels.setRNG(rand);
		return levels.getRandomEntry();
	}

	@Override
	public String toString() {
		return "W="+spawnWeight+", C="+Integer.toHexString(color)+", L="+levels.toString()+", I="+items.toString();
	}

}
