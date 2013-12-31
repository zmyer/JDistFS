/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name:FSSNThread.java
 *  
 *  Copyright 2013
 *  
 */

package cn.zmyer.dst.sn;

import cn.zmyer.dst.com.FileId;
import cn.zmyer.dst.com.Protocol;
import cn.zmyer.dst.com.Serialization;
import cn.zmyer.dst.utlis.Log;
import java.io.*;
import java.net.Socket;
import java.util.Vector;

public class FSSNThread extends Thread
{

    @SuppressWarnings("unchecked")
	public FSSNThread(SN sn, Socket socket)
    {
        this.sn = null;
        this.socket = null;
        id = -1L;
        fos = null;
        fileId = null;
        filePath = null;
        readlen = 0;
        fileLen = 0L;
        buffer = new byte[0x100000];
        ids = new Vector();
        this.sn = sn;
        this.socket = socket;
    }

    private String fileIdToPath(FileId fileId, boolean bMkdir)
    {
        String fids = "";
        int fid = fileId.getId();
        byte snid[] = fileId.getSnId();
        for(int i = 0; i < snid.length; i++)
            fids = (new StringBuilder(String.valueOf(fids))).append(String.valueOf(snid[i])).append("_").toString();

        String tmppath = (new StringBuilder(String.valueOf(sn.getPath()))).append("/").append(fids).toString();
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

    private Long getIds(String absoPath)
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
        long days = (new Long(temp & 0xffff)).longValue();
        ID = Long.valueOf(days << 48);
        temp = fids;
        ID = Long.valueOf(ID.longValue() | temp << 16);
        temp = Id[1];
        ID = Long.valueOf(ID.longValue() | temp << 8);
        temp = Id[0];
        ID = Long.valueOf(ID.longValue() | temp);
        return ID;
    }

    @SuppressWarnings("unchecked")
	private void GetFileIds(String path)
    {
        File file = new File(path);
        if(!file.isDirectory())
        {
            String absolutePath = file.getPath();
            Long ID = getIds(absolutePath);
            if(ID.longValue() != -1L)
                ids.add(ID);
        } else
        {
            String fileList[] = file.list();
            for(int i = 0; i < fileList.length; i++)
                GetFileIds((new StringBuilder(String.valueOf(path))).append("\\").append(fileList[i]).toString());

        }
    }

    private boolean InsertFile(DataOutputStream os, DataInputStream is)throws IOException
    {
        boolean insertSucc = true;
        String recvBuf = is.readUTF();
        if(recvBuf == null)
        	return false;
        
        String[] splitBuf = Serialization.unserialize(recvBuf);
        id = Integer.parseInt(splitBuf[0]);
        if(id == -1L)
        {
            Log.logger.error(" 文件存储节点SN与FSI通信协议异常, 文件ID出错!");
            return false;
        }
    
        try
        {
            fileId = new FileId(id);
            filePath = fileIdToPath(fileId, true);
            File file = new File(filePath);
            if(file.exists())
                throw new AssertionError();
            fos = new FileOutputStream(file);
            for(fileLen = is.readLong(); fileLen > 0L && (readlen = is.read(buffer)) > 0;)
            {
                fileLen -= readlen;
                fos.write(buffer, 0, readlen);
                fos.flush();
            }

        }
        catch(IOException e)
        {
            Log.logger.error((new StringBuilder("文件系统SN插入文件失败，文件ID:")).append(fileId).append(" ，文件长度:").append(buffer.length).append("，原因: ").append(e.toString()).toString());
            insertSucc = false;
        }
        fos.close();
        if(insertSucc)
        {
        	String sendBuf = Serialization.serialize(Protocol.PROT_SN_OK);
        	os.writeUTF(sendBuf);
            os.flush();
            return true;
        } 
        else
        {
        	String sendBuf = Serialization.serialize(Protocol.PROT_MAGIC,Protocol.PROT_SN_RE,Protocol.PROT_SN_FAILED);
        	os.writeUTF(sendBuf);
            os.flush();
            return false;
        }
    }

    private boolean GetFile(DataOutputStream os, DataInputStream is) throws IOException
    {
        long fileId = -1L;
        String recvBuf = is.readUTF();
        if(recvBuf == null)
        	return false;
        
        String[] splitBuf = Serialization.unserialize(recvBuf);
        fileId = Integer.parseInt(splitBuf[0]);
        if(fileId == -1L)
        {
            Log.logger.error("文件存储节点SN与FSI通信协议异常, 文件ID出错!");
            return false;
        }
        int readlen = 0;
        FileId fid = new FileId(fileId);
        FileInputStream fis = sn.getFile(fid);
        String filePath = sn.fileIdToPath(fid, true);
        File fs = new File(filePath);
        Long fileLen = Long.valueOf(fs.length());
        byte tp = Protocol.PROT_SN_SEND;
        if(fis == null)
        {
        	String sendBuf = Serialization.serialize(Protocol.PROT_MAGIC,tp,0L);
        	os.writeUTF(sendBuf);
            os.flush();
            return false;
        }
        String sendBuf = Serialization.serialize(Protocol.PROT_MAGIC,tp,1L,fileLen.longValue());
        os.writeUTF(sendBuf);
        os.flush();
        while((readlen = fis.read(buffer)) > 0) 
        {
            os.write(buffer, 0, readlen);
            os.flush();
        }
        fis.close();
        return true;
    }

