package Reika.Satisforestry.Config;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Random;

import com.google.common.base.Strings;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.WeightedRandom;
import Reika.DragonAPI.Instantiable.Data.WeightedRandom.DynamicWeight;
import Reika.DragonAPI.Instantiable.IO.LuaBlock;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.Java.ReikaStringParser;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.API.NodeEffectCallback;

public abstract class NodeResource<I> {

	public final String id;
	public final String displayName;
	public final int color;
	public final int spawnWeight;

	private final WeightedRandom<Purity> levels = new WeightedRandom();
	private final HashMap<Purity, WeightedRandom<NodeItem>> items = new HashMap();
	private final ArrayList<NodeEffect> effects = new ArrayList();

	public float speedFactor = 1;
	private float peacefulYieldScale = 1;

	public NodeResource(String s, String n, int w, int c, HashMap<String, Object> map) {
		id = s;
		displayName = Strings.isNullOrEmpty(n) || n.contains("NULL KEY") ? id : n;
		spawnWeight = w;
		color = c;
		for (Entry<String, Object> e : map.entrySet()) {
			levels.addEntry(Purity.valueOf(e.getKey()), (Integer)e.getValue());
		}
	}

	public int maxItemsPerType() {
		return Integer.MAX_VALUE;
	}

	public final void addItem(Purity p, I is, int weight, int min, int max, LuaBlock data) {
		if (items.size() >= this.maxItemsPerType())
			throw new IllegalStateException("Max item type limit exceeded when trying to add "+is);
		WeightedRandom<NodeItem> wr = items.get(p);
		if (wr == null) {
			wr = new WeightedRandom();
			items.put(p, wr);
		}
		NodeItem ni = new NodeItem(is, weight, min, max);
		ni.loadData(data);
		wr.addDynamicEntry(ni);
	}

	public final void addEffect(LuaBlock b) {
		String key = b.getString("effectType");
		EffectTypes t = EffectTypes.getByKey(key);
		if (t == null)
			throw new IllegalArgumentException("Invalid effect type '"+key+"'");
		NodeEffect e = new NodeEffect(t, t.parseData(b));
		effects.add(e);
	}

	public final NodeItem getRandomItem(int tier, Purity p, boolean manual) {
		WeightedRandom<NodeItem> wr = items.get(p);
		if (wr == null)
			return null;
		for (NodeItem i : wr.getValues())
			i.calc(tier, p, manual);
		return wr.getRandomEntry();
	}

	public final Purity getRandomPurity(Random rand) {
		levels.setRNG(rand);
		return levels.getRandomEntry();
	}

	public final Collection<NodeEffect> getEffects() {
		return Collections.unmodifiableCollection(effects);
	}

	public final boolean worksOnPeaceful() {
		return peacefulYieldScale > 0;
	}

	public final boolean hasNoItems() {
		return items.isEmpty();
	}

	public final HashMap<I, Double> getItemSet(Purity p) {
		HashMap<I, Double> ret = new HashMap();
		WeightedRandom<NodeItem> wr = items.get(p);
		for (NodeItem is : wr.getValues()) {
			is.calc(Integer.MAX_VALUE, p, false);
			ret.put(this.getItem(is), is.getWeight());
		}
		return ret;
	}

	public final Collection<ResourceItemView> getAllItems(Purity p) {
		Collection<ResourceItemView> ret = new ArrayList();
		for (NodeItem ni : items.get(p).getValues()) {
			ret.add(this.getView(ni, p));
		}
		return ret;
	}

	protected ResourceItemView getView(NodeItem ni, Purity p) {
		return new ResourceItemView(ni, p);
	}

	public final boolean produces(I is) {
		for (Purity p : Purity.list) {
			if (this.producesAt(is, p))
				return true;
		}
		return false;
	}

	public final boolean producesAt(I is, Purity p) {
		for (NodeItem ni : items.get(p).getValues()) {
			if (this.matchItem(ni, is)) {
				return true;
			}
		}
		return false;
	}

	public abstract I getItem(NodeItem obj);
	public abstract boolean matchItem(NodeItem obj, I is);

	public final I getItem(ResourceItemView obj) {
		return this.getItem(obj.item);
	}

	@Override
	public final String toString() {
		return "W="+spawnWeight+", C="+Integer.toHexString(color)+", L="+levels.toString()+", I="+items.toString();
	}

	public static enum Purity {
		IMPURE(0.5F),
		NORMAL(1),
		PURE(2);

		public final float yield;

		private static final int MINING_COOLDOWN = 10; //default resource node is 120/min = 2/s = 10t
		public static final Purity[] list = values();

		private Purity(float d) {
			yield = d;
		}

		public String getDisplayName() {
			if (this == NORMAL)
				return "Normal Purity";
			return ReikaStringParser.capFirstChar(this.name());
		}

		public int getCountdown() {
			return (int)(MINING_COOLDOWN/yield);
		}

		public Purity higher() {
			return this == PURE ? null : list[this.ordinal()+1];
		}

		public Purity lower() {
			return this == IMPURE ? null : list[this.ordinal()-1];
		}

