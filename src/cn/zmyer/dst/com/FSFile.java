// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   FSFile.java

package cn.zmyer.dst.com;

import java.io.File;
import java.io.Serializable;

public class FSFile
    implements Serializable
{

    public FSFile(long fileId, int snid, File fsFile, long fileSize, String destDirectory)
    {
        this.fileId = fileId;
        this.fsFile = fsFile;
        this.fileSize = fileSize;
        this.snid = snid;
        this.destDirectory = destDirectory;
    }

    public long getFileId()
    {
        return fileId;
    }

    public File getFile()
    {
        return fsFile;
    }

    public long getFileSize()
    {
        return fileSize;
    }

    public int getSnid()
    {
        return snid;
    }

    public String getDestDirectory()
    {
        return destDirectory;
    }

    private static final long serialVersionUID = 0x6339c0df167fe7f0L;
    private long fileId;
    private File fsFile;
    private long fileSize;
    private int snid;
    private String destDirectory;
}
