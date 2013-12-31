/*
 * 
 *  Author:zhouwei,zhouwei@ztgame.com
 *  
 *  Source File Name: Serialization.java
 *  
 *  Copyright 2012
 *  
 */


package cn.zmyer.dst.com;

public class Serialization {
	public Serialization()
	{
	}
	public static String serialize(Object...argms)
	{
		String buffer="";
		for(Object obj : argms)
		{
		     buffer += obj.toString()+"&";
		}
		return buffer;
	}
	public static String[] unserialize(String str)
	{
		String[] context = str.split("&");
		return context;
		
	}
}
