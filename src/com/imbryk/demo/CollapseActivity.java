package com.imbryk.demo;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.imbryk.text.NumberSpan;

public class CollapseActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_collapse);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		initViews();
	}

	private void initViews() {
		TextView tv;

		tv = (TextView) findViewById(R.id.collapse_text_1);
		String txt = getString(R.string.ipsum_with_links);
		tv.setText(Html.fromHtml(txt));

		NetworkImageView image = (NetworkImageView) findViewById(R.id.collapse_image);
		String url = "http://placekitten.com/600/400";
		image.setImageUrl(url, App.getImageLoader());

		tv = (TextView) findViewById(R.id.collapse_text_3);
		String[] ipsum = getResources().getStringArray(R.array.ipsum_array);

		CharSequence spannableText = null;
		int indent = getResources().getDimensionPixelSize(R.dimen.list_indent);
		for (int i = 0; i < ipsum.length; i++) {
			txt = ipsum[i];
			if (i < ipsum.length - 1) {
				txt = txt + "\n";
			}

			Spannable spannable = new SpannableString(txt);
			NumberSpan numberSpan = new NumberSpan(tv, i, indent, 1.2f,0xffdd1111);
			spannable.setSpan(numberSpan, 0, txt.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			if (spannableText == null) {
				spannableText = spannable;
			} else {
				spannableText = TextUtils.concat(spannableText, spannable);
			}
		}
		tv.setText(spannableText);

	}
}
