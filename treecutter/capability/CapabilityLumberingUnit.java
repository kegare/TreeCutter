package treecutter.capability;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class CapabilityLumberingUnit implements ICapabilityProvider
{
	private final LumberingUnit lumberUnit;

	public CapabilityLumberingUnit(EntityPlayer player)
	{
		this.lumberUnit = new LumberingUnit(player);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		return TreeCutterCapabilities.LUMBER_UNIT != null && capability == TreeCutterCapabilities.LUMBER_UNIT;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if (TreeCutterCapabilities.LUMBER_UNIT != null && capability == TreeCutterCapabilities.LUMBER_UNIT)
		{
			return TreeCutterCapabilities.LUMBER_UNIT.cast(lumberUnit);
		}

		return null;
	}

	public static void register()
	{
		CapabilityManager.INSTANCE.register(LumberingUnit.class,
			new Capability.IStorage<LumberingUnit>()
			{
				@Override
				public NBTBase writeNBT(Capability<LumberingUnit> capability, LumberingUnit instance, EnumFacing side)
				{
					return new NBTTagCompound();
				}

				@Override
				public void readNBT(Capability<LumberingUnit> capability, LumberingUnit instance, EnumFacing side, NBTBase nbt) {}
			},
			() -> new LumberingUnit(null)
		);
	}
}