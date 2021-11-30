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

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

public class NEISFConfig implements IConfigureNEI {

	private static final ResourceNodeHandler nodes = new ResourceNodeHandler();
	private static final DoggoDropHandler doggo = new DoggoDropHandler();

	@Override
	public void loadConfig() {
		Satisforestry.logger.log("Loading NEI Compatibility!");
		API.registerRecipeHandler(nodes);
		API.registerRecipeHandler(doggo);
	}

	@Override
	public String getName() {
		return "Satisforestry NEI Handlers";
	}

	@Override
	public String getVersion() {
		return "-";
	}

}
