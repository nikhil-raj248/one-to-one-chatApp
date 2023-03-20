package com.example.apptoconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.apptoconnect.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    MessagesAdapter messagesAdapter;
    ArrayList<Message> messages;

    String senderRoom,receiverRoom;

    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        messages=new ArrayList<>();
        messagesAdapter = new MessagesAdapter(this,messages);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(messagesAdapter);


        String name = getIntent().getStringExtra("name");
        String receiverUid = getIntent().getStringExtra("uid");
        String senderUid = FirebaseAuth.getInstance().getUid();

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        database=FirebaseDatabase.getInstance();

        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for(DataSnapshot d:snapshot.getChildren()){
                            Log.d("TAG", "onDataChange: "+d.getValue().getClass());

                            HashMap hashMap = (HashMap) d.getValue();

                            Log.d("TAG", "onDataChange1111: "+hashMap.get("message"));


                            Message message = new Message(hashMap.get("message").toString(),hashMap.get("senderId").toString(),Long.parseLong(hashMap.get("timestamp").toString()));
                            messages.add(message);
                            messagesAdapter.notifyDataSetChanged();
                            binding.recyclerView.scrollToPosition(messages.size() - 1);

                        }
//                        messagesAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        


        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageTxt = binding.messageBox.getText().toString();

                Log.d("TAG", "onClick: 1111122222");
                if(messageTxt.isEmpty())return;

                Date date=new Date();
                Message message=new Message(messageTxt,senderUid,date.getTime());
                binding.messageBox.setText("");

//                Log.d("TAG", "onClick:  "+message+"  "+message.getMessage()+"   "+messages.size());
//                messages.add(message);
//                messagesAdapter.notifyDataSetChanged();
//                Log.d("TAG", "onClick:  "+message+"  "+message.getMessage()+"   "+messages.size());
//


                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .push()
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                database.getReference().child("chats")
                                        .child(receiverRoom)
                                        .child("messages")
                                        .push()
                                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                            }
                                        });
                            }
                        });
            }
        });

        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}