package com.example.flightticketbooking;

import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FlightDetailsActivity extends AppCompatActivity {
    private static final String LOG_TAG = FlightDetailsActivity.class.getName();
    private FirebaseUser user;
    private TextView departureTV;
    private TextView fromTV;
    private TextView toTV;
    private TextView priceTV;
    private Button cancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flight_details);
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

        departureTV = findViewById(R.id.flightDetailsDepartDate);
        fromTV = findViewById(R.id.flightDetailsFrom);
        toTV = findViewById(R.id.flightDetailsTo);
        priceTV = findViewById(R.id.flightDetailsPrice);
        cancelBtn = findViewById(R.id.flightDetailsCancelBtn);

        Bundle bundle;
        bundle = getIntent().getExtras();
        if (bundle != null){
            if (bundle.getInt("secretKey") != 55){
                finish();
            }
            String date = bundle.getString("date");
            String from = bundle.getString("from");
            String to = bundle.getString("to");
            String price = bundle.getString("price");
            String firstName = bundle.getString("firstName");
            String lastName = bundle.getString("lastName");
            String fromWhere = bundle.getString("fromWhere");

            departureTV.setText(date);
            fromTV.setText(from);
            toTV.setText(to);
            priceTV.setText(price);
            if (fromWhere.equals(BookingsActivity.class.getName())){
                TextView firstNameET = findViewById(R.id.flightDetailsFirstNameET);
                TextView lastNameET = findViewById(R.id.flightDetailsLastNameET);
                firstNameET.setText(firstName);
                lastNameET.setText(lastName);
            }
        }

        cancelBtn.setOnClickListener(view -> {
            Animation animation = AnimationUtils.loadAnimation(FlightDetailsActivity.this, R.anim.scale_button);
            view.startAnimation(animation);
            finish();
        });
    }
}