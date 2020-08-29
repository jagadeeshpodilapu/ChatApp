package com.example.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText inputEmail, inputPassword, inputConfirmPassword;

    private MaterialButton btnRegister;
    private TextView alredyAccount;

    FirebaseAuth mAuth;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        alredyAccount = findViewById(R.id.alredyAccount);

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        btnRegister.setOnClickListener(v -> userRegistration());

        alredyAccount.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), LoginActivity.class)));
    }

    private void userRegistration() {

        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();
        String confirmPassword = inputConfirmPassword.getText().toString();

        if (email.trim().isEmpty()) {
            inputEmail.setError("Please Enter Email");
            inputEmail.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputEmail.setError("Please Enter Valid Email");
            inputEmail.requestFocus();
        } else if (password.trim().isEmpty()) {
            inputPassword.setError("Please Enter Password");
        } else if (password.trim().length() < 6) {
            inputPassword.setError("Password Must be 6 letters");
            inputPassword.requestFocus();
        } else if (!password.equals(confirmPassword)) {
            inputConfirmPassword.setError("Password must should be match");
            inputConfirmPassword.requestFocus();

        } else {

            progressDialog.setTitle("Registration");
            progressDialog.setMessage("Please Wait...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "Registration is Successful", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(RegisterActivity.this, SetupActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            });

        }

    }
}
