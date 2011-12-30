package co.touchlab.andclass.cc;

import co.touchlab.android.util.App;

/**
 * Created by IntelliJ IDEA.
 * User: kgalligan
 * Date: 10/15/11
 * Time: 1:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class ThreadInfo
{
    public static void printThreadInfo(String s)
    {
        App.log("Thread id: "+ Thread.currentThread().getId() + " - "+ s);
    }
}
