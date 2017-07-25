package treecutter.capability;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import treecutter.util.LumberingSnapshot;

public class LumberingUnit
{
	private final EntityPlayer player;

	private LumberingSnapshot snapshot;

	public LumberingUnit(EntityPlayer player)
	{
		this.player = player;
	}

	public LumberingSnapshot getLumbering(BlockPos pos)
	{
		return getLumbering(pos, true);
	}

	public LumberingSnapshot getLumbering(BlockPos pos, boolean refresh)
	{
		if (snapshot == null || refresh && !snapshot.equals(player.world, pos))
		{
			snapshot = new LumberingSnapshot(player.world, pos);
		}

		if (!snapshot.isChecked())
		{
			snapshot.checkForLumbering();
		}

		return snapshot;
	}

	@Nullable
	public LumberingSnapshot getCachedLumbering()
	{
		return snapshot;
	}

	public void clearCache()
	{
		snapshot = null;
	}

	public static LumberingUnit get(EntityPlayer player)
	{
		return ObjectUtils.defaultIfNull(TreeCutterCapabilities.getCapability(player, TreeCutterCapabilities.LUMBER_UNIT), new LumberingUnit(player));
	}
}