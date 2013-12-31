/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name:SN.java
 *  
 *  Copyright 2013
 *  
 */

package cn.zmyer.dst.sn;

import cn.zmyer.dst.com.FSSendList;
import cn.zmyer.dst.com.FileId;
import cn.zmyer.dst.com.PersistConn;
import cn.zmyer.dst.com.Protocol;
import cn.zmyer.dst.com.Serialization;
import cn.zmyer.dst.utlis.Log;
import java.io.*;
import java.net.Socket;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;

public class SN
{
    public class InsertFile extends Thread
    {

        public void setIndex(int i)
        {
            index = i;
            start();
        }

        public void run()
        {
            do
            {
                SN.insertNoCompressCount = 0;
                SN.insertCompressCount = 0;
                SN.insertNoNeedZipSize = 0.0D;
                SN.insertNeedZipSize = 0.0D;
                GetTotalSize(getPath());
                try
                {
                    Thread.sleep(60000L);
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }
            } while(true);
        }

        int index;

        public InsertFile()
        {
            super();
        }
    }

    public class SendHeartBeat extends TimerTask
    {

        public void run()
        {
            try
            {
            	String sendBuf = Serialization.serialize(Protocol.PROT_MAGIC,Protocol.PROT_SN_HB);
            	os.writeUTF(sendBuf);
            	os.flush();
            	
            	String recvBuf = is.readUTF();
            	String[] splitBuf = Serialization.unserialize(recvBuf);
            	if(Integer.parseInt(splitBuf[0]) != Protocol.PROT_MAGIC 
            			|| Integer.parseInt(splitBuf[1])!=Protocol.PROT_MS_HB)
            	      throw new AssertionError();
                Log.logger.info("发送心跳成功！");
            }
            catch(IOException e)
            {
                Log.logger.error((new StringBuilder("SendHeartBeat出错！，原因：")).append(e.toString()).toString());
                timer.cancel();
                if(pc != null)
                    pc.close();
                System.exit(1);
            }
        }

        private DataOutputStream os;
        private DataInputStream is; 

        public SendHeartBeat(PersistConn pc)
        {
            super();
            os = null;
            is = null;
            if(pc == null)
            {
                timer.cancel();
                return;
            } 
            else
            {
                os = new DataOutputStream(pc.getOutputStream());
                is = new DataInputStream(pc.getInputStream());
                return;
            }
        }
    }


    public SN(String path, int port) throws Exception
    {
        ID = -1;
        this.path = null;
        this.port = 0;
        ipMS = null;
        timer = null;
        pc = null;
        this.path = path;
        this.port = port;
        File file = new File(path);
        if(!file.exists())
            file.mkdirs();
        initSN();
        fileList = new FSSendList[THREAD_NUMBER];
        for(int i = 0; i < THREAD_NUMBER; i++)
            fileList[i] = new FSSendList();

        insert = new InsertFile[THREAD_NUMBER];
        for(int i = 0; i < THREAD_NUMBER; i++)
        {
            insert[i] = new InsertFile();
            insert[i].setIndex(i);
        }

        connMS();
    }

    private void initSN() throws IOException
    {
        BufferedReader br;
        String confLine;
        br = new BufferedReader(new FileReader(new File("conf/sn.conf")));
        confLine = br.readLine();
        if(confLine == null)
        {
            br.close();
            return;
        }
        try
        {
            ipMS = confLine;
            confLine = br.readLine();
            if(confLine != null)
                THREAD_NUMBER = (new Integer(confLine)).intValue();
            confLine = br.readLine();
            if(confLine != null)
                MAX_LIST_NUMBER = (new Integer(confLine)).intValue();
            br.close();
        }
        catch(Exception e)
        {
            Log.logger.error((new StringBuilder("SN 初始化失败：")).append(e.toString()).toString());
        }
        return;
    }

