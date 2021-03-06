package com.example.scheduleapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements
        View.OnClickListener{

    private static final int RC_SIGN_IN = 99;

    // Store all associated employee ids for user locally for synchronous lookup
    public static Set<String> ids = null;
    private MainActivityViewModel mViewModel;
    TextView titlepage, subtitlepage, endpage;
    Button btnAddNew;
    public String uid;
    //DatabaseReference reference;
    CollectionReference reference;
    RecyclerView ourdoes;
    RecyclerView ourdoes2;
    ArrayList<HomeCollection> list1, list2;
    ShiftAdapter doesAdapter, doesAdapter2;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        titlepage = findViewById(R.id.titlepage);
        subtitlepage = findViewById(R.id.subtitlepage);
        endpage = findViewById(R.id.endpage);

        btnAddNew = findViewById(R.id.btnAddNew);

        // import font
        //Typeface MLight = Typeface.createFromAsset(getAssets(), "fonts/ML.ttf");
        //Typeface MMedium = Typeface.createFromAsset(getAssets(), "fonts/MM.ttf");

        // customize font
       // titlepage.setTypeface(MMedium);
        //subtitlepage.setTypeface(MLight);
        //endpage.setTypeface(MLight);

       // btnAddNew.setTypeface(MLight);

        btnAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent a = new Intent(MainActivity.this,NewTaskAct.class);
                startActivity(a);
            }
        });


        // working with data
        ourdoes = findViewById(R.id.ourdoes);
        ourdoes2 = findViewById(R.id.ourdoes2);

        ourdoes.setLayoutManager(new LinearLayoutManager(this));
        ourdoes2.setLayoutManager(new LinearLayoutManager(this));
        list1 = new ArrayList<HomeCollection>();
        list2 = new ArrayList<HomeCollection>();

        // get data from firebase
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        String sortedDate = dateFormat.format(new Date());
        String date = dateFormat.format(calendar.getTime());
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            //reference = FirebaseDatabase.getInstance().getReference().child("shifts").child(uid);
            reference = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                    .collection("shifts");
            //reference.orderByChild("date").equalTo(date).addValueEventListener(new ValueEventListener() {
            reference.whereEqualTo("date", date).get().addOnCompleteListener( // .orderBy("startTime").startAt(sortedDate)
                    new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                for(QueryDocumentSnapshot document : task.getResult()){
                                    HomeCollection p = document.toObject(HomeCollection.class);
                                    list1.add(p);
                                }
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
                                Collections.sort(list1 , new Comparator<HomeCollection>() {
                                    @Override
                                    public int compare(HomeCollection o1 , HomeCollection o2) {
                                        try {
                                            return new SimpleDateFormat("hh:mm a").parse(o1.getStartTime()).compareTo(
                                                    new SimpleDateFormat("hh:mm a").parse(o2.getStartTime()));
                                        } catch (ParseException e) {
                                            return 0;
                                        }
                                    }
                                });
                                doesAdapter = new ShiftAdapter(MainActivity.this , list1);
                                ourdoes.setAdapter(doesAdapter);
                                doesAdapter.notifyDataSetChanged();
                            }
                        }
                    });
