package com.ceti.clverrouille;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Register
{
    private Date date;
    private String state;
    private String imagePath;

    public Register()
    {
    }

    public String getDateString()
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss",
                Locale.getDefault());

        return simpleDateFormat.format(date);
    }

    public void setDateString()
    {

    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public String getState()
    {
        return state;
    }

    public void setState(String state)
    {
        this.state = state;
    }

    public String getImagePath()
    {
        return imagePath;
    }

    public void setImagePath(String imagePath)
    {
        this.imagePath = imagePath;
    }
}
