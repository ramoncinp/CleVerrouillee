package com.ceti.clverrouille;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity
{
    private static final int LOG_IN_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Evaluar si la sesión esta iniciada
        if (!isLogedIn())
        {
            Intent intent = new Intent(this, LogInActivity.class);
            startActivityForResult(intent, LOG_IN_REQUEST_CODE);
        }
        else
        {
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

    private void initDatabase()
    {
        DatabaseReference sensors = FirebaseDatabase.getInstance().getReference().child("usuarios");
        sensors.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                parseData(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.d("Datos", "Error al obtener datos", databaseError.toException());
            }
        });
    }

    private void parseData(DataSnapshot dataSnapshot)
    {
        TextView nombreTv = findViewById(R.id.nombre);
        TextView emailTv = findViewById(R.id.correo);

        nombreTv.setText(dataSnapshot.child("nombre").getValue(String.class));
        emailTv.setText(dataSnapshot.child("email").getValue(String.class));
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
}
