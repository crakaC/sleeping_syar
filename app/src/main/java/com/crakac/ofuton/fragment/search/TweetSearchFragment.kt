package com.crakac.ofuton.fragment.search

import android.os.Bundle

import com.crakac.ofuton.C
import com.crakac.ofuton.R
import com.crakac.ofuton.activity.SearchActivity
import com.crakac.ofuton.fragment.timeline.AbstractTimelineFragment
import com.crakac.ofuton.util.TwitterUtils
import twitter4j.Query
import twitter4j.Status
import twitter4j.TwitterException

/**
 * Created by kosukeshirakashi on 2014/10/03.
 */
open class TweetSearchFragment : AbstractTimelineFragment(), SearchActivity.Searchable {
    protected var mQuery: String = ""
    private var mResultType: Query.ResultType? = null
    private var mOptionQuery: String? = null
    private var mIsNeedLookUp = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            mQuery = args.getString(C.QUERY)
            mOptionQuery = args.getString(C.OPTION_QUERY)
            mResultType = args.getSerializable(C.TYPE) as Query.ResultType?
            mIsNeedLookUp = args.getBoolean(C.NEED_LOOK_UP, false)
        }
        if (mResultType == null) {
            mResultType = Query.RECENT
        }
        if (savedInstanceState != null) {
            mQuery = savedInstanceState.getString(C.QUERY)
            mOptionQuery = savedInstanceState.getString(C.OPTION_QUERY)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setEmptyText(R.string.no_tweet)
        setPoolStatus(false)
    }

    override fun newStatuses(sinceId: Long, count: Int): List<Status>? {
        try {
            val result = TwitterUtils.getTwitterInstance().search(Query(buildQuery()).count(count).sinceId(sinceId).resultType(mResultType))
            if (mIsNeedLookUp) {
                val ids = result.tweets.map { t -> t.id }
                return TwitterUtils.getTwitterInstance().lookup(*ids.toLongArray());
            }
            return result.tweets
        } catch (e: TwitterException) {
            e.printStackTrace()
        }

        return null
    }

    override fun previousStatuses(maxId: Long, count: Int): List<Status>? {
        try {
            val result = TwitterUtils.getTwitterInstance().search(Query(buildQuery()).count(count).maxId(maxId - 1L).resultType(mResultType))
            if (mIsNeedLookUp) {
                val ids = result.tweets.map { t -> t.id }
                return TwitterUtils.getTwitterInstance().lookup(*ids.toLongArray());
            }
            return result.tweets
        } catch (e: TwitterException) {
            e.printStackTrace()
        }

        return null
    }

    override fun getTimelineName(): String {
        return "ツイート"
    }

    override fun search(query: String) {
        mQuery = query
        stopTask()
        mAdapter.clear()
        initTimeline()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(C.QUERY, mQuery)
        if (mOptionQuery != null) {
            outState.putString(C.OPTION_QUERY, mOptionQuery)
        }
        super.onSaveInstanceState(outState)
    }

    fun buildQuery(): String {
        if (mOptionQuery == null) {
            return mQuery
        }

        return mQuery + " " + mOptionQuery
    }
}
