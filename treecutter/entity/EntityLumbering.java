package treecutter.entity;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import treecutter.capability.LumberingUnit;
import treecutter.util.LumberingSnapshot;
import treecutter.util.TreeCutterUtils;

public class EntityLumbering extends Entity
{
	protected final EntityPlayer entityPlayer;
	protected final LumberingSnapshot snapshot;

	protected BlockPos currentPos;

	protected int delayTime = 10;
	protected int breakCount;

	protected final Set<BlockPos> decayedLeaves = Sets.newHashSet();

	public EntityLumbering(World world)
	{
		super(world);
		this.entityPlayer = null;
		this.snapshot = null;
	}

	public EntityLumbering(World world, EntityPlayer player, BlockPos pos)
	{
		this(new LumberingSnapshot(world, pos), player);
	}

	public EntityLumbering(LumberingSnapshot snapshot, EntityPlayer player)
	{
		super(snapshot.getWorld());
		this.entityPlayer = player;
		this.snapshot = snapshot;
		this.setPosition(snapshot.getOriginPos().getX() + 0.5D, snapshot.getOriginPos().getY() + 0.5D, snapshot.getOriginPos().getZ() + 0.5D);
	}

	public int getBreakCount()
	{
		return breakCount;
	}

	@Override
	public void onEntityUpdate()
	{
		if (delayTime > 0 || world.isRemote)
		{
			--delayTime;

			return;
		}

		if (entityPlayer == null || entityPlayer.isDead)
		{
			setDead();

			return;
		}

		if (!snapshot.isChecked())
		{
			snapshot.checkForLumbering();

			return;
		}

		currentPos = snapshot.getTargets().poll();

		if (currentPos == null)
		{
			setDead();

			return;
		}

		if (TreeCutterUtils.isLogWood(world.getBlockState(currentPos)))
		{
			int prevDamage = entityPlayer.getHeldItemMainhand().getItemDamage();

			if (TreeCutterUtils.harvestBlock(entityPlayer, currentPos))
			{
				entityPlayer.getHeldItemMainhand().setItemDamage(prevDamage);

				for (BlockPos pos : BlockPos.getAllInBox(currentPos.add(5, 3, 5), currentPos.add(-5, -1, -5)))
				{
					IBlockState state = world.getBlockState(pos);

					if (TreeCutterUtils.isTreeLeaves(state, world, pos) && !decayedLeaves.contains(pos))
					{
						world.scheduleBlockUpdate(pos, state.getBlock(), 30 + rand.nextInt(10), 1);

						if (rand.nextInt(5) == 0)
						{
							world.playEvent(2001, pos, Block.getStateId(state));
						}

						decayedLeaves.add(pos);
					}
				}

				switch (++breakCount)
				{
					case 1:
						delayTime = 4;
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

	@Override
	public void setDead()
	{
		super.setDead();

		if (entityPlayer != null)
		{
			LumberingUnit.get(entityPlayer).clearCache();
		}
	}

	@Override
	protected void entityInit() {}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {}
}