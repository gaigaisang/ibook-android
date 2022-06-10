package xyz.xiaogai.ibook.db;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import xyz.xiaogai.ibook.bean.Book;


public class iBookDBManager {
    private SQLiteDatabase db;

    public iBookDBManager(Context context) {
        iBookDBHelper helper = new iBookDBHelper(context);
        db = helper.getWritableDatabase();
    }
    
    public void add(List<Book> books) {
        try {
            db.beginTransaction();    //开始事务
            for (Book book : books) {
                db.execSQL("INSERT INTO book(id,name,author,price,image,description,category_name,num) VALUES(?,?,?,?,?,?,?,?)",
                        new Object[]{book.getId(), book.getName(), book.getAuthor(), book.getPrice(), book.getImage(), book.getDescription(), book.getCategory_name(), book.getNum()});
            }
            db.setTransactionSuccessful();    //事务成功完成
            db.endTransaction();    //结束事务
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

//    @SuppressLint("Range")
//    public List<Friend> getFriends() {
//        List<Friend> friends = new ArrayList<Friend>();
//        Cursor cursor = db.rawQuery("SELECT * FROM friend", null);
//        if (cursor != null && cursor.getCount() > 0) {
//            cursor.moveToFirst();
//            do {
//                Friend friend = new Friend();
//                friend.setNickname(cursor.getString(cursor.getColumnIndex("nickname")));
//                friend.setRemark(cursor.getString(cursor.getColumnIndex("remark")));
//                friend.setUser_id(cursor.getLong(cursor.getColumnIndex("user_id")));
//                System.out.println(friend.getNickname() + " " + friend.getRemark() + " " + friend.getUser_id());
//                friends.add(friend);
//            } while (cursor.moveToNext());
//            cursor.close();
//        }
//
//        return friends;
//    }
////    清空数据库
//    public void deleteAll() {
//        db.execSQL("DELETE FROM friend");
//    }

    // R
//	public List<Person> queryAll() {
//		List<Person> persons = new ArrayList<>();
//		Cursor cursor = db.rawQuery("SELECT * FROM person", null);
//		if (cursor != null && cursor.getCount() > 0) {
//			cursor.moveToFirst();
//			do {
//				Person person = new Person();
//				person.setId(cursor.getInt(cursor.getColumnIndex("id")));
//				person.setName(cursor.getString(cursor.getColumnIndex("name")));
//				person.setYearOfBirth(cursor.getInt(cursor.getColumnIndex("year_of_birth")));
//				person.setNationality(cursor.getString(cursor.getColumnIndex("nationality")));
//				person.setInfo(cursor.getString(cursor.getColumnIndex("info")));
//				person.setNetAsset(cursor.getInt(cursor.getColumnIndex("net_asset")));
//				person.setRanking(cursor.getInt(cursor.getColumnIndex("ranking")));
//				person.setIconID(cursor.getInt(cursor.getColumnIndex("icon_id")));
//				persons.add(person);
//			} while (cursor.moveToNext());
//			cursor.close();
//		}
//
//		return persons;
//	}
//
//	// U
//	public void updateNetAsset(Person person) {
//		ContentValues cv = new ContentValues();
//		cv.put("net_asset", person.getNetAsset());
//		db.update("person", cv, "name=?", new String[]{person.getName()});
//	}
//
//	// D
//	public void deletePerson(Person person) {
//		db.delete("person", "year_of_birth=?", new String[]{String.valueOf(person.getYearOfBirth())});
//	}

    public void closeDB() {
        if (db != null) {
            db.close();
        }
    }
}
