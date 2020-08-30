package com.example.linkbucket;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardsOfCollection extends AppCompatActivity {
    ArrayList<RecyclerItem> collectionCardList;
    Toolbar toolbar;
    RecyclerView collectionCardRecyclerView;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId,folderName;
    RecyclerAdapter collectionCardRecyclerAdapter;
    EditText collectionCardSearchBar;
    static int selectedCount;
    ImageView search, cancelSearch,selectAllCards,deselectAllCards,closeOptions;
    TextView title;
    static List<String> tempList = new ArrayList<>();
    LinearLayout collectionCardSearchTab;
    ImageView share;
    RecyclerView collectionAndFriendRecyclerView;
    ImageView backBtn2;
    final  List<String> selectedCardNames = new ArrayList<>();
    final ArrayList<CollectionAndFriendRecyclerItem> collectionAndFriendRecyclerList = new ArrayList<>();
    CollectionAndFriendRecyclerAdapter collectionAndFriendRecyclerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cards_of_collection);
        toolbar = findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        collectionCardRecyclerView = findViewById(R.id.collectionCardsRecyclerView);
        Intent intent = getIntent();
        closeOptions=findViewById(R.id.closeOptions);
        folderName = intent.getStringExtra("folder_name");
        title= findViewById(R.id.folderNameTitle);
        userId = fAuth.getCurrentUser().getUid();
        selectAllCards=findViewById(R.id.selectAllCards);
        deselectAllCards=findViewById(R.id.deselectAllCards);
        collectionCardList = new ArrayList<>();
         backBtn2= findViewById(R.id.backBtn2);
        share = findViewById(R.id.shareCollectionCard);
        collectionCardSearchBar = findViewById(R.id.collectionCardSearchBar);
        collectionCardSearchTab=findViewById(R.id.collectionCardSearchTab);
        search = findViewById(R.id.collectionCardSearch);
        closeOptions=findViewById(R.id.closeOptions);
        cancelSearch = findViewById(R.id.cancelCardSearch);
        selectedCount = 0;
        backBtn2.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), CollectionActivity.class)));
        title.setText(intent.getStringExtra("folder_name"));
        collectionCardRecyclerAdapter = new RecyclerAdapter(collectionCardList, this);
        fStore.collection("user+" + userId).document("allSavedThings").collection("Collections")
                .orderBy("upload_time", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        if (doc.getId().equals(folderName)) {
                            tempList = (List<String>) doc.get("Items");
                        }
                    }
                    showCards(tempList);
                });

        collectionCardRecyclerAdapter.setOnCardSelectedListener(cardNumber ->
        {
            if (selectedCount == 0) {
                title.setText(folderName);
                showMainToolbar();
            } else {
                showOptionsToolbar();
                selectDeselectCard(cardNumber);
            }
        });

        collectionCardRecyclerAdapter.setOnCardClickedListener(link -> {
            if (selectedCount == 0) {
                Uri uri;
                if (link.contains("https://") || link.contains("http://")) {
                    uri = Uri.parse(link);
                } else {
                    uri = Uri.parse("https://" + link);
                }
                Intent intent2 = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent2);
            }
        });

        search.setOnClickListener(v ->
        {
           collectionCardSearchTab.setVisibility(View.VISIBLE);
        });

        cancelSearch.setOnClickListener(v -> {
            collectionCardSearchTab.setVisibility(View.GONE);
            collectionCardSearchBar.setText("");
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        });

        collectionCardSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        selectAllCards.setOnClickListener(v -> {
            selectAll();
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        });

        deselectAllCards.setOnClickListener(v -> {
           deselectAll();
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        });

        closeOptions.setOnClickListener(v -> {
            for(RecyclerItem collectionCard:collectionCardList)
            {
                collectionCard.setFlag(-1);
            }
            collectionCardRecyclerAdapter.filterNewList(collectionCardList);
            collectionCardSearchBar.setText("");
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
            showMainToolbar();
        });
        share.setOnClickListener(v->{
            Dialog dialog = new Dialog(CardsOfCollection.this, R.style.DialogTheme);
            dialog.setContentView(R.layout.sharing_option);
            dialog.create();
            dialog.show();
            dialog.findViewById(R.id.closeShareDialog).setOnClickListener(v2 -> dialog.dismiss());
            dialog.findViewById(R.id.linkShare).setOnClickListener(v1 -> {
                dialog.dismiss();
                Dialog dialog1 = new Dialog(CardsOfCollection.this, R.style.DialogTheme);
                dialog1.setContentView(R.layout.change_detail_dialog);
                Button next = dialog1.findViewById(R.id.saveNewDetail);
                EditText bunchName = dialog1.findViewById(R.id.newDetail);
                bunchName.setHint("Enter a name for the bunch of links to be shared.");
                next.setText("Next");
                dialog1.create();
                dialog1.show();
                next.setOnClickListener(v2 -> {
                    fStore.collection("user+" + userId).document("Id").get()
                            .addOnSuccessListener(documentSnapshot -> {
                                String userName = documentSnapshot.getString("name");
                                createShareLink(userName, bunchName.getText().toString());
                                dialog1.dismiss();
                            });
                });
            });
           dialog.findViewById(R.id.apptoapp).setOnClickListener(v1->{
               dialog.dismiss();
               Dialog dialog2 = new Dialog(this, R.style.DialogTheme);
               dialog2.setContentView(R.layout.collection_selection_option);
               EditText search = dialog2.findViewById(R.id.searchFolderName);
               search.setHint("Loading...");
               collectionAndFriendRecyclerView = dialog2.findViewById(R.id.collectionNameRecyclerView);
               fStore.collection("usersList").get().addOnCompleteListener(task ->
               {
                   int i = 0;
                   for (QueryDocumentSnapshot document : task.getResult()) {
                       collectionAndFriendRecyclerList.add(new CollectionAndFriendRecyclerItem(R.drawable.ic_person, document.getString("name") + "(" + document.getString("personalId") + ")", document.getId()));
                       collectionAndFriendRecyclerAdapter.notifyItemChanged(i);
                       i++;
                       search.setHint("Search Users");
                   }
               });
               collectionAndFriendRecyclerAdapter = new CollectionAndFriendRecyclerAdapter(collectionAndFriendRecyclerList);
               collectionAndFriendRecyclerView.setAdapter(collectionAndFriendRecyclerAdapter);
               collectionAndFriendRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
               dialog2.show();
               dialog2.setCanceledOnTouchOutside(false);
               dialog2.setOnDismissListener(dialog1 -> {
                   collectionAndFriendRecyclerList.clear();
               });
               ImageView cancel = dialog2.findViewById(R.id.cancelFolderMaking);
               cancel.setOnClickListener(v3 -> {
                   collectionAndFriendRecyclerList.clear();
                   dialog2.dismiss();
               });
               search.addTextChangedListener(new TextWatcher() {
                   @Override
                   public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                   }

                   @Override
                   public void onTextChanged(CharSequence s, int start, int before, int count) {
                   }

                   @Override
                   public void afterTextChanged(Editable s) {
                       filterFolderName(s.toString());
                   }
               });
               collectionAndFriendRecyclerAdapter.setOnFolderClickedListener((folderName, userId2) -> {
                   dialog2.dismiss();
                   String name = folderName.substring(0, folderName.length() - 9);
                   String personalId = folderName.substring(folderName.length() - 8);
                   Dialog dialog1 = new Dialog(CardsOfCollection.this, R.style.DialogTheme);
                   dialog1.setContentView(R.layout.change_detail_dialog);
                   EditText title = dialog1.findViewById(R.id.newDetail);
                   title.setText("Share selected links with " + name + " ?");
                   title.setTextColor(getResources().getColor(android.R.color.black));
                   title.setBackgroundColor(getResources().getColor(R.color.colorMainYellow));
                   Button share = dialog1.findViewById(R.id.saveNewDetail);
                   share.setText("Next");
                   dialog1.create();
                   dialog1.show();
                   title.setEnabled(false);
                   dialog1.findViewById(R.id.saveNewDetail).setOnClickListener(v2 -> {
                       dialog1.dismiss();
                       Dialog dialog3 = new Dialog(CardsOfCollection.this, R.style.DialogTheme);
                       dialog3.setContentView(R.layout.change_detail_dialog);
                       Button next = dialog3.findViewById(R.id.saveNewDetail);
                       EditText bunchName = dialog3.findViewById(R.id.newDetail);
                       bunchName.setHint("Enter a name for the bunch of links to be shared.");
                       next.setText("Share");
                       dialog3.create();
                       dialog3.show();
                       next.setOnClickListener(v3 -> {
                           dialog3.dismiss();
                           shareTheCards(selectedCardNames, userId2, name, MainUserDashboard.userName, bunchName.getText().toString());
                       });
                   });
               });
           });
        });
    }

    public void createShareLink(String userName, String bunchName) {
        String path = MainUserDashboard.generateRandomShareId();
        Map<String, Object> newPathMap = new HashMap<>();
        newPathMap.put("Sender", userName);
        newPathMap.put("user Id", userId);
        newPathMap.put("type", "links");
        newPathMap.put("List", selectedCardNames);
        newPathMap.put("bunch_name", bunchName);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Map<String, Object> historyMap = new HashMap<>();
        historyMap.put("code", path);
        historyMap.put("bunch_name", bunchName);
        historyMap.put("share_time", timestamp.getTime());
        historyMap.put("type", "links");
        fStore.collection("shareLinkPaths").document("path+" + path).set(newPathMap)
                .addOnSuccessListener(aVoid -> {
                    fStore.collection("user+" + userId).document("pastShares").collection("code").document().set(historyMap)
                            .addOnSuccessListener(aVoid1 -> {
                                showGeneratedLink(userName, path);
                                deselectAll();
                                showMainToolbar();
                            });
                });
    }
    public void showGeneratedLink(String userName, String path) {
        Dialog dialog = new Dialog(CardsOfCollection.this, R.style.DialogTheme);
        dialog.setContentView(R.layout.code_show);
        TextView link = dialog.findViewById(R.id.linkView);
        link.setText(path);
        dialog.create();
        dialog.show();
        dialog.findViewById(R.id.closeLink).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.shareLink).setOnClickListener(v -> {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, userName + " has shared some links!\ncopy the code below and visit linkbucket.in to view the shared links" + "\ncode:" + path);
            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        });
    }
    public void filterFolderName(String text) {
        ArrayList<CollectionAndFriendRecyclerItem> filterNameList = new ArrayList<>();
        for (CollectionAndFriendRecyclerItem item : collectionAndFriendRecyclerList) {
            if (item.getTitle().toLowerCase().contains(text.toLowerCase())) {
                filterNameList.add(item);
            }
        }
        collectionAndFriendRecyclerAdapter.changeList(filterNameList);
    }
    public void filter(String text) {
        ArrayList<RecyclerItem> filterList = new ArrayList<>();
        for (RecyclerItem item : collectionCardList) {
            if (item.getCardName().toLowerCase().contains(text.toLowerCase())) {
                filterList.add(item);
            }
        }
        collectionCardRecyclerAdapter.filterNewList(filterList);
    }

    public  void selectAll()
    {
        collectionCardSearchBar.setText("");
        for(RecyclerItem collectionCard:collectionCardList)
        {
            collectionCard.setFlag(1);
        }
        selectedCount=tempList.size();
        title.setText(selectedCount+" Selected");
        collectionCardRecyclerAdapter.filterNewList(collectionCardList);
    }

    public void deselectAll()
    {
        collectionCardSearchBar.setText("");
        showMainToolbar();
        for(RecyclerItem collectionCard:collectionCardList)
        {
            collectionCard.setFlag(-1);
        }
        selectedCount=0;
        collectionCardRecyclerAdapter.filterNewList(collectionCardList);
    }

    public void selectDeselectCard(long cardNumber)
    {
        int position = 0;
        int i = 0;
        for (RecyclerItem recyclerItem : collectionCardList ) {
            if (recyclerItem.getCardNumber() == cardNumber) {
                position = i;
            }
            i++;
        }
        if (collectionCardList.get(position).getState()) {
            selectedCardNames.add("card " + collectionCardList.get(position).getCardNumber());
            Toast.makeText(this, "Selected Card At Position" + position, Toast.LENGTH_SHORT).show();
        } else {
            selectedCardNames.remove("card " + collectionCardList.get(position).getCardNumber());
            Toast.makeText(this, "Deselected Card At Position" + position, Toast.LENGTH_SHORT).show();
        }
    }




    public void showCards(List<String> cardList) {
        fStore.collection("user+" + userId).document("allSavedThings").collection("cards")
                .orderBy("upload_time", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task ->
                {
                    int i = 0;
                    for (QueryDocumentSnapshot doc2 : task.getResult()) {
                        if (cardList.contains(doc2.getId())) {
                            collectionCardList.add(new RecyclerItem(R.drawable.ic_plane, doc2.getString("card_name"), doc2.getString("card_desc"), doc2.getString("link"), CardsOfCollection.this,i,false));
                            collectionCardRecyclerAdapter.notifyItemChanged(i);
                            i++;
                        }
                    }
                });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, RecyclerView.VERTICAL, false);
        collectionCardRecyclerView.setLayoutManager(gridLayoutManager);
        collectionCardRecyclerView.setAdapter(collectionCardRecyclerAdapter);
    }

    public void changeCount(String op) {
        if (op.equals("plus")) {
            selectedCount++;
        } else if (op.equals("minus")) {
            --selectedCount;
        }
    }

    public void showMainToolbar()
    {
        title.setText(folderName);
        selectAllCards.setVisibility(View.INVISIBLE);
        deselectAllCards.setVisibility(View.INVISIBLE);
        share.setVisibility(View.INVISIBLE);
        closeOptions.setVisibility(View.INVISIBLE);
        backBtn2.setVisibility(View.VISIBLE);
        selectedCount=0;
    }
    public void showOptionsToolbar()
    {
        title.setText(selectedCount+" Selected");
        selectAllCards.setVisibility(View.VISIBLE);
        deselectAllCards.setVisibility(View.VISIBLE);
        share.setVisibility(View.VISIBLE);
        closeOptions.setVisibility(View.VISIBLE);
        backBtn2.setVisibility(View.INVISIBLE);
    }
    public void shareTheCards(List<String> selectedCards, String receiverUserId, String receiverName, String senderName, String bunchName) {
        Map<String, Object> shareMap = new HashMap<>();
        java.sql.Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        shareMap.put("Sender", senderName);
        shareMap.put("List", selectedCards);
        shareMap.put("status", 0);
        shareMap.put("senderUserId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        shareMap.put("timeStamp", timestamp.getTime());
        shareMap.put("bunch_name", bunchName);
        Map<String, Object> saveHistoryMap = new HashMap<>();
        saveHistoryMap.put("sent to", receiverName);
        saveHistoryMap.put("receiverUserId", receiverUserId);
        saveHistoryMap.put("List", selectedCards);
        saveHistoryMap.put("bunch_name", bunchName);
        FirebaseFirestore.getInstance().collection("user+" + receiverUserId).document("receivedShares")
                .collection("AppToApp").document().set(shareMap).addOnSuccessListener(aVoid -> {
            FirebaseFirestore.getInstance().collection("user+" + FirebaseAuth.getInstance().getCurrentUser().getUid()).document("pastShares")
                    .collection("toApp").document().set(saveHistoryMap)
                    .addOnSuccessListener(aVoid1 -> {
                        Toast.makeText(this, "All done", Toast.LENGTH_SHORT).show();
                        deselectAll();
                        showMainToolbar();
                    });
        });
    }
}