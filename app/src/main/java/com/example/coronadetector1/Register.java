package com.example.coronadetector1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Member;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class Register extends AppCompatActivity {

    EditText mFullName,mEmail,mPassword,mPhone;
    Button mRegisterBtn;
    TextView mLoginBtn,mID;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    DatabaseReference Ref;
    String mMAC;
    User user;
    int i = 0;    //To keep track of the members in the database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Here we map the values we got entered by the user into variables

        mFullName    = findViewById(R.id.FullName);
        mEmail       = findViewById(R.id.Email_Reg);
        mID          = findViewById(R.id.ID);
        mPassword    = findViewById(R.id.Password_Reg);
        mPhone       = findViewById(R.id.PhoneNumber);
        mRegisterBtn = findViewById(R.id.RegisterButton);
        mLoginBtn    = findViewById(R.id.LoginInRegistration);
        Ref          = FirebaseDatabase.getInstance().getReference().child("User"); //To store info into database
        fAuth        = FirebaseAuth.getInstance(); //This is used to authenticate the email and password we obtained using firebase
        progressBar  = findViewById(R.id.ProgressBar);
        user         = new User();

        if(fAuth.getCurrentUser() != null) { //If the user already has an account
            startActivity(new Intent(getApplicationContext(),MainActivity.class)); //send to main activity
            finish();
        }

        mRegisterBtn.setOnClickListener(new View.OnClickListener() { //When the user clicks on the register button
            @Override
            public void onClick(View v) {
                String email =  mEmail.getText().toString().trim(); //We use .tostring to convert from object to string and trim is used to format the data
                String password = mPassword.getText().toString().trim(); //Same as email

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

                // Register the user in firebase

                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {

                            // To get the MAC Address
                            try {
                                List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
                                for (NetworkInterface nif : all) {
                                    if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                                    byte[] macBytes = nif.getHardwareAddress();

                                    StringBuilder res1 = new StringBuilder();
                                    for (byte b : macBytes) {
                                        res1.append(Integer.toHexString(b & 0xFF) + ":");
                                    }

                                    if (res1.length() > 0) {
                                        res1.deleteCharAt(res1.length() - 1);
                                    }
                                   mMAC = res1.toString();
                                }
                            } catch (Exception ex) {
                            }
                            // To get the MAC Address
                            user.setmMAC(mMAC);
                            user.setmFullName(mFullName.getText().toString().trim());
                            user.setmEmail(mEmail.getText().toString().trim());
                            user.setmPassword(mPassword.getText().toString().trim());
                            user.setmPhone(Integer.parseInt(mPhone.getText().toString().trim()));
                            user.setmID(Integer.parseInt(mID.getText().toString().trim()));
                            Ref.child("User " + i).setValue(user);
                            i = i+1;
                            Toast.makeText(Register.this, "User Created!", Toast.LENGTH_SHORT).show(); // Message implying the user was created
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        }

                        else {

                            Toast.makeText(Register.this,"Error!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show(); // Message implying an error
                            progressBar.setVisibility(View.GONE);

                        }

                    }
                });

            }

        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() { //The Login Text Button
        @Override
            public void onClick(View V) {
                startActivity(new Intent(getApplicationContext(), Login.class)); // If the user clicks it, take them to the login Activity

        }

    });
    }
}
