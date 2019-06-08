package com.wangz.locate_demo.location;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName LocateByIp
 * @Auther: wangz
 * @Date: 2019/6/7 11:06
 * @Description: TODO
 */
public class LocateByIp {
    /**
     * 实现思路：
     *  1.获取本机公网ip，注意不是局域网ip
     *  2.根据ip获取地理位置，调用百度ip定位api ： http://api.map.baidu.com/lbsapi/cloud/ip-location-api.htm
     */

    /**=============================================实现======================================================*/
    /**
     * 读取所有内容
     *
     * @param rd
     * @return
     * @throws IOException
     */
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    /**
     * 拉取网页所有内容，并转化为Json格式
     *
     * @param url
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = JSON.parseObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    public String getAddress() {
        String ip = "";
        // 这个网址似乎不能了用了
        // String chinaz = "http://ip.chinaz.com";
        // 改用了太平洋的一个网址
        String chinaz = "http://whois.pconline.com.cn/";

        StringBuilder inputLine = new StringBuilder();
        String read = "";
        URL url;
        HttpURLConnection urlConnection = null;
        BufferedReader in = null;
        try {
            url = new URL(chinaz);
            urlConnection = (HttpURLConnection) url.openConnection();
            // 如有乱码的，请修改相应的编码集，这里是 gbk
            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "gbk"));
            while ((read = in.readLine()) != null) {
                inputLine.append(read).append("\r\n");
            }
            // System.out.println(inputLine.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 这个是之前的正则表达式,
        // Pattern p = Pattern.compile("\\<dd class\\=\"fz24\">(.*?)\\<\\/dd>");
        // 通过正则表达式匹配我们想要的内容，根据拉取的网页内容不同，正则表达式作相应的改变
        Pattern p = Pattern.compile("显示IP地址为(.*?)的位置信息");
        Matcher m = p.matcher(inputLine.toString());
        if (m.find()) {
            String ipstr = m.group(0);
            // 这里根据具体情况，来截取想要的内容
            ip = ipstr.substring(ipstr.indexOf("为") + 2, ipstr.indexOf("的") - 1);
            System.out.println(ip);
        }
        JSONObject json = null;
        String city = null;
        try {
            // 这里调用百度的ip定位api服务 详见 http://api.map.baidu.com/lbsapi/cloud/ip-location-api.htm
            json = readJsonFromUrl("http://api.map.baidu.com/location/ip?ak=F454f8a5efe5e577997931cc01de3974&ip=" + ip);
            System.out.println(json);
            city = (((JSONObject) ((JSONObject) json.get("content")).get("address_detail")).get("city")).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return city;
    }

    public static void main(String[] args) throws JSONException {
        LocateByIp Addr = new LocateByIp();
        String addr = Addr.getAddress();
        System.out.println(addr);
    }
}
