/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name: Log.java
 *  
 *  Copyright 2013
 *  
 */

package cn.zmyer.dst.utlis;

import java.io.IOException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Log
{

    public Log()
    {
    }

    public static void init(String confFile, String logFile) throws IOException
    {
        PropertyConfigurator.configure(confFile);
        FileAppender app = (FileAppender)Logger.getRootLogger().getAppender("file");
        app.setFile(logFile);
        app.activateOptions();
    }
    public static final Logger logger = Logger.getLogger(Log.class.getName());
}
