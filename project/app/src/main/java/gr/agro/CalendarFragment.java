package gr.agro;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;


public class CalendarFragment extends Fragment {

    private SimpleDateFormat dateFormat1 = new SimpleDateFormat("MMMM", new Locale("el", "GR"));
    private SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy", Locale.getDefault());
    private ArrayList<Integer> colors = new ArrayList<>();
    private CompactCalendarView calendarView;
    private EventAdapter adapter;
    private List<Work> eventsList = new ArrayList<>();
    private ListView listView;
    private HashMap<String, String> greekMonths = new HashMap<>();


    public CalendarFragment() {
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

        generateMonths();
        generateColors();
        fetchData();

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        listView = view.findViewById(R.id.listView);
        adapter = new EventAdapter(getActivity(), eventsList);
        listView.setAdapter(adapter);

        TextView monthYearTextTv = view.findViewById(R.id.monthYearTextTv);
        Date today = new Date();
        String month = greekMonths.get(dateFormat1.format(today));
        String year = dateFormat2.format(today);
        monthYearTextTv.setText(String.format("%s %s", month, year));

        calendarView = view.findViewById(R.id.calendarView);
        calendarView.setCurrentSelectedDayBackgroundColor(Color.GRAY);
        calendarView.shouldDrawIndicatorsBelowSelectedDays(true);
        calendarView.setLocale(TimeZone.getDefault(), new Locale("el", "GR"));

        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                List<Event> events = calendarView.getEvents(dateClicked);
                Log.d(Global.TAG, "Day was clicked: " + dateClicked + " with events " + events);
                updateListView(events);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                //Log.d(Global.TAG, "Month was scrolled to: " + dateFormat1.format(firstDayOfNewMonth));
                String month = greekMonths.get(dateFormat1.format(firstDayOfNewMonth));
                String year = dateFormat2.format(firstDayOfNewMonth);
                monthYearTextTv.setText(String.format("%s %s", month, year));
            }
        });

        return view;
    }


    private void updateListView(List<Event> events) {
        eventsList.clear();
        for (Event event : events) {
            if ( event.getData() instanceof Work ){
                eventsList.add((Work) event.getData());
            }
        }
        adapter.notifyDataSetChanged();
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
                fetchWorks(list);
            } else {
                Log.w(Global.TAG, "Error getting documents: ", task.getException());
            }
        });
    }


    private void fetchWorks(List<Employee> employees){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("works")
        .get()
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int counter = 0;
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Work work = document.toObject(Work.class);
                    work.setDocumentId(document.getId());
                    work.setEmployeeFullname(Global.getEmployeeFullNameByEmployeeDocumentId(employees, work.getEmployeeDocumentId()));
                    for(int year = 2024; year < 2027; year++){
                        List<Event> events = generateEvents(counter, year, work);
                        for(Event event : events){
                            calendarView.addEvent(event);
                        }
                        counter++;
                    }
                }
            } else {
                Log.w(Global.TAG, "Error getting documents: ", task.getException());
            }
        });
    }


    public List<Event> generateEvents(int counter, int year, Work work) {
        List<Event> events = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        // Set to the first day of the given year
        calendar.set(year, Calendar.JANUARY, 1);
        while(calendar.get(Calendar.DAY_OF_WEEK) != (work.getDay() + 2)){
            calendar.add(Calendar.DATE, 1);
        }
        // Iterate over all week days in the year
        while (calendar.get(Calendar.YEAR) == year) {
            long timeInMillis = calendar.getTimeInMillis();
            Event event = new Event(colors.get(counter), timeInMillis, work);
            events.add(event);
            // Move to the next week day
            calendar.add(Calendar.DATE, 7);
        }
        return events;
    }


    private void generateMonths(){
        greekMonths.put("Ιανουαρίου", "Ιανουάριος");
        greekMonths.put("Φεβρουαρίου", "Φεβρουάριος");
        greekMonths.put("Μαρτίου", "Μάρτιος");
        greekMonths.put("Απριλίου", "Απρίλιος");
        greekMonths.put("Μαΐου", "Μάϊος");
        greekMonths.put("Ιουνίου", "Ιούνιος");
        greekMonths.put("Ιουλίου", "Ιούλιος");
        greekMonths.put("Αυγούστου", "Αύγουστος");
        greekMonths.put("Σεπτεμβρίου", "Σεπτέμβριος");
        greekMonths.put("Οκτωβρίου", "Οκτώβριος");
        greekMonths.put("Νοεμβρίου", "Νοέμβριος");
        greekMonths.put("Δεκεμβρίου", "Δεκέμβριος");
    }


    private void generateColors(){
        Random random = new Random();
        Integer[] predefinedColors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN, Color.MAGENTA};
        for (Integer color : predefinedColors) {
            colors.add(color);
        }
        while (colors.size() < 1000) {
            int alpha = 255; // Full opacity
            int red = random.nextInt(256);
            int green = random.nextInt(256);
            int blue = random.nextInt(256);
            int customColor = Color.argb(alpha, red, green, blue);
            if (!colors.contains(customColor)) {
                colors.add(customColor);
            }
        }
        // SORT COLORS
        Collections.sort(colors, (color1, color2) -> {
            float[] hsv1 = new float[3];
            float[] hsv2 = new float[3];
            Color.colorToHSV(color1, hsv1);
            Color.colorToHSV(color2, hsv2);
            return Float.compare(hsv1[0], hsv2[0]);
        });
    }
}