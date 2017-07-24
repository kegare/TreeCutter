package treecutter.entity;

import java.util.PriorityQueue;
import java.util.Set;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import treecutter.capability.LumberingUnit;
import treecutter.util.TreeCutterUtils;

public class EntityLumbering extends Entity
{
	protected EntityPlayer entityPlayer;
	protected BlockPos originPos, checkPos, currentPos;

	protected boolean hasLeaf;

	protected int delayTime = 10;
	protected int breakCount;

	protected IBlockState leafState;

	protected PriorityQueue<BlockPos> lumberTargets, lumberLeaves;
	protected Set<BlockPos> decayedLeaves;

	public EntityLumbering(World world)
	{
		super(world);
	}

	public EntityLumbering(World world, EntityPlayer player)
	{
		this(world);
		this.entityPlayer = player;
	}

	public EntityLumbering(World world, EntityPlayer player, BlockPos pos)
	{
		this(world, player);
		this.originPos = pos;
		this.setPosition(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
	}

	public void checkForLumbering()
	{
		lumberTargets = Queues.newPriorityQueue();
		lumberLeaves = Queues.newPriorityQueue();
		decayedLeaves = Sets.newHashSet();

		checkPos = originPos;

		checkBranch();
	}

	public BlockPos getOriginPos()
	{
		return originPos;
	}

	public int getTargetCount()
	{
		return lumberTargets == null || lumberLeaves == null || lumberLeaves.size() < 1 ? 0 : lumberTargets.size();
	}

	public int getBreakCount()
	{
		return breakCount;
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

	@Override
	public void onUpdate()
	{
		if (delayTime > 0 || world.isRemote)
		{
			--delayTime;

			return;
		}

		if (entityPlayer == null || entityPlayer.isDead || originPos == null)
		{
			setDead();

			return;
		}

		if (lumberTargets == null || lumberLeaves == null)
		{
			checkForLumbering();

			return;
		}

		if (lumberLeaves.isEmpty())
		{
			setDead();

			return;
		}
		else if (lumberTargets.isEmpty())
		{
			currentPos = lumberLeaves.poll();

			if (currentPos != null)
			{
				for (BlockPos pos : BlockPos.getAllInBox(currentPos.add(4, 4, 4), currentPos.add(-4, -4, -4)))
				{
					IBlockState state = world.getBlockState(pos);

					if (TreeCutterUtils.isTreeLeaves(state, world, pos) && !decayedLeaves.contains(pos))
					{
						world.scheduleBlockUpdate(pos, state.getBlock(), 20 + rand.nextInt(8), 1);
						world.playEvent(2001, pos, Block.getStateId(state));

						decayedLeaves.add(pos);
					}
				}
			}

			return;
		}

		currentPos = lumberTargets.poll();

		if (currentPos != null && TreeCutterUtils.isLogWood(world.getBlockState(currentPos)))
		{
			if (harvestBlock(currentPos))
			{
				switch (++breakCount)
				{
					case 1:
						delayTime = 5;
						break;
					case 2:
						delayTime = 3;
						break;
					case 3:
						delayTime = 2;
						break;
					default:
						delayTime = 1;
				}
			}
		}
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
		int prevDamage = player.getHeldItemMainhand().getItemDamage();

		if (im.tryHarvestBlock(pos))
		{
			player.getHeldItemMainhand().setItemDamage(prevDamage);

			world.playEvent(2001, pos, Block.getStateId(state));

			return true;
		}

		return false;
	}

	@Override
	public void setDead()
	{
		super.setDead();

		LumberingUnit.get(entityPlayer).clearCache();
	}

	@Override
	protected void entityInit() {}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {}
}