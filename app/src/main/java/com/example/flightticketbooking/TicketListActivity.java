package com.example.flightticketbooking;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class TicketListActivity extends AppCompatActivity {
    private static final String LOG_TAG = TicketListActivity.class.getName();
    private FirebaseUser user;
    private FirebaseFirestore mStore;
    private CollectionReference mFlights;
    BottomNavigationView bottomNavigationView;
    private TextView fromSelector;
    private TextView toSelector;
    private ArrayList<String> placesOfDeparture;
    private ArrayList<String> destinations;
    private Dialog dialog;
    private RadioButton oneWayBtn;
    private RadioButton retourBtn;
    private TextView departDate;
    private TextView returnDate;
    private TextView emptyTV;
    private DatePickerDialog datePickerDialog;
    private RecyclerView mRecyclerView;
    private ArrayList<FlightItem> mItems;
    private FlightItemAdapter mAdapter;
    private Button searchBtn;
    private final long itemLimit = 10;
    private NotificationHelper mNotificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ticket_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            Log.d(LOG_TAG, "Authenticated user!");
        } else {
            Log.d(LOG_TAG, "Unauthenticated user!");
            finish();
        }

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.home);

        bottomNavigationView.setOnItemSelectedListener(menuItem ->  {
            if (menuItem.getItemId() == R.id.home){
                return true;
            }
            if (menuItem.getItemId() == R.id.bookings){
                startActivity(new Intent(this, BookingsActivity.class));
                overridePendingTransition(0,0);
                return true;
            }
            if (menuItem.getItemId() == R.id.profile){
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0,0);
                return true;
            }
            if (menuItem.getItemId() == R.id.logout){
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, MainActivity.class));
                return true;
            }
            return false;
        });

        fromSelector = findViewById(R.id.from_selector);
        toSelector = findViewById(R.id.to_selector);

        mStore = FirebaseFirestore.getInstance();
        mFlights = mStore.collection("Flights");

        placesOfDeparture = new ArrayList<>();
        destinations = new ArrayList<>();

        getPlaces(placesOfDeparture, destinations);

        fromSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeChooseLocationDialog(placesOfDeparture, fromSelector);
            }
        });

        toSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeChooseLocationDialog(destinations, toSelector);
            }
        });

        departDate = findViewById(R.id.depart_date);
        returnDate = findViewById(R.id.return_date);

        oneWayBtn = findViewById(R.id.oneWay);
        retourBtn = findViewById(R.id.retour);

        returnDate.setEnabled(false);
        oneWayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnDate.setEnabled(false);
            }
        });
        retourBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnDate.setEnabled(true);
            }
        });

        departDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    makeDatePickerDialog(departDate);
                }
            }
        );
        returnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeDatePickerDialog(returnDate);
            }
        });

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mItems = new ArrayList<>();
        mAdapter = new FlightItemAdapter(this, mItems);
        mRecyclerView.setAdapter(mAdapter);
        getFlights();

        searchBtn = findViewById(R.id.searchBtn);
        searchBtn.setEnabled(false);

        fromSelector.addTextChangedListener(searchBtnTextWatcher);
        toSelector.addTextChangedListener(searchBtnTextWatcher);
        departDate.addTextChangedListener(searchBtnTextWatcher);
        returnDate.addTextChangedListener(searchBtnTextWatcher);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(TicketListActivity.this, R.anim.scale_button);
                v.startAnimation(animation);

                if (!fromSelector.getText().toString().isEmpty() && !toSelector.getText().toString().isEmpty()){
                    mAdapter.filter(
                            fromSelector.getText().toString(),
                            toSelector.getText().toString(),
                            departDate.getText().toString(),
                            returnDate.getText().toString(),
                            retourBtn.isChecked());
                }
            }
        });

        mNotificationHelper = new NotificationHelper(this);

        emptyTV = findViewById(R.id.ticketListEmptyTV);
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (mAdapter.getItemCount() == 0) {
                    emptyTV.setVisibility(View.VISIBLE);
                } else {
                    emptyTV.setVisibility(View.GONE);
                }
            }
        });
    }
    TextWatcher searchBtnTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String from = fromSelector.getText().toString();
            String to = toSelector.getText().toString();
            String depDate = departDate.getText().toString();
            String reDate = returnDate.getText().toString();

            if (retourBtn.isChecked()){
                searchBtn.setEnabled(!from.isEmpty() && !to.isEmpty() && !depDate.isEmpty() && !reDate.isEmpty());
            } else if (oneWayBtn.isChecked()) {
                searchBtn.setEnabled(!from.isEmpty() && !to.isEmpty() && !depDate.isEmpty());
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    public void getFlights(){
        mFlights.orderBy("date").limit(itemLimit).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                FlightItem item = doc.toObject(FlightItem.class);
                if (item.getBookedBy().isEmpty()){
                    item.setId(doc.getId());
                    mItems.add(item);
                }
            }
            mAdapter.notifyDataSetChanged();
        });
    }
    public void reserveItem(FlightItem item){
        DocumentReference doc = mFlights.document(item._getId());
        doc.update("bookedBy", user.getUid()).addOnSuccessListener(unused -> {
            Log.d(LOG_TAG, "Booked successfully");
            Toast.makeText(this, "Booked successfully", Toast.LENGTH_LONG).show();
            for (FlightItem listItem : mItems) {
                if (listItem._getId().equals(item._getId())) {
                    mItems.remove(listItem);
                    break;
                }
            }
            mAdapter.notifyDataSetChanged();
        });
        mNotificationHelper.send("Booked successfully");
    }

    public void makeDatePickerDialog(TextView textView){
        final Calendar c = Calendar.getInstance();

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(TicketListActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String myYear = String.valueOf(year);
                String myMonth = String.format(Locale.getDefault(), "%02d", monthOfYear + 1);
                String myDay = String.format(Locale.getDefault(), "%02d", dayOfMonth);
                textView.setText(myYear + "-" + myMonth  + "-" + myDay);
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    public void makeChooseLocationDialog(ArrayList<String> places, TextView selector){
        dialog = new Dialog(TicketListActivity.this);
        dialog.setContentView(R.layout.dialog_searchable_spinner);

        dialog.getWindow().setLayout(650, 800);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        EditText editTextFrom = dialog.findViewById(R.id.editTextSpinner);
        ListView listViewFrom = dialog.findViewById(R.id.listViewSpinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(TicketListActivity.this, android.R.layout.simple_list_item_1, places);

        listViewFrom.setAdapter(adapter);

        editTextFrom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        listViewFrom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selector.setText(adapter.getItem(position));

                dialog.dismiss();
            }
        });
    }

    public void getPlaces(ArrayList<String> departures, ArrayList<String> destinations){
        Set<String> resultDep = new HashSet<>();
        Set<String> resultDest = new HashSet<>();
        mFlights.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots){
                    FlightItem item = doc.toObject(FlightItem.class);
                    resultDep.add(item.getFrom());
                    resultDest.add(item.getTo());
                }
                departures.addAll(resultDep);
                destinations.addAll(resultDest);
            }
        });
    }
}