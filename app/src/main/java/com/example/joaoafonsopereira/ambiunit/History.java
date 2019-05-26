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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v4.app.Fragment;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;


/**
 * Created by Asus on 31/03/2019.
 */

public class History extends Fragment implements AdapterView.OnItemSelectedListener {

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

        Spinner spinner = (Spinner) view.findViewById(R.id.sensor_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(History.this.getActivity(), R.array.sensors, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        spinner.setSelection(0);

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
                    ArrayList<String> meas = db.getMeasurementsByDate(date_txt, sensor, username);
                    if (meas.size()==0) {
                        Toast.makeText(getActivity(), "No measurements made", Toast.LENGTH_SHORT).show();
                    }
                    else {

                        String data = sensor + " data:\n";
                        for (int i = 0; i<meas.size(); i=i+2) {
                            data = data + meas.get(i) + "   " + meas.get(i+1) + "\n";
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

        return view;


    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        sensor = parent.getItemAtPosition(position).toString();
        graph.removeAllSeries();
        ArrayList<String> m = db.getMeasurements(sensor, username);
        if (m.size()>=20) {
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMaxX(19);
            series = new LineGraphSeries<>(new DataPoint[]{
                    new DataPoint(0, Integer.parseInt(m.get(m.size() - 20))),
                    new DataPoint(1, Integer.parseInt(m.get(m.size() - 19))),
                    new DataPoint(2, Integer.parseInt(m.get(m.size() - 18))),
                    new DataPoint(3, Integer.parseInt(m.get(m.size() - 17))),
                    new DataPoint(4, Integer.parseInt(m.get(m.size() - 16))),
                    new DataPoint(5, Integer.parseInt(m.get(m.size() - 15))),
                    new DataPoint(6, Integer.parseInt(m.get(m.size() - 14))),
                    new DataPoint(7, Integer.parseInt(m.get(m.size() - 13))),
                    new DataPoint(8, Integer.parseInt(m.get(m.size() - 12))),
                    new DataPoint(9, Integer.parseInt(m.get(m.size() - 11))),
                    new DataPoint(10, Integer.parseInt(m.get(m.size() - 10))),
                    new DataPoint(11, Integer.parseInt(m.get(m.size() - 9))),
                    new DataPoint(12, Integer.parseInt(m.get(m.size() - 8))),
                    new DataPoint(13, Integer.parseInt(m.get(m.size() - 7))),
                    new DataPoint(14, Integer.parseInt(m.get(m.size() - 6))),
                    new DataPoint(15, Integer.parseInt(m.get(m.size() - 5))),
                    new DataPoint(16, Integer.parseInt(m.get(m.size() - 4))),
                    new DataPoint(17, Integer.parseInt(m.get(m.size() - 3))),
                    new DataPoint(18, Integer.parseInt(m.get(m.size() - 2))),
                    new DataPoint(19, Integer.parseInt(m.get(m.size() - 1)))
            });
            graph.addSeries(series);
        }
        else if (m.size()>=15 && m.size()<20) {
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMaxX(14);
            series = new LineGraphSeries<>(new DataPoint[]{
                    new DataPoint(0, Integer.parseInt(m.get(m.size() - 15))),
                    new DataPoint(1, Integer.parseInt(m.get(m.size() - 14))),
                    new DataPoint(2, Integer.parseInt(m.get(m.size() - 13))),
                    new DataPoint(3, Integer.parseInt(m.get(m.size() - 12))),
                    new DataPoint(4, Integer.parseInt(m.get(m.size() - 11))),
                    new DataPoint(5, Integer.parseInt(m.get(m.size() - 10))),
                    new DataPoint(6, Integer.parseInt(m.get(m.size() - 9))),
                    new DataPoint(7, Integer.parseInt(m.get(m.size() - 8))),
                    new DataPoint(8, Integer.parseInt(m.get(m.size() - 7))),
                    new DataPoint(9, Integer.parseInt(m.get(m.size() - 6))),
                    new DataPoint(10, Integer.parseInt(m.get(m.size() - 5))),
                    new DataPoint(11, Integer.parseInt(m.get(m.size() - 4))),
                    new DataPoint(12, Integer.parseInt(m.get(m.size() - 3))),
                    new DataPoint(13, Integer.parseInt(m.get(m.size() - 2))),
                    new DataPoint(14, Integer.parseInt(m.get(m.size() - 1)))
            });
            graph.addSeries(series);
        }
        else if (m.size()>=10 && m.size()<15) {
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMaxX(9);
            series = new LineGraphSeries<>(new DataPoint[]{
                    new DataPoint(0, Integer.parseInt(m.get(m.size() - 10))),
                    new DataPoint(1, Integer.parseInt(m.get(m.size() - 9))),
                    new DataPoint(2, Integer.parseInt(m.get(m.size() - 8))),
                    new DataPoint(3, Integer.parseInt(m.get(m.size() - 7))),
                    new DataPoint(4, Integer.parseInt(m.get(m.size() - 6))),
                    new DataPoint(5, Integer.parseInt(m.get(m.size() - 5))),
                    new DataPoint(6, Integer.parseInt(m.get(m.size() - 4))),
                    new DataPoint(7, Integer.parseInt(m.get(m.size() - 3))),
                    new DataPoint(8, Integer.parseInt(m.get(m.size() - 2))),
                    new DataPoint(9, Integer.parseInt(m.get(m.size() - 1)))
            });
            graph.addSeries(series);
        }
        else if(m.size()>=5 && m.size()<10) {
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMaxX(4);
            series = new LineGraphSeries<>(new DataPoint[]{
                    new DataPoint(0, Integer.parseInt(m.get(m.size()-5))),
                    new DataPoint(1, Integer.parseInt(m.get(m.size()-4))),
                    new DataPoint(2, Integer.parseInt(m.get(m.size()-3))),
                    new DataPoint(3, Integer.parseInt(m.get(m.size()-2))),
                    new DataPoint(4, Integer.parseInt(m.get(m.size()-1)))
            });
            graph.addSeries(series);
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {

    }

}
