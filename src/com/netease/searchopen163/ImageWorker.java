package com.netease.searchopen163;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * 异步下载图片到指定的ImageView（ImageView的tag必须设置为图片URL）
 * @author teejoe (teejoe@163.com)
 *
 */
public class ImageWorker {

	public static final String TAG = "ImageWorker";
    private Drawable DEFAULT_ICON = null;
	private HashMap<String, Drawable> mImageCache;
    private BaseAdapter mAdapter;
    
    public ImageWorker (){
        mImageCache = new HashMap<String, Drawable>();
    }
    
    public void setDefaultIcon(Drawable icon){
    	DEFAULT_ICON = icon;
    }
    
    public Drawable loadImage (BaseAdapter adapter, ImageView view){
    	this.mAdapter = adapter;
        String url = (String) view.getTag();
        if (mImageCache.containsKey(url)){
            return mImageCache.get(url);
        }
        else {
            synchronized (this) {
                mImageCache.put(url, DEFAULT_ICON);
            }
            new ImageTask().execute(url);
            return DEFAULT_ICON;
        }
    }
    
    private class ImageTask extends AsyncTask<String, Void, Drawable>
    {
        private String mURL;

        @Override
        protected Drawable doInBackground(String... params) {
            mURL = params[0];
            InputStream istr;
            try {
                URL url = new URL(mURL);
                istr = url.openStream();
            } catch (MalformedURLException e) {
                Log.d(TAG, "Malformed: " + e.getMessage());
                throw new RuntimeException(e);
            } catch (IOException e){
                Log.d(TAG, "I/O : " + e.getMessage());
                throw new RuntimeException(e);
            }
            return Drawable.createFromStream(istr, "src");
        }

        @Override
        protected void onPostExecute(Drawable result) {
            super.onPostExecute(result);
            synchronized (this) {
                mImageCache.put(mURL, result);
            }
            mAdapter.notifyDataSetChanged();
        }
        
    }
}
