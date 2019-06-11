package com.ceti.clverrouille;

public class WifiDevice
{
    private String deviceName = "";
    private String apName = "";
    private String ssid = "";
    private String pass = "";
    private String host = "";
    private String port = "";
    private String currentIp = "";

    public WifiDevice()
    {

    }

    public String getDeviceName()
    {
        return deviceName;
    }

    public void setDeviceName(String deviceName)
    {
        this.deviceName = deviceName;
    }

    public String getSsid()
    {
        return ssid;
    }

    public void setSsid(String ssid)
    {
        this.ssid = ssid;
    }

    public String getPass()
    {
        return pass;
    }

    public void setPass(String pass)
    {
        this.pass = pass;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public String getPort()
    {
        return port;
    }

    public void setPort(String port)
    {
        this.port = port;
    }

    public String getCurrentIp()
    {
        return currentIp;
    }

    public void setCurrentIp(String currentIp)
    {
        this.currentIp = currentIp;
    }

    public String getApName()
    {
        return apName;
    }

    public void setApName(String apName)
    {
        this.apName = apName;
    }
}
