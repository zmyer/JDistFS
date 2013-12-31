/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name:LoadInfo.java
 *  
 *  Copyright 2013
 *  
 */

package cn.zmyer.dst.com;

import java.io.PrintStream;
import java.io.Serializable;
import java.text.DecimalFormat;

public class LoadInfo
    implements Serializable
{

    public LoadInfo(double perc[], double perc2, long disk, double diskPerc, 
            long bytes, long bytes2, int ins, int outs)
    {
        _cpuPerc = perc;
        _memPerc = perc2;
        _freeDisk = disk;
        _usedDiskPerc = diskPerc;
        _writeBytes = bytes;
        _readBytes = bytes2;
        _tcpIns = ins;
        _tcpOuts = outs;
    }

    public void print(PrintStream ps)
    {
        ps.println("CPUs Usage Percent:");
        ps.println();
        ps.println("CPU Index\tUsagePercent");
        DecimalFormat df = new DecimalFormat("00.00");
        double cpus[] = get_cpuPerc();
        for(int i = 0; i < cpus.length; i++)
            ps.println((new StringBuilder(String.valueOf(i + 1))).append("\t\t").append(df.format(cpus[i])).append("%").toString());

        ps.println();
        ps.println((new StringBuilder("Memory Usage Percent: ")).append(df.format(get_memPerc())).append("%").toString());
        ps.println();
        ps.println((new StringBuilder("Free Disk Space: ")).append(get_freeDisk()).append("MB").toString());
        ps.println((new StringBuilder("Disk Use Percent: ")).append(get_usedDiskPerc()).append("%").toString());
        ps.println();
        ps.println((new StringBuilder("Write Bytes: ")).append(get_writeBytes()).append("MB").toString());
        ps.println((new StringBuilder("Read Bytes: ")).append(get_readBytes()).append("MB").toString());
        ps.println();
        ps.println((new StringBuilder("TCP In Bound: ")).append(get_tcpIns()).toString());
        ps.println((new StringBuilder("TCP Out Bound: ")).append(get_tcpOuts()).toString());
    }

    public double[] get_cpuPerc()
    {
        return _cpuPerc;
    }

    public long get_freeDisk()
    {
        return _freeDisk;
    }

    public double get_memPerc()
    {
        return _memPerc;
    }

    public long get_readBytes()
    {
        return _readBytes;
    }

    public int get_tcpIns()
    {
        return _tcpIns;
    }

    public int get_tcpOuts()
    {
        return _tcpOuts;
    }

    public long get_writeBytes()
    {
        return _writeBytes;
    }

    public double get_usedDiskPerc()
    {
        return _usedDiskPerc;
    }

    private static final long serialVersionUID = 0x9fe64cfa30f755faL;
    double _cpuPerc[];
    double _memPerc;
    long _freeDisk;
    double _usedDiskPerc;
    long _writeBytes;
    long _readBytes;
    int _tcpIns;
    int _tcpOuts;
}
