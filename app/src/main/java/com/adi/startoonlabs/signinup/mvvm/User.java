package com.adi.startoonlabs.signinup.mvvm;

import android.view.View;

public class User {
    public String phoneNumber, userName;

    public User(){
        //required
    }

    public User(String phoneNumber, String userName){
        this.phoneNumber = phoneNumber;
        this.userName = userName;
    }

    public int nameVisibility(){
        return userName == null? View.GONE:View.VISIBLE;
    }

    public int phoneVisibility(){
        return phoneNumber == null? View.GONE:View.VISIBLE;
    }
}
