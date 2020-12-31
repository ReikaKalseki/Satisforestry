package Reika.Satisforestry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;

import Reika.DragonAPI.Instantiable.IO.LuaBlock;
import Reika.DragonAPI.Instantiable.IO.LuaBlock.LuaBlockDatabase;
import Reika.Satisforestry.BiomeConfig.DoggoLuaBlock;
import Reika.Satisforestry.Entity.EntityLizardDoggo;

public class DoggoDrop {

	public final String itemKey;
	public final int minCount;
	public final int maxCount;
	public final int baseWeight;

	private final ArrayList<Condition> requirements = new ArrayList();
	private final HashMap<Condition, Float> weightFactors = new HashMap();

	public DoggoDrop(String item, int min, int max, int wt) {
		itemKey = item;
		minCount = min;
		maxCount = max;
		baseWeight = wt;
	}

	public LuaBlock createLuaBlock(LuaBlock parent, LuaBlockDatabase tree) {
		LuaBlock lb = new DoggoLuaBlock("{", parent, tree);
		lb.putData("key", itemKey);
		lb.putData("minCount", minCount);
		lb.putData("maxCount", maxCount);
		lb.putData("weight", baseWeight);
		if (!requirements.isEmpty()) {
			DoggoLuaBlock reqs = new DoggoLuaBlock("limits", lb, tree);
			for (Condition c : requirements) {
				LuaBlock item = new DoggoLuaBlock("{", reqs, tree);
				item.putData("check", c.check.key);
				item.putData("value", String.valueOf(c.value));
			}
		}
		if (!weightFactors.isEmpty()) {
			DoggoLuaBlock reqs = new DoggoLuaBlock("weightFactors", lb, tree);
			for (Entry<Condition, Float> en : weightFactors.entrySet()) {
				Condition c = en.getKey();
				LuaBlock item = new DoggoLuaBlock("{", reqs, tree);
				item.putData("check", c.check.key);
				item.putData("value", String.valueOf(c.value));
				item.putData("factor", en.getValue());
			}
		}
		return lb;
	}

	public float getNetWeight(EntityLizardDoggo e) {
		for (Condition c : requirements) {
			if (!c.check.evaluate(e, c.value))
				return 0;
		}
		float val = baseWeight;
		for (Entry<Condition, Float> en : weightFactors.entrySet()) {
			Condition c = en.getKey();
			if (c.check.evaluate(e, c.value)) {
				val *= en.getValue();
			}
		}
		return val;
	}

	public void addWeightFactor(Checks c, Object req, float f) {
		weightFactors.put(new Condition(c, req), f);
	}

	public void addCondition(Checks c, Object req) {
		requirements.add(new Condition(c, req));
	}

	private static class Condition {

		public final Checks check;
		public final Object value;

		private Condition(Checks c, Object req) {
			check = c;
			value = req;
		}

	}

	public static enum Checks {
		NIGHT("is_night"),
		BIOME("biome_id"),
		HEALTH("doggo_min_health"),
		MAXY("max_y"),
		PEACEFUL("is_peaceful"),
		SKY("has_sky"),
		;

		public final String key;

		private Checks(String s) {
			key = s;
		}

		public boolean evaluate(EntityLizardDoggo e, Object val) {
			switch(this) {
				case BIOME:
					return e.worldObj.getBiomeGenForCoords(MathHelper.floor_double(e.posX), MathHelper.floor_double(e.posZ)) == val;
				case HEALTH:
					return e.getHealth() >= e.getMaxHealth()*(float)val;
				case MAXY:
					return e.posY <= (double)val;
				case NIGHT:
					return e.worldObj.isDaytime() != (boolean)val;
				case PEACEFUL:
					return (e.worldObj.difficultySetting == EnumDifficulty.PEACEFUL) == ((boolean)val == true);
				case SKY:
					return e.worldObj.canBlockSeeTheSky(MathHelper.floor_double(e.posX), (int)e.posY+1, MathHelper.floor_double(e.posZ));
			}
			return false;
		}

		public Object parseReq(String input) {
			switch(this) {
				case NIGHT:
				case PEACEFUL:
				case SKY:
					return Boolean.parseBoolean(input);
				case BIOME:
				case MAXY:
					return Integer.parseInt(input);
				case HEALTH:
					return Float.parseFloat(input);
			}
			return null;
		}
	}

}
