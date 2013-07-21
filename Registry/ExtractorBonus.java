/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.RotaryCraft.Registry;

import java.util.Random;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import Reika.DragonAPI.Auxiliary.ModOreList;
import Reika.DragonAPI.Libraries.ReikaItemHelper;
import Reika.RotaryCraft.Auxiliary.ExtractorModOres;
import Reika.RotaryCraft.Auxiliary.ItemStacks;

public enum ExtractorBonus {

	GOLD(ItemStacks.goldoresolution, ItemStacks.silverflakes, 0.125F),
	IRON(ItemStacks.ironoresolution, ItemStacks.aluminumpowder, 0.125F),
	COAL(ItemStacks.coaloresolution, new ItemStack(Item.gunpowder), 0.125F, ExtractorModOres.getFlakeProduct(ModOreList.URANIUM), APIRegistry.IC2),
	COPPER(ExtractorModOres.getSolutionProduct(ModOreList.COPPER), ItemStacks.ironoreflakes, 0.125F),
	LEAD(ExtractorModOres.getSolutionProduct(ModOreList.LEAD), ExtractorModOres.getFlakeProduct(ModOreList.FERROUS), 0.25F),
	SILVER(ExtractorModOres.getSolutionProduct(ModOreList.SILVER), ExtractorModOres.getFlakeProduct(ModOreList.IRIDIUM), 0.01F, APIRegistry.GREGTECH);

	private static final ExtractorBonus[] bonusList = ExtractorBonus.values();

	private ItemStack bonusItem;
	private ItemStack sourceItem;
	private float probability;
	private boolean hasMod = false;
	private ItemStack modBonus;
	private APIRegistry modReq;
	private boolean hasReq = false;
	private APIRegistry bonusReq;

	private ExtractorBonus(ItemStack in, ItemStack is, float chance, APIRegistry req) {
		bonusItem = is.copy();
		sourceItem = in.copy();
		probability = chance;
		if (req != null) {
			hasReq = true;
			bonusReq = req;
		}
	}

	private ExtractorBonus(ItemStack in, ItemStack is, float chance) {
		this(in, is, chance, null);
	}

	private ExtractorBonus(ItemStack in, ItemStack is, float chance, ItemStack mod, APIRegistry condition) {
		bonusItem = is.copy();
		sourceItem = in.copy();
		probability = chance;
		hasMod = true;
		modBonus = mod.copy();
		modReq = condition;
	}

	public static ExtractorBonus getBonusForIngredient(ItemStack is) {
		for (int i = 0; i < bonusList.length; i++) {
			ItemStack in = bonusList[i].sourceItem;
			if (ReikaItemHelper.matchStacks(is, in))
				return bonusList[i];
		}
		return null;
	}

	public ItemStack getBonus() {
		if (hasReq && !bonusReq.conditionsMet())
			return null;
		if (hasMod) {
			if (modReq.conditionsMet())
				return modBonus.copy();
		}
		return bonusItem.copy();
	}

	public void addBonusToItemStack(ItemStack is) {
		if (this.getBonus() == null)
			return;
		if (is != null) {
			if (!ReikaItemHelper.matchStacks(is, bonusItem))
				return;
		}
		if (is.stackSize+this.getBonus().stackSize > is.getMaxStackSize())
			return;
		int chance = (int)(1F/probability);
		Random r = new Random();
		if (r.nextInt(chance) > 0)
			return;
		is.stackSize += this.getBonus().stackSize;
	}

}
