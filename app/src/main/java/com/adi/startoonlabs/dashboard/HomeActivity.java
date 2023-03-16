package com.adi.startoonlabs.dashboard;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.adi.startoonlabs.databinding.ActivityHomeBinding;
import com.adi.startoonlabs.signinup.activities.SignInNUpActivity;
import com.adi.startoonlabs.signinup.mvvm.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    // data binding
    ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadData();

        binding.signOut.setOnClickListener(v -> signOut());

        binding.setUser(new User()); // default
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();

        // going back to LoginActivity
        Intent intent = new Intent(this, SignInNUpActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        // finishing current activity
        finish();
    }

    // loading user data
    private void loadData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getPhoneNumber() != null){
            FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(user.getPhoneNumber())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            binding.setUser(snapshot.getValue(User.class));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }
}