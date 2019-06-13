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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    private static final int LOG_IN_REQUEST_CODE = 0;
    private static final String TAG = MainActivity.class.getSimpleName();

    //Variables
    private String userId;

    //Views
    private FloatingActionButton fab;
    private RecyclerView devicesList;
    private TextView noDevices;

    //Objetos
    private FirebaseDatabase root;

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

            ArrayList<WifiDevice> wifiDevices = new ArrayList<>();

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
                    //Enviar request
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
                    //Enviar request
                }
            }
        });

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(content);

        Dialog dialog = dialogBuilder.create();
        dialog.setCancelable(true);
        dialog.show();
    }
}
