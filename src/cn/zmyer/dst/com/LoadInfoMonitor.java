/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name: LoadInfoMonitor.java
 *  
 *  Copyright 2013
 *  
 */

package cn.zmyer.dst.com;
import cn.zmyer.dst.utlis.Log;
import org.apache.log4j.Logger;
import org.hyperic.sigar.*;

public class LoadInfoMonitor
{

    public LoadInfoMonitor()
    {
    }

    public static LoadInfo getLoadInfo(String path)
    {
        LoadInfo li = null;
        try
        {
            Mem mem = si.getMem();
            double memUse = ((double)mem.getUsed() * 100D) / (double)mem.getTotal();
            Cpu cpus[] = si.getCpuList();
            double cpuUse[] = new double[cpus.length];
            for(int i = 0; i < cpus.length; i++)
                cpuUse[i] = ((double)(cpus[i].getSys() + cpus[i].getUser()) * 100D) / (double)cpus[i].getTotal();

            FileSystemUsage fsu = si.getFileSystemUsage(path);
            long freeDisk = fsu.getFree() / 1024L;
            double usePerc = fsu.getUsePercent() * 100D;
            long readBytes = fsu.getDiskReadBytes() / 0x100000L;
            long writeBytes = fsu.getDiskWriteBytes() / 0x100000L;
            NetStat ns = si.getNetStat();
            int tcpins = ns.getTcpInboundTotal();
            int tcpouts = ns.getTcpOutboundTotal();
            li = new LoadInfo(cpuUse, memUse, freeDisk, usePerc, writeBytes, readBytes, tcpins, tcpouts);
        }
        catch(SigarException e)
        {
            Log.logger.error((new StringBuilder("收集负载信息出错，原因：")).append(e.toString()).toString());
            li = null;
            return null;
        }
        return li;
    }

    public static void main(String args[])
    {
        LoadInfo li = getLoadInfo(".");
        li.print(System.out);
    }

    private static Sigar si = new Sigar();

}
