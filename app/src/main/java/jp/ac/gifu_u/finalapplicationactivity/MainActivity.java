package jp.ac.gifu_u.finalapplicationactivity;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.widget.RemoteViews;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.ac.gifu_u.finalapplicationactivity.util.ChallengeUtil;

public class MainActivity extends AppCompatActivity {

    private static final int BADGE_1DAYS = 1;
    private static final int BADGE_2DAYS = 2;
    private static final int BADGE_3DAYS = 3;
    private static final int BADGE_4DAYS = 4;
    private static final int BADGE_5DAYS = 5;

    private Button completeButton;
    private ImageView hanamaruImage;
    private TextView streakText;
    private LinearLayout badgesContainer;

    private SharedPreferences prefs;
    private static final String PREFS_NAME = "ChallengePrefs";
    private static final String KEY_DATE = "done_date";
    private static final String KEY_DONE = "done";
    private static final String KEY_LAST_DATE = "last_date";
    private static final String KEY_STREAK = "streak";
    private static final String KEY_CHALLENGE = "today_challenge";

    private String today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        TextView challengeText = findViewById(R.id.challengeText);
        completeButton     = findViewById(R.id.completeButton);
        hanamaruImage      = findViewById(R.id.hanamaruImage);
        streakText         = findViewById(R.id.streakText);
        badgesContainer    = findViewById(R.id.badgesContainer);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // ✅ ChallengeUtilからその日のチャレンジを取得（初回アクセスではなく日付更新ベース）
        String challenge = ChallengeUtil.getTodayChallenge(this);
        challengeText.setText(challenge);

        prefs.edit().putString(KEY_CHALLENGE, challenge).apply();

        completeButton.setOnClickListener(v -> {
            boolean isDone = prefs.getBoolean(KEY_DONE, false);

            if (isDone) {
                int currentStreak = prefs.getInt(KEY_STREAK, 0);
                prefs.edit()
                        .putBoolean(KEY_DONE, false)
                        .putInt(KEY_STREAK, Math.max(0, currentStreak - 1))
                        .putString(KEY_LAST_DATE, "")
                        .apply();
                resetButtonState();
                Toast.makeText(this, "キャンセルしました", Toast.LENGTH_SHORT).show();
            } else {
                prefs.edit()
                        .putBoolean(KEY_DONE, true)
                        .putString(KEY_DATE, today)
                        .apply();
                setButtonToDoneState();
                Toast.makeText(this, "チャレンジ達成！", Toast.LENGTH_SHORT).show();
                updateStreakOnPress();
            }

            updateStreakDisplay();
            updateBadges();

            // ✅ ウィジェットもリアルタイム更新
            AppWidgetManager manager = AppWidgetManager.getInstance(getApplicationContext());
            ComponentName widget = new ComponentName(getApplicationContext(), MSCWidgetProvider.class);
            int[] ids = manager.getAppWidgetIds(widget);
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_msc);
            views.setTextViewText(R.id.textChallenge, ChallengeUtil.getTodayChallenge(getApplicationContext()));
            views.setTextViewText(R.id.textStreak, "連続日数：" + prefs.getInt(KEY_STREAK, 0) + "日");
            manager.updateAppWidget(ids, views);
        });

        updateBadges();
    }

    @Override
    protected void onResume() {
        super.onResume();
        today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        loadButtonState();
        updateStreakDisplay();
        updateBadges();
    }

    private void loadButtonState() {
        String savedDate = prefs.getString(KEY_DATE, "");
        boolean isDone  = prefs.getBoolean(KEY_DONE, false);

        if (!today.equals(savedDate)) {
            prefs.edit().putBoolean(KEY_DONE, false).apply();
            resetButtonState();
        } else {
            if (isDone) setButtonToDoneState();
            else        resetButtonState();
        }

        String lastDate = prefs.getString(KEY_LAST_DATE, "");
        if (!lastDate.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                Date prev = sdf.parse(lastDate);
                Date curr = sdf.parse(today);
                long diffDays = (curr.getTime() - prev.getTime()) / (1000L * 60 * 60 * 24);
                if (diffDays > 1) {
                    prefs.edit()
                            .putInt(KEY_STREAK, 0)
                            .putString(KEY_LAST_DATE, "")
                            .apply();
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Date parse error", e);
            }
        }
    }

    private void updateStreakOnPress() {
        String lastDate = prefs.getString(KEY_LAST_DATE, "");
        int streak      = prefs.getInt(KEY_STREAK, 0);

        if (lastDate.isEmpty()) {
            streak = 1;
        } else if (lastDate.equals(today)) {
            return;
        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                Date prev = sdf.parse(lastDate);
                Date curr = sdf.parse(today);
                long diffDays = (curr.getTime() - prev.getTime()) / (1000L * 60 * 60 * 24);
                if (diffDays == 1) streak++;
                else streak = 1;
            } catch (Exception e) {
                Log.e("MainActivity", "Date parse error", e);
                streak = 1;
            }
        }

        prefs.edit()
                .putInt(KEY_STREAK, streak)
                .putString(KEY_LAST_DATE, today)
                .apply();
    }

    private void updateStreakDisplay() {
        int streak = prefs.getInt(KEY_STREAK, 0);
        streakText.setText(getString(R.string.streak_text, streak));
    }

    private void updateBadges() {
        int streak = prefs.getInt(KEY_STREAK, 0);
        badgesContainer.removeAllViews();

        int[] thresholds = {
                BADGE_1DAYS,
                BADGE_2DAYS,
                BADGE_3DAYS,
                BADGE_4DAYS,
                BADGE_5DAYS
        };

        LayoutInflater inflater = getLayoutInflater();

        for (int t : thresholds) {
            if (streak >= t) {
                View badge = inflater.inflate(R.layout.view_badge, badgesContainer, false);
                TextView title = badge.findViewById(R.id.badgeTitle);
                title.setText(t + "日");
                badgesContainer.addView(badge);
            }
        }
    }

    private void setButtonToDoneState() {
        completeButton.setBackgroundTintList(
                ColorStateList.valueOf(Color.parseColor("#C98A4A"))
        );
        completeButton.setText("やった！");
        hanamaruImage.setVisibility(View.VISIBLE);
    }

    private void resetButtonState() {
        completeButton.setBackgroundTintList(
                ColorStateList.valueOf(Color.parseColor("#F1B971"))
        );
        completeButton.setText("やった！");
        hanamaruImage.setVisibility(View.INVISIBLE);
    }
}