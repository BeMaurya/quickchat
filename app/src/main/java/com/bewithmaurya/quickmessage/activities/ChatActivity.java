package com.bewithmaurya.quickmessage.activities;

import static com.bewithmaurya.quickmessage.utilities.Constants.*;

import androidx.annotation.NonNull;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.bewithmaurya.quickmessage.adapters.ChatAdapter;
import com.bewithmaurya.quickmessage.databinding.ActivityChatBinding;
import com.bewithmaurya.quickmessage.models.ChatMessage;
import com.bewithmaurya.quickmessage.models.User;
import com.bewithmaurya.quickmessage.network.ApiClient;
import com.bewithmaurya.quickmessage.network.ApiService;
import com.bewithmaurya.quickmessage.utilities.Constants;
import com.bewithmaurya.quickmessage.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {
    private ActivityChatBinding binding;
    private User recieverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversionId = null;
    private Boolean isReceiverAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadReceiverDetails();
        setListeners();
        init();
        listenMessage();
    }

    private void init()
    {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString(recieverUser.image),
                preferenceManager.getString(KEY_USER_ID));
        binding.chatRecycerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage()
    {
        HashMap<String, Object> message = new HashMap<>();
        message.put(KEY_SENDER_ID, preferenceManager.getString(KEY_USER_ID));
        message.put(KEY_RECEIVER_ID, recieverUser.id);
        message.put(KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(KEY_TIMESTAMP,new Date());
        database.collection(KEY_COLLECTION_CHAT).add(message);
        if(conversionId != null)
        {
            updateConversion(binding.inputMessage.getText().toString());
        }
        else
        {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(KEY_SENDER_ID, preferenceManager.getString(KEY_USER_ID));
            conversion.put(KEY_SENDER_NAME, preferenceManager.getString(KEY_NAME));
            conversion.put(KEY_SENDER_IMAGE, preferenceManager.getString(KEY_IMAGE));
            conversion.put(KEY_RECEIVER_ID, recieverUser.id);
            conversion.put(KEY_RECEIVER_NAME, recieverUser.name);
            conversion.put(KEY_RECEIVER_IMAGE, recieverUser.image);
            conversion.put(KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversion.put(KEY_TIMESTAMP, new Date());
            addConversion(conversion);
        }
        if(!isReceiverAvailable)
        {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(recieverUser.token);

                JSONObject data = new JSONObject();
                data.put(KEY_USER_ID, preferenceManager.getString(KEY_USER_ID));
                data.put(KEY_NAME, preferenceManager.getString(KEY_NAME));
                data.put(KEY_FCM_TOKEN, preferenceManager.getString(KEY_FCM_TOKEN));
                data.put(KEY_MESSAGE, binding.inputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(REMOTE_MSG_DATA, data);
                body.put(REMOTE_MSG_REGISTRATION_IDS, tokens);

                sendNotification(body.toString());


            }catch (Exception e)
            {
                showToast(e.getMessage());
            }
        }
        binding.inputMessage.setText(null);
    }

    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message,Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody)
    {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful())
                {
                    try {
                        if(response.body() != null)
                        {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if(responseJson.getInt("failure") == 1)
                            {
                                JSONObject error = (JSONObject) results.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }

                    }catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                    showToast("Notification Sent Successfully");
                }
                else
                {
                    showToast("Error: "+response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, Throwable t) {
                showToast(t.getMessage());

            }
        });
    }

    private void listenAvailabilityOfReceiver()
    {
        database.collection(KEY_COLLECTION_USERS).document(
                recieverUser.id
        ).addSnapshotListener(ChatActivity.this, (value, error) -> {
            if(error != null)
            {
                return;
            }
            if(value != null)
            {
                if(value.getLong(KEY_AVAILABILITY) != null)
                {
                    int availability = Objects.requireNonNull(
                            value.getLong(KEY_AVAILABILITY)
                    ).intValue();
                    isReceiverAvailable = availability == 1;
                }
                recieverUser.token = value.getString(KEY_FCM_TOKEN);
                if(recieverUser.image == null)
                {
                    recieverUser.image = value.getString(KEY_IMAGE);
                    chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(recieverUser.image));
                    chatAdapter.notifyItemRangeChanged(0, chatMessages.size());
                }
            }
            if(isReceiverAvailable)
            {
                binding.textAvailability.setVisibility(View.VISIBLE);
            }
            else
            {
                binding.textAvailability.setVisibility(View.GONE);
            }
        });
    }

    private void listenMessage()
    {
        database.collection(KEY_COLLECTION_CHAT)
                .whereEqualTo(KEY_SENDER_ID, preferenceManager.getString(KEY_USER_ID))
                .whereEqualTo(KEY_RECEIVER_ID, recieverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(KEY_COLLECTION_CHAT)
                .whereEqualTo(KEY_SENDER_ID, recieverUser.id)
                .whereEqualTo(KEY_RECEIVER_ID, preferenceManager.getString(KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null)
        {
            return;
        }
        if(value != null)
        {
            int count = chatMessages.size();
            for(DocumentChange documentChange : value.getDocumentChanges())
            {
                if(documentChange.getType() == DocumentChange.Type.ADDED)
                {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(KEY_SENDER_ID);
                    chatMessage.recieverId = documentChange.getDocument().getString(KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if(count == 0)
            {
                chatAdapter.notifyDataSetChanged();
            }
            else
            {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecycerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecycerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if(conversionId == null)
        {
            checkForConversion();
        }
    };
    private Bitmap getBitmapFromEncodedString(String encodedImage)
    {
        if(encodedImage != null)
        {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        else
        {
            return null;
        }
    }
    private void loadReceiverDetails()
    {
        recieverUser = (User) getIntent().getSerializableExtra(KEY_USER);
        binding.textName.setText(recieverUser.name);
    }
    private void setListeners()
    {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConversion(HashMap<String, Object> conversion)
    {
        database.collection(KEY_COLLECTION_CONVERSATION)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    private void updateConversion(String message)
    {
        DocumentReference documentReference =
                database.collection(KEY_COLLECTION_CONVERSATION).document(conversionId);
        documentReference.update(
                KEY_LAST_MESSAGE, message,
                KEY_TIMESTAMP, new Date()
        );
    }

    private void checkForConversion()
    {
        if(chatMessages.size() != 0)
        {
            checkForConversionRemotely(
                    preferenceManager.getString(KEY_USER_ID),
                    recieverUser.id
            );
            checkForConversionRemotely(
                    recieverUser.id,
                    preferenceManager.getString(KEY_USER_ID)
            );
        }
    }

    private void checkForConversionRemotely(String senderId, String recieverId)
    {
        database.collection(KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(KEY_SENDER_ID, senderId)
                .whereEqualTo(KEY_RECEIVER_ID, recieverId)
                .get()
                .addOnCompleteListener(conversionCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversionCompleteListener = task -> {
      if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0)
      {
          DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
          conversionId = documentSnapshot.getId();
      }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}