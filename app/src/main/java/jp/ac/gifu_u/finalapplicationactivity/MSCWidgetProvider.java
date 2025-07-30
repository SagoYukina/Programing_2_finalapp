package jp.ac.gifu_u.finalapplicationactivity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MSCWidgetProvider extends AppWidgetProvider {

    private static final String PREFS_NAME = "ChallengePrefs";
    private static final String KEY_DATE = "done_date";
    private static final String KEY_DONE = "done";
    private static final String KEY_LAST_DATE = "last_date";
    private static final String KEY_STREAK = "streak";
    private static final String KEY_CHALLENGE = "today_challenge";
    private static final String ACTION_MARK_COMPLETE = "jp.ac.gifu_u.finalapplicationactivity.ACTION_MARK_COMPLETE";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String challenge = prefs.getString(KEY_CHALLENGE, "チャレンジが見つかりません");
        int streak = prefs.getInt(KEY_STREAK, 0);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_msc);
        views.setTextViewText(R.id.textChallenge, challenge);
        views.setTextViewText(R.id.textStreak, "連続日数：" + streak + "日");

        // ✅ ボタン押下時のBroadcast登録
        Intent intent = new Intent(context, MSCWidgetProvider.class);
        intent.setAction(ACTION_MARK_COMPLETE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.buttonComplete, pendingIntent);

        for (int id : appWidgetIds) {
            appWidgetManager.updateAppWidget(id, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (ACTION_MARK_COMPLETE.equals(intent.getAction())) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

            boolean isDone = prefs.getBoolean(KEY_DONE, false);
            String savedDate = prefs.getString(KEY_DATE, "");
            int streak = prefs.getInt(KEY_STREAK, 0);

            if (isDone && today.equals(savedDate)) {
                // ✅ キャンセル
                prefs.edit()
                        .putBoolean(KEY_DONE, false)
                        .putInt(KEY_STREAK, Math.max(0, streak - 1))
                        .putString(KEY_LAST_DATE, "")
                        .apply();
                Toast.makeText(context, "キャンセルしました", Toast.LENGTH_SHORT).show();
            } else {
                // ✅ チャレンジ達成
                String lastDate = prefs.getString(KEY_LAST_DATE, "");

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                    Date prev = lastDate.isEmpty() ? null : sdf.parse(lastDate);
                    Date curr = sdf.parse(today);

                    if (prev != null) {
                        long diffDays = (curr.getTime() - prev.getTime()) / (1000L * 60 * 60 * 24);
                        if (diffDays == 1) {
                            streak++;
                        } else {
                            streak = 1;
                        }
                    } else {
                        streak = 1;
                    }
                } catch (Exception e) {
                    streak = 1;
                }

                prefs.edit()
                        .putBoolean(KEY_DONE, true)
                        .putString(KEY_DATE, today)
                        .putString(KEY_LAST_DATE, today)
                        .putInt(KEY_STREAK, streak)
                        .apply();
                Toast.makeText(context, "チャレンジ達成！", Toast.LENGTH_SHORT).show();
            }

            // ✅ 再描画
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName widget = new ComponentName(context, MSCWidgetProvider.class);
            int[] ids = manager.getAppWidgetIds(widget);
            onUpdate(context, manager, ids);
        }
    }
}