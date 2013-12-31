/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name: FSIWorkerThread.java
 *  
 *  Copyright 2013
 *  
 */   

package cn.zmyer.dst.ms;

import cn.zmyer.dst.com.FileId;
import cn.zmyer.dst.com.Protocol;
import cn.zmyer.dst.com.Serialization;
import cn.zmyer.dst.utlis.Log;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class FSIWorkerThread extends Thread
{
    public class CompareId implements Comparator<Object>
    {

        public int compare(Object arg0, Object arg1)
        {
            NodeDesc NewSnd = (NodeDesc)arg0;
            NodeDesc DeadSnd = (NodeDesc)arg1;
            return NewSnd.get_id() <= DeadSnd.get_id() ? 0 : 1;
        }

        public CompareId()
        {
            super();
        }
    }

    public FSIWorkerThread(Socket _socket)
    {
        os = null;
        is = null;
        fsi_index = -1;
        rightNow = null;
        days = 0;
        this._socket = _socket;
        try
        {
            os = new DataOutputStream(_socket.getOutputStream());
            is = new DataInputStream(_socket.getInputStream());
        }
        catch(IOException e)
        {
            Log.logger.error((new StringBuilder("FSI(")).append(this._socket.getInetAddress().getHostAddress()).append(":").append(this._socket.getPort()).append(")连接错误：").append(e.toString()).toString());
        }
    }

    public void run()
    {
        try
        {
            init_fsi();
            do{
                do_one_request();
            }while(true);
        }
        catch(IOException e)
        {
            Log.logger.error((new StringBuilder("FSI("))
            		.append(_socket.getInetAddress().getHostAddress())
            		.append(":").append(_socket.getPort()).append(")连接中断: ")
            	    .append(e.toString()).toString());
        }
        if(MasterSN.fsi_count > 0)
        	MasterSN.fsi_count--;
        MasterSN.mapFSI.remove(Integer.valueOf(fsi_index));
        try {
			_socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private void init_fsi()throws IOException
    {
    	String recvBuf = is.readUTF();
    	String[] splitBuf = Serialization.unserialize(recvBuf);
    	if(Integer.parseInt(splitBuf[0]) != Protocol.PROT_MAGIC
    			||Integer.parseInt(splitBuf[1])!= Protocol.PROT_MS_INIT )
            throw new AssertionError();
      
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
        String sendBuf = Serialization.serialize(Protocol.PROT_MAGIC,ListSN);
        os.writeUTF(sendBuf);
        os.flush();
        
        fsi_index = MasterSN.fsi_count;
        MasterSN.fsi_count++;
        String ip = _socket.getInetAddress().getHostAddress();
        Log.logger.info((new StringBuilder("FSI("))
        		.append(ip).append(":").append(_socket.getPort())
        		.append(")初始化完成！").toString());
    }

    @SuppressWarnings("unchecked")
	private void alloc_nodeid(List snd)throws IOException
    {
        int startid = 0;
        int startid1 = 0;
        int startid2 = 0;
        rightNow = Calendar.getInstance();
        days = (new Long(rightNow.getTimeInMillis() / 0x5265c00L)).shortValue();
        List fidlist = new ArrayList(100);
        if(snd.size() != 0)
        {
            if(((NodeDesc)snd.get(0)).get_id() == ((NodeDesc)snd.get(1)).get_id())
            {
                startid = MasterSN.getIds((NodeDesc)snd.get(0), days);
                for(int i = startid; i < startid + 100; i++)
                    fidlist.add(new FileId(((NodeDesc)snd.get(0)).get_id(), ((NodeDesc)snd.get(0)).get_id(), days, i));

            } 
            else if(((NodeDesc)snd.get(0)).get_id() != ((NodeDesc)snd.get(1)).get_id())
            {
                startid1 = MasterSN.getIds((NodeDesc)snd.get(0), days);
                startid2 = MasterSN.getIds((NodeDesc)snd.get(1), days);
                startid = startid1 <= startid2 ? startid2 : startid1;
                for(int i = startid; i < startid + 100; i++)
                    fidlist.add(new FileId(((NodeDesc)snd.get(0)).get_id(), ((NodeDesc)snd.get(1)).get_id(), days, i));
            }
        } 
        else
        {
            for(int i = startid; i < startid + 100; i++)
                fidlist.add(new FileId((byte)-1, (byte)-1, days, i));

        }
        
        String buffer = "";
        for(int i = 0; i < fidlist.size(); i++)
            buffer = (new StringBuilder(String.valueOf(buffer))).append(String.valueOf(((FileId)fidlist.get(i)).getFileId())).append("/").toString();

        os.writeUTF(buffer);
        os.flush();
        if(snd.size() != 0)
        {
            Log.logger.info((new StringBuilder("FSI(")).append(_socket.getInetAddress().getHostAddress()).append(":").append(_socket.getPort()).append(")请求分配ID，分配到第").append(((NodeDesc)snd.get(0)).get_id()).append(" 号SN，从").append(startid).append("开始的").append(100).append("个文件ID").toString());
            Log.logger.info((new StringBuilder("FSI(")).append(_socket.getInetAddress().getHostAddress()).append(":").append(_socket.getPort()).append(")请求分配ID，分配到第").append(((NodeDesc)snd.get(1)).get_id()).append(" 号SN，从").append(startid).append("开始的").append(100).append("个文件ID").toString());
        } 
        else
        {
            Log.logger.info((new StringBuilder("FSI(")).append(_socket.getInetAddress().getHostAddress()).append(":").append(_socket.getPort()).append(")请求分配ID出错，SN全挂了").toString());
        }
    }

    @SuppressWarnings("unchecked")
	private void allocate_nodeid()throws IOException
    {
    	String sendBuf = Serialization.serialize(Protocol.PROT_MAGIC,Protocol.PROT_MS_ACK);
    	os.writeUTF(sendBuf);
    	os.flush();

        List snd = select_two_sn();
        alloc_nodeid(snd);
    }

    private int getListNumber(int SnId)
    {
        int i = -1;
        for(i = 0; i < MasterSN.listSN.size(); i++)
            if(((NodeDesc)MasterSN.listSN.get(i)).get_id() == SnId)
                break;
        return i;
    }

    @SuppressWarnings("unchecked")
	private void UpdateSkSN_ListSN(int Dead_Sn_Id, int New_Sn_Id)throws IOException
    {
        CompareId compareid = new CompareId();
        if(getListNumber(Dead_Sn_Id) == -1 || getListNumber(New_Sn_Id) == -1)
        {
            return;
        } 
        else
        {
            NodeDesc NewSnd = (NodeDesc)MasterSN.listSN.get(getListNumber(New_Sn_Id));
            NodeDesc DeadSnd = (NodeDesc)MasterSN.listSN.get(getListNumber(Dead_Sn_Id));
            DeadSnd.set_ip(NewSnd.get_ip());
            DeadSnd.set_port(NewSnd.get_port());
            DeadSnd.set_type(NewSnd.get_type());
            DeadSnd.set_start(NewSnd.get_start());
            DeadSnd.set_isActive(true);
            NewSnd.set_isActive(false);
            MasterSN.listSN.remove(getListNumber(New_Sn_Id));
            MasterSN.listSN.remove(getListNumber(Dead_Sn_Id));
            MasterSN.listSN.add(DeadSnd);
            MasterSN.listSN.add(NewSnd);
            Collections.sort(MasterSN.listSN, compareid);
            return;
        }
    }

    private void update_conf()throws IOException
    {
    	String recvBuf = is.readUTF();
        if(recvBuf == null)
            return;
        String[] splitBuf = Serialization.unserialize(recvBuf);
        if(recvBuf == null)
        	return;    
        String subSplit[] = splitBuf[0].split("/");
        if(subSplit.length <= 0)
            return;
        int deadid = Integer.valueOf(subSplit[0]).intValue();
        int newid = Integer.valueOf(subSplit[1]).intValue();
        short port = Short.valueOf(subSplit[3]).shortValue();
        int Dead_Sn_Id = Integer.valueOf(splitBuf[1]).intValue();
        int New_Sn_Id = Integer.valueOf(splitBuf[2]).intValue();
        if(MasterSN.UpdateSNId(deadid, newid, subSplit[2], port))
            System.out.println("Update MS_SN_CONF successed");
        else
            System.out.println("Update MS_SN_CONF failed");
        UpdateSkSN_ListSN(Dead_Sn_Id, New_Sn_Id);
    }

    @SuppressWarnings("unchecked")
	private void add_idmap(int type)throws IOException
    {
        String existFileName = "";
        String fileName = is.readUTF();
        if(fileName.equalsIgnoreCase(""))
        {
            os.writeUTF(fileName);
            os.flush();
            return;
        }
        String subString[] = fileName.split("#");
        for(int i = 0; i < subString.length; i++)
        {
            String fileInfo[] = subString[i].split("%");
            if(fileInfo != null && fileInfo.length != 0)
                if(MasterSN.IDMap.containsKey(Long.valueOf(fileInfo[0])) || MasterSN.IDMap.containsValue(fileInfo[1]))
                    existFileName = (new StringBuilder(String.valueOf(existFileName))).append(fileInfo[1]).append("%").toString();
                else
                    MasterSN.IDMap.put(Long.valueOf(fileInfo[0]), fileInfo[1]);
        }

        os.writeUTF(existFileName);
        os.flush();
        MasterSN.UpdateIDMapConf();
    }

    private void delete_idmap(int type)throws IOException
    {
        String fileId = "";
        String fileName = is.readUTF();
        String subString[] = fileName.split("#");
        if(subString == null || subString.length <= 0)
            return;
        for(int i = 0; i < subString.length; i++)
        {
            String fileInfo[] = subString[i].split("%");
            if(MasterSN.IDMap.containsKey(Long.valueOf(fileInfo[0])) || MasterSN.IDMap.containsValue(fileInfo[1]))
            {
                fileId = (new StringBuilder(String.valueOf(fileId))).append(fileInfo[0]).append("%").append(fileInfo[1]).append("#").toString();
                MasterSN.IDMap.remove(Long.valueOf(fileInfo[0]));
            }
        }
        os.writeUTF(fileId);
        os.flush();
        MasterSN.UpdateIDMapConf();
    }

    @SuppressWarnings("unchecked")
	private void get_idmap(int type)throws IOException
    {
        String fileId = "";
        Set entry = MasterSN.IDMap.entrySet();
        String fileName = is.readUTF();
        if(!fileName.equalsIgnoreCase(""))
        {
            Iterator it = entry.iterator();
            java.util.Map.Entry id = null;
            while(it.hasNext()) 
            {
                id = (java.util.Map.Entry)it.next();
                if(((String)id.getValue()).contains(fileName))
                    fileId = (new StringBuilder(String.valueOf(fileId))).append(String.valueOf(id.getKey())).append("%").append((String)id.getValue()).append("#").toString();
            }
        } 
        else
        {
            for(Iterator it = entry.iterator(); it.hasNext();)
            {
                java.util.Map.Entry id = (java.util.Map.Entry)it.next();
                fileId = (new StringBuilder(String.valueOf(fileId))).append(String.valueOf(id.getKey())).append("%").append((String)id.getValue()).append("#").toString();
            }

        }
        os.writeUTF(fileId);
        os.flush();
    }

    private void do_one_request() throws IOException
    {
        int magic = is.readInt();
        if(magic != 0xcbadefce)
            throw new AssertionError();
        int type = is.readByte();
        if(type != Protocol.PROT_SN_RE && type != Protocol.PROT_UPDATE_CONF && type != Protocol.PROT_ADD_FILE && type != Protocol.PROT_SN_OK && type !=Protocol.PROT_GET_FILE)
            return;
        switch(type)
        {
        case Protocol.PROT_SN_RE: // 'r'
            allocate_nodeid();
            break;

        case Protocol.PROT_UPDATE_CONF: // 'c'
            update_conf();
            break;

        case Protocol.PROT_ADD_FILE: // 'j'
            add_idmap(type);
            break;

        case Protocol.PROT_SN_OK: // 'o'
            delete_idmap(type);
            break;

        case Protocol.PROT_GET_FILE: // 'm'
            get_idmap(type);
            break;
        }
    }

    @SuppressWarnings("unchecked")
	private synchronized List select_two_sn()
    {
        SSFactory ssf = new SSFactory();
        SnSelect ss = ssf.getSS(1);
        List snd = new ArrayList(2);
        int snd_index[] = ss.getSnIndex();
        for(int i = 0; i < snd_index.length; i++)
            System.out.println((new StringBuilder("snd_index[i]=")).append(snd_index[i]).toString());

        if(snd_index[0] == -1 && snd_index[1] == -1)
            return snd;
        if(snd_index[0] == -1)
        {
            snd.add((NodeDesc)MasterSN.listSN.get(snd_index[1]));
            snd.add((NodeDesc)MasterSN.listSN.get(snd_index[1]));
        } else
        if(snd_index[1] == -1)
        {
            snd.add((NodeDesc)MasterSN.listSN.get(snd_index[0]));
            snd.add((NodeDesc)MasterSN.listSN.get(snd_index[0]));
        } else
        {
            snd.add((NodeDesc)MasterSN.listSN.get(snd_index[0]));
            snd.add((NodeDesc)MasterSN.listSN.get(snd_index[1]));
        }
        if(snd.size() == 0)
            System.out.println("snd is null");
        return snd;
    }

    Socket _socket;
    DataOutputStream os;
    DataInputStream is;
    int fsi_index;
    Calendar rightNow;
    short days;
}
