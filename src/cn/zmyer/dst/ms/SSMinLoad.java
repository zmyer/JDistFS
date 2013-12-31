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

import cn.zmyer.dst.com.LoadInfo;
import java.util.Map;
       
class SSMinLoad implements SnSelect
{

    SSMinLoad()
    {
    }

    public int[] getSnIndex()
    {
        double loadValues[] = getLoadValue(MasterSN.mapSnLI);
        if(loadValues == null)
        {
            System.out.println("loadValues is null");
            return null;
        }
        int maxIndex[] = {
            0, 1
        };
        int minIndex = -1;
        boolean flag = false;
        if(loadValues.length < 1)
            return null;
        if(loadValues.length == 1)
        {
            maxIndex[0] = 0;
            maxIndex[1] = 0;
            return maxIndex;
        }
        for(int i = 2; i < loadValues.length; i++)
        {
            if(loadValues[maxIndex[0]] > loadValues[maxIndex[1]])
            {
                minIndex = maxIndex[1];
                flag = true;
            } else
            {
                minIndex = maxIndex[0];
            }
            if(loadValues[i] > loadValues[minIndex])
            {
                if(flag)
                    maxIndex[1] = i;
                else
                    maxIndex[0] = i;
                flag = false;
            }
        }

        if(loadValues[maxIndex[0]] < 0.0D && loadValues[maxIndex[1]] < 0.0D)
            return null;
        if(loadValues[maxIndex[0]] < 0.0D)
            maxIndex[0] = maxIndex[1];
        else
        if(loadValues[maxIndex[1]] < 0.0D)
            maxIndex[1] = maxIndex[0];
        return maxIndex;
    }

    @SuppressWarnings("unchecked")
	private double[] getLoadValue(Map mapSNLI)
    {
        if(mapSNLI.size() == 0)
            return null;
        int size = MasterSN.listSN.size();
        double db[] = new double[size];
        for(int i = 0; i < size; i++)
        {
            NodeDesc nd = (NodeDesc)MasterSN.listSN.get(i);
            if(!nd.get_isActive())
            {
                db[i] = -1D;
            } else
            {
                LoadInfo li = (LoadInfo)mapSNLI.get(Integer.valueOf(i));
                if(li == null)
                {
                    db[i] = -1D;
                } else
                {
                    long writeBytes = li.get_writeBytes();
                    long readByte = li.get_readBytes();
                    long freeDisk = li.get_freeDisk();
                    db[i] = (double)freeDisk / ((double)writeBytes + (double)readByte * 0.5D + 1.0D);
                }
            }
        }

        return db;
    }
}
