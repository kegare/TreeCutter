package treecutter.client.config;

import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import treecutter.config.TreeCutterConfig;
import treecutter.core.TreeCutter;

@SideOnly(Side.CLIENT)
public class TreeCutterGuiFactory implements IModGuiFactory
{
	public static boolean detailInfo = true;
	public static boolean instantFilter = true;

	@Override
	public void initialize(Minecraft mc) {}

	@Override
	public boolean hasConfigGui()
	{
		return true;
	}

	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen)
	{
		return new GuiConfig(parentScreen, getConfigElements(), TreeCutter.MODID, false, false, I18n.format("treecutter.config.title"));
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories()
	{
		return null;
	}

	private List<IConfigElement> getConfigElements()
	{
		return new ConfigElement(TreeCutterConfig.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements();
	}
}