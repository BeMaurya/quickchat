package com.bewithmaurya.quickmessage.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.bewithmaurya.quickmessage.databinding.ActivityForgotPasswordBinding;
import com.bewithmaurya.quickmessage.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListener();
    }

    private void setListener() {
        binding.buttonReset.setOnClickListener(v ->
                {
                    if(isValidEmail())
                    {
                        resetPassword();
                    }
                }
                );
        
    }

    private void resetPassword() {
        loading(true);
        String email = binding.inputEmail.getText().toString();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    showToast("Please Check Your Email!");
                    startActivity(new Intent(getApplicationContext(),SignInActivity.class));
                }
                else
                {
                    loading(false);
                    showToast("Unable To Send Email");
                }
            }
        });
    }
    private void loading(Boolean isLoading)
    {
        if(isLoading)
        {
            binding.buttonReset.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else
        {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonReset.setVisibility(View.VISIBLE);
        }
    }


    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private Boolean isValidEmail()
    {
        if(binding.inputEmail.getText().toString().trim().isEmpty())
        {
            showToast("Enter Email");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches())
        {
            showToast("Enter Valid Email");
            return false;
        }
        else
        {
            return true;
        }
    }
}