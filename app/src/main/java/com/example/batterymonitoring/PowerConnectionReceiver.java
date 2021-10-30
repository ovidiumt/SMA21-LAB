package com.example.batterymonitoring;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class PowerConnectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,-1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL ;

        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED , -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB ;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC ;
        String chargingStatus = "";
        if(usbCharge)
            chargingStatus = "USB charging:" + isCharging;
        else if(acCharge)
            chargingStatus = "AC charging:" + isCharging;
        Intent newIntent =  new Intent(context , MainActivity.class);
        MainActivity.textView.setText(chargingStatus);
        newIntent.putExtra("status",chargingStatus);
        PendingIntent pendingIntent = PendingIntent.getActivity(context , 0 ,newIntent , 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context , MainActivity.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Charging status changed!")
                .setContentText(chargingStatus)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Name";
            String description = "A description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(MainActivity.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }


        NotificationManagerCompat notoficationManager = NotificationManagerCompat.from(context);

        notoficationManager.notify(MainActivity.notificationId,mBuilder.build());
    }
}
