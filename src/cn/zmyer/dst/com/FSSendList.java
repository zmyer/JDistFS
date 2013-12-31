/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name: FSSendList.java
 *  
 *  Copyright 2013
 *  
 */

package cn.zmyer.dst.com;

import java.util.ArrayList;

public class FSSendList
{

    @SuppressWarnings("unchecked")
	public FSSendList()
    {
        fileList = new ArrayList();
    }

    @SuppressWarnings("unchecked")
	public void insert(FSFile file)
    {
        synchronized(this)
        {
            fileList.add(file);
        }
    }

    @SuppressWarnings("unchecked")
	public void addList(ArrayList list)
    {
        synchronized(this)
        {
            fileList.addAll(list);
        }
    }

    public int size()
    {
        return fileList.size();
    }

    @SuppressWarnings("unchecked")
	public void moveList(FSSendList to)
    {
        synchronized(this)
        {
            to.fileList = fileList;
            fileList = new ArrayList();
        }
    }

    @SuppressWarnings("unchecked")
	public ArrayList getFileList()
    {
        return fileList;
    }

    @SuppressWarnings("unchecked")
	private ArrayList fileList;
}
