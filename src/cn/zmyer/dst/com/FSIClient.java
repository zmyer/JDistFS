/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name: FSIClient.java
 *  
 *  Copyright 2013
 *  
 */

package cn.zmyer.dst.com;
import java.io.*;

public class FSIClient extends Thread
{

    public FSIClient()
    {
    }

    public static void main(String args[])
    {
    	FSI fsi = new FSI();
        InputStreamReader is = new InputStreamReader(System.in);
        BufferedReader input = new BufferedReader(is);
        System.out.println("Usage: <commdand line>(AddFile/GetFile/DeleteFile)<savePath> <filename/fileId> .....");
        System.out.println("Usage: <AddFile> <SavePath> [<Filename1> <Filename2> <Filename3>...]");
        System.out.println("Usage: <AddDirectory> <SourceDirectory> <DestDirectory>");
        System.out.println("Usage: <GetFile> <DestPath>  [<SourcePath1> <SourcePath2> <SourcePath3>...]");
        System.out.println("Usage: <DeleteFile> [<filePath1> <filePath2> <filePath3>...]");
        System.out.println("Usage: <ls> <fileDirectory>");
        System.out.println("Usage: <lsRootDirectory>");
        System.out.println("Usage: <find> <fileName>");
        System.out.println("Usage: <Quit/quit>");
        System.out.println("Usage: <help>");
        do
        {
            System.out.print(">:");
            String command = null;
            try
            {
                command = input.readLine();
            }
            catch(IOException e1)
            {
                continue;
            }
            if(command == null || command.equalsIgnoreCase("quit"))
                break;
            String comms[] = command.split(" ");
            if(comms.length != 0)
                try
                {
                    switch(commands.valueOf(comms[0].toLowerCase()).key)
                    {

                    case 1: // '\001'
                        if(comms.length > 1)
                        {
                            for(int i = 2; i < comms.length; i++)
                                fsi.AddFile(comms[i], comms[1], true);

                        }
                        break;

                    case 2: // '\002'
                        if(comms.length > 2)
                        {
                            for(int i = 2; i < comms.length; i++)
                                fsi.GetFile(comms[i], comms[1]);

                        }
                        break;

                    case 3: // '\003'
                        if(comms.length > 1)
                        {
                            for(int i = 1; i < comms.length; i++)
                                fsi.Delete(comms[i]);

                        }
                        break;

                    case 4: // '\004'
                        if(comms.length >= 3)
                        {
                            for(int i = 1; i < comms.length - 1; i++)
                                fsi.AddDirectory(comms[i], comms[comms.length - 1]);

                        }
                        break;

                    case 9: // '\t'
                        if(comms.length >= 1)
                            fsi.GetRootDirectory();
                        break;

                    case 8: // '\b'
                        if(comms.length >= 2)
                        {
                            for(int i = 1; i < comms.length; i++)
                                fsi.Find(comms[i]);

                        }
                        break;

                    case 6: // '\006'
                        if(comms.length >= 2)
                        {
                            for(int i = 1; i < comms.length; i++)
                                fsi.GetFileList(comms[i]);

                        }
                        break;

                    case 10: // '\n'
                        System.out.println("Usage: <commdand line>(AddFile/GetFile/DeleteFile)<savePath> <filename/fileId> .....");
                        System.out.println("Usage: <AddFile> <SavePath> [<Filename1> <Filename2> <Filename3>...]");
                        System.out.println("Usage: <AddDirectory> <SourceDirectory> <DestDirectory>");
                        System.out.println("Usage: <GetFile> <DestPath>  [<SourcePath1> <SourcePath2> <SourcePath3>...]");
                        System.out.println("Usage: <DeleteFile> [<filePath1> <filePath2> <filePath3>...]");
                        System.out.println("Usage: <ls> <fileDirectory>");
                        System.out.println("Usage: <lsRootDirectory>");
                        System.out.println("Usage: <find> <fileName>");
                        System.out.println("Usage: <Quit/quit>");
                        System.out.println("Usage: <help>");
                        break;
                    }
                }
                catch(Exception exception) { }
        } while(true);
        System.out.println("DFS TEST END ...");
        System.exit(1);
    }

    public static int number = 0;
    public static boolean end = false;
}
