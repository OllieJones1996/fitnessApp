package com.example.ollie.fitnessapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


/**
 * A simple {@link Fragment} subclass.
 */
public class graphFragment extends Fragment {

    private GraphView graphSteps;
    private GraphView graphHR;
    private GraphView graphMood;


    public graphFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {

        super.onStart();
        graphPopulate();
    }

    public void graphPopulate(){
        graphSteps = (GraphView) this.getView().findViewById(R.id.graphSteps);
        graphHR = (GraphView) this.getView().findViewById(R.id.graphHR);
        graphMood = (GraphView) this.getView().findViewById(R.id.graphMood);

        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3)
        });
        graphSteps.addSeries(series);

        LineGraphSeries<DataPoint> series1 = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3)
        });
        graphHR.addSeries(series1);
        BarGraphSeries<DataPoint> series2 = new BarGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3)
        });
        graphMood.addSeries(series2);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_graph, container, false);
    }

}