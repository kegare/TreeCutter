package treecutter.util;

import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.oredict.OreDictionary;
import treecutter.config.TreeCutterConfig;
import treecutter.core.TreeCutter;

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

		if (!TreeCutterConfig.isValidTargetBlock(state))
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
		return isTreeLeaves(state, null, null);
	}

	public static boolean isTreeLeaves(IBlockState state, @Nullable World world, @Nullable BlockPos pos)
	{
		if (state.getBlock() instanceof BlockAir)
		{
			return false;
		}

		if (!TreeCutterConfig.isValidTargetBlock(state))
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

		if (world != null && pos != null && state.getBlock().isLeaves(state, world, pos))
		{
			return true;
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

	public static boolean harvestBlock(EntityPlayer entityPlayer, BlockPos pos)
	{
		if (entityPlayer == null || !(entityPlayer instanceof EntityPlayerMP))
		{
			return false;
		}

		EntityPlayerMP player = (EntityPlayerMP)entityPlayer;
		WorldServer world = player.getServerWorld();
		PlayerInteractionManager im = player.interactionManager;
		IBlockState state = world.getBlockState(pos);

		if (im.tryHarvestBlock(pos))
		{
			if (TreeCutter.proxy.isSinglePlayer())
			{
				world.playEvent(2001, pos, Block.getStateId(state));
			}

			return true;
		}

		return false;
	}

	public static int compareWithNull(Object o1, Object o2)
	{
		return (o1 == null ? 1 : 0) - (o2 == null ? 1 : 0);
	}

	public static boolean blockFilter(@Nullable BlockMeta blockMeta, @Nullable String filter)
	{
		if (blockMeta == null || Strings.isNullOrEmpty(filter))
		{
			return false;
		}

		if (StringUtils.containsIgnoreCase(blockMeta.getName(), filter))
		{
			return true;
		}

		if (StringUtils.containsIgnoreCase(blockMeta.getMetaString(), filter))
		{
			return true;
		}

		Block block = blockMeta.getBlock();
		ItemStack stack = new ItemStack(block, 1, blockMeta.getMeta());

		if (stack.getItem() == Items.AIR)
		{
			if (StringUtils.containsIgnoreCase(block.getLocalizedName(), filter))
			{
				return true;
			}

			if (StringUtils.containsIgnoreCase(block.getUnlocalizedName(), filter))
			{
				return true;
			}
		}
		else
		{
			if (StringUtils.containsIgnoreCase(stack.getDisplayName(), filter))
			{
				return true;
			}

			if (StringUtils.containsIgnoreCase(stack.getUnlocalizedName(), filter))
			{
				return true;
			}
		}

		if (StringUtils.containsIgnoreCase(block.getHarvestTool(blockMeta.getBlockState()), filter))
		{
			return true;
		}

		return false;
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

	public static ModContainer getModContainer()
	{
		ModContainer mod = Loader.instance().getIndexedModList().get(TreeCutter.MODID);

		if (mod == null)
		{
			mod = Loader.instance().activeModContainer();

			if (mod == null || !TreeCutter.MODID.equals(mod.getModId()))
			{
				return new DummyModContainer(TreeCutter.metadata);
			}
		}

		return mod;
	}
}