package net.minecraft.client.renderer.entity;

import net.minecraft.client.ClientEngine;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;

public class RenderFireball extends Render<EntityFireball>
{
    private float scale;

    public RenderFireball(RenderManager renderManagerIn, float scaleIn)
    {
        super(renderManagerIn);
        this.scale = scaleIn;
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity>) and this method has signature public void doRender(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doe
     */
    public void doRender(EntityFireball entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        GlStateManager.get().pushMatrix();
        this.bindEntityTexture(entity);
        GlStateManager.get().translate((float)x, (float)y, (float)z);
        GlStateManager.get().enableRescaleNormal();
        GlStateManager.get().scale(this.scale, this.scale, this.scale);
        TextureAtlasSprite textureatlassprite = mc.getRenderItem().getItemModelMesher().getParticleIcon(Items.fire_charge);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        float f = textureatlassprite.getMinU();
        float f1 = textureatlassprite.getMaxU();
        float f2 = textureatlassprite.getMinV();
        float f3 = textureatlassprite.getMaxV();
        float f4 = 1.0F;
        float f5 = 0.5F;
        float f6 = 0.25F;
        GlStateManager.get().rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.get().rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
        worldrenderer.pos(-0.5D, -0.25D, 0.0D).tex((double)f, (double)f3).normal(0.0F, 1.0F, 0.0F).endVertex();
        worldrenderer.pos(0.5D, -0.25D, 0.0D).tex((double)f1, (double)f3).normal(0.0F, 1.0F, 0.0F).endVertex();
        worldrenderer.pos(0.5D, 0.75D, 0.0D).tex((double)f1, (double)f2).normal(0.0F, 1.0F, 0.0F).endVertex();
        worldrenderer.pos(-0.5D, 0.75D, 0.0D).tex((double)f, (double)f2).normal(0.0F, 1.0F, 0.0F).endVertex();
        tessellator.draw();
        GlStateManager.get().disableRescaleNormal();
        GlStateManager.get().popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(EntityFireball entity)
    {
        return TextureMap.locationBlocksTexture;
    }
}
