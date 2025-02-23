package org.apache.logging.log4j;

import java.util.HashMap;

public class LogManager 
{
    private static HashMap<String, Logger> loggers = new HashMap<>();
    
    public static Logger getLogger()
    {
        String name = Thread.currentThread().getName();
        
        if (loggers.containsKey(name))
        {
            return loggers.get(name);
        }
        
        Logger logger = new Logger(name);
        loggers.put(name, logger);
        return logger;
    }
}
