package com.ceti.clverrouille;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class Constantes
{
    public static void showResultInDialog(String title, String message, Activity activity)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Regresar", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
