package com.crakac.ofuton.util;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.Touch;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.activity.UserDetailActivity;

import twitter4j.EntitySupport;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

/**
 * Created by kosukeshirakashi on 2014/10/23.
 */
public class LinkUtils {
    static class EntitySpan extends URLSpan {
        public EntitySpan(String url) {
            super(url);
        }

        @Override
        public void onClick(View widget) {
            String entity = getURL();
            Context context = widget.getContext();
            if (entity.startsWith("@")) {
                Intent i = new Intent(context, UserDetailActivity.class);
                i.putExtra(C.SCREEN_NAME, entity.replace("@", ""));
                context.startActivity(i);
            } else if (entity.startsWith("#")) {
                AppUtil.showToast("HashTag entity:" + entity);
            } else {
                super.onClick(widget);
            }
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            if(getURL().startsWith("@") || getURL().startsWith("#")){
                ds.setUnderlineText(false);
            }
        }
    }

    static class SensibleLinkOnTouchListener implements View.OnTouchListener {
        private static SensibleLinkOnTouchListener sInstance;
        public static SensibleLinkOnTouchListener getInstance(){
            if(sInstance == null){
                sInstance = new SensibleLinkOnTouchListener();
            }
            return sInstance;
        }

        private SensibleLinkOnTouchListener(){}

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            TextView widget = (TextView) v;
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                CharSequence text = widget.getText();
                Spannable buffer = Spannable.Factory.getInstance().newSpannable(text);
                ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

                if (link.length != 0) {
                    if(action == MotionEvent.ACTION_UP) {
                        link[0].onClick(widget);
                        Selection.removeSelection(buffer);
                        Log.d("MovementMethod", "removeSelection!");
                        widget.setSelected(false);
                    } else if(action == MotionEvent.ACTION_DOWN){
                        Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
                        Log.d("MovementMethod", String.format("setSelection!(%d->%d", buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0])));
                        widget.setSelected(true);
                    }
                    widget.setText(buffer);
                    return true;
                } else {
                    Selection.removeSelection(buffer);
                    widget.setText(buffer);
                    Touch.onTouchEvent(widget, buffer, event);
                    Log.d("MovementMethod", "removeSelection!");
                    widget.setSelected(false);
                    return false;
                }
            }
            return false;
        }
    }

    public static void autolink(final TextView view, String text, EntitySupport entitySupport) {
        if (TextUtils.isEmpty(text)) return;

        SpannableString spannable = new SpannableString(text);
        for (UserMentionEntity entity : entitySupport.getUserMentionEntities()) {
            spannable.setSpan(new URLSpan("@" + entity.getScreenName()), entity.getStart(), entity.getEnd(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        for (HashtagEntity entity : entitySupport.getHashtagEntities()) {
            spannable.setSpan(new URLSpan("#" + entity.getText()), entity.getStart(), entity.getEnd(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        text = Html.toHtml(spannable);

        for (URLEntity entity : entitySupport.getURLEntities()) {
            text = text.replace(entity.getURL(), "<a href=\"" + entity.getExpandedURL() + "\">" + entity.getDisplayURL() + "</a>");
        }

        for (MediaEntity entity : entitySupport.getMediaEntities()) {
            if(PrefUtil.getBoolean(R.string.show_image_in_timeline)){
                text = text.replace(entity.getURL(), "");
            } else {
                text = text.replace(entity.getURL(), "<a href=\"" + entity.getExpandedURL() + "\">" + entity.getDisplayURL() + "</a>");
            }
        }
        Log.d("autolink", text);
        spannable = new SpannableString(Html.fromHtml(text));
        URLSpan[] spans = spannable.getSpans(0, spannable.length(), URLSpan.class);
        for(URLSpan span : spans){
            int start = spannable.getSpanStart(span);
            int end = spannable.getSpanEnd(span);
            spannable.removeSpan(span);
            spannable.setSpan(new EntitySpan(span.getURL()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        view.setText(spannable);
        view.setOnTouchListener(SensibleLinkOnTouchListener.getInstance());
    }
}
