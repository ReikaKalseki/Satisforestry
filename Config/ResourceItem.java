package Reika.Satisforestry.Config;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Random;

import com.google.common.base.Strings;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.WeightedRandom;
import Reika.DragonAPI.Instantiable.IO.LuaBlock;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.API.NodeEffectCallback;
import Reika.Satisforestry.Blocks.BlockResourceNode.Purity;
import Reika.Satisforestry.Blocks.BlockResourceNode.TileResourceNode;

public class ResourceItem {

	public final String id;
	public final int color;
	public final int spawnWeight;

	private final WeightedRandom<Purity> levels = new WeightedRandom();
	private final HashMap<Purity, WeightedRandom<NodeItem>> items = new HashMap();
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

	public void addItem(Purity p, ItemStack is, int weight, int tier) {
		WeightedRandom<NodeItem> wr = items.get(p);
		if (wr == null) {
			wr = new WeightedRandom();
			items.put(p, wr);
		}
		wr.addEntry(new NodeItem(is, tier), weight);
	}

	public void addEffect(LuaBlock b) {
		String key = b.getString("effectType");
		EffectTypes t = EffectTypes.getByKey(key);
		if (t == null)
			throw new IllegalArgumentException("Invalid effect type '"+key+"'");
		NodeEffect e = new NodeEffect(t, t.parseData(b));
		effects.add(e);
	}

	public ItemStack getRandomItem(int tier, Purity p) {
		WeightedRandom<NodeItem> wr = items.get(p);
		return wr == null ? null : wr.getRandomEntry().item;
	}

	public Purity getRandomPurity(Random rand) {
		levels.setRNG(rand);
		return levels.getRandomEntry();
	}

	public Collection<NodeEffect> getEffects() {
		return Collections.unmodifiableCollection(effects);
	}

	public boolean hasNoItems() {
		return items.isEmpty();
	}

	public HashMap<ItemStack, Double> getItemSet(Purity p) {
		HashMap<ItemStack, Double> ret = new HashMap();
		WeightedRandom<NodeItem> wr = items.get(p);
		for (NodeItem is : wr.getValues()) {
			ret.put(is.item.copy(), wr.getWeight(is));
		}
		return ret;
	}

	@Override
	public String toString() {
		return "W="+spawnWeight+", C="+Integer.toHexString(color)+", L="+levels.toString()+", #="+minCount+"-"+maxCount+", I="+items.toString();
	}

	private static class NodeItem {

		private final ItemStack item;
		private final int tier;

		private NodeItem(ItemStack is, int t) {
			item = is.copy();
			tier = t;
		}

		@Override
		public String toString() {
			return item.toString()+" > "+tier;
		}

	}

	public static class NodeEffect {

		public final EffectTypes type;
		private final HashMap<String, Object> data;

		private NodeEffect(EffectTypes e, HashMap<String, Object> map) {
			type = e;
			data = map;
		}

		public void apply(TileResourceNode te, EntityPlayer ep) {
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

		private void apply(TileResourceNode te, EntityPlayer ep, HashMap<String, Object> data) {
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

		private Object[] unwrapArgs(TileResourceNode te, EntityPlayer ep, MethodArgument[] args) {
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
