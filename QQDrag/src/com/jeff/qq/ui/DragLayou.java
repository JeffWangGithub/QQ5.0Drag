package com.jeff.qq.ui;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.nineoldandroids.view.ViewHelper;

public class DragLayou extends FrameLayout {
	/**
	 * ViewDragHelper使用方式
	 * 1，使用工程create对象，并传递回调对象
	 * 2，处理回调的方法
	 * 3，在onInterceptTouchEvent方法中，判断是否应该拦截
	 * 4，在onTouchEvent方法，处理Drag,mDragHelper.processTouchEvent(ev)
	 */
	
	private static final String TAG = "DragLayou";

	private ViewDragHelper mDragHelper; //View拖动帮助类
	private View mLeftContent;
	private View mMainContent;
	private int mHeight;
	private int mWidth;
	private int mDragRange;  //可以进行拖动的范围
	private int mMainLeft; //mMainContent的left 坐标
	private Status mStatus = Status.Close; //当前拖拽的状态，

	private DragCallBack mCallBack;

	public DragLayou(Context context) {
		this(context,null);
	}

	public DragLayou(Context context, AttributeSet attrs) {
		this(context,attrs,0);
	}


	public DragLayou(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		mCallBack = new DragCallBack();
		mDragHelper = ViewDragHelper.create(this, 1.0f, mCallBack);
		
	}
	
	//当布局文件被填充完成后会被调用
	@Override
	protected void onFinishInflate() {
		mLeftContent = getChildAt(0);
		mMainContent = getChildAt(1);
	}
	
	//当前View的大小改变时，被调用
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mWidth = getMeasuredWidth();//当前控件的测量宽度
		mHeight = getMeasuredHeight();
		//可以进行拖动的范围
		mDragRange = (int) (mWidth * 0.6f);
	}

	class DragCallBack extends ViewDragHelper.Callback{

		//1，决定当前的view是否是可以拖动的view，返回true表示是可以被拖动的，false表示不能被拖动
		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			//判断当前是否是需要进行拖动的控件
			return child == mMainContent ;
		}

		//决定了View位置改变时，希望发生的其他事情（此时移动已经发生）；或者在这里进行最终位置修正。
		//高频实时的调用，在这里设置各个View的属性
		@Override
		public void onViewPositionChanged(View changedView, int left, int top,
				int dx, int dy) {
			if(mMainContent == changedView){
				mMainLeft = left;//mMainContent的left
			}else {
				//拖拽的如果是mLeftContent,而不是mMainContent
				mMainLeft += dx;
			}
			
			//进行范围的控制
			if(mMainLeft < 0){
				mMainLeft = 0;
			}else if(mMainLeft > mDragRange){
				mMainLeft = (int) mDragRange;//不能大于设定的滑动范围，
			}
			
			//执行伴随动画效果
			dispatchDragEvent(mMainLeft);
			
			if(mMainLeft == 0){
				mStatus = Status.Close;		
				if(mListener != null){
					mListener.onClosed();
				}
			}else if(mMainLeft == mDragRange){
				mStatus = Status.Open;
				if(mListener != null){
					mListener.onOpened();
				}
			}else {
				mStatus = Status.Draging;
			}
		}
		//当前capturedChild被拖动的时候
		@Override
		public void onViewCaptured(View capturedChild, int activePointerId) {
			super.onViewCaptured(capturedChild, activePointerId);
		}

		// 设置可以进行拖拽的范围
		@Override
		public int getViewHorizontalDragRange(View child) {
			return mDragRange;
		}
		//进行建议值的修正。（实际移动并未发生。left是一个建议值）
		@Override
		public int clampViewPositionHorizontal(View child, int left, int dx) {
//			Log.d(TAG, "left:"+left+",dx:"+dx);
			if(mMainLeft + dx < 0){
				return 0;
			}else if(mMainLeft + dx > mDragRange){
				return (int) mDragRange;
			}
			return left;
		}

		//拖动被释放的时候，希望发生什么事情。比如打开或者恢复
		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
