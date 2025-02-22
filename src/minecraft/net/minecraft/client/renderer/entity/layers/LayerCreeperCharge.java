package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.ModelCreeper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderCreeper;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.ResourceLocation;

public class LayerCreeperCharge implements LayerRenderer<EntityCreeper>
{
    private static final ResourceLocation LIGHTNING_TEXTURE = new ResourceLocation("textures/entity/creeper/creeper_armor.png");
    private final RenderCreeper creeperRenderer;
    private final ModelCreeper creeperModel = new ModelCreeper(2.0F);

    public LayerCreeperCharge(RenderCreeper creeperRendererIn)
    {
        this.creeperRenderer = creeperRendererIn;
    }

    public void doRenderLayer(EntityCreeper entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale)
    {
        if (entitylivingbaseIn.getPowered())
        {
            boolean flag = entitylivingbaseIn.isInvisible();
            GlStateManager.get().depthMask(!flag);
            this.creeperRenderer.bindTexture(LIGHTNING_TEXTURE);
            GlStateManager.get().matrixMode(5890);
            GlStateManager.get().loadIdentity();
            float f = (float)entitylivingbaseIn.ticksExisted + partialTicks;
            GlStateManager.get().translate(f * 0.01F, f * 0.01F, 0.0F);
            GlStateManager.get().matrixMode(5888);
            GlStateManager.get().enableBlend();
            float f1 = 0.5F;
            GlStateManager.get().color(f1, f1, f1, 1.0F);
            GlStateManager.get().disableLighting();
            GlStateManager.get().blendFunc(1, 1);
            this.creeperModel.setModelAttributes(this.creeperRenderer.getMainModel());
            this.creeperModel.render(entitylivingbaseIn, p_177141_2_, p_177141_3_, p_177141_5_, p_177141_6_, p_177141_7_, scale);
            GlStateManager.get().matrixMode(5890);
            GlStateManager.get().loadIdentity();
            GlStateManager.get().matrixMode(5888);
            GlStateManager.get().enableLighting();
            GlStateManager.get().disableBlend();
            GlStateManager.get().depthMask(flag);
        }
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }
}
