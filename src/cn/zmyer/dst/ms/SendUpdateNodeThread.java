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

import cn.zmyer.dst.com.PersistConn;
import cn.zmyer.dst.com.Protocol;
import cn.zmyer.dst.com.Serialization;
import cn.zmyer.dst.utlis.Log;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class SendUpdateNodeThread extends Thread
{

    public SendUpdateNodeThread(String ip, int updateType, int nodeIndex)
    {
        this.ip = ip;
        this.updateType = updateType;
        this.nodeIndex = nodeIndex;
    }

    public String getIp()
    {
        return ip;
    }

    public int getNodeIndex()
    {
        return nodeIndex;
    }

    public int getUpdateType()
    {
        return updateType;
    }

    public void run()
    {
        if(getUpdateType() == 0 || getUpdateType() == 1)
        {
            PersistConn pc = new PersistConn(ip, Short.parseShort(Integer.toString(10003)));
            Socket sock = pc.getSocket();
            if(sock == null)
            {
                Log.logger.error((new StringBuilder("发送SN更新信息时，连接FSI(")).append(ip).append(")失败，socket == null").toString());
                pc.close();
                return;
            }
            DataOutputStream os = pc.getOutputStream();
            try
            {
                String ListSN = "";
                for(int i = 0; i < MasterSN.listSN.size(); i++)
                {
                    ListSN = (new StringBuilder(String.valueOf(ListSN))).append(String.valueOf(((NodeDesc)MasterSN.listSN.get(i)).get_type())).append("/").toString();
                    ListSN = (new StringBuilder(String.valueOf(ListSN))).append(String.valueOf(((NodeDesc)MasterSN.listSN.get(i)).get_id())).append("/").toString();
                    ListSN = (new StringBuilder(String.valueOf(ListSN))).append(String.valueOf(((NodeDesc)MasterSN.listSN.get(i)).get_ip())).append("/").toString();
                    ListSN = (new StringBuilder(String.valueOf(ListSN))).append(String.valueOf(((NodeDesc)MasterSN.listSN.get(i)).get_port())).append("/").toString();
                    ListSN = (new StringBuilder(String.valueOf(ListSN))).append(String.valueOf(((NodeDesc)MasterSN.listSN.get(i)).get_start())).append("/").toString();
                    ListSN = (new StringBuilder(String.valueOf(ListSN))).append(String.valueOf(((NodeDesc)MasterSN.listSN.get(i)).get_isActive())).append("#").toString();
                }

                ListSN = (new StringBuilder(String.valueOf(ListSN))).append("\n").toString();
                String sendBuf = Serialization.serialize(Protocol.PROT_MAGIC,Protocol.PROT_MS_UPDATE,getUpdateType(),getNodeIndex(),ListSN);
                os.writeUTF(sendBuf);
                os.flush();
                Log.logger.info((new StringBuilder("发送SN更新信息至FSI(")).append(ip).append(")成功").toString());
            }
            catch(IOException e)
            {
                Log.logger.error((new StringBuilder("发送SN更新信息至FSI(")).append(ip).append(")失败，原因：").append(e.toString()).toString());
            }
            pc.close();
        } 
    }

    private String ip;
    private int updateType;
    private int nodeIndex;
}
