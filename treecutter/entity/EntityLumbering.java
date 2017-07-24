package treecutter.entity;

import java.util.PriorityQueue;

import com.google.common.collect.Queues;

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

	protected PriorityQueue<BlockPos> lumberTargets;

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
		checkPos = originPos;

		checkBranch();
	}

	public BlockPos getOriginPos()
	{
		return originPos;
	}

	public int getTargetCount()
	{
		return lumberTargets == null || !hasLeaf ? 0 : lumberTargets.size();
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

	protected boolean offer(BlockPos target)
	{
		if (validTarget(target) && !lumberTargets.contains(target))
		{
			checkPos = target;

			return lumberTargets.offer(target);
		}

		return false;
	}

	protected boolean validTarget(BlockPos target)
	{
		IBlockState state = world.getBlockState(target);

		if (state.getBlock().isAir(state, world, target) || state.getBlockHardness(world, target) < 0.0F)
		{
			return false;
		}

		if (target.getY() < originPos.getY())
		{
			return false;
		}

		if (TreeCutterUtils.isTreeLeaves(state) || state.getBlock().isLeaves(state, world, target))
		{
			hasLeaf = true;

			return false;
		}

		return TreeCutterUtils.isLogWood(state);
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

		if (lumberTargets == null)
		{
			checkForLumbering();
		}
		else if (lumberTargets.isEmpty() && currentPos != null)
		{
			int range = 4;

			for (BlockPos pos : BlockPos.getAllInBox(currentPos.add(range, range, range), currentPos.add(-range, -range, -range)))
			{
				IBlockState state = world.getBlockState(pos);
				Block block = state.getBlock();

				if (TreeCutterUtils.isTreeLeaves(state) || block.isLeaves(state, world, pos))
				{
					world.scheduleBlockUpdate(pos, block, 20 + rand.nextInt(8), 1);
					world.playEvent(2001, pos, Block.getStateId(state));
				}
			}

			setDead();

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