/*
 * 
 *  Author:zhouwei,zhouwei@ztgame.com
 *  
 *  Source File Name:FSI.java
 *  
 *  Copyright 2012
 *  
 */

package cn.zmyer.dst.com;

import cn.zmyer.dst.fsi.FS_Function;
import cn.zmyer.dst.utlis.Log;
import java.io.*;
import java.util.*;

public class FSI
{

    public FSI()
    {
        FileMsg = "";
    }

    private long addFile(InputStream is, long fileLen) throws IOException, InterruptedException
    {
        boolean isLargeFile = false;
        if(fileLen > 0xa00000L)
            isLargeFile = true;
        FS_Function fs_fsi = FS_Function.getInstance();
        Long id = Long.valueOf(fs_fsi.genFileId(isLargeFile));
        if(id.longValue() != -1L && fs_fsi.addFileSingle(id.longValue(), is, fileLen) != -1L)
        {
            System.out.println((new StringBuilder("File ID= ")).append(id).toString());
            return id.longValue();
        } else
        {
            Log.logger.error("File ID null");
            return -1L;
        }
    }

    private String changePathToUpperCase(String Path)
    {
        String tmpPath = "";
        boolean endSpash = false;
        if(Path.endsWith("/"))
            endSpash = true;
        String splitPath[] = Path.split("/");
        if(splitPath[0] != "")
            splitPath[0] = (new StringBuilder(String.valueOf(splitPath[0].toUpperCase()))).append("/").toString();
        else
            splitPath[0] = "/";
        tmpPath = splitPath[0];
        for(int i = 1; i < splitPath.length - 1; i++)
            tmpPath = (new StringBuilder(String.valueOf(tmpPath))).append(splitPath[i]).append("/").toString();

        if(splitPath.length > 1)
            if(endSpash)
                tmpPath = (new StringBuilder(String.valueOf(tmpPath))).append("/").toString();
            else
                tmpPath = (new StringBuilder(String.valueOf(tmpPath))).append(splitPath[splitPath.length - 1]).toString();
        return tmpPath;
    }

    private void addDirectory(String SourceDirectory, String destDirectory)throws IOException, InterruptedException
    {
        String destPath;
        File file;
        String absolutePath = null;
        String tmpPath = "";
        String filePath[] = destDirectory.split("/");
        if(filePath[0] != "")
            filePath[0] = (new StringBuilder(String.valueOf(filePath[0].toUpperCase()))).append("/").toString();
        else
            filePath[0] = "/";
        tmpPath = filePath[0];
        for(int i = 1; i < filePath.length; i++)
            tmpPath = (new StringBuilder(String.valueOf(tmpPath))).append(filePath[i]).append("/").toString();

        destDirectory = tmpPath;
        destPath = destDirectory;
        file = new File(SourceDirectory);
    //    if(file.isDirectory())
     //       break MISSING_BLOCK_LABEL_306;
        absolutePath = file.getPath();
        String subString[] = absolutePath.split("\\\\");
        if(subString == null || subString.length <= 0)
            return;
        destPath = (new StringBuilder(String.valueOf(destPath))).append(subString[1]).toString();
        for(int i = 2; i < subString.length; i++)
            destPath = (new StringBuilder(String.valueOf(destPath))).append("/").append(subString[i]).toString();

    //    if((id = AddFile(absolutePath, destDirectory, false)) == -1L)
    //        break MISSING_BLOCK_LABEL_362;
   //     this;
   //     FileMsg;
   //     JVM INSTR new #77  <Class StringBuilder>;
   //     JVM INSTR dup_x1 ;
  //      JVM INSTR swap ;
    //    String.valueOf();
   //     StringBuilder();
   //     String.valueOf(id);
   ////     append();
   //     "%";
  //      append();
    //    destPath;
   //     append();
   //     "#";
   //     append();
   //     toString();
  //     FileMsg;
  //      break MISSING_BLOCK_LABEL_362;
        String fileList[] = file.list();
        for(int i = 0; i < fileList.length; i++)
            addDirectory((new StringBuilder(String.valueOf(SourceDirectory))).append("/").append(fileList[i]).toString(), destDirectory);
    }

    private void getfile(long id, OutputStream os)throws IOException
    {
        FS_Function fs_fsi = FS_Function.getInstance();
        if(id != -1L && fs_fsi.getFile(id, os))
            System.out.println((new StringBuilder("File ID:")).append(id).append("Found").toString());
        else
            System.out.println((new StringBuilder("File ID:")).append(id).append("not exist").toString());
    }

