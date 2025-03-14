package net.minecraft.client.renderer.entity.layers;

import java.util.Random;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.boss.EntityDragon;

public class LayerEnderDragonDeath implements LayerRenderer<EntityDragon>
{
    public void doRenderLayer(EntityDragon entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale)
    {
        if (entitylivingbaseIn.deathTicks > 0)
        {
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            RenderHelper.get().disableStandardItemLighting();
            float f = ((float)entitylivingbaseIn.deathTicks + partialTicks) / 200.0F;
            float f1 = 0.0F;

            if (f > 0.8F)
            {
                f1 = (f - 0.8F) / 0.2F;
            }

            Random random = new Random(432L);
            GlStateManager.get().disableTexture2D();
            GlStateManager.get().shadeModel(7425);
            GlStateManager.get().enableBlend();
            GlStateManager.get().blendFunc(770, 1);
            GlStateManager.get().disableAlpha();
            GlStateManager.get().enableCull();
            GlStateManager.get().depthMask(false);
            GlStateManager.get().pushMatrix();
            GlStateManager.get().translate(0.0F, -1.0F, -2.0F);

            for (int i = 0; (float)i < (f + f * f) / 2.0F * 60.0F; ++i)
            {
                GlStateManager.get().rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.get().rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.get().rotate(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.get().rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.get().rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.get().rotate(random.nextFloat() * 360.0F + f * 90.0F, 0.0F, 0.0F, 1.0F);
                float f2 = random.nextFloat() * 20.0F + 5.0F + f1 * 10.0F;
                float f3 = random.nextFloat() * 2.0F + 1.0F + f1 * 2.0F;
                worldrenderer.begin(6, DefaultVertexFormats.POSITION_COLOR);
                worldrenderer.pos(0.0D, 0.0D, 0.0D).color(255, 255, 255, (int)(255.0F * (1.0F - f1))).endVertex();
                worldrenderer.pos(-0.866D * (double)f3, (double)f2, (double)(-0.5F * f3)).color(255, 0, 255, 0).endVertex();
                worldrenderer.pos(0.866D * (double)f3, (double)f2, (double)(-0.5F * f3)).color(255, 0, 255, 0).endVertex();
                worldrenderer.pos(0.0D, (double)f2, (double)(1.0F * f3)).color(255, 0, 255, 0).endVertex();
                worldrenderer.pos(-0.866D * (double)f3, (double)f2, (double)(-0.5F * f3)).color(255, 0, 255, 0).endVertex();
                tessellator.draw();
            }

            GlStateManager.get().popMatrix();
            GlStateManager.get().depthMask(true);
            GlStateManager.get().disableCull();
            GlStateManager.get().disableBlend();
            GlStateManager.get().shadeModel(7424);
            GlStateManager.get().color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.get().enableTexture2D();
            GlStateManager.get().enableAlpha();
            RenderHelper.get().enableStandardItemLighting();
        }
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }
}
