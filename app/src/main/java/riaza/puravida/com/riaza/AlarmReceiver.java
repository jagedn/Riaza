package riaza.puravida.com.riaza;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {
    public AlarmReceiver() {
    }

    static PendingIntent sender;

    @Override
    public void onReceive(Context context, Intent i) {
        Intent intent = new Intent(context,TakePictureActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putBoolean("take",true);
        intent.putExtras(bundle);

        context.startActivity(intent);
    }

    static final int WHAT = 192837;

    public static void scheduleStart(Activity activity){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 1);

        Intent intent = new Intent(activity, AlarmReceiver.class);
        sender = PendingIntent.getBroadcast(activity, WHAT, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender);
    }

    public static void scheduleNext(Activity activity){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        int pHour = Integer.parseInt(prefs.getString("sync_frequency","-1"));
        if( pHour != -1) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.MINUTE, pHour);

            Intent intent = new Intent(activity, AlarmReceiver.class);
            sender = PendingIntent.getBroadcast(activity, WHAT, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender);
        }

    }

    public static void cancelSchedule(Activity activity){
        if( sender != null ) {
            AlarmManager am = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
            am.cancel(sender);
        }
    }
}
