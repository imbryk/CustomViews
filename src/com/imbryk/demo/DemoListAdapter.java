package com.imbryk.demo;

import com.android.volley.toolbox.NetworkImageView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DemoListAdapter extends BaseAdapter {
	
	private int mCnt = 20;
	private String mImgUrlString = "http://placekitten.com/";
	private LayoutInflater mInflater;
	public DemoListAdapter( Context context) {
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mCnt;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		StaticHolder holder = null;
		if( convertView == null ){
			convertView = mInflater.inflate(R.layout.list_item, parent, false);
			holder = new StaticHolder();
			holder.image = (NetworkImageView) convertView.findViewById(R.id.list_item_image);
			holder.description = (TextView) convertView.findViewById(R.id.list_item_text);
			convertView.setTag(holder);
		}else{
			holder = (StaticHolder) convertView.getTag();
		}

		int ipsumHeight = 200-position; //forces different images
		int ipsumWidth = 140+position; //forces different images
		holder.image.setImageUrl(mImgUrlString+ipsumHeight+"/"+ipsumWidth, App.getImageLoader());
		holder.description.setText("item "+position);
		return convertView;
	}
	private static class StaticHolder {
		NetworkImageView image;
		TextView description;
	}
}
