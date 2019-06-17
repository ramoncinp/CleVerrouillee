package com.ceti.clverrouille;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

public class FullScreenPicture extends AppCompatActivity
{
    private String imagePath;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_picture);

        imagePath = getIntent().getStringExtra("imagePath");
        imageView = findViewById(R.id.image);
        imageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
        Picasso.get().load(new File(imagePath)).into(imageView);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (getSupportActionBar() != null)
            getSupportActionBar().show();
    }
}
