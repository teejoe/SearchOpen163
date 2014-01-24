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
 * ��������ָ���ؼ��������������ҳ��
 * Ŀ��URL��http://so.open.163.com/movie/search/searchprogram/ot0/
 * @author teejoe (teejoe@163.com)
 *
 */
public class DownloadHelper {
	public static final String TAG = "DownloadHelper";
	private static final String HOST = "http://so.open.163.com/movie/search/searchprogram/ot0/";
	private static final int HTTP_STATUS_OK = 200;
	private String mCharset;	// Ŀ����ҳ���ַ�������string.xml��ָ��
	
	
	DownloadHelper(Context context){
		mCharset = context.getResources().getString(R.string.keyword_charset);
	}
	
	/**
	 * ��ȡָ���ؼ������������ҳ
	 * @param keyword �������Ĺؼ���
	 * @param page ���ҳ��(1,2,3,...)
	 */
	public String getSearchResultPage(String keyword, int page){
		String result = null;
		try{
			// ����ָ������(gb2312)���ؼ���ת��Ϊʮ�����Ƹ�ʽ���ַ���
			String hex = bytesToHexString(keyword.getBytes(mCharset));
			
			// ��������url
			String request = HOST + hex + "/" + Integer.toString(page) + ".html?vs=" + hex;
			Log.v(TAG, request);
			
			// ��ȡ�������
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter("http.protocol.content-charset", mCharset);
			HttpGet get = new HttpGet(request);
			HttpResponse httpResponse = httpClient.execute(get);
			if (httpResponse.getStatusLine().getStatusCode() != HTTP_STATUS_OK){
				Log.e(TAG, "������ҳʧ��! ��ַ:" + request);
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
	 * ���ֽ�����ת��Ϊʮ�������ַ�������ÿ���ַ�ǰ��һ��%������URL
	 * @param inputBytes ��ת�����ֽ�����
	 * @return ת������ַ���
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
