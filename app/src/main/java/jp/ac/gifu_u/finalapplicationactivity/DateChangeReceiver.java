package jp.ac.gifu_u.finalapplicationactivity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import jp.ac.gifu_u.finalapplicationactivity.util.ChallengeUtil;

public class DateChangeReceiver extends BroadcastReceiver {
    private static final String PREFS_NAME = "ChallengePrefs";

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("ACTION_DAILY_UPDATE".equals(intent.getAction())) {
            // チャレンジ更新処理
            String challenge = ChallengeUtil.getTodayChallenge(context);

            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString("today_challenge", challenge).apply();

            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName widget = new ComponentName(context, MSCWidgetProvider.class);
            int[] ids = manager.getAppWidgetIds(widget);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_msc);
            int streak = prefs.getInt("streak", 0);
            views.setTextViewText(R.id.textChallenge, challenge);
            views.setTextViewText(R.id.textStreak, "連続日数：" + streak + "日");

            manager.updateAppWidget(ids, views);

            // 明日の5時に再セット
            setNextAlarm(context);
        }
    }

    public static void setNextAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // ✅ Android 12以上では、正確なアラームの権限があるかチェック
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // 許可がない場合、設定画面へ誘導するなどの対応が必要
                return;
            }
        }

        Intent intent = new Intent(context, DateChangeReceiver.class);
        intent.setAction("ACTION_DAILY_UPDATE");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // 明日の5時に設定
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 5);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // すでに5時を過ぎていたら翌日に
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DATE, 1);
        }

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    }
}