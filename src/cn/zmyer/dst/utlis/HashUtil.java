/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name:HashUtil.java
 *  
 *  Copyright 2013
 *  
 */  

package cn.zmyer.dst.utlis;

import java.security.NoSuchAlgorithmException;
import jonelo.jacksum.JacksumAPI;
import jonelo.jacksum.algorithm.AbstractChecksum;
import sun.misc.BASE64Encoder;

public class HashUtil
{

    private HashUtil()
    {
        resetAlgo(_algo);
    }

    public static HashUtil getInstance()
    {
        if(_hashUtil == null)
            _hashUtil = new HashUtil();
        return _hashUtil;
    }

    public static void resetAlgo(String algo)
    {
        try
        {
            _algo = algo;
            _checksum = JacksumAPI.getChecksumInstance(_algo);
            _hashUtil = null;
        }
        catch(NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
    }

    public String hash(byte value[])
    {
        if(_checksum == null)
        {
            return null;
        } else
        {
            _checksum.update(value);
            _checksum.setEncoding("hex");
            byte hashValue[] = _checksum.getByteArray();
            _checksum.reset();
            return encoder.encode(hashValue);
        }
    }

    private static String _algo = "crc64";
    private static AbstractChecksum _checksum = null;
    private static HashUtil _hashUtil = null;
    private static BASE64Encoder encoder = new BASE64Encoder();

}
