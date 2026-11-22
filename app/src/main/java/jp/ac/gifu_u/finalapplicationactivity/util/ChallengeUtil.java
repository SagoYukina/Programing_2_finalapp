package jp.ac.gifu_u.finalapplicationactivity.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import jp.ac.gifu_u.finalapplicationactivity.R;

public final class ChallengeUtil {

    private static final String PREF        = "ChallengePrefs";
    private static final String KEY_DATE    = "last_challenge_date";
    private static final String KEY_TEXT    = "today_challenge";

    /** 今日のチャレンジ内容を返す（初回 or 日付変更で更新） */
    public static String getTodayChallenge(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        String lastDate = sp.getString(KEY_DATE, "");
        String savedText = sp.getString(KEY_TEXT, null);

        if (!today.equals(lastDate)) {
            // 新しい日付ならランダムチャレンジ生成
            String[] list = ctx.getResources().getStringArray(R.array.challenge_list);
            String newText = list[new Random().nextInt(list.length)];

            sp.edit()
                    .putString(KEY_TEXT, newText)
                    .putString(KEY_DATE, today)
                    .apply();

            return newText;
        }

        // 同じ日なら保存済みの内容を返す
        if (savedText != null) return savedText;

        // 念のため fallback
        return "チャレンジが見つかりません";
    }

    private ChallengeUtil() {}  // インスタンス化禁止
}
