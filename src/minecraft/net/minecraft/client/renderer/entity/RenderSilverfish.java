package net.minecraft.client.renderer.entity;

import net.minecraft.client.ClientEngine;
import net.minecraft.client.model.ModelSilverfish;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.util.ResourceLocation;

public class RenderSilverfish extends RenderLiving<EntitySilverfish>
{
    private static final ResourceLocation silverfishTextures = new ResourceLocation("textures/entity/silverfish.png");

    public RenderSilverfish(RenderManager renderManagerIn, ClientEngine mc)
    {
        super(renderManagerIn, new ModelSilverfish(), 0.3F, mc);
    }

    protected float getDeathMaxRotation(EntitySilverfish entityLivingBaseIn)
    {
        return 180.0F;
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(EntitySilverfish entity)
    {
        return silverfishTextures;
    }
}
