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

class SSRoundRobin
    implements SnSelect
{

    SSRoundRobin()
    {
    }

    public int[] getSnIndex()
    {
        int indexs[] = new int[2];
        indexs[0] = -1;
        indexs[1] = -1;
        int flag = 0;
        int tmp = 0;
        if(MasterSN.listSN.size() == 0)
        {
            System.out.println("MasterSN is null");
            return indexs;
        }
        int index = MasterSN.currSNId;
        int old_id = index;
        MasterSN.currSNId++;
        if(MasterSN.currSNId >= MasterSN.listSN.size())
            MasterSN.currSNId = 0;
        NodeDesc snd = (NodeDesc)MasterSN.listSN.get(index);
        if(!snd.get_isActive())
        {
            index = MasterSN.currSNId;
            do
            {
                snd = (NodeDesc)MasterSN.listSN.get(index);
                if(snd.get_isActive())
                {
                    while(tmp < flag) 
                        if(indexs[tmp++] == MasterSN.currSNId)
                            break;
                    if(tmp == flag)
                        indexs[flag++] = MasterSN.currSNId;
                    if(flag == 2)
                        break;
                }
                MasterSN.currSNId++;
                if(MasterSN.currSNId >= MasterSN.listSN.size())
                    MasterSN.currSNId = 0;
                index = MasterSN.currSNId;
            } while(index != old_id);
        } else
        {
            indexs[flag++] = old_id;
            do
            {
                index = MasterSN.currSNId;
                if(index == old_id)
                    break;
                snd = (NodeDesc)MasterSN.listSN.get(index);
                if(snd.get_isActive())
                {
                    while(tmp < flag) 
                        if(indexs[tmp++] == MasterSN.currSNId)
                            break;
                    if(tmp == flag)
                    {
                        indexs[flag] = MasterSN.currSNId;
                        break;
                    }
                }
                MasterSN.currSNId++;
                if(MasterSN.currSNId >= MasterSN.listSN.size())
                    MasterSN.currSNId = 0;
            } while(true);
        }
        return indexs;
    }
}
