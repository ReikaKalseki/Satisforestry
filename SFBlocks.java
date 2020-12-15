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

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import Reika.DragonAPI.Instantiable.MetadataItemBlock;
import Reika.DragonAPI.Interfaces.Registry.BlockEnum;
import Reika.DragonAPI.Libraries.Java.ReikaStringParser;
import Reika.GeoStrata.Base.RockBlock;
import Reika.GeoStrata.Blocks.BlockShapedRock;
import Reika.GeoStrata.Blocks.BlockSmooth;
import Reika.Satisforestry.Blocks.BlockCaveShield;
import Reika.Satisforestry.Blocks.BlockPinkGrass;
import Reika.Satisforestry.Blocks.BlockPinkLeaves;
import Reika.Satisforestry.Blocks.BlockPinkLog;
import Reika.Satisforestry.Blocks.BlockRedBamboo;

public enum SFBlocks implements BlockEnum {

	LOG(BlockPinkLog.class, null, "Pink Birch Log"),
	BAMBOO(BlockRedBamboo.class, null, "Red Bamboo"),
	LEAVES(BlockPinkLeaves.class, null, "Pink Birch Leaves"),
	GRASS(BlockPinkGrass.class, MetadataItemBlock.class, "Pink Grass"),
	CAVESHIELD(BlockCaveShield.class, null, "Cave Stone"),
	;

	private final Class blockClass;
	private final String blockName;
	private final Class itemBlock;

	public static final SFBlocks[] blockList = values();

	private static final HashMap<Block, SFBlocks> IDMap = new HashMap();

	private SFBlocks(Class <? extends Block> cl, Class<? extends ItemBlock> ib, String n) {
		blockClass = cl;
		blockName = n;
		itemBlock = ib;
	}

	public Block getBlockInstance() {
		return Satisforestry.blocks[this.ordinal()];
	}

	public static SFBlocks getFromID(Item id) {
		return getFromID(Block.getBlockFromItem(id));
	}

	public static SFBlocks getFromID(Block id) {
		SFBlocks block = IDMap.get(id);
		if (block == null) {
			for (int i = 0; i < blockList.length; i++) {
				SFBlocks g = blockList[i];
				Block blockID = g.getBlockInstance();
				if (id == blockID) {
					IDMap.put(id, g);
					return g;
				}
			}
		}
		else {
			return block;
		}
		return null;
	}

	public Material getBlockMaterial() {
		switch(this) {
			case GRASS:
				return Material.plants;
			case LEAVES:
			case BAMBOO:
				return Material.leaves;
			case LOG:
				return Material.wood;
			default:
				return Material.rock;
		}
	}

	public boolean isRock() {
		return RockBlock.class.isAssignableFrom(blockClass);
	}

	@Override
	public Class[] getConstructorParamTypes() {
		switch(this) {
			case GRASS:
			case LEAVES:
			case BAMBOO:
			case LOG:
				return new Class[0];
		}
		return new Class[]{Material.class};
	}

	@Override
	public Object[] getConstructorParams() {
		switch(this) {
			case GRASS:
			case LEAVES:
			case BAMBOO:
			case LOG:
				return new Object[0];
		}
		return new Object[]{this.getBlockMaterial()};
	}

	@Override
	public String getUnlocalizedName() {
		return ReikaStringParser.stripSpaces(blockName);
	}

	public boolean isSmoothBlock() {
		return BlockSmooth.class.isAssignableFrom(blockClass);
	}

	@Override
	public Class getObjectClass() {
		return blockClass;
	}

	@Override
	public String getBasicName() {
		return blockName;
	}

	@Override
	public String getMultiValuedName(int meta) {
		switch(this) {
			default:
				return "";
		}
	}

	public boolean isShapedRock() {
		return BlockShapedRock.class.isAssignableFrom(blockClass);
	}

	@Override
	public boolean hasMultiValuedName() {
		return false;
	}

	@Override
	public int getNumberMetadatas() {
		return 1;
	}

	@Override
	public Class<? extends ItemBlock> getItemBlock() {
		return itemBlock;
	}

	@Override
	public boolean hasItemBlock() {
		return itemBlock != null;
	}

	public boolean isDummiedOut() {
		return blockClass == null;
	}

	public Item getItem() {
		return Item.getItemFromBlock(this.getBlockInstance());
	}

	@Override
	public ItemStack getStackOfMetadata(int meta) {
		return new ItemStack(this.getBlockInstance(), 1, meta);
	}
}
