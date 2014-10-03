/**
 * Created by Kosuke on 13/05/18.
 */

package com.crakac.ofuton.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import twitter4j.MediaEntity;
import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterAPIConfiguration;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterUtils {
	//private static final String TAG = TwitterUtils.class.getSimpleName();
    public static final int HTTP_CONNECTION_TIMEOUT_MS = 10000;
    public static final int HTTP_READ_TIMEOUT_MS = 30000;
	private static final String TOKEN = "token";
	private static final String TOKEN_SECRET = "tokenSecret";
	private static final String PREF_NAME = "accessToken";
    public static final String API_CONFIG_PREF = "api_config";
	private static Context sContext;
	private static Account sAccount;
	private static String mConsumerKey;
	private static String mConsumerSecret;

	public static void init(Context context) {
		sContext = context;
		AuthKey authKey = new AuthKey(context);
		mConsumerKey = authKey.getConsumerKey();
		mConsumerSecret = authKey.getConsumerSecret();
		sAccount = getCurrentAccount();
	}

    private static Configuration createConfiguration() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(mConsumerKey);
        cb.setOAuthConsumerSecret(mConsumerSecret);
        cb.setHttpConnectionTimeout(HTTP_CONNECTION_TIMEOUT_MS);
        cb.setHttpReadTimeout(HTTP_READ_TIMEOUT_MS);
        return cb.build();
    }

    /**
	 * Return Twitter instance without request token
	 *
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
	 * @param accessToken
	 */
	public static void storeAccessToken(AccessToken accessToken) {
		SharedPreferences preferences = sContext.getSharedPreferences(
				PREF_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(TOKEN, accessToken.getToken());
		editor.putString(TOKEN_SECRET, accessToken.getTokenSecret());
		editor.commit();
	}

	/**
	 * load access token from preference
	 *
	 * @return
	 */
	public static AccessToken loadAccessToken() {
		SharedPreferences preferences = sContext.getSharedPreferences(
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
	 * @return user id
	 */
	public static long getCurrentAccountId() {
		if (sAccount != null) {
			return sAccount.getUserId();
		} else {
			return -1;
		}
	}

	public static void removeAccount(Account account) {
		AccountDBAdapter dbAdapter = new AccountDBAdapter(sContext);
		dbAdapter.open();
		dbAdapter.deleteAccount(account.getUserId());
		dbAdapter.close();
	}

	public static boolean addAccount() {
		boolean result = false;
		Twitter tw = getTwitterInstanceWithoutToken();
		tw.setOAuthAccessToken(loadAccessToken());
		AccountDBAdapter dbAdapter = new AccountDBAdapter(sContext);
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
		AccountDBAdapter dbAdapter = new AccountDBAdapter(sContext);
		dbAdapter.open();
		dbAdapter.setCurrentAccount(account);
		dbAdapter.close();
		sAccount = account;
	}

	public static Account getCurrentAccount() {
		// すでにcurrentUserが存在する場合
		if (sAccount != null) {
			return sAccount;
		}
		AccountDBAdapter dbAdapter = new AccountDBAdapter(sContext);
		dbAdapter.open();
		Cursor c = dbAdapter.getCurrentAccount();
		if (c.moveToFirst()) {
			sAccount = new Account(
					c.getLong(c.getColumnIndex(AccountDBAdapter.COL_USERID)),
					c.getString(c.getColumnIndex(AccountDBAdapter.COL_SCREEN_NAME)),
					c.getString(c.getColumnIndex(AccountDBAdapter.COL_ICON_URL)),
					c.getString(c.getColumnIndex(AccountDBAdapter.COL_TOKEN)),
					c.getString(c
							.getColumnIndex(AccountDBAdapter.COL_TOKEN_SECRET)),
					c.getInt(c.getColumnIndex(AccountDBAdapter.COL_IS_CURRENT)) > 0);
		}
		dbAdapter.close();
		return sAccount;
	}

	public static boolean existsCurrentAccount() {
		return sAccount != null;
	}

	public static List<Account> getAccounts() {
		List<Account> users = new ArrayList<Account>();
		AccountDBAdapter dbAdapter = new AccountDBAdapter(sContext);
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
		AccountDBAdapter dbAdapter = new AccountDBAdapter(sContext);
		dbAdapter.open();
		result = dbAdapter.saveList(list);
		dbAdapter.close();
		return result;
	}

	public static boolean removeList(TwitterList list) {
		boolean result;
		AccountDBAdapter dbAdapter = new AccountDBAdapter(sContext);
		dbAdapter.open();
		result = dbAdapter.deleteList(list);
		dbAdapter.close();
		return result;
	}

	public static List<TwitterList> getListsOfCurrentAccount() {
		List<TwitterList> list = new ArrayList<TwitterList>();
		AccountDBAdapter dbAdapter = new AccountDBAdapter(sContext);
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

    public static void fetchApiConfigurationAsync(final Context context){
        SharedPreferences pref = PreferenceUtil.getSharedPreference(API_CONFIG_PREF);
        long fechedOn = pref.getLong("fetched_on", 0);
        if(Util.daysPast(fechedOn) == 0){
            return;
        }
        new ParallelTask<Void, Void, TwitterAPIConfiguration>() {

            @Override
            protected TwitterAPIConfiguration doInBackground(Void... params) {
                try {
                    return TwitterUtils.getTwitterInstance().getAPIConfiguration();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(TwitterAPIConfiguration configuration) {
                if (configuration == null) {
                    Log.d("APIConfiguration", "fail to fetch configuration.");
                    return;
                }
                Editor editor = context.getSharedPreferences(API_CONFIG_PREF, Context.MODE_PRIVATE).edit();
                editor.putLong("fetched_on", System.currentTimeMillis()).commit();
                Log.d("APIConfiguration", configuration.toString());
                Util.saveFile(context, configuration, API_CONFIG_PREF);
            }
        }.executeParallel();
    }
    public static TwitterAPIConfiguration getApiConfiguration(){
        TwitterAPIConfiguration configuration = Util.restoreFile(sContext, API_CONFIG_PREF);
        if(configuration != null){
            return configuration;
        }
        return new TwitterAPIConfiguration() {
            @Override
            public int getPhotoSizeLimit() {
                return 0;
            }

            @Override
            public int getShortURLLength() {
                return 22;
            }

            @Override
            public int getShortURLLengthHttps() {
                return 23;
            }

            @Override
            public int getCharactersReservedPerMedia() {
                return 23;
            }

            @Override
            public Map<Integer, MediaEntity.Size> getPhotoSizes() {
                return null;
            }

            @Override
            public String[] getNonUsernamePaths() {
                return new String[0];
            }

            @Override
            public int getMaxMediaPerUpload() {
                return 4;
            }

            @Override
            public RateLimitStatus getRateLimitStatus() {
                return null;
            }

            @Override
            public int getAccessLevel() {
                return 0;
            }
        };
    }
}