/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.RotaryCraft.Base.TileEntity;

import net.minecraft.world.World;
import Reika.RotaryCraft.API.ShaftMerger;
import Reika.RotaryCraft.Auxiliary.PowerSourceList;
import Reika.RotaryCraft.Auxiliary.Interfaces.SimpleProvider;

public abstract class TileEntity1DTransmitter extends TileEntityTransmissionMachine implements SimpleProvider {

	public int ratio;

	public static final int SHEARSTRENGTH = -1;
	public static final int TENSILESTRENGTH = -1;

	public void getIOSides(World world, int x, int y, int z, int meta, boolean hasVertical) {
		switch(meta){
		case 0:
			readx = x+1;
			ready = y;
			readz = z;
			writex = x-1;
			writey = y;
			writez = z;
			break;
		case 1:
			readx = x-1;
			ready = y;
			readz = z;
			writex = x+1;
			writey = y;
			writez = z;
			break;
		case 2:
			readx = x;
			ready = y;
			readz = z+1;
			writex = x;
			writey = y;
			writez = z-1;
			break;
		case 3:
			readx = x;
			ready = y;
			readz = z-1;
			writex = x;
			writey = y;
			writez = z+1;
			break;
		case 4:
			if (hasVertical) {
				readx = x;
				ready = y-1;
				readz = z;
				writex = x;
				writey = y+1;
				writez = z;
			}
			break;
		case 5:
			if (hasVertical) {
				readx = x;
				ready = y+1;
				readz = z;
				writex = x;
				writey = y-1;
				writez = z;
			}
			break;
		}
	}

	public abstract void transferPower(World world, int x, int y, int z, int meta);

	@Override
	public boolean canProvidePower() {
		return true;
	}

	@Override
	public PowerSourceList getPowerSources(TileEntityIOMachine io, ShaftMerger caller) {
		return PowerSourceList.getAllFrom(worldObj, readx, ready, readz, this, caller);
	}
}
