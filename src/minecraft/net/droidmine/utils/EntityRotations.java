package net.droidmine.utils;

import net.droidmine.MineBot;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

public class EntityRotations
{
    public static float[] getRotationsTo(EntityLivingBase target, MineBot bot)
    {
        double x = target.posX - bot.player().posX;
        double y = target.posY - bot.player().posY - (bot.player().getEyeHeight() - target.getEyeHeight());
        double z = target.posZ - bot.player().posZ;
        double hypot = MathHelper.sqrt_double(x * x + z * z);
        float yaw = (float) (MathHelper.func_181159_b(z, x) * 180 / Math.PI) - 90;
        float pitch = (float) -(MathHelper.func_181159_b(y, hypot) * 180 / Math.PI);
        return new float[] {yaw, pitch};
    }
    
    public static float[] getRotationsTo(BlockPos pos, MineBot bot, float yOffset)
    {
        double x = pos.getX() + 0.5D - bot.player().posX;
        double y = pos.getY() - 0.5D - yOffset - bot.player().posY;
        double z = pos.getZ() + 0.5D - bot.player().posZ;
        double hypot = MathHelper.sqrt_double(x * x + z * z);
        float yaw = (float) (MathHelper.func_181159_b(z, x) * 180 / Math.PI) - 90;
        float pitch = (float) -(MathHelper.func_181159_b(y, hypot) * 180 / Math.PI);
        return new float[] { yaw, pitch };
    }
}
