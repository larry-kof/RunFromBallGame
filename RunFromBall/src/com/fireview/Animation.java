package com.fireview;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

public class Animation {

	/** 上一帧播放时间 **/
	private long mLastPlayTime = 0;
	/** 播放当前帧的ID **/
	private int mPlayID = 0;
	/** 动画frame数量 **/
	private int mFrameCount = 0;
	/** 用于储存动画资源图片 **/
	private Bitmap[] mframeBitmap = null;
	/** 是否循环播放 **/
	private boolean mIsLoop = false;
	/** 播放结束 **/
	private boolean mIsend = false;
	/** 动画播放间隙时间 **/
	private static final int ANIM_TIME = 100;

	/**
	 * 构造函数
	 * 
	 * @param context
	 * @param frameBitmapID
	 * @param isloop
	 */
	public Animation(Context context, int[] frameBitmapID, boolean isloop) {
		mFrameCount = frameBitmapID.length;
		mframeBitmap = new Bitmap[mFrameCount];
		for (int i = 0; i < mFrameCount; i++) {
			mframeBitmap[i] = ReadBitMap(context, frameBitmapID[i]);
		}
		mIsLoop = isloop;
		new Thread(){
			public void run()
			{
				for (int i = 0; i < mFrameCount; i++) {
					float scale = 2f;
					
					/* 产生reSize后的Bitmap对象 */
					Matrix matrix = new Matrix();
					matrix.postScale(scale, scale);
					Bitmap temp = Bitmap.createBitmap(mframeBitmap[i], 0, 0, 
							mframeBitmap[i].getWidth(),
							mframeBitmap[i].getHeight(), matrix, true);
					mframeBitmap[i]=temp;
				}
			}
		}.start();
	}

	/**
	 * 构造函数
	 * 
	 * @param context
	 * @param frameBitmap
	 * @param isloop
	 */
	public Animation(Context context, Bitmap[] frameBitmap, boolean isloop) {
		mFrameCount = frameBitmap.length;
		mframeBitmap = frameBitmap;
		mIsLoop = isloop;
		
		for(int i=0;i<frameBitmap.length;i++)
		{
			double scale = 5;
			/* 计算出这次要缩小的比例 */
			float scaleWidth = (float) (mframeBitmap[i].getWidth() * scale);
			float scaleHeight = (float) (mframeBitmap[i].getHeight() * scale);
			/* 产生reSize后的Bitmap对象 */
			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleHeight);
			mframeBitmap[i] = Bitmap.createBitmap(mframeBitmap[i], 0, 0, 
					mframeBitmap[i].getWidth(),
					mframeBitmap[i].getHeight(), matrix, true);
			
			System.out.println("width="+mframeBitmap[i].getWidth());
		}
		
		
	}

	/**
	 * 绘制动画中的其中一帧
	 * 
	 * @param Canvas
	 * @param paint
	 * @param x
	 * @param y
	 * @param frameID
	 */
	public void DrawFrame(Canvas Canvas, Paint paint, int x, int y, int frameID) {
		Canvas.drawBitmap(mframeBitmap[frameID], x, y, paint);
	}

	/**
	 * 绘制动画
	 * 
	 * @param Canvas
	 * @param paint
	 * @param x
	 * @param y
	 */
	public void DrawAnimation(Canvas Canvas, Paint paint, int x, int y) {
		// 如果没有播放结束则继续播放
		if (!mIsend) {
			
			Canvas.drawBitmap(mframeBitmap[mPlayID],
					x - mframeBitmap[mPlayID].getWidth() / 2, y
							- mframeBitmap[mPlayID].getHeight() / 2, paint);
			long time = System.currentTimeMillis();
			if (time - mLastPlayTime > ANIM_TIME) {
				mPlayID++;
				mLastPlayTime = time;
				if (mPlayID >= mFrameCount) {
					// 标志动画播放结束
					mIsend = true;
					if (mIsLoop) {
						// 设置循环播放
						mIsend = false;
						mPlayID = 0;
					}
				}
			}
		}
	}

	/**是否连续动画*/
	public boolean ismIsend() {
		return mIsend;
	}

	public void setmIsend(boolean mIsend) {
		this.mIsend = mIsend;
	}

	/**
	 * 读取图片资源
	 * 
	 * @param context
	 * @param resId
	 * @return
	 */
	public Bitmap ReadBitMap(Context context, int resId) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		// 获取资源图片
		InputStream is = context.getResources().openRawResource(resId);
		return BitmapFactory.decodeStream(is, null, opt);
	}
}
