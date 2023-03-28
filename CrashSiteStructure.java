package Reika.Satisforestry;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Instantiable.Data.BlockStruct.FilledBlockArray;
import Reika.DragonAPI.Libraries.ReikaDirectionHelper;
import Reika.Satisforestry.Blocks.BlockMinerMulti.MinerBlocks;
import Reika.Satisforestry.Registry.SFBlocks;

public class CrashSiteStructure {

	public static FilledBlockArray getStructure(World world, int x, int y, int z, ForgeDirection dir) {
		FilledBlockArray arr = new FilledBlockArray(world);
		arr.setBlock(x, y, z, SFBlocks.CRASHSITE.getBlockInstance(), 0);
		if (dir.offsetY == 0) {
			ForgeDirection left = ReikaDirectionHelper.getLeftBy90(dir);
			arr.setBlock(x, y+1, z, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.GRAY));
			arr.setBlock(x, y-1, z, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.SILVER));
			arr.setBlock(x+left.offsetX, y, z+left.offsetZ, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.SILVER));
			arr.setBlock(x-left.offsetX, y, z-left.offsetZ, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.SILVER));
			arr.setBlock(x+left.offsetX, y-1, z+left.offsetZ, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.DARK));
			arr.setBlock(x-left.offsetX, y-1, z-left.offsetZ, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.DARK));

			arr.setBlock(x+dir.offsetX+left.offsetX*2, y-1, z+dir.offsetZ+left.offsetZ*2, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.SILVER));
			arr.setBlock(x+dir.offsetX-left.offsetX*2, y-1, z+dir.offsetZ-left.offsetZ*2, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.SILVER));
			arr.setBlock(x+dir.offsetX*3+left.offsetX*2, y-1, z+dir.offsetZ*3+left.offsetZ*2, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.SILVER));
			arr.setBlock(x+dir.offsetX*3-left.offsetX*2, y-1, z+dir.offsetZ*3-left.offsetZ*2, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.SILVER));

			for (int i = 1; i <= 5; i++) {
				for (int a = -1; a <= 1; a++) {
					for (int k = -1; k <= 1; k++) {
						MinerBlocks b = MinerBlocks.GRAY;
						if (k == -1 || (i == 3 && (a != 0 || k != 0)))
							b = MinerBlocks.DARK;
						else if (a != 0 || (i == 4 && k == 1))
							b = MinerBlocks.ORANGE;
						arr.setBlock(x+i*dir.offsetX+a*left.offsetX, y+k, z+i*dir.offsetZ+a*left.offsetZ, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(b));
					}
				}
			}

			for (int i = -1; i <= 1; i++)
				arr.setBlock(x+dir.offsetX*6-left.offsetX*i, y+1, z+dir.offsetZ*6-left.offsetZ*i, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.GRAY));
			arr.setBlock(x+dir.offsetX*6, y, z+dir.offsetZ*6, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.SILVER));
			arr.setBlock(x+dir.offsetX*6, y-1, z+dir.offsetZ*6, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.SILVER));
		}
		else {
			arr.setBlock(x-1, y, z, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.GRAY));
			arr.setBlock(x+1, y, z, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.SILVER));
			arr.setBlock(x, y, z+1, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.SILVER));
			arr.setBlock(x, y, z-1, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.SILVER));
			arr.setBlock(x+1, y, z+1, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.DARK));
			arr.setBlock(x+1, y, z-1, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.DARK));

			for (int i = 1; i <= 5; i++) {
				if (i == 3)
					continue;
				arr.setBlock(x, y-i, z, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.GRAY));
				arr.setBlock(x-1, y-i, z, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(i == 4 ? MinerBlocks.ORANGE : MinerBlocks.GRAY));
				arr.setBlock(x+1, y-i, z, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.DARK));
				arr.setBlock(x, y-i, z+1, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.ORANGE));
				arr.setBlock(x, y-i, z-1, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.ORANGE));
				arr.setBlock(x-1, y-i, z+1, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.ORANGE));
				arr.setBlock(x-1, y-i, z-1, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(MinerBlocks.ORANGE));
				arr.setBlock(x+1, y-i, z+1, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(i == 1 ? MinerBlocks.SILVER : MinerBlocks.DARK));
				arr.setBlock(x+1, y-i, z-1, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(i == 1 ? MinerBlocks.SILVER : MinerBlocks.DARK));
			}

			for (int i = -1; i <= 1; i++) {
				for (int k = -1; k <= 1; k++) {
					MinerBlocks b = MinerBlocks.DARK;
					if (i == 0 && k == 0)
						b = MinerBlocks.GRAY;
					else if (i == 1 && k != 0)
						b = MinerBlocks.SILVER;
					arr.setBlock(x+i, y-3, z+k, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(b));
				}
			}
			for (int i = -1; i <= 1; i++) {
				for (int k = -1; k <= 1; k++) {
					MinerBlocks b = MinerBlocks.GRAY;
					if (i == 1 && k != 0)
						b = MinerBlocks.DARK;
					arr.setBlock(x+i, y-6, z+k, SFBlocks.CRASHSITE.getBlockInstance(), getBlock(b));
				}
			}
		}
		return arr;
	}

	private static int getBlock(MinerBlocks b) {
		return b.ordinal()+1;
	}

}
