package com.ceti.clverrouille;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.METValidator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    private static final int LOG_IN_REQUEST_CODE = 0;
    private static final int EDIT_DEVICE = 1;
    private static final int UNLOCK_DEVICE = 2;

    private static final String TAG = MainActivity.class.getSimpleName();

    //Variables
    private int deviceOperation;
    private String llaveIngresada;
    private String userId;

    //Views
    private Dialog optionsDialog;
    private FloatingActionButton fab;
    private RecyclerView devicesList;
    private TextView noDevices;

    //Objetos
    private ArrayList<WifiDevice> wifiDevices = new ArrayList<>();
    private FirebaseDatabase root;
    private WifiDevice selectedDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Clé Verrouillée");

        //Evaluar si la sesión esta iniciada
        if (!isLogedIn())
        {
            Intent intent = new Intent(this, LogInActivity.class);
            startActivityForResult(intent, LOG_IN_REQUEST_CODE);
        }
        else
        {
            initViews();
            getUserId();
            initDatabase();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.log_out)
        {
            //Eliminar de shared preferences
            logOut();
        }
        else if (id == R.id.config_device)
        {
            //Iniciar actividad para escanear dispositivos
            Intent intent = new Intent(this, ConfigureDevice.class);
            intent.putExtra(ConfigureDevice.OPERATION, ConfigureDevice.CONFIGURE_WIFI);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOG_IN_REQUEST_CODE)
        {
            //Reiniciar
            this.recreate();
        }
    }

    private void initViews()
    {
        noDevices = findViewById(R.id.no_found_devices_tv);
        devicesList = findViewById(R.id.user_devices_list);
        fab = findViewById(R.id.add_device);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, ConfigureDevice.class);
                intent.putExtra(ConfigureDevice.OPERATION, ConfigureDevice.ADD_DEVICE_TO_DB);
                startActivity(intent);
            }
        });
    }

    private void getUserId()
    {
        SharedPreferences sp = getSharedPreferences(Constantes.SHARED_PREFERENCES_NAME,
                MODE_PRIVATE);
        userId = sp.getString(Constantes.USER_ID, "");
    }

    private void initDatabase()
    {
        //Obtener base de datos
        root = FirebaseDatabase.getInstance();

        //Obtener referencia de usuario
        DatabaseReference users = root.getReference("usuarios");

        //Ejecutar query para obtener usuario
        Query getUser = users.orderByKey().equalTo(userId);
        getUser.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                parseData(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Toast.makeText(MainActivity.this, "Error al obtener usuario", Toast.LENGTH_SHORT).show();
            }
        });

        //Obtener referencia de dispositivos
        DatabaseReference devices = root.getReference("dispositivos");
        devices.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                listUserDevices(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.d("UserDevices", databaseError.toString());
                devicesList.setVisibility(View.GONE);
                noDevices.setVisibility(View.VISIBLE);
            }
        });
    }

    private void listUserDevices(DataSnapshot dataSnapshot)
    {
        Log.d("UserDevices", dataSnapshot.toString());
        if (dataSnapshot.getValue() != null)
        {
            //Obtener contraseña
            //Evaluar contraseña

            /*
            {
                key = dispositivos,
                value = {
                    -LhH6fF8Ki2ymMFg_q1l={
                        currentIp=192.168.0.204,
                        llave=2803269,
                        pass=B1n4r1uM,
                        nfc=,
                        deviceName=Casa,
                        ssid=AP_OFICINA,
                        userId=-Lh0mYAFIF67B5oymvkg,
                        apName=LOCK_2803269}
                }
            }
             */

            wifiDevices = new ArrayList<>();

            for (DataSnapshot snapshot : dataSnapshot.getChildren())
            {
                WifiDevice wifiDevice = snapshot.getValue(WifiDevice.class);
                wifiDevices.add(wifiDevice);
            }

            LocksAdapter locksAdapter = new LocksAdapter(wifiDevices);
            locksAdapter.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    selectedDevice = wifiDevices.get(devicesList.getChildAdapterPosition(v));
                    showDeviceOptionDialog();
                }
            });

            devicesList.setAdapter(locksAdapter);
            devicesList.setLayoutManager(new LinearLayoutManager(this));
            devicesList.setVisibility(View.VISIBLE);
            noDevices.setVisibility(View.INVISIBLE);
        }
        else
        {
            devicesList.setVisibility(View.GONE);
            noDevices.setVisibility(View.VISIBLE);
        }
    }

    private void parseData(DataSnapshot dataSnapshot)
    {
        TextView nombreTv = findViewById(R.id.nombre);

        Log.d(TAG, "Usuario encontrado -> " + dataSnapshot.toString());
        if (dataSnapshot.getValue() == null)
        {
            Constantes.showResultInDialog("", "El usuario solicitado no existe",
                    MainActivity.this);
        }
        else
        {
            //Obtener contraseña
            //Evaluar contraseña

            //DataSnapshot { key = usuarios, value = {1={pass=abcdefg, nombre=Marco
            // Antonio, email=marcoAntonio@gmail.com}} }

            DataSnapshot userObject = dataSnapshot.getChildren().iterator().next();
            User mUser = userObject.getValue(User.class);

            //Mostrar nombre obtenidoasd
            nombreTv.setText(mUser.getNombre());
        }
    }

    private boolean isLogedIn()
    {
        //Obtener sharedPreferences
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constantes.SHARED_PREFERENCES_NAME, MODE_PRIVATE);

        //Evaluar si existe el elemento "UserID"
        return sharedPreferences.contains(Constantes.USER_ID);
    }

    private void logOut()
    {
        SharedPreferences.Editor editor =
                getSharedPreferences(Constantes.SHARED_PREFERENCES_NAME, MODE_PRIVATE).edit();

        editor.remove(Constantes.USER_ID);
        editor.apply();

        //Reiniciar actividad
        this.recreate();
    }

    private void showDeviceOptionDialog()
    {
        LayoutInflater inflater = getLayoutInflater();
        View content = inflater.inflate(R.layout.selected_device_option_dialog, null);

        final MaterialEditText llave = content.findViewById(R.id.llave);
        CardView unlock = content.findViewById(R.id.unlock);
        CardView edit = content.findViewById(R.id.edit_lock);

        final METValidator validator = new METValidator("Campo obligatorio")
        {
            @Override
            public boolean isValid(@NonNull CharSequence text, boolean isEmpty)
            {
                return !isEmpty;
            }
        };

        unlock.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (llave.validateWith(validator))
                {
                    llaveIngresada = llave.getText().toString();

                    //Enviar request
                    deviceOperation = UNLOCK_DEVICE;
                    searchAndSendRequest();
                    optionsDialog.dismiss();
                }
            }
        });

        edit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (llave.validateWith(validator))
                {
                    llaveIngresada = llave.getText().toString();

                    //Enviar request
                    deviceOperation = EDIT_DEVICE;
                    searchAndSendRequest();
                    optionsDialog.dismiss();
                }
            }
        });

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(content);

        optionsDialog = dialogBuilder.create();
        optionsDialog.setCancelable(true);
        optionsDialog.show();
    }

    private void searchAndSendRequest()
    {
        Toast.makeText(this, "Cargando...", Toast.LENGTH_SHORT).show();

        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                final String deviceIp = searchDevice();
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (!deviceIp.isEmpty())
                        {
                            sendConfig(deviceIp);
                        }
                        else
                        {
                            Toast.makeText(MainActivity.this, "Dispositivo no encontrado",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        thread.start();
    }

    private String searchDevice()
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
                    c.setSoTimeout(1500);
                    c.receive(receivePacket);

                    //Si hubo respuesta y no hubo timeout, obtener datos del dispositivo
                    String ipAdress = receivePacket.getAddress().getHostAddress();

                    //Obtener mensaje de respuesta
                    String message = new String(receivePacket.getData()).trim();

                    //Loggear mensaje
                    Log.d(TAG, "Message -> " + message);

                    //Crear WiFiDevice
                    if (selectedDevice.getApName().equals(message))
                    {
                        return ipAdress;
                    }
                }
                catch (IOException e)
                {
                    break;
                }
            }

            //Cerrar el puerto UDP
            c.close();

            return "";
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return "";
    }

    private void sendConfig(String ip)
    {
        JSONObject request = new JSONObject();

        try
        {
            request.put("llave", llaveIngresada);
            if (deviceOperation == UNLOCK_DEVICE)
            {
                request.put("key", "unlock");
            }
            else if (deviceOperation == EDIT_DEVICE)
            {
                request.put("key", "get_config");
            }
            else
            {
                return;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return;
        }


        UDPClient udpClient = new UDPClient(
                ip,
                request.toString(),
                new UDPClient.MessageListener()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.d(TAG, "Respuesta -> " + response);
                        String resultMessage = "";

                        try
                        {
                            //Evaluar respuesta
                            JSONObject jsonResponse = new JSONObject(response);

                            //Evaluar response
                            String responseVal = jsonResponse.getString("response");
                            if (responseVal.equals("ok"))
                            {
                                if (deviceOperation == UNLOCK_DEVICE)
                                {
                                    resultMessage = "Cerradura accionada";
                                }
                                else if (deviceOperation == EDIT_DEVICE)
                                {
                                    resultMessage = "Datos obtenidos";
                                    showEditDeviceDialog(jsonResponse);
                                }
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
                        Toast.makeText(MainActivity.this, resultMessage,
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        udpClient.execute("");
    }

    private void showEditDeviceDialog(JSONObject response)
    {
        LayoutInflater inflater = getLayoutInflater();
        View content = inflater.inflate(R.layout.dialog_wifi_device_info_complete, null);

        final MaterialEditText nombre = content.findViewById(R.id.wifi_device_name);
        final MaterialEditText ssid = content.findViewById(R.id.wifi_device_network_ssid);
        final MaterialEditText pass = content.findViewById(R.id.wifi_device_network_pass);
        final MaterialEditText nfc = content.findViewById(R.id.wifi_device_nfc);
        final MaterialEditText llave = content.findViewById(R.id.wifi_device_key);
        Button submit = content.findViewById(R.id.wifi_device_dialog_submit);

        final METValidator validator = new METValidator("Campo obligatorio")
        {
            @Override
            public boolean isValid(@NonNull CharSequence text, boolean isEmpty)
            {
                return !isEmpty;
            }
        };

        //Mostrar datos
        try
        {
            JSONObject data = response.getJSONObject("data");
            nombre.setText(selectedDevice.getDeviceName());
            ssid.setText(data.getString("ssid"));
            pass.setText(data.getString("pass"));
            nfc.setText(data.getString("nfc"));
            llave.setText(data.getString("llave"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        submit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                boolean valid = nombre.validateWith(validator);
                valid &= ssid.validateWith(validator);
                valid &= pass.validateWith(validator);
                valid &= llave.validateWith(validator);

                if (valid)
                {
                    Toast.makeText(MainActivity.this, "Editando info...", Toast.LENGTH_SHORT).show();
                    optionsDialog.dismiss();
                }
            }
        });

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(content);

        optionsDialog = dialogBuilder.create();
        optionsDialog.setCancelable(true);
        optionsDialog.show();
    }
}
