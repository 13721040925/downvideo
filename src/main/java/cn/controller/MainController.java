package cn.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;

@Controller
@RequestMapping("/ice")
public class MainController {
	public static String decodeHttpUrl(String url, String type) {
		int start = url.indexOf("http");
		int end = 0;
		switch (type) {
		case "1": {
			end = url.lastIndexOf("/");
			break;
		}
		case "2": {
			end = url.indexOf(" 复制");
			break;
		}
		case "3": {
			end = url.lastIndexOf("/");
			break;
		}
		}
		String decodeurl = url.substring(start, end);
		return decodeurl;
	}

	@CrossOrigin
	@RequestMapping(value = "/magic", produces = "application/json;charset=utf-8")
	@ResponseBody
	public String magic(HttpServletRequest request, HttpServletResponse response, String urls, String type) {
		Long timetmp = null;
		try {
			// 过滤链接，获取http连接地址
			System.out.println(">>>>>>>>>>>>>>" + urls);
			String finalUrl = decodeHttpUrl(urls, type);
			System.out.println(finalUrl);

			// 1.利用Jsoup抓取抖音链接#在抖音，记录美好生活#你们是这样的么
			// 抓取抖音网页
			String htmls = Jsoup.connect(finalUrl).ignoreContentType(true).execute().body();
			System.out.println(htmls); // 做测试时使用
			String matchUrl = "";// 图片
			String matchUrl1 = "";// 视频
			switch (type) {
			case "1": {
				Pattern patternCompile = Pattern.compile("(?<=cover: \")https?://.+(.\")");
				Pattern patternCompile1 = Pattern.compile("(?<=playAddr: \")https?://.+(?=\",)");
				Matcher m = patternCompile1.matcher(htmls);
				Matcher m1 = patternCompile.matcher(htmls);
				while (m.find()) {
					matchUrl = m.group(0);
				}
				matchUrl = matchUrl.substring(0, matchUrl.length() - 1);
				while (m1.find()) {
					matchUrl1 = m1.group(0).replaceAll("playwm", "play");
				}
				break;
			}
			case "2": {
				Pattern patternCompile = Pattern.compile("(?<=poster\":\")https?:.+(u002F)");// 图片
				Pattern patternCompile1 = Pattern.compile("(?<=playUrl\":\")https?:.+(u002F)");// 视频
				Matcher m = patternCompile.matcher(htmls);
				Matcher m1 = patternCompile1.matcher(htmls);
				while (m.find()) {
					matchUrl = m.group(0);
				}
				matchUrl = matchUrl.split("\",\"")[0].replaceAll("u002F", "/");
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < matchUrl.length(); i++) {
					if ((matchUrl.charAt(i)) == 92) {
						continue;
					}

					sb.append(matchUrl.charAt(i));
				}
				matchUrl = sb.toString();

				while (m1.find()) {
					matchUrl1 = m1.group(0);
				}
				matchUrl1 = matchUrl1.split("\",\"")[0].replaceAll("u002F", "/");
				sb = new StringBuilder();
				for (int i = 0; i < matchUrl1.length(); i++) {
					if ((matchUrl1.charAt(i)) == 92) {
						continue;
					}

					sb.append(matchUrl1.charAt(i));
				}
				matchUrl1 = sb.toString();
				break;
			}
			case "3": {
				String[] arr = htmls.split("url_list");

				matchUrl = arr[8].split("\",\"")[1];
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < matchUrl.length(); i++) {
					if ((matchUrl.charAt(i)) == 92) {
						continue;
					}

					sb.append(matchUrl.charAt(i));
				}
				matchUrl = sb.toString();

				matchUrl1 = arr[9].split("\",\"")[0].substring(4).replaceAll("app_id=0", "app_id=1").replaceAll("u0026",
						"&");
				sb = new StringBuilder();
				for (int i = 0; i < matchUrl1.length(); i++) {
					if ((matchUrl1.charAt(i)) == 92) {
						continue;
					}

					sb.append(matchUrl1.charAt(i));
				}
				matchUrl1 = sb.toString();
				break;
			}
			}

			// 将链接封装成流
			Map<String, String> headers = new HashMap<>();
			headers.put("Connection", "keep-alive");
			headers.put("Host", "aweme.snssdk.com");
			headers.put("User-Agent",
					"Mozilla/5.0 (iPhone; CPU iPhone OS 12_1_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/16D57 Version/12.0 Safari/604.1");

			// 6.利用Joup获取视频对象,并作封装成一个输入流对象
			BufferedInputStream in = Jsoup.connect(matchUrl).headers(headers).timeout(10000).ignoreContentType(true)
					.execute().bodyStream();
			BufferedInputStream in1 = Jsoup.connect(matchUrl1).headers(headers).timeout(10000).ignoreContentType(true)
					.execute().bodyStream();
			timetmp = new Date().getTime();
			String fileAddress = request.getSession().getServletContext().getRealPath("/") + "img/" + timetmp + ".jpg";
			String fileAddress1 = request.getSession().getServletContext().getRealPath("/") + "video/" + timetmp
					+ ".mp4";

			// 7.封装一个保存文件的路径对象
			File fileSavePath = new File(fileAddress);
			File fileSavePath1 = new File(fileAddress1);
			// 注:如果保存文件夹不存在,那么则创建该文件夹
			File fileParent = fileSavePath.getParentFile();
			if (!fileParent.exists()) {
				fileParent.mkdirs();
			}
			File fileParent1 = fileSavePath1.getParentFile();
			if (!fileParent1.exists()) {
				fileParent1.mkdirs();
			}
			// 8.新建一个输出流对象
			OutputStream out = new BufferedOutputStream(new FileOutputStream(fileSavePath));
			OutputStream out1 = new BufferedOutputStream(new FileOutputStream(fileSavePath1));
			// 9.遍历输出文件
			int b;
			while ((b = in.read()) != -1) {
				out.write(b);
			}

			out.close();// 关闭输出流
			in.close(); // 关闭输入流

			int b1;
			while ((b1 = in1.read()) != -1) {
				out1.write(b1);
			}
			out1.close();// 关闭输出流
			in1.close(); // 关闭输入流
			// 注:打印获取的链接
			System.out.println("-----去水印图片链接-----\n" + matchUrl);
			System.out.println("\n-----视频图片保存路径-----\n" + fileSavePath.getAbsolutePath());
			System.out.println("-----去水印链接-----\n" + matchUrl1);
			System.out.println("\n-----视频保存路径-----\n" + fileSavePath1.getAbsolutePath());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return JSON.toJSONString(timetmp);
	}

	@RequestMapping("/video")
	public String video() {
		return "video";
	}

	@RequestMapping("/teach")
	public String teach() {
		return "teach";
	}
}
