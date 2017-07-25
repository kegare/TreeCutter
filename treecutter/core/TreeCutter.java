package treecutter.core;

import java.util.Map;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;
import treecutter.capability.TreeCutterCapabilities;
import treecutter.client.TreeCutterRenderingRegistry;
import treecutter.client.handler.ClientEventHooks;
import treecutter.config.TreeCutterConfig;
import treecutter.handler.LumberingEventHooks;

@Mod
(
	modid = TreeCutter.MODID,
	guiFactory = "treecutter.client.config.TreeCutterGuiFactory",
	updateJSON = "https://raw.githubusercontent.com/kegare/TreeCutter/master/treecutter.json"
)
public class TreeCutter
{
	public static final String MODID = "treecutter";

	@Instance(MODID)
	public static TreeCutter instance;

	@SidedProxy(modId = MODID, clientSide = "treecutter.client.ClientProxy", serverSide = "treecutter.core.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(new LumberingEventHooks());

		if (event.getSide().isClient())
		{
			MinecraftForge.EVENT_BUS.register(new TreeCutterRegistration());
			MinecraftForge.EVENT_BUS.register(new ClientEventHooks());

			TreeCutterConfig.initEntries();

			TreeCutterRenderingRegistry.registerRenderers();

			TreeCutterCapabilities.registerCapabilities();
		}

		TreeCutterConfig.syncConfig();
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		TreeCutterConfig.refreshBlocks();
		TreeCutterConfig.refreshItems();
	}

	@NetworkCheckHandler
	public boolean netCheckHandler(Map<String, String> mods, Side side)
	{
		return true;
	}
}