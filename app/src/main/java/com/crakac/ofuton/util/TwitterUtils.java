/**
 * Created by Kosuke on 13/05/18.
 */

package com.crakac.ofuton.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterUtils {
	//private static final String TAG = TwitterUtils.class.getSimpleName();
	private static final String TOKEN = "token";
	private static final String TOKEN_SECRET = "tokenSecret";
	private static final String PREF_NAME = "accessToken";
	private static Context mContext;
	private static Account currentAccount;
	private static String mConsumerKey;
	private static String mConsumerSecret;

	private static Configuration createConfiguration(){
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setOAuthConsumerKey(mConsumerKey);
		cb.setOAuthConsumerSecret(mConsumerSecret);
		return cb.build();
	}

	public static void init(Context context) {
		mContext = context;
		AuthKey authKey = new AuthKey(context);
		mConsumerKey = authKey.getConsumerKey();
		mConsumerSecret = authKey.getConsumerSecret();
		currentAccount = getCurrentAccount();
	}

	/**
	 * Return Twitter instance without request token
	 *
	 * @param context
	 * @return
	 */
	public static Twitter getTwitterInstanceWithoutToken() {
		Configuration conf = createConfiguration();
		TwitterFactory factory = new TwitterFactory(conf);
		Twitter twitter = factory.getInstance();
		return twitter;
	}

	/**
	 * Twitter instance
	 *
	 * @param context
	 * @return
	 */
	public static Twitter getTwitterInstance() {
		Twitter twitter = getTwitterInstanceWithoutToken();
		if (existsCurrentAccount()) {
			Account currentUser = getCurrentAccount();
			twitter.setOAuthAccessToken(new AccessToken(currentUser.getToken(),
					currentUser.getTokenSecret()));
		}
		return twitter;
	}

	public static TwitterStream getTwitterStreamInstance() {
		TwitterStreamFactory factory = new TwitterStreamFactory(createConfiguration());
		TwitterStream twitter = factory.getInstance();

		if (existsCurrentAccount()) {
			Account currentUser = getCurrentAccount();
			twitter.setOAuthAccessToken(new AccessToken(currentUser.getToken(),
					currentUser.getTokenSecret()));
		}
		return twitter;
	}

	/**
	 * Store access_token to preference.
	 *
	 * @param context
	 * @param accessToken
	 */
	public static void storeAccessToken(AccessToken accessToken) {
		SharedPreferences preferences = mContext.getSharedPreferences(
				PREF_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(TOKEN, accessToken.getToken());
		editor.putString(TOKEN_SECRET, accessToken.getTokenSecret());
		editor.commit();
	}

	/**
	 * load access token from preference
	 *
	 * @param context
	 * @return
	 */
	public static AccessToken loadAccessToken() {
		SharedPreferences preferences = mContext.getSharedPreferences(
				PREF_NAME, Context.MODE_PRIVATE);
		String token = preferences.getString(TOKEN, null);
		String tokenSecret = preferences.getString(TOKEN_SECRET, null);
		if (token != null && tokenSecret != null) {
			return new AccessToken(token, tokenSecret);
		} else {
			return null;
		}
	}

	/**
	 * return current user's twitter id
	 *
	 * @param context
	 * @return user id
	 */
	public static long getCurrentAccountId() {
		if (currentAccount != null) {
			return currentAccount.getUserId();
		} else {
			return -1;
		}
	}

	public static void removeAccount(Account account) {
		AccountDBAdapter dbAdapter = new AccountDBAdapter(mContext);
		dbAdapter.open();
		dbAdapter.deleteAccount(account.getUserId());
		dbAdapter.close();
	}

	public static boolean addAccount() {
		boolean result = false;
		Twitter tw = getTwitterInstanceWithoutToken();
		tw.setOAuthAccessToken(loadAccessToken());
		AccountDBAdapter dbAdapter = new AccountDBAdapter(mContext);
		dbAdapter.open();
		twitter4j.User user;
		try {
			user = tw.showUser(tw.getId());
			if (dbAdapter.accountExists(user.getId())) {
				return false;
			}
			dbAdapter.saveAccount(new Account(user.getId(), user.getScreenName(),
					user.getProfileImageURL(), loadAccessToken().getToken(),
					loadAccessToken().getTokenSecret(), false));
			result = true;
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		dbAdapter.close();
		return result;
	}

	public static void setCurrentAccount(Account account) {
		// DB上に情報を保存
		AccountDBAdapter dbAdapter = new AccountDBAdapter(mContext);
		dbAdapter.open();
		dbAdapter.setCurrentAccount(account);
		dbAdapter.close();
		currentAccount = account;
	}

	public static Account getCurrentAccount() {
		// すでにcurrentUserが存在する場合
		if (currentAccount != null) {
			return currentAccount;
		}
		AccountDBAdapter dbAdapter = new AccountDBAdapter(mContext);
		dbAdapter.open();
		Cursor c = dbAdapter.getCurrentAccount();
		if (c.moveToFirst()) {
			currentAccount = new Account(
					c.getLong(c.getColumnIndex(AccountDBAdapter.COL_USERID)),
					c.getString(c.getColumnIndex(AccountDBAdapter.COL_SCREEN_NAME)),
					c.getString(c.getColumnIndex(AccountDBAdapter.COL_ICON_URL)),
					c.getString(c.getColumnIndex(AccountDBAdapter.COL_TOKEN)),
					c.getString(c
							.getColumnIndex(AccountDBAdapter.COL_TOKEN_SECRET)),
					c.getInt(c.getColumnIndex(AccountDBAdapter.COL_IS_CURRENT)) > 0);
		}
		dbAdapter.close();
		return currentAccount;
	}

	public static boolean existsCurrentAccount() {
		return currentAccount != null;
	}

	public static List<Account> getAccounts() {
		List<Account> users = new ArrayList<Account>();
		AccountDBAdapter dbAdapter = new AccountDBAdapter(mContext);
		dbAdapter.open();
		Cursor c = dbAdapter.getAllAccounts();
		while (c.moveToNext()) {
			users.add(new Account(
					c.getLong(c.getColumnIndex(AccountDBAdapter.COL_USERID)),
					c.getString(c.getColumnIndex(AccountDBAdapter.COL_SCREEN_NAME)),
					c.getString(c.getColumnIndex(AccountDBAdapter.COL_ICON_URL)),
					c.getString(c.getColumnIndex(AccountDBAdapter.COL_TOKEN)),
					c.getString(c
							.getColumnIndex(AccountDBAdapter.COL_TOKEN_SECRET)),
					c.getInt(c.getColumnIndex(AccountDBAdapter.COL_IS_CURRENT)) > 0));
		}
		dbAdapter.close();
		return users;
	}

	public static boolean addList(TwitterList list) {
		boolean result;
		AccountDBAdapter dbAdapter = new AccountDBAdapter(mContext);
		dbAdapter.open();
		result = dbAdapter.saveList(list);
		dbAdapter.close();
		return result;
	}

	public static boolean removeList(TwitterList list) {
		boolean result;
		AccountDBAdapter dbAdapter = new AccountDBAdapter(mContext);
		dbAdapter.open();
		result = dbAdapter.deleteList(list);
		dbAdapter.close();
		return result;
	}

	public static List<TwitterList> getListsOfCurrentAccount() {
		List<TwitterList> list = new ArrayList<TwitterList>();
		AccountDBAdapter dbAdapter = new AccountDBAdapter(mContext);
		dbAdapter.open();
		Cursor c = dbAdapter.getLists(getCurrentAccount().getUserId());
		while (c.moveToNext()) {
			list.add(new TwitterList(c.getLong(c
					.getColumnIndex(AccountDBAdapter.COL_USERID)), c.getInt(c
					.getColumnIndex(AccountDBAdapter.COL_LIST_ID)), c.getString(c
					.getColumnIndex(AccountDBAdapter.COL_LIST_NAME)), c
					.getString(c
							.getColumnIndex(AccountDBAdapter.COL_LIST_LONGNAME))));
		}
		dbAdapter.close();// cursorを使用するより先にcloseするとinvalid statement in
							// fillWindow()とかいうエラーが出る
		return list;
	}
}