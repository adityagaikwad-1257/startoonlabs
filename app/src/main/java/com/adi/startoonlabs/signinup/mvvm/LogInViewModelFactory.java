package com.adi.startoonlabs.signinup.mvvm;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class LogInViewModelFactory implements ViewModelProvider.Factory {
    private final Activity context;

    public LogInViewModelFactory(Activity context){
        this.context = context;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new LoginViewModel(context);
    }
}
