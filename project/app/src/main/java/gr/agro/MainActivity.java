package gr.agro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import org.aviran.cookiebar2.CookieBar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    BottomNavigationView bottomNavigationView;
    FirebaseAuth mAuth;
    EmployeesFragment employeesFragment = new EmployeesFragment();
    FarmsFragment farmsFragment = new FarmsFragment();
    CalendarFragment calendarFragment = new CalendarFragment();
    ReportFragment reportFragment = new ReportFragment();
    String startDate = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if ( Global.isNetworkConnected(getApplicationContext()) == false ){
            CookieBar.build(this)
            .setMessage(getString(R.string.internet_required))
            .setBackgroundColor(R.color.red)
            .setCookiePosition(CookieBar.BOTTOM).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if ( currentUser == null ){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.employees);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_menu, menu);
        return true;
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.employees) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, employeesFragment, "employees").commit();
            return true;
        } else if (id == R.id.farms) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, farmsFragment, "farms").commit();
            return true;
        } else if (id == R.id.calendar) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, calendarFragment, "calendar").commit();
            return true;
        } else if (id == R.id.report) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, reportFragment, "report").commit();
            return true;
        }
        return true;
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
            if (currentFragment instanceof EmployeesFragment) {
                addEmployee();
            } else if (currentFragment instanceof FarmsFragment) {
                addFarm();
            }
        }
        return super.onOptionsItemSelected(item);
    }


    private void addFarm(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.farm_alert, null);
        builder.setView(dialogView);

        EditText nameEditText = dialogView.findViewById(R.id.name);
        EditText sizeEditText = dialogView.findViewById(R.id.size);

        builder.setPositiveButton(getString(R.string.add), null)
        .setNegativeButton(getString(R.string.cancel), null);
        builder.setCancelable(false);

        final AlertDialog dialog = builder.create();
        dialog.show();

        // USER CLICKS THE POSITIVE BUTTON
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(v -> {
            if ( !Global.checkEditTexts(new EditText[]{nameEditText, sizeEditText}) ){
                CookieBar.build(MainActivity.this)
                .setMessage(getString(R.string.fields_missing_error))
                .setBackgroundColor(R.color.red)
                .setCookiePosition(CookieBar.BOTTOM).show();
                return;
            }
            // SUBMIT FARM
            farmsFragment.saveFarm(null, nameEditText.getText().toString(), sizeEditText.getText().toString());
            dialog.cancel();
        });
    }


    private void addEmployee(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.employee_alert, null);
        builder.setView(dialogView);
        EditText fullnameEditText = dialogView.findViewById(R.id.fullname);
        EditText telEditText = dialogView.findViewById(R.id.tel);
        EditText rateEditText = dialogView.findViewById(R.id.rate);
        EditText startDateEditText = dialogView.findViewById(R.id.startDate);
        startDateEditText.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            if ( !startDate.isEmpty() ){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar calendar = Calendar.getInstance();
                try {
                    Date date = sdf.parse(startDate);
                    calendar.setTime(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
            }
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
                monthOfYear = monthOfYear + 1;
                startDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year1, monthOfYear, dayOfMonth);
                startDateEditText.setText(startDate);
                Log.d(Global.TAG, startDate);
            }, year, month, day);
            datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
            datePickerDialog.show();
        });
        builder.setPositiveButton(getString(R.string.add), null)
        .setNegativeButton(getString(R.string.cancel), null);
        builder.setCancelable(false);

        final AlertDialog dialog = builder.create();
        dialog.show();

        // USER CLICKS THE POSITIVE BUTTON
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(v -> {
            if ( !Global.checkEditTexts(new EditText[]{fullnameEditText, telEditText, rateEditText, startDateEditText}) ){
                CookieBar.build(MainActivity.this)
                .setMessage(getString(R.string.fields_missing_error))
                .setBackgroundColor(R.color.red)
                .setCookiePosition(CookieBar.BOTTOM).show();
                return;
            }

            // SUBMIT EMPLOYEE
            employeesFragment.saveEmployee(
                null,
                fullnameEditText.getText().toString(),
                telEditText.getText().toString(),
                rateEditText.getText().toString(),
                startDateEditText.getText().toString(),
                1,
                null
            );

            dialog.cancel();
        });
    }

}