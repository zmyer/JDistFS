/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name:FSIThread.java
 *  
 *  Copyright 2013
 *  
 */

package cn.zmyer.dst.fsi;

import cn.zmyer.dst.utlis.Log;
import java.io.IOException;
import java.net.*;

public class FSIThread extends Thread
{

    public FSIThread(ServerSocket _socket)
    {
        this._socket = _socket;
    }

    public void run()
    {
        do
            try
            {
                Socket client = _socket.accept();
                client.setKeepAlive(true);
                client.setTcpNoDelay(true);
                Log.logger.info((new StringBuilder("接收到MaterSN连接：")).append(client.getInetAddress().getHostAddress()).append(":").append(client.getPort()).toString());
                (new FSIUpdateConnThread(client)).start();
            }
            catch(IOException e)
            {
                Log.logger.info((new StringBuilder("接收到MasterSN连接出错：")).append(e.toString()).toString());
            }
        while(true);
    }

    ServerSocket _socket;
}
