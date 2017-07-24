package treecutter.core;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import treecutter.entity.EntityLumbering;

public class TreeCutterRegistration
{
	@SubscribeEvent
	public void registerEntities(RegistryEvent.Register<EntityEntry> registry)
	{
		EntityRegistry.registerModEntity(new ResourceLocation(TreeCutter.MODID, "lumbering"), EntityLumbering.class, "Lumbering", 0, TreeCutter.instance, 32, 3, false);
	}
}