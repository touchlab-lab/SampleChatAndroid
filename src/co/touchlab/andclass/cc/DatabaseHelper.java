package co.touchlab.andclass.cc;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kgalligan
 * Date: 10/14/11
 * Time: 1:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "demo.db";
    private static final int DATABASE_VERSION = 1;
    private static DatabaseHelper instance;
    public static final String CHATLOG = "chatlog";

    public static synchronized DatabaseHelper getInstance(Context c)
    {
        if(instance == null)
            instance = new DatabaseHelper(c);

        return instance;
    }

    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        String createTable =
                "CREATE TABLE "+ CHATLOG +" "+
                        "(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                        "nickname VARCHAR, "+
                        "post VARCHAR"+
                        ")";

        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {
        sqLiteDatabase.execSQL("drop table "+ CHATLOG);
        onCreate(sqLiteDatabase);
    }

    public void addChatPost(String nickname, String post)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put("nickname", nickname);
        contentValues.put("post", post);
        getWritableDatabase().insertOrThrow(CHATLOG, null, contentValues);
    }

    public List<ChatMessage> getAllMessages()
    {
        Cursor cursor = getReadableDatabase().query(CHATLOG, new String[]{"nickname", "post"}, null, null, null, null, "id");
        List<ChatMessage> messages = new ArrayList<ChatMessage>();

        while (cursor.moveToNext())
        {
            messages.add(new ChatMessage(ChatMessage.Type.Message, cursor.getString(0), cursor.getString(1)));
        }

        return messages;
    }

    public void deleteAllMessages()
    {
        getWritableDatabase().delete(CHATLOG, null, null);
    }
}
