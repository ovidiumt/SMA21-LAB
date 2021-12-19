package com.upt.cti.smartwallet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.upt.cti.smartwallet.model.AppState;
import com.upt.cti.smartwallet.model.Payment;

import java.io.IOException;

public class AddPaymentActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private TextView order,type,cost, tTimestamp;
    private ValueEventListener databaseListener;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final int REQ_SIGNIN = 3;
    private EditText pOrder,pType,pCost;
    private Payment payment;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_payment_activity);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null) {
                TextView tLoginDetail = (TextView) findViewById(R.id.tLoginDetail);
                TextView tUser = (TextView) findViewById(R.id.tUser);
                tLoginDetail.setText("Firebase ID: " + user.getUid());
                tUser.setText("Email: " + user.getEmail());

                AppState.get().setUserId(user.getUid());
                attachDBListener(user.getUid());
            } else {
                startActivityForResult(new Intent(getApplicationContext(),
                        SignupActivity.class), REQ_SIGNIN);
            }
        };;

        order = (TextView) findViewById(R.id.orderView);
        type = (TextView) findViewById(R.id.tOrderType);
        cost = (TextView) findViewById(R.id.tOrderCost);
        pOrder = (EditText) findViewById(R.id.orderName);
        pType = (EditText) findViewById(R.id.orderType);
        pCost = (EditText) findViewById(R.id.orderCost);
        tTimestamp = (TextView) findViewById(R.id.tTimeStamp);

        final FirebaseDatabase database = FirebaseDatabase.getInstance("https://smart-wallet-27310-default-rtdb.europe-west1.firebasedatabase.app/");
        databaseReference = database.getReference();

        payment = AppState.get().getCurrentPayment();
        if (payment != null) {
            pOrder.setText(payment.getName());
            pCost.setText(String.valueOf(payment.getCost()));
            tTimestamp.setText("Time of payment: " + payment.timestamp);
            pType.setText(payment.getType());
        } else {
            tTimestamp.setText("");
        }
    }

    public void clicked(View view) throws IOException {
        switch (view.getId()) {
            case R.id.bSave:
                if (payment != null)
                    save(payment.timestamp);
                else
                    save(AppState.getCurrentTimeDate());
                break;
            case R.id.bCancel:
                if (payment != null) {
                    DatabaseReference monthReference = FirebaseDatabase.getInstance("https://smart-wallet-27310-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("wallet").child(payment.timestamp);
                    monthReference.keepSynced(true);
                    AppState.get().updateLocalBackup(this, payment, false);
                    delete(payment.timestamp);
                }
                else
                    Toast.makeText(this, "Payment does not exist", Toast.LENGTH_SHORT).show();break;
        }
    }

    private void createNewPaymentListener(Payment payment) {

        if (databaseReference != null && databaseListener != null)
            databaseReference.child("wallet").removeEventListener(databaseListener);

        databaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                dataSnapshot.child(dataSnapshot.getKey()).getRef().setValue(payment.getTimestamp());
                dataSnapshot.child("cost").getRef().setValue(payment.getCost());
                dataSnapshot.child("name").getRef().setValue(payment.getName());
                dataSnapshot.child("type").getRef().setValue(payment.getType());
                // explicit mapping of month name from entry key

            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        };

        // set new databaseListener
        databaseReference.child("wallet").addValueEventListener(databaseListener);
    }

    private void delete(String timestamp) {
        if (AppState.isNetworkAvailable(this))
            AppState.get().getDatabaseReference().child("wallet").child(timestamp).removeValue();
        else  Toast.makeText(this, "Internet not available!", Toast.LENGTH_SHORT).show();
        // finishes the current activity and returns to the last activity on the stack
        finish();
    }

    private void save(String timestamp) throws IOException {
        DatabaseReference monthReference = FirebaseDatabase.getInstance("https://smart-wallet-27310-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("wallet").child(timestamp);
        monthReference.keepSynced(true);
        Payment payment = new Payment(timestamp, Double.parseDouble(pCost.getText().toString()), pOrder.getText().toString(), pType.getText().toString(), AppState.get().getUserID());
        AppState.get().updateLocalBackup(this, payment, true);

        if (AppState.isNetworkAvailable(this)) {
            AppState.get().getDatabaseReference().child("wallet").child(timestamp).child("cost").setValue(Double.parseDouble(pCost.getText().toString()));
            AppState.get().getDatabaseReference().child("wallet").child(timestamp).child("name").setValue(pOrder.getText().toString());
            AppState.get().getDatabaseReference().child("wallet").child(timestamp).child("type").setValue(pType.getText().toString());
        }

        finish();
    }

    private void attachDBListener(String uid) {
        // setup firebase database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference();
        AppState.get().setDatabaseReference(databaseReference);

        databaseReference.child("wallet").child(uid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String timestamp = AppState.get().getTimeStamp();
                String user = AppState.get().getUserID();
                AppState.get().getDatabaseReference().child("wallet").child(user).child(timestamp).child("cost").setValue(Double.parseDouble(pCost.getText().toString()));
                AppState.get().getDatabaseReference().child("wallet").child(user).child(timestamp).child("name").setValue(pOrder.getText().toString());
                AppState.get().getDatabaseReference().child("wallet").child(user).child(timestamp).child("type").setValue(pType.getText().toString());
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

        });
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