		public Purity higherOrSelf() {
			return this == PURE ? this : list[this.ordinal()+1];
		}

		public Purity lowerOrSelf() {
			return this == IMPURE ? this : list[this.ordinal()-1];
		}
	}

	public class ResourceItemView {

		private final NodeItem item;
		public final Purity purity;

		public final float weight;
		public final int minAmount;
		public final int maxAmount;

		public final float manualWeightScale;
		public final float manualYieldScale;

		protected ResourceItemView(NodeItem ni, Purity p) {
			item = ni;
			purity = p;

			Float pw = ni.purityWeights.get(p);
			Float py = ni.purityYields.get(p);
			weight = (int)(ni.defaultWeight*(pw != null ? pw.floatValue() : 1));
			minAmount = (int)(ni.minAmount*(py != null ? py.floatValue() : 1));
			maxAmount = (int)(ni.maxAmount*(py != null ? py.floatValue() : 1));

			manualWeightScale = ni.manualWeightFactor;
			manualYieldScale = ni.manualAmountFactor;
		}

	}

	public class NodeItem implements DynamicWeight {

		protected final I item;
		protected final int defaultWeight;
		protected final int minAmount;
		protected final int maxAmount;

		protected float manualWeightFactor = 1;
		protected float manualAmountFactor = 1;
		protected int minimumTier = 0;
		private EnumMap<Purity, Float> purityWeights = new EnumMap(Purity.class);
		private EnumMap<Purity, Float> purityYields = new EnumMap(Purity.class);

		private float weight;

		protected NodeItem(I is, int def, int min, int max) {
			item = is;
			defaultWeight = def;
			minAmount = min;
			maxAmount = max;
		}

		private void loadData(LuaBlock lb) {
			if (lb.containsKey("manualWeightModifier"))
				manualWeightFactor = (float)lb.getDouble("manualWeightModifier");
			if (manualWeightFactor < 0)
				throw new IllegalArgumentException("Invalid manual weight scale");

			if (lb.containsKey("manualAmountModifier"))
				manualAmountFactor = (float)lb.getDouble("manualAmountModifier");
			if (manualAmountFactor < 0)
				throw new IllegalArgumentException("Invalid manual yield scale");

			if (lb.containsKey("peacefulScale"))
				peacefulYieldScale = (float)lb.getDouble("peacefulScale");
			if (peacefulYieldScale > 1 || peacefulYieldScale < 0)
				throw new IllegalArgumentException("Invalid peaceful scale");

			LuaBlock child = lb.getChild("weightModifiers");
			if (child != null) {
				for (String s : child.getKeys()) {
					Purity p = Purity.valueOf(s);
					purityWeights.put(p, (float)child.getDouble(s));
				}
			}
			child = lb.getChild("amountModifiers");
			if (child != null) {
				for (String s : child.getKeys()) {
					Purity p = Purity.valueOf(s);
					purityYields.put(p, (float)child.getDouble(s));
				}
			}
		}

		@Override
		public final String toString() {
			return item.toString()+" @ "+defaultWeight;
		}

		private void calc(int tier, Purity p, boolean manual) {
			weight = defaultWeight;
			if (manual)
				weight *= manualWeightFactor;
			Float mult = purityWeights.get(p);
			if (mult != null) {
				weight *= mult.floatValue();
			}
			if (tier < minimumTier)
				weight = 0;
		}

		@Override
		public final double getWeight() {
			return weight;
		}

		public final int getAmount(Purity p, int tier, boolean manual, boolean peaceful, Random rand) {
			double amt = ReikaRandomHelper.getRandomBetween(minAmount, (double)this.maxAmount, rand);
			if (peaceful)
				amt *= peacefulYieldScale;
			if (manual)
				amt *= manualAmountFactor;
			Float sc = purityYields.get(p);
			if (sc != null)
				amt *= sc.floatValue();
			return (int)Math.max(1, amt);
		}

	}

	public static class NodeEffect {

		public final EffectTypes type;
		private final HashMap<String, Object> data;

		private NodeEffect(EffectTypes e, HashMap<String, Object> map) {
			type = e;
			data = map;
		}

		public void apply(TileEntity te, EntityPlayer ep) {
			type.apply(te, ep, data);
		}

	}

	public static enum EffectTypes {
		DAMAGE("damage"),
		POTION("potion"),
		REFLECTIVE("reflective", "reflective invocation of any MC/mod java method"),
		CUSTOM("custom"),
		;

		public final String key;
		public final String comment;

		private static final HashMap<String, EffectTypes> keyMap = new HashMap();
		private static final HashMap<String, NodeEffectCallback> customEffects = new HashMap();

		private EffectTypes(String s) {
			this(s, null);
		}

		private EffectTypes(String s, String c) {
			key = s;
			comment = c;
		}

