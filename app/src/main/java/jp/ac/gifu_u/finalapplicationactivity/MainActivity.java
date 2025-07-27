package jp.ac.gifu_u.finalapplicationactivity;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.ac.gifu_u.finalapplicationactivity.util.ChallengeUtil;

public class MainActivity extends AppCompatActivity {

    // ── バッジ表示用しきい値 ──
    private static final int BADGE_1DAYS = 1;
    private static final int BADGE_2DAYS = 2;
    private static final int BADGE_3DAYS = 3;
    private static final int BADGE_4DAYS = 4;
    private static final int BADGE_5DAYS = 5;

    private Button completeButton;
    private ImageView hanamaruImage;
    private TextView streakText;
    private View badgeView;
    private TextView badgeTitle;

    private SharedPreferences prefs;
    private static final String PREFS_NAME = "ChallengePrefs";
    private static final String KEY_DATE = "done_date";
    private static final String KEY_DONE = "done";
    private static final String KEY_LAST_DATE = "last_date";
    private static final String KEY_STREAK = "streak";

    private String today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // ── 「今日」を取得 ──
        today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        // ビュー取得
        TextView challengeText = findViewById(R.id.challengeText);
        completeButton = findViewById(R.id.completeButton);
        hanamaruImage = findViewById(R.id.hanamaruImage);
        streakText = findViewById(R.id.streakText);
        badgeView = findViewById(R.id.badgeView);
        badgeTitle = badgeView.findViewById(R.id.badgeTitle);

        // チャレンジ文セット（毎回日付ベースで変わる）
        challengeText.setText(ChallengeUtil.getTodayChallenge(this));

        // SharedPreferences 初期化
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // ボタン押下処理
        completeButton.setOnClickListener(v -> {
            boolean isDone = prefs.getBoolean(KEY_DONE, false);

            if (isDone) {
                // ── キャンセル処理 ──
                int currentStreak = prefs.getInt(KEY_STREAK, 0);
                prefs.edit()
                        .putBoolean(KEY_DONE, false)
                        .putInt(KEY_STREAK, Math.max(0, currentStreak - 1))
                        .putString(KEY_LAST_DATE, "")
                        .apply();
                resetButtonState();
                Toast.makeText(this, "キャンセルしました", Toast.LENGTH_SHORT).show();
            } else {
                // ── 達成処理 ──
                prefs.edit()
                        .putBoolean(KEY_DONE, true)
                        .putString(KEY_DATE, today)
                        .apply();
                setButtonToDoneState();
                Toast.makeText(this, "チャレンジ達成！", Toast.LENGTH_SHORT).show();
                updateStreakOnPress();
            }

            // 表示更新
            updateStreakDisplay();
            updateBadges();
        });

        // 起動時にもバッジを更新しておく
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
        boolean isDone = prefs.getBoolean(KEY_DONE, false);

        if (!today.equals(savedDate)) {
            prefs.edit().putBoolean(KEY_DONE, false).apply();
            resetButtonState();
        } else {
            if (isDone) setButtonToDoneState();
            else resetButtonState();
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
        int streak = prefs.getInt(KEY_STREAK, 0);

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

        if (streak >= BADGE_5DAYS) {
            badgeTitle.setText(BADGE_5DAYS + "日");
            badgeView.setVisibility(View.VISIBLE);
        } else if (streak >= BADGE_4DAYS) {
            badgeTitle.setText(BADGE_4DAYS + "日");
            badgeView.setVisibility(View.VISIBLE);
        } else if (streak >= BADGE_3DAYS) {
            badgeTitle.setText(BADGE_3DAYS + "日");
            badgeView.setVisibility(View.VISIBLE);
        } else if (streak >= BADGE_2DAYS) {
            badgeTitle.setText(BADGE_2DAYS + "日");
            badgeView.setVisibility(View.VISIBLE);
        } else if (streak >= BADGE_1DAYS) {
            badgeTitle.setText(BADGE_1DAYS + "日");
            badgeView.setVisibility(View.VISIBLE);
        } else {
            badgeView.setVisibility(View.GONE);
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