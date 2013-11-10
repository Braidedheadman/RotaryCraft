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
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.ModRegistry.ModOreList;
import Reika.RotaryCraft.Auxiliary.ExtractorModOres;
import Reika.RotaryCraft.Auxiliary.ItemStacks;

public enum ExtractorBonus {

	GOLD(ItemStacks.goldoresolution, ItemStacks.silverflakes, 0.125F),
	IRON(ItemStacks.ironoresolution, ItemStacks.aluminumpowder, 0.125F),
	COAL(ItemStacks.coaloresolution, new ItemStack(Item.gunpowder), 0.0625F, ExtractorModOres.getFlakeProduct(ModOreList.URANIUM), ModList.INDUSTRIALCRAFT), //Nod to gregtech
	COPPER(ExtractorModOres.getSolutionProduct(ModOreList.COPPER), ItemStacks.ironoreflakes, 0.125F),
	LEAD(ExtractorModOres.getSolutionProduct(ModOreList.LEAD), ExtractorModOres.getFlakeProduct(ModOreList.FERROUS), 0.25F),
	NETHERGOLD(ExtractorModOres.getSolutionProduct(ModOreList.NETHERGOLD), ItemStacks.silverflakes, 0.125F),
	NETHERIRON(ExtractorModOres.getSolutionProduct(ModOreList.NETHERIRON), ItemStacks.aluminumpowder, 0.125F),
	SILVER(ExtractorModOres.getSolutionProduct(ModOreList.SILVER), ExtractorModOres.getFlakeProduct(ModOreList.IRIDIUM), 0.01F, ModList.INDUSTRIALCRAFT),
	PLATINUM(ExtractorModOres.getSolutionProduct(ModOreList.PLATINUM), ExtractorModOres.getFlakeProduct(ModOreList.IRIDIUM), 0.0625F, ModList.INDUSTRIALCRAFT),
	NETHERPLATINUM(ExtractorModOres.getSolutionProduct(ModOreList.NETHERPLATINUM), ExtractorModOres.getFlakeProduct(ModOreList.IRIDIUM), 0.125F, ModList.INDUSTRIALCRAFT),
	FERROUS(ExtractorModOres.getSolutionProduct(ModOreList.FERROUS), ExtractorModOres.getFlakeProduct(ModOreList.PLATINUM), 0.5F), //Since GregTech does it
	NETHERNICKEL(ExtractorModOres.getSolutionProduct(ModOreList.NETHERNICKEL), ExtractorModOres.getFlakeProduct(ModOreList.PLATINUM), 0.5F),
	SODALITE(ExtractorModOres.getSolutionProduct(ModOreList.SODALITE), ExtractorModOres.getFlakeProduct(ModOreList.ALUMINUM), 1F),
	PYRITE(ExtractorModOres.getSolutionProduct(ModOreList.PYRITE), ExtractorModOres.getFlakeProduct(ModOreList.SULFUR), 0.4F),
	BAUXITE(ExtractorModOres.getSolutionProduct(ModOreList.BAUXITE), ExtractorModOres.getFlakeProduct(ModOreList.ALUMINUM), 0.25F),
	IRIDIUM(ExtractorModOres.getSolutionProduct(ModOreList.IRIDIUM), ExtractorModOres.getFlakeProduct(ModOreList.PLATINUM), 0.5F),
	TUNGSTEN(ExtractorModOres.getSolutionProduct(ModOreList.TUNGSTEN), ItemStacks.ironoreflakes, 0.75F);

	private static final ExtractorBonus[] bonusList = ExtractorBonus.values();

	private ItemStack bonusItem;
	private ItemStack sourceItem;
	private float probability;
	private boolean hasMod = false;
	private ItemStack modBonus;
	private ModList modReq;
	private boolean hasReq = false;
	private ModList bonusReq;

	private ExtractorBonus(ItemStack in, ItemStack is, float chance, ModList req) {
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

	private ExtractorBonus(ItemStack in, ItemStack is, float chance, ItemStack mod, ModList condition) {
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
			//ReikaJavaLibrary.pConsole(bonusList[i]+" > "+in+"  >:<  "+is);
			if (ReikaItemHelper.matchStacks(is, in))
				return bonusList[i];
		}
		return null;
	}

	public ItemStack getBonus() {
		if (hasReq && !bonusReq.isLoaded())
			return null;
		if (hasMod) {
			if (modReq.isLoaded())
				return modBonus.copy();
		}
		return bonusItem.copy();
	}

	public void addBonusToItemStack(ItemStack[] inv, int slot) {
		ItemStack bonus = this.getBonus();
		if (bonus == null)
			return;
		int chance = (int)(1F/probability);
		Random r = new Random();
		if (r.nextInt(chance) > 0)
			return;
		if (inv[slot] != null) {
			if (!ReikaItemHelper.matchStacks(inv[slot], bonus))
				return;
			if (inv[slot].stackSize+bonus.stackSize > inv[slot].getMaxStackSize())
				return;
			inv[slot].stackSize += bonus.stackSize;
		}
		else {
			inv[slot] = bonus;
		}
	}

}
