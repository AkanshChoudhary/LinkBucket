package com.example.linkbucket;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ShareHistoryScreen extends AppCompatActivity {
    Spinner spinner;
    EditText historySearchBar;
    List<HistoryItem> codeItems = new ArrayList<>();
    List<HistoryItem> toAppItems = new ArrayList<>();
    RecyclerView historyRecyclerView;
    String[] choices = {"Select History Type", "Code", "App to App"};
    TextView title;
    ImageView back, close;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_history_screen);
        back = findViewById(R.id.back);
        close = findViewById(R.id.closeHistoryView);
        title = findViewById(R.id.historyTitle);
        back.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MainUserDashboard.class)));
        spinner = findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, choices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        historySearchBar = findViewById(R.id.historySearchBar);
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        FirebaseFirestore.getInstance().collection("user+" + FirebaseAuth.getInstance().getCurrentUser().getUid()).document("pastShares")
                .collection("code").get().addOnCompleteListener(task -> {
            for (QueryDocumentSnapshot document : task.getResult()) {
                boolean isLink = true ;
                if (document.getString("type") == "links") {
                    isLink = true;
                } else if(document.getString("type").equals("Folders")){
                    isLink = false;
                }
                codeItems.add(new HistoryItem(document.getString("bunch_name"), "code: " + document.getString("code"), isLink));
            }
            FirebaseFirestore.getInstance().collection("user+" + FirebaseAuth.getInstance().getCurrentUser().getUid()).document("pastShares")
                    .collection("toApp").get().addOnCompleteListener(task1 -> {
                for (QueryDocumentSnapshot document : task1.getResult()) {
                    boolean isLink2 = true;
                    if ((List<String>) document.get("List") != null) {
                        isLink2 = true;
                    } else if ((List<String>) document.get("Folders") != null) {
                        isLink2 = false;
                    }
                    toAppItems.add(new HistoryItem(document.getString("bunch_name"), document.getString("sent to"), isLink2));
                    Toast.makeText(this, String.valueOf(isLink2), Toast.LENGTH_SHORT).show();
                }
            });
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    showCodeItems(codeItems);
                } else if (position == 2) {
                    showToAppItems(toAppItems);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void showCodeItems(List<HistoryItem> codeItems) {
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        HistoryRecyclerAdapter historyRecyclerAdapter = new HistoryRecyclerAdapter(codeItems);
        historyRecyclerView.setAdapter(historyRecyclerAdapter);
        historyRecyclerAdapter.setOnHistoryClickedListener(position -> {
            List<HistoryItem> newList = new ArrayList<>();
            HistoryRecyclerAdapter historyRecyclerAdapter2 = new HistoryRecyclerAdapter(newList);
            historyRecyclerView.setAdapter(historyRecyclerAdapter2);
            cardsViewMode(codeItems.get(position).getBunch_name());
            close.setOnClickListener(v -> {
                mainView();
                historyRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
                historyRecyclerView.setAdapter(historyRecyclerAdapter);
            });
            FirebaseFirestore.getInstance().collection("shareLinkPaths").document("path+" + codeItems.get(position).getInfo().substring(6))
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot doc) {
                            if(doc.getString("type").equals("links")){
                                List<String> myCards = (List<String>) doc.get("List");
                                ArrayList<RecyclerItem> cardList = new ArrayList<>();
                                FirebaseFirestore.getInstance().collection("user+" + FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .document("allSavedThings").collection("cards").get()
                                        .addOnCompleteListener(task -> {
                                            for (QueryDocumentSnapshot dc : task.getResult()) {
                                                if (myCards.contains(dc.getId())) {
                                                    cardList.add(new RecyclerItem(R.drawable.ic_plane, dc.getString("card_name"), dc.getString("card_desc"), dc.getString("link"), ShareHistoryScreen.this, dc.getLong("cardNumber"), false));
                                                }
                                            }
                                            RecyclerAdapter recyclerAdapter = new RecyclerAdapter(cardList);
                                            GridLayoutManager gridLayoutManager = new GridLayoutManager(ShareHistoryScreen.this, 2, RecyclerView.VERTICAL, false);
                                            historyRecyclerView.setLayoutManager(gridLayoutManager);
                                            historyRecyclerView.setAdapter(recyclerAdapter);
                                            recyclerAdapter.setOnCardClickedListener(link -> {
                                                Uri uri;
                                                if (link.contains("https://") || link.contains("http://")) {
                                                    uri = Uri.parse(link);
                                                } else {
                                                    uri = Uri.parse("https://" + link);
                                                }
                                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                startActivity(intent);
                                            });
                                        });
                            }else {
                                List<String> list1 = (List<String>) doc.get("Folders");
                                ArrayList<FolderRecyclerItem> folderList2 = new ArrayList<>();
                                for (int i = 0; i < list1.size(); i++) {
                                    folderList2.add(new FolderRecyclerItem(list1.get(i)));
                                }
                                FolderRecyclerAdapter adapter= new FolderRecyclerAdapter(folderList2);
                                GridLayoutManager gridLayoutManager = new GridLayoutManager(ShareHistoryScreen.this, 2, RecyclerView.VERTICAL, false);
                                historyRecyclerView.setLayoutManager(gridLayoutManager);
                                historyRecyclerView.setAdapter(adapter);
                                adapter.setOnFolderClickedListener(folderName ->{
                                    Intent intent= new Intent(ShareHistoryScreen.this,CardsOfCollection.class);
                                    intent.putExtra("folder_name",folderName);
                                    startActivity(intent);
                                });
                            }
                        }
                    });
        });
    }

    public void cardsViewMode(String heading) {
        spinner.setVisibility(View.GONE);
        title.setVisibility(View.VISIBLE);
        title.setText(heading);
        back.setVisibility(View.GONE);
        close.setVisibility(View.VISIBLE);
    }

    public void mainView() {
        spinner.setVisibility(View.VISIBLE);
        title.setVisibility(View.GONE);
        back.setVisibility(View.VISIBLE);
        close.setVisibility(View.GONE);
    }

    public void showToAppItems(List<HistoryItem> toAppItems) {
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        HistoryRecyclerAdapter historyRecyclerAdapter = new HistoryRecyclerAdapter(toAppItems);
        historyRecyclerView.setAdapter(historyRecyclerAdapter);
        historyRecyclerAdapter.setOnHistoryClickedListener(position -> {
            List<HistoryItem> newList = new ArrayList<>();
            HistoryRecyclerAdapter historyRecyclerAdapter2 = new HistoryRecyclerAdapter(newList);
            historyRecyclerView.setAdapter(historyRecyclerAdapter2);
            cardsViewMode(toAppItems.get(position).getBunch_name());
            close.setOnClickListener(v -> {
                mainView();
                historyRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
                historyRecyclerView.setAdapter(historyRecyclerAdapter);
            });
            FirebaseFirestore.getInstance().collection("user+" + FirebaseAuth.getInstance().getCurrentUser().getUid()).document("pastShares").collection("toApp")
                    .get()
                    .addOnCompleteListener(task -> {
                        for (QueryDocumentSnapshot dc : task.getResult()) {
                            if (toAppItems.get(position).getBunch_name().equals(dc.getString("bunch_name"))) {
                                if (toAppItems.get(position).isLink) {
                                    List<String> list = (List<String>) dc.get("List");
                                    ArrayList<RecyclerItem> cardList1 = new ArrayList<>();
                                    FirebaseFirestore.getInstance().collection("user+" + FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .document("allSavedThings").collection("cards").get()
                                            .addOnCompleteListener(task1 -> {
                                                for (QueryDocumentSnapshot dc1 : task1.getResult()) {
                                                    if (list.contains(dc1.getId())) {
                                                        cardList1.add(new RecyclerItem(R.drawable.ic_plane, dc1.getString("card_name"), dc1.getString("card_desc"), dc1.getString("link"), ShareHistoryScreen.this, dc1.getLong("cardNumber"), false));
                                                    }
                                                }
                                                RecyclerAdapter recyclerAdapter = new RecyclerAdapter(cardList1);
                                                GridLayoutManager gridLayoutManager = new GridLayoutManager(ShareHistoryScreen.this, 2, RecyclerView.VERTICAL, false);
                                                historyRecyclerView.setLayoutManager(gridLayoutManager);
                                                historyRecyclerView.setAdapter(recyclerAdapter);
                                                recyclerAdapter.setOnCardClickedListener(link -> {
                                                    Uri uri;
                                                    if (link.contains("https://") || link.contains("http://")) {
                                                        uri = Uri.parse(link);
                                                    } else {
                                                        uri = Uri.parse("https://" + link);
                                                    }
                                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                    startActivity(intent);
                                                });
                                            });
                                } else {
                                    List<String> list = (List<String>) dc.get("Folders");
                                    ArrayList<FolderRecyclerItem> folderList1 = new ArrayList<>();
                                    for (int i = 0; i < list.size(); i++) {
                                        folderList1.add(new FolderRecyclerItem(list.get(i)));
                                    }
                                    FolderRecyclerAdapter adapter= new FolderRecyclerAdapter(folderList1);
                                    GridLayoutManager gridLayoutManager = new GridLayoutManager(ShareHistoryScreen.this, 2, RecyclerView.VERTICAL, false);
                                    historyRecyclerView.setLayoutManager(gridLayoutManager);
                                    historyRecyclerView.setAdapter(adapter);
                                    adapter.setOnFolderClickedListener(folderName ->{
                                        Intent intent= new Intent(ShareHistoryScreen.this,CardsOfCollection.class);
                                        intent.putExtra("folder_name",folderName);
                                        startActivity(intent);
                                    });
                                }
                            }
                        }
                    });
        });
    }
}