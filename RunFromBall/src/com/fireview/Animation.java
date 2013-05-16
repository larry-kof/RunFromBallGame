package com.fireview;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

public class Animation {

	/** ��һ֡����ʱ�� **/
	private long mLastPlayTime = 0;
	/** ���ŵ�ǰ֡��ID **/
	private int mPlayID = 0;
	/** ����frame���� **/
	private int mFrameCount = 0;
	/** ���ڴ��涯����ԴͼƬ **/
	private Bitmap[] mframeBitmap = null;
	/** �Ƿ�ѭ������ **/
	private boolean mIsLoop = false;
	/** ���Ž��� **/
	private boolean mIsend = false;
	/** �������ż�϶ʱ�� **/
	private static final int ANIM_TIME = 100;

	/**
	 * ���캯��
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
					
					/* ����reSize���Bitmap���� */
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
	 * ���캯��
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
			/* ��������Ҫ��С�ı��� */
			float scaleWidth = (float) (mframeBitmap[i].getWidth() * scale);
			float scaleHeight = (float) (mframeBitmap[i].getHeight() * scale);
			/* ����reSize���Bitmap���� */
			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleHeight);
			mframeBitmap[i] = Bitmap.createBitmap(mframeBitmap[i], 0, 0, 
					mframeBitmap[i].getWidth(),
					mframeBitmap[i].getHeight(), matrix, true);
			
			System.out.println("width="+mframeBitmap[i].getWidth());
		}
		
		
	}

	/**
	 * ���ƶ����е�����һ֡
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
	 * ���ƶ���
	 * 
	 * @param Canvas
	 * @param paint
	 * @param x
	 * @param y
	 */
	public void DrawAnimation(Canvas Canvas, Paint paint, int x, int y) {
		// ���û�в��Ž������������
		if (!mIsend) {
			
			Canvas.drawBitmap(mframeBitmap[mPlayID],
					x - mframeBitmap[mPlayID].getWidth() / 2, y
							- mframeBitmap[mPlayID].getHeight() / 2, paint);
			long time = System.currentTimeMillis();
			if (time - mLastPlayTime > ANIM_TIME) {
				mPlayID++;
				mLastPlayTime = time;
				if (mPlayID >= mFrameCount) {
					// ��־�������Ž���
					mIsend = true;
					if (mIsLoop) {
						// ����ѭ������
						mIsend = false;
						mPlayID = 0;
					}
				}
			}
		}
	}

	/**�Ƿ���������*/
	public boolean ismIsend() {
		return mIsend;
	}

	public void setmIsend(boolean mIsend) {
		this.mIsend = mIsend;
	}

	/**
	 * ��ȡͼƬ��Դ
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
		// ��ȡ��ԴͼƬ
		InputStream is = context.getResources().openRawResource(resId);
		return BitmapFactory.decodeStream(is, null, opt);
	}
}
