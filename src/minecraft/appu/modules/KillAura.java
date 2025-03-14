package appu.modules;

import net.droidmine.MineBot;
import net.droidmine.utils.ElapTime;
import net.droidmine.utils.EntityRotations;
import net.minecraft.entity.EntityLivingBase;

public class KillAura
{
    public boolean disabled = false, flag = false;
    private ElapTime elapTime = new ElapTime();
    private float CPS = 0;
    private MineBot bot;
    
    public KillAura(MineBot bot)
    {
        this.bot = bot;
    }
    
    public boolean onTick()
    {
        if (disabled) return true;
        EntityLivingBase target = TargetUtil.getTarget(bot);
        
        if (target != null)
        {
            float[] rotations = EntityRotations.getRotationsTo(target, bot);
            if (bot.pathFinder().isActive()) bot.pathFinder().cancel();
            bot.player().rotationYaw = rotations[0];
            bot.player().rotationPitch = rotations[1];
            bot.options().keyBindSprint.setPressed(true);
            bot.options().keyBindForward.setPressed(target.getDistanceToEntity(bot.player()) >= 2);
            bot.options().keyBindJump.setPressed(target.getDistanceToEntity(bot.player()) >= 2);
            
            if (elapTime.passed((long) (1000 / CPS)))
            {
                if (bot.player().getDistanceToEntity(target) <= 3)
                {
                    bot.player().swingItem();
                    bot.player().controller().attackEntity(bot.player(), target);
                }
                
                CPS = updateCPS();
            }
            
            flag = true;
            return false;
        }
        
        else if (flag)
        {
            bot.options().keyBindForward.setPressed(false);
            bot.options().keyBindJump.setPressed(false);
            flag = false;
        }
        
        return true;
    }
    
    private float updateCPS()
    {
        return (float) (7 + (11 - 7) * Math.random());
    }
}
