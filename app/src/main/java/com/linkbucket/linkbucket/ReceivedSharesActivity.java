package com.linkbucket.linkbucket;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ReceivedSharesActivity extends AppCompatActivity {
    RecyclerView receivedCardsRecyclerView;
    final List<HistoryItem> receivedList = new ArrayList<>();
    final ArrayList<ArrayList<String>> listOfAllLists = new ArrayList<>();
    final List<String> documentNames = new ArrayList<>();
    final List<String> senderUserIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_shares);
        receivedCardsRecyclerView = findViewById(R.id.receivedCardsRecyclerView);
        ImageView back = findViewById(R.id.back2);
        back.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MainUserDashboard.class)));
        HistoryRecyclerAdapter historyRecyclerAdapter = new HistoryRecyclerAdapter(receivedList);
        FirebaseFirestore.getInstance().collection("user+" + FirebaseAuth.getInstance().getCurrentUser().getUid()).document("receivedShares").collection("AppToApp")
                .orderBy("timeStamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {

                    int i = 0;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        receivedList.add(new HistoryItem(document.getString("bunch_name"), "sender: " + document.getString("Sender"), document.getLong("status")));
                        historyRecyclerAdapter.notifyItemChanged(i);
                        i++;
                        listOfAllLists.add((ArrayList<String>) document.get("List"));
                        documentNames.add(document.getId());
                        senderUserIds.add(document.getString("senderUserId"));
                    }
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        FirebaseFirestore.getInstance().collection("user+" + FirebaseAuth.getInstance().getCurrentUser().getUid()).document("receivedShares").collection("AppToApp")
                                .document(document.getId()).update("status", 1);
                    }
                });
        receivedCardsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        receivedCardsRecyclerView.setAdapter(historyRecyclerAdapter);
        historyRecyclerAdapter.setOnHistoryClickedListener(position -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ReceivedSharesActivity.this);
            bottomSheetDialog.setContentView(getLayoutInflater().inflate(R.layout.bottom_sheet, null));
            bottomSheetDialog.show();
            NavigationView nav = bottomSheetDialog.findViewById(R.id.nav);
            nav.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.view) {
                    /* we need a list of recycler items and pass it to the activity*/
                    Intent intent = new Intent(ReceivedSharesActivity.this, ReceivedShareCardView.class);
                    intent.putExtra("title", receivedList.get(position).bunch_name);
                    intent.putExtra("userId", senderUserIds.get(position));
                    intent.putStringArrayListExtra("list", listOfAllLists.get(position));
                    startActivity(intent);
                }
                if (id == R.id.add) {
                    Dialog dialog = new Dialog(ReceivedSharesActivity.this, R.style.DialogTheme);
                    dialog.setContentView(R.layout.change_detail_dialog);
                    TextView title= dialog.findViewById(R.id.optionalHeading);
                    title.setHint("Add the links as a collection?");
                    title.setTextSize(20);
                    title.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.black));
                    EditText name= dialog.findViewById(R.id.newDetail);
                    name.setHint("Collection name");
                    Button skip= dialog.findViewById(R.id.cancelBox);
                    skip.setText("Skip");
                    Button yes= dialog.findViewById(R.id.saveNewDetail);
                    dialog.create();
                    dialog.show();
                    skip.setOnClickListener(v->{
                        dialog.dismiss();
                       addCardsToAccount(position,"");
                       dialog.dismiss();
                    });
                    yes.setOnClickListener(v->{
                        addCardsToAccount(position,name.getText().toString());
                        dialog.dismiss();
                    });
                }
                if (id == R.id.delete) {
                    bottomSheetDialog.dismiss();
                    Dialog dialog = new Dialog(this, R.style.DialogTheme);
                    dialog.setContentView(R.layout.change_detail_dialog);
                    EditText title = dialog.findViewById(R.id.newDetail);
                    title.setEnabled(false);
                    title.setTextColor(ContextCompat.getColor(getApplicationContext(),android.R.color.black));
                    title.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorMainYellow));
                    title.setText("Delete the selected share ?");
                    Button delete = dialog.findViewById(R.id.saveNewDetail);
                    delete.setText("Delete");
                    dialog.show();
                    delete.setOnClickListener(v -> {
                        dialog.dismiss();
                        FirebaseFirestore.getInstance().collection("user+" + FirebaseAuth.getInstance().getCurrentUser().getUid()).document("receivedShares").collection("AppToApp")
                                .document(documentNames.get(position)).delete().addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                            receivedList.remove(position);
                            historyRecyclerAdapter.notifyItemRemoved(position);
                        });
                    });
                }
                return true;
            });
        });
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

    public void addCardsToAccount(int position,String collName){
        List<RecyclerItem> myList = new ArrayList<>();
        List<String> cardNumbers= new ArrayList<>();
        FirebaseFirestore.getInstance().collection("user+" + senderUserIds.get(position)).document("allSavedThings").collection("cards")
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot dc : task.getResult()) {
                        if (listOfAllLists.get(position).contains(dc.getId())) {
                            myList.add(new RecyclerItem(R.drawable.ic_plane, dc.getString("card_name"), dc.getString("card_desc"), dc.getString("link"), this, dc.getString("cardNumber"), false,false));
                        }
                    }

                    WriteBatch batch = FirebaseFirestore.getInstance().batch();
                    for (int i = 0; i < myList.size(); i++) {
                        java.sql.Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        String randomId = generateRandomCardId();
                        Map<String, Object> addMap = new HashMap<>();
                        addMap.put("card_name", myList.get(i).getCardName());
                        addMap.put("card_desc", myList.get(i).getCardDesc());
                        addMap.put("link", myList.get(i).getLink());
                        addMap.put("cardNumber", randomId);
                        addMap.put("upload_time", timestamp.getTime());
                        DocumentReference dc = FirebaseFirestore.getInstance().collection("user+" + FirebaseAuth.getInstance().getCurrentUser().getUid()).document("allSavedThings").collection("cards").document("card " + randomId);
                        batch.set(dc, addMap);
                        cardNumbers.add(randomId);
                    }
                    batch.commit().addOnCompleteListener(task1 -> {
                        if(collName==""){
                            Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();
                        }
                       else{
                            java.sql.Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                           Map<String,Object> collMap= new HashMap<>();
                           collMap.put("Items",cardNumbers);
                           collMap.put("upload_time",timestamp.getTime());
                            FirebaseFirestore.getInstance().collection("user+" + FirebaseAuth.getInstance().getCurrentUser().getUid()).document("allSavedThings").collection("Collections")
                                    .document(collName).set(collMap).addOnSuccessListener(aVoid -> Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show());
                        }
                    });

                });
    }
}