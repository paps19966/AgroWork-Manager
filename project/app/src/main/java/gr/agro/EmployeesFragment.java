package gr.agro;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableRow;
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

public class EmployeesFragment extends Fragment {

    private ListView listView;
    private EmployeeAdapter adapter;
    private List<Employee> employeesList = new ArrayList<>();
    private List<Farm> farmsList = new ArrayList<>();
    private List<CheckBox> farmCheckBoxes = new ArrayList<>();


    public EmployeesFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fetchFarms();
        fetchEmployees();
    }


    @Override
    public void onStart(){
        super.onStart();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_employees, container, false);
        listView = view.findViewById(R.id.listViewEmployees);
        adapter = new EmployeeAdapter(getActivity(), employeesList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            editEmployee(view1);
        });
        return view;
    }


    public void fetchEmployees(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("employees")
        .orderBy("created_at", Query.Direction.DESCENDING)
        .get()
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Employee> list = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Employee employee = document.toObject(Employee.class);
                    employee.setDocumentId(document.getId());
                    list.add(employee);
                }
                updateData(list);
            } else {
                Log.w(Global.TAG, "Error getting documents: ", task.getException());
            }
        });
    }


    public void fetchEmpFarmsForEmployee(String documentId, View dialogView){
        farmCheckBoxes.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("emp_farms")
        .whereEqualTo("employeeDocumentId", documentId)
        .get()
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<EmpFarm> empFarmList = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    EmpFarm empFarm = document.toObject(EmpFarm.class);
                    empFarmList.add(empFarm);
                }

                if ( !farmsList.isEmpty() ){
                    LinearLayout farmsView = dialogView.findViewById(R.id.farms);

                    for(int i = 0; i < farmsList.size(); i++) {

                        String farmId = farmsList.get(i).getDocumentId();

                        LinearLayout layoutRow = new LinearLayout(getContext());
                        layoutRow.setOrientation(LinearLayout.HORIZONTAL);
                        layoutRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        layoutRow.setPadding(0, 0, 0, 10);

                        CheckBox farmCheckBox = new CheckBox(getContext());
                        farmCheckBox.setText(farmsList.get(i).getName());
                        farmCheckBox.setTag(farmId);

                        Button scheduleBtn = new Button(getContext());
                        scheduleBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, getContext().getDrawable(android.R.drawable.ic_menu_add), null);

                        Drawable drawableRight = ContextCompat.getDrawable(getContext(), android.R.drawable.ic_menu_add);
                        if (drawableRight != null) {
                            drawableRight.setBounds(0, 0, drawableRight.getIntrinsicWidth(), drawableRight.getIntrinsicHeight());
                            scheduleBtn.setCompoundDrawables(null, null, drawableRight, null);
                        }
                        scheduleBtn.setText("Εργασίες");
                        scheduleBtn.setTextSize(13);
                        scheduleBtn.setAllCaps(false);
                        scheduleBtn.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

                        // EMPLOYEE IS IN THIS FARM
                        if ( !empFarmList.isEmpty() && empFarmList.stream().anyMatch(elem -> elem.getFarmDocumentId().equals(farmId)) ){
                            farmCheckBox.setChecked(true);
                        }

                        layoutRow.addView(farmCheckBox);
                        farmsView.addView(layoutRow);
                        farmCheckBoxes.add(farmCheckBox);
                    }
                }
            } else {
                Log.d(Global.TAG, "Error getting documents: ", task.getException());
            }
        });
    }


    public void fetchFarms(){
        farmsList.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("farms")
        .orderBy("created_at", Query.Direction.DESCENDING)
        .get()
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Farm farm = document.toObject(Farm.class);
                    farm.setDocumentId(document.getId());
                    farmsList.add(farm);
                }
            } else {
                Log.w(Global.TAG, "Error getting documents: ", task.getException());
            }
        });
    }


    public void updateData(List<Employee> list){
        employeesList.clear();
        employeesList.addAll(list);
        if ( adapter != null ){
            adapter.notifyDataSetChanged();
        }
    }


    private void editEmployee(View listViewItem) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.employee_alert, null);

        TextView documentId = listViewItem.findViewById(R.id.documentId);
        TextView isActive = listViewItem.findViewById(R.id.isActive);

        // FETCH FARMS FOR EMPLOYEE
        fetchEmpFarmsForEmployee(documentId.getText().toString(), dialogView);

        dialogView.findViewById(R.id.startDate).setVisibility(View.GONE);
        dialogView.findViewById(R.id.farmsPicker).setVisibility(View.VISIBLE);

        EditText fullnameEditText = dialogView.findViewById(R.id.fullname);
        TextView fullname = listViewItem.findViewById(R.id.fullname);
        fullnameEditText.setText(fullname.getTag().toString());

        EditText telEditText = dialogView.findViewById(R.id.tel);
        TextView tel = listViewItem.findViewById(R.id.tel);
        telEditText.setText(tel.getTag().toString());

        EditText rateEditText = dialogView.findViewById(R.id.rate);
        TextView rate = listViewItem.findViewById(R.id.rate);
        rateEditText.setText(rate.getTag().toString());

        Spinner isActiveSpinner = dialogView.findViewById(R.id.isActive);
        isActiveSpinner.setVisibility(View.VISIBLE);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.is_active_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        isActiveSpinner.setAdapter(adapter);
        String spinnerValue = "Ενεργός";
        int spinnerValueInt = Integer.parseInt(isActive.getText().toString());
        if ( spinnerValueInt != 1 ){
            spinnerValue = "Ανενεργός";
        }
        int position = adapter.getPosition(spinnerValue);
        isActiveSpinner.setSelection(position);

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

            // FIELDS CHECK
            if ( !Global.checkEditTexts(new EditText[]{fullnameEditText, telEditText, rateEditText}) ){
                CookieBar.build(getActivity())
                .setMessage(getString(R.string.fields_missing_error))
                .setBackgroundColor(R.color.red)
                .setCookiePosition(CookieBar.BOTTOM).show();
                return;
            }

            List<EmpFarm> empFarms = processEmpFarms(documentId.getText().toString());
            // SAVE EMPLOYEE
            saveEmployee(
                documentId.getText().toString(),
                fullnameEditText.getText().toString(),
                telEditText.getText().toString(),
                rateEditText.getText().toString(),
                null,
                isActiveSpinner.getSelectedItem().toString().equals("Ενεργός") ? 1 : 0,
                empFarms
            );

            dialog.cancel();
        });
    }


    public List<EmpFarm> processEmpFarms(String documentId){
        List<EmpFarm> empFarms = new ArrayList<>();
        for (CheckBox checkBox : farmCheckBoxes){
            if ( checkBox.isChecked() ){
                EmpFarm empFarm = new EmpFarm();
                empFarm.setEmployeeDocumentId(documentId);
                empFarm.setFarmDocumentId(checkBox.getTag().toString());
                empFarms.add(empFarm);
            }
        }
        return empFarms;
    }


    public void saveEmployee(String documentId, String fullname, String tel, String rate, String startDate, int isActive, List<EmpFarm> empFarms){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> employee = new HashMap<>();
        employee.put("fullname", fullname);
        employee.put("tel", tel);
        employee.put("rate_hour", rate);
        employee.put("is_active", isActive);
        // ADD
        if ( documentId == null ){
            employee.put("start_date", startDate);
            employee.put("created_at", FieldValue.serverTimestamp());
            db.collection("employees").add(employee)
            .addOnSuccessListener(documentReference -> {
                Log.d(Global.TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                CookieBar.build(getActivity())
                .setMessage(getString(R.string.employee_add_success))
                .setBackgroundColor(R.color.green)
                .setCookiePosition(CookieBar.BOTTOM).show();
                fetchEmployees();
            })
            .addOnFailureListener(e -> {
                Log.w(Global.TAG, "Error adding document", e);
                CookieBar.build(getActivity())
                .setMessage(getString(R.string.employee_add_fail))
                .setBackgroundColor(R.color.red)
                .setCookiePosition(CookieBar.BOTTOM).show();
            });
        }
        // UPDATE
        else{
            db.collection("employees").document(documentId).update(employee)
            .addOnSuccessListener(aVoid -> {
                Log.d(Global.TAG, "DocumentSnapshot successfully updated");
                CookieBar.build(getActivity())
                .setMessage(getString(R.string.employee_save_success))
                .setBackgroundColor(R.color.green)
                .setCookiePosition(CookieBar.BOTTOM).show();
                fetchEmployees();
            })
            .addOnFailureListener(e -> {
                Log.w(Global.TAG, "Error saving document", e);
                CookieBar.build(getActivity())
                .setMessage(getString(R.string.employee_save_fail))
                .setBackgroundColor(R.color.red)
                .setCookiePosition(CookieBar.BOTTOM).show();
            });

            processFarmsForEmployee(documentId, empFarms);

        }
    }


    public void processFarmsForEmployee(String documentId, List<EmpFarm> empFarms){
        // DELETE PREVIOUS EMP FARMS
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("emp_farms")
        .whereEqualTo("employeeDocumentId", documentId)
        .get()
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    db.collection("emp_farms").document(document.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> Log.d(Global.TAG, "Document successfully deleted"))
                    .addOnFailureListener(e -> Log.w(Global.TAG, "Error deleting document", e));
                }
                // ADD ALL FARMS TO EMPLOYEE
                addFarmsToEmployee(empFarms);
            } else {
                Log.d(Global.TAG, "Error getting documents: ", task.getException());
            }
        });
    }


    public void addFarmsToEmployee(List<EmpFarm> empFarms){
        if ( empFarms != null && !empFarms.isEmpty() ){
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            for (EmpFarm empFarm : empFarms) {
                db.collection("emp_farms")
                .add(empFarm)
                .addOnSuccessListener(documentReference -> {
                    Log.d(Global.TAG, "EmpFarm added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.w(Global.TAG, "Error adding document", e);
                    CookieBar.build(getActivity())
                    .setMessage(getString(R.string.employee_save_farms_fail))
                    .setBackgroundColor(R.color.red)
                    .setCookiePosition(CookieBar.BOTTOM).show();
                });
            }
        }
    }

}