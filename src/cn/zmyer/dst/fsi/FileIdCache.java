/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name: FileIdCache.java
 *  
 *  Copyright 2013
 *  
 */

package cn.zmyer.dst.fsi;

import cn.zmyer.dst.com.FileId;
import cn.zmyer.dst.com.Protocol;
import cn.zmyer.dst.com.Serialization;
import cn.zmyer.dst.utlis.Log;
import java.io.*;
import java.util.ArrayList;

public class FileIdCache
{

    public FileIdCache()
    {
    }

    public static void setMSSocket(DataOutputStream _oos, DataInputStream _ois)
    {
        oos = _oos;
        ois = _ois;
    }

    public static void nextDay()
    {
        currDay++;
        currIndex = 0;
        fidList = null;
    }

    public static synchronized long nextId(boolean isLargeFile)
    {
        FileId fid = null;
        int id;
        do
        {
            fid = nextFid();
            id = fid.getId();
        } while((!isLargeFile || id % 2 != 0) && (isLargeFile || id % 2 != 1));
        return fid.getFileId();
    }

    public static synchronized void clearFid()
    {
        if(fidList != null)
            fidList = null;
    }

    @SuppressWarnings("unchecked")
	private static void getNewFileId()throws IOException
    {
        ArrayList arrayList = new ArrayList();
        String FileID = ois.readUTF();
        String id[] = FileID.split("/");
        if(id.length < 0)
        {
            System.out.println("FileID ·ÖÅäÊ§°Ü");
            System.exit(-1);
        }
        for(int i = 0; i < id.length; i++)
        {
            FileId ids = new FileId(Long.valueOf(id[i]).longValue());
            arrayList.add(ids);
        }

        fidList = (FileId[])arrayList.toArray(new FileId[0]);
        currIndex = 0;
    }

    public static FileId nextFid()
    {
		try {
			String sendBuf = Serialization.serialize(Protocol.PROT_MAGIC,Protocol.PROT_SN_RE);
			oos.writeUTF(sendBuf);
			oos.flush();
			
			String recvBuf = ois.readUTF();
			String[] splitBuf = Serialization.unserialize(recvBuf);
			if(Integer.parseInt(splitBuf[0]) != Protocol.PROT_MAGIC
					||Integer.parseInt(splitBuf[1]) != Protocol.PROT_MS_ACK)
				throw new AssertionError();
			
			getNewFileId();
		} catch (IOException e) {
			Log.logger.error((new StringBuilder(
							"FileIdCache:nexFid:MasterSN?????"))
							.append(e.toString()).toString());
			System.exit(-1);
		}
		return fidList[currIndex++];
    }

    private static int currIndex = 0;
    private static int currDay = 0;
    private static FileId fidList[] = null;
    private static DataOutputStream oos = null;
    private static DataInputStream ois = null;

}
