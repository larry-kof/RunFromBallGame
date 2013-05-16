package com.my.runfromball;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import com.dot.Dot;
import com.dot.DotFactory;
import com.dot.LittleDot;
import com.fireview.SoundPlay;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;

public class ScreenView extends SurfaceView implements SurfaceHolder.Callback {

	public static final short DIRECTION_UP = 19;
	public static final short DIRECTION_DOWN = 20;
	public static final short DIRECTION_LEFT = 21;
	public static final short DIRECTION_RIGHT = 22;
	public static final short DIRECTION_NONE = 0;
	
	public static final int ID_SOUND_UP = 0;
	public static final int ID_SOUND_BLOW = 1;
	public static final int ID_SOUND_MULTIPLE = 2;
	
	private static final int SHOW_DIALOG=1;
	private static final int RESTART=2;

	private int screen_width;
	private int screen_height;
	private Activity activity = null;
	private Bitmap screen_bitmap = null;
	private Bitmap unit_bitmap;
	private Bitmap dir_person[][] = new Bitmap[4][4];
	private Paint mPaint = new Paint();
	private Paint timePaint =new Paint();
	private Paint circlePaint =new Paint();
	private Paint bestScorePaint =new Paint();
	private Bitmap person = null;
	private int person_x = 0, person_y = 0;
	private String timeLabel="mm:ss:msms";
	private String bestScore="00:00:000";
	private int bestScoreMs=0;
	private long totalTimeMs=0;

	private int moveDirection = 0; // 移动方向
	private int cur_dirction = DIRECTION_DOWN; // 目前面向
	private int move_p = 0; // 移动动画
	public boolean paintThread = true;
	private int circleRadius ;
	private int addNewCircleTime=3000/50;
	private int addNewCircleTimeCount=addNewCircleTime;
	
	
	
	final static int TIME = 5; // 圈数

	/**画面中的烟花数*/
	private Vector<Dot> lList = new Vector<Dot>();

	LittleDot[] ld = new LittleDot[200];
	private DotFactory df = null;
	
	private boolean fireworked=false;
	public static SoundPlay soundPlay;
	
	private class CircleMove{
		public double last_center_pos_x=0;
		public double last_center_pos_y=0;
		public double x_speed;
		public double y_speed;
		public double g;
		public long time_ms;
	};
	
	
	public static void initSound(Context context) {
		soundPlay = new SoundPlay();
		soundPlay.initSounds(context);
		soundPlay.loadSfx(context, R.raw.up, ID_SOUND_UP);
		soundPlay.loadSfx(context, R.raw.blow, ID_SOUND_BLOW);
		soundPlay.loadSfx(context, R.raw.multiple, ID_SOUND_MULTIPLE);
	}
	
	public void stopGame()
	{
		addNewCircleTimeCount=addNewCircleTime;
		person_x = screen_width / 2;
		person_y = (int)(screen_height - 48*1.5);
		circles.removeAll(circles);
		totalTimeMs=0;
		paintThread=false;
		fireworked=false;
		lList.removeAllElements();
		
	}
	
	public void startGame()
	{
		paintThread=true;
		new Thread(new Repaint()).start();
		new MyThread().start();
	}
	
	private void setTopfive()
	{
		Context ctx = ScreenView.this.getContext();       
        SharedPreferences sp = ctx.getSharedPreferences("SP", 0);
        int []top=new int[4];
        top[0]=sp.getInt("BEST_SCORE2", 0);
        top[1]=sp.getInt("BEST_SCORE3", 0);
        top[2]=sp.getInt("BEST_SCORE4", 0);
        top[3]=sp.getInt("BEST_SCORE5", 0);
        
        int i;
        for(i=0;i<4;i++)
        {
        	if(totalTimeMs>top[i])
        	{
        		break;
        	}
        }
        if(i!=4)
        {
	        Editor editor = sp.edit();
	        editor.putInt("BEST_SCORE"+(i+2), (int)totalTimeMs);
	        editor.commit();
        }
	}
	
