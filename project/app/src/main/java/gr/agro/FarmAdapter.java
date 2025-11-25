package gr.agro;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.aviran.cookiebar2.CookieBar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class FarmAdapter extends ArrayAdapter<Farm> {

    private final Activity context;
    private List<Farm> farms;

    public FarmAdapter(Activity context, List<Farm> farms) {
        super(context, 0, farms);
        this.farms = farms;
        this.context = context;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Farm farm = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.farm_list_item, parent, false);
        }

        TextView documentId = convertView.findViewById(R.id.documentId);
        documentId.setText(farm.getDocumentId());

        TextView name = convertView.findViewById(R.id.name);
        name.setText(farm.getName());
        name.setTag(farm.getName());

        TextView size = convertView.findViewById(R.id.size);
        size.setText("Στρέμματα:" + farm.getSize());
        size.setTag(farm.getSize());

        Button button = convertView.findViewById(R.id.deleteFarmBtn);
        button.setOnClickListener(v -> { confirmDeleteFarm(position, farm.getDocumentId()); });

        return convertView;
    }


    private void confirmDeleteFarm(int position, String documentId){
        new AlertDialog.Builder(this.context)
        .setTitle("Διαγραφή φάρμας")
        .setMessage("Είστε σίγουροι για τη διαγραφή;")
        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
            deleteFarm(position, documentId);
        })
        .setNegativeButton(android.R.string.no, null)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .show();
    }


    private void deleteFarm(int position, String documentId){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("farms").document(documentId)
        .delete()
        .addOnSuccessListener(aVoid -> {
            Log.d(Global.TAG, "Document successfully deleted");
            farms.remove(position);
            deleteEmpFarm(documentId);
            notifyDataSetChanged();
            CookieBar.build(this.context)
            .setMessage(this.context.getString(R.string.farm_delete_success))
            .setBackgroundColor(R.color.green)
            .setCookiePosition(CookieBar.BOTTOM).show();
        })
        .addOnFailureListener(e -> {
            Log.w(Global.TAG, "Error deleting document", e);
            CookieBar.build(this.context)
            .setMessage(this.context.getString(R.string.farm_delete_fail))
            .setBackgroundColor(R.color.red)
            .setCookiePosition(CookieBar.BOTTOM).show();
        });
    }


    private void deleteEmpFarm(String documentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("emp_farms")
        .whereEqualTo("farmDocumentId", documentId)
        .get()
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    db.collection("yourCollectionName").document(document.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> Log.d(Global.TAG, "DocumentSnapshot successfully deleted!"))
                    .addOnFailureListener(e -> Log.w(Global.TAG, "Error deleting document", e));
                }
            } else {
                Log.w(Global.TAG, "Error getting documents.", task.getException());
            }
        });
    }
}