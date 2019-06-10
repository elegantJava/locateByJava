package com.wangz.locate_demo.location;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    private static final String default_charset = Charset.forName("gbk").name();

    private static final String MAP_SERVICE_URI = "http://api.map.baidu.com/location/";

    /**
     * 拉取网页所有内容
     * @param url
     * @return string
     */
    public static String explore(String url){
        return explore(url,"");
    }
    public static String explore(String url,String charset){

        charset = StringUtils.isBlank(charset) ? default_charset : charset;
        String content = "";
        try {
            InputStream is = new URL(url).openStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is,charset));
            content = readByBuffer(rd);
        }catch (Exception ignored){}
//        finally {
//            is.close(); ARM 块会自动关闭流
//        }
        return content;
    }

    public static String readByBuffer(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        //缓冲逐行读取
        while ( (line = reader.readLine()) != null ) {
            sb.append(line);
        }
        return sb.toString();
    }

    public static String getIp(){
        String ip = "";
        // 太平洋ip解析地址
        String url = "http://whois.pconline.com.cn/";

        // 拉取网页信息--注意编码
        String resource = explore(url, Charset.forName("gbk").name());

        // 通过正则表达式匹配我们想要的内容，根据拉取的网页内容不同，正则表达式作相应的改变
        Pattern p = Pattern.compile("显示IP地址为(.*?)的位置信息");
        Matcher m = p.matcher(resource);
        if (m.find()) {
            String ipStr = m.group(0);
            // 这里根据具体情况，来截取想要的内容
            ip = ipStr.substring(ipStr.indexOf("为") + 2, ipStr.indexOf("的") - 1);
        }
        return ip;
    }


    public static String getAddress(String ip) {
        JSONObject json;
        String city = null;
        try {
            // 这里调用百度的ip定位api服务 详见 http://api.map.baidu.com/lbsapi/cloud/ip-location-api.htm
            String explore = explore(MAP_SERVICE_URI + "ip?ak=F454f8a5efe5e577997931cc01de3974&ip=" + ip);
            json =  JSON.parseObject(explore);
            System.out.println(json);
            city = (((JSONObject) ((JSONObject) json.get("content")).get("address_detail")).get("city")).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return city;
    }

    public static void main(String[] args) throws JSONException {
        String ip = getIp();
        System.out.println(ip);
        String addr = LocateByIp.getAddress(ip);
        System.out.println(addr);
    }
}
