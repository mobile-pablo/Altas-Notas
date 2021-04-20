package com.company.altasnotas.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.company.altasnotas.R;
import com.webianks.library.scroll_choice.ScrollChoice;

import java.util.ArrayList;
import java.util.List;

public class WalkthroughFragmentAdapter extends PagerAdapter {
Context context;
List<String> age = new ArrayList<>();
    public WalkthroughFragmentAdapter(Context context) {
        this.context =context;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view ==(ConstraintLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view= layoutInflater.inflate(R.layout.fragment_walkthough_slide,container,false);

        /**
         * I made so weird names becouse i will make those input on most of slides but with diffrent textview and data.
         * Only Age will be on slide first so it remain as it is
         */
        //Universal
        TextView counter = view.findViewById(R.id.walkthrough_slide_counter);

        TextView text_one, text_two,text_third;
        EditText input_one_s,input_two_s,input_third_s;

        text_one =view.findViewById(R.id.walkthrough_slide_first_string_textview);
        text_two =view.findViewById(R.id.walkthrough_slide_second_string_textview);
        text_third =view.findViewById(R.id.walkthrough_slide_third_string_textview);


        input_one_s = view.findViewById(R.id.walkthrough_slide_string_first_input);
        input_two_s = view.findViewById(R.id.walkthrough_slide_string_second_input);
        input_third_s = view.findViewById(R.id.walkthrough_slide_string_third_input);
        counter.setText("Step "+ (position+1)+"/3");

        //Slide 1
        TextView text_age;
        ScrollChoice input_age;

        text_age = view.findViewById(R.id.walkthrough_slide_age_textview);
        input_age = view.findViewById(R.id.walkthrough_slide_age_input);

        input_age.addItems(age,19);

        switch (position){
            case 0:
                text_one.setText("Name");
                text_two.setText("Surname");
                text_third.setVisibility(View.GONE);
                text_age.setVisibility(View.VISIBLE);

                input_one_s.setHint("John");
                input_two_s.setHint("Kowalski");
                loadAge();
                input_age.setVisibility(View.VISIBLE);
                input_third_s.setVisibility(View.GONE);
                break;

            case 1:
                text_one.setText("");
                text_two.setText("");
                text_third.setText("");
                text_third.setVisibility(View.VISIBLE);
                text_age.setVisibility(View.GONE);
                input_age.setVisibility(View.GONE);
                input_one_s.setHint("");
                input_two_s.setHint("");
                break;


            case 2:

                break;
        }







        container.addView(view);
        return view;
    }

    private void loadAge() {
        age.clear();
        for (int i = 1; i <=100 ; i++) {
                age.add(String.valueOf(i));
        }
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ConstraintLayout) object);
    }
}
