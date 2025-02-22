package net.minecraft.client.particle;

import net.minecraft.client.ClientEngine;
import net.minecraft.world.World;

public class EntityCritFX extends EntitySmokeFX
{
    protected EntityCritFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double p_i1201_8_, double p_i1201_10_, double p_i1201_12_, ClientEngine mc)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, p_i1201_8_, p_i1201_10_, p_i1201_12_, 2.5F, mc);
    }

    public static class Factory implements IParticleFactory
    {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, ClientEngine mc, int... p_178902_15_)
        {
            return new EntityCritFX(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, mc);
        }
    }
}
