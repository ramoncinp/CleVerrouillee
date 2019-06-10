package com.ceti.clverrouille;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

public class CustomProgressDialog
{
    private AlertDialog alertDialog;
    private Activity activity;

    public CustomProgressDialog(Activity activity)
    {
        this.activity = activity;
    }

    public void show(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View content = activity.getLayoutInflater().inflate(R.layout.dialog_progress_bar, null);
        builder.setView(content);

        TextView text = content.findViewById(R.id.text);
        text.setText(message);

        alertDialog = builder.create();
        alertDialog.show();
    }

    public void dismiss()
    {
        if (alertDialog != null)
        {
            alertDialog.dismiss();
        }
    }
}
