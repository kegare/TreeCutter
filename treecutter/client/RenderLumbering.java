package treecutter.client;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import treecutter.entity.EntityLumbering;

@SideOnly(Side.CLIENT)
public class RenderLumbering extends Render<EntityLumbering>
{
	protected RenderLumbering(RenderManager renderManager)
	{
		super(renderManager);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityLumbering entity)
	{
		return null;
	}
}