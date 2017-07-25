package treecutter.config;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.Level;

import com.google.common.collect.Lists;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.GuiConfigEntries.IConfigEntry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import treecutter.client.config.SelectBlocksEntry;
import treecutter.client.config.SelectItemsEntry;
import treecutter.util.TreeCutterLog;
import treecutter.util.TreeCutterUtils;

public class TreeCutterConfig
{
	public static Configuration config;

	public static boolean treeCutter;
	public static boolean fancyLumbering;
	public static ConfigItems effectiveItems = new ConfigItems();
	public static ConfigBlocks excludedBlocks = new ConfigBlocks();
	public static double treeHardness;
	public static boolean sneakAction;
	public static boolean versionNotice;

	public static Class<? extends IConfigEntry> selectBlocks, selectItems;

	@SideOnly(Side.CLIENT)
	public static void initEntries()
	{
		selectBlocks = SelectBlocksEntry.class;
		selectItems = SelectItemsEntry.class;
	}

	public static void loadConfig()
	{
		File file = new File(Loader.instance().getConfigDir(), "TreeCutter.cfg");

		config = new Configuration(file, true);

		try
		{
			config.load();
		}
		catch (Exception e)
		{
			File dest = new File(file.getParentFile(), file.getName() + ".bak");

			if (dest.exists())
			{
				dest.delete();
			}

			file.renameTo(dest);

			TreeCutterLog.log(Level.ERROR, e, "A critical error occured reading the " + file.getName() + " file, defaults will be used - the invalid file is backed up at " + dest.getName());
		}
	}

	public static void syncConfig()
	{
		if (config == null)
		{
			loadConfig();
		}

		Side side = FMLLaunchHandler.side();

		String category = Configuration.CATEGORY_GENERAL;
		Property prop;
		String comment;
		List<String> propOrder = Lists.newArrayList();

		prop = config.get(category, "treeCutter", true);
		prop.setLanguageKey("treecutter.config." + prop.getName());
		comment = I18n.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.setComment(comment);
		propOrder.add(prop.getName());
		treeCutter = prop.getBoolean(treeCutter);

		if (side.isClient())
		{
			prop = config.get(category, "fancyLumbering", true);
			prop.setLanguageKey("treecutter.config." + prop.getName());
			comment = I18n.translateToLocal(prop.getLanguageKey() + ".tooltip");
			prop.setComment(comment);
			propOrder.add(prop.getName());
			fancyLumbering = prop.getBoolean(fancyLumbering);
		}

		prop = config.get(category, "effectiveItems", new String[0]);
		prop.setConfigEntryClass(selectItems);
		prop.setLanguageKey("treecutter.config." + prop.getName());
		comment = I18n.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.setComment(comment);
		propOrder.add(prop.getName());
		effectiveItems.setValues(prop.getStringList());

		prop = config.get(category, "excludedBlocks", new String[0]);
		prop.setConfigEntryClass(selectBlocks);
		prop.setLanguageKey("treecutter.config." + prop.getName());
		comment = I18n.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.setComment(comment);
		propOrder.add(prop.getName());
		excludedBlocks.setValues(prop.getStringList());

		prop = config.get(category, "treeHardness", 1.5D);
		prop.setMinValue(0.0D).setMaxValue(10.0D);
		prop.setLanguageKey("treecutter.config." + prop.getName());
		comment = I18n.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.setComment(comment);
		propOrder.add(prop.getName());
		treeHardness = prop.getDouble(treeHardness);

		prop = config.get(category, "sneakAction", false);
		prop.setLanguageKey("treecutter.config." + prop.getName());
		comment = I18n.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.setComment(comment);
		propOrder.add(prop.getName());
		sneakAction = prop.getBoolean(sneakAction);

		if (side.isClient())
		{
			prop = config.get(category, "versionNotice", true);
			prop.setLanguageKey("treecutter.config." + prop.getName());
			comment = I18n.translateToLocal(prop.getLanguageKey() + ".tooltip");
			prop.setComment(comment);
			propOrder.add(prop.getName());
			versionNotice = prop.getBoolean(versionNotice);
		}

		config.setCategoryPropertyOrder(category, propOrder);

		if (config.hasChanged())
		{
			config.save();
		}
	}

	public static void refreshBlocks()
	{
		if (excludedBlocks != null)
		{
			excludedBlocks.refreshBlocks();
		}
	}

	public static void refreshItems()
	{
		if (effectiveItems != null)
		{
			effectiveItems.refreshItems();
		}
	}

	public static boolean isEffectiveItem(ItemStack stack)
	{
		if (stack.isEmpty())
		{
			return false;
		}

		if (effectiveItems == null || effectiveItems.isEmpty())
		{
			return TreeCutterUtils.isAxe(stack);
		}

		return effectiveItems.hasItemStack(stack);
	}

	public static boolean isValidTargetBlock(IBlockState state)
	{
		if (excludedBlocks == null || excludedBlocks.isEmpty())
		{
			return true;
		}

		return !excludedBlocks.hasBlockState(state);
	}
}