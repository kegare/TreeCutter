package treecutter.util;

import java.util.PriorityQueue;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;

import com.google.common.collect.Queues;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LumberingSnapshot
{
	private static final PriorityQueue<BlockPos> EMPTY_QUEUES = Queues.newPriorityQueue();

	private final World world;
	private final BlockPos originPos;

	private PriorityQueue<BlockPos> lumberTargets;
	private IBlockState lumberLeaf;

	private BlockPos checkPos;

	public LumberingSnapshot(World world, BlockPos pos)
	{
		this.world = world;
		this.originPos = pos;
	}

	public World getWorld()
	{
		return world;
	}

	public BlockPos getOriginPos()
	{
		return originPos;
	}

	public boolean isChecked()
	{
		return lumberTargets != null;
	}

	public boolean isEmpty()
	{
		return lumberTargets == null || lumberTargets.isEmpty();
	}

	public boolean equals(World worldIn, BlockPos pos)
	{
		if (worldIn == null || pos == null)
		{
			return false;
		}

		return world.provider.getDimensionType() == worldIn.provider.getDimensionType() && originPos.equals(pos);
	}

	public int getTargetCount()
	{
		return isEmpty() ? 0 : lumberTargets.size();
	}

	public PriorityQueue<BlockPos> getTargets()
	{
		return ObjectUtils.defaultIfNull(lumberTargets, EMPTY_QUEUES);
	}

	@Nullable
	public IBlockState getLeaf()
	{
		return lumberLeaf;
	}

	public void checkForLumbering()
	{
		lumberTargets = Queues.newPriorityQueue();
		checkPos = originPos;

		checkBranch();

		if (lumberLeaf == null)
		{
			lumberTargets.clear();
		}
	}

	private void checkBranch()
	{
		boolean flag;

		do
		{
			flag = false;

			for (BlockPos pos : BlockPos.getAllInBox(checkPos.add(1, 1, 1), checkPos.add(-1, -1, -1)))
			{
				if (offer(pos))
				{
					checkBranch();

					if (!flag)
					{
						flag = true;
					}
				}
			}
		}
		while (flag);
	}

	public boolean offer(BlockPos pos)
	{
		if (validTarget(pos) && !lumberTargets.contains(pos))
		{
			checkPos = pos;

			return lumberTargets.offer(pos);
		}

		if (validLeaf(pos) && lumberLeaf == null)
		{
			lumberLeaf = world.getBlockState(pos);
		}

		return false;
	}

	public boolean validTarget(BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);

		if (state.getBlock().isAir(state, world, pos) || state.getBlockHardness(world, pos) < 0.0F)
		{
			return false;
		}

		if (pos.getY() < originPos.getY())
		{
			return false;
		}

		return TreeCutterUtils.isLogWood(state);
	}

	public boolean validLeaf(BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);

		if (state.getBlock().isAir(state, world, pos))
		{
			return false;
		}

		if (pos.getY() <= originPos.getY())
		{
			return false;
		}

		return TreeCutterUtils.isTreeLeaves(state, world, pos);
	}
}