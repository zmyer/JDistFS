/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name: StartIdMgr.java
 *  
 *  Copyright 2013
 *  
 */  

package cn.zmyer.dst.ms;

import java.io.*;

public class StartIdMgr
{

    public StartIdMgr()
    {
    }

    public static int getStartId(short snid, short now) throws IOException
    {
        return getStartId((new Short(snid)).toString(), now);
    }

    public static void setStartIdWithDays(String snid, int startid, short days)
    {
        PrintWriter pw;
        try
        {
            pw = new PrintWriter(new FileWriter(new File((new StringBuilder("conf/sn_start_")).append(snid).append(".conf").toString())));
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return;
        }
        pw.println(startid);
        pw.println(days);
        pw.flush();
        pw.close();
    }

    public static int getStartId(String snid, short now) throws IOException
    {
        String start;
        BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(new File((new StringBuilder("conf/sn_start_")).append(snid).append(".conf").toString())));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
        try {
			start = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
        String old_days;
		try {
			old_days = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
        br.close();
        if(now == Short.parseShort(old_days))
        	return Integer.valueOf(start).intValue();
        setStartIdWithDays(snid, 0, now);
        return 0;
    }

    public static short getDays(String snid)
    {
        short old = -1;
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(new File((new StringBuilder("conf/sn_start_")).append(snid).append(".conf").toString())));
            br.readLine();
            String old_days = br.readLine();
            br.close();
            old = Short.parseShort(old_days);
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return old;
    }
}
