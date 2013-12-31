/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name: TimeS2L.java
 *  
 *  Copyright 2013
 *  
 */
 
package cn.zmyer.dst.utlis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TimeS2L
{

    private TimeS2L()
    {
    }

    public static Date timeS2l(String timeStr)
    {
        int i;
        if(timeStr == null)
            return null;
        i = 0;

	    for(;i<timeFormat.length;)
	    {
        Date dateLong;
		try {
			dateLong = ((SimpleDateFormat)dateFormatList.get(i)).parse(timeStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
        if(dateLong != null)
            return dateLong;
	    }
        return null;
    }

    @SuppressWarnings("unchecked")
	public static void initList()
    {
        Locale locale = Locale.US;
        dateFormatList = new ArrayList();
        for(int i = 0; i < timeFormat.length; i++)
        {
            SimpleDateFormat format = new SimpleDateFormat(timeFormat[i], locale);
            dateFormatList.add(format);
        }

    }

    private static final String timeFormat[] = {
        "EEE, d MMM yyyy HH:mm:ss Z", "yyyy-MM-dd HH:mm", "EEE, d MMM yyyy HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ssz", "dd MMM yyyy HH:mm:ss Z"
    };
    @SuppressWarnings("unchecked")
	private static List dateFormatList = null;

}
