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

import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.firebase.firestore.FirebaseFirestore;

import org.aviran.cookiebar2.CookieBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventAdapter extends ArrayAdapter<Work> {

    private final Activity context;
    private List<String> daysOfWeek;

    public EventAdapter(Activity context, List<Work> events) {
        super(context, 0, events);
        this.context = context;
        String[] stringArray = context.getResources().getStringArray(R.array.week_days);
        daysOfWeek = new ArrayList<>(Arrays.asList(stringArray));
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Work work = getItem(position);
        if ( convertView == null ){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_list_item, parent, false);
        }

        TextView fullnameTv = convertView.findViewById(R.id.fullnameTv);
        fullnameTv.setText(work.getEmployeeFullname());

        TextView dayTv = convertView.findViewById(R.id.dayTv);
        dayTv.setText(daysOfWeek.get(work.getDay()));

        TextView hoursTv = convertView.findViewById(R.id.hoursTv);
        String startTime = work.getStartTime();
        String endTine = work.getEndTime();
        hoursTv.setText(String.format("%s - %s", startTime, endTine));

        TextView commentsTv = convertView.findViewById(R.id.commentsTv);
        commentsTv.setText(work.getComments());

        return convertView;
    }

}