package sakura.kooi.fingerprintblocker;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;

public class StartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()) || "android.intent.action.QUICKBOOT_POWERON".equals(intent.getAction())) {
            SharedPreferences prefs = context.getSharedPreferences("fingerprint_blocker", MODE_PRIVATE);

            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName componentName = new ComponentName(context.getApplicationContext(), ScreenLockDeviceAdminReceiver.class);
            Log.i("FingerprintBlocker", "Power on");
            Log.i("FingerprintBlocker", "Prefs = " + prefs.getBoolean("enabled", false));
            Log.i("FingerprintBlocker", "Admin = " + devicePolicyManager.isAdminActive(componentName));
            if (prefs.getBoolean("enabled", false) && devicePolicyManager.isAdminActive(componentName)) {
                int h = prefs.getInt("hour", 3);
                int m = prefs.getInt("minute", 30);
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, h);
                calendar.set(Calendar.MINUTE, m);
                calendar.set(Calendar.SECOND, 0);
                if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                }

                Intent timerIntent = new Intent(context, LockReceiver.class);
                timerIntent.setAction(LockReceiver.ACTION_ALARM_RECEIVER);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 1001, timerIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                Log.i("FingerprintBlocker", "Calendar scheduled at " + h + ":" + m + " " + calendar.getTimeInMillis());
            }
        }

    }
}