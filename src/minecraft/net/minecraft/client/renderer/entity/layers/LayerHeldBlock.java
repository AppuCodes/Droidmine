package net.minecraft.client.renderer.entity.layers;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.ClientEngine;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderEnderman;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.monster.EntityEnderman;

public class LayerHeldBlock implements LayerRenderer<EntityEnderman>
{
    private final RenderEnderman endermanRenderer;
    private ClientEngine mc;

    public LayerHeldBlock(RenderEnderman endermanRendererIn, ClientEngine mc)
    {
        this.endermanRenderer = endermanRendererIn;
        this.mc = mc;
    }

    public void doRenderLayer(EntityEnderman entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale)
    {
        IBlockState iblockstate = entitylivingbaseIn.getHeldBlockState();

        if (iblockstate.getBlock().getMaterial() != Material.air)
        {
            BlockRendererDispatcher blockrendererdispatcher = mc.getBlockRendererDispatcher();
            GlStateManager.get().enableRescaleNormal();
            GlStateManager.get().pushMatrix();
            GlStateManager.get().translate(0.0F, 0.6875F, -0.75F);
            GlStateManager.get().rotate(20.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.get().rotate(45.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.get().translate(0.25F, 0.1875F, 0.25F);
            float f = 0.5F;
            GlStateManager.get().scale(-f, -f, f);
            int i = entitylivingbaseIn.getBrightnessForRender(partialTicks);
            int j = i % 65536;
            int k = i / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
            GlStateManager.get().color(1.0F, 1.0F, 1.0F, 1.0F);
            this.endermanRenderer.bindTexture(TextureMap.locationBlocksTexture);
            blockrendererdispatcher.renderBlockBrightness(iblockstate, 1.0F);
            GlStateManager.get().popMatrix();
            GlStateManager.get().disableRescaleNormal();
        }
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }
}
