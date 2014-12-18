package com.crakac.ofuton.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.fragment.ConversationFragment;

public class ConversationActivity extends FinishableActionbarActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversation);
		//会話を表示するフラグメントを生成し，先頭のツイートをArgumentsとして投げる
        if(savedInstanceState == null) {
            ConversationFragment cf = new ConversationFragment();
            Bundle b = new Bundle();
            b.putSerializable(C.STATUS, getIntent().getSerializableExtra(C.STATUS));
            cf.setArguments(b);
            //レイアウト内にフラグメントを突っ込む
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment, cf).commit();
        }
	}
}
