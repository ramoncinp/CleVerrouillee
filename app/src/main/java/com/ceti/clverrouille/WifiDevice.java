package com.ceti.clverrouille;

public class WifiDevice
{
    private String deviceName = "";
    private String apName = "";
    private String ssid = "";
    private String pass = "";
    private String currentIp = "";
    private String llave = "";
    private String nfc = "";
    private String userId = "";

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

    public String getLlave()
    {
        return llave;
    }

    public void setLlave(String llave)
    {
        this.llave = llave;
    }

    public String getNfc()
    {
        return nfc;
    }

    public void setNfc(String nfc)
    {
        this.nfc = nfc;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }
}
