package com.imbryk.demo;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.imbryk.view.BottomDrawer;

public class MainActivity extends ActionBarActivity {


	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

	private void initViews() {
		final BottomDrawer layout = (BottomDrawer)findViewById(R.id.drawer_layout);
		
		
		DemoListAdapter adapter = new DemoListAdapter( this );
		ListView listview = (ListView)findViewById(R.id.list);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
			}
		});
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
