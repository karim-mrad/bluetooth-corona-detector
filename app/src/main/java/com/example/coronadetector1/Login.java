package com.example.coronadetector1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    EditText mEmail, mPassword;
    Button mLoginBtn;
    TextView mCreateBtn,mForgotPass;
    ProgressBar progressBar;
    FirebaseAuth fAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail = findViewById(R.id.Email_Login);
        mPassword = findViewById(R.id.Password_Login);
        progressBar = findViewById(R.id.ProgressBarLogin);
        mLoginBtn = findViewById(R.id.LoginButton);
        mCreateBtn = findViewById(R.id.RegisterInLogin);
        mForgotPass = findViewById(R.id.ForgotPassword);
        fAuth = FirebaseAuth.getInstance();

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mEmail.getText().toString().trim(); //We use .tostring to convert from object to string and trim is used to format the data
                String password = mPassword.getText().toString().trim();//Same as email

                if (TextUtils.isEmpty(email)) { //If the user did not enter a email he/she will receive an error
                    mEmail.setError("Email is Required");
                    return;
                }

                if (TextUtils.isEmpty(password)) { //IF the user did not enter a password he/she will receive an error
                    mPassword.setError("Password is Required");
                    return;
                }
                if (password.length() < 6) { //if the password is less than 6 characters, personal choice
                    mPassword.setError("Password must be greater than 6 characters.");
                }

                progressBar.setVisibility(View.VISIBLE); // Showing that registration is in progress, makes the progress bar visible

                //Authenticate User

                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) { //If the login is successful
                            Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } else {
                            Toast.makeText(Login.this, "Error!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }

                    }

                });

            }

        });

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Register.class));
            }

        });

        mForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText Mail = new EditText(v.getContext());
                AlertDialog.Builder PasswordResetDialog = new AlertDialog.Builder(v.getContext());
                PasswordResetDialog.setTitle("Reset Password? ");
                PasswordResetDialog.setMessage("Enter your email to receive a reset link.");
                PasswordResetDialog.setView(Mail);

                PasswordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() { //If the user wants to reset his/her password
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //extract the email and set reset link
                        String email = Mail.getText().toString();
                        fAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) { //Sucess method
                                Toast.makeText(Login.this, "A reset link has been sent to your email!", Toast.LENGTH_SHORT).show(); //If link is sent, display this message
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) { //Failure method
                                Toast.makeText(Login.this, "Error! The reset link could not be sent." + e.getMessage(), Toast.LENGTH_SHORT).show(); // if we could not send email display this message
                            }
                        });

                    }

                });

                PasswordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //close the dialog and go back to login view

                    }

                });

                PasswordResetDialog.create().show();

            }

        });

    }

}