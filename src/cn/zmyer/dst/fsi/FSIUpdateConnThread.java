/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name: FSIUpdateConnThread.java
 *  
 *  Copyright 2013
 *  
 */

package cn.zmyer.dst.fsi;

import cn.zmyer.dst.com.Protocol;
import cn.zmyer.dst.com.Serialization;
import cn.zmyer.dst.ms.NodeDesc;
import cn.zmyer.dst.utlis.Log;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FSIUpdateConnThread extends Thread
{

    public FSIUpdateConnThread(Socket _socket)
    {
        os = null;
        is = null;
        this._socket = _socket;
        try
        {
            os = new DataOutputStream(_socket.getOutputStream());
            is = new DataInputStream(_socket.getInputStream());
        }
        catch(IOException e)
        {
            Log.logger.error((new StringBuilder("MasterSN连接错误：")).append(e.toString()).toString());
        }
    }

    public void run()
    {
        try
        {
            do_one_update();
        }
        catch(IOException e)
        {
            Log.logger.error((new StringBuilder("MasterSN（")).append(_socket.getInetAddress().getHostAddress()).append(":").append(_socket.getPort()).append("）连接中断: ").append(e.toString()).toString());
            return;
        }
    }

    @SuppressWarnings("unchecked")
	private void do_one_update()throws IOException
    {
    	List listSN = new ArrayList();
        NodeDesc desc = null;
        
        String recvBuf = is.readUTF();
        if(recvBuf == null)
        	throw new IOException();
        
        String[] splitBuf = Serialization.unserialize(recvBuf);
        if(Integer.parseInt(splitBuf[0]) != Protocol.PROT_MAGIC 
        		|| Integer.parseInt(splitBuf[1]) != Protocol.PROT_MS_UPDATE)
            throw new AssertionError();
        
        int updateType = Integer.parseInt(splitBuf[2]);
        int nodeIndex = Integer.parseInt(splitBuf[3]);
        
        String List = splitBuf[4];
        String SNList[] = List.split("#");
        for(int i = 0; i < SNList.length; i++)
        {
            String subSplit[] = SNList[i].split("/");
            if(subSplit.length >= 6)
            {
                desc = new NodeDesc(Short.valueOf(subSplit[0]).shortValue(), Byte.valueOf(subSplit[1]).byteValue(), subSplit[2], Short.valueOf(subSplit[3]).shortValue());
                if(Boolean.valueOf(subSplit[5]).booleanValue())
                    desc.set_isActive(true);
                listSN.add(desc);
            }
        }

        FS_Function fs_fsi = FS_Function.getInstance();
        fs_fsi.updateConn(updateType, nodeIndex, listSN);
    }

    Socket _socket;
    DataOutputStream os;
    DataInputStream is;
}
