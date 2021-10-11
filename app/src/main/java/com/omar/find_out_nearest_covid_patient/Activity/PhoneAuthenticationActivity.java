package com.omar.find_out_nearest_covid_patient.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.omar.find_out_nearest_covid_patient.R;

import java.util.concurrent.TimeUnit;

public class PhoneAuthenticationActivity extends AppCompatActivity {

    private Button SendVerificationCodeButton,VerifyButton;
    private EditText InputPhoneNumber,InputVerificationCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog loadingBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_authentication);

        SendVerificationCodeButton=findViewById(R.id.send_ver_code_button);
        VerifyButton=findViewById(R.id.verify_button);
        InputPhoneNumber=findViewById(R.id.phone_number_input);
        InputVerificationCode=findViewById(R.id.verification_code_input);
        mAuth=FirebaseAuth.getInstance();
        loadingBar=new ProgressDialog(PhoneAuthenticationActivity.this);

        SendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneNumber=InputPhoneNumber.getText().toString();

                if (TextUtils.isEmpty(phoneNumber)){

                    Toast.makeText(PhoneAuthenticationActivity.this, "Please enter your phone number first..", Toast.LENGTH_SHORT).show();
                }
                else {

                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("please wait, while we are authenticating your phone...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    Toast.makeText(PhoneAuthenticationActivity.this, ""+phoneNumber, Toast.LENGTH_SHORT).show();

                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(mAuth)
                                    .setPhoneNumber(phoneNumber)       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(PhoneAuthenticationActivity.this)                 // Activity (for callback binding)
                                    .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);

                }
            }
        });

        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);
                String verificationCode=InputVerificationCode.getText().toString();

                if (TextUtils.isEmpty(verificationCode)){
                    Toast.makeText(PhoneAuthenticationActivity.this, "Please write verification code first...", Toast.LENGTH_SHORT).show();
                }
                else {

                    loadingBar.setTitle("Verification Code");
                    loadingBar.setMessage("please wait, while we are verifiying verification...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {


                Toast.makeText(PhoneAuthenticationActivity.this, "Invalid Phone Number, Please enter correct phone number with your country code...", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
                SendVerificationCodeButton.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);

                VerifyButton.setVisibility(View.INVISIBLE);
                InputVerificationCode.setVisibility(View.INVISIBLE);
                Log.w("TAG", "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                loadingBar.dismiss();

                mVerificationId = verificationId;
                mResendToken = token;

                Toast.makeText(PhoneAuthenticationActivity.this, "Code has been sent, please check and verify...", Toast.LENGTH_SHORT).show();

                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                VerifyButton.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);
            }
        };

    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(PhoneAuthenticationActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {


                            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(task.getResult().getUser().getPhoneNumber());
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    if (snapshot.exists()){

                                        int count= (int) snapshot.getChildrenCount();
                                        if (count>=3){
                                            loadingBar.dismiss();
                                            Toast.makeText(PhoneAuthenticationActivity.this, "Congratulations, you're logged in successfully...", Toast.LENGTH_SHORT).show();
                                            SendToMainActivity();
                                        }
                                        else {
                                            loadingBar.dismiss();
                                            Toast.makeText(PhoneAuthenticationActivity.this, "Congratulations, you're logged in successfully...", Toast.LENGTH_SHORT).show();
                                            SendToCreateActivity();
                                        }

                                    }
                                    else {
                                        loadingBar.dismiss();
                                        Toast.makeText(PhoneAuthenticationActivity.this, "Congratulations, you're logged in successfully...", Toast.LENGTH_SHORT).show();
                                        SendToCreateActivity();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                        } else {
                            String message=task.getException().toString();
                            Toast.makeText(PhoneAuthenticationActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void SendToCreateActivity() {
        Intent intent=new Intent(PhoneAuthenticationActivity.this,CreateAccountActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void SendToMainActivity() {

        Intent intent=new Intent(PhoneAuthenticationActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}