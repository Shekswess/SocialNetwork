package com.example.finalproject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPost extends RecyclerView.Adapter<AdapterPost.MyHolder> {

    Context context;
    List<ModelPost> postList;

    String myUid;

    private DatabaseReference likesRef;
    private DatabaseReference postsRef;

    boolean mProcessLikes = false;

    public AdapterPost(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") int position) {
        String uid = postList.get(position).getUid();
        String uEmail = postList.get(position).getuEmail();
        String uName = postList.get(position).getuName();
        String uDp = postList.get(position).getuDp();
        String pId = postList.get(position).getpId();
        String pTitle = postList.get(position).getpTitle();
        String pDescription = postList.get(position).getpDescr();
        String pImage = postList.get(position).getpImage();
        String pTimeStamp = postList.get(position).getpTime();
        String pLikes = postList.get(position).getpLikes();
        String pComments = postList.get(position).getpComments();

        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(pTime);
        holder.pTitleTv.setText(pTitle);
        holder.pDescriptionTv.setText(pDescription);
        holder.pLikesTv.setText(pLikes + " Likes");
        holder.pCommentsTv.setText(pComments +" Comments");

        setLikes(holder, pId);

        try{
            Picasso.get().load(uDp).placeholder(R.drawable.ic_default_img).into(holder.uPictureIv);
        }
        catch(Exception e)
        {

        }

        if(pImage.equals("noImage"))
        {
            holder.pImageIv.setVisibility(View.GONE);
        }
        else
        {
            holder.pImageIv.setVisibility(View.VISIBLE);
            try{
                Picasso.get().load(pImage).into(holder.pImageIv);
            }
            catch(Exception e)
            {

            }
        }


        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOptions(holder.moreBtn, uid, myUid, pId, pImage);
            }
        });

        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pLikes = Integer.parseInt(postList.get(position).getpLikes());
                mProcessLikes = true;
                String postId = postList.get(position).getpId();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(mProcessLikes)
                        {
                            if(snapshot.child(postId).hasChild(myUid))
                            {
                                postsRef.child(postId).child("pLikes").setValue(""+(pLikes - 1));
                                likesRef.child(postId).child(myUid).removeValue();
                                mProcessLikes = false;
                            }
                            else
                            {
                                postsRef.child(postId).child("pLikes").setValue(""+(pLikes +1 ));
                                likesRef.child(postId).child(myUid).setValue("Liked");
                                mProcessLikes = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent = new Intent(context, PostDetailActivity.class);
               intent.putExtra("postId", pId);
               context.startActivity(intent);
            }
        });

        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) holder.pImageIv.getDrawable();
                if(bitmapDrawable == null)
                {
                    shareTextOnly(pTitle, pDescription);
                }
                else {
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle,pDescription,bitmap);
                }
            }
        });

        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, TheProfileActivity.class);
                intent.putExtra("uid",uid);
                context.startActivity(intent);
            }
        });

    }

    private void shareTextOnly(String pTitle, String pDescription) {
        String shareBody = pTitle + "\n" + pDescription;

        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(sIntent, "Share Via"));

    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {
        String shareBody = pTitle + "\n" + pDescription;
        Uri uri = saveImageToShare(bitmap);
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.setType("image/png");
        context.startActivity(Intent.createChooser(sIntent, "Share Via"));

    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs();
            File file = new File(imageFolder, "shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "com.example.finalproject.fireprovider", file);
        }
        catch (Exception e)
        {
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }


    private void setLikes(MyHolder holder, String postKey) {

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postKey).hasChild(myUid)){
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like, 0, 0, 0);
                    holder.likeBtn.setText("Liked");
                }
                else
                {
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
                    holder.likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, String pId, String pImage) {
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);
        if(uid.equals(myUid))
        {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }
        popupMenu.getMenu().add(Menu.NONE,2,0,"View Detail");
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if(id ==0){
                    beginDelete(pId,pImage);
                }
                else if(id == 1)
                {
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key","editPost");
                    intent.putExtra("editPostId",pId);
                    context.startActivity(intent);


                }
                else if(id == 2)
                {
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId", pId);
                    context.startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        if(pImage.equals("noImage")){
            deleteWithoutImage(pId);
        }
        else{
            deleteWithImage(pId, pImage);
        }
    }

    private void deleteWithoutImage(String pId) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting...");

        Query firebaseQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        firebaseQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren())
                {
                    ds.getRef().removeValue();
                }
                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void deleteWithImage(String pId, String pImage){
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting...");

        StorageReference picReference = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Query firebaseQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                firebaseQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren())
                        {
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView uPictureIv, pImageIv;
        TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv, pLikesTv, pCommentsTv;
        ImageButton moreBtn;
        Button likeBtn, commentBtn, shareBtn;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            pImageIv = itemView.findViewById(R.id.pImageIv);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            pTimeTv = itemView.findViewById(R.id.uTimeTv);
            pTitleTv = itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
            pLikesTv = itemView.findViewById(R.id.pLikesTv);
            pCommentsTv = itemView.findViewById(R.id.pCommentsTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            profileLayout = itemView.findViewById(R.id.profileLayout);

        }
    }
}
