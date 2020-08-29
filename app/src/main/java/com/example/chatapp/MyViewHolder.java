package com.example.chatapp;

import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

class MyViewHolder extends RecyclerView.ViewHolder {

    CircleImageView profileImageView;
    ImageView postImage, likeImage, commentsImage,sendComment;
    TextView username, timeAgo, postDesc, likesCounter, commentsCounter;
    EditText inputComments;


    public MyViewHolder(@NonNull View view) {
        super(view);

        profileImageView = view.findViewById(R.id.profileImageView);
        postImage = view.findViewById(R.id.postImage);
        username = view.findViewById(R.id.addPostUsername);
        timeAgo = view.findViewById(R.id.timeAgo);
        postDesc = view.findViewById(R.id.postDesc);
        likeImage = view.findViewById(R.id.like);
        commentsImage = view.findViewById(R.id.comments);
        likesCounter = view.findViewById(R.id.likesCounter);
        commentsCounter = view.findViewById(R.id.commentsCounter);
        sendComment=view.findViewById(R.id.sendComment);
        inputComments=view.findViewById(R.id.inputComments);

    }




    public void countLikes(String postKey, String uid, DatabaseReference likeRef) {


        likeRef.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.child(uid).exists()){
                    likeImage.setColorFilter(Color.GREEN);
                }else{
                    likeImage.setColorFilter(Color.BLACK);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        likeRef.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    int totalLikes= (int) snapshot.getChildrenCount();
                    likesCounter.setText(totalLikes+"");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }
}
