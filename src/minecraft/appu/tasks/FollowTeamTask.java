package appu.tasks;

import net.droidmine.MineBot;
import net.minecraft.entity.Entity;

public class FollowTeamTask extends GameTask
{
    public void onTick(MineBot bot)
    {
        Entity teammate = bot.findEntity("Appu26J");
        
        if (teammate != null && !bot.pathFinder().isActive())
        {
            bot.pathFinder().goTo(teammate.getPosition());
        }
    }
}
