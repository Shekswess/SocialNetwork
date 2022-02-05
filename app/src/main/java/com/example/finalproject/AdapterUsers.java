package com.example.finalproject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>{
    Context context;
    List<ModelUser> userList;

    public AdapterUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
            String userImage = userList.get(position).getImage();
            String userName = userList.get(position).getName();
            String userEmail = userList.get(position).getEmail();
            String UID = userList.get(position).getUid();

            holder.mNameTv.setText(userName);
            holder.mEmailTv.setText(userEmail);
            try{
                Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img).into(holder.avatarIv);
            }
            catch (Exception e)
            {

            }

            holder.itemView.setOnClickListener((v)-> {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(i == 0)
                    {
                        Intent intent = new Intent(context, TheProfileActivity.class);
                        intent.putExtra("uid",UID);
                        context.startActivity(intent);
                    }
                    if(i == 1){
                        Intent intent = new Intent(context,ChatActivity.class);
                        intent.putExtra("Uid", UID);
                        context.startActivity(intent);
                    }
                }
                });
                builder.create().show();
            });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView avatarIv;
        TextView mNameTv, mEmailTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            avatarIv = itemView.findViewById(R.id.avatarIv);
            mEmailTv = itemView.findViewById(R.id.emailTv);
            mNameTv = itemView.findViewById(R.id.nameTv);
        }
    }
}
