/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name:  FileId.java
 *  
 *  Copyright 2013
 */

package cn.zmyer.dst.com;

import java.io.Serializable;

public class FileId implements Serializable
{

    public FileId(long fileId)
    {
        snId = new byte[2];
        this.fileId = fileId;
        if(fileId == -1L)
        {
            snId[0] = -1;
            snId[1] = -1;
            day = -1;
            id = -1;
        } 
        else
        {
            long temp = fileId;
            snId[0] = (new Long(temp & 255L)).byteValue();
            snId[1] = (new Long(temp >> 8 & 255L)).byteValue();
            day = (new Long(temp >> 48 & 65535L)).shortValue();
            id = (new Long(temp >> 16 & 65535L)).shortValue();
        }
    }

    public FileId(byte snId, byte snId2, short day, int id)
    {
        this.snId = new byte[2];
        this.snId[0] = snId;
        this.snId[1] = snId2;
        this.day = day;
        this.id = id;
        if(snId < 0 || snId2 < 0 || day < 0 || id < 0)
        {
            fileId = -1L;
        } else
        {
            long temp = 0L;
            temp = day;
            fileId = temp << 48;
            temp = id;
            fileId |= temp << 16;
            temp = snId2;
            fileId |= temp << 8;
            temp = snId;
            fileId |= temp;
        }
    }

    public short getDay()
    {
        return day;
    }

    public long getFileId()
    {
        return fileId;
    }

    public int getId()
    {
        return id;
    }

    public byte[] getSnId()
    {
        return snId;
    }

    private static final long serialVersionUID = 0x8b70c69edd0ce06bL;
    public static final int INVALID_FILE_ID = -1;
    byte snId[];
    short day;
    int id;
    long fileId;
}
