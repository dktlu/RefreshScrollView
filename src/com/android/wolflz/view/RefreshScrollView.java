package com.android.wolflz.view;

import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.wolflz.R;
import com.android.wolflz.util.DateUtil;

/**
 * 可下拉刷新的scrollView
 * 
 * @author wolflz  
 * @time 2016年6月25日 下午1:37:18
 */
public class RefreshScrollView extends ScrollView {

	private enum HeaderState {
		HEADER_NORMAL,
		HEADER_REFRESH_LOW,
		HEADER_REFRESH_HIGH,
		HEADER_REFRESHING
	}
	
	private final static int RATIO = 3;
	
	private LinearLayout innerLayout;
	private LinearLayout headerView;
	private ImageView arrowImageView;
	private ProgressBar progressBar;
	private TextView tipsTextview;
	private TextView lastUpdatedTextView;
	
	private int headContentHeight;
	
	private RotateAnimation animation;
	private RotateAnimation reverseAnimation;
	
	private int startY;
	private HeaderState mHeaderState;
	private boolean isBack;
	private boolean isRefreshable = false;
	private boolean isRecored, canReturn;
	
	private OnRefreshListener refreshListener;
	private Context mContext;
	public RefreshScrollView(Context context) {
		super(context);
		mContext = context;
		init(context);
	}