    private void getFile(long id, String absoluteSavePath)throws IOException
    {
        File fs = new File(absoluteSavePath);
        if(fs.exists())
        {
        	getfile(id,new FileOutputStream(fs));
        	return;
        }
        else
        	throw new AssertionError();
    }

    public long AddFile(String filename, String destDirectory, boolean isCache)throws IOException, InterruptedException
    {
        FS_Function fs_fsi = FS_Function.getInstance();
        File fs = new File(filename);
        if(fs == null || !fs.exists())
            return -1L;
        long fileLen = fs.length();
        String fileinfo[] = filename.split("\\\\");
        if(fileinfo == null || fileinfo.length <= 1)
        {
            fileinfo = filename.split("/");
            if(fileinfo == null || fileinfo.length <= 0)
                return -1L;
        }
        destDirectory = (new StringBuilder(String.valueOf(destDirectory))).append(fileinfo[1]).toString();
        for(int i = 2; i < fileinfo.length; i++)
            destDirectory = (new StringBuilder(String.valueOf(destDirectory))).append("/").append(fileinfo[i]).toString();

        if(fs_fsi.isFileExist(destDirectory))
        {
            System.out.println((new StringBuilder(String.valueOf(destDirectory))).append("  Exist, Add Failed").toString());
            return -1L;
        }
        if(isCache)
        {
            boolean isLargeFile = false;
            String fileInfo = "";
            String absolutePath = destDirectory;
            if(fileLen > 0xa00000L)
                isLargeFile = true;
            Long id = Long.valueOf(fs_fsi.genFileId(isLargeFile));
            if(id.longValue() != -1L && fs_fsi.addFile(id.longValue(), fs, fileLen, destDirectory))
            {
                fileInfo = (new StringBuilder(String.valueOf(fileInfo))).append(String.valueOf(id)).append("%").append(absolutePath).append("#").toString();
                fs_fsi.AddFileMsg(fileInfo);
                System.out.println((new StringBuilder("File ID: ")).append(id).toString());
                return id.longValue();
            } 
            else
            {
                Log.logger.error("Filed ID NULL");
                return -1L;
            }
        } 
        else
        {
            return addFile(new FileInputStream(fs), fileLen);
        }
    }

    public void AddDirectory(String SourceDirectory, String destDirectory)throws IOException, InterruptedException
    {
        FileMsg = "";
        FS_Function fs_fsi = FS_Function.getInstance();
        addDirectory(SourceDirectory, destDirectory);
        fs_fsi.AddFileMsg(FileMsg);
    }

    public long AddBuffer(byte buffer[], int bufflen)throws IOException
    {
        boolean isLargeFile = false;
        if(bufflen > 0xa00000)
        {
            Log.logger.info("Buffer len is too larger,please select file to send");
            return -1L;
        }
        FS_Function fs_fsi = FS_Function.getInstance();
        Long id = Long.valueOf(fs_fsi.genFileId(isLargeFile));
        if(id.longValue() != -1L && fs_fsi.addBuffer(id.longValue(), buffer, bufflen))
        {
            Log.logger.info((new StringBuilder("Buffer ID= ")).append(id).toString());
            return id.longValue();
        } else
        {
            Log.logger.error("Buffer ID null");
            return -1L;
        }
    }

    @SuppressWarnings("unchecked")
	public HashMap getFileList(String filePath)
        throws IOException
    {
        FS_Function fs_fsi = FS_Function.getInstance();
        if(!filePath.equalsIgnoreCase(""))
            filePath = changePathToUpperCase(filePath);
        HashMap files = fs_fsi.getFileList(filePath);
        return files;
    }

    @SuppressWarnings("unchecked")
	public void GetFileList(String filePath)
        throws IOException
    {
        HashMap files = getFileList(filePath);
        if(files.size() == 0)
        {
            Log.logger.info((new StringBuilder(String.valueOf(filePath))).append(" is empty").toString());
        } else
        {
            java.util.Map.Entry entry;
            for(Iterator it = files.entrySet().iterator(); it.hasNext(); System.out.println((String)entry.getValue()))
                entry = (java.util.Map.Entry)it.next();

        }
    }

    @SuppressWarnings("unchecked")
	public Vector getRootDirectory()
        throws IOException
    {
        Vector root = new Vector();
        HashMap files = getFileList("");
        if(files.size() == 0)
        {
            Log.logger.info("The Whole FileSystem is empty");
        } else
        {
            for(Iterator it = files.entrySet().iterator(); it.hasNext();)
            {
                java.util.Map.Entry entry = (java.util.Map.Entry)it.next();
                String subString[] = ((String)entry.getValue()).split("/");
                if(!root.contains(subString[0]))
                    root.add(subString[0]);
            }
        }
        return root;
    }

