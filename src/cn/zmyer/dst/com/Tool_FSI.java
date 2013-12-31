/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name: Tool_FSI.java

 *  
 *  Copyright 2013
 *  
 */
  
package cn.zmyer.dst.com;

import cn.zmyer.dst.tool.Tool;
import java.io.*;

public class Tool_FSI
{

    public Tool_FSI()
    {
    }

    public static void main(String args[])
    {
        Tool tool;
        BufferedReader input;
        System.out.println("DFS TOOL START ...");
        System.out.println("Usage: <CopyFile> <DeadSnId> <NewSnId>");
        System.out.println("Usage: <Quit/quit>");
        tool = new Tool();
        InputStreamReader is = new InputStreamReader(System.in);
        input = new BufferedReader(is);
       
        String command;
        String comms[];
        for(;;)
        {
        System.out.print(">:");
        command = null;
        try
        {
            command = input.readLine();
        }
        catch(IOException e1)
        {
            break;
        }
        if(command == null || command.equalsIgnoreCase("quit"))
            break; /* Loop/switch isn't completed */
        comms = command.split(" ");
        if(comms.length == 0)
            continue; /* Loop/switch isn't completed */
        if(!command.equalsIgnoreCase("copyfile") || comms.length > 2)
            try
            {
                int DeadSnId = Integer.valueOf(comms[1]).intValue();
                int NewSnId = Integer.valueOf(comms[2]).intValue();
                tool.CopyFile(DeadSnId, NewSnId);
            }
            catch(Exception exception) 
            { 
            	break;
            }
        }
        System.out.println("DFS TOOL END ...");
        System.exit(1);
        return;
    }
}
