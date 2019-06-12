package com.ceti.clverrouille;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.METValidator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ConfigureDevice extends AppCompatActivity
{
    private static final String TAG = ConfigureDevice.class.getSimpleName();

    //Variables
    private String deviceIP;

    //Views
    private Dialog configDeviceDialog;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private TextView noDevices;

    //Objetos
    private ArrayList<WifiDevice> wifiDevices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_device);

        //Definir titulo
        setTitle("Configurar dispositivo");

        //Agregar flecha para regresar
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Inicialziar views
        recyclerView = findViewById(R.id.found_devices_list);
        progressBar = findViewById(R.id.progress_bar);
        noDevices = findViewById(R.id.no_found_devices_tv);

        //Realizar scann en un thread
        Thread scannThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                startScann();
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        listWifiDevices();
                    }
                });
            }
        });

        //Ejecutar scann
        scannThread.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void startScann()
    {
        DatagramSocket c;
        try
        {
            c = new DatagramSocket();
            c.setBroadcast(true);

            byte[] sendData = "EVERYTHING IS COPACETIC".getBytes();

            try
            {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                        InetAddress.getByName("255.255.255.255"), 2401);

                c.send(sendPacket);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            //Obtener respuestas de los dispositivos de la red
            while (true)
            {
                try
                {
                    byte[] recvBuf = new byte[256];
                    DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                    c.setSoTimeout(6000);
                    c.receive(receivePacket);

                    //Si hubo respuesta y no hubo timeout, obtener datos del dispositivo
                    String ipAdress = receivePacket.getAddress().getHostAddress();

                    //Obtener mensaje de respuesta
                    String message = new String(receivePacket.getData()).trim();

                    //Crear WiFiDevice
                    WifiDevice wifiDevice = new WifiDevice();
                    wifiDevice.setDeviceName(message);
                    wifiDevice.setCurrentIp(ipAdress);

                    //Agregar dispositivo encontrado a la lista
                    wifiDevices.add(wifiDevice);

                    //Loggear mensaje
                    Log.d(TAG, "Message -> " + message);
                }
                catch (IOException e)
                {
                    break;
                }
            }

            //Ordenar dispostiivos obtenidos
            Collections.sort(wifiDevices, new WiFiDevicesComparator());

            //Cerrar el puerto UDP
            c.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void listWifiDevices()
    {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        WifiDeviceAdapter wifiDeviceAdapter = new WifiDeviceAdapter(wifiDevices, this);
        wifiDeviceAdapter.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                accessWiFiDeviceDialog();
                deviceIP =
                        wifiDevices.get(recyclerView.getChildAdapterPosition(view)).getCurrentIp();
            }
        });

        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setAdapter(wifiDeviceAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(false);

        if (wifiDevices.isEmpty())
        {
            noDevices.setVisibility(View.VISIBLE);
        }
        else
        {
            noDevices.setVisibility(View.GONE);
        }

        progressBar.setVisibility(View.GONE);
    }

    private void accessWiFiDeviceDialog()
    {
        LayoutInflater inflater = getLayoutInflater();
        View content = inflater.inflate(R.layout.dialog_wifi_device_info, null);

        final MaterialEditText wifiNetworkSsid =
                content.findViewById(R.id.wifi_device_network_ssid);
        final MaterialEditText wifiNetworkPassword =
                content.findViewById(R.id.wifi_device_network_pass);
        final MaterialEditText wifiDeviceKey = content.findViewById(R.id.wifi_device_key);

        Button saveConfig = content.findViewById(R.id.wifi_device_dialog_submit);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(content);

        configDeviceDialog = dialogBuilder.create();
        configDeviceDialog.setCancelable(true);
        configDeviceDialog.show();

        saveConfig.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Validar
                METValidator emptyValidator = new METValidator("Campo obligatorio")
                {
                    @Override
                    public boolean isValid(@NonNull CharSequence text, boolean isEmpty)
                    {
                        return !isEmpty;
                    }
                };

                boolean isValid;

                isValid = wifiNetworkSsid.validateWith(emptyValidator);
                isValid &= wifiNetworkPassword.validateWith(emptyValidator);
                isValid &= wifiDeviceKey.validateWith(emptyValidator);

                if (isValid)
                {
                    showProgressBarInDialog();

                    //Obtener datos en JSON
                    JSONObject requestBody = new JSONObject();
                    try
                    {
                        requestBody.put("key", "config_wifi");
                        requestBody.put("llave", wifiDeviceKey.getText().toString());

                        JSONObject data = new JSONObject();
                        data.put("ssid", wifiNetworkSsid.getText().toString());
                        data.put("pass", wifiNetworkPassword.getText().toString());
                        requestBody.put("data", data);

                        sendConfig(requestBody);
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                        Toast.makeText(ConfigureDevice.this, "Error al procesar la informacion",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void sendConfig(JSONObject request)
    {
        UDPClient udpClient = new UDPClient(
                deviceIP,
                request.toString(),
                new UDPClient.MessageListener()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.d(TAG, "Respuesta -> " + response);
                        String resultMessage;

                        try
                        {
                            //Evaluar respuesta
                            JSONObject jsonResponse = new JSONObject(response);

                            //Evaluar response
                            String responseVal = jsonResponse.getString("response");
                            if (responseVal.equals("ok"))
                            {
                                resultMessage = "Dispositivo configurado \" " +
                                        "+\n\"correctamente\\nSe reiniciará para aplicar los " +
                                        "cambios\"";

                                //Cerrar diálogo
                                configDeviceDialog.dismiss();
                            }
                            else
                            {
                                resultMessage = "Error de autenticación";
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                            resultMessage = "Error al procesar respuesta";
                        }

                        //Mostrar resultado
                        Toast.makeText(ConfigureDevice.this, resultMessage,
                                Toast.LENGTH_SHORT).show();

                        if (!configDeviceDialog.isShowing())
                        {
                            //Salir de la actividad
                            finish();
                        }
                        else
                        {
                            //Dar oportunidad a que ingrese la llave correcta
                            showContentInDialog();
                        }
                    }
                }
        );

        udpClient.execute("");
    }

    private void showProgressBarInDialog()
    {
        ProgressBar progressBar = configDeviceDialog.findViewById(R.id.wifi_device_save_progress);
        progressBar.setVisibility(View.VISIBLE);

        LinearLayout linearLayout = configDeviceDialog.findViewById(R.id.dialog_content);
        linearLayout.setVisibility(View.GONE);
    }

    private void showContentInDialog()
    {
        ProgressBar progressBar = configDeviceDialog.findViewById(R.id.wifi_device_save_progress);
        progressBar.setVisibility(View.GONE);

        LinearLayout linearLayout = configDeviceDialog.findViewById(R.id.dialog_content);
        linearLayout.setVisibility(View.VISIBLE);
    }

    private class WiFiDevicesComparator implements Comparator<WifiDevice>
    {
        @Override
        public int compare(WifiDevice wifiDevice, WifiDevice t1)
        {
            return wifiDevice.getDeviceName().compareTo(t1.getDeviceName());
        }
    }
}
