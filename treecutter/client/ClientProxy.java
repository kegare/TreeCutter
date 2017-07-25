package treecutter.client;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import treecutter.core.CommonProxy;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	@Override
	public boolean isSinglePlayer()
	{
		return FMLClientHandler.instance().getClient().isSingleplayer();
	}
}