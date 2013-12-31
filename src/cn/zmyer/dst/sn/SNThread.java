/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name:SNThread.java
 *  
 *  Copyright 2013
 *  
 */

package cn.zmyer.dst.sn;

import cn.zmyer.dst.utlis.Log;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SNThread extends Thread
{

    public SNThread(SN sn, int port)
        throws IOException
    {
        ss = null;
        this.sn = null;
        this.sn = sn;
        ss = new ServerSocket(port);
    }

    public void run()
    {
        do
            try
            {
                Socket socket = ss.accept();
                socket.setKeepAlive(true);
                socket.setTcpNoDelay(true);
                Log.logger.info((new StringBuilder("接收到FSI连接：")).append(socket.getInetAddress()).append(":").append(socket.getPort()).toString());
                (new FSSNThread(sn, socket)).start();
            }
            catch(IOException e)
            {
                Log.logger.info((new StringBuilder("接收到FSI连接出错：")).append(e.toString()).toString());
            }
        while(true);
    }

    ServerSocket ss;
    SN sn;
}