    private void connMS()throws Exception
    {
        pc = new PersistConn(ipMS, Short.parseShort(Integer.toString(10001)));
        Socket sock = pc.getSocket();
        if(sock == null)
        {
            System.out.println((new StringBuilder("连接MS:"))
            		.append(ipMS).append(":")
            		.append(10001).append("失败!").toString());
            pc.close();
            throw new Exception("MasterSN has not openned yet.");
        }
        DataOutputStream os = new DataOutputStream(pc.getOutputStream());
        DataInputStream is = new DataInputStream(pc.getInputStream());
       
        String sendBuf = Serialization.serialize(Protocol.PROT_MAGIC,Protocol.PROT_SN_HB,getPort());
        os.writeUTF(sendBuf);        
        os.flush();
        
       	String recvBuf = is.readUTF();
    	String[] splitBuf = Serialization.unserialize(recvBuf);
    	if(Integer.parseInt(splitBuf[0]) != Protocol.PROT_MAGIC)
            throw new AssertionError();
    	
    	int hb = Integer.parseInt(splitBuf[1]);
        if(hb == Protocol.PROT_MS_HB)
        {
        	ID = Integer.parseInt(splitBuf[2]);
            System.out.println((new StringBuilder("SN("))
            		.append(getID()).append(")心跳初始化连接成功").toString());
        } 
        else if(hb ==Protocol.PROT_MS_DU)
        {
            Log.logger.error(" 此SN已启动，退出");
            pc.close();
            System.exit (1);
        }
    }

    public String fileIdToPath(FileId fileId, boolean bMkdir)
    {
        int fid = fileId.getId();
        byte snid[] = fileId.getSnId();
        String fids = "";
        for(int i = 0; i < snid.length; i++)
            fids = (new StringBuilder(String.valueOf(fids))).append(String.valueOf(snid[i])).append("_").toString();

        String tmppath = (new StringBuilder(String.valueOf(path))).append("/").append(fids).toString();
        tmppath = (new StringBuilder(String.valueOf(tmppath))).append("/").append(String.format("%02X", new Object[] {
            Short.valueOf(fileId.getDay())
        })).toString();
        tmppath = (new StringBuilder(String.valueOf(tmppath))).append("/").append(String.format("%02X", new Object[] {
            Integer.valueOf((fid & 0xff000000) >> 24)
        })).toString();
        tmppath = (new StringBuilder(String.valueOf(tmppath))).append("/").append(String.format("%02X", new Object[] {
            Integer.valueOf((fid & 0xff0000) >> 16)
        })).toString();
        tmppath = (new StringBuilder(String.valueOf(tmppath))).append("/").append(String.format("%02X", new Object[] {
            Integer.valueOf((fid & 0xff00) >> 8)
        })).toString();
        if(bMkdir)
            (new File(tmppath)).mkdirs();
        tmppath = (new StringBuilder(String.valueOf(tmppath))).append("/").append(String.format("%02X", new Object[] {
            Integer.valueOf(fid & 0xff)
        })).toString();
        return tmppath;
    }

    private Long getId(String absoPath)
    {
        Long ID = Long.valueOf(0L);
        int fids = 0;
        long temp = 0L;
        if(absoPath == null)
            return Long.valueOf(-1L);
        String subSplit[] = absoPath.split("\\\\");
        if(subSplit.length < 6)
            return Long.valueOf(-1L);
        String id[] = subSplit[1].split("_");
        byte Id[] = new byte[id.length];
        for(int i = 0; i < id.length; i++)
            Id[i] = Byte.valueOf(id[i]).byteValue();

        int temps = 0;
        temps = Integer.parseInt(subSplit[3], 16) & 0xff;
        fids |= temps << 24;
        temps = Integer.parseInt(subSplit[4], 16) & 0xff;
        fids |= temps << 16;
        temps = Integer.parseInt(subSplit[5], 16) & 0xff;
        fids |= temps << 8;
        temps = Integer.parseInt(subSplit[6], 16) & 0xff;
        fids |= temps;
        temp = Integer.parseInt(subSplit[2], 16);
        long days = (new Long(temp & 65535L)).longValue();
        ID = Long.valueOf(days << 48);
        temp = fids;
        ID = Long.valueOf(ID.longValue() | temp << 16);
        temp = Id[1];
        ID = Long.valueOf(ID.longValue() | temp << 8);
        temp = Id[0];
        ID = Long.valueOf(ID.longValue() | temp);
        return ID;
    }

