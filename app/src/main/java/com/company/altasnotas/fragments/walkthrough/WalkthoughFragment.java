package com.company.altasnotas.fragments.walkthrough;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.adapters.WalkthroughFragmentAdapter;
import com.company.altasnotas.fragments.home.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class WalkthoughFragment extends Fragment {

    private  int currentPage=0;
    private WalkthroughFragmentAdapter slideAdapter;
    private ArrayList<TextView> mDots;
    private ViewPager viewPager;
    private LinearLayout linearLayout;
    private Button nextBtn, backBtn;
    private MainActivity mainActivity;
    private DatabaseReference database;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
      View view =  inflater.inflate(R.layout.fragment_walkthough, container, false);

        mainActivity = (MainActivity) getActivity();
        BottomNavigationView bottomNavigationView = mainActivity.findViewById(R.id.main_nav_bottom);
        bottomNavigationView.setVisibility(View.GONE);

      viewPager = view.findViewById(R.id.walkthrough_ViewPager);
      linearLayout = view.findViewById(R.id.walkthrough_dots_layout);
      slideAdapter = new WalkthroughFragmentAdapter(mainActivity.getSupportFragmentManager());
      viewPager.setAdapter(slideAdapter);

      addDots(0);

      database = FirebaseDatabase.getInstance().getReference();
      mAuth =FirebaseAuth.getInstance();

      nextBtn = view.findViewById(R.id.walkthrough_next_btn);
      backBtn = view.findViewById(R.id.walkthrough_back_btn);

      viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
          @Override
          public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

          }

          @Override
          public void onPageSelected(int position) {
            addDots(position);
            currentPage=position;
            int lastOne = mDots.size() -1;

              if (position == 0) {
                  nextBtn.setEnabled(true);
                  backBtn.setEnabled(false);

                  backBtn.setVisibility(View.INVISIBLE);
                  nextBtn.setText("Next");

                  backBtn.setText("");
              } else if (position == lastOne) {
                  nextBtn.setText("Finish");
                  backBtn.setText("Back");

              } else {
                  nextBtn.setEnabled(true);
                  backBtn.setEnabled(true);

                  backBtn.setVisibility(View.VISIBLE);
                  nextBtn.setText("Next");
                  backBtn.setText("Back");

              }
          }

          @Override
          public void onPageScrollStateChanged(int state) {

          }
      });

      backBtn.setOnClickListener(v -> viewPager.setCurrentItem(currentPage-1));

      nextBtn.setOnClickListener(v -> {
          if(currentPage+1==1){
            //  slideAdapter.updateBIO(1);
          }else if(currentPage+1==2){
          //    slideAdapter.updateBIO(2);
          }else if(currentPage+1==3){
              int count = getActivity().getSupportFragmentManager().getBackStackEntryCount();
          //    slideAdapter.updateBIO(3);
              for (int i = 0; i < count; i++) {
                  getActivity().getSupportFragmentManager().popBackStack();
                     }

              bottomNavigationView.setVisibility(View.VISIBLE);
              getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new HomeFragment()).commit();

          }
          viewPager.setCurrentItem(currentPage+1);
      });


      return view;
    }

    private void addDots(int i) {
        mDots = new ArrayList<>(3);
        for (int j = 0; j < 3; j++) {
            mDots.add(new TextView(getContext()));
        }

        linearLayout.removeAllViews();
        int light =  getResources().getColor(R.color.project_light_cyan);
        int dark =  getResources().getColor(R.color.project_dark_cyan);
        for(TextView textView : mDots){
            textView = new TextView(requireContext());
            textView.setText(Html.fromHtml("&#8226;"));
            textView.setTextSize(35F);
            textView.setTextColor(light);

        }

        if(mDots.size() >0){
            mDots.get(i).setTextColor(dark);
        }


    }
}