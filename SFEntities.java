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

import net.minecraft.entity.Entity;

import Reika.DragonAPI.Interfaces.Registry.EntityEnum;
import Reika.Satisforestry.Entity.EntityEliteStinger;
import Reika.Satisforestry.Entity.EntityFlyingManta;
import Reika.Satisforestry.Entity.EntityLizardDoggo;

public enum SFEntities implements EntityEnum {

	ELITESTINGER(EntityEliteStinger.class, "Elite Stinger", 0x00740E, 0x00BE0E),
	MANTA(EntityFlyingManta.class, "Giant Flying Manta", 0x0094FF, 0x42AAFF),
	DOGGO(EntityLizardDoggo.class, "Lizard Doggo", 0x42AAFF, 0xFF6A55),
	;

	public final String entityName;
	private final Class entityClass;
	private final int eggColor1;
	private final int eggColor2;
	private final boolean hasEgg;

	public static final SFEntities[] entityList = values();

	private SFEntities(Class<? extends Entity> c, String s) {
		this(c, s, -1, -1);
	}

	private SFEntities(Class<? extends Entity> c, String s, int c1, int c2) {
		entityClass = c;
		entityName = s;

		eggColor1 = c1;
		eggColor2 = c2;
		hasEgg = c1 >= 0 && c2 >= 0;
	}

	@Override
	public String getBasicName() {
		return entityName;
	}

	@Override
	public boolean isDummiedOut() {
		return false;
	}

	@Override
	public Class getObjectClass() {
		return entityClass;
	}

	@Override
	public String getUnlocalizedName() {
		return entityName;
	}

	@Override
	public int getTrackingDistance() {
		return this == MANTA ? 512 : 96;
	}

	@Override
	public boolean sendsVelocityUpdates() {
		return true;
	}

	@Override
	public boolean hasSpawnEgg() {
		return hasEgg;
	}

	@Override
	public boolean hasGlobalID() {
		return true;
	}

	@Override
	public int eggColor1() {
		return eggColor1;
	}

	@Override
	public int eggColor2() {
		return eggColor2;
	}

}
