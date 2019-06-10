package com.ceti.clverrouille;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.METValidator;

public class AddUser extends AppCompatActivity
{
    private final String TAG = LogInActivity.class.getSimpleName();

    private MaterialEditText name;
    private MaterialEditText email;
    private MaterialEditText pass;
    private MaterialEditText confirmPass;

    private CustomProgressDialog progressDialog;

    private DatabaseReference users;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        setTitle("Registrarse");

        initViews();

        //Obtener referencia de usuarios
        users = FirebaseDatabase.getInstance().getReference("usuarios");
    }

    private void initViews()
    {
        name = findViewById(R.id.name_et);
        email = findViewById(R.id.username_et);
        pass = findViewById(R.id.password_et);
        confirmPass = findViewById(R.id.confirm_password_et);
        progressDialog = new CustomProgressDialog(this);

        Button signInButton = findViewById(R.id.log_in_button);
        signInButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (validateEditText())
                {
                    progressDialog.show("Agregando usuario");

                    //Crear instancia de nuevo usuario
                    User user = new User();

                    try
                    {
                        user.setNombre(name.getText().toString());
                        user.setEmail(email.getText().toString());
                        user.setPass(pass.getText().toString());
                    }
                    catch (NullPointerException e)
                    {
                        e.printStackTrace();
                        Toast.makeText(AddUser.this, "Error al procesar la información",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    users.push().setValue(user).addOnSuccessListener(new OnSuccessListener<Void>()
                    {
                        @Override
                        public void onSuccess(Void aVoid)
                        {
                            progressDialog.dismiss();

                            //Dato agregado
                            Log.d(TAG, "Dato agregado");
                            Toast.makeText(AddUser.this, "Usuario registrado correctamente",
                                    Toast.LENGTH_LONG).show();

                            //Volver a la actividad principal
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            progressDialog.dismiss();

                            //Dato no agregado
                            Log.d(TAG, "Error al agregar dato");
                        }
                    });
                }
            }
        });
    }

    private boolean validateEditText()
    {
        boolean valid = email.validateWith(new METValidator("Email inválido")
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

        valid &= name.validateWith(new METValidator("Nombre no válido")
        {
            @Override
            public boolean isValid(@NonNull CharSequence text, boolean isEmpty)
            {
                return !isEmpty;
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

        valid &= confirmPass.validateWith(new METValidator("Contraseña inválida")
        {
            @Override
            public boolean isValid(@NonNull CharSequence text, boolean isEmpty)
            {
                return !isEmpty;
            }
        });

        if (valid)
        {
            //Validar que las contraseñas sean iguales
            String pass1, pass2;
            pass1 = pass.getText().toString();
            pass2 = confirmPass.getText().toString();

            if (!pass1.equals(pass2))
            {
                Constantes.showResultInDialog("Registrarse", "Las contraseñas no coinciden", this);
                valid = false;
            }
        }

        return valid;
    }
}
