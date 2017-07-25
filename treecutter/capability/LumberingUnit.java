package treecutter.capability;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import treecutter.entity.EntityLumbering;

public class LumberingUnit
{
	private final EntityPlayer player;

	private EntityLumbering lumberingCache;

	public LumberingUnit(EntityPlayer player)
	{
		this.player = player;
	}

	public EntityLumbering getLumbering(BlockPos pos)
	{
		return getLumbering(pos, true);
	}

	public EntityLumbering getLumbering(BlockPos pos, boolean refresh)
	{
		if (lumberingCache == null || lumberingCache.isDead || refresh && lumberingCache.getOriginPos() != pos)
		{
			lumberingCache = new EntityLumbering(player.world, player, pos);
		}

		if (!refresh && lumberingCache.getOriginPos() != pos)
		{
			return lumberingCache;
		}

		lumberingCache.checkForLumbering();

		return lumberingCache;
	}

	@Nullable
	public EntityLumbering getCachedLumbering()
	{
		return lumberingCache;
	}

	public void clearCache()
	{
		lumberingCache = null;
	}

	public static LumberingUnit get(EntityPlayer player)
	{
		return ObjectUtils.defaultIfNull(TreeCutterCapabilities.getCapability(player, TreeCutterCapabilities.LUMBER_UNIT), new LumberingUnit(player));
	}
}