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

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import Reika.DragonAPI.Auxiliary.EnumLook;
import Reika.DragonAPI.Instantiable.BlockArray;
import Reika.DragonAPI.Libraries.ReikaEntityHelper;
import Reika.DragonAPI.Libraries.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.ReikaWorldHelper;
import Reika.RotaryCraft.RotaryConfig;
import Reika.RotaryCraft.Auxiliary.PipeConnector;
import Reika.RotaryCraft.Base.RotaryModelBase;
import Reika.RotaryCraft.Base.TileEntityPowerReceiver;
import Reika.RotaryCraft.Models.ModelPump;
import Reika.RotaryCraft.Registry.MachineRegistry;
import Reika.RotaryCraft.Registry.SoundRegistry;

public class TileEntityPump extends TileEntityPowerReceiver implements PipeConnector {

	public int liquidID = -1;
	public int liquidLevel;

	private BlockArray blocks = new BlockArray();

	private int soundtick = 200;

	public int damage = 0;


	public final static int CAPACITY = 24*RotaryConfig.MILLIBUCKET;

	public int liquidPressure = 0;

	/** Rate of conversion - one power++ = one tick-- per operation */
	public static final int FALLOFF = 8; //512W per 1 kPa

	TileEntityReservoir tile;

	public void getPressure() {
		int overPower = (int)(power-MINPOWER);
		if (overPower <= 0) {
			liquidPressure = 0;
			return;
		}
		liquidPressure = overPower/FALLOFF;
	}

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {
		super.updateTileEntity();
		soundtick++;
		if (world.getBlockMaterial(x, y-1, z) != Material.water && world.getBlockMaterial(x, y-1, z) != Material.lava)
			return;
		if (!ReikaMathLibrary.isValueInsideBoundsIncl(8, 11, world.getBlockId(x, y-1, z)))
			return;
		if (blocks.isEmpty() || liquidLevel == 0) {
			blocks.setLiquid(world.getBlockMaterial(x, y-1, z));
			blocks.recursiveAddLiquidWithBounds(world, x, y-1, z, x-16, 0, z-16, x+16, y-1, z+16);
			blocks.reverseBlockOrder();
			//ReikaJavaLibrary.pConsole(FMLCommonHandler.instance().getEffectiveSide()+" sized "+blocks.getSize());
			//blocks.recursiveFillWithBounds(world, x, y-1, z, Block.waterMoving.blockID, x-32, 0, z-32, x+32, y-1, z+32);
			//blocks.recursiveFillWithBounds(world, x, y-1, z, Block.waterStill.blockID, x-32, 0, z-32, x+32, y-1, z+32);
		}
		boolean res = false;
		tile = null;
		tickcount++;
		this.getIOSides(world, x, y, z, this.getBlockMetadata());
		this.getPower(true, false);
		power = omega*torque;
		if (damage > 400)
			power = 0;
		this.getPressure();
		if (this.checkForReservoir(world, x, y, z, meta)) {
			tile = (TileEntityReservoir)world.getBlockTileEntity(x, y-1, z);
			res = true;
		}
		//ReikaJavaLibrary.pConsole(FMLCommonHandler.instance().getEffectiveSide()+" for "+blocks.getSize());
		if (blocks.isEmpty())
			return;
		if (this.operationComplete(tickcount, 0) && power > MINPOWER) {
			if (!res) {
				//int loc[] = this.findSourceBlock(world, x, y, z);
				int[] loc = blocks.getNextAndMoveOn();
				//ReikaJavaLibrary.pConsole(loc[0]+"  "+loc[1]+"  "+loc[2]+"  for side "+FMLCommonHandler.instance().getEffectiveSide());
				this.harvest(world, x, y, z, loc);
				tickcount = 0;
				//ModLoader.getMinecraftInstance().ingameGUI.addChatMessage(String.format("%d", this.liquidID));
			}
			else if (tile != null) {
				tile.liquidLevel--;
				liquidID = tile.liquidID;
				liquidLevel++;
			}
		}
		if (liquidLevel <= 0) {
			liquidID = -1;
			liquidLevel = 0;
		}
		if (power > MINPOWER && soundtick >= 100) {
			soundtick = 0;
			SoundRegistry.playSoundAtBlock(SoundRegistry.PUMP, world, x, y, z, 0.5F, 1);
		}
		if (power > MINPOWER)
			this.suckUpMobs(world, x, y, z);
	}

	private void suckUpMobs(World world, int x, int y, int z) {
		AxisAlignedBB box = AxisAlignedBB.getAABBPool().getAABB(x, y-1, z, x+1, y, z+1);
		List inbox = world.getEntitiesWithinAABB(EntityLiving.class, box);
		for (int i = 0; i < inbox.size(); i++) {
			EntityLiving e = (EntityLiving)inbox.get(i);
			e.attackEntityFrom(DamageSource.generic, 5);
		}
		if (inbox.size() > 0 && !ReikaEntityHelper.allAreDead(inbox, false))
			damage++;
		if (damage > 400)
			this.breakPump(world, x, y, z);
	}

	private void breakPump(World world, int x, int y, int z) {
		world.playSoundEffect(x, y, z, "random.break", 1F, 1F);
		//for (int i = 0; i < 8; i++)
		//	ReikaItemHelper.dropItem(world, x+par5Random.nextDouble(), y+par5Random.nextDouble(), z+par5Random.nextDouble(), ItemStacks.scrap);
	}

