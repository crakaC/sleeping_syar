package com.crakac.ofuton.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AccountDBAdapter {
	static final String DATABASE_NAME = "acount.db";
	static final int DATABASE_VERSION = 1;

	public static final String ACCOUNT_TABLE = "users";
	public static final String LIST_TABLE = "lists";

	public static final String COL_USERID = "UserID";

	public static final String COL_SCREEN_NAME = "ScreenName";
	public static final String COL_ICON_URL = "IconURL";
	public static final String COL_TOKEN = "Token";
	public static final String COL_TOKEN_SECRET = "TokenSecret";
	public static final String COL_IS_CURRENT = "IsCurrent";

	public static final String COL_LIST_ID = "ListID";
	public static final String COL_LIST_NAME = "Name";
	public static final String COL_LIST_LONGNAME = "FullName";

	protected final Context context;
	protected DatabaseHelper dbHelper;
	protected SQLiteDatabase db;

	public AccountDBAdapter(Context context){
		this.context = context;
		dbHelper = new DatabaseHelper(this.context);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper{
		Context mContext;
		public DatabaseHelper(Context context){
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mContext = context;
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			try{
				execSql(db, "sql/create");
			} catch (IOException e){
				e.printStackTrace();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			try{
				execSql(db,"sql/drop" );
			} catch (IOException e){
				e.printStackTrace();
			}
			onCreate(db);
		}

		/**
		 * 引数に指定したassets内のsqlを実行する
		 * @param db
		 * @param assetsDir
		 */
		private void execSql(SQLiteDatabase db, String assetsDir) throws IOException {
			AssetManager as = mContext.getResources().getAssets();
			try{
				String files[] = as.list(assetsDir);
				for(int i = 0; i < files.length; i++){
					String str = readFile(as.open(assetsDir+"/"+files[i]));
					for(String sql : str.split("/")){
						db.execSQL(sql);
					}
				}
			} catch (IOException e){
				e.printStackTrace();
			}
		}

		/**
		 * ファイルから文字列を読み込む
		 * @param is
		 * @return ファイルの文字列
		 * @throws java.io.IOException
		 */
		private String readFile(InputStream is) throws IOException{
			BufferedReader br = null;
			try{
				br = new BufferedReader(new InputStreamReader(is));

				StringBuilder sb = new StringBuilder();
				String str;
				while((str = br.readLine())!=null){
					sb.append(str + "\n");
				}
				return sb.toString();
			} finally {
				if(br!=null){
					br.close();
				}
			}
		}
	}

	public AccountDBAdapter open() {
		db = dbHelper.getWritableDatabase();
		return this;
	}

	public void close(){
		dbHelper.close();
	}

	public Cursor getAllAccounts(){
		return db.query(ACCOUNT_TABLE, null, null, null, null, null, null);
	}

	public boolean accountExists(long userId){
		String[] columns = { COL_USERID };
		Cursor c = db.query(ACCOUNT_TABLE, columns, COL_USERID+"="+userId, null, null, null, null);
		return c.moveToFirst();
	}

	public boolean deleteAccount(long userId){
		return db.delete(ACCOUNT_TABLE, COL_USERID + "=" + userId, null ) > 0;
	}

	/**
	 * ユーザーを追加する.追加する前に，すでにユーザーが存在していないかチェックすること．
	 * @param account
	 */
	public void saveAccount(Account account){
		deleteAccount(account.getUserId());
		ContentValues values = new ContentValues();
		values.put(COL_USERID, account.getUserId());
		values.put(COL_SCREEN_NAME, account.getScreenName());
		values.put(COL_ICON_URL, account.getIconUrl());
		values.put(COL_TOKEN, account.getToken());
		values.put(COL_TOKEN_SECRET, account.getTokenSecret());
		db.insertOrThrow(ACCOUNT_TABLE, null, values);
	}

	public void setCurrentAccount(Account account) {
		//前回までのcurrent userをただのuserにする
		ContentValues values = new ContentValues();
		values.put(COL_IS_CURRENT, -1);
		db.update(ACCOUNT_TABLE, values, COL_IS_CURRENT + "=1", null);
		values = new ContentValues();
		values.put(COL_IS_CURRENT, 1);
		db.update(ACCOUNT_TABLE, values, COL_USERID + "=" +account.getUserId(), null);
	}
	public Cursor getCurrentAccount(){
		return db.query(ACCOUNT_TABLE, null, COL_IS_CURRENT+"=1", null, null, null, null);
		//COL_IS_CURRENTは-1でfalse, 1がtrueってことにしてる
	}

	public Cursor getLists(long userId){
		String selection ="UserID = ?";
		String[] selectionArgs = { String.valueOf(userId) };
		return db.query(LIST_TABLE, null, selection, selectionArgs, null, null, COL_LIST_ID + " desc");
	}

	public boolean saveList(TwitterList list){
		deleteList(list);
		ContentValues values = new ContentValues();
		values.put(COL_USERID, list.getUserId());
		values.put(COL_LIST_ID, list.getListId());
		values.put(COL_LIST_NAME, list.getName());
		values.put(COL_LIST_LONGNAME, list.getFullName());
		return db.insertOrThrow(LIST_TABLE, null, values) > 0;
	}
	/**
	 * 指定したリストIDのリストを消す．
	 * @param listId
	 * @return
	 */
	public boolean deleteList(TwitterList list){
		String whereClause = COL_LIST_ID + " = ? AND " + COL_USERID + " = ?";
		String[] whereArgs = new String[]{ Long.toString(list.getListId()), Long.toString(list.getUserId())};
		return db.delete(LIST_TABLE, whereClause, whereArgs ) > 0;
	}

	/**
	 * ユーザーIDのリストを全部消す．
	 * @param userId
	 * @return
	 */
	public boolean deleteLists(long userId){
		return db.delete(LIST_TABLE, COL_USERID + "=" + userId, null) > 0;
	}
}
