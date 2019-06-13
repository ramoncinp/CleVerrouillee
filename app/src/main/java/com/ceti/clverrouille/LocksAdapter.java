package com.ceti.clverrouille;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class LocksAdapter extends RecyclerView.Adapter<LocksAdapter
        .WifiDeviceViewHolder> implements View.OnClickListener
{
    private final ArrayList<WifiDevice> wifiDevices;
    private View.OnClickListener listener;

    public LocksAdapter(ArrayList<WifiDevice> wifiDevices)
    {
        this.wifiDevices = wifiDevices;
    }

    public void setOnClickListener(View.OnClickListener listener)
    {
        this.listener = listener;
    }

    @NonNull
    @Override
    public WifiDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lock_item_layout,
                parent, false);

        WifiDeviceViewHolder wifiDeviceViewHolder = new WifiDeviceViewHolder(v);
        v.setOnClickListener(this);

        return wifiDeviceViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull WifiDeviceViewHolder holder, int position)
    {
        WifiDevice wifiDevice = wifiDevices.get(position);
        holder.name.setText(wifiDevice.getDeviceName());
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
        private TextView name;

        WifiDeviceViewHolder(View itemView)
        {
            super(itemView);
            name = itemView.findViewById(R.id.device_name);
        }
    }
}