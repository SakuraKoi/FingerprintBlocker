package sakura.kooi.fingerprintblocker;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private Button btnEnableAdmin;
    private Button btnSetupTimer;
    private Button btnDisableTimer;
    private Button btnDisableAdmin;
    private Button btnTest;

    private DevicePolicyManager devicePolicyManager;
    private ComponentName componentName;
    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.btnEnableAdmin = findViewById(R.id.btnEnableDeviceAdmin);
        this.btnSetupTimer = findViewById(R.id.btnSetupTimer);
        this.btnDisableTimer = findViewById(R.id.btnRemoveTimer);
        this.btnDisableAdmin = findViewById(R.id.btnDisableAdmin);
        this.btnTest = findViewById(R.id.btnTest);

        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(this.getApplicationContext(), ScreenLockDeviceAdminReceiver.class);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        this.btnEnableAdmin.setOnClickListener(e -> {
            Intent intent = new Intent("android.app.action.ADD_DEVICE_ADMIN");
            intent.putExtra("android.app.extra.DEVICE_ADMIN", componentName);
            intent.putExtra("android.app.extra.ADD_EXPLANATION", this.getString(R.string.desc_device_admin));
            this.startActivityForResult(intent, 0);
        });

        this.btnSetupTimer.setOnClickListener(e -> {
            int h = 3;
            int m = 30;
            SharedPreferences prefs = getSharedPreferences("fingerprint_blocker", MODE_PRIVATE);
            prefs.edit()
                    .putBoolean("enabled", true)
                    .putInt("hour", h)
                    .putInt("minute", m)
                    .apply();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, h);
            calendar.set(Calendar.MINUTE, m);
            calendar.set(Calendar.SECOND, 0);
            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            Intent intent = new Intent(this, LockReceiver.class);
            intent.setAction(LockReceiver.ACTION_ALARM_RECEIVER);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 1001, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
            updateState();
            Toast.makeText(this, R.string.toast_task_created, Toast.LENGTH_LONG).show();
            Log.i("FingerprintBlocker", "Alarm enabled at " + calendar.getTimeInMillis());
        });

        this.btnDisableTimer.setOnClickListener(e -> {
            SharedPreferences prefs = getSharedPreferences("fingerprint_blocker", MODE_PRIVATE);
            prefs.edit()
                    .putBoolean("enabled", false)
                    .apply();
            Intent intent = new Intent(this, LockReceiver.class);
            intent.setAction(LockReceiver.ACTION_ALARM_RECEIVER);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 1001, intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            updateState();
            Toast.makeText(this, R.string.toast_task_removed, Toast.LENGTH_LONG).show();
            Log.i("FingerprintBlocker", "Alarm disabled");
        });

        this.btnDisableAdmin.setOnClickListener(e -> {
            devicePolicyManager.removeActiveAdmin(componentName);
            updateState();
        });

        this.btnTest.setOnClickListener(e -> {
            Intent intent = new Intent(this, LockReceiver.class);
            intent.setAction(LockReceiver.ACTION_ALARM_RECEIVER);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 1001, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+TimeUnit.MINUTES.toMillis(2), pendingIntent);
            Toast.makeText(this, R.string.toast_try_unlock, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateState();
    }

    private void updateState() {
        if (!devicePolicyManager.isAdminActive(componentName)) {
            this.btnEnableAdmin.setEnabled(true);
            this.btnSetupTimer.setEnabled(false);
            this.btnDisableTimer.setEnabled(false);
            this.btnDisableAdmin.setEnabled(false);
            this.btnTest.setEnabled(false);
            return;
        }
        Log.i("FingerprintBlocker", "Device admin check passed");
        this.btnEnableAdmin.setEnabled(false);
        this.btnDisableAdmin.setEnabled(true);
        this.btnTest.setEnabled(true);

        Intent intent = new Intent(this, LockReceiver.class);
        intent.setAction(LockReceiver.ACTION_ALARM_RECEIVER);
        if (PendingIntent.getBroadcast(this.getApplicationContext(), 1001, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE) == null) {
            this.btnSetupTimer.setEnabled(true);
            this.btnDisableTimer.setEnabled(false);
            return;
        }
        Log.i("FingerprintBlocker", "Alarm check passed");
        this.btnDisableAdmin.setEnabled(false);
        this.btnSetupTimer.setEnabled(false);
        this.btnDisableTimer.setEnabled(true);
    }

    protected void onActivityResult(final int n, final int n2, final Intent intent) {
        super.onActivityResult(n, n2, intent);
        updateState();
    }
}