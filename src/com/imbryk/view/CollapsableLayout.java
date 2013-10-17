package com.imbryk.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.TextView;

import com.imbryk.demo.R;

public class CollapsableLayout extends ViewGroup {

	private static final int DEFAULT_COLLAPSED_TOTAL_HEIGHT = 200; //dp
	private static final int[] DRAWABLE_STATE_COLLAPSED = { R.attr.state_collapsed };

	private ScrollerCompat mScroller;
	private int mExpandedContentHeight = 0;
	private int mCollapsedContentHeight = 0;
	private int mCollapsedTotalHeight;

	private boolean mAlignToLines = true;
	private boolean mIsCollapsed = true;
	private boolean mIsCollapsable = true;
	private int mMaxHeaderHeight = 0;
	private int mMaxFooterHeight = 0;

	private int mAnimationDuration = 500;

	private boolean mIsAnimating = false;
	private boolean mInLayout = false;

	private boolean mHasClickToggleResId = false;
	private int mClickToggleResId;

	private View mInnerToggle;
	private View mOuterToggle;
	
	private boolean mHasRequestedLayoutDelayed = false;

	private OnCollapseListener mListener;

	private OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			toggle();
		}
	};



	public void setOnCollapseListener(OnCollapseListener listener) {
		mListener = listener;
	}

	public boolean isCollapsed() {
		return mIsCollapsed;
	}

	public void toggle() {
		if (mScroller.getFinalY() == mExpandedContentHeight) {
			collapse();
		} else {
			expand();
		}
	}

	public void expand() {
		if( mIsCollapsable ){
			if (mScroller.getFinalY() != mExpandedContentHeight) {
				int curr = mScroller.getCurrY();
				mIsAnimating = true;
				int dy = mExpandedContentHeight - curr;
				int maxDy = mExpandedContentHeight - mCollapsedContentHeight;
	
				float prop = Math.abs((float) dy / (float) maxDy);
	
				mScroller.startScroll(0, curr, 0, dy, (int) (prop * mAnimationDuration));
			}
			mIsCollapsed = false;
			refreshDrawableState();
			requestLayoutDelayed();
		}
	}

	public void collapse() {
		if( mIsCollapsable ){
			if (mScroller.getFinalY() != mCollapsedContentHeight) {
				int curr = mScroller.getCurrY();
				mIsAnimating = true;
				int dy = mCollapsedContentHeight - curr;
				int maxDy = mExpandedContentHeight - mCollapsedContentHeight;
	
				float prop = Math.abs((float) dy / (float) maxDy);
	
				mScroller.startScroll(0, curr, 0, dy, (int) (prop * mAnimationDuration));
			}
			mIsCollapsed = true;
			refreshDrawableState();
			requestLayoutDelayed();
		}
	}

	private void requestLayoutDelayed() {
		if( mHasRequestedLayoutDelayed ){
			L("-\nrequestLayoutDelayed attempt rejected");
			return;
		}
		L("-\nrequestLayoutDelayed");
		mHasRequestedLayoutDelayed = true;
		postDelayed(new Runnable() {

			@Override
			public void run() {
				mHasRequestedLayoutDelayed = false;
				if (mInLayout) {
					L(" - postpone requestLayoutDelayed");
					requestLayoutDelayed();
				} else {
					L(" - run requestLayoutDelayed");
					if (mScroller.computeScrollOffset()) {
						int currY = mScroller.getCurrY();

						L(" -- setCurrY",currY);
						if (currY == mScroller.getFinalY() && mIsAnimating) {
							if (currY == mExpandedContentHeight) {
								mIsCollapsed = false;
								if (mListener != null) {
									mListener.onExpanded(CollapsableLayout.this);
								}
							} else if (currY == mCollapsedContentHeight) {
								mIsCollapsed = true;
								if (mListener != null) {
									mListener.onCollapsed(CollapsableLayout.this);
								}
							}
							mIsAnimating = false;
							refreshDrawableState();
						}
						L(" -- requestLayout");
						requestLayout();
					} else if (mExpandedContentHeight == 0) {
						L(" -- requestLayout (expanded height = 0");
						requestLayout();
					}
				}
			}
		}, 10);
	}

	public CollapsableLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	public CollapsableLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CollapsableLayout(Context context) {
		this(context, null);
	}

	private void init(Context context, AttributeSet attrs) {
		mScroller = ScrollerCompat.create(context);

		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CollapsableLayout);
		
		boolean isClickable = a.getBoolean(R.styleable.CollapsableLayout_android_clickable, true);
		
		mAlignToLines = a.getBoolean(R.styleable.CollapsableLayout_align_to_lines, true);
		mAnimationDuration = a.getInt(R.styleable.CollapsableLayout_animations_duration, mAnimationDuration);
		
		mHasClickToggleResId = a.hasValue(R.styleable.CollapsableLayout_toggle_button);
		if( mHasClickToggleResId ){
			mClickToggleResId = a.getResourceId(R.styleable.CollapsableLayout_toggle_button, -1);
		}
		
		mCollapsedTotalHeight = a.getDimensionPixelOffset(R.styleable.CollapsableLayout_collapsed_height, -1);
		if( mCollapsedTotalHeight == -1 ){
			WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
			DisplayMetrics outMetrics = new DisplayMetrics();
			wm.getDefaultDisplay().getMetrics(outMetrics );
			mCollapsedTotalHeight = (int) (DEFAULT_COLLAPSED_TOTAL_HEIGHT*outMetrics.density);
		}
		
		a.recycle();

		if (isClickable) {
			setOnClickListener(onClickListener);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		if (mOuterToggle != null) {
			mOuterToggle.setOnClickListener(null);
		}
		super.onDetachedFromWindow();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		if (mHasClickToggleResId && mInnerToggle == null) {
			mOuterToggle = findViewById(mClickToggleResId);
			ViewParent parent = this;
			while (mOuterToggle == null && parent != null && parent instanceof View) {
				mOuterToggle = ((View)parent).findViewById(mClickToggleResId);
				parent = parent.getParent();
			}
			if (mOuterToggle != null) {
				mOuterToggle.setOnClickListener(onClickListener);
			}
		}
	}

	@Override
	protected void onFinishInflate() {
		checkValidChildren();
		if( mHasClickToggleResId ){
			mInnerToggle = findViewById(mClickToggleResId);
			if (mInnerToggle != null) {
				mInnerToggle.setOnClickListener(onClickListener);
			}
		}
		super.onFinishInflate();
	}

	private void checkValidChildren() {
		final int childCount = getChildCount();
		boolean hasCollapsableView = false;
		for (int i = 0; i < childCount; i++) {
			final View child = getChildAt(i);

			if (child.getVisibility() == GONE) {
				continue;
			}
			if (isCollapasbleView(child)) {
				if (hasCollapsableView) {
					throw new IllegalStateException("only one child can have layout_gravity equal Gravity.NO_GRAVITY");
				}
				hasCollapsableView = true;
			} else if (!isHeaderView(child) && !isFooterView(child)) {
				throw new IllegalStateException("Child " + child + " at index " + i
						+ " does not have a valid layout_gravity - must be Gravity.TOP, "
						+ "Gravity.BOTTOM or Gravity.NO_GRAVITY");
			}
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
	}

	//*******************************************************************************************
	//* Measure
	//*******************************************************************************************
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		L("-\nonMeasure", mIsCollapsable ? "is collapsable":"not collapsable");
		final int paddingLeft = getPaddingLeft();
		final int paddingTop = getPaddingTop();
		final int paddingRight = getPaddingRight();
		final int paddingBottom = getPaddingBottom();

		int maxHeight = 0;
		int maxWidth = 0;
		final int childCount = getChildCount();

		mMaxFooterHeight = 0;
		mMaxHeaderHeight = 0;

		View collapsableView = null;
		boolean hasCollapsableView = false;
		for (int i = 0; i < childCount; i++) {
			final View child = getChildAt(i);

			final LayoutParams lp = (LayoutParams) child.getLayoutParams();
			boolean isHeaderView = isHeaderView(child);
			boolean isFooterView = isFooterView(child);
			
			if( !mIsCollapsable && (isHeaderView || isFooterView ) && !lp.alwaysVisible){
				child.setVisibility(GONE);
			}
			
			if (child.getVisibility() == GONE) {
				continue;
			}
			boolean isCollapsableView = isCollapasbleView(child);
			if (!isFooterView && !isHeaderView && !isCollapsableView) {
				throw new IllegalStateException("Child " + child + " at index " + i
						+ " does not have a valid layout_gravity - must be Gravity.TOP, "
						+ "Gravity.BOTTOM or Gravity.NO_GRAVITY");
			}

			if (isCollapsableView) {
				if (hasCollapsableView) {
					throw new IllegalStateException("only one child can have layout_gravity equal Gravity.NO_GRAVITY");
				}
				hasCollapsableView = true;
				collapsableView = child;
				//measure collapsable last, as we need footer and header heights
			} else {
				final int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, paddingLeft + paddingRight
						+ lp.leftMargin + lp.rightMargin, lp.width);
				final int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, paddingTop + paddingBottom
						+ lp.topMargin + lp.bottomMargin, lp.height);

				child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

				maxWidth = Math.max(maxWidth, child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
				if (isHeaderView) {
					mMaxHeaderHeight = Math.max(mMaxHeaderHeight, child.getMeasuredHeight() + lp.topMargin
							+ lp.bottomMargin);
				} else if (isFooterView) {
					mMaxFooterHeight = Math.max(mMaxFooterHeight, child.getMeasuredHeight() + lp.topMargin
							+ lp.bottomMargin);
				}
			}
		}

		if (hasCollapsableView) {

			L("measure collapsible view", !mIsAnimating);
			final LayoutParams lp = (LayoutParams) collapsableView.getLayoutParams();

			final int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, paddingLeft + paddingRight
					+ lp.leftMargin + lp.rightMargin, lp.width);
			final int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, paddingTop + paddingBottom
					+ lp.topMargin + lp.bottomMargin, mScroller.getCurrY());

			if (!mIsAnimating) {
				final int expandedContentHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, paddingTop
						+ paddingBottom + lp.topMargin + lp.bottomMargin, lp.height);
				collapsableView.measure(childWidthMeasureSpec, expandedContentHeightMeasureSpec);
				int newExpandedContentHeight = collapsableView.getMeasuredHeight();
				L("newExpandedContentHeight", newExpandedContentHeight);
				if( newExpandedContentHeight != mExpandedContentHeight ){
					mExpandedContentHeight = newExpandedContentHeight;
					L("mExpanded", mExpandedContentHeight);
					if( !mIsCollapsed ){
						if( mScroller.getCurrY() != mExpandedContentHeight ){
							mScroller.startScroll(0, mExpandedContentHeight, 0, 0, 0);
						}
					}
				}


				int expandedTotalHeight = mExpandedContentHeight + lp.topMargin + lp.bottomMargin
						+ paddingTop + paddingBottom + mMaxFooterHeight + mMaxHeaderHeight;
				
				if( expandedTotalHeight < mCollapsedTotalHeight ){
					mIsCollapsable = false;
					mIsCollapsed = false;
					if( mScroller.getCurrY() != mExpandedContentHeight ){
						mScroller.startScroll(0, mExpandedContentHeight, 0, 0, 0);
					}
				}
				
				if( mIsCollapsable ){
					int intendedCollapsedContentHeight = mCollapsedTotalHeight - mMaxFooterHeight - mMaxHeaderHeight
							- paddingTop - paddingBottom;
					if (intendedCollapsedContentHeight < 0) {
						intendedCollapsedContentHeight = 0;
					}
					L("intendedCollapsedContentHeight", intendedCollapsedContentHeight);
					final int collapsedContentHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, paddingTop
							+ paddingBottom + lp.topMargin + lp.bottomMargin, intendedCollapsedContentHeight);
					collapsableView.measure(childWidthMeasureSpec, collapsedContentHeightMeasureSpec);
					int newCollapsedContentHeight = collapsableView.getMeasuredHeight();
					L("newCollapsed", newCollapsedContentHeight);
					if (newCollapsedContentHeight != mCollapsedContentHeight && mAlignToLines) {
						newCollapsedContentHeight = correctCollapsedSize(newCollapsedContentHeight, collapsableView);
					}
					if (newCollapsedContentHeight != mCollapsedContentHeight) {
						mCollapsedContentHeight = newCollapsedContentHeight;
						L("mCollapsed", mCollapsedContentHeight);
						if( mIsCollapsed ){
							if( mScroller.getCurrY() != mCollapsedContentHeight ){
								mScroller.startScroll(0, mCollapsedContentHeight, 0, 0, 0);
							}
						}
					}
				}

			}

			collapsableView.measure(childWidthMeasureSpec, childHeightMeasureSpec);

			maxWidth = Math.max(maxWidth, collapsableView.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
			maxHeight = Math.max(maxHeight, collapsableView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
			
			
		}

		maxHeight += paddingTop + paddingBottom + mMaxFooterHeight + mMaxHeaderHeight;
		maxWidth += paddingLeft + paddingRight;

		setMeasuredDimension(maxWidth, maxHeight);
	}

	private int correctCollapsedSize(int startSize, View child) {
		TextView tv = findTextViewToCorrect(child);

		if (tv != null) {
			int h = tv.getMeasuredHeight();

			for (int i = 0; i < tv.getLineCount(); i++) {
				Rect bounds = new Rect();
				tv.getLineBounds(i, bounds);
				if (bounds.bottom > h) {
					return startSize - (h - bounds.top);
				}
			}
		}

		return startSize;
	}

	private TextView findTextViewToCorrect(View child) {
		if (child instanceof TextView) {
			TextView tv = (TextView) child;
			int h = tv.getMeasuredHeight();
			for (int i = 0; i < tv.getLineCount(); i++) {
				Rect bounds = new Rect();
				tv.getLineBounds(i, bounds);
				if (bounds.bottom > h) {
					return tv;
				}
			}
		} else if (child instanceof ViewGroup) {
			ViewGroup g = (ViewGroup) child;
			int n = g.getChildCount();
			for (int i = 0; i < n; i++) {
				TextView tv = findTextViewToCorrect(g.getChildAt(i));
				if (tv != null) {
					return tv;
				}
			}
		}
		return null;
	}

	//*******************************************************************************************
	//* Layout
	//*******************************************************************************************
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		L("-\nonLayout", changed, l, t, r, b);
		mInLayout = true;
		final int paddingLeft = getPaddingLeft();
		final int paddingRight = getPaddingRight();
		final int paddingTop = getPaddingTop();
		final int paddingBottom = getPaddingBottom();

		final int parentLeft = paddingLeft;
		final int parentRight = r - l - paddingRight;
		final int parentTop = paddingTop;
		final int parentBottom = b - t - paddingBottom;

		final int childCount = getChildCount();

		boolean hasCollapsableView = false;
		for (int i = 0; i < childCount; i++) {
			final View child = getChildAt(i);

			if (child.getVisibility() == GONE) {
				continue;
			}
			boolean isHeaderView = isHeaderView(child);
			boolean isFooterView = isFooterView(child);
			boolean isCollapsableView = isCollapasbleView(child);
			if (!isFooterView && !isHeaderView && !isCollapsableView) {
				throw new IllegalStateException("Child " + child + " at index " + i
						+ " does not have a valid layout_gravity - must be Gravity.TOP, "
						+ "Gravity.BOTTOM or Gravity.NO_GRAVITY");
			}

			if (isCollapsableView) {
				if (hasCollapsableView) {
					throw new IllegalStateException("only one child can have layout_gravity equal Gravity.NO_GRAVITY");
				}
				hasCollapsableView = true;
			}
			final LayoutParams lp = (LayoutParams) child.getLayoutParams();
			int horizontalGravity = getHorizontalGravity(child);
			final int width = child.getMeasuredWidth();
			final int height = child.getMeasuredHeight();

			int childLeft = 0;
			int childRight = 0;
			if (horizontalGravity == Gravity.RIGHT) {
				childRight = parentRight - lp.rightMargin;
				childLeft = childRight - width;
			} else if (horizontalGravity == Gravity.CENTER_HORIZONTAL) {
				childLeft = (parentLeft + lp.leftMargin + parentRight - lp.leftMargin - width) / 2;
				childRight = childLeft + width;
			} else {
				childLeft = parentLeft + lp.leftMargin;
				childRight = childLeft + width;
			}

			if (isCollapsableView) {
				final int childTop = parentTop + mMaxHeaderHeight + lp.topMargin;
				child.layout(childLeft, childTop, childRight, childTop + height);
			} else if (isHeaderView) {
				final int childTop = parentTop + lp.topMargin;
				child.layout(childLeft, childTop, childRight, childTop + height);
			} else if (isFooterView) {
				final int childBottom = parentBottom - lp.bottomMargin;
				child.layout(childLeft, childBottom - height, childRight, childBottom);
			}
		}
		mInLayout = false;
		requestLayoutDelayed();
	}

	//*******************************************************************************************
	//*Helper functions
	//*******************************************************************************************
	int getHorizontalGravity(View child) {
		final int gravity = ((LayoutParams) child.getLayoutParams()).gravity;
		final int absGravity = GravityCompat.getAbsoluteGravity(gravity, ViewCompat.getLayoutDirection(child));
		return absGravity & Gravity.HORIZONTAL_GRAVITY_MASK;
	}

	boolean isCollapasbleView(View child) {
		return ((LayoutParams) child.getLayoutParams()).gravity == Gravity.NO_GRAVITY;
	}

	boolean isFooterView(View child) {
		final int gravity = ((LayoutParams) child.getLayoutParams()).gravity;
		return (gravity & Gravity.BOTTOM) == Gravity.BOTTOM;
	}

	boolean isHeaderView(View child) {
		final int gravity = ((LayoutParams) child.getLayoutParams()).gravity;
		return (gravity & Gravity.TOP) == Gravity.TOP;
	}

	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (mIsCollapsed) {
			mergeDrawableStates(drawableState, DRAWABLE_STATE_COLLAPSED);
		}
		return drawableState;
	}

	//*******************************************************************************************
	//*Creating custom Layout Parameters
	//*******************************************************************************************
	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		return p instanceof LayoutParams && super.checkLayoutParams(p);
	}

	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
		return p instanceof LayoutParams ? new LayoutParams((LayoutParams) p)
				: p instanceof ViewGroup.MarginLayoutParams ? new LayoutParams((MarginLayoutParams) p)
						: new LayoutParams(p);
	}

	@Override
	public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new LayoutParams(getContext(), attrs);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	public static class LayoutParams extends ViewGroup.MarginLayoutParams {

		public int gravity = Gravity.NO_GRAVITY;
		public boolean alwaysVisible = false;

		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);

			final TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.CollapsableLayout_Layout);
			gravity = a.getInt(R.styleable.CollapsableLayout_Layout_android_layout_gravity, Gravity.NO_GRAVITY);
			alwaysVisible = a.getBoolean(R.styleable.CollapsableLayout_Layout_always_visible, false);
			a.recycle();
		}

		public LayoutParams(int width, int height) {
			super(width, height);
		}

//		public LayoutParams(int width, int height, int gravity) {
//			this(width, height);
//			this.gravity = gravity;
//		}
//		public LayoutParams(int width, int height, int gravity, boolean alwaysVisible) {
//			this(width, height);
//			this.gravity = gravity;
//			this.alwaysVisible = alwaysVisible;
//		}

		public LayoutParams(LayoutParams source) {
			super(source);
			this.gravity = source.gravity;
			this.alwaysVisible = source.alwaysVisible;
		}

		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}

		public LayoutParams(ViewGroup.MarginLayoutParams source) {
			super(source);
		}
	}

	//*******************************************************************************************
	//*Listener interface
	//*******************************************************************************************
	public interface OnCollapseListener {
		void onCollapsed(CollapsableLayout view);

		void onExpanded(CollapsableLayout view);

	}

	//*******************************************************************************************
	//*Log helper function
	//*******************************************************************************************
	public static void L(Object... msg) {
//		StringBuilder sb = new StringBuilder();
//		for (int i = 0; i < msg.length; i++) {
//			if (i > 0)
//				sb.append(" ");
//			sb.append(msg[i] != null ? msg[i].toString() : "null");
//		}
//		Log.v("TEST", sb.toString() );
	}
}