	public RefreshScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init(context);
	}

	/**
	 * 初始化控�?
	 * */
	private void init(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		headerView = (LinearLayout) inflater.inflate(R.layout.refresh_header, null);
		
		innerLayout = new LinearLayout(context);
		innerLayout.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		innerLayout.setOrientation(LinearLayout.VERTICAL);
		
		arrowImageView = (ImageView) headerView.findViewById(R.id.head_arrowImageView);
		progressBar = (ProgressBar) headerView.findViewById(R.id.head_progressBar);
		tipsTextview = (TextView) headerView.findViewById(R.id.head_tipsTextView);
		lastUpdatedTextView = (TextView) headerView.findViewById(R.id.head_lastUpdatedTextView);
		lastUpdatedTextView.setText(context.getString(R.string.refresh_update_time) + DateUtil.parseDateToString(new Date(), DateUtil.FORMAT_COMMON));
		
		measureView(headerView);
		
		headContentHeight = headerView.getMeasuredHeight();
		headerView.setPadding(0, -1 * headContentHeight, 0, 0);
		headerView.invalidate();

		innerLayout.addView(headerView);
		addView(innerLayout);
		
		animation = new RotateAnimation(0, -180,
			RotateAnimation.RELATIVE_TO_SELF, 0.5f,
			RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(250);
		animation.setFillAfter(true);

		reverseAnimation = new RotateAnimation(-180, 0,
			RotateAnimation.RELATIVE_TO_SELF, 0.5f,
			RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(250);
		reverseAnimation.setFillAfter(true);
		
		mHeaderState = HeaderState.HEADER_NORMAL;
		isRefreshable = false;
		canReturn = false;
	}
	
	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}
	
	private void ChangeHeaderByState(HeaderState state) {
		mHeaderState = state;
		switch(mHeaderState) {
		case HEADER_REFRESH_HIGH:
			arrowImageView.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.startAnimation(animation);
			tipsTextview.setText(mContext.getString(R.string.refresh_release_to_refresh));
			break;
		case HEADER_REFRESH_LOW:
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.VISIBLE);
			//是由HeaderState.HEADER_REFRESH_HIGH状�?转变来的
			if (isBack) {
				isBack = false;
				arrowImageView.clearAnimation();
				arrowImageView.startAnimation(reverseAnimation);
				tipsTextview.setText(mContext.getString(R.string.refresh_pulldown_to_refresh));
			} else {
				tipsTextview.setText(mContext.getString(R.string.refresh_pulldown_to_refresh));
			}
			break;
		case HEADER_REFRESHING:
			headerView.setPadding(0, 0, 0, 0);
			progressBar.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.GONE);
			tipsTextview.setText(mContext.getString(R.string.refresh_header_loading));
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			break;
		case HEADER_NORMAL:
			headerView.setPadding(0, -1 * headContentHeight, 0, 0);
			progressBar.setVisibility(View.GONE);
			arrowImageView.clearAnimation();
			arrowImageView.setImageResource(R.drawable.pulldown_arrow);
			tipsTextview.setText(mContext.getString(R.string.refresh_pulldown_to_refresh));
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setText(mContext.getString(R.string.refresh_update_time) 
					+ DateUtil.parseDateToString(new Date(), DateUtil.FORMAT_COMMON));
			break;
		default:
			break;
		}
	}

	
	/**
	 * touch事件
	 * */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (isRefreshable) {
			switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (getScrollY() == 0 && !isRecored) {
					isRecored = true;
					startY = (int) event.getY();
				}
				break;
			case MotionEvent.ACTION_UP:
				if (mHeaderState != HeaderState.HEADER_REFRESHING) {
					if (mHeaderState == HeaderState.HEADER_NORMAL) {
						// �?��都不�?
					}
					if (mHeaderState == HeaderState.HEADER_REFRESH_LOW) {
						ChangeHeaderByState(HeaderState.HEADER_NORMAL);
					}
					if (mHeaderState == HeaderState.HEADER_REFRESH_HIGH) {
						ChangeHeaderByState(HeaderState.HEADER_REFRESHING);
						onRefresh();
					}
				}
				isRecored = false;
				isBack = false;
				break;
			case MotionEvent.ACTION_MOVE:
				int tempY = (int) event.getY();
				if (!isRecored && getScrollY() == 0) {
					isRecored = true;
					startY = tempY;
				}

				if (mHeaderState != HeaderState.HEADER_REFRESHING && isRecored) {
					// 可以松手去刷新了
					if (mHeaderState == HeaderState.HEADER_REFRESH_HIGH) {
						canReturn = true;
						if (((tempY - startY) / RATIO < headContentHeight) && (tempY - startY) > 0) {
							ChangeHeaderByState(HeaderState.HEADER_REFRESH_LOW);
						} else if (tempY - startY <= 0) {
							ChangeHeaderByState(HeaderState.HEADER_NORMAL);
						} else {
						}
					}
					if (mHeaderState == HeaderState.HEADER_REFRESH_LOW) {
						canReturn = true;
						if ((tempY - startY) / RATIO >= headContentHeight) {
							isBack = true;
							ChangeHeaderByState(HeaderState.HEADER_REFRESH_HIGH);
						} else if (tempY - startY <= 0) {
							ChangeHeaderByState(HeaderState.HEADER_NORMAL);
						}
					}

					if (mHeaderState == HeaderState.HEADER_NORMAL) {
						if (tempY - startY > 0) {
							ChangeHeaderByState(HeaderState.HEADER_REFRESH_LOW);
						}
					}

					if (mHeaderState == HeaderState.HEADER_REFRESH_LOW) {
						headerView.setPadding(0, -1 * headContentHeight + (tempY - startY) / RATIO, 0, 0);
					}

					if (mHeaderState == HeaderState.HEADER_REFRESH_HIGH) {
						headerView.setPadding(0, (tempY - startY) / RATIO - headContentHeight, 0, 0);
					}
					if (canReturn) {
						canReturn = false;
						return true;
					}
				}
				break;
			}
		}
		return super.onTouchEvent(event);
	}

	public void setonRefreshListener(OnRefreshListener refreshListener) {
		this.refreshListener = refreshListener;
		isRefreshable = true;
	}

	public interface OnRefreshListener {
		public void onRefresh();
	}
	
	private void onRefresh() {
		if (refreshListener != null) {
			refreshListener.onRefresh();
		}
	}
	
	public void addChild(View child) {
		innerLayout.addView(child);
	}

	public void addChild(View child, int position) {
		innerLayout.addView(child, position);
	}
	
	public void onRefreshComplete() {
		ChangeHeaderByState(HeaderState.HEADER_NORMAL);
		invalidate();
		scrollTo(0, 0);
	}
}
