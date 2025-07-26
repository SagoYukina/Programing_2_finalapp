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
    private static final String PREFS_NAME    = "ChallengePrefs";
    private static final String KEY_DATE      = "done_date";
    private static final String KEY_DONE      = "done";
    private static final String KEY_LAST_DATE = "last_date";
    private static final String KEY_STREAK    = "streak";

    private String today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // ビュー取得
        TextView challengeText = findViewById(R.id.challengeText);
        completeButton = findViewById(R.id.completeButton);
        hanamaruImage  = findViewById(R.id.hanamaruImage);
        streakText     = findViewById(R.id.streakText);

        // チャレンジ文セット（毎回日付ベースで変わる）
        challengeText.setText(ChallengeUtil.getTodayChallenge(this));

        // SharedPreferences 初期化
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // ボタン押下処理
        completeButton.setOnClickListener(v -> {
            boolean isDone = prefs.getBoolean(KEY_DONE, false);

            if (isDone) {
                // キャンセル処理
                int currentStreak = prefs.getInt(KEY_STREAK, 0);
                prefs.edit()
                        .putBoolean(KEY_DONE, false)
                        .putInt(KEY_STREAK, Math.max(0, currentStreak - 1))
                        .putString(KEY_LAST_DATE, "")
                        .apply();
                resetButtonState();
                Toast.makeText(this, "キャンセルしました", Toast.LENGTH_SHORT).show();
            } else {
                // 達成処理
                prefs.edit()
                        .putBoolean(KEY_DONE, true)
                        .putString(KEY_DATE, today)
                        .apply();
                setButtonToDoneState();
                Toast.makeText(this, "チャレンジ達成！", Toast.LENGTH_SHORT).show();
                updateStreakOnPress();
            }
            updateStreakDisplay();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 毎回フォアグラウンド復帰時に「今日」を再取得
        today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        // ボタン状態＆ストリークリセットチェック
        loadButtonState();
        // 画面にストリーク数を反映
        updateStreakDisplay();
    }

    /**
     * ボタン完了状態を復元しつつ、
     * 前回押下日とのギャップが1日以上あればストリークをリセットする
     */
    private void loadButtonState() {
        String savedDate = prefs.getString(KEY_DATE, "");
        boolean isDone   = prefs.getBoolean(KEY_DONE, false);

        // ── 今日かどうかで「やった！」ボタンの状態だけ決める ──
        if (!today.equals(savedDate)) {
            // 今日初回 → DONEフラグ false に
            prefs.edit().putBoolean(KEY_DONE, false).apply();
            resetButtonState();
        } else {
            // 今日中に一度でも達成していれば DONE
            if (isDone) setButtonToDoneState();
            else        resetButtonState();
        }

        // ── 前回押下日とのギャップチェック ──
        String lastDate = prefs.getString(KEY_LAST_DATE, "");
        if (!lastDate.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                Date prev = sdf.parse(lastDate);
                Date curr = sdf.parse(today);
                long diffDays = (curr.getTime() - prev.getTime())
                        / (1000L * 60 * 60 * 24);
                if (diffDays > 1) {
                    // 1日以上空いた → ストリーク & 前回日付リセット
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

    /**
     * ボタン押下時にのみ呼ぶストリーク更新。
     * ・初回押下→1
     * ・同日2回目以降→無視
     * ・前日押下→+1
     * ・それ以外→1にリセット
     */
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
                long diffDays = (curr.getTime() - prev.getTime())
                        / (1000L * 60 * 60 * 24);
                if (diffDays == 1) streak++;
                else               streak = 1;
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

    /** 連続日数を画面に反映 */
    private void updateStreakDisplay() {
        int streak = prefs.getInt(KEY_STREAK, 0);
        streakText.setText(getString(R.string.streak_text, streak));
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