package com.simarjot.bookwala;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import in.aabhasjindal.otptextview.OTPListener;
import in.aabhasjindal.otptextview.OtpTextView;

public class OtpActivity extends AppCompatActivity {
    private String mobileNo;
    private FirebaseAuth mAuth;
    private String verificationCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private OtpTextView otpTextView;
    private TextView tvMobileNo;
    private String otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        ImageButton btnSignIn = findViewById(R.id.button2);
        otpTextView = findViewById(R.id.otp_view);
        tvMobileNo = findViewById(R.id.mobile_tv);

        otpTextView.setOtpListener(new OTPListener() {
            @Override
            public void onInteractionListener() {
                // fired when user types something in the Otpbox
            }

            @Override
            public void onOTPComplete(String otp) {
                OtpActivity.this.otp = otp;
            }
        });

        mobileNo = getIntent().getStringExtra(EnterPhoneNumberActivity.MOBILE_EXTRA);

        startFirebaseLogin();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobileNo,           // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks


        btnSignIn.setOnClickListener(v -> {
            if (otp == null || otp.length() < 6) {
                Toast.makeText(OtpActivity.this, "please enter the OTP", Toast.LENGTH_SHORT).show();
            } else {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, otp);
                Log.d("nerd", credential.getSmsCode());
                signInWithPhone(credential);
            }
        });
    }


    private void startFirebaseLogin() {

        mAuth = FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(OtpActivity.this, "verification failed", Toast.LENGTH_SHORT).show();
                Log.d(EnterPhoneNumberActivity.TAG, "verification failed", e);
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationCode = s;
                tvMobileNo.setText("OTP has been sent to you on " + getIntent().getStringExtra(EnterPhoneNumberActivity.MOBILE_EXTRA_FORMATTED));
                Toast.makeText(OtpActivity.this, "Code sent", Toast.LENGTH_SHORT).show();
                Log.d(EnterPhoneNumberActivity.TAG, verificationCode);
            }
        };
    }

    private void signInWithPhone(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(OtpActivity.this, "otp is correct", Toast.LENGTH_SHORT).show();
                        boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                        if (isNewUser) {
                            Intent registrationIntent = new Intent(OtpActivity.this, RegistrationActivity.class);
                            registrationIntent.putExtra(EnterPhoneNumberActivity.MOBILE_EXTRA, mobileNo);
                            startActivity(registrationIntent);
                        } else {
                            setResult(RESULT_OK);
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Incorrect OTP" +
                                "", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
            Log.d(EnterPhoneNumberActivity.TAG, "verification failed", e);
        });
    }
}