	private Handler handler =new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what)
			{
			case SHOW_DIALOG:
				Toast.makeText(ScreenView.this.getContext(), "你一共坚持了"+timeLabel, Toast.LENGTH_SHORT).show();
				setTopfive();
				if(bestScoreMs<totalTimeMs)
				{
					bestScoreMs=(int) totalTimeMs;
					setBestScoreStr();
					Context ctx = ScreenView.this.getContext();       
			        SharedPreferences sp = ctx.getSharedPreferences("SP", 0);
			        //存入数据
			        Editor editor = sp.edit();
			        editor.putInt("BEST_SCORE", bestScoreMs);
			        editor.commit();
				}
				break;
			case RESTART:
				stopGame();
				startGame();
				break;
			}
		}
		
	};
	

	private SurfaceHolder surfaceHolder;

	private ArrayList<CircleMove> circles=new ArrayList<ScreenView.CircleMove>();
	public ScreenView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		activity = (Activity) context;
		screen_width = activity.getWindowManager().getDefaultDisplay()
				.getWidth();
		screen_height = activity.getWindowManager().getDefaultDisplay()
				.getHeight();
		
		df = new DotFactory();
		new MyThread().start();
		initSound(context);
		
		person_x = screen_width / 2;
		person_y = (int)(screen_height - 48*1.5);
		

		unit_bitmap = new BitmapDrawable(getContext().getResources()
				.openRawResource(R.drawable.m0)).getBitmap();
		
		circleRadius = screen_height/20;

		surfaceHolder = this.getHolder();
		surfaceHolder.addCallback(this);
		createBitmap();
		setFocusable(true);

		setLongClickable(true);
		
		circlePaint.setColor(Color.BLACK);
		circlePaint.setStyle(Paint.Style.STROKE);
		circlePaint.setAntiAlias(true);
		
		timePaint.setColor(Color.GRAY);
		timePaint.setAntiAlias(true);
		timePaint.setTextSize(55);
		
		bestScorePaint.setColor(Color.BLACK);
		bestScorePaint.setTextSize(15);
		bestScorePaint.setAntiAlias(true);
		
		SharedPreferences sp = context.getSharedPreferences("SP", 0);
		bestScoreMs=sp.getInt("BEST_SCORE", 0);
		setBestScoreStr();
		
	}

	
	private void setBestScoreStr()
	{
		int ms=(int) (bestScoreMs%1000);
		int sec=(int) (bestScoreMs/1000);
		int min=0;
		if(sec>60)
		{
			min=sec/60;
			sec=sec%60;
		}
		String strMs="";
		if(ms<10)
		{
			strMs="00"+ms;
		}
		else if(ms<100)
		{
			strMs="0"+ms;
		}
		else strMs=""+ms;
		String strSec="";
		if(sec<10)
		{
			strSec="0"+sec;
		}
		else strSec=""+sec;
		
		String strMin="";
		if(min<10)
		{
			strMin="0"+min;
		}
		else strMin=""+min;
		bestScore=strMin+":"+strSec+":"+strMs;
	}
	private void createBitmap() {
		screen_bitmap = Bitmap.createBitmap(screen_width, screen_height,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(screen_bitmap);
		canvas.drawColor(Color.WHITE);
		int row = screen_height%32==0?screen_height / 32:screen_height / 32+1;
		int col = screen_width%32==0? screen_width / 32:screen_width/32+1;
		Paint p = new Paint();
		for (int r = 0; r < row; r++)
			for (int c = 0; c < col; c++) {
				canvas.drawBitmap(unit_bitmap, c * 32, r * 32, p);
			}
		person = new BitmapDrawable(getContext().getResources()
				.openRawResource(R.drawable.r01)).getBitmap();

		for (short i = 0; i < 4; i++) {
			for (short f = 0; f < 4; f++) {
				dir_person[i][f] = Bitmap.createBitmap(person, f * 32, i * 48,
						32, 48);
				Matrix matrix = new Matrix();
				matrix.postScale(1.5f, 1.5f);
				dir_person[i][f] = Bitmap.createBitmap(dir_person[i][f], 0, 0,
						dir_person[i][f].getWidth(), dir_person[i][f].getHeight(), matrix, true);

			}
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_POINTER_1_DOWN:
			if (event.getX() <= screen_width / 2) {
				moveDirection = DIRECTION_LEFT;
				cur_dirction = DIRECTION_LEFT;
			} else {
				moveDirection = DIRECTION_RIGHT;
				cur_dirction = DIRECTION_RIGHT;
			}
			break;
		case MotionEvent.ACTION_POINTER_1_UP:
			moveDirection = 0;
			move_p = 0;
			break;
		case MotionEvent.ACTION_DOWN:
			if (event.getX() <= screen_width / 2) {
				moveDirection = DIRECTION_LEFT;
				cur_dirction = DIRECTION_LEFT;
			} else {
				moveDirection = DIRECTION_RIGHT;
				cur_dirction = DIRECTION_RIGHT;
			}
			break;
		case MotionEvent.ACTION_UP:
			moveDirection = 0;
			move_p = 0;
			cur_dirction = DIRECTION_DOWN;
			break;

		}

		return super.onTouchEvent(event);
	}
	private long drawTimeMs=0;
	private class Repaint implements Runnable {

		public void run() {
			while (paintThread) {
				long sss=System.currentTimeMillis();
				Canvas canvas = surfaceHolder.lockCanvas(null);

				if (moveDirection == DIRECTION_UP) {
					person_y -= 5;
				} else if (moveDirection == DIRECTION_DOWN) {
					person_y += 5;
				} else if (moveDirection == DIRECTION_LEFT) {
					if(person_x>=5)
						person_x -= 5;
				} else if (moveDirection == DIRECTION_RIGHT) {
					if(person_x<=screen_width-32-5)
						person_x += 5;
				}
				if (moveDirection != 0) {
					move_p++;
					if (move_p == 4) {
						move_p = 0;
					}
				}

				if (screen_bitmap != null) {
					canvas.drawBitmap(screen_bitmap, 0, 0, mPaint);
					short sd = 0;
					if (cur_dirction == DIRECTION_UP) {
						sd = 3;
					} else if (cur_dirction == DIRECTION_LEFT) {
						sd = 1;
					} else if (cur_dirction == DIRECTION_RIGHT) {
						sd = 2;
					} else if (cur_dirction == DIRECTION_DOWN) {
						sd = 0;
					}
					canvas.drawBitmap(dir_person[sd][move_p], person_x,
							person_y, mPaint);
				}
				
				if(addNewCircleTimeCount>=addNewCircleTime)
				{
					addNewCircleTimeCount=0;
					CircleMove circle=new CircleMove();
					Random random=new Random();
					int randomInt=random.nextInt(300)+30;
					circle.x_speed=randomInt;
					circle.y_speed=0;
					circle.g=circles.size()*50+100;
					circle.last_center_pos_x=randomInt;
					circle.last_center_pos_y=circleRadius;
					circle.time_ms=0;
					circles.add(circle);
				}
				
				addNewCircleTimeCount++;
				for(int i=0;i<circles.size();i++)
				{
					CircleMove temp=circles.get(i);
					float cy;
					float cx;
					cx=(float)(temp.last_center_pos_x+temp.x_speed*drawTimeMs/1000);
					if(temp.y_speed>=0)
						cy=(float)(temp.last_center_pos_y+0.5*temp.g*temp.time_ms*temp.time_ms/1000000);
					else
						cy=(float) (screen_height-circleRadius-
								Math.abs(temp.y_speed)*temp.time_ms/1000+0.5*temp.g*temp.time_ms*temp.time_ms/1000000);
					
					
					if(cy+circleRadius>=screen_height)
					{
						cy=screen_height-circleRadius;
						temp.y_speed=0-temp.g*temp.time_ms/1000;
						temp.time_ms=0;
					}
					else if(cy-circleRadius<=0)
					{
						cy=circleRadius;
						temp.y_speed=0;
						temp.time_ms=0;
					}
					else if(cx+circleRadius>screen_width)
					{
						cx=screen_width-circleRadius;
						temp.last_center_pos_x=(double)cx;
						temp.x_speed=0-Math.abs(temp.x_speed);
					}
					else if(cx-circleRadius<=0)
					{
						cx=circleRadius;
						temp.last_center_pos_x=(double)(temp.last_center_pos_x+temp.x_speed*temp.time_ms/1000);
						temp.x_speed=Math.abs(temp.x_speed);
					}
					temp.last_center_pos_x=cx;
					canvas.drawCircle(cx, cy, circleRadius, circlePaint);
					temp.time_ms+=drawTimeMs;
					
					totalTimeMs+=drawTimeMs;
					if(collisionCheck(person_x,person_y,cx,cy)||
							collisionCheck(person_x+32,person_y,cx,cy))
					{
						paintThread=false;
						handler.sendEmptyMessage(SHOW_DIALOG);
						new Thread(){
							public void run()
							{
								try {
									Thread.sleep(3000);
									handler.sendEmptyMessage(RESTART);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}.start();
					}
					
				}
				
				int ms=(int) (totalTimeMs%1000);
				int sec=(int) (totalTimeMs/1000);
				int min=0;
				if(sec>60)
				{
					min=sec/60;
					sec=sec%60;
				}
				String strMs="";
				if(ms<10)
				{
					strMs="00"+ms;
				}
				else if(ms<100)
				{
					strMs="0"+ms;
				}
				else strMs=""+ms;
				String strSec="";
				if(sec<10)
				{
					strSec="0"+sec;
				}
				else strSec=""+sec;
				
				String strMin="";
				if(min<10)
				{
					strMin="0"+min;
				}
				else strMin=""+min;
				timeLabel=strMin+":"+strSec+":"+strMs;
				canvas.drawText(timeLabel, screen_width/2-timePaint.measureText(timeLabel)/2, 55, timePaint);
				canvas.drawText("最好成绩:"+bestScore, 0, 20,bestScorePaint);
				
				if(bestScoreMs<totalTimeMs&&fireworked==false&&bestScoreMs!=0)
					new Thread(){
						public void run()
						{
							overBestScore();
						}
					}.start();
				
				synchronized (lList) {
					for (int i = 0; i < lList.size(); i++) {
						lList.get(i).myPaint(canvas, lList);
					}
				}				
				
				surfaceHolder.unlockCanvasAndPost(canvas);
				drawTimeMs=(System.currentTimeMillis()-sss);
			}
		}
	}

	
	private void overBestScore()
	{
		if(bestScoreMs<totalTimeMs&&fireworked==false)
		{
			fireworked=true;
			for(int i=0;i<5;i++)
			{
				Dot dot = null;
				int rand = (int) (Math.random() * 99);
				Random random=new Random();
				dot = df.makeDot(this.getContext(), rand, random.nextInt(screen_width),
						random.nextInt(80)+screen_height/2-40);
				synchronized (lList) {
					lList.add(dot);
					soundPlay.play(ID_SOUND_UP, 0);
				}
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private boolean collisionCheck(float collision_x,float collision_y,float circle_center_x,
			float circle_center_y)
	{
		float distance=(float) Math.sqrt(Math.pow(collision_x-circle_center_x, 2)+
				Math.pow(collision_y-circle_center_y, 2));
		if(distance<(float)circleRadius)
			return true;
		return false;
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
		SharedPreferences sp = this.getContext().getSharedPreferences("SP", 0);
		int firstplay=sp.getInt("fisrtplay", 0);
		if(firstplay==0)
		{
			Editor editor = sp.edit();
	        editor.putInt("fisrtplay", 1);
	        editor.commit();
	        AlertDialog.Builder dialog=new AlertDialog.Builder(getContext());
			dialog.setTitle("提示");
			dialog.setMessage(R.string.game_guide);
			dialog.setPositiveButton("确定", new android.content.DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
					new Thread(new Repaint()).start();
				}
			});
			dialog.show();
		}
		else
		{
			new Thread(new Repaint()).start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		paintThread = false;
		circles.removeAll(circles);
	}
	
	
	
	class MyThread extends Thread {
		// 用于控制烟火在空中滞留的时间
		int times = 0;

		public void run() {
			Dot dot = null;
			while (paintThread) {

				try {
					Thread.sleep(100);
				} catch (Exception e) {
					System.out.println(e);
				}

				synchronized (lList) {
					// 防止画面的烟花个数多于50个
					while (lList.size() > 50) {
						System.out.println("当前数目超过50");
						for (int i = 0; i < 10; i++) {
							lList.remove(i);
						}
					}
				}

				for (int i = 0; i < lList.size(); i++) {
					dot = (Dot) lList.get(i);
					if (dot.state == 1 && !dot.whetherBlast()) {
						dot.rise();
					}
					// 如果是whetherBlast()返回的是true，那么就把该dot的state设置为2
					else if (dot.state == 1 && dot.state != 2) {
						dot.state = 2;
						soundPlay.play(ID_SOUND_BLOW, 0);
					} else if (dot.state == 3) {

					}
					// 规定，每个爆炸点最多是TIME圈，超过就会消失
					if (dot.circle >= TIME) {
						// 在空中滞留一秒才消失
						if (times >= 10) {
							dot.state = 4;
							times = 0;
						} else {
							times++;
						}
						// dot.state = 4;
					}
				}
			}
		}
	}

}
