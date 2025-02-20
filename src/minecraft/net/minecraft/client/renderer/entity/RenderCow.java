package net.minecraft.client.renderer.entity;

import net.minecraft.client.ClientEngine;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.util.ResourceLocation;

public class RenderCow extends RenderLiving<EntityCow>
{
    private static final ResourceLocation cowTextures = new ResourceLocation("textures/entity/cow/cow.png");

    public RenderCow(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn, ClientEngine mc)
    {
        super(renderManagerIn, modelBaseIn, shadowSizeIn, mc);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(EntityCow entity)
    {
        return cowTextures;
    }
}
