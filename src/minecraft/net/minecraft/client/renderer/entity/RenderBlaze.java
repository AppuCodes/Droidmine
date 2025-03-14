package net.minecraft.client.renderer.entity;

import net.minecraft.client.ClientEngine;
import net.minecraft.client.model.ModelBlaze;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.util.ResourceLocation;

public class RenderBlaze extends RenderLiving<EntityBlaze>
{
    private static final ResourceLocation blazeTextures = new ResourceLocation("textures/entity/blaze.png");

    public RenderBlaze(RenderManager renderManagerIn, ClientEngine mc)
    {
        super(renderManagerIn, new ModelBlaze(), 0.5F, mc);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(EntityBlaze entity)
    {
        return blazeTextures;
    }
}
