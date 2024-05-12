package com.example.flightticketbooking;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private static final String LOG_TAG = ProfileActivity.class.getName();
    BottomNavigationView bottomNavigationView;
    private FirebaseUser user;
    private User mUserInFireStore;
    private FirebaseFirestore mStore;
    private CollectionReference mUsers;
    private TextView profileEmailTV;
    private EditText firstNameET;
    private EditText lastNameET;
    private Button modifyBtn;
    private Button deleteAccountBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.profile);

        bottomNavigationView.setOnItemSelectedListener(menuItem ->  {
            if (menuItem.getItemId() == R.id.home){
                startActivity(new Intent(this, TicketListActivity.class));
                overridePendingTransition(0,0);
                return true;
            }
            if (menuItem.getItemId() == R.id.bookings){
                startActivity(new Intent(this, BookingsActivity.class));
                overridePendingTransition(0,0);
                return true;
            }
            if (menuItem.getItemId() == R.id.profile){
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
        mUsers = mStore.collection("Users");
        profileEmailTV = findViewById(R.id.profileEmailTV);
        firstNameET = findViewById(R.id.profileFirstNameEditText);
        lastNameET = findViewById(R.id.profileLastNameEditText);
        modifyBtn = findViewById(R.id.modifyProfileBtn);
        deleteAccountBtn = findViewById(R.id.deleteAccountBtn);

        initializeFields();

        modifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(ProfileActivity.this, R.anim.scale_button);
                v.startAnimation(animation);

                updateData();
            }
        });

        deleteAccountBtn.setOnClickListener(v -> {
            Animation animation = AnimationUtils.loadAnimation(ProfileActivity.this, R.anim.scale_button);
            v.startAnimation(animation);

            deleteUserWithConfirmation();
        });
    }

    public void initializeFields(){
        mUsers.whereEqualTo("uid", user.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                mUserInFireStore = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                mUserInFireStore.setDocId(queryDocumentSnapshots.getDocuments().get(0).getId());
                firstNameET.setText(mUserInFireStore.getFirstName());
                lastNameET.setText(mUserInFireStore.getLastName());
            }
        });
        profileEmailTV.setText(user.getEmail());
    }

    public void updateData(){
        mUsers.whereEqualTo("uid", user.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                DocumentSnapshot user = queryDocumentSnapshots.getDocuments().get(0);
                String docId = user.getId();
                Map<String, Object> updates = new HashMap<>();
                updates.put("firstName", firstNameET.getText().toString());
                updates.put("lastName", lastNameET.getText().toString());

                mUsers.document(docId).update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(LOG_TAG, "User updated successfully");
                        Toast.makeText(ProfileActivity.this, "User updated successfully", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(LOG_TAG, "User update error!");
                        Toast.makeText(ProfileActivity.this, "User update error!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public void deleteUserWithConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteUser();
                        startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                })
                .show();
    }

    private void deleteUser() {
        DocumentReference doc = mUsers.document(mUserInFireStore._getDocId());
        doc.delete();
        user.delete();
    }
}