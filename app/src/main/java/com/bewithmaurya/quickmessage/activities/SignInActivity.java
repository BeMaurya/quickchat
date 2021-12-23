package com.bewithmaurya.quickmessage.activities;

import androidx.annotation.IntRange;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.bewithmaurya.quickmessage.R;
import com.bewithmaurya.quickmessage.databinding.ActivitySignInBinding;
import com.bewithmaurya.quickmessage.utilities.Constants;
import com.bewithmaurya.quickmessage.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {
    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN))
        {
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }
    private void setListeners()
    {
            binding.textCreateNewAccount.setOnClickListener(v ->
                    startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
            binding.textForgotPassword.setOnClickListener(v ->
                    startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class)));
            binding.buttonSignIn.setOnClickListener(v -> {
                if(isValidsignInDetails())
                {
                    signIn();
                }
            });
    }
    private void signIn()
    {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get().addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0)
                    {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent  = new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    else
                    {
                        loading(false);
                        showTost("Unable To Sign In");
                    }
        });
    }
    private void loading(Boolean isLoading)
    {
        if(isLoading)
        {
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else
        {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignIn.setVisibility(View.VISIBLE);
        }
    }
    private void showTost(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private Boolean isValidsignInDetails()
    {
        if(binding.inputEmail.getText().toString().trim().isEmpty())
        {
            showTost("Enter Email");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches())
        {
            showTost("Enter Valid Email");
            return false;
        } else if(binding.inputPassword.getText().toString().trim().isEmpty())
        {
            showTost("Enter Password");
            return false;
        }
        else
        {
            return true;
        }
    }
}