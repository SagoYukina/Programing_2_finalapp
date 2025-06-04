package jp.ac.gifu_u.finalapplicationactivity.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import jp.ac.gifu_u.finalapplicationactivity.R;

public final class ChallengeUtil {

    private static final String PREF     = "challenge_pref";
    private static final String KEY_DATE = "lastShownDate";
    private static final String KEY_TEXT = "todayChallengeText";

    /** 日付を見て、今日のチャレンジ文を返す */
    public static String getTodayChallenge(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);

        // ← ここが LocalDate ではなく Date＋SDF
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(new Date());

        // すでに今日取得済み？
        if (today.equals(sp.getString(KEY_DATE, ""))) {
            return sp.getString(KEY_TEXT, "");   // 保存済みを返す
        }

        // 新しい日 → ランダム抽選
        String[] list = ctx.getResources().getStringArray(R.array.challenge_list);
        String text   = list[new Random().nextInt(list.length)];

        // 保存
        sp.edit()
                .putString(KEY_DATE, today)
                .putString(KEY_TEXT, text)
                .apply();

        return text;
    }

    private ChallengeUtil() {}   // インスタンス不可
}
