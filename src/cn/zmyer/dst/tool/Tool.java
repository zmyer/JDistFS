/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name:Tool.java
 *  
 *  Copyright 2013
 *  
 */
 
package cn.zmyer.dst.tool;

import cn.zmyer.dst.fsi.FS_Function;
import cn.zmyer.dst.utlis.Log;
import java.io.IOException;

public class Tool
{

    public Tool()
    {
    }

    public void CopyFile(int deadid, int newid)
        throws IOException
    {
        FS_Function fs_fsi = FS_Function.getInstance();
        if(deadid == -1 || newid == -1 || !fs_fsi.DataMove(deadid, newid))
            Log.logger.info("Move failed");
        else
            Log.logger.info("Move successed");
    }
}
