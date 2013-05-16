package com.my.runfromball;


import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {

	private ScreenView showView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        showView = new ScreenView(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(showView);
    }


    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		showView.paintThread=false;
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode==KeyEvent.KEYCODE_MENU)
		{
			showView.stopGame();
		}
		return super.onKeyUp(keyCode, event);
	}


	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId())
		{
		case R.id.action_guide:
			AlertDialog.Builder dialog=new AlertDialog.Builder(this);
			dialog.setTitle("提示");
			dialog.setMessage(R.string.game_guide);
			dialog.setPositiveButton("确定", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
					showView.startGame();
				}
			});
			dialog.show();
			break;
		case R.id.top_five:
			Context ctx = this;       
	        SharedPreferences sp = ctx.getSharedPreferences("SP", 0);
	        int []top=new int[5];
	        top[0]=sp.getInt("BEST_SCORE", 0);
	        top[1]=sp.getInt("BEST_SCORE2", 0);
	        top[2]=sp.getInt("BEST_SCORE3", 0);
	        top[3]=sp.getInt("BEST_SCORE4", 0);
	        top[4]=sp.getInt("BEST_SCORE5", 0);
	        
	        String message="";
	        for(int i=0;i<5;i++)
	        {
	        	message+="第"+(i+1)+"名："+converToTimeLabel(top[i]);
	        	if(i<4)
	        	{
	        		message+="\n";
	        	}
	        }
	        AlertDialog.Builder dialog_topfive=new AlertDialog.Builder(this);
	        dialog_topfive.setTitle("提示");
	        dialog_topfive.setMessage(message);
	        dialog_topfive.setPositiveButton("确定", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
					showView.startGame();
				}
			});
	        dialog_topfive.show();
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	
	private String converToTimeLabel(int timeMs)
	{
		{
			int ms=(int) (timeMs%1000);
			int sec=(int) (timeMs/1000);
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
			return strMin+":"+strSec+":"+strMs;
		}
	}
	
    
}
