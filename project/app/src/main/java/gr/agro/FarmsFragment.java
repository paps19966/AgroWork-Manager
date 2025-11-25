package gr.agro;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.aviran.cookiebar2.CookieBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FarmsFragment extends Fragment {

    private ListView listView;
    private FarmAdapter adapter;
    private List<Farm> farmList = new ArrayList<>();

    public FarmsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_farms, container, false);
        listView = view.findViewById(R.id.listViewFarms);
        adapter = new FarmAdapter(getActivity(), farmList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            editFarm(view1);
        });
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        fetchFarms();
    }


    public void fetchFarms(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("farms")
        .orderBy("created_at", Query.Direction.DESCENDING)
        .get()
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Farm> list = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Farm farm = document.toObject(Farm.class);
                    farm.setDocumentId(document.getId());
                    list.add(farm);
                    //Log.d(Global.TAG, document.getId());
                }
                updateData(list);
            } else {
                Log.w(Global.TAG, "Error getting documents: ", task.getException());
            }
        });
    }


    public void updateData(List<Farm> list){
        farmList.clear();
        farmList.addAll(list);
        if ( adapter != null ){
            adapter.notifyDataSetChanged();
        }
    }


    private void editFarm(View listViewItem) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.farm_alert, null);

        EditText nameEditText = dialogView.findViewById(R.id.name);
        TextView name = listViewItem.findViewById(R.id.name);
        nameEditText.setText(name.getTag().toString());

        EditText sizeEditText = dialogView.findViewById(R.id.size);
        TextView size = listViewItem.findViewById(R.id.size);
        sizeEditText.setText(size.getTag().toString());

        TextView documentId = listViewItem.findViewById(R.id.documentId);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        builder.setPositiveButton(getString(R.string.save), null);
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();

        // USER CLICKS THE POSITIVE BUTTON
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(v -> {
            if ( !Global.checkEditTexts(new EditText[]{nameEditText, sizeEditText}) ){
                CookieBar.build(getActivity())
                .setMessage(getString(R.string.fields_missing_error))
                .setBackgroundColor(R.color.red)
                .setCookiePosition(CookieBar.BOTTOM).show();
                return;
            }

            // SAVE FARM
            saveFarm(documentId.getText().toString(), nameEditText.getText().toString(), sizeEditText.getText().toString());
            dialog.cancel();
        });
    }


    public void saveFarm(String documentId, String name, String size){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> farm = new HashMap<>();
        farm.put("name", name);
        farm.put("size", size);
        // ADD
        if ( documentId == null ){
            farm.put("created_at", FieldValue.serverTimestamp());
            db.collection("farms").add(farm)
            .addOnSuccessListener(documentReference -> {
                Log.d(Global.TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                CookieBar.build(getActivity())
                .setMessage(getString(R.string.farm_add_success))
                .setBackgroundColor(R.color.green)
                .setCookiePosition(CookieBar.BOTTOM).show();
                fetchFarms();
            })
            .addOnFailureListener(e -> {
                Log.w(Global.TAG, "Error adding document", e);
                CookieBar.build(getActivity())
                .setMessage(getString(R.string.farm_add_fail))
                .setBackgroundColor(R.color.red)
                .setCookiePosition(CookieBar.BOTTOM).show();
            });
        }
        // UPDATE
        else{
            db.collection("farms").document(documentId).update(farm)
            .addOnSuccessListener(aVoid -> {
                Log.d(Global.TAG, "DocumentSnapshot successfully updated");
                CookieBar.build(getActivity())
                .setMessage(getString(R.string.farm_save_success))
                .setBackgroundColor(R.color.green)
                .setCookiePosition(CookieBar.BOTTOM).show();
                fetchFarms();
            })
            .addOnFailureListener(e -> {
                Log.w(Global.TAG, "Error saving document", e);
                CookieBar.build(getActivity())
                .setMessage(getString(R.string.farm_save_fail))
                .setBackgroundColor(R.color.red)
                .setCookiePosition(CookieBar.BOTTOM).show();
            });
        }
    }
}