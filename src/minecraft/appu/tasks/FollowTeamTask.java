package appu.tasks;

import net.droidmine.MineBot;
import net.droidmine.utils.EntityRotations;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class FollowTeamTask extends GameTask
{
    public String teamName;
    
    public void onTick(MineBot bot)
    {
        Entity teammate = bot.findEntity(teamName);
        
        if (teammate != null)
        {
            if (entityOnScreen(teammate, bot.player()))
            {
                float[] rotations = EntityRotations.getRotationsTo((EntityLivingBase) teammate, bot);
                if (bot.pathFinder().isActive()) bot.pathFinder().cancel();
                bot.player().rotationYaw = rotations[0];
                bot.player().rotationPitch = rotations[1];
                bot.options().keyBindSprint.setPressed(true);
                bot.options().keyBindForward.setPressed(bot.player().getDistanceToEntity(teammate) >= 2);
                bot.options().keyBindJump.setPressed(bot.player().getDistanceToEntity(teammate) >= 2);
            }
            
            else if (!bot.pathFinder().isActive())
            {
                bot.pathFinder().goTo(teammate.getPosition());
            }
        }
    }
    
    public boolean entityOnScreen(Entity entity, EntityPlayerSP player)
    {
        Vec3 playerPos = new Vec3(player.posX, player.posY + player.getEyeHeight(), player.posZ),
             targetPos = new Vec3(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
        
        MovingObjectPosition result = player.worldObj.rayTraceBlocks(playerPos, targetPos, false, true, false);
        return result == null || result.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY;
    }
}
