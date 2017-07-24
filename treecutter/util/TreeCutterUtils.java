package treecutter.util;

import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class TreeCutterUtils
{
	private static final Set<IBlockState> LOG_DICT_CACHE = Sets.newHashSet();
	private static final Set<IBlockState> LEAVES_DICT_CACHE = Sets.newHashSet();

	public static boolean isItemEqual(ItemStack target, ItemStack input)
	{
		if (target.getHasSubtypes())
		{
			return OreDictionary.itemMatches(target, input, false);
		}

		return target.getItem() == input.getItem();
	}

	public static boolean isLogWood(IBlockState state)
	{
		if (state.getBlock() instanceof BlockAir)
		{
			return false;
		}

		if (state.getBlock() instanceof BlockLog)
		{
			return true;
		}

		if (LOG_DICT_CACHE.contains(state))
		{
			return true;
		}

		for (ItemStack stack : OreDictionary.getOres("logWood", false))
		{
			if (isItemEqual(new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)), stack))
			{
				LOG_DICT_CACHE.add(state);

				return true;
			}
		}

		return false;
	}

	public static boolean isTreeLeaves(IBlockState state)
	{
		if (state.getBlock() instanceof BlockAir)
		{
			return false;
		}

		if (state.getBlock() instanceof BlockLeaves || state.getMaterial() == Material.LEAVES)
		{
			return true;
		}

		if (LEAVES_DICT_CACHE.contains(state))
		{
			return true;
		}

		for (ItemStack stack : OreDictionary.getOres("treeLeaves", false))
		{
			if (isItemEqual(new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)), stack))
			{
				LEAVES_DICT_CACHE.add(state);

				return true;
			}
		}

		return false;
	}

	public static boolean isAxe(ItemStack stack)
	{
		if (stack.getItem() instanceof ItemAxe)
		{
			return true;
		}

		if (stack.getItem().getToolClasses(stack).contains("axe"))
		{
			return true;
		}

		return false;
	}

	public static int compareWithNull(Object o1, Object o2)
	{
		return (o1 == null ? 1 : 0) - (o2 == null ? 1 : 0);
	}

	public static boolean itemFilter(@Nullable ItemMeta itemMeta, @Nullable String filter)
	{
		if (itemMeta == null || Strings.isNullOrEmpty(filter))
		{
			return false;
		}

		if (StringUtils.containsIgnoreCase(itemMeta.getName(), filter))
		{
			return true;
		}

		ItemStack stack = itemMeta.getItemStack();

		if (StringUtils.containsIgnoreCase(stack.getDisplayName(), filter))
		{
			return true;
		}

		if (StringUtils.containsIgnoreCase(stack.getUnlocalizedName(), filter))
		{
			return true;
		}

		if (stack.getItem().getToolClasses(stack).contains(filter))
		{
			return true;
		}

		return false;
	}
}