package gr.agro;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.aviran.cookiebar2.CookieBar;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EmployeeWorkActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    private String employeeDocumentId = null;
    private ListView listView;
    private WorkAdapter adapter;
    private List<Work> worksList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_work);

        employeeDocumentId = getIntent().getStringExtra("employeeDocumentId");
        if ( employeeDocumentId == null || employeeDocumentId.isEmpty() ){
            CookieBar.build(this).setMessage(getString(R.string.employee_not_found)).setBackgroundColor(R.color.red).setCookiePosition(CookieBar.BOTTOM).show();
            finish();
            return;
        }

        String employeeFullname = getIntent().getStringExtra("employeeFullname");
        setTitle(employeeFullname);

        listView = findViewById(R.id.worksListView);
        adapter = new WorkAdapter(this, worksList);
        listView.setAdapter(adapter);

        fetchWorks();
    }


    private void fetchWorks(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("works")
        .whereEqualTo("employeeDocumentId", employeeDocumentId)
        .get()
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Work> list = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Work work = document.toObject(Work.class);
                    work.setDocumentId(document.getId());
                    list.add(work);
                }
                updateData(list);
            } else {
                Log.w(Global.TAG, "Error getting documents: ", task.getException());
            }
        });
    }


    public void updateData(List<Work> list){
        worksList.clear();
        worksList.addAll(list);
        if ( adapter != null ){
            adapter.notifyDataSetChanged();
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.work_alert, null);

        // COMMENTS
        EditText comments = dialogView.findViewById(R.id.comments);

        // DAY
        Spinner daysSpinner = dialogView.findViewById(R.id.days);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.week_days, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daysSpinner.setAdapter(adapter);

        // START TIME
        Spinner startTimeSpinner = dialogView.findViewById(R.id.startTime);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.time_slots, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startTimeSpinner.setAdapter(adapter2);

        // END TIME
        Spinner endTimeSpinner = dialogView.findViewById(R.id.endTime);
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this, R.array.time_slots, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        endTimeSpinner.setAdapter(adapter3);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setPositiveButton(getString(R.string.add), null);
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(v -> {

            String startTime = startTimeSpinner.getSelectedItem().toString();
            String endTime = endTimeSpinner.getSelectedItem().toString();

            // TIMES EQUAL
            if ( startTime.equals(endTime) ){
                CookieBar.build(this)
                .setMessage(getString(R.string.time_slots_same))
                .setBackgroundColor(R.color.red)
                .setCookiePosition(CookieBar.BOTTOM).show();
                return;
            }

            // INVALID END TIME
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime sTime = LocalTime.parse(startTime, formatter);
            LocalTime eTime = LocalTime.parse(endTime, formatter);
            if (eTime.isBefore(sTime)) {
                CookieBar.build(this)
                .setMessage(getString(R.string.time_slots_invalid))
                .setBackgroundColor(R.color.red)
                .setCookiePosition(CookieBar.BOTTOM).show();
                return;
            }

            // ADD WORK
            Work work = new Work();
            int day = daysSpinner.getSelectedItemPosition();
            work.setEmployeeDocumentId(employeeDocumentId);
            work.setDay(day);
            work.setStartTime(startTime);
            work.setEndTime(endTime);
            work.setComments(comments.getText().toString());
            addWorkToEmployee(work);

            dialog.cancel();
        });

        return super.onOptionsItemSelected(item);
    }


    public void addWorkToEmployee(Work work){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("works")
        .add(work)
        .addOnSuccessListener(documentReference -> {
            Log.d(Global.TAG, "Document added with ID: " + documentReference.getId());
            CookieBar.build(this)
            .setMessage(getString(R.string.work_add_success))
            .setBackgroundColor(R.color.green)
            .setCookiePosition(CookieBar.BOTTOM).show();
            // UPDATE DATA
            worksList.add(work);
            adapter.notifyDataSetChanged();
        })
        .addOnFailureListener(e -> {
            Log.w(Global.TAG, "Error adding document", e);
            CookieBar.build(this)
            .setMessage(getString(R.string.work_add_fail))
            .setBackgroundColor(R.color.red)
            .setCookiePosition(CookieBar.BOTTOM).show();
        });
    }
}