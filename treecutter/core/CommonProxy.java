package treecutter.core;

import net.minecraftforge.fml.common.FMLCommonHandler;

public class CommonProxy
{
	public boolean isSinglePlayer()
	{
		return FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer();
	}
}