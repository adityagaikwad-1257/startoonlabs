package com.adi.startoonlabs.signinup.fragments;

import static com.adi.startoonlabs.signinup.mvvm.LogInInterface.PHONE_KEY;

import android.os.Bundle;
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
import com.adi.startoonlabs.apputil.LoadingDialog;
import com.adi.startoonlabs.databinding.SignUpFragmentLayoutBinding;
import com.adi.startoonlabs.signinup.mvvm.LogInInterface;
import com.adi.startoonlabs.signinup.mvvm.LoginViewModel;

public class SignUpFragment extends Fragment {
    private static final String TAG = "aditya";

    // binding
    SignUpFragmentLayoutBinding binding;

    // view model
    LoginViewModel loginViewModel;

    // loading dialog
    LoadingDialog loadingDialog;

    public SignUpFragment(){
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
        binding = SignUpFragmentLayoutBinding.inflate(inflater, container, false);

        initialize();

        clickEvents();

        return binding.getRoot();
    }

    private void initialize() {
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);

        loadingDialog = loginViewModel.getLoadingDialog();

        // showing error message through view model
        if (loginViewModel.isSignUpErrorFlag()){
            binding.errorMsg.setVisibility(View.VISIBLE);
            binding.errorMsg.setText(loginViewModel.getSignUpMessage());
        }else{
            binding.errorMsg.setVisibility(View.GONE);
        }

        // receiving phone number extra with the arguments
        Bundle args = getArguments();
        if (args != null){
            binding.mobileNumber.setText(args.getString(PHONE_KEY));
        }
    }

    private void clickEvents() {
        binding.backBtn.setOnClickListener(v -> navigateBack());

        binding.signUpBtn.setOnClickListener(v -> {
            if (verifyUserInput() && AppUtil.isConnectedToInternet(requireContext())){

                // showing loading dialog
                loadingDialog.show("Signing you up");
                // disabling sign up button
                binding.signUpBtn.setEnabled(false);

                loginViewModel.isUser("+91"+binding.mobileNumber.getText(),
                        isUser -> {

                            if (isUser){
                                // already a user

                                // removing loading dialog
                                loadingDialog.dismiss();
                                // enabling back sign up button
                                binding.signUpBtn.setEnabled(true);

                                // showing error
                                setErrorMessage(true, getString(R.string.user_already_exist_error), View.VISIBLE);
                            }else{
                                // creating new user
                                // removing error message
                                loginViewModel.setSignUpError(false, null);
                                binding.errorMsg.setVisibility(View.GONE);

                                // creating a new user
                                loginViewModel.signUp("+91"+binding.mobileNumber.getText(), 
                                        binding.userName.getText().toString(),
                                        isSuccess -> {
                                            // removing loading dialog
                                            loadingDialog.dismiss();
                                            // enabling back sign up button
                                            binding.signUpBtn.setEnabled(true);
                                            
                                            if (isSuccess){
                                                // signUp successful
                                                // moving back to sign in page

                                                navigateBack();
                                            }else{
                                                // signUp unsuccessful
                                                
                                                // showing error
                                                setErrorMessage(true, getString(R.string.sign_up_error_msg), View.VISIBLE);
                                            }
                                        });
                            }
                        });
            }
        });
    }

    public void navigateBack(){
        if (isAdded()) {
            Navigation.findNavController(requireActivity(), R.id.fragmentContainerView)
                    .popBackStack();
            Toast.makeText(getActivity(), "Sign up successful.", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingDialog.dismiss();
        }
    }

    public void setErrorMessage(boolean flag, String errorMessage, int visibility){
        loginViewModel.setSignUpError(flag, errorMessage);

        binding.errorMsg.setVisibility(visibility);
        binding.errorMsg.setText(errorMessage);
    }

    private boolean verifyUserInput(){
        boolean allOkay = true;

        if (binding.userName.getText().toString().trim().isEmpty()){
            // invalid user name
            allOkay = false;
            binding.userName.setError("User name required");
        }

        if (!binding.mobileNumber.getText().toString().matches(LogInInterface.PHONE_REGEX)){
            // invalid mobile number
            allOkay = false;
            binding.mobileNumber.setError("Invalid phone number");
        }

        return allOkay;
    }
}