		private HashMap<String, Object> parseData(LuaBlock b) {
			switch(this) {
				case REFLECTIVE:
					HashMap<String, Object> map = b.asHashMap();
					String cn = (String)map.get("class");
					if (cn == null)
						throw new IllegalArgumentException("Invalid reflective definition - no class specified");
					Class c;
					try {
						c = Class.forName(cn);
					}
					catch (ClassNotFoundException ex) {
						throw new IllegalArgumentException("Invalid reflective definition - no such class");
					}
					map.put("class", c);
					LuaBlock argC = b.getChild("args");
					if (argC == null)
						throw new IllegalArgumentException("Invalid reflective definition - no arguments specified");
					ArrayList<String> args = new ArrayList(argC.getDataValues());
					MethodArgument[] argArr = new MethodArgument[args.size()];
					Class[] argTypes = new Class[argArr.length];
					for (int i = 0; i < argArr.length; i++) {
						argArr[i] = MethodArgument.parse(args.get(i));
						argTypes[i] = argArr[i].type.type;
					}
					map.put("args", argArr);
					String mn = (String)map.get("method");
					if (mn == null)
						throw new IllegalArgumentException("Invalid reflective definition - no method name specified");
					Method m;
					try {
						m = c.getDeclaredMethod(mn, argTypes);
					}
					catch (NoSuchMethodException ex) {
						throw new IllegalArgumentException("Invalid reflective effect definition - no such method", ex);
					}
					m.setAccessible(true);
					map.put("method", m);
					String inst = (String)map.get("instance");
					map.put("instance", inst != null ? MethodArgument.parse(inst) : null);
					return map;
				default:
					return b.asHashMap();
			}
		}

		private void apply(TileEntity te, EntityPlayer ep, HashMap<String, Object> data) {
			switch(this) {
				case CUSTOM:
					customEffects.get(data.get("effectName")).apply(te, ep);
					break;
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
				case REFLECTIVE:
					try {
						Method m = (Method)data.get("method");
						MethodArgument inst = (MethodArgument)data.get("instance");
						MethodArgument[] args = (MethodArgument[])data.get("args");
						m.invoke(inst != null ? inst.getValue(te, ep) : null, this.unwrapArgs(te, ep, args));
					}
					catch (Exception e) {
						Satisforestry.logger.logError("Error applying reflectively-defined resource effect!");
						e.printStackTrace();
					}
					break;
			}
		}

		private Object[] unwrapArgs(TileEntity te, EntityPlayer ep, MethodArgument[] args) {
			Object[] ret = new Object[args.length];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = args[i].getValue(te, ep);
			}
			return ret;
		}

		static {
			for (EffectTypes c : values()) {
				keyMap.put(c.key, c);
			}
		}

		public static void addCustomCallback(String name, NodeEffectCallback call) {
			/*
			Class[] types = new Class[]{String.class, String.class};
			Object[] args = new Object[]{name, call.getComment()};
			EffectTypes c = EnumHelper.addEnum(EffectTypes.class, name.toUpperCase(), types, args);
			customEffects.put(c.name(), call);
			return c;
			 */
			customEffects.put(name, call);
		}

		public static EffectTypes getByKey(String s) {
			return keyMap.get(s);
		}
	}

	private static enum MethodArgumentType {

		INT(int.class),
		FLOAT(float.class),
		BOOLEAN(boolean.class),
		STRING(String.class),
		PLAYER(EntityPlayer.class),
		WORLD(World.class),
		X(int.class),
		Y(int.class),
		Z(int.class),
		TILE(TileEntity.class),
		;

		private final Class type;

		private MethodArgumentType(Class c) {
			type = c;
		}

		private Object getValue(Object data, TileEntity te, EntityPlayer ep) {
			switch(this) {
				case INT:
				case FLOAT:
				case BOOLEAN:
				case STRING:
					return data;
				case PLAYER:
					return ep;
				case WORLD:
					return ep.worldObj;
				case X:
					return MathHelper.floor_double(ep.posX);
				case Y:
					return MathHelper.floor_double(ep.posY);
				case Z:
					return MathHelper.floor_double(ep.posZ);
				case TILE:
					return te;
			}
			return null;
		}

		private Object parseLiteralValue(String val) {
			if (Strings.isNullOrEmpty(val))
				return null;
			switch(this) {
				case INT:
					return Integer.parseInt(val);
				case FLOAT:
					return (float)Double.parseDouble(val);
				case BOOLEAN:
					return Boolean.parseBoolean(val);
				case STRING:
					return val;
				default:
					return null;
			}
		}

	}

	private static class MethodArgument {

		private final MethodArgumentType type;
		private final Object data;

		private MethodArgument(MethodArgumentType t, Object o) {
			type = t;
			data = o;
		}

		private Object getValue(TileEntity te, EntityPlayer ep) {
			return type.getValue(data, te, ep);
		}

		private static MethodArgument parse(String s) {
			try {
				int idx = s.indexOf('(');
				String val = null;
				if (idx >= 0) {
					val = s.substring(idx+1, s.length()-1);
					s = s.substring(0, idx);
				}
				MethodArgumentType type = MethodArgumentType.valueOf(s.toUpperCase(Locale.ENGLISH));
				return new MethodArgument(type, type.parseLiteralValue(val));
			}
			catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Invalid argument type. Valid types: "+ReikaJavaLibrary.getEnumNameList(MethodArgumentType.class));
			}
		}
	}

}
