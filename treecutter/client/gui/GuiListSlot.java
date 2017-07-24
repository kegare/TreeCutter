package treecutter.client.gui;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;

import com.google.common.base.Strings;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

@SideOnly(Side.CLIENT)
public abstract class GuiListSlot extends GuiSlot
{
	public GuiListSlot(Minecraft mc, int width, int height, int top, int bottom, int slotHeight)
	{
		super(mc, width, height, top, bottom, slotHeight);
	}

	public void scrollUp()
	{
		int i = getAmountScrolled() % getSlotHeight();

		if (i == 0)
		{
			scrollBy(-getSlotHeight());
		}
		else
		{
			scrollBy(-i);
		}
	}

	public void scrollDown()
	{
		scrollBy(getSlotHeight() - getAmountScrolled() % getSlotHeight());
	}

	public void scrollToTop()
	{
		scrollBy(-getAmountScrolled());
	}

	public void scrollToEnd()
	{
		scrollBy(getSlotHeight() * getSize());
	}

	public abstract void scrollToSelected();

	public void scrollToPrev()
	{
		scrollBy(-(getAmountScrolled() % getSlotHeight() + (bottom - top) / getSlotHeight() * getSlotHeight()));
	}

	public void scrollToNext()
	{
		scrollBy(getAmountScrolled() % getSlotHeight() + (bottom - top) / getSlotHeight() * getSlotHeight());
	}

	public void drawItemStack(RenderItem renderer, ItemStack stack, int x, int y)
	{
		drawItemStack(renderer, stack, x, y, null, null);
	}

	public void drawItemStack(RenderItem renderer, ItemStack stack, int x, int y, FontRenderer fontRenderer, @Nullable String overlay)
	{
		if (stack.isEmpty())
		{
			return;
		}

		if (stack.getMetadata() == OreDictionary.WILDCARD_VALUE)
		{
			NBTTagCompound nbt = stack.getTagCompound();

			stack = new ItemStack(stack.getItem(), stack.getCount());
			stack.setTagCompound(nbt);
		}

		GlStateManager.enableRescaleNormal();
		RenderHelper.enableGUIStandardItemLighting();

		renderer.renderItemIntoGUI(stack, x, y);

		if (!Strings.isNullOrEmpty(overlay))
		{
			renderer.renderItemOverlayIntoGUI(ObjectUtils.defaultIfNull(stack.getItem().getFontRenderer(stack), fontRenderer), stack, x, y, overlay);
		}

		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
	}

	public void drawItemStack(RenderItem renderer, @Nullable IBlockState state, int x, int y)
	{
		drawItemStack(renderer, state, x, y, null, null);
	}

	public void drawItemStack(RenderItem renderer, @Nullable IBlockState state, int x, int y, FontRenderer fontRenderer, @Nullable String overlay)
	{
		if (state == null)
		{
			return;
		}

		Item item = Item.getItemFromBlock(state.getBlock());

		if (item == Items.AIR)
		{
			return;
		}

		int meta = state.getBlock().getMetaFromState(state);

		drawItemStack(renderer, new ItemStack(item, 1, meta), x, y, fontRenderer, overlay);
	}
}