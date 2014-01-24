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
    private InputMethodManager mInputCtrl;		// ��������̵���ʾ
	private String mPreKeyword = "";
	
	private List<ResultListItem> mResultList;	// �������
	private ListView mListView;					// �������ListView
	private BaseAdapter mAdapter;				// ��������б�Adapter
	private EditText mEditText;					// �ؼ��������
	private ProgressBar mProgressBar;			// ������
	
	private DownloadHandler mHandler;			// ������һ����ҳ��ͨ����Handler����ListView
	private DownloadThread mDownloadThread;		// �������������ҳ���߳�
	private ImageWorker mImageWorker;			// �����첽����ͼƬ
	
	
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
			
			// ���������
            mInputCtrl.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
            
            // �ж������Ƿ�Ϊ��
            String keyword = mEditText.getText().toString();
			if (keyword.equals("") || keyword.equals(mPreKeyword))
				break;
			
			mPreKeyword = keyword;
			
			// ��յ�ǰ�б�
			mResultList.clear();
			mAdapter.notifyDataSetChanged();
			
			// ��ʾ������
			mProgressBar.setVisibility(View.VISIBLE);
			
			// �����һ�ε������̻߳������У��������жϡ�
			if (mDownloadThread != null && 
				mDownloadThread.getState() != Thread.State.TERMINATED){
				mDownloadThread.interrupt();
			}
			
			// �����µ������߳�
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
	 * �������ListView��Adapter
	 */
	class ResultListAdapter extends BaseAdapter{

		/**
		 * ˢ��ListView���ݣ�����������µ�item
		 * @param newItems ����ӵ�item
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
			holder.teacher.setText("��ʦ��" + mResultList.get(position).teacher);
			holder.school.setText("ѧУ��" + mResultList.get(position).school);
			holder.category.setText("���ͣ�" + mResultList.get(position).category);
			holder.icon.setTag(mResultList.get(position).imageURL);
			Drawable drawable = mImageWorker.loadImage(mAdapter, holder.icon);
			holder.icon.setImageDrawable(drawable);
			
			return convertView;
		}
	}
	
	/**
	 * ������һ����ҳ��ͨ�����Handler����ListView����
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
				String note = "�Բ���û���ҵ�\"" + msg.obj + "\"�����Ƶ��";
				Toast toast1 = Toast.makeText(mContextRef.get(), note, Toast.LENGTH_SHORT);
				toast1.setGravity(Gravity.CENTER, 0, 0);
				toast1.show();
				mainAcvitiy.mPreKeyword = "";
				break;
			case MSG_NETWORK_PROBLEM:
				Toast toast2 = Toast.makeText(mContextRef.get(), "�޷����ӵ�������" , Toast.LENGTH_SHORT);
				toast2.setGravity(Gravity.CENTER, 0, 0);
				toast2.show();
				mainAcvitiy.mPreKeyword = "";
				break;
			}
		}
	};

	
	/**
	 * �����������������ҳ���߳�
	 */
	class DownloadThread extends Thread{
		@Override
		public void run(){
			DownloadHelper helper = new DownloadHelper(MainActivity.this);
			String keyword = mEditText.getText().toString();
			
			int resultCount = 0;	// ��ǰҳ�������������
			int pageID = 1;			// �������ҳ����(��1��ʼ)
			boolean hasResult = false;
			
			// ���������������ҳ�棬ֱ��ҳ���в�������������������
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
