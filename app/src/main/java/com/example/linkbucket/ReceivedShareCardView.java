package com.example.linkbucket;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReceivedShareCardView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_share_card_view);
        ImageView back=findViewById(R.id.backBtn3);
        back.setOnClickListener(v->startActivity(new Intent(getApplicationContext(),MainUserDashboard.class)));
        TextView title = findViewById(R.id.titleName);
        List<String> mainList = getIntent().getStringArrayListExtra("list");
        ArrayList<RecyclerItem> myList = new ArrayList<>();
        title.setText(getIntent().getStringExtra("title"));
        String userId = getIntent().getStringExtra("userId");
        RecyclerView receivedShareCardRecyclerView = findViewById(R.id.recyclerView2);
        RecyclerAdapter myAdapter = new RecyclerAdapter(myList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, RecyclerView.VERTICAL, false);
        receivedShareCardRecyclerView.setLayoutManager(gridLayoutManager);
        receivedShareCardRecyclerView.setAdapter(myAdapter);
        FirebaseFirestore.getInstance().collection("user+" + userId).document("allSavedThings").collection("cards")
                .get().addOnCompleteListener(task -> {
            int i = 0;
            for (QueryDocumentSnapshot dc : task.getResult()) {
                if (mainList.contains(dc.getId())) {
                    myList.add(new RecyclerItem(R.drawable.ic_plane, dc.getString("card_name"), dc.getString("card_desc"), dc.getString("link"), this, dc.getLong("cardNumber"),false));
                    myAdapter.notifyItemChanged(i);
                    i++;
                }
            }
        });
        myAdapter.setOnCardClickedListener(link -> {
            Uri uri;
            if (link.contains("https://") || link.contains("http://")) {
                uri = Uri.parse(link);
            } else {
                uri = Uri.parse("https://" + link);
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
    }
}