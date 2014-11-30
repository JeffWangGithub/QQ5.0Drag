package com.jeff.qq;

import java.util.Random;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jeff.qq.ui.DragLayou;
import com.jeff.qq.ui.DragLayou.OnDragLayoutListener;
import com.jeff.qq.utils.Cheeses;
import com.jeff.qq.utils.Utils;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        
        initView();       
    }

	private void initView() {
		
		final ListView mLeftList = (ListView) findViewById(R.id.lv_menu_list);
        
        mLeftList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Cheeses.sCheeseStrings){
        	
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView tv = new TextView(MainActivity.this);
				tv.setText(Cheeses.sCheeseStrings[position]);
				tv.setTextColor(Color.WHITE);
				tv.setTextSize(24);
				return tv;
			}
        });
        
        final Random random = new Random();
        
        ListView mContentList = (ListView) findViewById(R.id.lv_content);
        mContentList.setAdapter(new ArrayAdapter<String>(MainActivity.this,  android.R.layout.simple_list_item_1,Cheeses.NAMES));
        
        DragLayou mDragLayout = (DragLayou) findViewById(R.id.dl);
        mDragLayout.setListener(new OnDragLayoutListener() {
			
			@Override
			public void onOpened() {
				Utils.showToast(getApplicationContext(), "Open");
				mLeftList.smoothScrollToPosition(random.nextInt(50));
				
			}
			
			@Override
			public void onDraging(float percent) {
				Utils.showToast(getApplicationContext(), "Close");
			}
			
			@Override
			public void onClosed() {
				
			}
		});
        
	}

}
