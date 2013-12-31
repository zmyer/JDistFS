/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name: Statistic.java
 *  
 *  Copyright 2013
 *  
 */

package cn.zmyer.dst.utlis;

import java.io.PrintStream;
import java.sql.Timestamp;

public class Statistic
{

    public Statistic(String name)
    {
        this.name = null;
        d = new Duration();
        runTimes = 0;
        memSize = 0;
        this.name = (new StringBuilder(String.valueOf(name))).append(" 测试信息统计").toString();
        out = System.out;
    }

    public Statistic()
    {
        name = null;
        d = new Duration();
        runTimes = 0;
        memSize = 0;
        name = "... 测试信息统计";
        out = System.out;
    }

    public void setOut(PrintStream out)
    {
        this.out = out;
    }

    public void start()
    {
        d.start();
        out.println("---------------------------------");
        out.println((new StringBuilder(String.valueOf(name))).append("开始:").toString());
        out.println((new StringBuilder(" 开始时间")).append((new Timestamp(d.getStart())).toString()).toString());
        out.println();
    }

    public void runTimesIncrease()
    {
        synchronized(this)
        {
            runTimes++;
        }
    }

    public void reStart()
    {
        stop();
        start();
    }

    public void memSizeIncrease(int memSize)
    {
        this.memSize += memSize;
    }

    public void stop()
    {
        d.stop();
        printStat();
        out.println((new StringBuilder(String.valueOf(name))).append("结束:").toString());
        out.println((new StringBuilder("结束时间")).append((new Timestamp(d.getEnd())).toString()).toString());
        out.println();
        out.println("---------------------------------");
    }

    public void clear()
    {
        runTimes = 0;
        memSize = 0;
    }

    public void print(String info)
    {
        out.println(info);
    }

    public void println(String info)
    {
        out.println(info);
    }

    public void printStat()
    {
        out.println("打印统计信息:");
        out.println((new StringBuilder("Run Times is ")).append(runTimes).toString());
        out.println((new StringBuilder("Memory takes ")).append(memSize).toString());
        double temp = d.getDuration();
        out.println((new StringBuilder("Time takes ")).append(temp / 1000D).append("s").toString());
        temp = runTimes;
        if(d.getDuration() == 0L)
            temp = -1D;
        else
            temp = (temp / (double)d.getDuration()) * 1000D;
        out.println((new StringBuilder("Run times for every second (ps) ")).append(temp).toString());
        out.println();
        clear();
    }

    public void setName(String name)
    {
        this.name = name;
    }

    String name;
    Duration d;
    PrintStream out;
    int runTimes;
    int memSize;
}
