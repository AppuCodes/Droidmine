package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.ClientEngine;
import net.minecraft.client.model.ModelQuadruped;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderMooshroom;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.init.Blocks;

public class LayerMooshroomMushroom implements LayerRenderer<EntityMooshroom>
{
    private final RenderMooshroom mooshroomRenderer;

    public LayerMooshroomMushroom(RenderMooshroom mooshroomRendererIn)
    {
        this.mooshroomRenderer = mooshroomRendererIn;
    }

    public void doRenderLayer(EntityMooshroom entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale)
    {
        if (!entitylivingbaseIn.isChild() && !entitylivingbaseIn.isInvisible())
        {
            BlockRendererDispatcher blockrendererdispatcher = mooshroomRenderer.getRenderManager().mc.getBlockRendererDispatcher();
            this.mooshroomRenderer.bindTexture(TextureMap.locationBlocksTexture);
            GlStateManager.get().enableCull();
            GlStateManager.get().cullFace(1028);
            GlStateManager.get().pushMatrix();
            GlStateManager.get().scale(1.0F, -1.0F, 1.0F);
            GlStateManager.get().translate(0.2F, 0.35F, 0.5F);
            GlStateManager.get().rotate(42.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.get().pushMatrix();
            GlStateManager.get().translate(-0.5F, -0.5F, 0.5F);
            blockrendererdispatcher.renderBlockBrightness(Blocks.red_mushroom.getDefaultState(), 1.0F);
            GlStateManager.get().popMatrix();
            GlStateManager.get().pushMatrix();
            GlStateManager.get().translate(0.1F, 0.0F, -0.6F);
            GlStateManager.get().rotate(42.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.get().translate(-0.5F, -0.5F, 0.5F);
            blockrendererdispatcher.renderBlockBrightness(Blocks.red_mushroom.getDefaultState(), 1.0F);
            GlStateManager.get().popMatrix();
            GlStateManager.get().popMatrix();
            GlStateManager.get().pushMatrix();
            ((ModelQuadruped)this.mooshroomRenderer.getMainModel()).head.postRender(0.0625F);
            GlStateManager.get().scale(1.0F, -1.0F, 1.0F);
            GlStateManager.get().translate(0.0F, 0.7F, -0.2F);
            GlStateManager.get().rotate(12.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.get().translate(-0.5F, -0.5F, 0.5F);
            blockrendererdispatcher.renderBlockBrightness(Blocks.red_mushroom.getDefaultState(), 1.0F);
            GlStateManager.get().popMatrix();
            GlStateManager.get().cullFace(1029);
            GlStateManager.get().disableCull();
        }
    }

    public boolean shouldCombineTextures()
    {
        return true;
    }
}
