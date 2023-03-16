package com.adi.startoonlabs.signinup.mvvm;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import com.adi.startoonlabs.apputil.LoadingDialog;
import com.google.firebase.auth.PhoneAuthProvider;

public class LoginViewModel extends ViewModel {

    // repository
    LoginRepository loginRepository;

    // sign up error message and flag
    protected String signUpMessage;
    protected boolean signUpErrorFlag;

    // loading dialog
    protected LoadingDialog loadingDialog;

    public LoginViewModel(@NonNull Activity context){
        this.loginRepository = new LoginRepository();
        this.loadingDialog = LoadingDialog.create(context);
    }

    public LoadingDialog getLoadingDialog() {
        return loadingDialog;
    }

    // setting sign up error message and flag
    public void setSignUpError(boolean flag, String errorMessage){
        this.signUpMessage = errorMessage;
        this.signUpErrorFlag = flag;
    }

    public String getSignUpMessage() {
        return signUpMessage;
    }

    public boolean isSignUpErrorFlag() {
        return signUpErrorFlag;
    }

    public void verifyPhone(String phoneNumber, Activity activity, LogInInterface login, @Nullable PhoneAuthProvider.ForceResendingToken resendingToken){
        loginRepository.verifyPhone(phoneNumber, activity, login, resendingToken);
    }

    public void logInWithPhone(String verificationId, String otp, LoginRepository.LogInSuccess success, LoginRepository.LogInFailed fail){
        loginRepository.logInWithPhone(verificationId, otp, success, fail);
    }

    public void signOut(Context context){
        loginRepository.signOut(context);
    }

    public void isUser(String phoneNumber, LoginRepository.IsUserCallBack callBack){
        loginRepository.isAUser(phoneNumber, callBack);
    }

    public void signUp(String phoneNumber, String userName, LoginRepository.TaskListener<Boolean> taskListener){
        loginRepository.signUp(phoneNumber, userName, taskListener);
    }
}
