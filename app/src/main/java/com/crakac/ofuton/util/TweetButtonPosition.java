package com.crakac.ofuton.util;

import com.crakac.ofuton.R;

public enum TweetButtonPosition{
    Right(0),
    Center(1),
    Left(2),
    Unknown(-1);

    int position;
    TweetButtonPosition(int position){
        this.position = position;
    }

    public static TweetButtonPosition strToEnum(String posStr){
        for(TweetButtonPosition val : values()){
            if(posStr.equals(val.name())){
                return val;
            }
        }
        return Unknown;
    }

    public static TweetButtonPosition current(){
        String value = PrefUtil.getString(R.string.tweet_button_position);
        if(value == null){
            return Right;
        }
        return TweetButtonPosition.strToEnum(value);
    }

    public int toInt(){
        return position;
    }

}
