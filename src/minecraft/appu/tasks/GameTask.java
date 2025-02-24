package appu.tasks;

import net.droidmine.MineBot;

public abstract class GameTask
{
    public boolean done = false;
    public abstract void onTick(MineBot bot);
}
