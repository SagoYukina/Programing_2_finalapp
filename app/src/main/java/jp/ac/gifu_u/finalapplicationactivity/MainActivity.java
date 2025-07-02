package jp.ac.gifu_u.finalapplicationactivity;

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

import jp.ac.gifu_u.finalapplicationactivity.util.ChallengeUtil;

public class MainActivity extends AppCompatActivity {

    private TextView challengeText;
    private Button completeButton;
    private ImageView hanamaruImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);  // ← layoutファイル名が"activity_main.xml"でOK

        // チャレンジ文のリストを取得
        String[] challenges = getResources().getStringArray(R.array.challenge_list);
        Log.d("ChallengeTest", "challenge[0] = " + challenges[0]);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        setSupportActionBar(findViewById(R.id.toolbar));

        // チャレンジ文をセット
        challengeText = findViewById(R.id.challengeText);
        challengeText.setText(ChallengeUtil.getTodayChallenge(this));

        // ボタン・花丸のView取得
        completeButton = findViewById(R.id.completeButton);
        hanamaruImage = findViewById(R.id.hanamaruImage);  // 画像Viewをxmlに定義しておくこと！

        // ボタンクリック時の処理
        completeButton.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "チャレンジ達成！", Toast.LENGTH_SHORT).show();

            // ボタンの色を暗くする
            completeButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C98A4A")));
            completeButton.setEnabled(false);  // 再クリック防止

            // 花丸を表示
            hanamaruImage.setVisibility(View.VISIBLE);
        });
    }
}
