package com.crakac.ofuton.util;

import java.util.Map;

import twitter4j.MediaEntity;
import twitter4j.URLEntity;

/**
 * Created by kosukeshirakashi on 2014/10/07.
 */
public class GuessedMediaEntity implements MediaEntity {
    private URLEntity mURLEntity;

    public GuessedMediaEntity(URLEntity urlEntity) {
        mURLEntity = urlEntity;
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public String getMediaURL() {
        return mURLEntity.getExpandedURL();
    }

    @Override
    public String getMediaURLHttps() {
        return getMediaURL();
    }

    @Override
    public Map<Integer, Size> getSizes() {
        return null;
    }

    @Override
    public String getType() {
        return "photo";
    }

    @Override
    public int getVideoAspectRatioWidth() {
        return 0;
    }

    @Override
    public int getVideoAspectRatioHeight() {
        return 0;
    }

    @Override
    public long getVideoDurationMillis() {
        return 0;
    }

    @Override
    public Variant[] getVideoVariants() {
        return new Variant[0];
    }

    @Override
    public String getExtAltText() {
        return null;
    }

    @Override
    public String getText() {
        return mURLEntity.getText();
    }

    @Override
    public String getURL() {
        return mURLEntity.getURL();
    }

    @Override
    public String getExpandedURL() {
        return mURLEntity.getExpandedURL();
    }

    @Override
    public String getDisplayURL() {
        return mURLEntity.getDisplayURL();
    }

    @Override
    public int getStart() {
        return mURLEntity.getStart();
    }

    @Override
    public int getEnd() {
        return mURLEntity.getEnd();
    }
}
