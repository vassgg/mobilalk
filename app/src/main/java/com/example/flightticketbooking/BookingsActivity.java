package com.example.flightticketbooking;

import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.net.NoRouteToHostException;
import java.util.ArrayList;

public class BookingsActivity extends AppCompatActivity {
    private static final String LOG_TAG = BookingsActivity.class.getName();
    BottomNavigationView bottomNavigationView;
    private FirebaseUser user;
    private FirebaseFirestore mStore;
    private CollectionReference mFlights;
    private RecyclerView mRecyclerView;
    private ArrayList<FlightItem> mItems;
    private FlightItemAdapter mAdapter;
    private NotificationHelper mNotificationHelper;
    private TextView emptyMTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bookings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bookings);

        bottomNavigationView.setOnItemSelectedListener(menuItem ->  {
            if (menuItem.getItemId() == R.id.home){
                startActivity(new Intent(this, TicketListActivity.class));
                overridePendingTransition(0,0);
                return true;
            }
            if (menuItem.getItemId() == R.id.bookings){
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

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            Log.d(LOG_TAG, "Authenticated user!");
        } else {
            Log.d(LOG_TAG, "Unauthenticated user!");
            finish();
        }

        mStore = FirebaseFirestore.getInstance();
        mFlights = mStore.collection("Flights");

        mRecyclerView = findViewById(R.id.bookingRecyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mItems = new ArrayList<>();
        mAdapter = new FlightItemAdapter(this, mItems);
        mRecyclerView.setAdapter(mAdapter);
        getFlights();

        emptyMTV = findViewById(R.id.bookingsEmptyMessageTV);
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (mAdapter.getItemCount() == 0) {
                    emptyMTV.setVisibility(View.VISIBLE);
                } else {
                    emptyMTV.setVisibility(View.GONE);
                }
            }
        });

        mNotificationHelper = new NotificationHelper(this);
    }

    private void getFlights() {
        mFlights.orderBy("date").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                FlightItem item = doc.toObject(FlightItem.class);
                if (item.getBookedBy().equals(user.getUid())){
                    item.setId(doc.getId());
                    mItems.add(item);
                }
            }
            mAdapter.notifyDataSetChanged();
        });
    }
    public void revokeItem(FlightItem item){
        DocumentReference doc = mFlights.document(item._getId());
        doc.update("bookedBy", "").addOnSuccessListener(unused -> {
            Log.d(LOG_TAG, "Canceled successfully");
            Toast.makeText(this, "Canceled successfully", Toast.LENGTH_LONG).show();
            for (FlightItem listItem : mItems) {
                if (listItem._getId().equals(item._getId())) {
                    mItems.remove(listItem);
                    break;
                }
            }
            mAdapter.notifyDataSetChanged();
        });
        mNotificationHelper.cancel();
    }
}