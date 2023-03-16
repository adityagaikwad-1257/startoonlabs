package com.adi.startoonlabs.signinup.fragments;

import static com.adi.startoonlabs.apputil.AppUtil.isConnectedToInternet;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.adi.startoonlabs.dashboard.HomeActivity;
import com.adi.startoonlabs.databinding.OtpVerifyFragmentLayoutBinding;
import com.adi.startoonlabs.signinup.mvvm.LogInInterface;
import com.adi.startoonlabs.signinup.mvvm.LoginViewModel;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OtpVerificationFragment extends Fragment implements LogInInterface {
    private static final String TAG = "aditya";

    // data binding
    OtpVerifyFragmentLayoutBinding binding;

    // arguments
    String phoneNumber, verificationCode;
    PhoneAuthProvider.ForceResendingToken forceResendingToken;

    // loading dialog
    LoadingDialog loadingDialog;

    // Login view model
    LoginViewModel loginViewModel;

    public OtpVerificationFragment(){
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
        binding = OtpVerifyFragmentLayoutBinding.inflate(inflater, container, false);

        initialize();

        clickEvents();

        return binding.getRoot();
    }

    private void initialize() {
        // receiving arguments
        Bundle args = getArguments();
        if (args != null){
            phoneNumber = args.getString(PHONE_KEY);
            binding.phoneNumberTv.setText(phoneNumber);

            verificationCode = args.getString(OTP_VERIFICATION_CODE_KEY);
            forceResendingToken = (PhoneAuthProvider.ForceResendingToken) args.getParcelable(RESEND_TOKEN_KEY);
        }

        // initiating loginViewModel
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);

        // initiating loading dialog
        loadingDialog = loginViewModel.getLoadingDialog();

        // initiating otp boxes text change listeners
        setUpOtpBoxes();
    }

    private void clickEvents() {
        binding.backBtn.setOnClickListener(v -> navigateBack());
        
        binding.resendOtp.setOnClickListener(v -> resendOtp());
        
        binding.submitOtp.setOnClickListener(v -> {
            if (checkOtpBoxes() && isConnectedToInternet(requireContext())){
                loadingDialog.show("Verifying OTP");
                loginViewModel.logInWithPhone(verificationCode, getOtp(),
                        authResult -> {
                            // success callback

                            // dismissing loading dialog
                            loadingDialog.dismiss();

                            // going to home activity
                            if (isAdded()){
                                Intent intent = new Intent(requireActivity(), HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);

                                requireActivity().finish(); // finishing LogInActivity
                            }
                        },
                        exception -> {
                            // fail callback
                            // dismissing loading dialog
                            loadingDialog.dismiss();

                            // showing error
                            String title;
                            String message;

                            if (exception.getLocalizedMessage() != null){
                                if (exception.getLocalizedMessage().contains("invalid")){
                                    title = "Invalid OTP";
                                    message = "Please make sure you are entering the same OTP received via SMS.";
                                }else if(exception.getLocalizedMessage().contains("disabled") &&
                                        exception.getLocalizedMessage().contains("account")){
                                    title = "Account Disabled :(";
                                    message = "Your account at STARTOON LABS has been disabled by administrator.";
                                }else{
                                    title = "Something went wrong";
                                    message = exception.getLocalizedMessage();
                                }
                            }else{
                                title = "something went wrong";
                                message = "It's not you, It's us.\nKindly try again later";
                            }

                            ErrorDialog.create(requireContext()).show(R.raw.error,
                                    title, message, "ok", null);

                        });
            }
        });
    }

    public void navigateBack(){
        if (isAdded())
            Navigation.findNavController(requireActivity(), R.id.fragmentContainerView)
                    .popBackStack();
        else {
            loadingDialog.dismiss();
        }
    }

    public String getOtp(){
        String otp = "";

        otp += binding.otp1.getText();
        otp += binding.otp2.getText();
        otp += binding.otp3.getText();
        otp += binding.otp4.getText();
        otp += binding.otp5.getText();
        otp += binding.otp6.getText();

        return otp;
    }

    // check if all the otp boxes are filled
    private boolean checkOtpBoxes() {
        boolean allOkay = true;

        if (!binding.otp1.getText().toString().matches("[0-9]")) allOkay = false;
        else if (!binding.otp2.getText().toString().matches("[0-9]")) allOkay = false;
        else if (!binding.otp3.getText().toString().matches("[0-9]")) allOkay = false;
        else if (!binding.otp4.getText().toString().matches("[0-9]")) allOkay = false;
        else if (!binding.otp5.getText().toString().matches("[0-9]")) allOkay = false;
        else if (!binding.otp6.getText().toString().matches("[0-9]")) allOkay = false;

        if (!allOkay) Toast.makeText(requireContext(), "Enter valid OTP", Toast.LENGTH_SHORT).show();
        
        return allOkay;
    }

    private void resendOtp() {
        if (!isConnectedToInternet(requireContext())){
            // not connected to internet
            return;
        }

        loadingDialog.show("Resending OTP"); // showing loading view

        loginViewModel.verifyPhone(phoneNumber, getActivity(), this /*Call backs*/, forceResendingToken);
    }

    private void setUpOtpBoxes() {
        binding.otp1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) binding.otp2.requestFocus();
            }
        });

        binding.otp2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) binding.otp3.requestFocus();
                else binding.otp1.requestFocus();
            }
        });

        binding.otp3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) binding.otp4.requestFocus();
                else binding.otp2.requestFocus();
            }
        });

        binding.otp4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) binding.otp5.requestFocus();
                else binding.otp3.requestFocus();
            }
        });

        binding.otp5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) binding.otp6.requestFocus();
                else binding.otp4.requestFocus();
            }
        });

        binding.otp6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) AppUtil.closeKeyboard(requireActivity());
                else binding.otp5.requestFocus();
            }
        });
    }

    /*
        ( resend OTP ) verify phone number call backs
     */
    // called when OTP is resent successfully
    @Override
    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken, String phoneNumber) {
        Log.d(TAG, "onCodeSent: otp ");
        loadingDialog.showDone("OTP SENT");

        this.verificationCode = s; // updating verification id
        this.forceResendingToken = forceResendingToken; // updating token
    }

    // Google services automatically reads the incoming code and Auto-verifies, thus this function is called
    // This method is called after onCodeSent()
    @Override
    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
        Log.d(TAG, "onVerificationCompleted: otp");
        loadingDialog.dismiss(); // dismissing loading view
    }

    // called when resend OTP failed
    @Override
    public void onVerificationFailed(@NonNull FirebaseException e) {
        Log.d(TAG, "onVerificationFailed: " + e.getMessage());
        loadingDialog.dismiss(); // dismissing loading view

        String error_message = e.getLocalizedMessage() == null?"":e.getLocalizedMessage();

        // showing error dialog
        ErrorDialog.create(requireContext()).show(R.raw.error,
                "SOMETHING WENT WRONG", error_message, "Ok", null);
    }
}
