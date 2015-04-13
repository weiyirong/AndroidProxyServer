package com.cfun.proxy.Base;

import android.app.Activity;
import android.view.MotionEvent;

/**
 * Created by CFun on 2015/4/11.
 */
public class BaseActivity extends Activity
{
	boolean enableTouchEvent = true;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		if (enableTouchEvent)
			return super.dispatchTouchEvent(ev);
		else
			return true;
	}
	public void setEnableTouchEvent(boolean enableTouchEvent)
	{
		//设置是否允许Activity分发触摸事件，主要用在因Fragment在切换动画期间触摸而导致的崩溃
		this.enableTouchEvent = enableTouchEvent;
	}

	@Override
	public void finish()
	{
		setEnableTouchEvent(false);
		super.finish();
	}
}