    @SuppressWarnings("unchecked")
	public void GetRootDirectory()throws IOException
    {
        Vector root = getRootDirectory();
        for(int i = 0; i < root.size(); i++)
            System.out.println((String)root.get(i));
    }

    @SuppressWarnings("unchecked")
	public Vector find(String filename)throws IOException
    {
        Vector file = new Vector();
        HashMap files = getFileList("");
        if(files.size() > 0)
        {
        	for(Iterator it = files.entrySet().iterator();it.hasNext();)
        	{
                java.util.Map.Entry entry = (java.util.Map.Entry)it.next();
                if(((String)entry.getValue()).contains(filename))
                    file.add((String)entry.getValue());
        	}
        }
        else
        {
            Log.logger.info("The Whole FileSystem is empty");
        } 
        return file;
    }

    @SuppressWarnings("unchecked")
	public void Find(String filename)
        throws IOException
    {
        Vector file = find(filename);
        for(int i = 0; i < file.size(); i++)
            System.out.println((String)file.get(i));

    }

    @SuppressWarnings("unchecked")
	public void GetFile(String filePath, String absoluteSavePath)
        throws IOException
    {
        String absoPath = "";
        java.util.Map.Entry entry = null;
        FS_Function fs_fsi = FS_Function.getInstance();
        String Path = "";
        boolean isDirectory = false;
        if(absoluteSavePath.endsWith("/"))
            isDirectory = true;
        String savePath[] = absoluteSavePath.split("/");
        int length = savePath.length;
        int temp;
        if(!isDirectory)
            temp = length - 1;
        else
            temp = length;
        for(int i = 0; i < temp; i++)
            Path = (new StringBuilder(String.valueOf(Path))).append(savePath[i]).append("/").toString();

        File fs = new File(Path);
        fs.mkdirs();
        filePath = changePathToUpperCase(filePath);
        HashMap files = fs_fsi.getFileList(filePath);
        if(files.size() != 0)
        {
            Iterator it = files.entrySet().iterator();
            if(isDirectory)
            {
                Path = absoluteSavePath;
                while(it.hasNext()) 
                {
                    entry = (java.util.Map.Entry)it.next();
                    String subString[] = ((String)entry.getValue()).split("/");
                    if(subString.length > 0)
                    {
                        if(subString.length > 2)
                        {
                            absoluteSavePath = (new StringBuilder(String.valueOf(absoluteSavePath))).append(subString[1]).append("/").toString();
                            for(int i = 2; i < subString.length - 1; i++)
                                absoluteSavePath = (new StringBuilder(String.valueOf(absoluteSavePath))).append(subString[i]).append("/").toString();

                        }
                        File tempfp = new File(absoluteSavePath);
                        tempfp.mkdirs();
                        absoPath = (new StringBuilder(String.valueOf(absoluteSavePath))).append(subString[subString.length - 1]).toString();
                        getFile(Long.valueOf((String)entry.getKey()).longValue(), absoPath);
                        absoluteSavePath = Path;
                    }
                }
            } else
            {
                entry = (java.util.Map.Entry)it.next();
                String subString[] = ((String)entry.getValue()).split("/");
                if(subString.length <= 0)
                    return;
                getFile(Long.valueOf((String)entry.getKey()).longValue(), absoluteSavePath);
            }
        }
    }

    public byte[] GetBuffer(Long id)
        throws IOException
    {
        FS_Function fs_fsi = FS_Function.getInstance();
        byte buffer[] = (byte[])null;
        if(id.longValue() != -1L && (buffer = fs_fsi.getBuffer(id.longValue())) != null)
        {
            System.out.println((new StringBuilder("File ID:")).append(id).append("Found").toString());
            return buffer;
        } 
        else
        {
            System.out.println((new StringBuilder("File ID:")).append(id).append("not exist").toString());
            return null;
        }
    }

    public boolean Delete(String filePath)throws IOException
    {
        FS_Function fs_fsi = FS_Function.getInstance();
        filePath = changePathToUpperCase(filePath);
        if(fs_fsi.DeleteFile(filePath))
        {
            System.out.println((new StringBuilder("File Name=")).append(filePath).append(", Delete Success").toString());
            return true;
        } 
        else
        {
            System.out.println((new StringBuilder("File Name=")).append(filePath).append(", Delete Fail").toString());
            return false;
        }
    }

    private String FileMsg;
}
