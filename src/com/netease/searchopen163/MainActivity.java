package com.netease.searchopen163;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements OnClickListener{
	public static final String TAG = "MainActivity";
	private static final int MSG_APPEND_RESULT = 0x0001;
	private static final int MSG_NO_RESULT = 0x0002;
	private static final int MSG_NETWORK_PROBLEM = 0x0003;
	
	private LayoutInflater mInflater;
    private InputMethodManager mInputCtrl;		// 控制软键盘的显示
	private String mPreKeyword = "";
	
	private List<ResultListItem> mResultList;	// 搜索结果
	private ListView mListView;					// 搜索结果ListView
	private BaseAdapter mAdapter;				// 搜索结果列表Adapter
	private EditText mEditText;					// 关键字输入框
	private ProgressBar mProgressBar;			// 进度条
	
	private DownloadHandler mHandler;			// 下载完一个网页后通过此Handler更新ListView
	private DownloadThread mDownloadThread;		// 下载搜索结果网页的线程
	private ImageWorker mImageWorker;			// 负责异步下载图片
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         
        mResultList = new ArrayList<ResultListItem>();
        
        mEditText = (EditText)findViewById(R.id.edit_keyword);
        mProgressBar = (ProgressBar)findViewById(R.id.progress_bar);
        
        mAdapter = new ResultListAdapter();
        mListView = (ListView)findViewById(R.id.list_search_result);
        mListView.setAdapter(mAdapter);
       
        mInflater = LayoutInflater.from(this);
        mHandler = new DownloadHandler(this);
        
        mImageWorker = new ImageWorker();
        mImageWorker.setDefaultIcon(getResources().getDrawable(R.drawable.cover_icon_default));
        
        Button searchButton = (Button)findViewById(R.id.btn_search);
        searchButton.setOnClickListener(this);
        
        mInputCtrl = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public void onStop(){
    	super.onStop();
    	if (mDownloadThread != null){
    		mDownloadThread.interrupt();
    	}
    }

	@Override
	public void onClick(View view) {
		switch (view.getId()){
		case R.id.btn_search:
			
			// 隐藏软键盘
            mInputCtrl.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
            
            // 判断输入是否为空
            String keyword = mEditText.getText().toString();
			if (keyword.equals("") || keyword.equals(mPreKeyword))
				break;
			
			mPreKeyword = keyword;
			
			// 清空当前列表
			mResultList.clear();
			mAdapter.notifyDataSetChanged();
			
			// 显示进度条
			mProgressBar.setVisibility(View.VISIBLE);
			
			// 如果上一次的下载线程还在运行，则另其中断。
			if (mDownloadThread != null && 
				mDownloadThread.getState() != Thread.State.TERMINATED){
				mDownloadThread.interrupt();
			}
			
			// 运行新的下载线程
			mDownloadThread = new DownloadThread();
			mDownloadThread.start();
			break;
		default:
			break;
		}
	}
	
	static class ViewHolder{
		ImageView icon;
		TextView title, teacher, school, category;
	}

	/**
	 * 搜索结果ListView的Adapter
	 */
	class ResultListAdapter extends BaseAdapter{

		/**
		 * 刷新ListView数据，向其中添加新的item
		 * @param newItems 新添加的item
		 */
		public synchronized void refreshAdapter(List<ResultListItem> newItems){
			mResultList.addAll(newItems);
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			return mResultList.size();
		}

		@Override
		public Object getItem(int position) {
			return mResultList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null){
				convertView = mInflater.inflate(R.layout.listview_item, parent, false);
				holder = new ViewHolder();
				holder.icon = (ImageView)convertView.findViewById(R.id.cover_image);
				holder.title = (TextView)convertView.findViewById(R.id.title);
				holder.teacher = (TextView)convertView.findViewById(R.id.teacher);
				holder.school = (TextView)convertView.findViewById(R.id.school);
				holder.category = (TextView)convertView.findViewById(R.id.category);
				convertView.setTag(holder);
			}
			else{
				holder = (ViewHolder)convertView.getTag();
			}
			holder.title.setText(mResultList.get(position).title);
			holder.teacher.setText("讲师：" + mResultList.get(position).teacher);
			holder.school.setText("学校：" + mResultList.get(position).school);
			holder.category.setText("类型：" + mResultList.get(position).category);
			holder.icon.setTag(mResultList.get(position).imageURL);
			Drawable drawable = mImageWorker.loadImage(mAdapter, holder.icon);
			holder.icon.setImageDrawable(drawable);
			
			return convertView;
		}
	}
	
	/**
	 * 下载完一个网页后，通过这个Handler更新ListView数据
	 *
	 */
	static class DownloadHandler extends Handler{
		WeakReference<Context> mContextRef = null;
		DownloadHandler(Context context){
			mContextRef = new WeakReference<Context>(context);
		}
		
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg){
			MainActivity mainAcvitiy = (MainActivity) mContextRef.get();
			mainAcvitiy.mProgressBar.setVisibility(View.INVISIBLE);
			
			switch(msg.what){
			case MSG_APPEND_RESULT:
				ResultListAdapter adapter = (ResultListAdapter)mainAcvitiy.mAdapter;
				adapter.refreshAdapter((List<ResultListItem>)msg.obj);
				break;
			case MSG_NO_RESULT:
				String note = "对不起，没有找到\"" + msg.obj + "\"相关视频！";
				Toast toast1 = Toast.makeText(mContextRef.get(), note, Toast.LENGTH_SHORT);
				toast1.setGravity(Gravity.CENTER, 0, 0);
				toast1.show();
				mainAcvitiy.mPreKeyword = "";
				break;
			case MSG_NETWORK_PROBLEM:
				Toast toast2 = Toast.makeText(mContextRef.get(), "无法连接到服务器" , Toast.LENGTH_SHORT);
				toast2.setGravity(Gravity.CENTER, 0, 0);
				toast2.show();
				mainAcvitiy.mPreKeyword = "";
				break;
			}
		}
	};

	
	/**
	 * 负责下载搜索结果网页的线程
	 */
	class DownloadThread extends Thread{
		@Override
		public void run(){
			DownloadHelper helper = new DownloadHelper(MainActivity.this);
			String keyword = mEditText.getText().toString();
			
			int resultCount = 0;	// 当前页面搜索结果条数
			int pageID = 1;			// 搜索结果页面编号(从1开始)
			boolean hasResult = false;
			
			// 递增下载搜索结果页面，直到页面中不包含结果，则下载完成
			do{
				String resultPage = helper.getSearchResultPage(keyword, pageID++);
				if (resultPage == null && !Thread.currentThread().isInterrupted()){
					Message msg = new Message();
					msg.what = MSG_NETWORK_PROBLEM;
					mHandler.sendMessage(msg);
					return;
				}
				
				List<ResultListItem> itemList = ResultPageParser.getResultItemList(resultPage);
				if (itemList == null)	return;
				
				resultCount = itemList.size();
				if (resultCount > 0 && !Thread.currentThread().isInterrupted()){
					hasResult = true;
					//mResultList.addAll(itemList);
					Message msg = new Message();
					msg.what = MSG_APPEND_RESULT;
					msg.obj = itemList;
					mHandler.sendMessage(msg);
				}
				
			}while(resultCount > 0 && !Thread.currentThread().isInterrupted());
			
			if (!hasResult && !Thread.currentThread().isInterrupted()){
				Message msg = new Message();
				msg.what = MSG_NO_RESULT;
				msg.obj = keyword;
				mHandler.sendMessage(msg);
			}
		}
	}
}
