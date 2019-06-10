package com.ceti.clverrouille;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.METValidator;

public class LogInActivity extends AppCompatActivity
{
    private final String TAG = LogInActivity.class.getSimpleName();

    private CustomProgressDialog alertDialog;
    private DatabaseReference users;

    //Views
    private MaterialEditText username;
    private MaterialEditText pass;

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
        username = findViewById(R.id.username_et);
        pass = findViewById(R.id.password_et);

        Button logInButton = findViewById(R.id.log_in_button);
        logInButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (validateEditText())
                {
                    alertDialog.show("Cargando...");
                    getUser(username.getText().toString(), pass.getText().toString());
                }
            }
        });

        TextView addUserButton = findViewById(R.id.add_user_button);
        addUserButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(LogInActivity.this, AddUser.class);
                startActivity(intent);
            }
        });

        alertDialog = new CustomProgressDialog(this);
    }

    private boolean validateEditText()
    {
        boolean valid = username.validateWith(new METValidator("Email inválido")
        {
            @Override
            public boolean isValid(@NonNull CharSequence text, boolean isEmpty)
            {
                //Obtener String
                String email = text.toString();

                if (isEmpty)
                {
                    return false;
                }
                else if (!email.contains("@"))
                {
                    return false;
                }

                return true;
            }
        });

        valid &= pass.validateWith(new METValidator("Contraseña inválida")
        {
            @Override
            public boolean isValid(@NonNull CharSequence text, boolean isEmpty)
            {
                return !isEmpty;
            }
        });

        return valid;
    }

    private void getUser(String email, final String password)
    {
        users = FirebaseDatabase.getInstance().getReference("usuarios");
        Query query = users.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                alertDialog.dismiss();

                Log.d(TAG, "Usuario encontrado -> " + dataSnapshot.toString());
                if (dataSnapshot.getValue() == null)
                {
                    Constantes.showResultInDialog("Iniciar sesión", "El usuario no existe",
                            LogInActivity.this);
                }
                else
                {
                    //Obtener contraseña
                    //Evaluar contraseña

                    //DataSnapshot { key = usuarios, value = {1={pass=abcdefg, nombre=Marco
                    // Antonio, email=marcoAntonio@gmail.com}} }

                    User mUser = dataSnapshot.getChildren().iterator().next().getValue(User.class);

                    String gottenPass = pass.getText().toString();
                    if (mUser.getPass().equals(gottenPass))
                    {
                        Constantes.showResultInDialog("Iniciar sesión", "Autenticao'",
                                LogInActivity.this);
                    }
                    else
                    {
                        Constantes.showResultInDialog("Iniciar sesión", "Error de autenticacion",
                                LogInActivity.this);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.d(TAG, "Ocurrió un error, intenté de nuevo");
            }
        });
    }
}
