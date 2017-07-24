package treecutter.handler;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import treecutter.capability.LumberingUnit;
import treecutter.config.TreeCutterConfig;
import treecutter.entity.EntityLumbering;
import treecutter.util.TreeCutterUtils;

public class LumberingEventHooks
{
	@SubscribeEvent
	public void onBreakSpeed(BreakSpeed event)
	{
		EntityPlayer player = event.getEntityPlayer();
		ItemStack held = player.getHeldItemMainhand();

		if (!TreeCutterConfig.isEffectiveItem(held))
		{
			return;
		}

		if (TreeCutterConfig.sneakAction && !player.isSneaking())
		{
			return;
		}

		IBlockState state = event.getState();

		if (!TreeCutterUtils.isLogWood(state))
		{
			return;
		}

		BlockPos pos = event.getPos();
		EntityLumbering entity = LumberingUnit.get(player).getLumbering(pos);
		int count = entity.getTargetCount();

		if (count <= 0 || !player.capabilities.isCreativeMode && held.getMaxDamage() - held.getItemDamage() < count - 1)
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

		if (TreeCutterConfig.sneakAction && !player.isSneaking())
		{
			return;
		}

		IBlockState state = event.getState();

		if (!TreeCutterUtils.isLogWood(state))
		{
			return;
		}

		BlockPos pos = event.getPos();
		EntityLumbering entity = LumberingUnit.get(player).getCachedLumbering();

		if (entity == null)
		{
			if (player.capabilities.isCreativeMode)
			{
				entity = LumberingUnit.get(player).getLumbering(pos);
			}
			else return;
		}

		if (entity.getOriginPos() != pos)
		{
			return;
		}

		int count = entity.getTargetCount();

		if (count <= 0)
		{
			return;
		}

		if (!player.capabilities.isCreativeMode)
		{
			int amount = count - 1;

			if (held.getMaxDamage() - held.getItemDamage() < amount)
			{
				return;
			}

			held.damageItem(amount, player);
		}

		world.spawnEntity(entity);
	}
}