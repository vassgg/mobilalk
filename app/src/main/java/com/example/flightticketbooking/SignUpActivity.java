package com.example.flightticketbooking;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {
    private static final String LOG_TAG = SignUpActivity.class.getName();
    private static final int SECRET_KEY = 99;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private CollectionReference mUsers;
    EditText firstnameET;
    EditText lastnameET;
    EditText emailET;
    EditText passwordET;
    EditText passwordAgainET;
    Button signUpBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);

        if (secret_key != 99) {
            finish();
        }

        firstnameET = findViewById(R.id.firstnameEditText);
        lastnameET = findViewById(R.id.lastnameEditText);
        emailET = findViewById(R.id.emailEditText);
        passwordET = findViewById(R.id.passwordEditText);
        passwordAgainET = findViewById(R.id.passwordAgainEditText);
        signUpBtn = findViewById(R.id.signUpButton);

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mUsers = mStore.collection("Users");

        signUpBtn.setEnabled(false);

        firstnameET.addTextChangedListener(signUpTextWatcher);
        lastnameET.addTextChangedListener(signUpTextWatcher);
        emailET.addTextChangedListener(signUpTextWatcher);
        passwordET.addTextChangedListener(signUpTextWatcher);
        passwordAgainET.addTextChangedListener(signUpTextWatcher);
    }
    TextWatcher signUpTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String firstname = firstnameET.getText().toString();
            String lastname = lastnameET.getText().toString();
            String email = emailET.getText().toString();
            String password = passwordET.getText().toString();
            String passwordAgain = passwordAgainET.getText().toString();

            signUpBtn.setEnabled(!TextUtils.isEmpty(firstname) && !TextUtils.isEmpty(lastname) && !TextUtils.isEmpty(email)
                    && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(passwordAgain));
        }
        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    public void signUp(View view){
        Animation animation = AnimationUtils.loadAnimation(SignUpActivity.this, R.anim.scale_button);
        view.startAnimation(animation);

        String firstname = firstnameET.getText().toString();
        String lastname = lastnameET.getText().toString();
        String email = emailET.getText().toString();
        String pw = passwordET.getText().toString();
        String pwAgain = passwordAgainET.getText().toString();

        if (!pw.equals(pwAgain)){
            Log.e(LOG_TAG, "Passwords doesn't match!");
            return;
        }

        Log.i(LOG_TAG, "Signed up: " + firstname + " " + lastname + ", email: " + email);

        mAuth.createUserWithEmailAndPassword(email, pw).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Log.d(LOG_TAG, "Signed Up!");
                    mUsers.add(new User(mAuth.getCurrentUser().getUid(), firstname, lastname));
                    startBooking();
                } else {
                    Toast.makeText(SignUpActivity.this, "User wasn't created: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void cancel(View view) {
        Animation animation = AnimationUtils.loadAnimation(SignUpActivity.this, R.anim.scale_button);
        view.startAnimation(animation);
        finish();
    }

    public void startBooking(/* vmi */){
        Intent intent = new Intent(this, TicketListActivity.class);
        intent.putExtra("SECRECT_KEY", SECRET_KEY);
        startActivity(intent);
    }
}