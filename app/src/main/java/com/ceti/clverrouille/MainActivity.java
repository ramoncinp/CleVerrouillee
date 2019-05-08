package com.ceti.clverrouille;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDatabase();
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
}
