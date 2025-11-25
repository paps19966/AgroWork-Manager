package gr.agro;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import org.aviran.cookiebar2.CookieBar;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorkAdapter extends ArrayAdapter<Work> {

    private List<String> daysOfWeek;
    private final Activity context;
    private List<Work> works;

    public WorkAdapter(Activity context, List<Work> works) {
        super(context, 0, works);
        this.context = context;
        String[] stringArray = context.getResources().getStringArray(R.array.week_days);
        daysOfWeek = new ArrayList<>(Arrays.asList(stringArray));
        this.works = works;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Work work = getItem(position);
        if ( convertView == null ){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.work_list_item, parent, false);
        }

        TextView documentIdTv = convertView.findViewById(R.id.documentId);
        documentIdTv.setText(work.getDocumentId());

        TextView dayTv = convertView.findViewById(R.id.dayTv);
        dayTv.setText(daysOfWeek.get(work.getDay()));

        TextView timeTv = convertView.findViewById(R.id.timeTv);
        timeTv.setText(work.getStartTime() + " - " + work.getEndTime());

        TextView commentsTv = convertView.findViewById(R.id.commentsTv);
        commentsTv.setText(work.getComments());

        Button button = convertView.findViewById(R.id.deleteWorkBtn);
        button.setOnClickListener(v -> { confirmDeleteWork(position, work.getDocumentId()); });

        return convertView;
    }


    private void confirmDeleteWork(int position, String documentId){
        new AlertDialog.Builder(this.context)
        .setTitle("Διαγραφή εργασίας")
        .setMessage("Είστε σίγουροι για τη διαγραφή;")
        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
            deleteWork(position, documentId);
        })
        .setNegativeButton(android.R.string.no, null)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .show();
    }


    private void deleteWork(int position, String documentId){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("works").document(documentId)
        .delete()
        .addOnSuccessListener(aVoid -> {
            Log.d(Global.TAG, "Document successfully deleted");
            works.remove(position);
            notifyDataSetChanged();
            CookieBar.build(this.context)
            .setMessage(this.context.getString(R.string.work_delete_success))
            .setBackgroundColor(R.color.green)
            .setCookiePosition(CookieBar.BOTTOM).show();
        })
        .addOnFailureListener(e -> {
            Log.w(Global.TAG, "Error deleting document", e);
            CookieBar.build(this.context)
            .setMessage(this.context.getString(R.string.work_delete_fail))
            .setBackgroundColor(R.color.red)
            .setCookiePosition(CookieBar.BOTTOM).show();
        });
    }

}