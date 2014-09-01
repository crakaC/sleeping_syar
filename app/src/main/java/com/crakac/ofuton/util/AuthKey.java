package com.crakac.ofuton.util;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;

import android.content.Context;

/**
 * Twitter consumer key and consumer secret.
 * @author Kosuke
 *
 */
public class AuthKey {
	private String mConsumerKey;
	private String mConsumerSecret;
	
	//リソースから引っ張ってくるだけじゃあセキュリティ上よろしくないので，暗号化したファイルから読み込むようにしたい．
	private void loadConsumerKey(Context context){
		if(C.IS_PRODUCT){
			mConsumerKey = context.getString(R.string.twitter_consumer_key);
		} else {
			mConsumerKey = context.getString(R.string.twitter_consumer_key_debug);
		}
	}
	
	private void loadConsumerSecret(Context context){
		if(C.IS_PRODUCT){
			mConsumerSecret = context.getString(R.string.twitter_consumer_secret);
		} else {
			mConsumerSecret = context.getString(R.string.twitter_consumer_secret_debug);
		}
	}
	
	public AuthKey(Context context){
		loadConsumerKey(context);
		loadConsumerSecret(context);
	}
	
	public String getConsumerKey() {
		return mConsumerKey;
	}
	
	public String getConsumerSecret() {
		return mConsumerSecret;
	}
}
