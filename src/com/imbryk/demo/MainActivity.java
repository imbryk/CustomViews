package com.imbryk.demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.imbryk.view.BottomDrawer;

public class MainActivity extends Activity {


	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

	private void initViews() {
		final BottomDrawer layout = (BottomDrawer)findViewById(R.id.drawer_layout);
		
		
		
		String[] items = getResources().getStringArray(R.array.items);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
		ListView listview = (ListView)findViewById(R.id.list);
		listview.setAdapter(adapter);
		listview.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				layout.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		});
	}
}
