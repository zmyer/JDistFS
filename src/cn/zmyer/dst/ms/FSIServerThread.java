/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name:FSIServerThread.java

 *  
 *  Copyright 2013
 *  
 */
  
package cn.zmyer.dst.ms;

import cn.zmyer.dst.utlis.Log;
import java.io.IOException;
import java.net.*;

public class FSIServerThread extends Thread
{

    public FSIServerThread(ServerSocket _socket)
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
                Log.logger.info((new StringBuilder("接收到FSI连接：")).append(client.getInetAddress().getHostAddress()).append(":").append(client.getPort()).toString());
                (new FSIWorkerThread(client)).start();
            }
            catch(IOException e)
            {
                Log.logger.info((new StringBuilder("接收到FSI连接出错：")).append(e.toString()).toString());
            }
        while(true);
    }

    ServerSocket _socket;
}
