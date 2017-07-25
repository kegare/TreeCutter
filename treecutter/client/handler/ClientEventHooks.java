package treecutter.client.handler;

import com.google.common.base.Strings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import treecutter.config.TreeCutterConfig;
import treecutter.core.TreeCutter;
import treecutter.util.Version;

@SideOnly(Side.CLIENT)
public class ClientEventHooks
{
	@SubscribeEvent
	public void onConfigChanged(OnConfigChangedEvent event)
	{
		if (event.getModID().equals(TreeCutter.MODID))
		{
			TreeCutterConfig.syncConfig();

			if (event.isWorldRunning())
			{
				TreeCutterConfig.refreshBlocks();
				TreeCutterConfig.refreshItems();
			}
		}
	}

	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event)
	{
		GuiScreen gui = event.getGui();

		if (gui != null && GuiModList.class == gui.getClass())
		{
			String desc = I18n.format("treecutter.description");

			if (!Strings.isNullOrEmpty(desc))
			{
				TreeCutter.metadata.description = desc;
			}
		}
	}

	@SubscribeEvent
	public void onConnected(ClientConnectedToServerEvent event)
	{
		if (!TreeCutterConfig.versionNotice)
		{
			return;
		}

		Minecraft mc = FMLClientHandler.instance().getClient();
		ITextComponent message;
		ITextComponent name = new TextComponentString(TreeCutter.metadata.name);
		name.getStyle().setColor(TextFormatting.GRAY);

		if (Version.isOutdated())
		{
			ITextComponent latest = new TextComponentString(Version.getLatest().toString());
			latest.getStyle().setColor(TextFormatting.YELLOW);

			message = new TextComponentTranslation("treecutter.version.message", name);
			message.appendText(" : ").appendSibling(latest);
			message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, TreeCutter.metadata.url));

			mc.ingameGUI.getChatGUI().printChatMessage(message);
		}

		message = null;

		if (Version.DEV_DEBUG)
		{
			message = new TextComponentTranslation("treecutter.version.message.dev", name);
		}
		else if (Version.isBeta())
		{
			message = new TextComponentTranslation("treecutter.version.message.beta", name);
		}
		else if (Version.isAlpha())
		{
			message = new TextComponentTranslation("treecutter.version.message.alpha", name);
		}

		if (message != null)
		{
			mc.ingameGUI.getChatGUI().printChatMessage(message);
		}
	}
}