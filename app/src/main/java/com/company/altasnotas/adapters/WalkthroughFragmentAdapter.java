package com.company.altasnotas.adapters;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.company.altasnotas.fragments.walkthrough.fragments.WalkthroughSlideFragment;


public class WalkthroughFragmentAdapter extends FragmentStatePagerAdapter {

    public WalkthroughFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {

     WalkthroughSlideFragment fragment = new   WalkthroughSlideFragment();
        Bundle bundle = new Bundle();
        i = i + 1;
        bundle.putString("MESSAGE", "Step " + i+"/3" );
        fragment.setArguments(bundle);


        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }
    /*
    private Context context;
    private  FirebaseAuth mAuth;
    private DatabaseReference database;
    private List<String> age = new ArrayList<>();
    private List<String> gender = new ArrayList<>(2);
    private List<String> fav_music = new ArrayList<>(10);
    private ArrayAdapter<String>   spinner_adapter_music;
    private ArrayAdapter<String>   spinner_adapter_gender;
    private int default_index_age = 19;
    //Universal
    private TextView counter;

    private TextView text_one, text_two, text_spinner, space_spinner;
    private EditText input_one_s,input_two_s;
    private Spinner spinner;

    private TextView text_age;
    private ScrollChoice input_age;
    public WalkthroughFragmentAdapter(Context context) {
        this.context =context;
        loadAge();
        loadGender();
        loadMusic();
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
        mAuth = FirebaseAuth.getInstance();

         * I made so weird names becouse i will make those input on most of slides but with diffrent textview and data.
         * Only Age will be on slide first so it remain as it is


        counter= view.findViewById(R.id.walkthrough_slide_counter);

        text_one =view.findViewById(R.id.walkthrough_slide_first_string_textview);
        text_two =view.findViewById(R.id.walkthrough_slide_second_string_textview);
        text_spinner = view.findViewById(R.id.walkthrough_slide_spinner_textview);
        space_spinner = view.findViewById(R.id.walkthrough_slide_spinner_blank_space);

        input_one_s = view.findViewById(R.id.walkthrough_slide_string_first_input);
        input_two_s = view.findViewById(R.id.walkthrough_slide_string_second_input);
        spinner = view.findViewById(R.id.walkthrough_slide_spinner);

        spinner_adapter_music = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, fav_music);
        spinner_adapter_gender = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, gender);

        counter.setText("Step "+ (position+1)+"/3");

        //Slide 1


        text_age = view.findViewById(R.id.walkthrough_slide_age_textview);
        input_age = view.findViewById(R.id.walkthrough_slide_age_input);

        input_age.addItems(age,default_index_age);

        input_age.setOnItemSelectedListener(new ScrollChoice.OnItemSelectedListener() {
            @Override
            public void onItemSelected(ScrollChoice scrollChoice, int position, String name) {
                    default_index_age = Integer.parseInt(age.get(position))-1;
            }
        });


        switch (position){
            case 0:
                text_one.setText("Name");
                text_two.setText("Surname");
                text_spinner.setVisibility(View.GONE);
                space_spinner.setVisibility(View.GONE);
                text_age.setVisibility(View.VISIBLE);

                input_one_s.setHint("John");
                input_two_s.setHint("Kowalski");



                input_age.setVisibility(View.VISIBLE);
                spinner.setVisibility(View.GONE);
                break;

            case 1:
                text_one.setText("Favorite band");
                text_two.setText("Favorite Musician ");
                text_spinner.setText("Gender");
                space_spinner.setVisibility(View.VISIBLE);
                spinner.setAdapter(spinner_adapter_gender);

                text_spinner.setVisibility(View.VISIBLE);
                text_age.setVisibility(View.GONE);
                input_age.setVisibility(View.GONE);
                input_one_s.setHint("Roots");
                input_two_s.setHint("John Legend");
                break;


            case 2:
                text_one.setText("Email");
                text_two.setText("Phone Number");

                input_one_s.setText(mAuth.getCurrentUser().getEmail());
                input_one_s.setEnabled(false);
                input_two_s.setHint("+48 742 231 542");


                text_spinner.setText("Favorite Music Category");
                spinner.setAdapter(spinner_adapter_music);
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


    private void loadGender() {
        gender.clear();
        gender.add("Male");
        gender.add("Female");
    }

    private void loadMusic(){
        fav_music.add("Pop");
        fav_music.add("Rock");
        fav_music.add("Punk");
        fav_music.add("Jazz");
        fav_music.add("Classical");
        fav_music.add("Techno");
        fav_music.add("Electro");
        fav_music.add("Dubstep");
        fav_music.add("Country");
        fav_music.add("Indie");
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ConstraintLayout) object);
    }


   public void updateBIO(int state){
       mAuth = FirebaseAuth.getInstance();
       database = FirebaseDatabase.getInstance().getReference();

      User local_u = new User("","","",0,true, "",0,0,"","","","");
       database.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               if(snapshot.exists()){
                   local_u.name = snapshot.child("name").getValue().toString();
                   local_u.surname = snapshot.child("surname").getValue().toString();
                   local_u.mail = snapshot.child("mail").getValue().toString();
                   local_u.age=  Integer.parseInt(snapshot.child("age").getValue().toString());
                   local_u.gender = Boolean.parseBoolean(snapshot.child("gender").getValue().toString());
                   local_u.photo_id = snapshot.child("photo_id").getValue().toString();
                   local_u.playlist_amount = Integer.parseInt(snapshot.child("playlist_amount").getValue().toString());
                   local_u.fav_song_amount = Integer.parseInt(snapshot.child("fav_song_amount").getValue().toString());


                   switch (state){
                       case 1:
                            local_u.name = input_one_s.getText().toString();
                            local_u.surname = input_two_s.getText().toString();
                            local_u.age = Integer.parseInt(input_age.getCurrentSelection());

                            database.child("users").child(mAuth.getCurrentUser().getUid()).setValue(local_u);
                           break;
                       case 2:
                           local_u.fav_band =  input_one_s.getText().toString();
                           local_u.fav_musician = input_two_s.getText().toString();
                           Object s =spinner.getSelectedItem();

                           if(!s.equals(null))
                           local_u.gender = Boolean.parseBoolean(spinner.getSelectedItem().toString());

                           database.child("users").child(mAuth.getCurrentUser().getUid()).setValue(local_u);
                           break;
                       case 3:
                           local_u.number = input_two_s.getText().toString();
                           local_u.fav_category =  spinner.getSelectedItem().toString();
                           database.child("users").child(mAuth.getCurrentUser().getUid()).setValue(local_u);
                           break;
                   }

               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });


    } */
}
