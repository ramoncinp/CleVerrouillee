package com.ceti.clverrouille;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient extends AsyncTask<String, Void, String>
{
    private String serverIp;
    private String message;

    private MessageListener messageListener;

    public UDPClient(String serverIp, String message, MessageListener messageListener)
    {
        this.serverIp = serverIp;
        this.message = message;
        this.messageListener = messageListener;
    }

    @Override
    protected void onPostExecute(String s)
    {
        super.onPostExecute(s);

        //Retornar respuesta
        messageListener.onResponse(s);
    }

    @Override
    protected String doInBackground(String... strings)
    {
        return sendUdpMessage();
    }

    public String sendUdpMessage()
    {
        DatagramSocket c;
        String responseMessage = "";

        try
        {
            c = new DatagramSocket();
            c.setBroadcast(true);

            byte[] sendData = message.getBytes();

            try
            {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                        InetAddress.getByName(serverIp), 2401);

                c.send(sendPacket);
                c.send(sendPacket);
                c.send(sendPacket);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            //Obtener respuestas de los dispositivo
            while (true)
            {
                try
                {
                    byte[] recvBuf = new byte[256];
                    DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                    c.setSoTimeout(3000);
                    c.receive(receivePacket);

                    //Obtener mensaje
                    responseMessage = new String(receivePacket.getData()).trim();
                    break;
                }
                catch (IOException e)
                {
                    break;
                }
            }

            //Close the port!
            c.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return responseMessage;
    }

    public interface MessageListener
    {
        void onResponse(String response);
    }
}
