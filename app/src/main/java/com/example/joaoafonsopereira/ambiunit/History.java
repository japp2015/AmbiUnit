package com.example.joaoafonsopereira.ambiunit;

import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v4.app.Fragment;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;


/**
 * Created by Asus on 31/03/2019.
 */

public class History extends Fragment {

    private static final String TAG = "History";
    DatabaseConnection db;
    String username;
    String sensor;
    private LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
    private GraphView graph;
    private TextView date_choice;
    private DatePickerDialog.OnDateSetListener date;
    private Button send_btn;
    String date_txt = new String();

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history, container, false);

        db = new DatabaseConnection(History.this.getActivity());
        username = getActivity().getIntent().getStringExtra("username");

        graph = (GraphView) view.findViewById(R.id.graph);

        date_choice = (TextView) view.findViewById(R.id.date_choice);

        date_choice.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        getActivity(),
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        date,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Log.d(TAG, "onDateSet: mm/dd/yyyy: " + month + "/" + day + "/" + year);
                String d = String.valueOf(day);
                String m = String.valueOf(month);
                if (d.length()==1) {
                    d = "0" + d;
                }
                if (m.length()==1) {
                    m = "0" + m;
                }
                date_txt = m + "/" + d + "/" + year;
                date_choice.setText(date_txt);
            }

        };

        send_btn = view.findViewById(R.id.send_btn);
        send_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (date_choice.equals("select")) {
                    Toast.makeText(getContext(), "Choose a date", Toast.LENGTH_SHORT).show();
                } else {
                    date_txt = date_txt.replace("/", "-");
                    ArrayList<String> meas = db.getMeasurementsByDate(date_txt, username);
                    if (meas.size()==0) {
                        Toast.makeText(getActivity(), "No measurements made", Toast.LENGTH_SHORT).show();
                    }
                    else {

                        String data = "TIME|CO-AX|CO-D4|MiCS|TEMP|HUM\n\n";
                        for (int i = 0; i<meas.size(); i=i+6) {
                            data = data + meas.get(i) + "   " + meas.get(i+1) + "   " +  meas.get(i+2) + "   " + meas.get(i+3) + "   " + meas.get(i+4) + "   " + meas.get(i+5)+ "\n";
                        }

                        String email = db.getEmail(username);

                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                        emailIntent.setData(Uri.parse("mailto:" + email));
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Data from " + date_txt);
                        emailIntent.putExtra(Intent.EXTRA_TEXT, data);


                        try {
                            startActivity(Intent.createChooser(emailIntent, "Send " + sensor + " data from " + date_txt + " using..."));
                        } catch (ActivityNotFoundException ex) {
                            Toast.makeText(getActivity(), "No email clients installed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }


            }

        });

        ArrayList<String> co_ax = db.getCOAXMeasurements(username);
        ArrayList<String> co_d4 = db.getCOD4Measurements(username);
        ArrayList<String> mics = db.getMICSMeasurements(username);

        int size = co_ax.size();

        LineGraphSeries<DataPoint> ax_series = new LineGraphSeries<>();
        LineGraphSeries<DataPoint> d4_series = new LineGraphSeries<>();
        LineGraphSeries<DataPoint> mics_series = new LineGraphSeries<>();

        for(int i=1;i<size;i++){
            DataPoint point = new DataPoint(i, Double.parseDouble(co_ax.get(co_ax.size()-i-1)));
            ax_series.appendData(point, true, size);
        }

        for(int i=1;i<size;i++){
            DataPoint point = new DataPoint(i, Double.parseDouble(co_d4.get(co_ax.size()-i-1)));
            d4_series.appendData(point, true, size);
        }

        for(int i=1;i<size;i++){
            DataPoint point = new DataPoint(i, Double.parseDouble(mics.get(co_ax.size()-i-1)));
            mics_series.appendData(point, true, size);
        }

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMaxX(size);

        ax_series.setColor(Color.RED);
        ax_series.setTitle("CO-AX");
        d4_series.setColor(Color.GREEN);
        d4_series.setTitle("CO-D4");
        mics_series.setColor(Color.BLUE);
        mics_series.setTitle("MiCS-4514");



        graph.addSeries(ax_series);
        graph.addSeries(d4_series);
        graph.addSeries(mics_series);

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        return view;


    }


}
