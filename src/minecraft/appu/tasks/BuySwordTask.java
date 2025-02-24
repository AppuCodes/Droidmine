package appu.tasks;

import net.droidmine.MineBot;
import net.droidmine.utils.EntityRotations;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public class BuySwordTask extends GameTask
{
    private int ticks = 0, delay = 0, id = 0;
    
    public void onTick(MineBot bot)
    {
        ticks++;
        Entity shop = bot.findEntity("ITEM SHOP");
        if (shop == null) return;
        
        if (bot.player().getDistanceToEntity(shop) > 3)
        {
            if (ticks == 1)
            {
                bot.player().rotationYaw -= 180;
            }
            
            bot.options().keyBindForward.setPressed(true);
            
            if (ticks > 20)
            {
                float[] rotations = EntityRotations.getRotationsTo((EntityLivingBase) shop, bot);
                bot.player().rotationYaw = rotations[0];
                bot.player().rotationPitch = rotations[1];
            }
        }
        
        else
        {
            if (delay == 0)
            {
                float[] rotations = EntityRotations.getRotationsTo((EntityLivingBase) shop, bot);
                bot.player().rotationYaw = rotations[0];
                bot.player().rotationPitch = rotations[1];
                bot.options().keyBindUseItem.onTick(bot.options().keyBindUseItem.getKeyCode());
            }
            
            delay++;
            
            if (delay > 10 && id == 0)
            {
                bot.player().controller().windowClick(bot.player().openContainer.windowId, 20, 0, 0, bot.player());
                id = 1;
            }
            
            if (delay > 15)
            {
                bot.player().closeScreen();
                done = true;
            }
        }
    }
}
