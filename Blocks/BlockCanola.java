/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.RotaryCraft.Blocks;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.RotaryCraft.Base.BlockBasic;
import Reika.RotaryCraft.Registry.ItemRegistry;

public class BlockCanola extends BlockBasic {

	Random rand = new Random();

	public BlockCanola(int ID) {
		super(ID, Material.plants);
		this.setHardness(0F);
		this.setResistance(0F);
		this.setLightValue(0F);
		this.setStepSound(soundGrassFootstep);
		//this.blockIndexInTexture = 36;
		////this.requiresSelfNotify[this.blockID] = true;
		this.setTickRandomly(true);
	}

	@Override
	public int idDropped(int par1, Random par2Random, int par3)
	{
		return 0;
	}

	@Override
	public final ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		return ItemRegistry.CANOLA.getStackOf();
	}

	@Override
	public boolean isBlockFoliage(World world, int x, int y, int z)
	{
		return true;
	}

	@Override
	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune)
	{
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		ret.add(this.getDrops(metadata));
		return ret;
	}

	/**
	 * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
	 */
	public int getBlockTextureFromSideAndMetadata(int par1, int par2)
	{
		if (par2 < 0)
		{
			par2 = 9;
		}
		if (par2 > 2)
			par2 += 2;
		if (par2 > 11)
			par2 = 11;

		return 0;//this.blockIndexInTexture + par2;
	}

	@Override
	public void updateTick(World par1World, int x, int y, int z, Random par5Random) {
		if ((par1World.getBlockLightValue(x, y, z) < 9 && !par1World.canBlockSeeTheSky(x, y, z)) || par1World.getBlockId(x, y-1, z) != Block.tilledField.blockID) {
			this.die(par1World, x, y, z);
		}
		else if (par1World.getBlockLightValue(x, y, z) >= 9)  {
			int metadata = par1World.getBlockMetadata(x, y, z);
			if (metadata < 9) {
				if (par5Random.nextInt(3) == 0) {
					metadata++;
					ReikaWorldHelper.legacySetBlockMetadataWithNotify(par1World, x, y, z, metadata);
				}
			}
		}
	}

	public void die(World world, int x, int y, int z) {
		world.setBlock(x, y, z, 0);
	}

	public ItemStack getDrops(int metadata) {
		int ndrops = 2+rand.nextInt(8)+rand.nextInt(5);
		if (metadata < 9)
			ndrops = 1;
		ItemStack items = ItemRegistry.CANOLA.getCraftedProduct(ndrops);
		return items;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer par5EntityPlayer, int par5, float f1, float f2, float f3) {
		if (par5EntityPlayer.getCurrentEquippedItem() != null) {
			if (par5EntityPlayer.getCurrentEquippedItem().getItem() instanceof ItemDye && par5EntityPlayer.getCurrentEquippedItem().getItemDamage() == 15) {
				ReikaWorldHelper.legacySetBlockMetadataWithNotify(world, x, y, z, 9);
				for (int i = 0; i < 16; i++)
					world.spawnParticle("happyVillager", x+rand.nextDouble(), y+rand.nextDouble(), z+rand.nextDouble(), 0, 0, 0);
				if (!par5EntityPlayer.capabilities.isCreativeMode)
					par5EntityPlayer.getCurrentEquippedItem().stackSize--;
				return true;
			}
		}
		return false;
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		this.testStable(world, x, y, z);
		super.onBlockAdded(world, x, y, z);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int changedid) {
		;//this.testStable(world, x, y, z);
	}

	public void testStable(World world, int x, int y, int z) {
		if ((world.getBlockLightValue(x, y, z) < 9 && !world.canBlockSeeTheSky(x, y, z)) || world.getBlockId(x, y-1, z) != Block.tilledField.blockID) {
			this.die(world, x, y, z);
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		return AxisAlignedBB.getBoundingBox(x + minX, y + minY, z + minZ, x + maxX, world.getBlockMetadata(x, y, z)/9F, z + maxZ);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4)
	{
		int var5 = par1IBlockAccess.getBlockMetadata(par2, par3, par4);
		float var6 = var5/9.0F;
		if (var6 < 0.125F)
			var6 = 0.125F;
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, var6, 1.0F);
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public int getRenderType() {
		return 6;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public int damageDropped(int par1)
	{
		return 0;
	}

	@Override
	public int quantityDropped(Random par1Random)
	{
		return 0;
	}

	@Override
	public Icon getIcon(int s, int meta) {
		return icons[meta][s];
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		for (int j = 0; j <= 9; j++) {
			for (int i = 0; i < 6; i++) {
				icons[j][i] = par1IconRegister.registerIcon("RotaryCraft:canola"+String.valueOf(j));
			}
		}
	}
}
