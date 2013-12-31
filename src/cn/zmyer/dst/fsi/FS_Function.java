/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name: FS_Function.java
 *  
 *  Copyright 2013
 *  
 */

package cn.zmyer.dst.fsi;
import cn.zmyer.dst.com.FSFile;
import cn.zmyer.dst.com.FSSendList;
import cn.zmyer.dst.com.FileId;
import cn.zmyer.dst.com.PersistConn;
import cn.zmyer.dst.com.Protocol;
import cn.zmyer.dst.com.Serialization;
import cn.zmyer.dst.ms.NodeDesc;
import cn.zmyer.dst.utlis.Log;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class FS_Function {
	class SNInsert extends Thread {
		
		public void run() 
		{
			do { 	
				byte sn_id = -1;
				synchronized (insertCache)
				{
					if (insertCache.size() > 0) 
					{
						for (int i = 0; i < listSN.size(); i++) 
						{
							NodeDesc snd = (NodeDesc) listSN.get(i);
							sn_id = (new Byte(snd.get_id())).byteValue();
							FSSendList fileList = (FSSendList) insertCache.get(Integer.valueOf(sn_id));
							if (fileList != null 
									&& sn_id != -1
									&& ((FSSendList) insertCache.get(Integer.valueOf(sn_id))).size() != 0) 
							{
								((FSSendList) insertCache.get(Integer.valueOf(sn_id))).moveList(sendList);
								try
								{
									sendFileList(sn_id, sendList.getFileList());
								} catch (IOException e)
								{
									e.printStackTrace();
								}
							}
						}

					}
				}
			} while (true);
		}

		FSSendList sendList;
		public SNInsert() {
			super();
			sendList = new FSSendList();
		}
	}

	@SuppressWarnings("unchecked")
	public FS_Function() throws IOException {
		skSN = new HashMap();
		insertCache = new HashMap();
		listSN = new ArrayList();
		INSERT_THREAD_NUMBER = 1;
		ipMS = null;
		oos = null;
		ois = null;
		compressSize = 102400000D;
		Log.init("conf/log4j.properties", "log/fsi.log");
		initFSI();
		connMS();
		connSN();
		serverFSIUpdate();
	}

    @SuppressWarnings("unchecked")
	public FS_Function(String root_path, String msUrl) throws IOException {
		skSN = new HashMap();
		insertCache = new HashMap();
		listSN = new ArrayList();
		INSERT_THREAD_NUMBER = 1;
		ipMS = null;
		oos = null;
		ois = null;
		compressSize = 102400000D;
		Log.init("conf/log4j.properties", "log/fsi.log");
		ipMS = msUrl;
		connMS();
		connSN();
		serverFSIUpdate();
	}

	private void initFSI() throws IOException 
	{
		BufferedReader br;
		String confLine;
		br = new BufferedReader(new FileReader(new File("conf/fsi.conf")));
		confLine = br.readLine();
		if (confLine == null) {
			br.close();
			return;
		}
		try {
			ipMS = confLine;
			confLine = br.readLine();
			if (confLine != null)
				INSERT_THREAD_NUMBER = (new Integer(confLine)).intValue();
			br.close();
			br = new BufferedReader(new FileReader(new File(
					"conf/fsCompress.conf")));
			confLine = br.readLine();
			if (confLine != null) {
				double confSize = (new Double(confLine)).doubleValue();
				compressSize = confSize * 1024D;
			}
			br.close();
			SNInsert insertThread[] = new SNInsert[INSERT_THREAD_NUMBER];
			for (int i = 0; i < insertThread.length; i++) {
				insertThread[i] = new SNInsert();
				insertThread[i].start();
			}

		} catch (Exception e) 
		{
			Log.logger.error((new StringBuilder("FSI 初始化失败: ")).append(e.toString()).append("此FSI退出").toString());
			System.exit(1);
		}
		return;
	}

	@SuppressWarnings("unchecked")
	private void connMS()
	{
		try {
			PersistConn pc = new PersistConn(ipMS, Short.parseShort(Integer.toString(10002)));
			Socket sock = pc.getSocket();
			if (sock == null)
			{
				System.out.println((new StringBuilder("连接MasterSN: "))
								.append(ipMS)
								.append(":")
								.append(10002)
								.append("失败!，此FSI退出")
								.toString());
				pc.close();
				System.exit(1);
			} 
			else {
				System.out.println((new StringBuilder("连接MasterSN: "))
								.append(ipMS).append(":")
								.append(10002).append("成功!").toString());
			}
			oos = pc.getOutputStream();
			ois = pc.getInputStream();
			FileIdCache.setMSSocket(oos, ois);

			String sendBuf = Serialization.serialize(Protocol.PROT_MAGIC,Protocol.PROT_MS_INIT);
			oos.writeUTF(sendBuf);
			oos.flush();
			
			String recvBuf = ois.readUTF();
			if(recvBuf ==null)
				return;
			
			String[] splitBuf = Serialization.unserialize(recvBuf);
			NodeDesc desc = null;
			int magic = Integer.parseInt(splitBuf[0]);
			if (magic != Protocol.PROT_MAGIC)
				throw new AssertionError();
			
			String ListSN = splitBuf[1];
			if (listSN != null)
				System.out.println("FSI<->MasterSN初始化成功!");
			String split[] = ListSN.split("#");
			for (int i = 0; i < split.length; i++) {
				String subSplit[] = split[i].split("/");
				if (subSplit.length >= 6) 
				{
					desc = new NodeDesc(
							Short.valueOf(subSplit[0]).shortValue(), 
							Byte.valueOf(subSplit[1]).byteValue(),
							subSplit[2], 
							Short.valueOf(subSplit[3]).shortValue());
					if (Boolean.valueOf(subSplit[5]).booleanValue())
						desc.set_isActive(true);
					listSN.add(desc);
				}
			}

		} catch (Exception e) {
			Log.logger.error((new StringBuilder("FSI: 连接MasterSN失败: "))
					.append(e.toString()).append("此FSI退出！")
					.toString());
			System.exit(1);
		}
	}

	public static void init(String rootPath, String msUrl) throws IOException {
		if (fs_fsi == null)
			fs_fsi = new FS_Function(rootPath, msUrl);
	}

	@SuppressWarnings("unchecked")
	private void connSN() {
		if (listSN == null)
			return;
		for (int i = 0; i < listSN.size(); i++) 
		{
			NodeDesc snd = (NodeDesc) listSN.get(i);
			if (snd.get_isActive() && snd.get_port() >= 0) 
			{
				PersistConn tmp = new PersistConn(snd.get_ip(), snd.get_port());
				Socket sock = tmp.getSocket();
				if (sock == null) {
					System.out.println((new StringBuilder("连接SN("))
							.append(snd.get_id()).append("): ")
							.append(snd.get_ip()).append(":")
							.append(snd.get_port()).append("失败!")
							.toString());
					return;
				}
				System.out.println((new StringBuilder("连接SN("))
						.append(snd.get_id()).append("): ")
						.append(snd.get_ip()).append(":")
						.append(snd.get_port()).append("成功!")
						.toString());
				skSN.put(new Integer(snd.get_id()), tmp);
				FSSendList tmpList = new FSSendList();
				synchronized (insertCache) {
					insertCache.put(new Integer(snd.get_id()), tmpList);
				}
			}
		}

	}

	private void serverFSIUpdate() {
		try {
			ServerSocket skSNServer = new ServerSocket(10003);
			Log.logger.info("绑定FSI服务端口：10003成功！");
			(new FSIThread(skSNServer)).start();
		} catch (IOException e) {
			Log.logger.error((new StringBuilder("绑定FSI服务端口：10003失败: "))
							.append(e.toString())
							.append("此FSI退出！").toString());
			System.exit(1);
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized void updateConn(int type, int sn_index, List listSN) {
		NodeDesc snd = (NodeDesc) listSN.get(sn_index);
		if (type == 0) {
			PersistConn pc = new PersistConn(snd.get_ip(), snd.get_port());
			if (pc == null) {
				System.out.println((new StringBuilder("新增SN，FSI连接SN(").append(snd.get_id()).append(":").append(snd.get_ip()).append(":").append(snd.get_port()).append("失败!").toString()));
				return;
			}
			System.out.println((new StringBuilder("新增SN，FSI连接SN(")).append(snd.get_id()).append("): ").append(snd.get_ip()).append(":").append(snd.get_port()).append("成功!").toString());
			skSN.put(new Integer(snd.get_id()), pc);
			snd.get_id();
			
			FSSendList tmpList = new FSSendList();
			synchronized (insertCache) {
				insertCache.put(new Integer(snd.get_id()), tmpList);
			}
		} else if (type == 1) {
			int sn_id = (new Integer(snd.get_id())).intValue();
			PersistConn pc = (PersistConn) skSN.get(Integer.valueOf(sn_id));
			pc.close();
			System.out.println((new StringBuilder("SN("))
							.append(snd.get_id())
							.append("): ")
							.append(snd.get_ip())
							.append(":")
							.append(snd.get_port())
							.append("挂了!，FSI关闭和此SN的连接成功").toString());
			skSN.remove(Integer.valueOf(sn_id));
			synchronized (insertCache) {
				insertCache.remove(Integer.valueOf(sn_id));
			}
		}
	}

	public static synchronized FS_Function getInstance() throws IOException {
		if (fs_fsi == null)
			fs_fsi = new FS_Function();
		return fs_fsi;
	}

	public long genFileId(boolean isLargeFile) {
		return FileIdCache.nextId(isLargeFile);
	}

	@SuppressWarnings("unchecked")
	private synchronized boolean sendFile(byte snId[], long id, InputStream fs,long fileLen) throws IOException 
	{
		Vector pcList = new Vector();
		Vector oss = new Vector();
		Vector iss = new Vector();
		DataOutputStream os = null;
		DataInputStream is = null;
		PersistConn pc = null;
		for (int i = 0; i < snId.length; i++)
			if (snId[i] != -1 && snId[i] != 0) {
				pc = (PersistConn) skSN.get(Integer.valueOf(snId[i]));
				if (pc != null)
					pcList.add(pc);
			}

		int readlen = 0;
		byte buffers[] = new byte[0x100000];
		for (int i = 0; i < pcList.size(); i++) {
			os = ((PersistConn) pcList.get(i)).getOutputStream();
			if (os != null) {
				oss.add(os);
				is = ((PersistConn) pcList.get(i)).getInputStream();
				if (is != null)
					iss.add(is);
			}
		}

		try {
			for (int i = 0; i < oss.size(); i++) {
				String sendBuf = Serialization.serialize(Protocol.PROT_MAGIC,Protocol.PROT_MS_ADD,id,fileLen);
				((DataOutputStream) oss.get(i)).writeUTF(sendBuf);
				((DataOutputStream) oss.get(i)).flush();
			}

			while ((readlen = fs.read(buffers)) > 0) {
				for (int i = 0; i < oss.size(); i++) {
					((DataOutputStream) oss.get(i)).write(buffers, 0, readlen);
					((DataOutputStream) oss.get(i)).flush();
				}

			}
			int result = -1;
			String resBuf = null;
			for (int i = 0; i < iss.size(); i++) {
				resBuf = ((DataInputStream) iss.get(i)).readUTF();
				result = Integer.parseInt(resBuf);
				if(result != Protocol.PROT_SN_OK)
					throw new IOException();
			}

		} catch (IOException e) {
			Log.logger.error((new StringBuilder(
					"FSI 插入文件失败: "))
					.append(e.toString()).toString());
			return false;
		}
		oss.clear();
		iss.clear();
		return true;
	}

	@SuppressWarnings("unchecked")
	private synchronized boolean sendBuffer(byte snId[], long id,byte buffer[], int bufflen) throws IOException 
	{
		Vector pcList = new Vector();
		Vector oss = new Vector();
		Vector iss = new Vector();
		DataOutputStream os = null;
		DataInputStream is = null;
		PersistConn pc = null;
		for (int i = 0; i < snId.length; i++)
			if (snId[i] != -1 && snId[i] != -1) {
				pc = (PersistConn) skSN.get(Integer.valueOf(snId[i]));
				if (pc != null)
					pcList.add(pc);
			}

		for (int i = 0; i < pcList.size(); i++) {
			os = ((PersistConn) pcList.get(i)).getOutputStream();
			if (os != null) {
				oss.add(os);
				is = ((PersistConn) pcList.get(i)).getInputStream();
				if (is != null) {
					iss.add(is);
					os = null;
					is = null;
				}
			}
		}

		try {
			for (int i = 0; i < oss.size(); i++) {
				String sendBuf = Serialization.serialize(Protocol.PROT_MAGIC,Protocol.PROT_MS_ADD,id,bufflen);
				((DataOutputStream) oss.get(i)).writeUTF(sendBuf);
				((DataOutputStream) oss.get(i)).flush();
			}

			for (int i = 0; i < oss.size(); i++) {
				((DataOutputStream) oss.get(i)).write(buffer, 0, bufflen);
				((DataOutputStream) oss.get(i)).flush();
			}

			int result = -1;
			String resBuf = null;
			for (int i = 0; i < iss.size(); i++)
			{
				resBuf = ((DataInputStream) iss.get(i)).readUTF();
				result = Integer.parseInt(resBuf);
				if(result != Protocol.PROT_SN_OK)
					throw new IOException();
			}
		} catch (IOException e) {
			Log.logger.error((new StringBuilder("FSI 插入文件失败: "))
					.append(e.toString()).toString());
			return false;
		}
		oss.clear();
		iss.clear();
		return true;
	}

	@SuppressWarnings("unchecked")
	private boolean sendFileList(byte snId, ArrayList arrayList)throws IOException 
	{
		FileInputStream fis = null;
		File fs = null;
		long id = -1L;
		int len = arrayList.size();
		long fileLen = -1L;
		byte snIds[] = new byte[2];
		snIds[0] = snId;
		snIds[1] = -1;
		for (int i = 0; i < len; i++) {
			id = ((FSFile) arrayList.get(i)).getFileId();
			fs = ((FSFile) arrayList.get(i)).getFile();
			fileLen = ((FSFile) arrayList.get(i)).getFileSize();
			fis = new FileInputStream(fs);
			if (!sendFile(snIds, id, fis, fileLen))
				Log.logger.error((new StringBuilder("File ID="))
						.append(id)
						.append(", Insert SnID=").append(snId)
						.append(" Failed").toString());
		}

		return true;
	}

	private boolean sendFileDirec(long id, InputStream is, long fileLen)throws IOException 
	{
		FileId fid = new FileId(id);
		byte snId[] = fid.getSnId();
		boolean succ = false;
		for (int i = 0; i < snId.length; i++)
		{
			for (int j = i + 1; j < snId.length; j++)
				if (snId[j] == snId[i])
					snId[j] = -1;
		}

		if (sendFile(snId, id, is, fileLen))
			succ = true;
		return succ;
	}

	private synchronized DataInputStream getFileStream(long id) throws IOException 
	{
		FileId fi = new FileId(id);
		byte snId[]= fi.getSnId();
		DataInputStream is = null;
		DataOutputStream os = null;
		int index = 0;
		
		for (; index < snId.length; ++index) 
		{
			PersistConn pc;
			if ((long) snId[index] == -1L)
				continue; /* Loop/switch isn't completed */
			pc = (PersistConn) skSN.get(Integer.valueOf(snId[index]));
			if (pc == null) {
				continue; /* Loop/switch isn't completed */
			}
			try
			{
				os = pc.getOutputStream();
				if (os == null)
					throw new IOException();

				String sendBuf = Serialization.serialize(Protocol.PROT_MAGIC,Protocol.PROT_SN_GET,id);
				os.writeUTF(sendBuf);
				os.flush();
			}
			// Misplaced declaration of an exception variable
			catch (IOException e) {
				Log.logger.error((new StringBuilder("FSI 发送获取文件请求失败: ")).append(e.toString()).toString());
				continue; /* Loop/switch isn't completed */
			}
			is = pc.getInputStream();
			if (is == null)
				throw new IOException();
			
			String recvBuf = is.readUTF();
			if(recvBuf == null)
				throw new IOException();
			
			String[] splitBuf = Serialization.unserialize(recvBuf);
			if(Integer.parseInt(splitBuf[0]) != Protocol.PROT_MAGIC
					|| Integer.parseInt(splitBuf[1]) != Protocol.PROT_SEND_OK)
				throw new IOException();
			Long len = Long.valueOf(splitBuf[2]);
			if (len.longValue() == 0L)
				continue; /* Loop/switch isn't completed */
			return is;
		}
		return null;
	}

	private boolean transFile(long id, DataOutputStream os) throws IOException
	{	
		int len = 0;
		DataInputStream fis = getFileStream(id);
		if (fis == null)
			return false;
		
		byte buffer[] = new byte[0x100000];
		String recvBuf = fis.readUTF();
		if(recvBuf == null)
			return false;
		
		Long fileLen = Long.valueOf(recvBuf);
		os.writeLong(fileLen.longValue());
		for (; fileLen.longValue() > 0L && (len = fis.read(buffer)) > 0; os.flush()) 
		{
			fileLen = Long.valueOf(fileLen.longValue() - (long) len);
			os.write(buffer, 0, len);
		}
		fis = null;
		return true;
	}

	@SuppressWarnings("unchecked")
	private void UpdateSkSN_ListSN(int Dead_Sn_Id, int New_Sn_Id)throws IOException
	{
		if (getListNumber(Dead_Sn_Id) == -1 || getListNumber(New_Sn_Id) == -1)
			return;
		else 
		{
			NodeDesc NewSnd = (NodeDesc) listSN.get(getListNumber(New_Sn_Id));
			NodeDesc DeadSnd = (NodeDesc) listSN.get(getListNumber(Dead_Sn_Id));
			DeadSnd.set_ip(NewSnd.get_ip());
			DeadSnd.set_port(NewSnd.get_port());
			DeadSnd.set_type(NewSnd.get_type());
			DeadSnd.set_start(NewSnd.get_start());
			DeadSnd.set_isActive(true);
			NewSnd.set_isActive(false);
			PersistConn pc = (PersistConn) skSN.get(Integer.valueOf(New_Sn_Id));
			skSN.remove(Byte.valueOf(DeadSnd.get_id()));
			skSN.remove(new Integer(NewSnd.get_id()));
			skSN.put(new Integer(DeadSnd.get_id()), pc);
			return;
		}
	}

	@SuppressWarnings("unchecked")
	private Vector GetFileId(int New_Sn_Id, int Dead_Sn_Id, Vector snID)throws IOException 
	{
		PersistConn pc = null;
		FileId fi = null;
		byte snId[] = (byte[]) null;
		DataOutputStream os = null;
		DataInputStream is = null;
		Vector id = new Vector();
		
		for (int j = 0; j < snID.size(); j++) {
			pc = (PersistConn) skSN.get(snID.get(j));
			if (pc == null)
				return null;
			if (((Integer) snID.get(j)).intValue() != New_Sn_Id) {
				os = pc.getOutputStream();
				if (os == null)
					throw new IOException();

				String sendBuf = Serialization.serialize(Protocol.PROT_MAGIC,Protocol.PROT_GET_FILE_ID);
				os.writeUTF(sendBuf);
				os.flush();
				
				is = pc.getInputStream();
				if (is == null)
					throw new IOException();
				
				String recvBuf = is.readUTF();
				int idBuffLen = Integer.parseInt(recvBuf);
				if (idBuffLen == 0) {
					os = null;
					is = null;
				} else {
					String filid = "";
					String subString = "";
					for (; idBuffLen > 0; idBuffLen -= subString.length()) {
						subString = is.readUTF();
						if (subString == null || subString.length() <= 0)
							break;
						filid = (new StringBuilder(String.valueOf(filid)))
								.append(subString).toString();
					}

					String FileSpilt[] = filid.split("/");
					if (FileSpilt.length <= 0) {
						os = null;
						is = null;
					} else {
						for (int k = 0; k < FileSpilt.length; k++) {
							long ids = Long.valueOf(FileSpilt[k]).longValue();
							if (ids <= 0L) {
								os = null;
								is = null;
							} else {
								fi = new FileId(ids);
								snId = fi.getSnId();
								for (int s = 0; s < snId.length; s++)
									if (snId[s] == Dead_Sn_Id)
										id.add(Long.valueOf(ids));
							}
						}

						os = null;
						is = null;
					}
				}
			}
		}

		return id;
	}

	@SuppressWarnings("unchecked")
	private boolean CopyFiles(int New_Sn_Id, Vector id) throws IOException 
	{
		PersistConn pc = null;
		DataOutputStream os = null;
		DataInputStream is = null;
		pc = (PersistConn) skSN.get(Integer.valueOf(New_Sn_Id));
		if (pc == null)
			return false;
		os = pc.getOutputStream();
		if (os == null)
			throw new IOException();
		is = pc.getInputStream();
		if (is == null)
			throw new IOException();
		for (int k = 0; k < id.size(); k++) 
		{
			String sendBuf = Serialization.serialize(Protocol.PROT_MAGIC,Protocol.PROT_SEND_FILE_ID,id.get(k));
			os.writeUTF(sendBuf);
			os.flush();
			if (transFile(((Long) id.get(k)).longValue(), os))
				System.out.println((new StringBuilder("File ID=")).append(id.get(k)).append(" , \u8F6C\u79FB\u6210\u529F").toString());
			
			String recvBuf = is.readUTF();
			int type = Integer.parseInt(recvBuf);
			if(type != Protocol.PROT_SN_SEND)
				throw new IOException();
		}
		return true;
	}

	private boolean deleteFile(long id) throws IOException
	{
		PersistConn pc = null;
		DataOutputStream os = null;
		DataInputStream is = null;
		boolean succ = false;
		FileId fid = new FileId(id);
		byte SnIDs[] = fid.getSnId();
		if (SnIDs[0] == -1 && SnIDs[1] == -1)
			return succ;
		for (int i = 0; i < SnIDs.length; i++) 
		{
			for (int j = i + 1; j < SnIDs.length; j++)
				if (SnIDs[i] == SnIDs[j])
					SnIDs[j] = -1;

		}

		for (int i = 0; i < SnIDs.length; i++)
			if (SnIDs[i] != -1) {
				pc = (PersistConn) skSN.get(Integer.valueOf(SnIDs[i]));
				if (pc != null) {
					os = pc.getOutputStream();
					if (os != null) {
						is = pc.getInputStream();
						if (is == null) {
							os = null;
						}
						else 
						{
							String sendBuf = Serialization.serialize(Protocol.PROT_MAGIC,Protocol.PROT_FILE_DELETE,id);
							os.writeUTF(sendBuf);
							os.flush();
							
							String recvBuf = is.readUTF();
							String[] splitBuf = Serialization.unserialize(recvBuf);	
							int magic = Integer.parseInt(splitBuf[0]);
							if (magic != Protocol.PROT_MAGIC)
								throw new IOException();
							int type = Integer.parseInt(splitBuf[1]);
							if (type != Protocol.PROT_FILE_DELETE_OK) {
								System.out.println((new StringBuilder("File ID=")).append(id).append(" ,Snid=").append(SnIDs[i]).append(" Delete failed").toString());
								throw new IOException();
							}
							succ = true;
						}
					}
				}
			}

		return succ;
	}

	private void DeleteMsg(String Msg) throws IOException 
	{
		String sendBuf = Serialization.serialize(Protocol.PROT_MAGIC,Protocol.PROT_DELETE_FILE,Msg);
		oos.writeUTF(sendBuf);
		oos.flush();
		
		String DeleteMsg = ois.readUTF();
		String subString[] = DeleteMsg.split("#");
		if (subString == null || subString.length <= 0)
			return;
		for (int i = 0; i < subString.length; i++) {
			String fileInfo[] = subString[i].split("%");
			System.out.println((new StringBuilder(String.valueOf(fileInfo[1]))).append(" Delete Successed").toString());
		}

	}

	private int getListNumber(int SnId) {
		int i = -1;
		for (i = 0; i < listSN.size(); i++)
			if (((NodeDesc) listSN.get(i)).get_id() == SnId)
				break;
		return i;
	}

	public boolean AddFileMsg(String fileMsg) throws IOException 
	{
		String sendBuf = Serialization.serialize(Protocol.PROT_MAGIC,Protocol.PROT_ADD_FILE,fileMsg);
		oos.writeUTF(sendBuf);
		oos.flush();
		
		String existName = ois.readUTF();
		if (existName != null && !existName.equalsIgnoreCase("")) 
		{
			String existfiles[] = existName.split("%");
			for (int i = 0; i < existfiles.length; i++)
				System.out.println((new StringBuilder(String.valueOf(existfiles[i]))).append(",  Exist, Add Failed").toString());
			return false;
		}
		return true;
	}

	public boolean addFile(long id, File file, long fileLen,String destDirectory) throws IOException, InterruptedException
	{
		FileId fi = new FileId(id);
		byte snId[] = fi.getSnId();
		for (int i = 0; i < snId.length; i++) {
			for (int j = i + 1; j < snId.length; j++)
				if (snId[j] == snId[i])
					snId[j] = -1;

		}

		synchronized (insertCache) {
			for (int i = 0; i < snId.length; i++)
				if (snId[i] != -1) {
					FSSendList list = (FSSendList) insertCache.get(Integer
							.valueOf(snId[i]));
					if (list == null) {
						Log.logger.error("List is null");
					} else {
						FSFile fsFile = new FSFile(id, snId[i], file, fileLen,destDirectory);
						list.insert(fsFile);
					}
				}
		}
		return true;
	}

	public long addFileSingle(long id, InputStream is, long fileLen)throws IOException 
	{
		if (sendFileDirec(id, is, fileLen))
			return id;
		return -1L;
	}

	public boolean addBuffer(long id, byte buffer[], int bufflen)throws IOException 
	{
		FileId fid = new FileId(id);
		byte snid[] = fid.getSnId();
		boolean succ = true;
		if (!sendBuffer(snid, id, buffer, bufflen))
		{
			succ = false;
			Log.logger.error((new StringBuilder("Buffer ID=")).append(id).append(", Add failed").toString());
		}
		return succ;
	}

	public boolean getFile(long id, OutputStream os) throws IOException {
		DataInputStream fis = getFileStream(id);
		int len = 0;
		if (fis == null)
			return false;
		
		byte buffer[] = new byte[0x100000];
		for (Long fileLen = Long.valueOf(fis.readLong()); fileLen.longValue() > 0L
				&& (len = fis.read(buffer)) > 0; os.flush()) {
			fileLen = Long.valueOf(fileLen.longValue() - (long) len);
			os.write(buffer, 0, len);
		}
		os.close();
		fis = null;
		return true;
	}

	@SuppressWarnings("unchecked")
	public HashMap getFileList(String filePath) throws IOException 
	{
		HashMap files = new HashMap();
		String sendBuf = Serialization.serialize(Protocol.PROT_MAGIC,Protocol.PROT_GET_FILE,filePath);
		oos.writeUTF(sendBuf);
		oos.flush();
		
		String fileMsg = ois.readUTF();
		if (fileMsg.equalsIgnoreCase(""))
			return files;
		String subString[] = fileMsg.split("#");
		for (int i = 0; i < subString.length; i++) {
			String fileInfo[] = subString[i].split("%");
			if (fileInfo != null && fileInfo.length != 0)
				files.put(fileInfo[0], fileInfo[1]);
		}
		return files;
	}

	@SuppressWarnings("unchecked")
	public boolean isFileExist(String filename) throws IOException
	{
		HashMap files = getFileList(filename);
		return files.size() != 0;
	}

	public byte[] getBuffer(long id) throws IOException
	{
		int readlen = 0;
		int pos = 0;
		FileId fid = new FileId(id);
		int fileid = fid.getId();
		if (fileid % 2 == 0) {
			Log.logger.error("File is too Larger");
			return null;
		}
		DataInputStream fis = getFileStream(id);
		if (fis == null)
			return null;
		
		byte buffer[] = new byte[0x100000];
		byte buff[] = new byte[0xa00000];
		for (Long fileLen = Long.valueOf(fis.readLong()); fileLen.longValue() > 0L
				&& (readlen = fis.read(buffer)) > 0;) {
			fileLen = Long.valueOf(fileLen.longValue() - (long) readlen);
			System.arraycopy(buffer, 0, buff, pos, readlen);
			pos += readlen;
		}

		byte buffs[] = new byte[pos];
		System.arraycopy(buff, 0, buffs, 0, pos);
		if (buffs != null)
			return buffs;
		return null;
	}

	@SuppressWarnings("unchecked")
	public boolean DeleteFile(String filePath) throws IOException
	{
		String deleteMsg = "";
		HashMap files = getFileList(filePath);
		if (files == null || files.size() == 0)
			return false;
		for (Iterator it = files.entrySet().iterator(); it.hasNext();) {
			java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
			if (deleteFile(Long.valueOf((String) entry.getKey()).longValue()))
				deleteMsg = (new StringBuilder(String.valueOf(deleteMsg))).append((String) entry.getKey()).append("%").append((String) entry.getValue()).append("#").toString();
		}

		if (deleteMsg != "") {
			DeleteMsg(deleteMsg);
			return true;
		} 
		return false;
	}

	@SuppressWarnings("unchecked")
	public boolean DataMove(int Dead_Sn_Id, int New_Sn_Id) throws IOException {
		int sn_id = -1;
		Vector snID = new Vector();
		Vector id = null;
		if (getListNumber(New_Sn_Id) == -1 || getListNumber(Dead_Sn_Id) == -1)
			return false;
		NodeDesc NewSnd = (NodeDesc) listSN.get(getListNumber(New_Sn_Id));
		NodeDesc DeadSnd = (NodeDesc) listSN.get(getListNumber(Dead_Sn_Id));
		if (NewSnd == null || DeadSnd == null) {
			Log.logger.error("NewSnd is null");
			return false;
		}
		String msg = (new StringBuilder(String.valueOf(String.valueOf(Dead_Sn_Id))))
		         .append("/").append(String.valueOf(New_Sn_Id)).append("/").append(NewSnd.get_ip())
				 .append("/").append(String.valueOf(NewSnd.get_port()))
				 .toString();
		
		String sendBuf = Serialization.serialize(Protocol.PROT_MAGIC,Protocol.PROT_UPDATE_CONF,msg,Dead_Sn_Id,New_Sn_Id);
		oos.writeUTF(sendBuf);
		oos.flush();
		int i;
		for (i = 0; i < listSN.size(); i++) {
			NodeDesc snd = (NodeDesc) listSN.get(i);
			sn_id = (new Integer(snd.get_id())).intValue();
			if (sn_id == Dead_Sn_Id && snd.get_isActive())
				break;
			if (snd.get_isActive())
				snID.add(Integer.valueOf(sn_id));
		}

		if (i == listSN.size()) {
			id = GetFileId(New_Sn_Id, Dead_Sn_Id, snID);
			if (id == null || id.size() == 0)
				return false;
			if (CopyFiles(New_Sn_Id, id)) 
			{
				UpdateSkSN_ListSN(Dead_Sn_Id, New_Sn_Id);
				return true;
			} 
		} 
		return false;
	}

	public double getCompressSize() {
		return compressSize;
	}

	public void setCompressSize(double compressSize) {
		this.compressSize = compressSize;
	}

	private static FS_Function fs_fsi = null;
	@SuppressWarnings("unchecked")
	private HashMap skSN;
	private HashMap insertCache;
	private int INSERT_THREAD_NUMBER;
	private String ipMS;
	@SuppressWarnings("unchecked")
	private List listSN;
	private DataOutputStream oos;
	private DataInputStream ois;
	private double compressSize;
}