/*
                //reference.orderByChild("startTime").startAt(sortedDate).addValueEventListener(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // set code to retrieve data and replace layout
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        HomeCollection p = dataSnapshot1.getValue(HomeCollection.class);
                        list1.add(p);
                    }
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
                    Collections.sort(list1 , new Comparator<HomeCollection>() {
                        @Override
                        public int compare(HomeCollection o1 , HomeCollection o2) {
                            try {
                                return new SimpleDateFormat("hh:mm a").parse(o1.getStartTime()).compareTo(
                                        new SimpleDateFormat("hh:mm a").parse(o2.getStartTime()));
                            } catch (ParseException e) {
                                return 0;
                            }
                        }
                    });
                    doesAdapter = new ShiftAdapter(MainActivity.this , list1);
                    ourdoes.setAdapter(doesAdapter);
                    doesAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }

            });*/
            Calendar calendar2 = Calendar.getInstance();
            Date c = calendar2.getTime();
            String currentDate = dateFormat.format(c);
            calendar2.add(Calendar.DAY_OF_YEAR , 1);
            c = calendar2.getTime();

            String date2 = dateFormat.format(c);
            reference = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                    .collection("shifts");
            //reference.orderByChild("date").equalTo(date).addValueEventListener(new ValueEventListener() {
            reference.whereEqualTo("date", date2).get().addOnCompleteListener( //.orderBy("startTime").startAt(sortedDate)
                    new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                for(QueryDocumentSnapshot document : task.getResult()){
                                    HomeCollection p = document.toObject(HomeCollection.class);
                                    list2.add(p);
                                }
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
                                Collections.sort(list2 , new Comparator<HomeCollection>() {
                                    @Override
                                    public int compare(HomeCollection o1 , HomeCollection o2) {
                                        try {
                                            return new SimpleDateFormat("hh:mm a").parse(o1.getStartTime()).compareTo(
                                                    new SimpleDateFormat("hh:mm a").parse(o2.getStartTime()));
                                        } catch (ParseException e) {
                                            return 0;
                                        }
                                    }
                                });
                                doesAdapter2 = new ShiftAdapter(MainActivity.this , list2);
                                ourdoes2.setAdapter(doesAdapter2);
                                doesAdapter2.notifyDataSetChanged();
                            }
                        }
                    });
            //reference = FirebaseDatabase.getInstance().getReference().child("shifts").child(uid);
           // reference.orderByChild("date").equalTo(date2).addValueEventListener(new ValueEventListener() {
                //reference.orderByChild("startTime").startAt(sortedDate).addValueEventListener(new ValueEventListener() {
                /*
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // set code to retrieve data and replace layout
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        HomeCollection p = dataSnapshot1.getValue(HomeCollection.class);
                        list2.add(p);
                    }
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
                    Collections.sort(list2 , new Comparator<HomeCollection>() {
                        @Override
                        public int compare(HomeCollection o1 , HomeCollection o2) {
                            try {
                                return new SimpleDateFormat("hh:mm a").parse(o1.getStartTime()).compareTo(
                                        new SimpleDateFormat("hh:mm a").parse(o2.getStartTime()));
                            } catch (ParseException e) {
                                return 0;
                            }
                        }
                    });
                    doesAdapter2 = new ShiftAdapter(MainActivity.this , list2);
                    ourdoes2.setAdapter(doesAdapter2);
                    doesAdapter2.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }

            });*/
        }
        else{
            startSignIn();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ids = new HashSet<String>();
        // Start sign in if necessary
        if (shouldStartSignIn()) {
            startSignIn();
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

            firebaseFirestore.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                    .collection("employees").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            // Populate local ArrayList with all ids in database
                            for (DocumentSnapshot querySnapshot : task.getResult())
                                    ids.add(querySnapshot.getString("id"));
                        }
                    });
            return;
        }
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .collection("employees").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        // Populate local ArrayList with all ids in database
                        for (DocumentSnapshot querySnapshot : task.getResult())
                            ids.add(querySnapshot.getString("id"));
                    }
                });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    private void startSignIn() {
        // Sign in with FirebaseUI
        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(Arrays.asList(

                        new AuthUI.IdpConfig.EmailBuilder().build()
                ))
                .setIsSmartLockEnabled(false)
                .build();

        startActivityForResult(intent, RC_SIGN_IN);
        mViewModel.setIsSigningIn(true);
    }

    @Override
    protected void onActivityResult(int requestCode , int resultCode , Intent data) {
        super.onActivityResult(requestCode , resultCode , data);
        if (requestCode == RC_SIGN_IN) {
            mViewModel.setIsSigningIn(false);

            if (resultCode != RESULT_OK && shouldStartSignIn()) {
                startSignIn();
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_employee:
                Intent intent = new Intent(MainActivity.this, AddNewEmployee.class);
                startActivity(intent);
                break;
            case R.id.menu_view_calendar:
                Intent intent2 = new Intent(MainActivity.this, CalendarTest.class);
                startActivity(intent2);
                break;
            case R.id.menu_view_employee:
                Intent intent3 = new Intent(MainActivity.this, ViewEmployees.class);
                startActivity(intent3);
                break;
            case R.id.menu_sign_out:
                AuthUI.getInstance().signOut(this);
                startSignIn();
                break;

        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onClick(View v) {

    }
    private boolean shouldStartSignIn() {
        return (!mViewModel.getIsSigningIn() && FirebaseAuth.getInstance().getCurrentUser() == null);
    }
}