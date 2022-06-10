package xyz.xiaogai.ibook.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class iBookDBHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "book.db";
	private static final int DATABASE_VERSION = 1;

	public iBookDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS book(id TEXT PRIMARY KEY AUTOINCREMENT,name TEXT,author TEXT,price INTEGER,image TEXT,description TEXT,category_name TEXT,num INTEGER)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
