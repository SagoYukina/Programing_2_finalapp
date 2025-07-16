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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import jp.ac.gifu_u.finalapplicationactivity.util.ChallengeUtil;

public class MainActivity extends AppCompatActivity {

    private Button completeButton;
    private ImageView hanamaruImage;

    private SharedPreferences prefs;
    private final String PREFS_NAME = "ChallengePrefs";
    private final String KEY_DATE = "done_date";
    private final String KEY_DONE = "done";
    private final String KEY_LAST_DATE = "last_date";
    private final String KEY_STREAK = "streak";

    private String today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        TextView challengeText = findViewById(R.id.challengeText);
        challengeText.setText(ChallengeUtil.getTodayChallenge(this));

        completeButton = findViewById(R.id.completeButton);
        hanamaruImage = findViewById(R.id.hanamaruImage);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadButtonState();

        completeButton.setOnClickListener(v -> {
            boolean isDone = prefs.getBoolean(KEY_DONE, false);

            if (isDone) {
                // 押し直したら未達成状態に戻す
                prefs.edit().putBoolean(KEY_DONE, false).apply();
                resetButtonState();
                Toast.makeText(MainActivity.this, "キャンセルしました", Toast.LENGTH_SHORT).show();
            } else {
                // 達成した場合の保存
                prefs.edit()
                        .putBoolean(KEY_DONE, true)
                        .putString(KEY_DATE, today)
                        .apply();
                setButtonToDoneState();
                Toast.makeText(MainActivity.this, "チャレンジ達成！", Toast.LENGTH_SHORT).show();
            }
            // 「やった」ボタン押下時に連続日数を更新
            updateStreakIfNeeded();
        });
        updateStreakDisplay();  // 表示更新
    }

    private void loadButtonState() {
        String savedDate = prefs.getString(KEY_DATE, "");
        boolean isDone = prefs.getBoolean(KEY_DONE, false);

        if (today.equals(savedDate) && isDone) {
            setButtonToDoneState();
        } else {
            resetButtonState();
            prefs.edit().putBoolean(KEY_DONE, false).putString(KEY_DATE, today).apply();
        }
        updateStreakIfNeeded();
    }

    private void updateStreakIfNeeded() {
        System.out.print("A");
        String lastDate = prefs.getString(KEY_LAST_DATE, "");
        int streak = prefs.getInt(KEY_STREAK, 0);

        if (lastDate.isEmpty()) {
            // 初回：0日で保存
            prefs.edit()
                    .putInt(KEY_STREAK, 1)
                    .putString(KEY_LAST_DATE, today)
                    .apply();
            return;
        }

        if (lastDate.equals(today)) {
            // 今日すでに記録済み
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            Date last = sdf.parse(lastDate);
            Date current = sdf.parse(today);

            if (last != null && current != null) {
                long diff = (current.getTime() - last.getTime()) / (1000 * 60 * 60 * 24);

                if (diff == 1) {
                    streak++;
                } else {
                    streak = 1; // 連続じゃないのでリセット
                }

                prefs.edit()
                        .putInt(KEY_STREAK, streak)
                        .putString(KEY_LAST_DATE, today)
                        .apply();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Date parse error", e);
        }

        updateStreakDisplay();
    }

    private void updateStreakDisplay() {
        int streak = prefs.getInt(KEY_STREAK, 0);
        TextView streakText = findViewById(R.id.streakText);
        streakText.setText(getString(R.string.streak_text, streak));  // プレースホルダ使用
    }

    private void setButtonToDoneState() {
        completeButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C98A4A")));
        completeButton.setText("やった！");
        hanamaruImage.setVisibility(View.VISIBLE);
    }

    private void resetButtonState() {
        completeButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1B971")));
        completeButton.setText("やった！");
        hanamaruImage.setVisibility(View.INVISIBLE);
    }
}
