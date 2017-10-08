package com.example.farseer;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/10/8.
 * This class is a adapter for View list which is used to shoe all reports for a certain location.
 */

public class ReportAdapter extends BaseAdapter{

    List<HashMap<String,String>> list;
    Context context;
    ArrayList<Report> reports;

    ReportAdapter(@NonNull Context context, List<HashMap<String,String>> list) {

        this.context = context;
        this.list = list;
        reports = new ArrayList<>();
        Resources res = context.getResources();
        HashMap<String, String> cox;
        for (int i = 0; i < list.size(); i++)
        {
            cox = list.get(i);
            String username = cox.get("name");
            String date = cox.get("date");
            String species = cox.get("species");
            String description = cox.get("context");
            reports.add(new Report(username, date, description, species));
        }
    }

    @Override
    public int getCount() {
        return reports.size();
    }

    @Override
    public Object getItem(int i) {
        return reports.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.custon_listview,parent,false);
        TextView username = (TextView)row.findViewById(R.id.usernamee);
        TextView date = (TextView)row.findViewById(R.id.date);
        TextView description = (TextView)row.findViewById(R.id.description);
        TextView species = (TextView)row.findViewById(R.id.species);
        Report currentReport = reports.get(position);
        username.setText(currentReport.username);
        date.setText(currentReport.date);
        description.setText(currentReport.description);
        species.setText(currentReport.species);

        return row;
    }
}
