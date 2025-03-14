package appu.modules;

import java.util.ArrayList;
import java.util.Comparator;

import net.droidmine.MineBot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public class TargetUtil
{
    public static EntityLivingBase getTarget(MineBot bot)
    {
        ArrayList<EntityLivingBase> targets = new ArrayList<>();
        
        for (Entity entity : bot.world().loadedEntityList)
        {
            if (entity instanceof EntityLivingBase)
            {
                EntityLivingBase target = (EntityLivingBase) entity;
                
                if (target.getDistanceToEntity(bot.player()) <= 10 && valid(target, bot))
                    targets.add(target);
            }
        }
        
        if (targets.isEmpty()) return null;
        targets.sort(Comparator.comparingDouble(entity -> entity.getDistanceToEntity(bot.player())));
        return targets.get(0);
    }
    
    public static boolean valid(EntityLivingBase entity, MineBot bot)
    {
        return !entity.isEntityEqual(bot.player()) && entity.isEntityAlive() &&
               !entity.isInvisibleToPlayer(bot.player()) && !entity.isOnSameTeam(bot.player()) &&
               !entity.getDisplayName().getUnformattedText().contains("-") && !entity.getDisplayName().getUnformattedText().contains(" ");
    }
}
