package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText inputEmail,inputPassword;

    MaterialButton btnLogin;

    FirebaseAuth mAuth;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        inputEmail=findViewById(R.id.inputEmail);
        inputPassword=findViewById(R.id.inputPassword);
        btnLogin=findViewById(R.id.btnLogin);

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        btnLogin.setOnClickListener(v -> {
           userLogin();
        });


        findViewById(R.id.newAccount).setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),RegisterActivity.class)));
    }

    private void userLogin(){
        if(inputEmail.getText().toString().trim().isEmpty()){

            inputEmail.setError("Please Enter Email");
            inputEmail.requestFocus();
        }else if(inputPassword.getText().toString().trim().isEmpty()){
            inputPassword.setError("Please Enter Password");
            inputPassword.requestFocus();
        }else{
            progressDialog.setTitle("Registration");
            progressDialog.setMessage("Please Wait...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            mAuth.signInWithEmailAndPassword(inputEmail.getText().toString(), inputPassword.getText().toString()).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Login is Successful", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(LoginActivity.this, SetupActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                }else {
                    progressDialog.dismiss();
                    Toast.makeText(this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
