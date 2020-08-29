package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);


        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        mRef= FirebaseDatabase.getInstance().getReference().child("Users");

        Runnable runnable=new Runnable() {
            @Override
            public void run() {
               /* Intent i=new Intent(getApplicationContext(),LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);*/

               if(mUser!=null){
                   mRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                       @Override
                       public void onDataChange(@NonNull DataSnapshot snapshot) {


                           if(snapshot.exists()){
                              startActivity(new Intent(SplashActivity.this,MainActivity.class));
                              finish();
                           }else {
                               startActivity(new Intent(SplashActivity.this,SetupActivity.class));
                               finish();

                           }
                       }

                       @Override
                       public void onCancelled(@NonNull DatabaseError error) {

                       }
                   });
               }
               else{
                   startActivity(new Intent(SplashActivity.this,LoginActivity.class));

               }
            }


        };
        Handler handler=new Handler();

        handler.postDelayed(runnable,300);
    }
    }

