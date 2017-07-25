package treecutter.handler;

import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import treecutter.core.TreeCutter;
import treecutter.util.TreeCutterUtils;

public class QuickLumbering
{
	private static final Random RANDOM = new Random();

	protected World world;
	protected EntityPlayer entityPlayer;
	protected BlockPos originPos, checkPos, currentPos;

	protected PriorityQueue<BlockPos> lumberTargets, lumberLeaves;
	protected Set<BlockPos> decayedLeaves;

	protected IBlockState leafState;

	public QuickLumbering(World world, EntityPlayer player, BlockPos pos)
	{
		this.world = world;
		this.entityPlayer = player;
		this.originPos = pos;
	}

	public BlockPos getOriginPos()
	{
		return originPos;
	}

	public void checkForLumbering()
	{
		lumberTargets = Queues.newPriorityQueue();
		lumberLeaves = Queues.newPriorityQueue();
		decayedLeaves = Sets.newHashSet();

		checkPos = originPos;

		checkBranch();
	}

	public int getTargetCount()
	{
		return lumberTargets == null || lumberLeaves == null || lumberLeaves.size() < 1 ? 0 : lumberTargets.size();
	}

	public void doLumbering()
	{
		while (!lumberTargets.isEmpty())
		{
			BlockPos pos = lumberTargets.poll();

			if (TreeCutterUtils.isLogWood(world.getBlockState(pos)))
			{
				harvestBlock(pos);
			}
		}

		while (!lumberLeaves.isEmpty())
		{
			BlockPos leafPos = lumberLeaves.poll();

			for (BlockPos pos : BlockPos.getAllInBox(leafPos.add(4, 4, 4), leafPos.add(-4, -4, -4)))
			{
				IBlockState state = world.getBlockState(pos);

				if (TreeCutterUtils.isTreeLeaves(state, world, pos) && !decayedLeaves.contains(pos))
				{
					world.scheduleBlockUpdate(pos, state.getBlock(), 20 + RANDOM.nextInt(8), 1);

					if (TreeCutter.proxy.isSinglePlayer())
					{
						world.playEvent(2001, pos, Block.getStateId(state));
					}

					decayedLeaves.add(pos);
				}
			}
		}
	}

	protected void checkBranch()
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

	protected boolean offer(BlockPos pos)
	{
		if (validTarget(pos) && !lumberTargets.contains(pos))
		{
			checkPos = pos;

			return lumberTargets.offer(pos);
		}

		if (validLeaf(pos) && !lumberLeaves.contains(pos))
		{
			if (lumberLeaves.isEmpty())
			{
				leafState = world.getBlockState(pos);
			}

			return lumberLeaves.offer(pos);
		}

		return false;
	}

	protected boolean validTarget(BlockPos pos)
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

	protected boolean validLeaf(BlockPos pos)
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

		if (leafState != null)
		{
			return state.getBlock() == leafState.getBlock() && state.getBlock().getMetaFromState(state) == leafState.getBlock().getMetaFromState(leafState);
		}

		return TreeCutterUtils.isTreeLeaves(state, world, pos);
	}

	protected boolean harvestBlock(BlockPos pos)
	{
		if (entityPlayer == null || !(entityPlayer instanceof EntityPlayerMP))
		{
			return false;
		}

		EntityPlayerMP player = (EntityPlayerMP)entityPlayer;
		PlayerInteractionManager im = player.interactionManager;
		IBlockState state = world.getBlockState(pos);

		if (im.tryHarvestBlock(pos))
		{
			if (TreeCutter.proxy.isSinglePlayer())
			{
				world.playEvent(2001, pos, Block.getStateId(state));
			}

			return true;
		}

		return false;
	}
}