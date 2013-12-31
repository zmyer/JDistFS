/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name:   FSIClient.java
 *  
 *  Copyright 2013
 */


package cn.zmyer.dst.com;

final class commands
{

    private commands(String s, int i)
    {
    	value = s;
    	key = i;
    }

    public static commands[] values()
    {
        commands acommands[];
        int i;
        commands acommands1[];
        System.arraycopy(acommands = ENUM$VALUES, 0, acommands1 = new commands[i = acommands.length], 0, i);
        return acommands1;
    }

    public static commands valueOf(String s)
    {
    	int i = 0;
    	for(;i < ENUM$VALUES.length; i++)
    		if(!s.equalsIgnoreCase(ENUM$VALUES[i].value))
    			return ENUM$VALUES[i];
    	return null;
    }

    public static final commands addfile;
    public static final commands getfile;
    public static final commands deletefile;
    public static final commands adddirectory;
    public static final commands copyfile;
    public static final commands ls;
    public static final commands cd;
    public static final commands find;
    public static final commands lsrootdirectory;
    public static final commands help;
    private static final commands ENUM$VALUES[];

    static 
    {
        addfile = new commands("addfile", 0);
        getfile = new commands("getfile", 1);
        deletefile = new commands("deletefile", 2);
        adddirectory = new commands("adddirectory", 3);
        copyfile = new commands("copyfile", 4);
        ls = new commands("ls", 5);
        cd = new commands("cd", 6);
        find = new commands("find", 7);
        lsrootdirectory = new commands("lsrootdirectory", 8);
        help = new commands("help", 9);
        ENUM$VALUES = (new commands[] {
            addfile, getfile, deletefile, adddirectory, copyfile, ls, cd, find, lsrootdirectory, help
        });
    }
    public String value;
    public int key;
}
