package com.adi.startoonlabs.signinup.mvvm;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class LoginRepository {

    // send otp
    public void verifyPhone(String phoneNumber, Activity activity, LogInInterface login, @Nullable PhoneAuthProvider.ForceResendingToken resendingToken){
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(activity)          // Activity (for callback binding)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                if (login != null) login.onVerificationCompleted(phoneAuthCredential);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                if (login != null) login.onVerificationFailed(e);
                            }

                            @Override
                            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                if (login != null) login.onCodeSent(s, forceResendingToken, phoneNumber);
                            }
                        })
                        .setForceResendingToken(resendingToken)// OnVerificationStateChangedCallbacks
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private static final String TAG = "aditya";

    // log in with phone
    public void logInWithPhone(String verificationId, String otp, LogInSuccess success, LogInFailed fail){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);

        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    Log.d(TAG, "logInWithPhone: successful log in ");

                    success.success(authResult);

                }).addOnFailureListener(e -> {
                    Log.d(TAG, "logInWithPhone: failed log in" + e.getLocalizedMessage());

                    fail.fail(e);

                });
    }

    public void signUp(String phoneNumber, String userName, TaskListener<Boolean> taskListener){
        // creating user model
        User user = new User(phoneNumber, userName);

        // saving user details to Firebase realtime database
        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(phoneNumber)
                .setValue(user)
                .addOnCompleteListener(task -> {
                    taskListener.callback(task.isSuccessful());
                });
    }

    public void signOut(Context context){
        FirebaseAuth.getInstance().signOut();
    }

    // checking if user already exists in firebase realtime database
    public void isAUser(String phoneNumber, IsUserCallBack callback){
        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        callback.isUser(snapshot.hasChild(phoneNumber));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @FunctionalInterface
    public interface IsUserCallBack{
        void isUser(boolean isUser);
    }

    @FunctionalInterface
    public interface TaskListener<T>{
        void callback(T t);
    }

    @FunctionalInterface
    public interface LogInSuccess{
        void success(AuthResult authResult);
    }

    @FunctionalInterface
    public interface LogInFailed{
        void fail(Exception e);
    }
}