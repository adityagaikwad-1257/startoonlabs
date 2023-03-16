package com.adi.startoonlabs.signinup.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.adi.startoonlabs.R;
import com.adi.startoonlabs.apputil.AppUtil;
import com.adi.startoonlabs.apputil.ErrorDialog;
import com.adi.startoonlabs.apputil.LoadingDialog;
import com.adi.startoonlabs.databinding.SignInFragmentLayoutBinding;
import com.adi.startoonlabs.signinup.mvvm.LogInInterface;
import com.adi.startoonlabs.signinup.mvvm.LogInViewModelFactory;
import com.adi.startoonlabs.signinup.mvvm.LoginRepository;
import com.adi.startoonlabs.signinup.mvvm.LoginViewModel;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class SignInFragment extends Fragment implements LogInInterface, LoginRepository.IsUserCallBack {
    private static final String TAG = "aditya";

    // data binding object
    SignInFragmentLayoutBinding binding;

    // view model
    LoginViewModel loginViewModel;

    // loading dialog
    LoadingDialog loadingDialog;

    public SignInFragment(){
        // required empty constructor
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        loadingDialog.dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SignInFragmentLayoutBinding.inflate(inflater, container, false);

        initialize();

        clickEvents();

        return binding.getRoot();
    }

    private void initialize() {
        loginViewModel = new ViewModelProvider(requireActivity(), new LogInViewModelFactory(requireActivity())).get(LoginViewModel.class);

        loadingDialog = loginViewModel.getLoadingDialog();

        // removing error of sign up page <- default
        loginViewModel.setSignUpError(false, null);
    }

    private void clickEvents() {
        binding.signUpBtn.setOnClickListener(v -> navigate(R.id.action_signInFragment_to_signUpFragment, null));

        binding.signInBtn.setOnClickListener(v -> {
            if (verifyPhoneInput() && AppUtil.isConnectedToInternet(requireContext())){
                // showing loading dialog
                loadingDialog.show("Verifying user");
                // disabling sign in button
                binding.signInBtn.setEnabled(false);

                loginViewModel.isUser("+91"+binding.mobileNumber.getText().toString(), this);
            }
        });
    }

    private void sendOtp() {
        if (isAdded())
            loginViewModel.verifyPhone("+91"+binding.mobileNumber.getText(),
                requireActivity(), this, null);
    }

    private boolean verifyPhoneInput() {
        boolean allOkay = true;

        if (!binding.mobileNumber.getText().toString().matches(LogInInterface.PHONE_REGEX)){
            allOkay = false;
            binding.mobileNumber.setError("Invalid phone number");
        }

        return allOkay;
    }

    private void gotoOtpFrag(String phoneNumber, String verificationCode, PhoneAuthProvider.ForceResendingToken forceResendingToken){
        /*
            go to OTP fragment
         */
        Bundle args = new Bundle();
        args.putString(PHONE_KEY, phoneNumber); // passing phone number
        args.putString(OTP_VERIFICATION_CODE_KEY, verificationCode); // passing phone number
        args.putParcelable(RESEND_TOKEN_KEY , forceResendingToken); // force resend token

        navigate(R.id.action_signInFragment_to_otpVerificationFragment, args);
    }

    public void navigate(int action, Bundle args){
        if (isAdded())
            Navigation.findNavController(requireActivity(), R.id.fragmentContainerView)
                    .navigate(action, args);
        else {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
        Log.d(TAG, "onVerificationCompleted: ");
        loadingDialog.dismiss(); // removing loading dialog
    }

    @Override
    public void onVerificationFailed(@NonNull FirebaseException e) {
        Log.d(TAG, "onVerificationFailed: " + e.getMessage());
        loadingDialog.dismiss(); // removing loading dialog
        binding.signInBtn.setEnabled(true); // enabling back sign in button

        String error_message = e.getLocalizedMessage() == null?"":e.getLocalizedMessage();

        // showing error dialog
        ErrorDialog.create(requireContext()).show(R.raw.error,
                "SOMETHING WENT WRONG", error_message, "Ok", null);
    }

    @Override
    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken, String phoneNumber) {
        Log.d(TAG, "onCodeSent: " + s);
        loadingDialog.dismiss(); // removing loading dialog
        gotoOtpFrag(phoneNumber, s, forceResendingToken);
        binding.signInBtn.setEnabled(true); // enabling back sign in button
    }

    // checking if user exists in our database
    @Override
    public void isUser(boolean isUser) {
        Log.d(TAG, "clickEvents: " + isUser);
        if (isUser){
            // already a user

            // sending otp
            // changing loading dialog message
            loadingDialog.changeMessage("Sending otp");
            sendOtp();

        }else{
            // user need to sign up

            //dismissing loading dialog
            loadingDialog.dismiss();
            // enabling back sign in button
            binding.signInBtn.setEnabled(true);

            Bundle args = new Bundle();
            args.putString(PHONE_KEY, binding.mobileNumber.getText().toString());

            // navigating user ot sign up fragment and showing a toast
            navigate(R.id.action_signInFragment_to_signUpFragment, args);
            Toast.makeText(requireActivity(), "User doesn't exist, please sign up", Toast.LENGTH_SHORT).show();
        }
    }
}
