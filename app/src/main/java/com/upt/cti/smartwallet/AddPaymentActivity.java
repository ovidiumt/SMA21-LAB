package com.upt.cti.smartwallet;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.upt.cti.smartwallet.model.AppState;
import com.upt.cti.smartwallet.model.Payment;

public class AddPaymentActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private TextView order,type,cost, tTimestamp;
    private ValueEventListener databaseListener;
    private EditText pOrder,pType,pCost;
    private Payment payment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_payment_activity);

        order = (TextView) findViewById(R.id.orderView);
        type = (TextView) findViewById(R.id.tOrderType);
        cost = (TextView) findViewById(R.id.tOrderCost);
        pOrder = (EditText) findViewById(R.id.orderName);
        pType = (EditText) findViewById(R.id.orderType);
        pCost = (EditText) findViewById(R.id.orderCost);
        tTimestamp = (TextView) findViewById(R.id.tTimeStamp);

        final FirebaseDatabase database = FirebaseDatabase.getInstance("https://smartwallet-5a50b-default-rtdb.firebaseio.com/");
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

    public void clicked(View view) {
        switch (view.getId()) {
            case R.id.bSave:
                if (payment != null)
                    save(payment.timestamp);
                else
                    save(AppState.getCurrentTimeDate());
                break;
            case R.id.bCancel:
                if (payment != null)
                    delete(payment.timestamp);
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
        AppState.get().getDatabaseReference().child("wallet").child(timestamp).removeValue();

        // finishes the current activity and returns to the last activity on the stack
        finish();
    }

    private void save(String timestamp){

        AppState.get().getDatabaseReference().child("wallet").child(timestamp).child("cost").setValue(Double.parseDouble(pCost.getText().toString()));
        AppState.get().getDatabaseReference().child("wallet").child(timestamp).child("name").setValue(pOrder.getText().toString());
        AppState.get().getDatabaseReference().child("wallet").child(timestamp).child("type").setValue(pType.getText().toString());

        finish();
    }
}