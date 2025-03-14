package org.apache.logging.log4j;

import java.util.HashMap;

public class LogManager 
{
    private static HashMap<String, Logger> loggers = new HashMap<>();
    
    public static Logger getLogger()
    {
        return getLogger(Thread.currentThread().getName());
    }
    
    public static Logger getLogger(String name)
    {
        if (loggers.containsKey(name))
        {
            return loggers.get(name);
        }
        
        Logger logger = new Logger(name);
        loggers.put(name, logger);
        return logger;
    }
}
