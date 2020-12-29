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

import net.minecraftforge.common.config.Property;

import Reika.DragonAPI.Interfaces.Configuration.BooleanConfig;
import Reika.DragonAPI.Interfaces.Configuration.BoundedConfig;
import Reika.DragonAPI.Interfaces.Configuration.IntegerConfig;

public enum SFOptions implements BooleanConfig, IntegerConfig, BoundedConfig {

	BIOMEID("Pink Forest Biome ID", 144),
	SIMPLEAUTO("Enable Simple Automation for Resource Node", false),
	CAVEMOBS("Cave Mob Spawn Multiplier", 1)
	;

	private String label;
	private boolean defaultState;
	private int defaultValue;
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

	public boolean isBoolean() {
		return type == boolean.class;
	}

	public boolean isNumeric() {
		return type == int.class;
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
}
