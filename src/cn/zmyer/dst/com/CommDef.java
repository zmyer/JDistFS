/*
 * 
 *  Author:zhouwei,zhouwei@ztgame.com
 *  
 *  Source File Name: CommDef.java
 *  
 *  Copyright 2012
 */

package cn.zmyer.dst.com;


public class CommDef
{

    public CommDef()
    {
    }

    public static final int ID_FS_SN = 1;
    public static final int MAX_SN_COUNT = 255;
    public static final int MAX_FSI_COUNT = 255;
    public static final int MS_SN_PORT = 10001;
    public static final int MS_FSI_PORT = 10002;
    public static final int FSI_MS_PORT = 10003;
    public static final int SN_INIT_START_ID = 0;
    public static final int SN_ONE_TIME_IDS = 100;
    public static final long INVALID_FILE_ID = -1L;
    public static final byte INVALID_SN_ID = -1;
    public static final long HEART_BEAT_DELAY = 10000L;
    public static final int MS_SN_INC = 0;
    public static final int MS_SN_DEC = 1;
    public static final String CONF_MS_SN = "conf/ms_sn.conf";
    public static final String CONF_MS_IDMAP = "conf/ms_idmap.conf";
}
