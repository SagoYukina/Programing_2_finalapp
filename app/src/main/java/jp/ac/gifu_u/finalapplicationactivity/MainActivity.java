package jp.ac.gifu_u.finalapplicationactivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import android.widget.Toast;

import jp.ac.gifu_u.finalapplicationactivity.util.ChallengeUtil;   // ← util のパッケージに合わせて

public class MainActivity extends AppCompatActivity {

    private TextView challengeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);                 // もともとの処理①
        setContentView(R.layout.activity_main); // もともとの処理②

        // チャレンジ文のリストを取得
        String[] challenges = getResources().getStringArray(R.array.challenge_list);
        Log.d("ChallengeTest", "challenge[0] = " + challenges[0]);

        // ステータスバー分のパディング反映（もともとの処理③）
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // Toolbar を ActionBar に
        setSupportActionBar(findViewById(R.id.toolbar));

        // ① TextView を取得
        challengeText = findViewById(R.id.challengeText);

        // ② 今日のチャレンジを取得して表示
        challengeText.setText(ChallengeUtil.getTodayChallenge(this));

        Button completeButton = findViewById(R.id.completeButton);
        completeButton.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "チャレンジ達成！", Toast.LENGTH_SHORT).show();
        });
    }
}
