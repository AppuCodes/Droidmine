package net.droidmine.utils;

public class ElapTime
{
    private long time = System.currentTimeMillis();
    
    public boolean passed(long ms)
    {
        boolean pass = (time + ms) <= System.currentTimeMillis();
        if (pass) reset();
        return pass;
    }
    
    private void reset() { time = System.currentTimeMillis(); }
}
