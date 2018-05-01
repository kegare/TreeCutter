package treecutter.handler;

import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import treecutter.capability.LumberingUnit;
import treecutter.config.TreeCutterConfig;
import treecutter.core.TreeCutter;
import treecutter.entity.EntityLumbering;
import treecutter.util.LumberingSnapshot;
import treecutter.util.TreeCutterUtils;

public class LumberingEventHooks
{
	private static final Random RANDOM = new Random();
	private static final Set<String> SNEAK_INFO = Sets.newHashSet();

	private boolean lumbering;

	@SubscribeEvent
	public void onBreakSpeed(BreakSpeed event)
	{
		if (!TreeCutterConfig.treeCutter)
		{
			return;
		}

		EntityPlayer player = event.getEntityPlayer();

		if (!TreeCutter.proxy.isSinglePlayer())
		{
			return;
		}

		ItemStack held = player.getHeldItemMainhand();

		if (!TreeCutterConfig.isEffectiveItem(held))
		{
			return;
		}

		if (!TreeCutterUtils.isLogWood(event.getState()))
		{
			return;
		}

		if (TreeCutterConfig.sneakAction && !player.isSneaking())
		{
			if (!player.world.isRemote && SNEAK_INFO.add(player.getCachedUniqueIdString()))
			{
				player.sendStatusMessage(new TextComponentTranslation("treecutter.message.sneak"), true);
			}

			return;
		}

		if (TreeCutterConfig.nonSneakAction && player.isSneaking())
		{
			return;
		}

		LumberingSnapshot snapshot = LumberingUnit.get(player).getLumbering(event.getPos());

		if (snapshot.isEmpty())
		{
			return;
		}

		int count = snapshot.getTargetCount();

		if (!player.capabilities.isCreativeMode && held.getMaxDamage() - held.getItemDamage() < count - 1)
		{
			return;
		}

		float hardness = (float)TreeCutterConfig.treeHardness;

		if (hardness <= 0.0F)
		{
			return;
		}

		float speed = event.getNewSpeed();
		float power = hardness * 1.7145F;
		float newSpeed = Math.min(speed / (count * (0.5F - power * 0.1245F)), speed);

		event.setNewSpeed(newSpeed);
	}

	@SubscribeEvent
	public void onBlockBreak(BreakEvent event)
	{
		if (lumbering || !TreeCutterConfig.treeCutter)
		{
			return;
		}

		World world = event.getWorld();

		if (world.isRemote)
		{
			return;
		}

		EntityPlayer player = event.getPlayer();
		ItemStack held = player.getHeldItemMainhand();

		if (!TreeCutterConfig.isEffectiveItem(held))
		{
			return;
		}

		if (!TreeCutterUtils.isLogWood(event.getState()))
		{
			return;
		}

		if (TreeCutterConfig.sneakAction && !player.isSneaking())
		{
			if (SNEAK_INFO.add(player.getCachedUniqueIdString()))
			{
				player.sendStatusMessage(new TextComponentString(I18n.translateToLocal("treecutter.message.sneak")), true);
			}

			return;
		}

		if (TreeCutterConfig.nonSneakAction && player.isSneaking())
		{
			return;
		}

		BlockPos originPos = event.getPos();

		if (!TreeCutterConfig.fancyLumbering || !TreeCutter.proxy.isSinglePlayer())
		{
			LumberingSnapshot snapshot = LumberingUnit.get(player).getLumbering(originPos);

			if (snapshot.isEmpty())
			{
				return;
			}

			lumbering = true;

			Set<BlockPos> decayedLeaves = Sets.newHashSet();

			while (!snapshot.getTargets().isEmpty())
			{
				BlockPos target = snapshot.getTargets().poll();

				if (TreeCutterUtils.isLogWood(world.getBlockState(target)) && TreeCutterUtils.harvestBlock(player, target))
				{
					for (BlockPos pos : BlockPos.getAllInBox(target.add(5, 3, 5), target.add(-5, -1, -5)))
					{
						IBlockState state = world.getBlockState(pos);

						if (TreeCutterUtils.isTreeLeaves(state, world, pos) && !decayedLeaves.contains(pos))
						{
							world.scheduleBlockUpdate(pos, state.getBlock(), 30 + RANDOM.nextInt(10), 1);

							if (TreeCutter.proxy.isSinglePlayer() && RANDOM.nextInt(5) == 0)
							{
								world.playEvent(2001, pos, Block.getStateId(state));
							}

							decayedLeaves.add(pos);
						}
					}
				}
			}

			lumbering = false;
		}
		else
		{
			LumberingSnapshot snapshot = LumberingUnit.get(player).getLumbering(originPos, false);

			if (snapshot.isEmpty() || !snapshot.equals(world, originPos))
			{
				return;
			}

			if (!player.capabilities.isCreativeMode)
			{
				int amount = snapshot.getTargetCount() - 1;

				if (held.getMaxDamage() - held.getItemDamage() < amount)
				{
					return;
				}

				held.damageItem(amount, player);
			}

			world.spawnEntity(new EntityLumbering(snapshot, player));
		}
	}

	@SubscribeEvent
	public void playerLoggedOut(PlayerLoggedOutEvent event)
	{
		SNEAK_INFO.remove(event.player.getCachedUniqueIdString());
	}
}