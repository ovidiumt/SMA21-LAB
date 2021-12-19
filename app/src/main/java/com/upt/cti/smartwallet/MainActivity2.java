package com.upt.cti.smartwallet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.upt.cti.smartwallet.model.AppState;
import com.upt.cti.smartwallet.model.Payment;
import com.upt.cti.smartwallet.ui.PaymentAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {
    private static final String TAG_MONTH = "";

    public enum Month {
        January, February, March, April, May, June, July, August,
        September, October, November, December;

        public static int monthNameToInt(Month month) {
            return month.ordinal();
        }

        public static Month intToMonthName(int index) {
            return Month.values()[index];
        }

        public static int monthFromTimestamp(String timestamp) {
            int month = Integer.parseInt(timestamp.substring(5, 7));
            return month - 1;
        }
    }
    private DatabaseReference databaseReference;
    private List<Payment> payments = new ArrayList<>();
    private TextView tStatus;
    private ListView listPayments;
    private final static String PREFERENCES_SETTINGS = "prefs_settings";
    private SharedPreferences sharedPreferences;
    private Integer currentMonth;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final int REQ_SIGNIN = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        tStatus = findViewById(R.id.tStatus);
        listPayments = findViewById(R.id.listPayments);
        PaymentAdapter adapter = new PaymentAdapter(this, R.layout.payment_item, payments);
        listPayments.setAdapter(adapter);
        sharedPreferences =  getSharedPreferences(PREFERENCES_SETTINGS, Context.MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null) {
                TextView tLoginDetail = findViewById(R.id.tLoginDetail);
                TextView tUser = findViewById(R.id.tUser);
                tLoginDetail.setText("Firebase ID: " + user.getUid());
                tUser.setText("Email: " + user.getEmail());

                AppState.get().setUserId(user.getUid());
                attachDBListener(user.getUid());
            } else {
                startActivityForResult(new Intent(getApplicationContext(),
                        SignupActivity.class), REQ_SIGNIN);

            }
        };


        currentMonth = sharedPreferences.getInt(TAG_MONTH, -1);
        if (currentMonth == -1)
            currentMonth = Month.monthFromTimestamp(AppState.getCurrentTimeDate());

        // setup firebase
        final FirebaseDatabase database = FirebaseDatabase.getInstance("https://smartwallet-5a50b-default-rtdb.firebaseio.com/");
        databaseReference = database.getReference();

        listPayments.setOnItemClickListener((adapterView, view, i, l) -> {
            AppState.get().setCurrentPayment(payments.get(i));
            startActivity(new Intent(getApplicationContext(), AddPaymentActivity.class));
        });

        System.out.println("Aici");
        databaseReference.child("wallet").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                System.out.println("onChild");
                Payment payment = dataSnapshot.getValue(Payment.class);
                if (payment != null) {
                    if(currentMonth == Month.monthFromTimestamp(dataSnapshot.getKey())) {
                        payment.timestamp = dataSnapshot.getKey();
                        if (!payments.contains(payment)) {
                            payments.add(payment);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        if (!AppState.isNetworkAvailable(this)) {
            // has local storage already
            if (AppState.get().hasLocalStorage(this)) {
                payments = AppState.get().loadFromLocalBackup(this, Month.intToMonthName(currentMonth).toString());
                tStatus.setText("Found " + payments.size() + " payments for " + Month.intToMonthName(currentMonth) + ".");
            } else {
                Toast.makeText(this, "This app needs an internet connection!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

    }

    private void attachDBListener(String uid) {
        // setup firebase database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference();
        AppState.get().setDatabaseReference(databaseReference);

        databaseReference.child("wallet").child(uid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
            //...
        });
    }

    public void clicked(View view) {
        switch (view.getId()) {
            case R.id.bAdd:
                AppState.get().setDatabaseReference(databaseReference);
                AppState.get().setCurrentPayment(null);
                startActivity(new Intent(this, AddPaymentActivity.class));
                break;
            case R.id.bNext:
                ++currentMonth;
                if(currentMonth == 12) currentMonth = 11;
                sharedPreferences.edit().putInt(TAG_MONTH, currentMonth).apply();
                System.out.println(Month.intToMonthName(currentMonth));
                recreate();
                break;
            case R.id.bPrevious:
                --currentMonth;
                if(currentMonth == -1) currentMonth = 0;
                sharedPreferences.edit().putInt(TAG_MONTH, currentMonth).apply();
                System.out.println(Month.intToMonthName(currentMonth));
                recreate();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}