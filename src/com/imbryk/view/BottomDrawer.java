package com.imbryk.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewGroupCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.imbryk.demo.R;

public class BottomDrawer extends ViewGroup {

    /**
     * Minimum velocity that will be detected as a fling
     */
    private static final int MIN_FLING_VELOCITY = 400; // dips per second
    private static final int MOVE_OFFSET = 10; // dips


    public static enum State{
    	UNDEFINED(0), COLLAPSED(1), EXPANDED(2), HIDDEN(3);
    	
    	public int getValue(){
    		return value;
    	}
    	private final int value;
    	State(int value) { this.value= value; }
    	
    	public String toString() {
    		if( this == COLLAPSED)
    			return "COLLAPSED";
    		else if( this == EXPANDED)
    			return "EXPANDED";
    		else if( this == HIDDEN)
    			return "HIDDEN";
    		else
    			return "UNDEFINED";
    	}
    	public static State getState( int value ){
    		if( value == COLLAPSED.getValue()){
    			return COLLAPSED;
    		}else if( value == EXPANDED.getValue()){
    			return EXPANDED;
    		}else if( value == HIDDEN.getValue()){
    			return HIDDEN;
    		}else{
    			return UNDEFINED;
    		}
    	}
    }

			
	private ViewDragHelper mDragger;
	private DragHelperCallback mDragHelperCallback;
	
	private View mPanel;
	private View mContent;
	private View mFiller;
	
	private boolean mInLayout = false;
    private boolean mFirstLayout = true;

	private int mTotalDrag;
	private int mLastMotionX;
	private int mLastMotionY;
	private int mTouchSlop;
	private int mMinimumVelocity;

	public int mPanelBottom = 0;
	public int mPanelTop = 0;
	private int mPanelLeft = 0;

	private int mTotalHeight;
	private int mTopCollapsed;
	private int mTopExpanded;
	private int mTopHidden;

	private int mResIdDragHandle;
	private int mResIdToggleButton;

	private View mToggleButton;

	private View mDragHandle;

	private float mFriction = 1;
	private int mMoveOffset = MOVE_OFFSET;
	private int mOverscrollCollor = -1;
	private int mObscureCollor = -1;
	private boolean mUseObscureCollor;
	private boolean mUseOverscrollCollor;

	private float mPanelPosition;
	private int mGoal;
	private State mState = State.COLLAPSED;

	private State getState() {
		if( mPanel == null ){
			return State.UNDEFINED;
		}
		int draggerState = mDragger.getViewDragState();
		if( draggerState == ViewDragHelper.STATE_SETTLING ){
			if( mGoal == mTopExpanded )return State.EXPANDED;
			else if( mGoal == mTopCollapsed )return State.COLLAPSED;
			else if( mGoal == mTopHidden ) return State.HIDDEN;
			else return State.UNDEFINED;
		}else if(draggerState == ViewDragHelper.STATE_IDLE){
			int top = mPanel.getTop();
			if( top == mTopExpanded )return State.EXPANDED;
			else if( top == mTopCollapsed )return State.COLLAPSED;
			else if( top == mTopHidden ) return State.HIDDEN;
			else return State.UNDEFINED;
		}else{
			return mState;
		}
	}
	private void setState( State state){
		mDragger.abort();
		mState = state;
		if( state == State.UNDEFINED){
			return;
		}
		mGoal = state == State.EXPANDED ? mTopExpanded :
				( state == State.HIDDEN ? mTopHidden : mTopCollapsed );
		
		if( mPanel != null ){
	        final int dy = mGoal - mPanel.getTop();
	
	        if (dy != 0) {

				int top = mGoal;
				mPanelTop = top ;
				mPanelPosition = (float)(mTopCollapsed-top)/(float)(mTopCollapsed-mTopExpanded);
				if( mPanelPosition > 1 )mPanelPosition = 1;
				else if( mPanelPosition < 0 )mPanelPosition = 0;
				mPanelBottom  = top+mPanel.getMeasuredHeight();
				
	        	mPanel.offsetTopAndBottom(dy);
				ViewCompat.postInvalidateOnAnimation(BottomDrawer.this);
	        }
		}
	}
	private void setStateSmooth( State state, boolean useDragVelocity ){
		mState = state;
		if( state == State.UNDEFINED || mPanel == null ){
			return;
		}
		
		int goal = state == State.EXPANDED ? mTopExpanded :
				( state == State.HIDDEN ? mTopHidden : mTopCollapsed );
		boolean shouldSlide = false;
		
		if( mDragger.getViewDragState() == ViewDragHelper.STATE_SETTLING){
			shouldSlide = mGoal != goal;
		}else{
			shouldSlide = mPanel.getTop() != goal;
		}
		mGoal = goal;
		if( shouldSlide ){
			boolean result = useDragVelocity ? 
					mDragger.settleCapturedViewAt( mPanelLeft, goal) :
					mDragger.smoothSlideViewTo(mPanel, mPanelLeft, goal);
			if( result ){
				ViewCompat.postInvalidateOnAnimation(BottomDrawer.this);
			}
		}
	}
	
