/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name: UpdateConnThread.java
 *  
 *  Copyright 2013
 *  
 */

package cn.zmyer.dst.ms;

public class UpdateConnThread extends Thread
{

    public UpdateConnThread(int updateType, int nodeIndex)
    {
        this.updateType = updateType;
        this.nodeIndex = nodeIndex;
    }

    public void run()
    {
        if(getUpdateType() == 0 || getUpdateType() == 1)
        {
            if(MasterSN.mapFSI.size() == 0)
                return;
            for(int i = 0; i < 255; i++)
            {
                String ipFSI = (String)MasterSN.mapFSI.get(Integer.valueOf(i));
                if(ipFSI != null)
                    (new SendUpdateNodeThread(ipFSI, getUpdateType(), getNodeIndex())).start();
            }

        } else
        {
            return;
        }
    }

    public int getNodeIndex()
    {
        return nodeIndex;
    }

    public int getUpdateType()
    {
        return updateType;
    }

    private int updateType;
    private int nodeIndex;
}
