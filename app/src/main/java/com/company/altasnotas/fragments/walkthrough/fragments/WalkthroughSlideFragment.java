package com.company.altasnotas.fragments.walkthrough.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.company.altasnotas.R;


public class WalkthroughSlideFragment extends Fragment {

    TextView textView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View view =  inflater.inflate(R.layout.fragment_walkthrough_slide, container, false);
        textView = view.findViewById(R.id.walkthrough_slide_counter);

        String message = getArguments().getString("MESSAGE");
        textView.setText(message);

        return view;
    }
}