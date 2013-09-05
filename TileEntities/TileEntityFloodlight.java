/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.RotaryCraft.TileEntities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import Reika.DragonAPI.Instantiable.BlockArray;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.RotaryCraft.RotaryCraft;
import Reika.RotaryCraft.Auxiliary.RangedEffect;
import Reika.RotaryCraft.Base.RotaryModelBase;
import Reika.RotaryCraft.Base.TileEntityBeamMachine;
import Reika.RotaryCraft.Models.ModelLamp;
import Reika.RotaryCraft.Models.ModelVLamp;
import Reika.RotaryCraft.Registry.ConfigRegistry;
import Reika.RotaryCraft.Registry.MachineRegistry;
import Reika.RotaryCraft.Registry.RotaryAchievements;

public class TileEntityFloodlight extends TileEntityBeamMachine implements RangedEffect {

	public int distancelimit = ConfigRegistry.FLOODLIGHTRANGE.getValue();
	public int lightlevel;
	public boolean beammode = false;

	/** Used to detect if floodlight just turned off */
	private boolean wentdark = false;

	private BlockArray beam = new BlockArray();
	private boolean markUpdate = true;

	/** Rate of conversion - one power++ = 1/falloff ++ light levels */
	public static final int FALLOFF = 1024; //1kW a light level right now

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {
		super.updateTileEntity();
		power = omega*torque;
		this.getIOSides(world, x, y, z, meta);
		this.getPower();
		//if (par5Random.nextInt(20) == 0)
		//this.lightsOut(world, x, y, z);
		this.makeBeam(world, x, y, z, meta);
	}

	public void getPower() {
		super.getPower(false, true);
		lightlevel = ReikaMathLibrary.extrema(-1+(int)power/FALLOFF, 0, "max");
		lightlevel = ReikaMathLibrary.extrema(lightlevel, 15, "absmin");
		if (lightlevel >= 15)
			RotaryAchievements.FLOODLIGHT.triggerAchievement(this.getPlacer());
	}

	@Override
	public void makeBeam(World world, int x, int y, int z, int metadata) {
		//boolean blocked = false;
		int range = this.getRange();
		//1 kW - configured so light level 15 (sun) requires approx power of sun on Earth's surface
		/*
			wentdark = false;
			for (int i = 1; (i < range || range == -1) && !blocked && !Block.opaqueCubeLookup[world.getBlockId(x+xstep, y+ystep, z+zstep)] && (!beammode || y+ystep*i <= 125);i++) {//&& world.getBlockId(x+xstep, y+ystep, z+zstep) != RotaryCraft.lightblock.blockID; i++) {
				int idview = world.getBlockId(x+xstep*i, y+ystep*i, z+zstep*i);
				if (idview == 0 || ((idview == RotaryCraft.lightblock.blockID || idview == RotaryCraft.beamblock.blockID) && world.getBlockMetadata(x+xstep*i, y+ystep*i, z+zstep*i) != lightlevel)) { //Only overwrite air blocks or wrong-value light blocks
					if (beammode && lightlevel >= 15)
						world.setBlock(x+xstep*i, y+ystep*i, z+zstep*i, RotaryCraft.beamblock.blockID);
					else if (!beammode)
						world.setBlock(x+xstep*i, y+ystep*i, z+zstep*i, RotaryCraft.lightblock.blockID, lightlevel, 3);
					world.markBlockForUpdate(x+xstep*i, y+ystep*i, z+zstep*i);
				}
				if (Block.opaqueCubeLookup[idview])
					blocked = true;
			}
		}
		else if (!wentdark)
			this.lightsOut(world, x, y, z);*/
		if (markUpdate) {
			beam.clear();
			markUpdate = false;
		}
		if (beam.isEmpty())
			beam.addLineOfClear(world, x, y, z, range, xstep, ystep, zstep);
		//ReikaJavaLibrary.pConsole(beam);
		int size = beam.getSize();
		wentdark = false;
		boolean pow = power >= MINPOWER;
		if (!pow) {
			wentdark = true;
			markUpdate = true;
		}
		for (int i = 0; i < size; i++) {
			int[] xyz = beam.getNthBlock(i);
			if (pow) {
				world.setBlock(xyz[0], xyz[1], xyz[2], RotaryCraft.lightblock.blockID, lightlevel, 3);
			}
			else {
				if (world.getBlockId(xyz[0], xyz[1], xyz[2]) == RotaryCraft.lightblock.blockID)
					world.setBlock(xyz[0], xyz[1], xyz[2], 0);
			}
			world.markBlockForUpdate(xyz[0], xyz[1], xyz[2]);
		}
	}

	public void lightsOut(World world, int x, int y, int z) {
		world.markBlockForUpdate(x, y, z);
		world.notifyBlocksOfNeighborChange(x, y, z, this.getTileEntityBlockID());
		wentdark = true;
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound NBT)
	{
		super.writeToNBT(NBT);
		NBT.setInteger("light", lightlevel);
		NBT.setBoolean("beam", beammode);
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(NBTTagCompound NBT)
	{
		super.readFromNBT(NBT);
		lightlevel = NBT.getInteger("light");
		beammode = NBT.getBoolean("beam");
	}

	@Override
	public boolean hasModelTransparency() {
		return false;
	}

	@Override
	public RotaryModelBase getTEModel(World world, int x, int y, int z) {
		int dmg = world.getBlockMetadata(x, y, z);
		if (dmg < 4)
			return new ModelLamp();
		else
			return new ModelVLamp();
	}

	@Override
	public void animateWithTick(World world, int x, int y, int z) {

	}

	@Override
	public int getRange() {
		return distancelimit;
	}

	@Override
	public int getMachineIndex() {
		return MachineRegistry.FLOODLIGHT.ordinal();
	}

	@Override
	public int getMaxRange() {
		return distancelimit;
	}

	@Override
	public int getRedstoneOverride() {
		return 0;
	}
}
