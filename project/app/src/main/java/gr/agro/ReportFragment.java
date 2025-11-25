package gr.agro;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ReportFragment extends Fragment {

    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private ListView listView;
    private ReportAdapter adapter;
    private List<Employee> employeesList = new ArrayList<>();

    public ReportFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.add_menu, menu);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);
        listView = view.findViewById(R.id.listView);
        adapter = new ReportAdapter(getActivity(), employeesList);
        listView.setAdapter(adapter);
        return view;
    }


    @Override
    public void onStart(){
        super.onStart();
        fetchData();
    }


    public void updateData(List<Employee> list){
        employeesList.clear();
        employeesList.addAll(list);
        if ( adapter != null ){
            adapter.notifyDataSetChanged();
        }
    }


    public void fetchData(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("employees")
        .whereEqualTo("is_active",1)
        .get()
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Employee> list = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Employee employee = document.toObject(Employee.class);
                    employee.setDocumentId(document.getId());
                    list.add(employee);
                }
                fetchWorksForEmployees(list);
            } else {
                Log.w(Global.TAG, "Error getting documents: ", task.getException());
            }
        });
    }


    private void fetchWorksForEmployees(List<Employee> employees){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for(Employee employee : employees){
            db.collection("works")
            .whereEqualTo("employeeDocumentId", employee.getDocumentId())
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<Work> works = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Work work = document.toObject(Work.class);
                        work.setDocumentId(document.getId());
                        works.add(work);
                    }
                    employee.setWorks(works);
                    processData(employee);
                    updateData(employees);
                } else {
                    Log.w(Global.TAG, "Error getting documents: ", task.getException());
                }
            });
        }
    }


    private void processData(Employee employee){
        if ( employee.getWorks() == null || employee.getWorks().isEmpty() ){
            employee.setTotalHoursPerWeek(0);
            employee.setTotalSalaryPerWeek(0);
            return;
        }
        long totalDurationInMinutes = 0;
        for (Work work : employee.getWorks()) {
            LocalTime startTime = LocalTime.parse(work.getStartTime(), timeFormatter);
            LocalTime endTime = LocalTime.parse(work.getEndTime(), timeFormatter);
            long durationInMinutes = ChronoUnit.MINUTES.between(startTime, endTime);
            totalDurationInMinutes += durationInMinutes;
        }
        double totalWorksDurationPerWeekHours = totalDurationInMinutes / 60.0;
        employee.setTotalHoursPerWeek(totalWorksDurationPerWeekHours);
        double ratePerHour = Double.parseDouble(employee.getRate_hour());
        double totalSalaryPerWeek = 0;
        if ( totalWorksDurationPerWeekHours > 0 && ratePerHour > 0 ){
            totalSalaryPerWeek = totalWorksDurationPerWeekHours * ratePerHour;
        }
        employee.setTotalSalaryPerWeek(totalSalaryPerWeek);
        //Log.d(Global.TAG,  "employee: " + employee.getFullname() + " | duration: " + totalDurationInHours);
    }
}