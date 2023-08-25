/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.Satisforestry.Registry;

import net.minecraftforge.common.config.Property;

import Reika.DragonAPI.Interfaces.Configuration.BooleanConfig;
import Reika.DragonAPI.Interfaces.Configuration.BoundedConfig;
import Reika.DragonAPI.Interfaces.Configuration.DecimalConfig;
import Reika.DragonAPI.Interfaces.Configuration.IntegerConfig;
import Reika.DragonAPI.Interfaces.Configuration.StringConfig;
import Reika.DragonAPI.Interfaces.Configuration.UserSpecificConfig;
import Reika.Satisforestry.Satisforestry;

public enum SFOptions implements BooleanConfig, IntegerConfig, DecimalConfig, StringConfig, UserSpecificConfig, BoundedConfig {

	BIOMEID("Pink Forest Biome ID", 144),
	//SIMPLEAUTO("Enable Simple Automation for Resource Node", false),
	CAVEMOBS("Cave Mob Spawn Multiplier", 1),
	GLOBALSHADER("Apply Poison Shader In All Biomes", false),
	ALTSHADER("Use Alternate Poison Shader", false),
	MUSIC("Satisfactory OST Folder", ""),
	BORDERORE("Biome Border Ore Cluster Rate", 1F),
	BLUEGREENSLUGS("Make Green Power Slugs Blue", false), //In Update 5, Satisfactory changed green power slugs to blue. Enable this to also apply that change
	PALEBERRYPOLLEN("Paleberry Butterfly Fertilization Requires Pink Birch Pollen", false), //Whether the ability of the Paleberry Sucker butterfly to fertilize paleberries depends on having a pink birch tree in range
	SLOWTREEGEN("Delayed Giant Pink Tree Generation", false), //Whether to engage delayed pink tree gen as described above
	COMPACTCOALITEM("Compacted Coal Unlock Required Item (Empty for None)", "minecraft:coal*64"),
	COMPACTCOALPOWER("Compacted Coal Unlock Required Power (power type;amount pertick;ticks to maintain)", "RF;300;600"),
	TURBOFUELITEM("Turbofuel Unlock Required Item (Empty for None)", ""),
	TURBOFUELPOWER("Turbofuel Unlock Required Power (power type;amount pertick;ticks to maintain)", "RF;2400;600"),
	DOGGOCLUSTERS("Lizard Doggo Ore Yield Type", "ore"), //This option allows you to choose what kind of item ('ore' for ore blocks, 'dust' for the dust form of the resource, 'products' for whatever the usual smelted result is, 'cluster' for ThaumCraft native clusters) doggos will find when told to find X kind of ore. If the type chosen has no mapping for the ore in question (eg native clusters without TC installed), the ore block will be used as a fallback.
	RFCOST("Machinery RF Cost Tier", 3), //1-4, with 1 being the cheapest RF costs, suitable for packs where the most powerful generators are 60-100RF/t, 2 being for packs with generator caps around 2500RF/t, and 3 being for packs with caps around 30000RF/t per generator, and 4 for packs with anything capable of significantly exceeding that. Does not affect EU or RC versions of the machines, and will subtract 10% efficiency for each "step down" from 50% at max cost.
	;

	private String label;
	private boolean defaultState;
	private int defaultValue;
	private String defaultString;
	private float defaultFloat;
	private Class type;

	public static final SFOptions[] optionList = SFOptions.values();

	private SFOptions(String l, boolean d) {
		label = l;
		defaultState = d;
		type = boolean.class;
	}

	private SFOptions(String l, int d) {
		label = l;
		defaultValue = d;
		type = int.class;
	}

	private SFOptions(String l, String s) {
		label = l;
		defaultString = s;
		type = String.class;
	}

	private SFOptions(String l, float d) {
		label = l;
		defaultFloat = d;
		type = float.class;
	}

	public boolean isBoolean() {
		return type == boolean.class;
	}

	public boolean isNumeric() {
		return type == int.class;
	}

	public boolean isDecimal() {
		return type == float.class;
	}

	@Override
	public boolean isString() {
		return type == String.class;
	}

	public Class getPropertyType() {
		return type;
	}

	public String getLabel() {
		return label;
	}

	public boolean getState() {
		return (Boolean)Satisforestry.config.getControl(this.ordinal());
	}

	public int getValue() {
		return (Integer)Satisforestry.config.getControl(this.ordinal());
	}

	@Override
	public String getString() {
		return (String)Satisforestry.config.getControl(this.ordinal());
	}

	public float getFloat() {
		return (Float)Satisforestry.config.getControl(this.ordinal());
	}

	public boolean isDummiedOut() {
		return type == null;
	}

	@Override
	public boolean getDefaultState() {
		return defaultState;
	}

	@Override
	public int getDefaultValue() {
		return defaultValue;
	}

	@Override
	public String getDefaultString() {
		return defaultString;
	}

	@Override
	public float getDefaultFloat() {
		return defaultFloat;
	}

	@Override
	public boolean isEnforcingDefaults() {
		return false;
	}

	@Override
	public boolean shouldLoad() {
		return true;
	}

	@Override
	public boolean isValueValid(Property p) {
		switch(this) {
			case BIOMEID:
				return p.getInt() >= 40 && p.getInt() <= 255;
			case CAVEMOBS:
				return p.getDouble() >= 0.25 && p.getDouble() <= 5;
			default:
				return true;
		}
	}

	@Override
	public String getBoundsAsString() {
		switch(this) {
			case BIOMEID:
				return "(40-255)";
			case CAVEMOBS:
				return "(0.25-5)";
			default:
				return "";
		}
	}

	@Override
	public boolean isUserSpecific() {
		switch(this) {
			case GLOBALSHADER:
			case MUSIC:
			case BLUEGREENSLUGS:
				return true;
			default:
				return false;
		}
	}
}