	private boolean checkForReservoir(World world, int x, int y, int z, int meta) {
		MachineRegistry id = MachineRegistry.getMachine(world, x, y-1, z);
		if (id == MachineRegistry.RESERVOIR) {
			tile = (TileEntityReservoir)world.getBlockTileEntity(x, y-1, z);
			if (tile == null)
				return false;
			if (tile.liquidID != liquidID && liquidID != -1)
				return false;
			return (tile.liquidLevel > 0);
		}
		return false;
	}

	public void harvest(World world, int x, int y, int z, int[] loc) {
		if (world.isRemote)
			return;
		if (!this.isSource(world, loc[0], loc[1], loc[2]))
			return;
		if (liquidLevel >= CAPACITY)
			return;
		//ModLoader.getMinecraftInstance().ingameGUI.addChatMessage(String.format("%d  %d  %d  %d", loc[0], loc[1], loc[2], world.getBlockId(loc[0], loc[1], loc[2])));
		if (!ReikaWorldHelper.is1p9InfiniteLava(world, loc[0], loc[1], loc[2]))
			world.setBlock(loc[0], loc[1], loc[2], 0);
		liquidLevel += RotaryConfig.MILLIBUCKET;
		world.markBlockForUpdate(loc[0], loc[1], loc[2]);
	}

	public boolean isSource(World world, int x, int y, int z) {
		//ModLoader.getMinecraftInstance().ingameGUI.addChatMessage(String.format("%d, %d, %d, %d", x,y,z,(int)id));
		//ReikaWorldHelper.legacySetBlockWithNotify(world, x, y, z, 49);
		boolean dmg0 = (world.getBlockMetadata(x, y, z) == 0);
		int liqid = world.getBlockId(x, y, z);
		if (liqid == 10 || liqid == 8)
			liqid++;
		boolean liq = (liqid == liquidID);
		if (liquidID == -1 && world.getBlockId(x, y, z) > 7 && world.getBlockId(x, y, z) < 12) {
			liq = true;
			liquidID = world.getBlockId(x, y, z);
			if (liquidID == 10)
				liquidID = 11;
			if (liquidID == 8)
				liquidID = 9;
		}
		//ModLoader.getMinecraftInstance().ingameGUI.addChatMessage(String.valueOf(liq)+"  "+String.valueOf(dmg0));
		return (dmg0 && liq);
	}

	public int[] findSourceBlock(World world, int x, int y, int z) {
		int[] loc = {x,y-1,z};
		int tries = 0;
		boolean found = false;
		//ModLoader.getMinecraftInstance().ingameGUI.addChatMessage(String.format("%d", world.getBlockId(x, y-1, z)));
		while (!this.isSource(world, loc[0], loc[1], loc[2]) && tries < 200 && !found) {
			//ModLoader.getMinecraftInstance().ingameGUI.addChatMessage(String.format("%d  %d  %d  %d", loc[0], loc[1], loc[2], world.getBlockId(loc[0], loc[1], loc[2])));
			loc[0] += -1 + par5Random.nextInt(3);
			loc[1] = y -6 + par5Random.nextInt(7);
			loc[2] += -1 + par5Random.nextInt(3);
			tries++;	// to prevent 1fps
			if (ReikaMathLibrary.py3d(loc[0]-x, 0, loc[2]-z) > 16) {
				loc[0] = x;
				loc[2] = z;
			}
		}
		//ModLoader.getMinecraftInstance().ingameGUI.addChatMessage(String.format("%d  %d  %d  %d", loc[0], loc[1], loc[2], world.getBlockId(loc[0], loc[1], loc[2])));
		return loc;
	}

	public void getIOSides(World world, int x, int y, int z, int metadata) {
		switch(metadata) {
		case 1:
			readx = xCoord+1;
			readz = zCoord;
			readx2 = xCoord-1;
			readz2 = zCoord;
			break;
		case 0:
			readz = zCoord+1;
			readx = xCoord;
			readx2 = xCoord;
			readz2 = zCoord-1;
			break;
		}
		ready = yCoord;
		ready2 = yCoord;
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound NBT)
	{
		super.writeToNBT(NBT);
		NBT.setInteger("liquidLevel", liquidLevel);
		NBT.setInteger("liquid", liquidID);
		NBT.setInteger("pressure", liquidPressure);
		NBT.setInteger("dmg", damage);
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(NBTTagCompound NBT)
	{
		super.readFromNBT(NBT);
		liquidLevel = NBT.getInteger("liquidLevel");
		liquidID = NBT.getInteger("liquid");
		liquidPressure = NBT.getInteger("pressure");
		damage = NBT.getInteger("dmg");
	}

	@Override
	public boolean hasModelTransparency() {
		return true;
	}

	@Override
	public RotaryModelBase getTEModel(World world, int x, int y, int z) {
		return new ModelPump();
	}

	@Override
	public void animateWithTick(World world, int x, int y, int z) {
		if (!this.isInWorld()) {
			phi = 0;
			return;
		}
		if (power < MINPOWER)
			return;
		phi += ReikaMathLibrary.doubpow(ReikaMathLibrary.logbase(omega+1, 2), 1.05);
	}

	@Override
	public int getMachineIndex() {
		return MachineRegistry.PUMP.ordinal();
	}

	@Override
	public int getRedstoneOverride() {
		return 15*liquidPressure/1000;
	}

	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return m == MachineRegistry.PIPE;
	}

	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry p, EnumLook side) {
		return !side.isTopOrBottom();
	}

	@Override
	public void onEMP() {}
}