	public void expand(){
		setStateSmooth(State.EXPANDED, false);
	}
	public void showOrCollapse(){
		setStateSmooth(State.COLLAPSED, false);
	}
	public void collapse(){
		if( getState() == State.EXPANDED ){
			showOrCollapse();
		}
	}
	public void show(){
		if( getState() == State.HIDDEN ){
			showOrCollapse();
		}
	}
	public void hide(){
		setStateSmooth(State.HIDDEN, false);
	}
	
	public BottomDrawer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context,attrs);
	}

	public BottomDrawer(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public BottomDrawer(Context context) {
		this(context,null);
	}

	private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BottomDrawer);
        mResIdDragHandle = a.getResourceId(R.styleable.BottomDrawer_drag_handle, -1 );
        mResIdToggleButton = a.getResourceId(R.styleable.BottomDrawer_toggle_button, -1 );
        mFriction = a.getFloat(R.styleable.BottomDrawer_overscroll_friction, mFriction);
        
        int initStateVal = a.getInt(R.styleable.BottomDrawer_initial_state, State.COLLAPSED.getValue());
      
        mState = State.getState(initStateVal);
        if( mState == State.UNDEFINED ){
        	mState = State.COLLAPSED;
        }
        
        if( a.hasValue(R.styleable.BottomDrawer_overscroll_color) ){
        	mOverscrollCollor = a.getColor(R.styleable.BottomDrawer_overscroll_color, -1);
        	mUseOverscrollCollor = true;
        }else{
        	mUseOverscrollCollor = false;
        }
        if( a.hasValue(R.styleable.BottomDrawer_obscure_color) ){
        	mObscureCollor = a.getColor(R.styleable.BottomDrawer_obscure_color, -1);
        	mUseObscureCollor = true;
        }else{
        	mUseObscureCollor = false;
        }
        a.recycle();
		int n = getChildCount();
		if( n > 3 ){
			throw new RuntimeException("BottomHidingPanelLayout cannot have more than 3 child elements");
		}
		

		final ViewConfiguration configuration = ViewConfiguration.get(context);
		mTouchSlop = configuration.getScaledTouchSlop() / 2;
		mMinimumVelocity = configuration.getScaledMinimumFlingVelocity() * 3;
		
        final float density = getResources().getDisplayMetrics().density;
        final float minVel = MIN_FLING_VELOCITY * density;
        mMoveOffset = (int)(MOVE_OFFSET*density);
		
        mDragHelperCallback = new DragHelperCallback();
		mDragger = ViewDragHelper.create(this, 1.0f, mDragHelperCallback);
		mDragger.setMinVelocity(minVel);
        ViewGroupCompat.setMotionEventSplittingEnabled(this, true);
        
        setWillNotDraw(false);
	}


	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mDragHandle = findViewById(mResIdDragHandle);
		mToggleButton = findViewById(mResIdToggleButton);
		Log.v("DRAWER", "finish inflate "+mResIdDragHandle+", "+mResIdToggleButton);
		Log.v("DRAWER", "finish inflate "+mDragHandle+", "+mToggleButton);
		if( mToggleButton != null ){
			mToggleButton.setOnClickListener( new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.v("DRAWER", "toggle");
					if( getState() == State.EXPANDED){
						collapse();
					}else{
						expand();
					}
				}
			});
		}
	}
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mFirstLayout = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mFirstLayout = true;
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (h != oldh) {
            mFirstLayout = true;
        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

//        L("ON_MEASURE");
    	
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        final int paddingRight = getPaddingRight();
        final int paddingBottom = getPaddingBottom();
        int layoutHeight;
        int layoutWidth;

        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("Width must have an exact value or MATCH_PARENT");
        } else if (heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("Height must have an exact value or MATCH_PARENT");
        }

        final int childCount = getChildCount();

        layoutHeight = heightSize - paddingTop - paddingBottom;
        layoutWidth = widthSize - paddingLeft- paddingRight;
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            if (i == 0) {

                final int contentWidthSpec = MeasureSpec.makeMeasureSpec(
                		layoutWidth, MeasureSpec.EXACTLY);
                final int contentHeightSpec = MeasureSpec.makeMeasureSpec(
                		layoutHeight, MeasureSpec.EXACTLY);
                child.measure(contentWidthSpec, contentHeightSpec);
            } else  if(i==1){
                final int drawerWidthSpec = MeasureSpec.makeMeasureSpec(
                		layoutWidth, MeasureSpec.EXACTLY);
                final int drawerHeightSpec = MeasureSpec.makeMeasureSpec(
                		layoutHeight, MeasureSpec.UNSPECIFIED);
                child.measure(drawerWidthSpec, drawerHeightSpec);
            } else if( i==2 ){
            	final int widthSpec = MeasureSpec.makeMeasureSpec(
            			layoutWidth, MeasureSpec.EXACTLY);
            	final int heightSpec = MeasureSpec.makeMeasureSpec(
            			layoutHeight, MeasureSpec.AT_MOST);
            	child.measure(widthSpec, heightSpec);
            	
            }
        }

        setMeasuredDimension(widthSize, heightSize);
    }
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mInLayout  = true;
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        final int paddingBottom = getPaddingBottom();
        mTotalHeight = b-t;
        final int childCount = getChildCount();
        for (int i = 0; i<childCount; i++ ){
            View child = getChildAt(i);


            if( i == 0 ){
                if (child.getVisibility() == GONE) {
                	mContent = null;
                    continue;
                }
            	mContent = child;
                final int childTop = paddingTop;
                final int childBottom = childTop + child.getMeasuredHeight();
                final int childLeft = paddingLeft;
                final int childRight = childLeft + child.getMeasuredWidth();
                child.layout(childLeft, childTop, childRight, childBottom);
            }else if( i==1 ){
                if (child.getVisibility() == GONE) {
                	mPanel = null;
                    continue;
                }
            	if( mPanel == null ){
            		changed = true;
            	}
            	mPanel = child; 
            	int height = child.getMeasuredHeight();
            	if (mFirstLayout) {
            		mPanelBottom = mTotalHeight-paddingBottom;
            		mPanelPosition = 1;
            	}
            	final int childBottom = mPanelBottom;
				final int childTop = childBottom-height;
            	final int childLeft = paddingLeft;
            	final int childRight = childLeft + child.getMeasuredWidth();
            	child.layout(childLeft, childTop, childRight, childBottom);
            	mPanelLeft = childLeft;
            	
            	if (mFirstLayout) {
                    final float density = getResources().getDisplayMetrics().density;
            		int handleHeight = mDragHandle!=null ? mDragHandle.getMeasuredHeight() : (int)(150*density);
            		mPanelBottom = mTotalHeight-paddingBottom;
            		mTopExpanded = mPanelBottom-height;
            		mTopCollapsed = mTopExpanded+height-handleHeight;
            		mTopHidden = mTopExpanded+height;
            	}
            	
        		mFirstLayout = false;
            }else if( i== 2){
                if (child.getVisibility() == GONE) {
                	mFiller = null;
                    continue;
                }
            	mFiller = child;
            	if( mFirstLayout ){
            		continue;
            	}
            	final int childTop = 0;
            	final int childBottom = childTop + child.getMeasuredHeight();
            	final int childLeft = paddingLeft;
            	final int childRight = childLeft + child.getMeasuredWidth();
            	child.layout(childLeft, childTop, childRight, childBottom);
            	
            }
		}
        mInLayout = false;
        if( changed ){
        	setState(mState);
        }
	}

	@Override
    public void requestLayout() {
        if (!mInLayout) {
            super.requestLayout();
        }
    }

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = MotionEventCompat.getActionMasked(ev);
		int pointerId = MotionEventCompat.getPointerId(ev, MotionEventCompat.getActionIndex(ev) );
		boolean interceptForDrag = false;
		if( mDragger.getViewDragState() == ViewDragHelper.STATE_IDLE ){
			interceptForDrag = mDragger.shouldInterceptTouchEvent(ev);
		}
        if( action == MotionEvent.ACTION_DOWN){

			mTotalDrag = 0;
			mLastMotionX = (int) ev.getRawX();
			mLastMotionY = (int) ev
					.getRawY();
        }else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
			mDragger.cancel();
			return false;
		}else if( action == MotionEvent.ACTION_MOVE ){
			if( mDragger.getViewDragState() == ViewDragHelper.STATE_IDLE ){
	            final float x = ev.getX();
	            final float y = ev.getY();
	            if( mDragger.isViewUnder(mPanel, (int)x, (int)y) ){
	
	    			int xi = (int) ev.getRawX();
	    			int yi = (int) ev.getRawY();
	    			int dx = xi - mLastMotionX;
	    			int dy = yi - mLastMotionY;
	    			int adx = Math.abs(dx);
	    			int ady = Math.abs(dy);
	    			mLastMotionX = xi;
	    			mLastMotionY = yi;
	    			if (adx >= ady) {
	    				//if direction is horizontal allow touch event propagation
	    				mTotalDrag = 0;
	    				return false;
	    			} else {
	    				//check how much of the actual vertical drag occured 
	    				mTotalDrag += ady;
	    			}
	
	    			if (mTotalDrag < mTouchSlop) {
	    				//propagate event if there was not enough to decide  
	    				return false;
	    			}
	    			if( dy != 0 ){
	    				mDragger.captureChildView(mPanel, pointerId);
	    				return true;
	    			}
	            }
			}
		}
		return interceptForDrag;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		mDragger.processTouchEvent(ev);
		return super.onTouchEvent(ev);
	}

	@Override
	protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        final int save = canvas.save(Canvas.CLIP_SAVE_FLAG);
        
        boolean result = false;
		if( child == mContent ){
			if( mPanel!=null ){
				int bottom = mDragHandle!=null ? mPanel.getTop()+mDragHandle.getMeasuredHeight() : mPanel.getBottom();
	    		Rect rect = new Rect(mContent.getLeft(), mContent.getTop(), mContent.getRight(), bottom);
	    		canvas.clipRect(rect);
			}
    		result = super.drawChild(canvas, child, drawingTime);
	    		

    		if( mUseObscureCollor ){
	            final int baseAlpha = (mObscureCollor & 0xff000000) >>> 24;
	            final int alpha = (int) (baseAlpha * mPanelPosition);
	            final int color = alpha << 24 | (mObscureCollor & 0xffffff);
	            if( alpha > 1 ){
	            	canvas.drawColor(color);
	            }
    		}
		}else if( child == mFiller ){
			if( mPanel != null ){
				canvas.translate(0, mPanel.getBottom());
			}
			result = super.drawChild(canvas, child, drawingTime);
		}else if( child == mPanel){
			result = super.drawChild(canvas, child, drawingTime);
			if( mUseOverscrollCollor ){
	    		Rect rect = new Rect(mContent.getLeft(), mPanel.getBottom(), mContent.getRight(), mContent.getBottom());
	    		canvas.clipRect(rect);
	    		canvas.drawColor(mOverscrollCollor);
			}
		}else{
			result = super.drawChild(canvas, child, drawingTime);
		}
        
        

        canvas.restoreToCount(save);
		return result;
	}


    @Override
    public void computeScroll() {
        if (mDragger.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

	private class DragHelperCallback extends ViewDragHelper.Callback {

		@Override
		public int clampViewPositionHorizontal(View child, int left, int dx) {
			return mPanelLeft;
		}
		@Override
		public int clampViewPositionVertical(View child, int top, int dy) {
			if( top < mTopExpanded ){
				if( dy <= 0 ){
					float dropMultiplier = 1-0.5f*mFriction;
					int height = child.getMeasuredHeight();
					if( height >= mTotalHeight ){
						return mTopExpanded;
					}else{
						int val = (int) ((mTotalHeight - height)*dropMultiplier);
						int d = mTopExpanded - top + dy;
						float prop = (float)d/(float)val;
						if( prop >= 1 ){
							setStateSmooth(State.COLLAPSED, false);
							return mTopExpanded;
						} else if( prop <= 0 ){
							prop = 0;
						} else {
							prop = (float) Math.sqrt(prop);
						}
						prop*=mFriction;
						return Math.max((int) (top - prop*(float)dy), 0);
					}
				}else{
					return top;
				}
			}else{
				return Math.min( mTopCollapsed, top );
			}
		}

		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			return child == mPanel;
		}
		@Override
		public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
			super.onViewPositionChanged(changedView, left, top, dx, dy);
			mPanelTop = top;
			mPanelPosition = (float)(mTopCollapsed-top)/(float)(mTopCollapsed-mTopExpanded);
			if( mPanelPosition > 1 )mPanelPosition = 1;
			else if( mPanelPosition < 0 )mPanelPosition = 0;
			mPanelBottom  = top + mPanel.getMeasuredHeight();
			ViewCompat.postInvalidateOnAnimation(BottomDrawer.this);
		}

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
        	if( Math.abs(yvel) > mMinimumVelocity ){
        		if( yvel > 0 ){
					setStateSmooth(State.COLLAPSED, true);
        		}else{
        			if( mPanelTop < mTopExpanded ){
        				setStateSmooth(State.EXPANDED, false);
        			}else{
						setStateSmooth(State.EXPANDED, true);
        			}
        		}
        	}else{
	        	int mid = (mTopExpanded + mTopCollapsed)/2;
	        	if( mPanelTop > mid ){
	        		setStateSmooth(State.COLLAPSED, false);
	        	}else{
					setStateSmooth(State.EXPANDED, true);
	        	}
        	}
        }
	}
	
	//*******************************************************************************************
	//*List view auto showing/hiding
	//*******************************************************************************************

	
	ListViewHelper mListViewHelper;
	
	int mScrollD = 0;

	int mScrollPosition = 0;
	
	/**
	 * Helper method for auto hiding, showing and collapsing the drawer if if the listview is moved
	 * 
	 * To use it simply pass the {@link OnScrollListener#onScroll(AbsListView, int, int, int)} 
	 * from the {@link OnScrollListener} 
	 * with all the parameters
	 *   
	 * @param view
	 * @param firstVisibleItem
	 * @param visibleItemCount
	 * @param totalItemCount
	 */
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if( mListViewHelper == null || !mListViewHelper.isHelperForList(view)){
			mListViewHelper = new ListViewHelper(view);
		}
		int d = mListViewHelper.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		
		mScrollD+=d;
		boolean isDragging = mDragger.getViewDragState() == ViewDragHelper.STATE_DRAGGING;
		
		if( mScrollD > mMoveOffset){
			mScrollD = mMoveOffset;
			if( !isDragging ) showOrCollapse();
		}else if( mScrollD < -mMoveOffset){
			mScrollD = -mMoveOffset;
			if( !isDragging ) hide();
		}
	}
    
}