    private void GetTotalSize(String path)
    {
        File file = new File(path);
        if(!file.isDirectory())
        {
            String absolutePath = file.getPath();
            File fs = new File(absolutePath);
            if(!fs.exists())
                throw new AssertionError();
            long id = getId(absolutePath).longValue();
            FileId fid = new FileId(id);
            int Id = fid.getId();
            if(Id % 2 == 0)
            {
                insertCompressCount++;
                insertNeedZipSize += fs.length();
            } else
            {
                insertNoCompressCount++;
                insertNoNeedZipSize += fs.length();
            }
        }
        else
        {
            String fileList[] = file.list();
            for(int i = 0; i < fileList.length; i++)
                GetTotalSize((new StringBuilder(String.valueOf(path))).append("\\").append(fileList[i]).toString());

        }
    }

    public FileInputStream getFile(FileId fileId)
    {
        FileInputStream is;
        String filePath = fileIdToPath(fileId, false);
        File file = new File(filePath);
        try 
        {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			getFileError++;
			Log.logger.error((new StringBuilder("文件系统SN获取文件失败，文件ID:")).append(fileId).append("，原因: ").append(e.toString()).toString());
			return null;
		}
        return is;
    }

    private boolean serveFSI(int port)
    {
        try
        {
            (new SNThread(this, port)).start();
        }
        catch(IOException e)
        {
            Log.logger.error("文件系统SN服务线程启动失败!");
            return false;
        }
        return true;
    }

    private boolean heartBeat()
    {
        if(pc == null)
        {
            Log.logger.error("SN发送心跳的连接为null，不能发送心跳");
            return false;
        } else
        {
            timer = new Timer();
            timer.schedule(new SendHeartBeat(pc), 1000L, 10000L);
            return true;
        }
    }

