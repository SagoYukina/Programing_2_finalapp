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

    private Button completeButton;
    private ImageView hanamaruImage;
    private TextView streakText;

    private SharedPreferences prefs;
    private static final String PREFS_NAME     = "ChallengePrefs";
    private static final String KEY_DATE       = "done_date";
    private static final String KEY_DONE       = "done";
    private static final String KEY_LAST_DATE  = "last_date";
    private static final String KEY_STREAK     = "streak";

    private String today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // チャレンジ文セット
        TextView challengeText = findViewById(R.id.challengeText);
        challengeText.setText(ChallengeUtil.getTodayChallenge(this));

        // ビュー取得
        completeButton = findViewById(R.id.completeButton);
        hanamaruImage  = findViewById(R.id.hanamaruImage);
        streakText     = findViewById(R.id.streakText);

        // SharedPreferences 初期化
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadButtonState();

        completeButton.setOnClickListener(v -> {
            boolean isDone = prefs.getBoolean(KEY_DONE, false);

            if (isDone) {
                // ── キャンセル処理 ──
                int currentStreak = prefs.getInt(KEY_STREAK, 0);
                prefs.edit()
                        .putBoolean(KEY_DONE, false)
                        .putInt(KEY_STREAK, Math.max(0, currentStreak - 1))
                        // ✅ 追加：キャンセル時は［前回押下日］をクリア
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

                // ── ストリーク更新 ──
                updateStreakOnPress();
            }

            // 表示を更新
            updateStreakDisplay();
        });

        // 起動時のストリーク表示
        updateStreakDisplay();
    }

    /** ビューの状態（色・花丸）のみ復元。日付・連続記録は触らない */
    private void loadButtonState() {
        String savedDate = prefs.getString(KEY_DATE, "");
        boolean isDone   = prefs.getBoolean(KEY_DONE, false);

        if (today.equals(savedDate) && isDone) {
            setButtonToDoneState();
        } else {
            resetButtonState();
        }
    }

    /**
     * ボタン押下時のストリーク更新。
     * 前回押下日が空 or 違う日なら1扱い、前日なら＋1、同日2回目以降は無視。
     */
    private void updateStreakOnPress() {
        String lastDate = prefs.getString(KEY_LAST_DATE, "");
        int streak      = prefs.getInt(KEY_STREAK, 0);

        if (lastDate.isEmpty()) {
            // 初回
            streak = 1;
        } else if (lastDate.equals(today)) {
            // 同じ日2回目以降 → 無視
            return;
        } else {
            // 日付差分を計算
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                Date prev = sdf.parse(lastDate);
                Date curr = sdf.parse(today);
                long diffDays = (curr.getTime() - prev.getTime())
                        / (1000L * 60 * 60 * 24);

                if (diffDays == 1) {
                    streak++;
                } else {
                    streak = 1;
                }
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

    /** 画面に連続日数を表示 */
    private void updateStreakDisplay() {
        int streak = prefs.getInt(KEY_STREAK, 0);
        streakText.setText(getString(R.string.streak_text, streak));
    }

    /** 達成時のボタン・花丸表示 */
    private void setButtonToDoneState() {
        completeButton.setBackgroundTintList(
                ColorStateList.valueOf(Color.parseColor("#C98A4A"))
        );
        completeButton.setText("やった！");
        hanamaruImage.setVisibility(View.VISIBLE);
    }

    /** 未達成時のボタン表示 */
    private void resetButtonState() {
        completeButton.setBackgroundTintList(
                ColorStateList.valueOf(Color.parseColor("#F1B971"))
        );
        completeButton.setText("やった！");
        hanamaruImage.setVisibility(View.INVISIBLE);
    }
}