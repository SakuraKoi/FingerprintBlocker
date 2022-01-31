package sakura.kooi.fingerprintblocker;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LockReceiver extends BroadcastReceiver {
    public static final String ACTION_ALARM_RECEIVER = "fingerprint_blocker";

    @Override
    public void onReceive(Context context, Intent intent) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        devicePolicyManager.lockNow();
        Log.i("FingerprintBlocker", "locked screen via device admin");
    }
}