    private static void loopStat()
    {
        double rate = 0.0D;
        DecimalFormat sizeFormat = new DecimalFormat("###,###.000");
        DecimalFormat rateFormat = null;
        try
        {
            rateFormat = (DecimalFormat)NumberFormat.getPercentInstance();
        }
        catch(ClassCastException e)
        {
            System.err.println(e);
        }
        rateFormat.applyPattern("00.00%");
        do
        {
            insertTotalSize = insertNoNeedZipSize + insertNeedZipSize;
            insertFileCount = insertNoCompressCount + insertCompressCount;
            Log.logger.info("文件系统SN正在运行...");
            Log.logger.info((new StringBuilder("interval is(s): ")).append((double)interval / 1000D).toString());
            Log.logger.info((new StringBuilder("insertFile Succ: ")).append(insertNoCompressCount + insertCompressCount).toString());
            Log.logger.info((new StringBuilder("insertFile NoCompressed: ")).append(insertNoCompressCount).toString());
            Log.logger.info((new StringBuilder("insertFile Compressed: ")).append(insertCompressCount).toString());
            if(insertFileCount != 0)
                rate = (double)insertCompressCount / (double)insertFileCount;
            else
                rate = 0.0D;
            Log.logger.info((new StringBuilder("Compressed Count rate: ")).append(rateFormat.format(rate)).toString());
            Log.logger.info((new StringBuilder("insertFile Total Size: ")).append(sizeFormat.format((insertNoNeedZipSize + insertNeedZipSize) / 1073741824D)).append("GB").toString());
            Log.logger.info((new StringBuilder("insertFile Compressed Size: ")).append(sizeFormat.format(insertTotalCompressSize / 1073741824D)).append("GB").toString());
            if(insertTotalSize != 0.0D)
                rate = insertTotalCompressSize / insertTotalSize;
            else
                rate = 0.0D;
            Log.logger.info((new StringBuilder("TotalCompressed Size rate: ")).append(rateFormat.format(rate)).toString());
            Log.logger.info((new StringBuilder("insertFile NoNeedZip Size: ")).append(sizeFormat.format((double)TotalFileSize / 1073741824D)).append("GB").toString());
            Log.logger.info((new StringBuilder("insertFile NeedZip Size: ")).append(sizeFormat.format(insertNeedZipSize / 1073741824D)).append("GB").toString());
            Log.logger.info((new StringBuilder("insertFile NeedZipCompressed Size: ")).append(sizeFormat.format(insertNeedZipCompressSize / 1073741824D)).append("GB").toString());
            if(insertNeedZipSize != 0.0D)
                rate = insertNeedZipCompressSize / insertNeedZipSize;
            else
                rate = 0.0D;
            Log.logger.info((new StringBuilder("NeedZipCompressed Size rate: ")).append(rateFormat.format(rate)).toString());
            Log.logger.info((new StringBuilder("insertFile Failed: ")).append(insertFileError).toString());
            int t = 0;
            TOTAL_LIST_NUMBER = 0;
            for(int i = 0; i < THREAD_NUMBER; i++)
            {
                t = fileList[i].size();
                TOTAL_LIST_NUMBER += t;
            }

            Log.logger.info((new StringBuilder("fileList total rest: ")).append(TOTAL_LIST_NUMBER).toString());
            Log.logger.info((new StringBuilder("getFile Succ: ")).append(getFileCount).toString());
            Log.logger.info((new StringBuilder("getFile Failed: ")).append(getFileError).toString());
            try
            {
                Thread.sleep(interval);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        } while(true);
    }

    public int getID()
    {
        return ID;
    }

    public void setID(int id)
    {
        ID = id;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public static void main(String args[]) throws IOException
    {
        if(args.length == 0)
            Log.init("conf/log4j.properties", "log/sn.log");
        else
            Log.init("conf/log4j.properties", (new StringBuilder("log/sn")).append(args[0]).append(".log").toString());
        if(args.length < 2)
        {
            Log.logger.error("参数错误：sn sn_port sn_path");
            return;
        }
        int port = (new Integer(args[0])).intValue();
        String path = args[1];
        SN sn = null;
        try
        {
            sn = new SN(path, port);
        }
        catch(Exception e)
        {
            Log.logger.error((new StringBuilder("初始化SN错误, 原因: ")).append(e.toString()).toString());
            System.exit(1);
            return;
        }
        if(!sn.serveFSI(port))
            return;
        if(!sn.heartBeat())
        {
            return;
        } 
        else
        {
            loopStat();
            return;
        }
    }

    private int ID;
    private String path;
    private int port;
    private String ipMS;
    Timer timer;
    static int insertFileCount = 0;
    static int insertFileError = 0;
    static int insertCompressCount = 0;
    static int insertNoCompressCount = 0;
    static double insertNoNeedZipSize = 0.0D;
    static double insertNeedZipSize = 0.0D;
    static double insertNeedZipCompressSize = 0.0D;
    static double insertTotalSize = 0.0D;
    static double insertTotalCompressSize = 0.0D;
    static int getFileCount = 0;
    static int getFileError = 0;
    static int interval = 50000;
    static int THREAD_NUMBER = 1;
    static int TOTAL_LIST_NUMBER = 0;
    static int MAX_LIST_NUMBER = 0x186a0;
    static int TotalFileSize = 0;
    public static InsertFile insert[] = null;
    public static FSSendList fileList[] = null;
    static final double KB_TO_GB = 1073741824D;
    PersistConn pc;
}
