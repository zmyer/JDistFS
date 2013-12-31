/*
 * 
 *  Author:zhouwei,zhouwei198732@126.com
 *  
 *  Source File Name:NodeDesc.java
 *  
 *  Copyright 2013
 *  
 */ 

package cn.zmyer.dst.ms;
import java.io.Serializable;

public class NodeDesc implements Serializable
{

    public byte get_id()
    {
        return _id;
    }

    public void set_id(byte _id)
    {
        this._id = _id;
    }

    public String get_ip()
    {
        return _ip;
    }

    public void set_ip(String _ip)
    {
        this._ip = _ip;
    }

    public short get_port()
    {
        return _port;
    }

    public void set_port(short _port)
    {
        this._port = _port;
    }

    public NodeDesc(short _type, byte _id, String _ip, short _port)
    {
        this._type = _type;
        this._id = _id;
        this._ip = _ip;
        this._port = _port;
        _start = 0;
        _isActive = false;
    }

    public NodeDesc(String _type, String _id, String _ip, String _port)
    {
        this._type = Short.valueOf(_type).shortValue();
        this._id = Byte.valueOf(_id).byteValue();
        this._ip = _ip;
        this._port = Short.valueOf(_port).shortValue();
        _start = 0;
        _isActive = false;
    }

    public int get_start()
    {
        return _start;
    }

    public void set_start(int _start)
    {
        this._start = _start;
    }

    public short get_type()
    {
        return _type;
    }

    public void set_type(short _type)
    {
        this._type = _type;
    }

    public boolean get_isActive()
    {
        return _isActive;
    }

    public void set_isActive(boolean active)
    {
        _isActive = active;
    }

    private static final long serialVersionUID = 0x825f12ac0cb43f8fL;
    private short _type;
    private byte _id;
    private String _ip;
    private short _port;
    private int _start;
    private boolean _isActive;
}
