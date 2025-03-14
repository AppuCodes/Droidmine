package appu.tasks;

import net.droidmine.MineBot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.util.BlockPos;

public class CollectIronTask extends GameTask
{
    private BlockPos generator = null;
    private boolean callGoto = false;
    
    public void onTick(MineBot bot)
    {
        if (generator == null)
        {
            for (Entity entity : bot.world().loadedEntityList)
            {
                if (entity instanceof EntityItem)
                {
                    EntityItem item = (EntityItem) entity;
                    
                    if (item.getEntityItem() != null && item.getEntityItem().getItem().equals(Items.iron_ingot)
                            && item.getDistanceToEntity(bot.player()) < 15)
                    {
                        generator = item.getPosition();
                        break;
                    }
                }
            }
        }
        
        else
        {
            if (!bot.pathFinder().isActive())
            {
                bot.pathFinder().goTo(generator);
                callGoto = true;
            }
        }
        
        if (generator != null && bot.player().getDistance(generator.getX(), generator.getY(), generator.getZ()) < 1 && callGoto
                && bot.pathFinder().isActive())
            bot.pathFinder().cancel();
        
        if (bot.player().inventory.getStackInSlot(1) != null &&
                bot.player().inventory.getStackInSlot(1).stackSize >= 10)
        {
            done = true;
        }
    }
}
