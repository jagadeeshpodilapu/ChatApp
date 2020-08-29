package com.example.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 101;
    private CircleImageView profileImage;

    private EditText inputUsername, inputCity, inputCountry, inputProfession;

    private MaterialButton btnSave;
    Uri imageUri;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mRef;
    StorageReference storageRef;

    ProgressDialog progressDialog;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        toolbar=findViewById(R.id.appBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Setup Profile");

        profileImage = findViewById(R.id.profileImageView);
        inputUsername = findViewById(R.id.userName);
        inputCity = findViewById(R.id.inputCity);
        inputCountry = findViewById(R.id.inputCountry);
        inputProfession = findViewById(R.id.inputProfession);
        btnSave = findViewById(R.id.btnSave);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference().child("Users");
        storageRef = FirebaseStorage.getInstance().getReference().child("ProfileImages");

        progressDialog=new ProgressDialog(this);

        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE);
        });

        btnSave.setOnClickListener(v -> saveData());
    }


    private void saveData() {

        if (inputUsername.getText().toString().isEmpty() || inputUsername.getText().toString().length() < 3) {
            inputUsername.setError(" username not valid");
            inputUsername.requestFocus();
        } else if (inputCity.getText().toString().isEmpty() || inputCity.getText().toString().length() < 3) {
            inputCity.setError("enter valid city");
            inputCity.requestFocus();
        } else if (inputCountry.getText().toString().isEmpty() || inputCountry.getText().toString().length() < 3) {
            inputCountry.setError("enter  valid country ");
            inputCountry.requestFocus();
        } else if (inputProfession.getText().toString().isEmpty() || inputProfession.getText().toString().length() < 3) {
            inputProfession.setError("enter  valid profession");
            inputProfession.requestFocus();
        } else if (imageUri == null) {
            Toast.makeText(this, "upload Image", Toast.LENGTH_SHORT).show();
        } else {

            progressDialog.setTitle("SetUp Profile");
            progressDialog.setMessage("Please Wait...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();


            storageRef.child(mUser.getUid()).putFile(imageUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {


                    storageRef.child(mUser.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            HashMap hashMap = new HashMap();

                            hashMap.put("username", inputUsername.getText().toString());
                            hashMap.put("city", inputCity.getText().toString());
                            hashMap.put("country", inputCountry.getText().toString());
                            hashMap.put("profession", inputProfession.getText().toString());
                            hashMap.put("profileImage", uri.toString());
                            hashMap.put("status", "offline");



                            mRef.child(mUser.getUid()).updateChildren(hashMap)
                                    .addOnSuccessListener(o -> {
                                        startActivity(new Intent(SetupActivity.this,MainActivity.class));

                                        progressDialog.dismiss();

                                        Toast.makeText(SetupActivity.this, "Details Saved successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(SetupActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    });
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }
}
