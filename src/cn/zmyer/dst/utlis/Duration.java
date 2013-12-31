/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name: Duration.java
 *  
 *  Copyright 2013
 *  
 */ 

package cn.zmyer.dst.utlis;


public class Duration
{

    public Duration()
    {
        start = System.currentTimeMillis();
        end = start;
    }

    public void start()
    {
        start = System.currentTimeMillis();
    }

    public void stop()
    {
        end = System.currentTimeMillis();
    }

    public long getDuration()
    {
        return end - start;
    }

    public long getEnd()
    {
        return end;
    }

    public long getStart()
    {
        return start;
    }

    private long start;
    private long end;
}
