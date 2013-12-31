/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name: SNWorkerThread.java
 *  
 *  Copyright 2013
 *  
 */ 

package cn.zmyer.dst.ms;

import cn.zmyer.dst.com.Protocol;
import cn.zmyer.dst.com.Serialization;
import cn.zmyer.dst.utlis.Log;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class SNWorkerThread extends Thread
{

    public SNWorkerThread(Socket _socket)
    {
        os = null;
        is = null;
        isCon = true;
        currentID = -1;
        nodeType = -1;
        sn_index = -1;
        this._socket = _socket;
        try
        {
            os = new DataOutputStream(_socket.getOutputStream());
            is = new DataInputStream(_socket.getInputStream());
        }
        catch(IOException e)
        {
            Log.logger.error((new StringBuilder("SN(")).append(this._socket.getInetAddress().getHostAddress()).append(":").append(this._socket.getPort()).append(")连接错误：").append(e.toString()).toString());
        }
    }

    public void run()
    {
        try {
			if(!init_sn())
			    return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        while(isCon) 
            do_one_heartbeat();
    }

    private void do_one_heartbeat()
    {
        try
        {
        	String recvBuf = is.readUTF();
        	if(recvBuf==null)
        		return;
        	
        	String[] splitBuf = Serialization.unserialize(recvBuf);
        	if(Integer.parseInt(splitBuf[0])!= Protocol.PROT_MAGIC
        			|| Integer.parseInt(splitBuf[1]) != Protocol.PROT_SN_HB)
                throw new AssertionError();
       
        	String sendBuf = Serialization.serialize(Protocol.PROT_MAGIC,Protocol.PROT_MS_HB);
        	os.writeUTF(sendBuf);
            os.flush();
        }
        catch(IOException e1)
        {
            isCon = false;
            NodeDesc snd = (NodeDesc)MasterSN.listSN.get(sn_index);
            snd.set_isActive(false);
            (new UpdateConnThread(1, sn_index)).start();
            MasterSN.mapSnLI.remove(Integer.valueOf(sn_index));
            MasterSN.sn_conf_count_fs--;
            Log.logger.error((new StringBuilder("SN("))
            		.append(currentID)
            		.append(")(")
            		.append(_socket.getInetAddress().getHostAddress())
            		.append(":").append(_socket.getPort())
            		.append(")翘了").toString());
            try
            {
                _socket.close();
            }
            catch(IOException e)
            {
                Log.logger.error((new StringBuilder("MasterSN:SNWorkerThread:do_one_heartbeat:socket关闭出错：")).append(e.toString()).toString());
            }
        }
    }

    @SuppressWarnings("unchecked")
	private boolean init_sn() throws IOException
    {
        short port;
        String ip;
        NodeDesc snd;
        
        String recvBuf = is.readUTF();
        if(recvBuf == null)
        	return false;
        
        String[] splitBuf = Serialization.unserialize(recvBuf);
        if(Integer.parseInt(splitBuf[0]) != Protocol.PROT_MAGIC
        		|| Integer.parseInt(splitBuf[1])!= Protocol.PROT_SN_HB)
            throw new AssertionError();
        
        nodeType = 1;
        port = (short) Integer.parseInt(splitBuf[2]);
        ip = _socket.getInetAddress().getHostAddress();
        snd = MasterSN.getSN(ip, Short.valueOf(port));
        if(snd == null)
        {
            currentID = MasterSN.getSNId(ip, port);
            snd = new NodeDesc(nodeType, currentID, ip, port);
            MasterSN.listSN.add(snd);
        } 
        else
        {
            currentID = snd.get_id();
        }       
        
        String sendBuf = "";
        try
        {
            Calendar rightNow = Calendar.getInstance();
            short days = (new Long(rightNow.getTimeInMillis() / 0x5265c00L)).shortValue();
            snd.set_start(StartIdMgr.getStartId(currentID, days));
            snd.set_isActive(true); 

            sendBuf = Serialization.serialize(Protocol.PROT_MAGIC,Protocol.PROT_MS_HB,currentID);
            os.writeUTF(sendBuf);
            os.flush();
            sn_index = MasterSN.listSN.indexOf(snd);
            (new UpdateConnThread(0, sn_index)).start();
            MasterSN.sn_conf_count_fs++;
            Log.logger.info((new StringBuilder(" 配置第")).append(MasterSN.sn_conf_count_fs).append("个SN，ID:").append(currentID).append("，位于:").append(ip).append(":").append(port).toString());
            Log.logger.info((new StringBuilder("已配置fs sn数量:")).append(MasterSN.sn_conf_count_fs).toString());
            Log.logger.info((new StringBuilder("建立心跳成功，来自：SN（")).append(currentID).append("）（").append(ip).append(":").append(_socket.getPort()).append(")").toString());
        }
        catch(IOException e1)
        {
            Log.logger.error((new StringBuilder("MasterSN:SNWorkerThread:init_sn出错：")).append(e1.toString()).toString());
            try
            {
                _socket.close();
            }
            catch(IOException e)
            {
                Log.logger.error((new StringBuilder("MasterSN:SNWorkerThread:init_sn:socket关闭出错：")).append(e.toString()).toString());
            }
            return false;
        }
        return true;
    }

    Socket _socket;
    DataOutputStream os;
    DataInputStream is;
    boolean isCon;
    byte currentID;
    short nodeType;
    int sn_index;
}
