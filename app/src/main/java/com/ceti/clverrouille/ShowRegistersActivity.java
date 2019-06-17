package com.ceti.clverrouille;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ShowRegistersActivity extends AppCompatActivity
{
    //Views
    private RecyclerView registersList;

    //Objetos
    private ArrayList<Register> registers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_registers);

        //Agregar flecha para regresar
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        registersList = findViewById(R.id.registers_list);
        getRegisters();
    }

    private void getRegisters()
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss",
                Locale.getDefault());

        String directoryPath = Environment.getExternalStorageDirectory().toString();
        directoryPath += "/CleVerroulle/";

        File mDir = new File(directoryPath);
        if (!mDir.exists() && mDir.isDirectory())
        {
            Toast.makeText(this, "No hay registros", Toast.LENGTH_SHORT).show();
            return;
        }
        else
        {
            registers.clear();

            for (File file : mDir.listFiles())
            {
                Register register = new Register();

                Date date;
                String state, dateString;
                String fileName = file.getName();

                int pipeIdx = fileName.indexOf("|");
                if (pipeIdx != -1)
                {
                    state = fileName.substring(pipeIdx + 1, pipeIdx + 2);
                    dateString = fileName.substring(0, pipeIdx);

                    try
                    {
                        date = simpleDateFormat.parse(dateString);
                        register.setDate(date);
                    }
                    catch (ParseException e)
                    {
                        e.printStackTrace();
                    }

                    register.setState(state);
                    register.setImagePath(file.getAbsolutePath());

                    registers.add(register);
                }
            }

            RegistersAdapter registersAdapter = new RegistersAdapter(registers);
            registersAdapter.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Register register = registers.get(registersList.getChildAdapterPosition(v));

                    Intent intent = new Intent(ShowRegistersActivity.this, FullScreenPicture.class);
                    intent.putExtra("imagePath", register.getImagePath());
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim
                            .fade_out);
                }
            });
            registersList.setAdapter(registersAdapter);
            registersList.setLayoutManager(new LinearLayoutManager(this));
        }
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
}
