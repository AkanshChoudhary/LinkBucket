package com.linkbucket.linkbucket;

import android.content.Context;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShareAppToApp {
    final List<String> selectedCards;
    final String senderName;
    final String receiverUserId;
    final Context context;
    final String receiverName;
    final boolean isLink;
    final String bunchName;

    public ShareAppToApp(List<String> selectedCards, String receiverUserId, String receiverName, String senderName, Context context,boolean isLink,String bunchName) {
        this.selectedCards = selectedCards;
        this.context = context;
        this.receiverUserId = receiverUserId;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.isLink= isLink;
        this.bunchName= bunchName;
    }

    public void shareTheCards(List<String> selectedCards, String receiverUserId, String receiverName, String senderName, Context context,boolean isLink,String bunchName) {
        Map<String, Object> shareMap = new HashMap<>();
        java.sql.Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        shareMap.put("Sender", senderName);
        if(isLink){shareMap.put("List", selectedCards);}
        else{shareMap.put("FoldersList",selectedCards);}
        shareMap.put("status",0);
        shareMap.put("senderUserId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        shareMap.put("timeStamp",timestamp.getTime());
        shareMap.put("bunch_name",bunchName);
        Map<String, Object> saveHistoryMap = new HashMap<>();
        saveHistoryMap.put("sent to", receiverName);
        saveHistoryMap.put("receiverUserId", receiverUserId);
        if(isLink){saveHistoryMap.put("List", selectedCards);}
        else {saveHistoryMap.put("Folders", selectedCards);}
        saveHistoryMap.put("bunch_name",bunchName);
        FirebaseFirestore.getInstance().collection("user+" + receiverUserId).document("receivedShares")
                .collection("AppToApp").document().set(shareMap).addOnSuccessListener(aVoid -> {
            FirebaseFirestore.getInstance().collection("user+" + FirebaseAuth.getInstance().getCurrentUser().getUid()).document("pastShares")
                    .collection("toApp").document().set(saveHistoryMap)
                    .addOnSuccessListener(aVoid1 -> {
                        Toast.makeText(context, "All done", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
