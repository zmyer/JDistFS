/*
 * 
 *  Author:zhouwei,zhouwei@ztgame.com
 *  
 *  Source File Name: FsCache.java
 *  
 */

package cn.zmyer.dst.com;

public class FsCache
{

    public FsCache(String digest, long fileId)
    {
        _digest = null;
        _fileId = -1L;
        _digest = digest;
        _fileId = fileId;
    }

    public long getFileId()
    {
        return _fileId;
    }

    public boolean isSameFile(String digest)
    {
        if(_digest == null && digest == null)
            return true;
        if(_digest == null || digest == null)
            return false;
        else
            return _digest.equals(digest);
    }

    String _digest;
    long _fileId;
}
