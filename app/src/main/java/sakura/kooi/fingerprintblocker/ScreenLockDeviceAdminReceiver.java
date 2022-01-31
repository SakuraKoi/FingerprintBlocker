package sakura.kooi.fingerprintblocker;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ScreenLockDeviceAdminReceiver extends DeviceAdminReceiver
{
    public void onDisabled(final Context context, Intent intent) {
        super.onDisabled(context, intent);
        Toast.makeText(context, R.string.toast_admin_disabled, Toast.LENGTH_LONG).show();
    }

    public void onEnabled(final Context context, final Intent intent) {
        super.onEnabled(context, intent);
        Toast.makeText(context, R.string.toast_admin_enabled, Toast.LENGTH_LONG).show();
    }
}
