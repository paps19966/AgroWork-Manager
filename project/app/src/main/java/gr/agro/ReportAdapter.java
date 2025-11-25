package gr.agro;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ReportAdapter extends ArrayAdapter<Employee> {

    public ReportAdapter(Context context, List<Employee> employees) {
        super(context, 0, employees);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Employee employee = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.report_list_item, parent, false);
        }

        TextView fullnameTv = convertView.findViewById(R.id.fullnameTv);
        fullnameTv.setText(employee.getFullname());

        TextView rateTv = convertView.findViewById(R.id.rateTv);
        rateTv.setText("Μισθός/ώρα:" + employee.getRate_hour() + "€");

        TextView totalWorksTv = convertView.findViewById(R.id.totalWorksTv);
        if ( employee.getWorks() != null ){
            totalWorksTv.setText(employee.getWorks().size() + " εργασίες/βδομάδα");
        }

        Log.d(Global.TAG, employee.getFullname() + " | " + employee.getTotalSalaryPerWeek());

        TextView totalSalaryPerWeekTv = convertView.findViewById(R.id.totalSalaryPerWeekTv);
        totalSalaryPerWeekTv.setText(employee.getTotalSalaryPerWeek() + "€/βδομάδα");

        return convertView;
    }

}