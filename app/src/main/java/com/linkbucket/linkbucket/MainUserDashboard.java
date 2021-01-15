package com.linkbucket.linkbucket;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainUserDashboard extends AppCompatActivity {
    /*
      usersList->
        (c)      <userId>->
                    (d)    name
                           personalId
                     .
                     .
                     .
                     .
     shareLinkPaths->
        (c)          path+<some alpha num string>->
                             (d)                   bunch name,
                                                   sender user id,
                                                   type: link/folder
                                                   List<having only names of cards/folders>(data accessed from user+userId part)
     user+uid->
        (c)     pastShares->
                    (d)          code->
                                  (c)
                                          <shared code 1> ->
                                               (d)          bunch name.
                                          <shared code 2> ->
                                                            bunch name.
                                                  .
                                                  .
                                                  .
                                 toApp->
                                  (c)
                                        <any document name> ->
                                               (d)              sent to
                                                                bunch name/folder name
                                        <any document name> ->
                                               (d)         sent to
                                                           bunch name/folder name
                                                 .
                                                 .
                                                 .
                allSavedThings->
                          (d)             cards->
                                             (c)   card1->
                                                      (d)  Name
                                                           Link
                                                           desc
                                                           timestamp
                                                    card2
                                                    card3
                                                     .
                                                     .
                                                     .
                                       all collections->
                                            (c)     collection name1
                                                        (d)
                                                    collection name2
                                                    collection name3
                                                        .
                                                        .
                                                        .
                       Id->
                       (d)    name
                              personalId

                  */
    /*
    * */
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    ImageView openDrawer, profile;
    NavigationView navigationView;
    ArrayList<RecyclerItem> itemList;
    RecyclerView recyclerView;
    FirebaseFirestore firebaseFirestore;
    String userId;
    FirebaseAuth firebaseAuth;
    RecyclerAdapter recyclerAdapter;
    public static int selectedCount;
    TextView countView;
    LinearLayout searchLayout;
    ImageView right;
    ImageView selectAll;
    final List<String> selectedCardNames = new ArrayList<>();
    static final ArrayList<CollectionAndFriendRecyclerItem> collectionAndFriendRecyclerList = new ArrayList<>();
    CollectionAndFriendRecyclerAdapter collectionAndFriendRecyclerAdapter;
    RecyclerView collectionAndFriendRecyclerView;
    EditText searchTab;
    ImageView deselectAll, left;
    ImageView closeSearch;
    static String userName;
    SwipeRefreshLayout swipeRefreshLayout;

    public enum Item {card_name, card_desc, link}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user_dashboard);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        searchTab = findViewById(R.id.searchTab);
        openDrawer = findViewById(R.id.openDrawer);
        profile = findViewById(R.id.profile);
        navigationView = findViewById(R.id.navView);
        firebaseFirestore = FirebaseFirestore.getInstance();
        itemList = new ArrayList<>();
        swipeRefreshLayout = findViewById(R.id.refresh);
        firebaseAuth = FirebaseAuth.getInstance();
        userId = firebaseAuth.getCurrentUser().getUid();
        countView = findViewById(R.id.count);
        recyclerAdapter = new RecyclerAdapter(itemList, this);
        selectedCount = 0;
        searchLayout = findViewById(R.id.searchLayout);
        closeSearch = findViewById(R.id.closeSearch);
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean("firstStart", true);

        searchTab.addTextChangedListener(new TextWatcher() {
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

        firebaseFirestore.collection("user+" + userId).document("allSavedThings").collection("cards")
                .orderBy("upload_time", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    int i = 0;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String name, desc, link;
                        String cardNumber;
                        name = document.getString("card_name");
                        desc = document.getString("card_desc");
                        link = document.getString("link");
                        cardNumber =  document.getString("cardNumber");
                        itemList.add(new RecyclerItem(R.drawable.ic_plane, name, desc, link, MainUserDashboard.this, cardNumber, true,false));
                        recyclerAdapter.notifyItemChanged(i);
                        i++;
                    }
                    firebaseFirestore.collection("user+" + userId).document("Id").get()
                            .addOnSuccessListener(documentSnapshot -> userName = documentSnapshot.getString("name"));
                });
        if(isFirstTime){
          firstTimePrompt();
        }

        recyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(recyclerAdapter);
        left = findViewById(R.id.share);
        selectAll = findViewById(R.id.selectAll);
        deselectAll = findViewById(R.id.deSelectAll);
        right = findViewById(R.id.folder);
        findViewById(R.id.openDrawer).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.newCard) {
                MainUserDashboard.this.newCard();
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            if (id == R.id.myCollections) {
                drawerLayout.closeDrawer(GravityCompat.START);
                MainUserDashboard.this.startActivity(new Intent(MainUserDashboard.this.getApplicationContext(), CollectionActivity.class));
            }
            if (id == R.id.search) {
                drawerLayout.closeDrawer(GravityCompat.START);
                searchLayout.setVisibility(View.VISIBLE);
            }
            if (id == R.id.logout) {
                firebaseAuth.signOut();
                MainUserDashboard.this.startActivity(new Intent(MainUserDashboard.this.getApplicationContext(), LoginSignupScreen.class));
            }
            if (id == R.id.seeProfile) {
                MainUserDashboard.this.startActivity(new Intent(MainUserDashboard.this.getApplicationContext(), UserProfile.class));
            }
            if (id == R.id.shareHistory) {
                MainUserDashboard.this.startActivity(new Intent(MainUserDashboard.this.getApplicationContext(), ShareHistoryScreen.class));
            }
            if (id == R.id.received) {
                MainUserDashboard.this.startActivity(new Intent(MainUserDashboard.this.getApplicationContext(), ReceivedSharesActivity.class));
            }
            return true;
        });
        findViewById(R.id.profile).setOnClickListener(v -> {
            if (selectedCardNames.size() > 0) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                deselectAll();
                showMainToolbar();
                selectedCardNames.clear();
            } else {
                startActivity(new Intent(getApplicationContext(), UserProfile.class));
            }
        });
        closeSearch.setOnClickListener(v -> {
            searchTab.setText("");
            searchLayout.setVisibility(View.GONE);
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
            recyclerAdapter.filterNewList(itemList);
        });
        recyclerAdapter.setOnCardSelectedListener(cardNumber -> {
            if (selectedCount == 0) {
                showMainToolbar();
                selectedCardNames.clear();

            } else {
                showSelectionToolbar();
                selectDeselectCard(cardNumber);
            }
        });

        selectAll.setOnClickListener(v ->
        {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
            selectAll();
        });
        deselectAll.setOnClickListener(v -> {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
            deselectAll();
            showMainToolbar();
        });

        right.setOnClickListener(v -> {
            makeFolder();
        });

        recyclerAdapter.setOnCardClickedListener(link -> {
            if (selectedCount == 0) {
                Uri uri;
                if (link.contains("https://") || link.contains("http://")) {
                    uri = Uri.parse(link);
                } else {
                    uri = Uri.parse("https://" + link);
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        recyclerAdapter.setOnMoreClickedListener(recyclerItem ->
        {
            if(selectedCount==0){
                Dialog dialog = new Dialog(MainUserDashboard.this, R.style.DialogTheme);
                dialog.setContentView(R.layout.edit_card_dialog);
                dialog.create();
                dialog.show();
                dialog.setCanceledOnTouchOutside(false);
                dialog.findViewById(R.id.closeDialog).setOnClickListener(v -> dialog.dismiss());
                dialog.findViewById(R.id.editName).setOnClickListener(v -> {
                    editDetail(Item.card_name, recyclerItem);
                    dialog.dismiss();
                });
                dialog.findViewById(R.id.editDescription).setOnClickListener(v -> {
                    editDetail(Item.card_desc, recyclerItem);
                    dialog.dismiss();
                });
                dialog.findViewById(R.id.editLink).setOnClickListener(v -> {
                    editDetail(Item.link, recyclerItem);
                    dialog.dismiss();
                });
                dialog.findViewById(R.id.deleteCard).setOnClickListener(v -> {
                    //TODO: 3) remove from fb 4) make checking statement for coll
                    FirebaseFirestore.getInstance().collection("user+"+userId).document("allSavedThings").collection("cards")
                            .document("card "+ recyclerItem.getCardNumber()).delete()
                            .addOnSuccessListener(aVoid -> {
                                itemList.remove(recyclerItem);
                                recyclerAdapter.notifyDataSetChanged();
                                dialog.dismiss();
                            });
                });
            }
        });

        left.setOnClickListener(v -> {
            Dialog dialog = new Dialog(MainUserDashboard.this, R.style.DialogTheme);
            dialog.setContentView(R.layout.sharing_option);
            dialog.create();
            dialog.show();
            dialog.findViewById(R.id.closeShareDialog).setOnClickListener(v2 -> dialog.dismiss());
            dialog.setCanceledOnTouchOutside(false);
            dialog.findViewById(R.id.linkShare).setOnClickListener(v1 -> {
                firebaseFirestore.collection("user+" + userId).document("Id").get()
                        .addOnSuccessListener(documentSnapshot -> {
                            String userName = documentSnapshot.getString("name");
                            createShareLink(userName);
                            dialog.dismiss();
                        });

            });
            dialog.findViewById(R.id.apptoapp).setOnClickListener(v1 -> {
                dialog.dismiss();
                shareAppToApp();
            });
        });

        firebaseFirestore.collection("user+" + userId).document("receivedShares").collection("AppToApp")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    int i = 0;
                    for (DocumentSnapshot dc : queryDocumentSnapshots.getDocuments()) {
                        if ((long) dc.get("status") == 0) {
                            navigationView.getMenu().getItem(navigationView.getMenu().size() - 1).setTitle("you have new cards");
                            navigationView.getMenu().getItem(navigationView.getMenu().size() - 1).setIcon(R.drawable.ic_new_alert);
                        }
                        if ((long) dc.get("status") == 1) {
                            i++;
                            if (i == queryDocumentSnapshots.getDocuments().size()) {
                                navigationView.getMenu().getItem(navigationView.getMenu().size() - 1).setIcon(R.drawable.ic_plane);
                                navigationView.getMenu().getItem(navigationView.getMenu().size() - 1).setTitle("Received");
                            }
                        }
                    }
                });

        swipeRefreshLayout.setOnRefreshListener(() ->
        {
            recyclerView.setAlpha((float) 0.3);
            itemList.clear();
            recyclerAdapter.notifyDataSetChanged();
            firebaseFirestore.collection("user+" + userId).document("allSavedThings").collection("cards")
                    .orderBy("upload_time", Query.Direction.ASCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        int i = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name, desc, link;
                            String cardNumber;
                            name = document.getString("card_name");
                            desc = document.getString("card_desc");
                            link = document.getString("link");
                            cardNumber = document.getString("cardNumber");
                            itemList.add(new RecyclerItem(R.drawable.ic_plane, name, desc, link, MainUserDashboard.this, cardNumber, true,false));
                            recyclerAdapter.notifyItemChanged(i);
                            i++;
                        }
                        firebaseFirestore.collection("user+" + userId).document("Id").get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    userName = documentSnapshot.getString("name");
                                    swipeRefreshLayout.setRefreshing(false);
                                    recyclerView.setAlpha(1);
                                });
                    });
        });
    }

    public void firstTimePrompt()
    {    AlertDialog.Builder builder1 = new AlertDialog.Builder(MainUserDashboard.this, R.style.DialogTheme);
        View v1 = getLayoutInflater().inflate(R.layout.welcome_dialog, null);
        TextView message=v1.findViewById(R.id.promptmessage);
        message.setText("Hey! Welcome to LinkBucket.Now you can save , group , and share various different links together.");
        builder1.setView(v1);
        final AlertDialog alert1 = builder1.create();
        alert1.show();
        alert1.setCanceledOnTouchOutside(false);
        v1.findViewById(R.id.cancelFirstPrompt).setOnClickListener(v ->
        {
            alert1.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(MainUserDashboard.this, R.style.DialogTheme);
            View v2 = getLayoutInflater().inflate(R.layout.edit_prompt, null);
            builder.setView(v2);
            final AlertDialog alert = builder.create();
            alert.getWindow().setGravity(Gravity.TOP | Gravity.START);
            alert.show();
            alert.setCanceledOnTouchOutside(false);
            v2.findViewById(R.id.cancelPrompt).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alert.dismiss();
                    SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("firstStart", false);
                    editor.apply();

                }
            });
        });
    }


    public void sendNotif() {
        createNotificationChannel();
        Intent intent = new Intent(this, CollectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this, "myId"
        ).setSmallIcon(R.drawable.ic_person).setContentTitle("Notification").setContentText("this is a notif").setAutoCancel(true)
                .setContentIntent(pendingIntent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(100, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "myChannelName";
            String description = "myChannelDesc";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("myId", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void editDetail(final Item item, RecyclerItem recyclerItem) {
        Dialog dialog = new Dialog(MainUserDashboard.this, R.style.DialogTheme);
        dialog.setContentView(R.layout.change_detail_dialog);
        EditText detail = dialog.findViewById(R.id.newDetail);
        if (item == Item.card_name) {
            detail.setHint("Change Name");
        }
        if (item == Item.card_desc) {
            detail.setHint("Change Description");
        }
        if (item == Item.link) {
            detail.setHint("Change link");
        }
        dialog.create();
        dialog.show();
        int position = 0;
        int i = 0;
        for (RecyclerItem recyc : itemList) {
            if (recyc == recyclerItem) {
                position = i;
            } else {
                i++;
            }
        }
        dialog.findViewById(R.id.cancelBox).setOnClickListener(v -> dialog.dismiss());
        int finalPosition = position;
        dialog.findViewById(R.id.saveNewDetail).setOnClickListener(v -> {
            String newDetail = detail.getText().toString();
            if (item == Item.card_name) {
                itemList.get(finalPosition).setCardName(newDetail);
                recyclerAdapter.notifyDataSetChanged();
                updateDetailOnFireStore(Item.card_name, newDetail, itemList.get(finalPosition).getCardNumber());
            } else if (item == Item.card_desc) {
                itemList.get(finalPosition).setCardDesc(newDetail);
                recyclerAdapter.notifyDataSetChanged();
                updateDetailOnFireStore(Item.card_desc, newDetail, itemList.get(finalPosition).getCardNumber());
            } else if (item == Item.link) {
                itemList.get(finalPosition).setLink(newDetail);
                recyclerAdapter.notifyDataSetChanged();
                updateDetailOnFireStore(Item.link, newDetail, itemList.get(finalPosition).getCardNumber());
            }
            dialog.dismiss();
        });
    }

    private void updateDetailOnFireStore(Item item, String detail, String id) {
        firebaseFirestore.collection("user+" + userId).document("allSavedThings").collection("cards").document("card " + id)
                .update(item.toString(), detail)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Detail Updated", Toast.LENGTH_SHORT).show();
                });
    }

    private void filter(String text) {
        if(text.length()>=1){
            ArrayList<RecyclerItem> filterList = new ArrayList<>();
            for (RecyclerItem item : itemList) {
                if (item.getCardName().toLowerCase().contains(text.toLowerCase())||item.getCardDesc().toLowerCase().contains(text.toLowerCase())) {
                    filterList.add(item);
                }
            }
            recyclerAdapter.filterNewList(filterList);
        }
        else{
            recyclerAdapter.filterNewList(itemList);
        }
    }

    public void changeCount(String op) {
        if (op.equals("plus")) {
            selectedCount++;
        } else if (op.equals("minus")) {
            --selectedCount;
        }
    }

    public void selectAll() {
        if (selectAll.getVisibility() == View.VISIBLE) {
            selectedCardNames.clear();
            for (int i = 0; i < itemList.size(); i++) {
                selectedCardNames.add("card " + itemList.get(i).getCardNumber());
                searchTab.setText("");
                searchLayout.setVisibility(View.GONE);
                itemList.get(i).setFlag(1);
            }
            recyclerAdapter.filterNewList(itemList);
            countView.setText((itemList.size()) + " Selected");
        }
    }

    public void deselectAll() {
        if (selectAll.getVisibility() == View.VISIBLE) {
            selectedCardNames.clear();
            countView.setText("");
            searchTab.setText("");
            selectedCount = 0;
            searchLayout.setVisibility(View.GONE);
            for (RecyclerItem recyclerItem : itemList) {
                recyclerItem.changeSelected(false);
            }
            recyclerAdapter.filterNewList(itemList);
        }
    }

    public void showMainToolbar() {
        countView.setVisibility(View.INVISIBLE);
        selectAll.setVisibility(View.INVISIBLE);
        deselectAll.setVisibility(View.INVISIBLE);
        openDrawer.setVisibility(View.VISIBLE);
        profile.setImageResource(R.drawable.ic_person);
        left.setVisibility(View.INVISIBLE);
        right.setVisibility(View.INVISIBLE);
    }

    public void showSelectionToolbar() {
        countView.setVisibility(View.VISIBLE);
        selectAll.setVisibility(View.VISIBLE);
        deselectAll.setVisibility(View.VISIBLE);
        openDrawer.setVisibility(View.INVISIBLE);
        profile.setImageResource(R.drawable.ic_black_cancel);
        left.setVisibility(View.VISIBLE);
        right.setVisibility(View.VISIBLE);
        countView.setText(selectedCount + " Selected");
    }


    public void newCard() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainUserDashboard.this, R.style.DialogTheme);
        View v2 = MainUserDashboard.this.getLayoutInflater().inflate(R.layout.new_card_dialog, null);
        EditText cardName = v2.findViewById(R.id.newCardName);
        EditText cardDesc = v2.findViewById(R.id.newDesc);
        EditText link = v2.findViewById(R.id.newLink);
        dialog.setView(v2);
        AlertDialog alert = dialog.create();
        alert.show();
        alert.setCanceledOnTouchOutside(false);

        v2.findViewById(R.id.cancel).setOnClickListener(v -> {
            alert.dismiss();
        });

        v2.findViewById(R.id.save_action).setOnClickListener(v -> {
            String cardname = cardName.getText().toString();
            String carddesc = cardDesc.getText().toString();
            String cardlink = link.getText().toString();
            String randomId = generateRandomCardId();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            Map<String, Object> newCardMap = new HashMap<>();
            newCardMap.put("card_name", cardname);
            newCardMap.put("card_desc", carddesc);
            newCardMap.put("link", cardlink);
            newCardMap.put("cardNumber", randomId) ;
            newCardMap.put("upload_time", timestamp.getTime());
            firebaseFirestore.collection("user+" + userId).document("allSavedThings").collection("cards").document("card " + randomId)
                    .set(newCardMap)
                    .addOnSuccessListener(aVoid -> {
                        itemList.add(new RecyclerItem(R.drawable.ic_plane, cardName.getText().toString(), cardDesc.getText().toString(), link.getText().toString(), MainUserDashboard.this, randomId, true,false));
                        recyclerAdapter.notifyItemChanged(itemList.size() - 1);
                        Toast.makeText(MainUserDashboard.this, "Done", Toast.LENGTH_SHORT).show();
                        alert.dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(MainUserDashboard.this, "error", Toast.LENGTH_SHORT).show());
            alert.dismiss();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        moveTaskToBack(true);
    }

    public void selectDeselectCard(String id) {
        int position = 0;
        int i = 0;
        for (RecyclerItem recyclerItem : itemList) {
            if (recyclerItem.getCardNumber() == id) {
                position = i;
            }
            i++;
        }
        if (itemList.get(position).getSelected()) {
            selectedCardNames.add("card " + itemList.get(position).getCardNumber());
            Toast.makeText(this, "Selected Card At Position" + position, Toast.LENGTH_SHORT).show();
        } else {
            selectedCardNames.remove("card " + itemList.get(position).getCardNumber());
            Toast.makeText(this, "Deselected Card At Position" + position, Toast.LENGTH_SHORT).show();
        }

    }

    public void makeFolder() {
        Dialog dialog = new Dialog(this, R.style.DialogTheme);
        dialog.setContentView(R.layout.collection_selection_option);
        collectionAndFriendRecyclerView = dialog.findViewById(R.id.collectionNameRecyclerView);
        collectionAndFriendRecyclerList.add(new CollectionAndFriendRecyclerItem(R.drawable.ic_create_new_folder_black_24dp, "New Folder", ""));
        firebaseFirestore.collection("user+" + userId).document("allSavedThings").collection("Collections")
                .orderBy("upload_time", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    int i = 1;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        collectionAndFriendRecyclerList.add(new CollectionAndFriendRecyclerItem(R.drawable.ic_folder_shared_black_24dp, document.getId(), ""));
                        collectionAndFriendRecyclerAdapter.notifyItemChanged(i);
                        i++;
                    }
                });
        collectionAndFriendRecyclerAdapter = new CollectionAndFriendRecyclerAdapter(collectionAndFriendRecyclerList);
        collectionAndFriendRecyclerView.setAdapter(collectionAndFriendRecyclerAdapter);
        collectionAndFriendRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        ImageView cancel = dialog.findViewById(R.id.cancelFolderMaking);
        dialog.setOnDismissListener(dialog1 -> {
            collectionAndFriendRecyclerList.clear();
        });
        cancel.setOnClickListener(v -> {
            collectionAndFriendRecyclerList.clear();
            dialog.dismiss();
        });

        EditText searchFolderName = dialog.findViewById(R.id.searchFolderName);
        searchFolderName.addTextChangedListener(new TextWatcher() {
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
            if (folderName.equals("New Folder")) {
                dialog.dismiss();
                Dialog dialog1 = new Dialog(MainUserDashboard.this, R.style.DialogTheme);
                dialog1.setContentView(R.layout.change_detail_dialog);
                EditText newFolderName = dialog1.findViewById(R.id.newDetail);
                dialog1.create();
                dialog1.show();
                dialog1.findViewById(R.id.cancelBox).setOnClickListener(v -> dialog1.dismiss());
                dialog1.findViewById(R.id.saveNewDetail).setOnClickListener(v -> {
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    Map<String, Object> newFolderMap = new HashMap<>();
                    newFolderMap.put("Items", selectedCardNames);
                    newFolderMap.put("upload_time", timestamp.getTime());
                    firebaseFirestore.collection("user+" + userId).document("allSavedThings").collection("Collections").document(newFolderName.getText().toString())
                            .set(newFolderMap)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Folder made", Toast.LENGTH_SHORT).show();
                                dialog1.dismiss();
                                deselectAll();
                                showMainToolbar();
                            });
                });
            } else {
                firebaseFirestore.collection("user+" + userId).document("allSavedThings").collection("Collections").document(folderName)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            List<String> currentList = new ArrayList<>();
                            currentList = (List<String>) documentSnapshot.get("Items");
                            selectedCardNames.removeAll(currentList);
                            currentList.addAll(selectedCardNames);
                            firebaseFirestore.collection("user+" + userId).document("allSavedThings").collection("Collections").document(folderName)
                                    .update("Items", currentList)
                                    .addOnSuccessListener(aVoid -> Toast.makeText(MainUserDashboard.this, "Done", Toast.LENGTH_SHORT).show());
                            dialog.dismiss();
                            deselectAll();
                            showMainToolbar();
                        });
            }
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

    public void createShareLink(String userName) {
        String path = generateRandomShareId();
        Map<String, Object> newPathMap = new HashMap<>();
        newPathMap.put("Sender", userName);
        newPathMap.put("user Id", userId);
        newPathMap.put("type", "links");
        newPathMap.put("List", selectedCardNames);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Map<String, Object> historyMap = new HashMap<>();
        historyMap.put("code", path);
        historyMap.put("share_time", timestamp.getTime());
        historyMap.put("type", "links");
        firebaseFirestore.collection("shareLinkPaths").document("path+" + path).set(newPathMap)
                .addOnSuccessListener(aVoid -> {
                    firebaseFirestore.collection("user+" + userId).document("pastShares").collection("code").document().set(historyMap)
                            .addOnSuccessListener(aVoid1 -> {
                                showGeneratedLink(userName, path);
                                deselectAll();
                                showMainToolbar();
                            });
                });
    }

    public void showGeneratedLink(String userName, String path) {
        Dialog dialog = new Dialog(MainUserDashboard.this, R.style.DialogTheme);
        dialog.setContentView(R.layout.code_show);
        TextView link = dialog.findViewById(R.id.linkView);
        link.setText(path);
        dialog.create();
        dialog.show();
        dialog.findViewById(R.id.closeLink).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.shareLink).setOnClickListener(v -> {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, userName + " has shared some links!\nClick on the link to view them https://linkbucket-3b660.web.app/#/"+path+ " to view the shared links");
            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        });
    }

    public static String generateRandomShareId() {
        char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrst0123456789".toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            char c = chars[random.nextInt(chars.length)];
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }
    public static String generateRandomCardId() {
        char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrst0123456789".toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 7; i++) {
            char c = chars[random.nextInt(chars.length)];
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
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

    public void shareAppToApp() {
        Dialog dialog2 = new Dialog(this, R.style.DialogTheme);
        dialog2.setContentView(R.layout.collection_selection_option);
        EditText search = dialog2.findViewById(R.id.searchFolderName);
        search.setHint("Loading...");
        collectionAndFriendRecyclerView = dialog2.findViewById(R.id.collectionNameRecyclerView);
        firebaseFirestore.collection("usersList").get().addOnCompleteListener(task ->
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
            Dialog dialog1 = new Dialog(MainUserDashboard.this, R.style.DialogTheme);
            dialog1.setContentView(R.layout.change_detail_dialog);
            EditText title = dialog1.findViewById(R.id.newDetail);
            title.setText("Share selected links with " + name + " ?");
            title.setTextColor(ContextCompat.getColor(getApplicationContext(),android.R.color.black));
            title.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorMainYellow));
            Button share = dialog1.findViewById(R.id.saveNewDetail);
            share.setText("Next");
            dialog1.create();
            dialog1.show();
            title.setEnabled(false);
            dialog1.findViewById(R.id.saveNewDetail).setOnClickListener(v2 -> {
                dialog1.dismiss();
                Dialog dialog3 = new Dialog(MainUserDashboard.this, R.style.DialogTheme);
                dialog3.setContentView(R.layout.change_detail_dialog);
                Button next = dialog3.findViewById(R.id.saveNewDetail);
                EditText bunchName = dialog3.findViewById(R.id.newDetail);
                bunchName.setHint("Enter a name for the bunch of links to be shared.");
                next.setText("Share");
                dialog3.create();
                dialog3.show();
                next.setOnClickListener(v3 -> {
                    dialog3.dismiss();
                    shareTheCards(selectedCardNames, userId2, name, userName, bunchName.getText().toString());
                });
            });
        });

    }
}
