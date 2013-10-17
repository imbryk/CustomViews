package com.imbryk.view;

import android.util.SparseIntArray;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class ListViewHelper {
	
	SparseIntArray mHeights = new SparseIntArray();
	int mPrevFirstVisibleItem = 0;
	int mPrevVisibleItemCount = 0;
	int mPrevTotalItemCount = 0;
	int mScrollPosition = 0;
	int mTopItemsHeight = 0;
	
	private AbsListView mList;


	public int getScroll(){
		return mScrollPosition;
	}
	public boolean isHelperForList( AbsListView list ){
		return mList == list;
	}
	public ListViewHelper( AbsListView list ){
		mList = list;
	}
	
	

	/**
	 * Helper method for calculating scroll position and move offset
	 * 
	 * To use it simply pass the {@link OnScrollListener#onScroll(AbsListView, int, int, int)} 
	 * from the {@link OnScrollListener} 
	 * with all the parameters
	 *   
	 * @param view
	 * @param firstVisibleItem
	 * @param visibleItemCount
	 * @param totalItemCount
	 * 
	 * @return current move offset  
	 */
	public int onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if( visibleItemCount == 0 ){
			return 0;
		}
		if (mPrevFirstVisibleItem != firstVisibleItem || mPrevTotalItemCount != totalItemCount
				|| mPrevVisibleItemCount != visibleItemCount) {
			if( firstVisibleItem == 0 ){
				mHeights = new SparseIntArray(totalItemCount);
				mTopItemsHeight = 0;
			}
			for( int i = 0; i < visibleItemCount ; i++ ){
				int id = i+firstVisibleItem;
				View v = view.getChildAt(i);
				mHeights.put(id, v.getMeasuredHeight());
			}
		}
		if( mPrevFirstVisibleItem != firstVisibleItem ){
			if( firstVisibleItem > mPrevFirstVisibleItem){
				int justHiddenItemsCount = firstVisibleItem - mPrevFirstVisibleItem;
				int justHiddenItemsHeight = 0;
				for( int i = 0; i < justHiddenItemsCount; i++){
					int id = i+mPrevFirstVisibleItem;
					justHiddenItemsHeight += mHeights.get(id);
				}
				mTopItemsHeight+=justHiddenItemsHeight;
			}else if(firstVisibleItem!=0){
				int justDisplayedItemsCount = mPrevFirstVisibleItem-firstVisibleItem;
				int justDisplayedItemsHeight = 0;
				for( int i = 0; i < justDisplayedItemsCount; i++){
					int id = i+firstVisibleItem;
					justDisplayedItemsHeight += mHeights.get(id);
				}
				mTopItemsHeight-=justDisplayedItemsHeight;
			}
		}
		View v = view.getChildAt(0);
		int prevScroll = mScrollPosition;
		mScrollPosition = v.getTop() - mTopItemsHeight;
		
		int delta = mScrollPosition - prevScroll;
		
		mPrevFirstVisibleItem = firstVisibleItem;
		mPrevTotalItemCount = totalItemCount;
		mPrevVisibleItemCount = visibleItemCount;
		
		return delta;
	}
}
