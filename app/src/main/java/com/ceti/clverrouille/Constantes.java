package com.ceti.clverrouille;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;

public class Constantes
{
    static void showProgressDialog(Activity activity, AlertDialog alertDialog)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View content = activity.getLayoutInflater().inflate(R.layout.dialog_progress_bar, null);
        builder.setView(content);

        alertDialog = builder.create();
        alertDialog.show();
    }
}
