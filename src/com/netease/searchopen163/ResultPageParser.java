package com.netease.searchopen163;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

/**
 * �������������ҳ��������ȡ���γ���Ϣ
 * @author teejoe (teejoe@163.com)
 *
 */
public class ResultPageParser {
	
	static final String TAG = "ResultPageParser";
	
	// ��ҳ������������ֵĿ�ʼ��־
	static final String BEGIN_TAG = "<p class=\"searchArea-result\">";	
	
	// ��ҳ������������ֵĽ�����־
	static final String END_TAG = "<ul class=\"contentArea-resultList\">";	
	
	// group(1)ƥ�����������Ƶ����
	static final Pattern COUNT_PATTERN = Pattern.compile("�����Ƶ<em>(\\d*)</em>��");
	
	// ƥ�����ͼƬurl
	static final Pattern IMAGE_URL_PATTERN = Pattern.compile("http://vimg1.*jpg");
	
	// group(1)ƥ��γ̱���
	static final Pattern TITLE_PATTERN = Pattern.compile("contentArea-resultList-title\\s*cBlue\">[\\s\\S]*?>(.*?)</a>");
	
	// group(1)ƥ����Ƶ�ۿ�����
	static final Pattern TARGET_URL_PATTERN = Pattern.compile("contentArea-resultList-play\"\\s*href=\"(http://v\\.163.*?)\"");
	
	// group(1)ƥ��ѧУ
	static final Pattern SCHOOL_PATTERN = Pattern.compile("ѧУ��<em.*?>(.*?)</em>");
	
	// group(1)ƥ�佲ʦ
	static final Pattern TEACHER_PATTERN = Pattern.compile("��ʦ��<em.*?>(.*?)</em>");
	
	// group(1)ƥ��γ̷���
	static final Pattern CATEGORY_PATTERN = Pattern.compile("���ͣ�<em.*?>(.*?)</em>");
	/**
	 * ��ȡ��ҳ�а�����������Ĳ���
	 * @param oriPage ԭʼ��ҳhtml
	 * @return ԭʼ��ҳ�а�����������Ĳ���
	 */
	private static String getResultContent(String oriPage){
		if (oriPage == null)	return null;
		
		int begin = oriPage.indexOf(BEGIN_TAG);
		int end = oriPage.lastIndexOf(END_TAG);
		if (begin == -1 || end == -1){
			Log.e("ResultPageParser", "Can't find BEGIN_TAG or END_TAG!");
			return null;
		}
		
		return oriPage.substring(begin, end);
	}
	
	/**
	 * ��ȡ������������Ŀ
	 * @param resultContent �������ҳ���html����
	 * @return ���������Ŀ
	 */
	public static int getTotalResultCount(String resultContent){
		int count = 0;
		Matcher m = COUNT_PATTERN.matcher(resultContent);
		if (m.find()){
			String res = m.group(1);
			count = Integer.valueOf(res);
		}
		return count;
	}
	
	/**
	 * ��ȡ����ͼƬ��URL
	 * @param resultContent �������ҳ��html����
	 * @return ����ͼƬURL�б�
	 */
	private static List<String> getImageUrlList(String resultContent){
		List<String> result = new ArrayList<String>();
		
		Matcher m = IMAGE_URL_PATTERN.matcher(resultContent);
		while (m.find()){
			String url = m.group();
			result.add(url);
		}
		return result;
	}
	
	/**
	 * ��ȡ�γ̱���
	 * @param resultContent �������ҳ��html����
	 * @return
	 */
	private static List<String> getTitleList(String resultContent){
		List<String> result = new ArrayList<String>();
		
		Matcher m = TITLE_PATTERN.matcher(resultContent);
		while (m.find()){
			String url = m.group(1);
			result.add(url);
		}
		return result;
	}
	
	/**
	 * ��ȡ�γ̹ۿ�����
	 * @param resultContent �������ҳ��html����
	 * @return �γ̹ۿ������б�
	 */
	private static List<String> getMovieUrlList(String resultContent){
		List<String> result = new ArrayList<String>();
		
		Matcher m = TARGET_URL_PATTERN.matcher(resultContent);
		while (m.find()){
			String url = m.group(1);
			result.add(url);
		}
		return result;
	}
	
	/**
	 * ��ȡ�γ�ѧУ��
	 * @param resultContent �������ҳ��html����
	 * @return �γ�ѧУ�б�
	 */
	private static List<String> getSchoolList(String resultContent){
		List<String> result = new ArrayList<String>();
		
		Matcher m = SCHOOL_PATTERN.matcher(resultContent);
		while (m.find()){
			String name = m.group(1);
			result.add(name);
		}
		return result;
	}
	
	/**
	 * ��ȡ�γ̽�ʦ��
	 * @param resultContent �������ҳ��html����
	 * @return �γ̽�ʦ���б�
	 */
	private static List<String> getTeacherList(String resultContent){
		List<String> result = new ArrayList<String>();
		
		Matcher m = TEACHER_PATTERN.matcher(resultContent);
		while (m.find()){
			String name = m.group(1);
			result.add(name);
		}
		return result;
	}
	
	/**
	 * ��ȡ�γ̷���
	 * @param resultContent �������ҳ��html����
	 * @return �γ̷����б�
	 */
	private static List<String> getCategoryList(String resultContent){
		List<String> result = new ArrayList<String>();
		
		Matcher m = CATEGORY_PATTERN.matcher(resultContent);
		while (m.find()){
			String category = m.group(1);
			result.add(category);
		}
		return result;
	}
	
	/**
	 * ��ȡ��ҳ�����������Ŀ
	 * @param oriPage Ԫ����ҳhtml
	 * @return ���������Ŀ�б�
	 */
	public static List<ResultListItem> getResultItemList(String oriPage){
		if (oriPage == null)	return null;
		
		// ��ȡԭʼ��ҳ�а�����������Ĳ���
		String resultContent = getResultContent(oriPage);
		if (resultContent == null)
			return null;
		
		// �ֱ���ȡͼƬURL���γ̱��⣬�γ�ѧУ���γ̽�ʦ���γ̷��࣬�γ̲��ŵ�ַ
		List<String> imageUrlList = getImageUrlList(resultContent);
		List<String> titleList = getTitleList(resultContent);
		List<String> schoolList = getSchoolList(resultContent);
		List<String> teacherList = getTeacherList(resultContent);
		List<String> categoryList = getCategoryList(resultContent);
		List<String> movieUrlList = getMovieUrlList(resultContent);
		
		// ����Ƿ���ȡ��ȷ
		int count = titleList.size();
		if (!(count == imageUrlList.size()
			  && count == schoolList.size()
		      && count == teacherList.size()
		      && count == categoryList.size()
		      && count == movieUrlList.size())){
			
			Log.e(TAG, "���������������");
			return null;
		}
		
		// ��װ��SearchResultItem�б�
		List<ResultListItem> itemList = new ArrayList<ResultListItem>();
		for (int i = 0; i < count; i++){
			ResultListItem item = new ResultListItem();
			item.imageURL = imageUrlList.get(i);
			item.targetURL = movieUrlList.get(i);
			item.title = titleList.get(i);
			item.school = schoolList.get(i);
			item.teacher = teacherList.get(i);
			item.category = categoryList.get(i);
			
			itemList.add(item);
		}
		
		return itemList;
	}
}
