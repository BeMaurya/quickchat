package com.bewithmaurya.quickmessage.activities;

import static com.bewithmaurya.quickmessage.utilities.Constants.KEY_COLLECTION_USERS;
import static com.bewithmaurya.quickmessage.utilities.Constants.KEY_EMAIL;
import static com.bewithmaurya.quickmessage.utilities.Constants.KEY_IMAGE;
import static com.bewithmaurya.quickmessage.utilities.Constants.KEY_IS_SIGNED_IN;
import static com.bewithmaurya.quickmessage.utilities.Constants.KEY_NAME;
import static com.bewithmaurya.quickmessage.utilities.Constants.KEY_PASSWORD;
import static com.bewithmaurya.quickmessage.utilities.Constants.KEY_USER_ID;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bewithmaurya.quickmessage.databinding.ActivitySignUpBinding;
import com.bewithmaurya.quickmessage.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }
    private void setListeners()
    {
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
        binding.buttonSignUp.setOnClickListener(v ->{
            if(isValidsignUpDetails())
            {
                signUp();
            }
        });
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void showTost(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void signUp()
    {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(KEY_NAME, binding.inputName.getText().toString());
        user.put(KEY_EMAIL, binding.inputEmail.getText().toString());
        user.put(KEY_PASSWORD, binding.inputPassword.getText().toString());
        user.put(KEY_IMAGE, encodedImage);
        database.collection(KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
           loading(false);
           preferenceManager.putBoolean(KEY_IS_SIGNED_IN, true);
           preferenceManager.putString(KEY_USER_ID, documentReference.getId());
           preferenceManager.putString(KEY_NAME, binding.inputName.getText().toString());
           preferenceManager.putString(KEY_IMAGE, encodedImage);
           Intent intent = new Intent(getApplicationContext(),MainActivity.class);
           intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
           startActivity(intent);
        })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showTost(exception.getMessage());
                });
    }
    private String encodeImage(Bitmap bitmap)
    {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
    result ->{
                if(result.getResultCode() == RESULT_OK)
                {
                  if(result.getData() != null)
                  {
                      Uri imageUri = result.getData().getData();
                      try
                      {
                          InputStream inputStream = getContentResolver().openInputStream(imageUri);
                          Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                          binding.imageProfile.setImageBitmap(bitmap);
                          binding.textAddImage.setVisibility(View.GONE);
                          encodedImage = encodeImage(bitmap);
                      }
                      catch(FileNotFoundException e)
                      {
                          e.printStackTrace();
                      }

                  }
                }
    });
    private Boolean isValidsignUpDetails()
    {
        if(encodedImage == null){
            showTost("Select profile Image");
            return false;
        }
        else if(binding.inputName.getText().toString().trim().isEmpty())
        {
            showTost("Enter Name");
            return false;
        }
        else if(binding.inputEmail.getText().toString().trim().isEmpty())
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
        else if(binding.inputConformPassword.getText().toString().trim().isEmpty())
        {
            showTost("Confirm Your Password");
            return false;
        }
        else if(binding.inputPassword.getText().toString().equals(binding.inputConformPassword.getText().toString().trim().isEmpty()))
        {
            showTost("Password & Confirm Pasword must be same");
            return false;
        }
        else
        {
            return true;
        }
    }
    private void loading(Boolean isLoading)
    {
        if(isLoading)
        {
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else
        {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
        }
    }
}