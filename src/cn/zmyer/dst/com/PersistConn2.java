/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name:PersistConn2.java
 *  
 *  Copyright 2013
 *  
 */

package cn.zmyer.dst.com;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PersistConn2
{
    public class Conn extends Thread
    {

        public void run()
        {
            while(isConn) 
            {
                if(sock == null)
                {
                    is = null;
                    os = null;
                    try
                    {
                        sock = new Socket(_ip, _port);
                        if(sock != null)
                        {
                            sock.setKeepAlive(true);
                            sock.setTcpNoDelay(true);
                            os = new ObjectOutputStream(sock.getOutputStream());
                            is = new ObjectInputStream(sock.getInputStream());
                            System.out.println((new StringBuilder("新连接OK: ")).append(sock.getInetAddress()).append(sock.getPort()).toString());
                        }
                    }
                    catch(Exception e)
                    {
                        sock = null;
                        is = null;
                        os = null;
                        System.out.println((new StringBuilder("Connect to ")).append(_ip).append(":").append(_port).append(" Failed").toString());
                        System.out.println("ReConnect Start...");
                    }
                }
                try
                {
                    sleep(1000L);
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            if(sock != null)
                try
                {
                    sock.close();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
        }

        public Conn()
        {
            super();
        }
    }


    public PersistConn2(String _ip, short _port)
    {
        sock = null;
        os = null;
        is = null;
        con = null;
        lock = (new ReentrantReadWriteLock()).writeLock();
        isConn = true;
        count = 0;
        this._ip = _ip;
        this._port = _port;
        System.out.println((new StringBuilder("_port=")).append(_port).toString());
        try
        {
            sock = new Socket(_ip, _port);
            if(sock != null)
            {
                sock.setTcpNoDelay(true);
                sock.setKeepAlive(true);
                os = new ObjectOutputStream(sock.getOutputStream());
                is = new ObjectInputStream(sock.getInputStream());
                System.out.println((new StringBuilder("连接OK: ")).append(sock.getInetAddress().getHostAddress()).append(":").append(sock.getPort()).toString());
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.out.println((new StringBuilder("连接Failed: ")).append(_ip).append(":").append(_port).toString());
            sock = null;
            is = null;
            os = null;
        }
        con = new Conn();
        con.start();
    }

    public ObjectInputStream getInputStream()
    {
        return is;
    }

    public ObjectOutputStream getOutputStream()
    {
        return os;
    }

    public Socket getSocket()
    {
        return sock;
    }

    public void reConn()
    {
    	try{
    		sock = null;
    		os = null;
    		is = null;
    		Thread.sleep(1000L);
    	}
    	catch(InterruptedException e)
    	{
    		
    	}
    }

    public void close()
    {
        isConn = false;
    }

    private String _ip;
    private short _port;
    Socket sock;
    ObjectOutputStream os;
    ObjectInputStream is;
    Conn con;
    Lock lock;
    boolean isConn;
    public int count;


}
