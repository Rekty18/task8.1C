package com.example.chatbot;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private EditText messageEditText;
    private Button sendButton;
    private List<ChatMessage> chatMessages;
    private String username;
    private ChatService chatService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        username = getIntent().getStringExtra("username");

        TextView welcomeTextView = findViewById(R.id.welcomeTextView);
        welcomeTextView.setText("Welcome " + username + "!");

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setAdapter(chatAdapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        chatService = ApiClient.getClient().create(ChatService.class);

        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessageToChatbot(message);
                messageEditText.setText("");
            }
        });
    }

    private void sendMessageToChatbot(String userMessage) {
        chatMessages.add(new ChatMessage(username, userMessage));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);

        List<Map<String, String>> history = new ArrayList<>();
        for (int i = 0; i < chatMessages.size(); i++) {
            Map<String, String> chatPair = new HashMap<>();
            ChatMessage msg = chatMessages.get(i);
            chatPair.put("User", msg.getSender().equals(username) ? msg.getMessage() : "...");
            chatPair.put("Llama", msg.getSender().equals("ChatBot") ? msg.getMessage() : "...");
            history.add(chatPair);
        }

        ChatRequest request = new ChatRequest(userMessage, history);
        chatService.sendMessage(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResponse() != null) {
                    chatMessages.add(new ChatMessage("ChatBot", response.body().getResponse()));
                } else {
                    chatMessages.add(new ChatMessage("ChatBot", "No response from server"));
                    Log.e("ChatActivity", "Unsuccessful response ");
                }
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
            }


            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                Log.e("ChatActivity", "Error", t);
            }

        });
    }

}

