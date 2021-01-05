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

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import Reika.DragonAPI.Instantiable.MetadataItemBlock;
import Reika.DragonAPI.Interfaces.Registry.BlockEnum;
import Reika.DragonAPI.Libraries.Java.ReikaStringParser;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockCaveShield;
import Reika.Satisforestry.Blocks.BlockCaveSpawner;
import Reika.Satisforestry.Blocks.BlockDecoration;
import Reika.Satisforestry.Blocks.BlockDecoration.DecorationType;
import Reika.Satisforestry.Blocks.BlockGasEmitter;
import Reika.Satisforestry.Blocks.BlockPinkGrass;
import Reika.Satisforestry.Blocks.BlockPinkGrass.GrassTypes;
import Reika.Satisforestry.Blocks.BlockPinkLeaves;
import Reika.Satisforestry.Blocks.BlockPinkLog;
import Reika.Satisforestry.Blocks.BlockRedBamboo;
import Reika.Satisforestry.Blocks.BlockResourceNode;
import Reika.Satisforestry.Blocks.BlockTerrain;
import Reika.Satisforestry.Blocks.BlockTerrain.TerrainType;

public enum SFBlocks implements BlockEnum {

	LOG(BlockPinkLog.class, null, "Pink Birch Log"),
	BAMBOO(BlockRedBamboo.class, null, "Red Bamboo"),
	LEAVES(BlockPinkLeaves.class, null, "Pink Birch Leaves"),
	GRASS(BlockPinkGrass.class, MetadataItemBlock.class, "Pink Grass"),
	CAVESHIELD(BlockCaveShield.class, null, "Cave Stone"),
	SPAWNER(BlockCaveSpawner.class, null, "Cracked Cave Stone"),
	GASEMITTER(BlockGasEmitter.class, null, "Gas Vent"),
	RESOURCENODE(BlockResourceNode.class, null, "Resource Node"),
	TERRAIN(BlockTerrain.class, MetadataItemBlock.class, ""),
	DECORATION(BlockDecoration.class, MetadataItemBlock.class, ""),
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

	@Override
	public Class[] getConstructorParamTypes() {
		switch(this) {
			case GRASS:
			case LEAVES:
			case BAMBOO:
			case LOG:
				return new Class[0];
			default:
				return new Class[]{Material.class};
		}
	}

	@Override
	public Object[] getConstructorParams() {
		switch(this) {
			case GRASS:
			case LEAVES:
			case BAMBOO:
			case LOG:
				return new Object[0];
			default:
				return new Object[]{this.getBlockMaterial()};
		}
	}

	@Override
	public String getUnlocalizedName() {
		return ReikaStringParser.stripSpaces(blockName);
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
			case DECORATION:
				return DecorationType.list[meta].name;
			case TERRAIN:
				return TerrainType.list[meta].name;
			case GRASS:
				String s = GrassTypes.list[meta].name;
				return s != null ? s : this.getBasicName();
			default:
				return "";
		}
	}

	@Override
	public boolean hasMultiValuedName() {
		switch(this) {
			case TERRAIN:
			case DECORATION:
			case GRASS:
				return true;
			default:
				return false;
		}
	}

	@Override
	public int getNumberMetadatas() {
		switch(this) {
			case DECORATION:
				return DecorationType.list.length;
			case TERRAIN:
				return TerrainType.list.length;
			case GRASS:
				return GrassTypes.list.length;
			default:
				return 1;
		}
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
