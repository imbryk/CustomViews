package com.imbryk.demo;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

public class DemoPagerAdapter extends PagerAdapter {


	private int mCnt = 4;
	private String mImgUrlString = "http://placekitten.com/";
	private final LayoutInflater mInflater;

	public DemoPagerAdapter(Context context) {
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}



	@Override
	public Object instantiateItem(ViewGroup container, int position) {

		//Inflate the layout
		View view = mInflater.inflate(R.layout.pager_item, container, false); 
		NetworkImageView image = (NetworkImageView) view.findViewById(R.id.pager_item_image);
		TextView description = (TextView) view.findViewById(R.id.pager_item_text);
				
		
		int ipsumHeight = 300-position; //forces different images
		int ipsumWidth = 240+position; //forces different images
		image.setImageUrl(mImgUrlString+ipsumHeight+"/"+ipsumWidth, App.getImageLoader());
		
		description.setText("item "+position);
		
		container.addView(view);

		return view;

	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		final View view = (View) object;
		container.removeView(view);
	}

	@Override
	public int getCount() {
		return mCnt;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}
}
