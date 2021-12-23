package com.bewithmaurya.quickmessage.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bewithmaurya.quickmessage.databinding.ItemContainerReentConversionBinding;
import com.bewithmaurya.quickmessage.listeners.ConversionListener;
import com.bewithmaurya.quickmessage.models.ChatMessage;
import com.bewithmaurya.quickmessage.models.User;

import java.util.List;


public class RecentCoversationAdapter extends RecyclerView.Adapter<RecentCoversationAdapter.ConversionViewHolder>
{

    public final List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;
    public RecentCoversationAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener)
    {
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerReentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder{
        ItemContainerReentConversionBinding binding;
        ConversionViewHolder(ItemContainerReentConversionBinding itemContainerReentConversionBinding)
        {
            super(itemContainerReentConversionBinding.getRoot());
            binding = itemContainerReentConversionBinding;
        }
        void setData(ChatMessage chatMessage)
        {
            binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage));
            binding.textName.setText(chatMessage.conversionName);
            binding.textRecentMessage.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.id = chatMessage.conversionId;
                user.name = chatMessage.conversionName;
                user.image = chatMessage.conversionImage;
                conversionListener.onConversionClicked(user);
            });
        }
    }

    private Bitmap getConversionImage(String enccodedImage)
    {
        byte[] bytes = Base64.decode(enccodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
