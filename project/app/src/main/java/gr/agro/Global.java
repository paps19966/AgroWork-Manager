package gr.agro;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Global {

    public static String TAG = "kris";

    public static boolean isNetworkConnected(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if ( ni == null ){
            return false;
        } else{
            return true;
        }
    }


    public static boolean checkEditTexts(EditText[] edts){
        for(EditText et : edts){
            if ( et.getText().toString().trim().length() == 0 ){
                return false;
            }
        }
        return true;
    }


    public static String convertDate(String dateStr){
        String formattedDateString = "";
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat targetFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date date = originalFormat.parse(dateStr);
            formattedDateString = targetFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            return formattedDateString;
        }
    }


    public static String getEmployeeFullNameByEmployeeDocumentId(List<Employee> employees, String employeeDocumentId){
        if ( employees.isEmpty() ){
            return null;
        }
        for(Employee employee : employees){
            if ( employee.getDocumentId().equals(employeeDocumentId) ){
                return employee.getFullname();
            }
        }
        return null;
    }

}
