package com.ceti.clverrouille;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class WifiDeviceAdapter extends RecyclerView.Adapter<WifiDeviceAdapter
        .WifiDeviceViewHolder> implements View.OnClickListener
{
    private final ArrayList<WifiDevice> wifiDevices;
    private Context context;
    private View.OnClickListener listener;

    public WifiDeviceAdapter(ArrayList<WifiDevice> wifiDevices, Context context)
    {
        this.wifiDevices = wifiDevices;
        this.context = context;
    }

    public void setOnClickListener(View.OnClickListener listener)
    {
        this.listener = listener;
    }

    @NonNull
    @Override
    public WifiDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.wifi_device_layout,
                parent, false);

        WifiDeviceViewHolder wifiDeviceViewHolder = new WifiDeviceViewHolder(v);
        v.setOnClickListener(this);

        return wifiDeviceViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull WifiDeviceViewHolder holder, int position)
    {
        WifiDevice wifiDevice = wifiDevices.get(position);

        holder.ssid.setText(wifiDevice.getApName());
        holder.ipAddress.setVisibility(View.VISIBLE);
        holder.ipAddress.setText(wifiDevice.getCurrentIp());
        holder.wifiIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable
                .ic_wifi_green));
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount()
    {
        return wifiDevices.size();
    }

    @Override
    public void onClick(View view)
    {
        if (listener != null) listener.onClick(view);
    }

    static class WifiDeviceViewHolder extends RecyclerView.ViewHolder
    {
        private TextView ssid;
        private TextView apName;
        private TextView ipAddress;
        private ImageView wifiIcon;

        WifiDeviceViewHolder(View itemView)
        {
            super(itemView);

            apName = itemView.findViewById(R.id.wifi_device_ap_name);
            ssid = itemView.findViewById(R.id.wifi_device_name);
            ipAddress = itemView.findViewById(R.id.wifi_device_ip_address);
            wifiIcon = itemView.findViewById(R.id.wifi_device_wifi_icon);
        }
    }
}