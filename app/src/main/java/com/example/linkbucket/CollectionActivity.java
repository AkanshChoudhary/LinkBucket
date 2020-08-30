package com.example.linkbucket;

import android.app.Dialog;
import android.content.Intent;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CollectionActivity extends AppCompatActivity {
    ArrayList<FolderRecyclerItem> folderItemList;
    Toolbar toolbar;
    RecyclerView folderRecyclerView;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;
    FolderRecyclerAdapter folderRecyclerAdapter;
    EditText folderSearchBar;
    int selectedCount;
    LinearLayout folderSearchTab;
    TextView title;
    ImageView selectAllFolders, deselectAllFolders, share, close;
    static int finalFolderNumber;
    List<String> selectedFolderNames = new ArrayList<>();
    RecyclerView collectionAndFriendRecyclerView;
    CollectionAndFriendRecyclerAdapter collectionAndFriendRecyclerAdapter;
    String userName;
    ImageView backBtn;
    static final ArrayList<CollectionAndFriendRecyclerItem> collectionAndFriendRecyclerList = new ArrayList<>();
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        toolbar = findViewById(R.id.toolbar2);
        folderRecyclerView = findViewById(R.id.collectionRecyclerView);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        setSupportActionBar(toolbar);
        folderItemList = new ArrayList<>();
        userId = fAuth.getCurrentUser().getUid();
        folderRecyclerAdapter = new FolderRecyclerAdapter(folderItemList, this);
        folderSearchBar = findViewById(R.id.folderSearchBar);
        ImageView folderSearch = findViewById(R.id.folderSearch);
        ImageView cancelSearch = findViewById(R.id.cancelCardSearch);
        selectedCount = 0;
        share = findViewById(R.id.folderShare);
        folderSearchTab = findViewById(R.id.folderSearchTab);
        backBtn = findViewById(R.id.backBtn);
        selectAllFolders = findViewById(R.id.selectAllFolder);
        deselectAllFolders = findViewById(R.id.deSelectAllFolders);
        close = findViewById(R.id.close);
        title = findViewById(R.id.title);
        swipeRefreshLayout=findViewById(R.id.collectionRefresh);
        folderSearch.setOnClickListener(v -> {
            folderSearchTab.setVisibility(View.VISIBLE);
        });
        backBtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MainUserDashboard.class)));
        cancelSearch.setOnClickListener(v -> {
            folderSearchBar.setText("");
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
            folderSearchTab.setVisibility(View.GONE);
        });
        fStore.collection("user+" + userId).document("allSavedThings").collection("Collections")
                .orderBy("upload_time", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    int i = 0;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        folderItemList.add(new FolderRecyclerItem(document.getId()));
                        folderRecyclerAdapter.notifyItemChanged(i);
                        i++;
                    }
                    finalFolderNumber = task.getResult().size();
                    fStore.collection("user+" + userId).document("Id").get()
                            .addOnSuccessListener(documentSnapshot -> userName = documentSnapshot.getString("name"));
                });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, RecyclerView.VERTICAL, false);
        folderRecyclerView.setLayoutManager(gridLayoutManager);
        folderRecyclerView.setAdapter(folderRecyclerAdapter);

        folderSearchBar.addTextChangedListener(new TextWatcher() {
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

        folderRecyclerAdapter.setOnCardSelectedListener(folderName -> {
            Toast.makeText(this, folderName, Toast.LENGTH_SHORT).show();
            if (selectedCount == 0) {
                title.setText("My Collections");
                selectedFolderNames.clear();
                showMainToolbar();
            } else {
                selectDeselectCard(folderName);
                showSelectionToolbar();
            }
        });
        folderRecyclerAdapter.setOnFolderClickedListener(folderName -> {
            Toast.makeText(this, folderName, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CollectionActivity.this, CardsOfCollection.class);
            intent.putExtra("folder_name", folderName);
            startActivity(intent);
        });

        close.setOnClickListener(v -> {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
            folderSearchBar.setText("");
            showMainToolbar();
            deselectAll();
        });

        cancelSearch.setOnClickListener(v -> {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
            folderSearchBar.setText("");
            folderSearchTab.setVisibility(View.GONE);
        });

        selectAllFolders.setOnClickListener(v -> {
            for (FolderRecyclerItem folderRecyclerItem : folderItemList) {
                folderRecyclerItem.setFlag(1);
            }
            folderRecyclerAdapter.filterNewList(folderItemList);
            selectedCount = finalFolderNumber;
            title.setText(selectedCount + " Selected");
        });
        deselectAllFolders.setOnClickListener(v -> {
            deselectAll();
            showMainToolbar();
        });

        share.setOnClickListener(v -> {
            Dialog dialog = new Dialog(CollectionActivity.this, R.style.DialogTheme);
            dialog.setContentView(R.layout.sharing_option);
            dialog.create();
            dialog.show();
            dialog.setCanceledOnTouchOutside(false);
            dialog.findViewById(R.id.closeShareDialog).setOnClickListener(v2 -> dialog.dismiss());

            dialog.findViewById(R.id.linkShare).setOnClickListener(v1 -> {
                Dialog dialog1 = new Dialog(CollectionActivity.this, R.style.DialogTheme);
                dialog1.setContentView(R.layout.change_detail_dialog);
                Button next = dialog1.findViewById(R.id.saveNewDetail);
                EditText bunchName = dialog1.findViewById(R.id.newDetail);
                bunchName.setHint("Enter a name for the bunch of links to be shared.");
                next.setText("Next");
                dialog1.create();
                dialog1.show();
                dialog.dismiss();
                next.setOnClickListener(v2 -> {
                    fStore.collection("user+" + userId).document("Id").get()
                            .addOnSuccessListener(documentSnapshot -> {
                                String userName = documentSnapshot.getString("name");
                                createShareLink(userName, bunchName.getText().toString());
                                dialog1.dismiss();
                            });
                });
                dialog1.findViewById(R.id.cancelBox).setOnClickListener(v2 -> {
                    dialog1.dismiss();
                });
            });

            dialog.findViewById(R.id.apptoapp).setOnClickListener(v1 -> {

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
                collectionAndFriendRecyclerAdapter.setOnFolderClickedListener((folderName, userId2) -> {
                    dialog2.dismiss();
                    String name = folderName.substring(0, folderName.length() - 9);
                    String personalId = folderName.substring(folderName.length() - 8);
                    Dialog dialog1 = new Dialog(CollectionActivity.this, R.style.DialogTheme);
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
                        Dialog dialog3 = new Dialog(CollectionActivity.this, R.style.DialogTheme);
                        dialog3.setContentView(R.layout.change_detail_dialog);
                        Button next = dialog3.findViewById(R.id.saveNewDetail);
                        EditText bunchName = dialog3.findViewById(R.id.newDetail);
                        bunchName.setHint("Enter a name for the Folder(s) to be shared.");
                        next.setText("Share");
                        dialog3.create();
                        dialog3.show();
                        next.setOnClickListener(v3 ->
                        {
                          shareTheCards(selectedFolderNames, userId2, name, userName,  bunchName.getText().toString());
                          dialog3.dismiss();
                        });
                        dialog3.findViewById(R.id.cancelBox).setOnClickListener(v3 -> {
                            dialog3.dismiss();
                        });
                    });
                });
            });
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            folderRecyclerView.setAlpha((float) 0.3);
            folderItemList.clear();
            folderRecyclerAdapter.notifyDataSetChanged();
            fStore.collection("user+" + userId).document("allSavedThings").collection("Collections")
                    .orderBy("upload_time", Query.Direction.ASCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        int i = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            folderItemList.add(new FolderRecyclerItem(document.getId()));
                            folderRecyclerAdapter.notifyItemChanged(i);
                            i++;
                        }
                        finalFolderNumber = task.getResult().size();
                        fStore.collection("user+" + userId).document("Id").get()
                                .addOnSuccessListener(documentSnapshot ->
                                {
                                    userName = documentSnapshot.getString("name");
                                    swipeRefreshLayout.setRefreshing(false);
                                    folderRecyclerView.setAlpha(1);
                                });
                    });
        });
    }

    public void createShareLink(String userName, String bunchName) {
        String path = "f"+generateRandomShareId();
        Map<String, Object> newPathMap = new HashMap<>();
        newPathMap.put("Sender", userName);
        newPathMap.put("user Id", userId);
        newPathMap.put("type", "Folders");
        newPathMap.put("bunch_name", bunchName);
        newPathMap.put("Folders", selectedFolderNames);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Map<String, Object> historyMap = new HashMap<>();
        historyMap.put("code", path);
        historyMap.put("bunch_name", bunchName);
        historyMap.put("share_time", timestamp.getTime());
        historyMap.put("type", "Folders");
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
        Dialog dialog = new Dialog(CollectionActivity.this, R.style.DialogTheme);
        dialog.setContentView(R.layout.code_show);
        TextView link = dialog.findViewById(R.id.linkView);
        link.setText(path);
        dialog.create();
        dialog.show();
        dialog.findViewById(R.id.closeLink).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.shareLink).setOnClickListener(v -> {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, userName + " has shared some links!\ncopy the code below and visit linkbucket.in to view the shared links" + "\n" + path);
            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        });
    }

    private String generateRandomShareId() {
        char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrst0123456789".toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            char c = chars[random.nextInt(chars.length)];
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }


    private void filter(String text) {
        ArrayList<FolderRecyclerItem> filterList = new ArrayList<>();
        for (FolderRecyclerItem item : folderItemList) {
            if (item.getFolderName().toLowerCase().contains(text.toLowerCase())) {
                filterList.add(item);
            }
        }
        folderRecyclerAdapter.filterNewList(filterList);
    }

    public void changeSelectedCount(String op) {
        if (op.equals("plus")) {
            selectedCount++;
        } else if (op.equals("minus")) {
            selectedCount--;
        }
    }

    public void selectDeselectCard(String folderName) {
        int i = 0;
        int pos = -1;
        for (FolderRecyclerItem item : folderItemList) {
            if (item.getFolderName() == folderName) {
                pos = i;
                break;
            }
            i++;
        }
        if (folderItemList.get(pos).getState()) {
            selectedFolderNames.add(folderName);
            Toast.makeText(this, "Selected Card At Position" + pos, Toast.LENGTH_SHORT).show();
        } else {
            selectedFolderNames.remove(folderName);
            Toast.makeText(this, "Deselected Card At Position" + pos, Toast.LENGTH_SHORT).show();
        }
    }


    public void showMainToolbar() {
        share.setVisibility(View.INVISIBLE);
        deselectAllFolders.setVisibility(View.INVISIBLE);
        selectAllFolders.setVisibility(View.INVISIBLE);
        close.setVisibility(View.INVISIBLE);
        title.setText("My Collections");
        backBtn.setVisibility(View.VISIBLE);
        selectedCount = 0;
    }

    public void showSelectionToolbar() {
        share.setVisibility(View.VISIBLE);
        deselectAllFolders.setVisibility(View.VISIBLE);
        selectAllFolders.setVisibility(View.VISIBLE);
        close.setVisibility(View.VISIBLE);
        backBtn.setVisibility(View.GONE);
        title.setText(selectedCount + " Selected");
    }

    public void deselectAll() {
        for (FolderRecyclerItem folderRecyclerItem : folderItemList) {
            folderRecyclerItem.setFlag(-1);
        }
        selectedFolderNames.clear();
        selectedCount=0;
        folderRecyclerAdapter.filterNewList(folderItemList);
    }
    public void shareTheCards(List<String> selectedCards, String receiverUserId, String receiverName, String senderName, String bunchName) {
        Map<String, Object> shareMap = new HashMap<>();
        java.sql.Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        shareMap.put("Sender", senderName);
        shareMap.put("Folders", selectedCards);
        shareMap.put("status", 0);
        shareMap.put("senderUserId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        shareMap.put("timeStamp", timestamp.getTime());
        shareMap.put("bunch_name", bunchName);
        Map<String, Object> saveHistoryMap = new HashMap<>();
        saveHistoryMap.put("sent to", receiverName);
        saveHistoryMap.put("receiverUserId", receiverUserId);
        saveHistoryMap.put("Folders", selectedCards);
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