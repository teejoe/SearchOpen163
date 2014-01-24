package com.netease.searchopen163;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

/**
 * 解析搜索结果网页，从中提取出课程信息
 * @author teejoe (teejoe@163.com)
 *
 */
public class ResultPageParser {
	
	static final String TAG = "ResultPageParser";
	
	// 网页中搜索结果部分的开始标志
	static final String BEGIN_TAG = "<p class=\"searchArea-result\">";	
	
	// 网页中搜索结果部分的结束标志
	static final String END_TAG = "<ul class=\"contentArea-resultList\">";	
	
	// group(1)匹配搜索结果视频个数
	static final Pattern COUNT_PATTERN = Pattern.compile("相关视频<em>(\\d*)</em>个");
	
	// 匹配封面图片url
	static final Pattern IMAGE_URL_PATTERN = Pattern.compile("http://vimg1.*jpg");
	
	// group(1)匹配课程标题
	static final Pattern TITLE_PATTERN = Pattern.compile("contentArea-resultList-title\\s*cBlue\">[\\s\\S]*?>(.*?)</a>");
	
	// group(1)匹配视频观看链接
	static final Pattern TARGET_URL_PATTERN = Pattern.compile("contentArea-resultList-play\"\\s*href=\"(http://v\\.163.*?)\"");
	
	// group(1)匹配学校
	static final Pattern SCHOOL_PATTERN = Pattern.compile("学校：<em.*?>(.*?)</em>");
	
	// group(1)匹配讲师
	static final Pattern TEACHER_PATTERN = Pattern.compile("讲师：<em.*?>(.*?)</em>");
	
	// group(1)匹配课程分类
	static final Pattern CATEGORY_PATTERN = Pattern.compile("类型：<em.*?>(.*?)</em>");
	/**
	 * 提取网页中包含搜索结果的部分
	 * @param oriPage 原始网页html
	 * @return 原始网页中包含搜索结果的部分
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
	 * 获取相关搜索结果数目
	 * @param resultContent 搜索结果页面的html内容
	 * @return 搜索结果数目
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
	 * 获取封面图片的URL
	 * @param resultContent 搜索结果页面html内容
	 * @return 封面图片URL列表
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
	 * 获取课程标题
	 * @param resultContent 搜索结果页面html内容
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
	 * 获取课程观看链接
	 * @param resultContent 搜索结果页面html内容
	 * @return 课程观看链接列表
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
	 * 获取课程学校名
	 * @param resultContent 搜索结果页面html内容
	 * @return 课程学校列表
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
	 * 获取课程讲师名
	 * @param resultContent 搜索结果页面html内容
	 * @return 课程讲师名列表
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
	 * 获取课程分类
	 * @param resultContent 搜索结果页面html内容
	 * @return 课程分类列表
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
	 * 提取网页中搜索结果项目
	 * @param oriPage 元素网页html
	 * @return 搜索结果项目列表
	 */
	public static List<ResultListItem> getResultItemList(String oriPage){
		if (oriPage == null)	return null;
		
		// 截取原始网页中包含搜索结果的部分
		String resultContent = getResultContent(oriPage);
		if (resultContent == null)
			return null;
		
		// 分别提取图片URL，课程标题，课程学校，课程讲师，课程分类，课程播放地址
		List<String> imageUrlList = getImageUrlList(resultContent);
		List<String> titleList = getTitleList(resultContent);
		List<String> schoolList = getSchoolList(resultContent);
		List<String> teacherList = getTeacherList(resultContent);
		List<String> categoryList = getCategoryList(resultContent);
		List<String> movieUrlList = getMovieUrlList(resultContent);
		
		// 检查是否提取正确
		int count = titleList.size();
		if (!(count == imageUrlList.size()
			  && count == schoolList.size()
		      && count == teacherList.size()
		      && count == categoryList.size()
		      && count == movieUrlList.size())){
			
			Log.e(TAG, "搜索结果解析错误！");
			return null;
		}
		
		// 封装成SearchResultItem列表
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
