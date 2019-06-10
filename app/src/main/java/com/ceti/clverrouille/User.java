package com.ceti.clverrouille;

public class User
{
    private String nombre;
    private String email;
    private String pass;

    public User()
    {
    }

    public User(String nombre, String email, String pass)
    {
        this.nombre = nombre;
        this.email = email;
        this.pass = pass;
    }

    public String getNombre()
    {
        return nombre;
    }

    public void setNombre(String nombre)
    {
        this.nombre = nombre;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPass()
    {
        return pass;
    }

    public void setPass(String pass)
    {
        this.pass = pass;
    }
}

