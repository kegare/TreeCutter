package treecutter.capability;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import treecutter.core.TreeCutter;

public class TreeCutterCapabilities
{
	@CapabilityInject(LumberingUnit.class)
	public static Capability<LumberingUnit> LUMBER_UNIT = null;

	public static void registerCapabilities()
	{
		CapabilityLumberingUnit.register();

		MinecraftForge.EVENT_BUS.register(new TreeCutterCapabilities());
	}

	public static <T> boolean isValid(Capability<T> capability)
	{
		return capability != null;
	}

	public static <T> boolean hasCapability(ICapabilityProvider entry, Capability<T> capability)
	{
		return entry != null && isValid(capability) && entry.hasCapability(capability, null);
	}

	@Nullable
	public static <T> T getCapability(ICapabilityProvider entry, Capability<T> capability)
	{
		return hasCapability(entry, capability) ? entry.getCapability(capability, null) : null;
	}

	@SubscribeEvent
	public void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event)
	{
		if (event.getObject() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)event.getObject();

			event.addCapability(new ResourceLocation(TreeCutter.MODID), new CapabilityLumberingUnit(player));
		}
	}
}