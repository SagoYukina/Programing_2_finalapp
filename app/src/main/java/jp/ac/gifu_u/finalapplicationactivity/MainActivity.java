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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.ac.gifu_u.finalapplicationactivity.util.ChallengeUtil;

public class MainActivity extends AppCompatActivity {

    private TextView challengeText;
    private Button completeButton;
    private ImageView hanamaruImage;

    private SharedPreferences prefs;
    private final String PREFS_NAME = "ChallengePrefs";
    private final String KEY_DATE = "done_date";
    private final String KEY_DONE = "done";

    private String today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        String[] challenges = getResources().getStringArray(R.array.challenge_list);
        Log.d("ChallengeTest", "challenge[0] = " + challenges[0]);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        setSupportActionBar(findViewById(R.id.toolbar));

        challengeText = findViewById(R.id.challengeText);
        challengeText.setText(ChallengeUtil.getTodayChallenge(this));

        completeButton = findViewById(R.id.completeButton);
        hanamaruImage = findViewById(R.id.hanamaruImage);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

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
        });
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
