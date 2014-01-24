package com.netease.searchopen163;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.util.Log;

/**
 * 负责下载指定关键字搜索结果的网页。
 * 目标URL：http://so.open.163.com/movie/search/searchprogram/ot0/
 * @author teejoe (teejoe@163.com)
 *
 */
public class DownloadHelper {
	public static final String TAG = "DownloadHelper";
	private static final String HOST = "http://so.open.163.com/movie/search/searchprogram/ot0/";
	private static final int HTTP_STATUS_OK = 200;
	private String mCharset;	// 目标网页的字符集，在string.xml中指定
	
	
	DownloadHelper(Context context){
		mCharset = context.getResources().getString(R.string.keyword_charset);
	}
	
	/**
	 * 获取指定关键字搜索结果网页
	 * @param keyword 待搜索的关键字
	 * @param page 结果页码(1,2,3,...)
	 */
	public String getSearchResultPage(String keyword, int page){
		String result = null;
		try{
			// 采用指定编码(gb2312)将关键字转换为十六进制格式的字符串
			String hex = bytesToHexString(keyword.getBytes(mCharset));
			
			// 设置请求url
			String request = HOST + hex + "/" + Integer.toString(page) + ".html?vs=" + hex;
			Log.v(TAG, request);
			
			// 获取搜索结果
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter("http.protocol.content-charset", mCharset);
			HttpGet get = new HttpGet(request);
			HttpResponse httpResponse = httpClient.execute(get);
			if (httpResponse.getStatusLine().getStatusCode() != HTTP_STATUS_OK){
				Log.e(TAG, "下载网页失败! 网址:" + request);
				return null; 
			}
			
			HttpEntity entity = httpResponse.getEntity();
			if (entity != null){
				result = EntityUtils.toString(entity, mCharset).trim();
			}
		}catch (Exception e){
			Log.e(TAG, "download failed!");
			e.printStackTrace();
		}
		
		return result;
	}
	/**
	 * 将字节数组转换为十六进制字符串，并每个字符前加一个%，用于URL
	 * @param inputBytes 待转换的字节数组
	 * @return 转换后的字符串
	 */
	public static String bytesToHexString(byte[] inputBytes){
		
		StringBuffer buf = new StringBuffer();

		for (byte b: inputBytes){
			buf.append('%');
			String hex = Integer.toHexString((int)b & 0xff);
			if (hex.length() < 2){
				buf.append('0');
			}
			buf.append(hex);
		}
		
		return buf.toString().toUpperCase();
	}
}
