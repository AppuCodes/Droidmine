package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.ModelWither;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderWither;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class LayerWitherAura implements LayerRenderer<EntityWither>
{
    private static final ResourceLocation WITHER_ARMOR = new ResourceLocation("textures/entity/wither/wither_armor.png");
    private final RenderWither witherRenderer;
    private final ModelWither witherModel = new ModelWither(0.5F);

    public LayerWitherAura(RenderWither witherRendererIn)
    {
        this.witherRenderer = witherRendererIn;
    }

    public void doRenderLayer(EntityWither entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale)
    {
        if (entitylivingbaseIn.isArmored())
        {
            GlStateManager.get().depthMask(!entitylivingbaseIn.isInvisible());
            this.witherRenderer.bindTexture(WITHER_ARMOR);
            GlStateManager.get().matrixMode(5890);
            GlStateManager.get().loadIdentity();
            float f = (float)entitylivingbaseIn.ticksExisted + partialTicks;
            float f1 = MathHelper.cos(f * 0.02F) * 3.0F;
            float f2 = f * 0.01F;
            GlStateManager.get().translate(f1, f2, 0.0F);
            GlStateManager.get().matrixMode(5888);
            GlStateManager.get().enableBlend();
            float f3 = 0.5F;
            GlStateManager.get().color(f3, f3, f3, 1.0F);
            GlStateManager.get().disableLighting();
            GlStateManager.get().blendFunc(1, 1);
            this.witherModel.setLivingAnimations(entitylivingbaseIn, p_177141_2_, p_177141_3_, partialTicks);
            this.witherModel.setModelAttributes(this.witherRenderer.getMainModel());
            this.witherModel.render(entitylivingbaseIn, p_177141_2_, p_177141_3_, p_177141_5_, p_177141_6_, p_177141_7_, scale);
            GlStateManager.get().matrixMode(5890);
            GlStateManager.get().loadIdentity();
            GlStateManager.get().matrixMode(5888);
            GlStateManager.get().enableLighting();
            GlStateManager.get().disableBlend();
        }
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }
}
