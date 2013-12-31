/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name: MasterSN.java
 *  
 *  Copyright 2013
 *  
 */    

package cn.zmyer.dst.ms;

import cn.zmyer.dst.utlis.Log;
import java.io.*;
import java.net.ServerSocket;
import java.util.*;

@SuppressWarnings("unchecked")
public class MasterSN
{
    public MasterSN()
    {
    }
    private static boolean initMSSN()
    {
        listSN = Collections.synchronizedList(new ArrayList(255));
        File conf = new File("conf/ms_sn.conf");
        if(!conf.exists())
        {
            try
            {
                conf.createNewFile();
            }
            catch(IOException e)
            {
                Log.logger.error((new StringBuilder("创建配置文件conf/ms_sn.conf失败:")).append(e.toString()).toString());
                return false;
            }
            return true;
        }
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(new File("conf/ms_sn.conf")));
            do
            {
                String confLine = br.readLine();
                if(confLine == null || confLine.equalsIgnoreCase(""))
                    break;
                String confs[] = confLine.split(":");
                NodeDesc snd = new NodeDesc(Integer.toString(1), confs[0], confs[1], confs[2]);
                listSN.add(snd);
            } while(true);
            br.close();
        }
        catch(FileNotFoundException e)
        {
            Log.logger.error((new StringBuilder("无法找到配置文件：conf/ms_sn.conf,")).append(e.toString()).toString());
            return false;
        }
        catch(IOException e)
        {
            Log.logger.error((new StringBuilder("读取配置文件：conf/ms_sn.conf失败,")).append(e.toString()).toString());
            return false;
        }
        File idmap = new File("conf/ms_idmap.conf");
        if(!idmap.exists())
        {
            try
            {
                idmap.createNewFile();
            }
            catch(IOException e)
            {
                Log.logger.error((new StringBuilder(" 创建IDMap配置文件conf/ms_idmap.conf失败,")).append(e.toString()).toString());
                return false;
            }
        }
        try
        {
            BufferedReader idmapreader = new BufferedReader(new FileReader(new File("conf/ms_idmap.conf")));
            do
            {
                String confLine = idmapreader.readLine();
                if(confLine == null || confLine.equalsIgnoreCase(""))
                           break;
                String confs[] = confLine.split("=");
                IDMap.put(Long.valueOf(confs[0]), confs[1]);
            } while(true);
            idmapreader.close();
        }
        catch(IOException e)
        {
            Log.logger.error((new StringBuilder("读IDMap配置文件：conf/ms_idmap.conf失败,")).append(e.toString()).toString());
            return false;
        }
        return true;
    }

    public static synchronized int getIds(NodeDesc snd, short now)
    {
        String snid = Short.toString(snd.get_id());
        short old_days = StartIdMgr.getDays(snid);
        int old_start = 0;
        if(old_days == now)
        {
            old_start = snd.get_start();
            snd.set_start(old_start + 100);
            StartIdMgr.setStartIdWithDays(Short.toString(snd.get_id()), snd.get_start(), now);
        } 
        else
        {
            StartIdMgr.setStartIdWithDays(snid, 0, now);
            snd.set_start(0);
        }
        return old_start;
    }

    private static boolean serveSN()
    {
        try
        {
            ServerSocket skSNServer = new ServerSocket(10001);
            Log.logger.info("绑定SN服务端口：10001成功！");
            (new SNServerThread(skSNServer)).start();
        }
        catch(IOException e)
        {
            Log.logger.error((new StringBuilder("绑定SN服务端口：10001失败:")).append(e.toString()).toString());
            return false;
        }
        return true;
    }

    private static boolean serveFSI()
    {
        try
        {
            ServerSocket skFSIServer = new ServerSocket(10002);
            Log.logger.info("绑定FSI服务端口：10002成功！");
            (new FSIServerThread(skFSIServer)).start();
        }
        catch(IOException e)
        {
            Log.logger.error((new StringBuilder("绑定FSI服务端口：10002失败: ")).append(e.toString()).toString());
            return false;
        }
        return true;
    }

    private static void loopStat()
    {
        do
        {
            Log.logger.info("Master-SN 正在运行..");
            try
            {
                Thread.sleep(0x186a0L);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                System.out.println(e.toString());
            }
        } while(true);
    }

    public static NodeDesc getSN(String ip, Short port)
    {
        NodeDesc nd = null;
        for(int i = 0; i < listSN.size(); i++)
        {
            NodeDesc tmpNode = (NodeDesc)listSN.get(i);
            if(port.shortValue() != tmpNode.get_port() || ip.compareTo(tmpNode.get_ip()) != 0)
                continue;
            nd = tmpNode;
            break;
        }
        return nd;
    }

    public static synchronized byte getSNId(String ip, short port)
    {
        byte id = -1;
        int index = listSN.size() + 1;
        id = Byte.parseByte(Integer.toString(index));
        try
        {
            BufferedWriter bw = new BufferedWriter(new FileWriter("conf/ms_sn.conf", true));
            String content = (new StringBuilder(String.valueOf(Integer.toString(index)))).append(":").append(ip).append(":").append(Short.toString(port)).toString();
            bw.write(content);
            bw.newLine();
            bw.flush();
            bw.close();
        }
        catch(IOException e)
        {
            Log.logger.error((new StringBuilder("记录SN节点信息至conf/ms_sn.conf出错：")).append(e.toString()).toString());
        }
        return id;
    }

    private static int getListNumber(int SnId)
    {
        int i = -1;
        for(i = 0; i < listSN.size(); i++)
            if(((NodeDesc)listSN.get(i)).get_id() == SnId)
                break;
        return i;
    }

    public static synchronized boolean UpdateIDMapConf()throws IOException
    {
        String confMsg = "";
        Set entryset = IDMap.entrySet();
        for(Iterator it = entryset.iterator(); it.hasNext();)
        {
            java.util.Map.Entry entry = (java.util.Map.Entry)it.next();
            confMsg = (new StringBuilder(String.valueOf(confMsg))).append(String.valueOf(entry.getKey())).append("=").append((String)entry.getValue()).append("\n").toString();
        }

        File fs = new File("conf/ms_idmap.conf");
        FileOutputStream fos = new FileOutputStream(fs, false);
        if(fos != null)
        {
            fos.write("".getBytes());
            fos.write(confMsg.getBytes());
            fos.close();
            return true;
        }
        else
            return false;
    }

    public static synchronized boolean UpdateSNId(int deadid, int newid, String ip, short port)throws IOException
    {
        String confMsg = "";
        if(getListNumber(deadid) == -1)
            return false;
        NodeDesc ptr = (NodeDesc)listSN.get(getListNumber(deadid));
        if(ptr == null)
            return false;
        ptr.set_ip(ip);
        ptr.set_port(port);
        BufferedReader br = new BufferedReader(new FileReader(new File("conf/ms_sn.conf")));
        do
        {
            String confLine = br.readLine();
            if(confLine == null)
                break;
            if(!confLine.equalsIgnoreCase(""))
            {
                String confs[] = confLine.split(":");
                if(Integer.valueOf(confs[0]).intValue() == deadid)
                    confLine = (new StringBuilder(String.valueOf(Integer.toString(deadid)))).append(":").append(ip).append(":").append(Short.toString(port)).toString();
                else if(Integer.valueOf(confs[0]).intValue() == newid)
                    confLine = (new StringBuilder(String.valueOf(Integer.toString(newid)))).append(":0.0.0.0:0").toString();
                confMsg = (new StringBuilder(String.valueOf(confMsg))).append(confLine).append("\n").toString();
            }
        } while(true);
        br.close();
        File fs = new File("conf/ms_sn.conf");
        FileOutputStream fos = new FileOutputStream(fs, false);
        if(fos != null)
        {   fos.write("".getBytes());
            fos.write(confMsg.getBytes());
            fos.close();
            return true;   
        } 
        else
           return false;
    }

    public static void main(String args[]) throws IOException
    {
        Log.init("conf/log4j.properties", "log/ms-sn.log");
        if(!initMSSN())
            return;
        if(!serveSN())
            return;
        if(!serveFSI())
        {
            return;
        } 
        else
        {
            loopStat();
            return;
        }
    }

    static short sn_conf_count_fs = 0;
    static List listSN = null;
    static Map mapFSI = Collections.synchronizedMap(new HashMap(255));
    static int fsi_count = 0;
    static int currSNId = 0;
    static Map mapSnLI = Collections.synchronizedMap(new HashMap());
    static Map IDMap = Collections.synchronizedMap(new HashMap());

}