//			Log.d(TAG, "xvel:"+xvel);
			
			if(mMainLeft < mDragRange*0.5f){
				//释放时，距离左边小于mDragRange的一半
				close(true);
			}else if( mMainLeft>=mDragRange*0.5f){
				open(true);
			}
		}
	}
	
	
	/**
	 * 拖动状态改变的回调接口
	 * @author Jeff
	 */
	public interface OnDragLayoutListener{
		void onClosed();
		void onOpened();
		/**
		 * 正在拖动时调用，
		 * @param percent 拖动完成的百分比
		 */
		void onDraging(float percent);
	}
	
	OnDragLayoutListener mListener = null;

	public OnDragLayoutListener getmListener() {
		return mListener;
	}

	/**设置回调
	 * @param mListener
	 */
	public void setListener(OnDragLayoutListener mListener) {
		this.mListener = mListener;
	}

	/**
	 * 计算拖拽的百分比，并执行伴随动画
	 * @param mMainLeft
	 * 每次更新都会调用
	 * 根据当前执行的位置计算百分比percent
	 */
	private void dispatchDragEvent(int left) {
		float percent = left/(float)mDragRange; //当前drag的百分比
		Log.d(TAG, "percent:"+percent);
		//伴随动画：
		//mLeftContent的伴随动画
		//#1,设置缩放，
		//mLeftContent从一半到全部
		ViewHelper.setScaleX(mLeftContent, 0.5f+0.5f*percent);
		ViewHelper.setScaleY(mLeftContent, 0.5f + 0.5f*percent);
		//#2，设置平移，从-Width/2到0
		ViewHelper.setTranslationX(mLeftContent, -mWidth/2*(1-percent));
		//#3,设置透明度
		ViewHelper.setAlpha(mLeftContent, 0.5f+0.5f*percent);
		
		//主面板的伴随动画
		//mMainContent从全部倒一半
		float convert = 1 - percent*0.2f;
		ViewHelper.setScaleX(mMainContent, convert);
		ViewHelper.setScaleY(mMainContent, convert);
		
		if(mListener != null){
			mListener.onDraging(percent);
		}
	}
	

	/**
	 * 打开mMainContent的方法
	 * @param isSmooth 是否执行平滑的动画
	 */
	private void open(boolean isSmooth) {
		if(isSmooth){
			smoothSliding(mDragRange,0);
		}else{
			mMainContent.layout(mDragRange, 0, mDragRange+mWidth, mHeight);
		}
	}

	/**
	 * 平滑的移动,执行平滑的动画
	 */
	private void smoothSliding(int finalLeft, int finalTop) {
		//平滑的滑动到目标位置,返回true表示，仍未执行完毕。false表示执行完毕。
		//此方法返回true时，必须要调用continueSettling(boolean )方法
		// A.执行平滑动画（引发动画的开始），返回true代表有未完成的动画, 需要继续执行
		boolean continueSettle = mDragHelper.smoothSlideViewTo(mMainContent, finalLeft, finalTop);		
		if(continueSettle){
			//需要传递根ViewGroup，因为需要重新绘制整个界面
			ViewCompat.postInvalidateOnAnimation(this);//重新绘制当前的View				
		}
	}
	
	/**
	 * 关闭mMainContent的方法
	 */
	private void close(boolean isSmooth) {
		if(isSmooth){
			smoothSliding(0, 0);
		}else{
			mMainContent.layout(0, 0,mWidth, mHeight);
		}
	}
	
	//更新动画，或者该变子View的value时，此方法会被调用。每一祯变化都会调用。高频调用
	@Override
	public void computeScroll() {
		// B. 高频率调用(每帧绘制之前都会调用)
		// 决定是否有下一个变动等待执行
		if(mDragHelper.continueSettling(true)){//在computeScroll中调用continueSttling方法，参数要制定成true
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		//将事件传递给ViewDragHelper，并调用shouldInterceptTouchEvent，判断是否拦截
		return mDragHelper.shouldInterceptTouchEvent(ev);
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mDragHelper.processTouchEvent(event);
		return true;
	}
	
	/**
	 * 枚举类，表示当前拖拽的状态
	 * @author Jeff
	 */
	public static enum Status{
		Close, Open,Draging
	}
	
}
