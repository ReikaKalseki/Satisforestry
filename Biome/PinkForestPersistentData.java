package Reika.Satisforestry.Biome;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.Biomewide.BiomewideFeatureGenerator;


public class PinkForestPersistentData extends WorldSavedData {

	private static final String IDENTIFIER = "PinkForestData";

	public PinkForestPersistentData() {
		super(IDENTIFIER);
	}

	public PinkForestPersistentData(String s) {
		super(s);
	}

	@Override
	public void readFromNBT(NBTTagCompound NBT) {
		try {
			BiomewideFeatureGenerator.instance.readFromNBT(NBT.getCompoundTag("biomewideFeatures"));
			//PinkTreeGeneratorBase.blockPlacer.readFromNBT(NBT.getCompoundTag("queuedTreeBlocks"));
		}
		catch (Exception e) {
			Satisforestry.logger.logError("Failed reading persistent NBT data for biome. It is recommended you repair or delete /data/"+IDENTIFIER+".dat");
			e.printStackTrace();
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound NBT) {
		NBTTagCompound tag = new NBTTagCompound();
		BiomewideFeatureGenerator.instance.writeToNBT(tag);
		NBT.setTag("biomewideFeatures", tag);

		//tag = new NBTTagCompound();
		//PinkTreeGeneratorBase.blockPlacer.writeToNBT(tag);
		//NBT.setTag("queuedTreeBlocks", tag);
	}

	public static PinkForestPersistentData initNetworkData(World world) {
		PinkForestPersistentData data = (PinkForestPersistentData)world.loadItemData(PinkForestPersistentData.class, IDENTIFIER);
		if (data == null) {
			data = new PinkForestPersistentData();
			world.setItemData(IDENTIFIER, data);
		}
		return data;
	}

}
