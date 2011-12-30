package co.touchlab.andclass.cc;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by IntelliJ IDEA.
 * User: kgalligan
 * Date: 10/14/11
 * Time: 2:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class Prefs
{

    public static final String APP_PREFS = "APP_PREFS";
    public static final String NICKNAME = "NICKNAME";

    public static SharedPreferences getPreferences(Context c)
    {
        return c.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE);
    }

    public static String getStringPref(Context c, String key, String defaultVal)
    {
        return getPreferences(c).getString(key, defaultVal);
    }

    public static void setStringPref(Context c, String key, String val)
    {
        SharedPreferences.Editor prefEdit = getPreferences(c).edit();
        prefEdit.putString(key, val);
        prefEdit.commit();
    }

    public static String getNickname(Context c)
    {
        return getStringPref(c, NICKNAME, null);
    }

    public static void setNickname(Context c, String nickname)
    {
        setStringPref(c, NICKNAME, nickname);
    }
}
