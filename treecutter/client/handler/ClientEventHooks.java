package treecutter.client.handler;

import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import treecutter.config.TreeCutterConfig;
import treecutter.core.TreeCutter;

@SideOnly(Side.CLIENT)
public class ClientEventHooks
{
	@SubscribeEvent
	public void onConfigChanged(OnConfigChangedEvent event)
	{
		if (event.getModID().equals(TreeCutter.MODID))
		{
			TreeCutterConfig.syncConfig();
		}
	}
}