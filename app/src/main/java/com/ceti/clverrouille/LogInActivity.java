package com.ceti.clverrouille;

import android.app.AlertDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LogInActivity extends AppCompatActivity
{
    private AlertDialog alertDialog;
    private DatabaseReference users;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        //Inicializar Views
        initViews();
    }

    private void initViews()
    {
        Button logInButton = findViewById(R.id.log_in_button);
        logInButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Constantes.showProgressDialog(LogInActivity.this, alertDialog);
            }
        });
    }

    private void getUser(String email)
    {
        users = FirebaseDatabase.getInstance().getReference("usuarios");
        Query query = users.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
}
