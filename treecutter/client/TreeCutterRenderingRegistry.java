package treecutter.client;

import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import treecutter.entity.EntityLumbering;

public class TreeCutterRenderingRegistry
{
	@SideOnly(Side.CLIENT)
	public static void registerRenderers()
	{
		RenderingRegistry.registerEntityRenderingHandler(EntityLumbering.class, RenderLumbering::new);
	}
}