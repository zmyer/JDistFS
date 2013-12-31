/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name:FSIWorkerThread.java
 *  
 *  Copyright 2013
 *  
 */   

package cn.zmyer.dst.ms;

class SSFactory
{

    SSFactory()
    {
    }

    public SnSelect getSS(int type)
    {
        if(type == 1)
            return new SSRoundRobin();
        if(type == 2)
            return new SSMinLoad();
        else
            return null;
    }

    static final int ROUND_ROBIN = 1;
    static final int MIN_LOAD = 2;
}
