package net.minecraft.client.particle;

import net.minecraft.client.ClientEngine;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class MobAppearance extends EntityFX
{
    private EntityLivingBase entity;

    protected MobAppearance(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, ClientEngine mc)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D, mc);
        this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
        this.motionX = this.motionY = this.motionZ = 0.0D;
        this.particleGravity = 0.0F;
        this.particleMaxAge = 30;
    }

    public int getFXLayer()
    {
        return 3;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        super.onUpdate();

        if (this.entity == null)
        {
            EntityGuardian entityguardian = new EntityGuardian(this.worldObj);
            entityguardian.setElder();
            this.entity = entityguardian;
        }
    }

    /**
     * Renders the particle
     */
    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float p_180434_4_, float p_180434_5_, float p_180434_6_, float p_180434_7_, float p_180434_8_)
    {
        if (this.entity != null)
        {
            RenderManager rendermanager = mc.getRenderManager();
            rendermanager.setRenderPosition(EntityFX.interpPosX, EntityFX.interpPosY, EntityFX.interpPosZ);
            float f = 0.42553192F;
            float f1 = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge;
            GlStateManager.get().depthMask(true);
            GlStateManager.get().enableBlend();
            GlStateManager.get().enableDepth();
            GlStateManager.get().blendFunc(770, 771);
            float f2 = 240.0F;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, f2, f2);
            GlStateManager.get().pushMatrix();
            float f3 = 0.05F + 0.5F * MathHelper.sin(f1 * (float)Math.PI);
            GlStateManager.get().color(1.0F, 1.0F, 1.0F, f3);
            GlStateManager.get().translate(0.0F, 1.8F, 0.0F);
            GlStateManager.get().rotate(180.0F - entityIn.rotationYaw, 0.0F, 1.0F, 0.0F);
            GlStateManager.get().rotate(60.0F - 150.0F * f1 - entityIn.rotationPitch, 1.0F, 0.0F, 0.0F);
            GlStateManager.get().translate(0.0F, -0.4F, -1.5F);
            GlStateManager.get().scale(f, f, f);
            this.entity.rotationYaw = this.entity.prevRotationYaw = 0.0F;
            this.entity.rotationYawHead = this.entity.prevRotationYawHead = 0.0F;
            rendermanager.renderEntityWithPosYaw(this.entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks);
            GlStateManager.get().popMatrix();
            GlStateManager.get().enableDepth();
        }
    }

    public static class Factory implements IParticleFactory
    {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, ClientEngine mc, int... p_178902_15_)
        {
            return new MobAppearance(worldIn, xCoordIn, yCoordIn, zCoordIn, mc);
        }
    }
}
