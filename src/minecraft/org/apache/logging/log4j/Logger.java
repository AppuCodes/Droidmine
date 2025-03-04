package org.apache.logging.log4j;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Logger
{
    private String name;
    
    public Logger(String name)
    {
        this.name = name;
    }
    
    public void info(String message)
    {
        System.out.println(getTime() + "[" + name + "] " + message);
    }
    
    public void warn(String message, Object[] objects)
    {
        System.out.printf(getTime() + "[" + name + "] " + message, objects);
    }
    
    public void error(Object object)
    {
        System.err.println(getTime() + "[" + name + "] " + object);
    }
    
    public void error(String message, Object object)
    {
        System.err.println(getTime() + "[" + name + "] " + message + " " + object);
    }
    
    public void warn(String message) { info(message); }
    public void fatal(Object object) { error(object); }
    
    private String getTime()
    {
        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return "[" + now.format(formatter) + "] ";
    }
}