    private void GetFileId(DataOutputStream os, DataInputStream is) throws IOException
    {
        String id = "";
        int pos = 0;
        int idBufferLen = 0;
        int end = 0;
        int needSendLen = 0;
        
        ids.clear();
        GetFileIds(sn.getPath());
        
        if(ids.size() == 0)
        {
        	String sendBuf = Serialization.serialize(0);
        	os.writeUTF(sendBuf);
            os.flush();
            return;
        }
        try
        {
            for(int i = 0; i < ids.size(); i++)
                id = (new StringBuilder(String.valueOf(id))).append(String.valueOf(ids.get(i))).append("/").toString();

            idBufferLen = id.length();
        	String sendBuf = Serialization.serialize(idBufferLen);
        	os.writeUTF(sendBuf);
        	os.flush();
        	
            for(needSendLen = idBufferLen; idBufferLen > pos && needSendLen > 0; needSendLen -= 1024)
            {
                end = pos + 1024;
                if(end >= idBufferLen)
                    end = idBufferLen;
                String subString = id.substring(pos, end);
                os.writeUTF(subString);
                os.flush();
                pos = end;
            }

        }
        catch(IOException e)
        {
            Log.logger.error(e.toString());
        }
        return;
    }

    private void DeleteFile(DataOutputStream os, DataInputStream is)throws IOException
    {
    	String recvBuf = is.readUTF();
    	if(recvBuf == null)
    		return;
    	String[] splitBuf = Serialization.unserialize(recvBuf);
    	id = Integer.parseInt(splitBuf[0]);
        FileId fid = new FileId(id);
        String filepath = fileIdToPath(fid, false);
        File file = new File(filepath);
        if(file.exists() && file.delete())
        {
        	String sendBuf = Serialization.serialize(Protocol.PROT_MAGIC,Protocol.PROT_FILE_DELETE_OK);
        	os.writeUTF(sendBuf);
            os.flush();
        } 
        else
        {
        	String sendBuf = Serialization.serialize(Protocol.PROT_MAGIC,Protocol.PROT_FILE_DELETE_FAIL);
        	os.writeUTF(sendBuf);
            os.flush();
        }
    }

    private void MoveFile(DataOutputStream os, DataInputStream is) throws IOException
    {
    	String recvBuf = is.readUTF();
    	if(recvBuf == null)
    		return;
    	String[] splitBuf = Serialization.unserialize(recvBuf);
        Long id = Long.valueOf(splitBuf[0]);
        fileId = new FileId(id.longValue());
        filePath = fileIdToPath(fileId, true);
        File file = new File(filePath);
        if(file.exists())
            throw new AssertionError();
        fos = new FileOutputStream(file);
        Long fileLen = Long.valueOf(Integer.parseInt(splitBuf[1]));
        for(int len = 0; fileLen.longValue() > 0L && (len = is.read(buffer)) > 0; fos.flush())
        {
            fileLen = Long.valueOf(fileLen.longValue() - (long)len);
            fos.write(buffer, 0, len);
        }
        String sendBuf = Serialization.serialize(Protocol.PROT_SEND_OK);
        os.writeUTF(sendBuf);
        os.flush();
    }

    public void run()
    {
    	DataOutputStream os = null;
    	DataInputStream is = null;
        try
        {
            os = new DataOutputStream(socket.getOutputStream());
            is = new DataInputStream(socket.getInputStream());
            do
            {
            	String recvBuf = is.readUTF();
            	if(recvBuf == null)
            		break;
            	String[] splitBuf = Serialization.unserialize(recvBuf);
                int magic = Integer.parseInt(splitBuf[0]);
                if(magic != Protocol.PROT_MAGIC)
                {
                    Log.logger.error((new StringBuilder("文件存储节点SN与FSI通信协议异常, 魔数: ")).append(Integer.toHexString(magic)).append("(应为:").append(Integer.toHexString(Protocol.PROT_MAGIC)).append(")").toString());
                    continue;
                }
                byte type = (byte) Integer.parseInt(splitBuf[1]);
                if(type != Protocol.PROT_SN_ADD 
                		&& type != Protocol.PROT_SN_GET 
                		&& type != Protocol.PROT_GET_FILE_ID 
                		&& type != Protocol.PROT_SEND_FILE_ID 
                		&& type != Protocol.PROT_SN_HB)
                {
                    Log.logger.error("文件存储节点SN与FSI通信协议异常, 操作类型出错!");
                    continue;
                }
                switch(type)
                {
                case Protocol.PROT_SN_ADD : // 'a'
                    InsertFile(os, is);
                    break;

                case Protocol.PROT_SN_GET:// 'g'
                    GetFile(os, is);
                    break;

                case Protocol.PROT_GET_FILE_ID: // 'd'
                    GetFileId(os, is);
                    break;

                case Protocol.PROT_SEND_FILE_ID: // 'l'
                    MoveFile(os, is);
                    break;

                case Protocol.PROT_SN_HB: // 'h'
                    DeleteFile(os, is);
                    break;
                }
            } while(true);
        }
        catch(IOException e)
        {
            Log.logger.error((new StringBuilder("文件存储节点SN与FSI通信异常: ")).append(e.toString()).toString());
        }
        try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    SN sn;
    private Socket socket;
    private long id;
    private FileOutputStream fos;
    private FileId fileId;
    private String filePath;
    private int readlen;
    private long fileLen;
    private byte buffer[];
    @SuppressWarnings("unchecked")
	Vector ids;
}
