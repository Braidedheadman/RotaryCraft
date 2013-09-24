/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.RotaryCraft.Auxiliary;

import java.awt.Color;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.src.BaseMod;
import net.minecraft.src.ModLoader;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import Reika.DragonAPI.ModInteract.MekToolHandler;
import Reika.DragonAPI.ModInteract.TinkerToolHandler;
import Reika.RotaryCraft.GuiHandler;
import Reika.RotaryCraft.Base.RotaryCraftTileEntity;
import Reika.RotaryCraft.Registry.GuiRegistry;
import Reika.RotaryCraft.Registry.MachineRegistry;
import Reika.RotaryCraft.TileEntities.Production.TileEntityEngine;
import Reika.RotaryCraft.TileEntities.Transmission.TileEntitySplitter;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class RotaryAux {
	public static boolean[] liquidReceiver = new boolean[256];

	public static int blockModel;

	public static final String items1png = "C:/Users/Reika/Downloads/mcp/src/minecraft/Reika/RotaryCraft/Textures/Items/items.png";
	public static final String items2png = "C:/Users/Reika/Downloads/mcp/src/minecraft/Reika/RotaryCraft/Textures/Items/items2.png";
	public static final String items3png = "C:/Users/Reika/Downloads/mcp/src/minecraft/Reika/RotaryCraft/Textures/Items/items3.png";
	public static final String modexpng = "C:/Users/Reika/Downloads/mcp/src/minecraft/Reika/RotaryCraft/Textures/Items/modextracts.png";
	public static final String modingotpng = "C:/Users/Reika/Downloads/mcp/src/minecraft/Reika/RotaryCraft/Textures/Items/modingots.png";
	public static final String terrainpng = "C:/Users/Reika/Downloads/mcp/src/minecraft/Reika/RotaryCraft/Textures/Terrain/textures.png";
	public static final String tileentdir = "C:/Users/Reika/Downloads/mcp/src/minecraft/Reika/RotaryCraft/Textures/TileEntityTex/";
	public static final String mididir = "C:/Users/Reika/Downloads/mcp/src/minecraft/Reika/RotaryCraft/MIDIs/";

	public static final Color[] sideColors = {Color.CYAN, Color.BLUE, Color.YELLOW, Color.BLACK, new Color(255, 120, 0), Color.MAGENTA};
	public static final String[] sideColorNames = {"CYAN", "BLUE", "YELLOW", "BLACK", "ORANGE", "MAGENTA"};

	public static void initializeModel (BaseMod mod) {
		blockModel = ModLoader.getUniqueBlockModelID(mod, true);
	}

	public static final boolean hasGui(World world, int x, int y, int z, EntityPlayer ep) {
		MachineRegistry m = MachineRegistry.getMachine(world, x, y, z);
		if (m == MachineRegistry.ENGINE) {
			TileEntityEngine te = (TileEntityEngine)world.getBlockTileEntity(x, y, z);
			if (te == null)
				return false;
			if (te.type == null)
				return false;
			if (!te.type.hasGui())
				return false;
			return true;
		}
		if (m == MachineRegistry.SPLITTER) {
			TileEntitySplitter te = (TileEntitySplitter)world.getBlockTileEntity(x, y, z);
			return (te.getBlockMetadata() >= 8);
		}
		if (m == MachineRegistry.SCREEN)
			return !ep.isSneaking();
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			Object GUI = GuiHandler.instance.getClientGuiElement(GuiRegistry.MACHINE.ordinal(), ep, world, x, y, z);
			if (GUI != null)
				return true;
		}
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			Object GUI = GuiHandler.instance.getServerGuiElement(GuiRegistry.MACHINE.ordinal(), ep, world, x, y, z);
			if (GUI != null)
				return true;
		}
		return false;
	}

	public static int get4SidedMetadataFromPlayerLook(EntityLiving ep) {
		int i = MathHelper.floor_double((ep.rotationYaw * 4F) / 360F + 0.5D);
		while (i > 3)
			i -= 4;
		while (i < 0)
			i += 4;
		switch (i) {
		case 0:
			return 2;
		case 1:
			return 1;
		case 2:
			return 3;
		case 3:
			return 0;
		}
		return -1;
	}

	public static int get6SidedMetadataFromPlayerLook(EntityLiving ep) {
		if (MathHelper.abs(ep.rotationPitch) < 60) {
			int i = MathHelper.floor_double((ep.rotationYaw * 4F) / 360F + 0.5D);
			while (i > 3)
				i -= 4;
			while (i < 0)
				i += 4;
			switch (i) {
			case 0:
				return 2;
			case 1:
				return 1;
			case 2:
				return 3;
			case 3:
				return 0;
			}
		}
		else { //Looking up/down
			if (ep.rotationPitch > 0)
				return 4; //set to up
			else
				return 5; //set to down
		}
		return -1;
	}

	public static int get2SidedMetadataFromPlayerLook(EntityLiving ep) {
		int i = MathHelper.floor_double((ep.rotationYaw * 4F) / 360F + 0.5D);
		while (i > 3)
			i -= 4;
		while (i < 0)
			i += 4;

		switch (i) {
		case 0:
			return 0;
		case 1:
			return 1;
		case 2:
			return 0;
		case 3:
			return 1;
		}
		return -1;
	}

	public static void flipXMetadatas(TileEntity t) {
		if (!(t instanceof RotaryCraftTileEntity))
			return;
		RotaryCraftTileEntity te = (RotaryCraftTileEntity)t;
		int m = te.getBlockMetadata();
		switch (m) {
		case 0:
			te.setBlockMetadata(1);
			break;
		case 1:
			te.setBlockMetadata(0);
			break;
		}
	}

	public static void flipZMetadatas(TileEntity t) {
		if (!(t instanceof RotaryCraftTileEntity))
			return;
		RotaryCraftTileEntity te = (RotaryCraftTileEntity)t;
		int m = te.getBlockMetadata();
		switch (m) {
		case 2:
			te.setBlockMetadata(3);
			break;
		case 3:
			te.setBlockMetadata(2);
			break;
		}
	}

	public static boolean canHarvestSteelMachine(EntityPlayer ep) {
		if (ep.capabilities.isCreativeMode)
			return false;
		ItemStack eitem = ep.inventory.getCurrentItem();
		if (eitem == null)
			return false;
		if (TinkerToolHandler.getInstance().isPick(eitem) && TinkerToolHandler.getInstance().isStoneOrBetter(eitem))
			return true;
		if (MekToolHandler.getInstance().isPickTypeTool(eitem))
			return true;
		if (!(eitem.getItem() instanceof ItemPickaxe))
			return false;
		if (eitem.getItem().canHarvestBlock(Block.oreIron, eitem))
			return true;
		return false;
	}
}
