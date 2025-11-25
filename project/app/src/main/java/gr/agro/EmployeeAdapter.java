package gr.agro;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.util.List;

public class EmployeeAdapter extends ArrayAdapter<Employee> {

    public Context context;

    public EmployeeAdapter(Context context, List<Employee> employees) {
        super(context, 0, employees);
        this.context = context;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Employee employee = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.employee_list_item, parent, false);
        }

        TextView documentId = convertView.findViewById(R.id.documentId);
        documentId.setText(employee.getDocumentId());

        TextView fullname = convertView.findViewById(R.id.fullname);
        fullname.setText(employee.getFullname());
        fullname.setTag(employee.getFullname());

        TextView tel = convertView.findViewById(R.id.tel);
        tel.setText("Τηλ:" + employee.getTel());
        tel.setTag(employee.getTel());

        TextView rate = convertView.findViewById(R.id.rate);
        rate.setText("Μισθός/ώρα:" + employee.getRate_hour() + "€");
        rate.setTag(employee.getRate_hour());

        TextView startDate = convertView.findViewById(R.id.startDate);
        startDate.setText("Έναρξη:" + Global.convertDate(employee.getStart_date()));
        startDate.setTag(employee.getStart_date());

        if ( employee.getIs_active() != 1 ){
            convertView.setBackgroundColor(getContext().getColor(R.color.inactive));
        } else{
            convertView.setBackgroundColor(getContext().getColor(R.color.white));
        }
        TextView isActive = convertView.findViewById(R.id.isActive);
        isActive.setText(employee.getIs_active() + "");

        Button button = convertView.findViewById(R.id.editEmployeeBtn);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(context, EmployeeWorkActivity.class);
            intent.putExtra("employeeDocumentId", employee.getDocumentId());
            intent.putExtra("employeeFullname", employee.getFullname());
            context.startActivity(intent);
        });

        return convertView;
    }



}