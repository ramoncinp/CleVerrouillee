/*
Copyright 2013 Square, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.ceti.clverrouille;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class RegistersAdapter extends RecyclerView.Adapter<RegistersAdapter
        .RegistersViewHolder> implements View.OnClickListener
{
    private final ArrayList<Register> registers;
    private View.OnClickListener listener;

    public RegistersAdapter(ArrayList<Register> registers)
    {
        this.registers = registers;
    }

    public void setOnClickListener(View.OnClickListener listener)
    {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RegistersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.register_item_layout,
                parent, false);

        RegistersViewHolder registersViewHolder = new RegistersViewHolder(v);
        v.setOnClickListener(this);

        return registersViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RegistersViewHolder holder, int position)
    {
        Register register = registers.get(position);
        holder.date.setText(register.getDateString());

        if (register.getState().equals("1"))
        {
            holder.status.setText("Cerradura accionada");
        }
        else
        {
            holder.status.setText("Error de autenticación");
        }

        File file = new File(register.getImagePath());
        Picasso.get().load(file).into(holder.image);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount()
    {
        return registers.size();
    }

    @Override
    public void onClick(View view)
    {
        if (listener != null) listener.onClick(view);
    }

    static class RegistersViewHolder extends RecyclerView.ViewHolder
    {
        private TextView status;
        private TextView date;
        private ImageView image;

        RegistersViewHolder(View itemView)
        {
            super(itemView);
            status = itemView.findViewById(R.id.status);
            date = itemView.findViewById(R.id.date);
            image = itemView.findViewById(R.id.imageView);
        }
    }
}