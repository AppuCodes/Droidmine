package net.minecraft.client.particle;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.ClientEngine;
import net.minecraft.world.World;

public class EntityBlockDustFX extends EntityDiggingFX
{
    protected EntityBlockDustFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, IBlockState state, ClientEngine mc)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, state, mc);
        this.motionX = xSpeedIn;
        this.motionY = ySpeedIn;
        this.motionZ = zSpeedIn;
    }

    public static class Factory implements IParticleFactory
    {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, ClientEngine mc, int... p_178902_15_)
        {
            IBlockState iblockstate = Block.getStateById(p_178902_15_[0]);
            return iblockstate.getBlock().getRenderType() == -1 ? null : (new EntityBlockDustFX(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, iblockstate, mc)).func_174845_l();
        }
    }
}
