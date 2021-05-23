package com.wl;

import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;

public class TencentMap {

    @Test(dataProvider = "LocationSearchDataProvider")
    public void locationSearch(ITestContext context, Map<String, String> params) {
        XmlTest xmlTest = context.getCurrentXmlTest();
        String uri = xmlTest.getParameter("uri");
        String api = xmlTest.getParameter("locationSearchAPI");
        String secretkey = xmlTest.getParameter("secretkey");

        params.put("key", context.getCurrentXmlTest().getParameter("key"));

        // 对参数位置有严格要求，需按照升序排列.
        List<Map.Entry<String,String>> paramList = new ArrayList<Map.Entry<String,String>>(params.entrySet());
        Collections.sort(paramList,new Comparator<Map.Entry<String,String>>() {
            //升序排序
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        String city = null;
        String autoExtend = null;
        String boundary = params.get("boundary");
        String searchScopeMethod = boundary.substring(0, boundary.indexOf("("));
        if (searchScopeMethod.equals("region")) {
            city = boundary.substring(boundary.indexOf("(") + 1, boundary.indexOf(","));
            autoExtend = boundary.substring(boundary.indexOf(",") + 1, boundary.indexOf(")"));
        }

        List<NameValuePair> pairList = new ArrayList<NameValuePair>();

        String key = null;
        String value = null;
        StringBuilder sigBuilder = new StringBuilder();
        sigBuilder.append(api).append("?");
        for(Map.Entry<String,String> param:paramList){
            key = param.getKey();
            value = param.getValue();
            sigBuilder.append(key + "=" + value + "&");

            if (key.equals("boundary") && searchScopeMethod.equals("region") && city.length() > 0) {
                value = searchScopeMethod + "(" + URLEncoder.encode(city) + "," + autoExtend + ")";
            }
            if (key.equals("keyword")) {
                value = URLEncoder.encode(value);
            }

            pairList.add(new BasicNameValuePair(key, value));
        }
        if (sigBuilder.length() > 0) {
            sigBuilder.deleteCharAt(sigBuilder.length()-1);
        }
        if (secretkey.length() > 0) {
            sigBuilder.append(secretkey);
            pairList.add(new BasicNameValuePair("sig", DigestUtils.md5Hex(sigBuilder.toString())));
        }

        CloseableHttpResponse response = null;
        HttpClientUtil clientUtil = new HttpClientUtil();
        try {
            response = clientUtil.getExecute(uri+"/ws/place/v1/search", pairList);

            if (response.getStatusLine().getStatusCode() == 200) {
                Assert.assertTrue(true);

                HttpEntity entity = response.getEntity();
                String content = EntityUtils.toString(entity, Charset.defaultCharset());
                EntityUtils.consume(entity);

                JSONObject jsonObject = JSONObject.fromObject(content);
                if (Integer.parseInt(jsonObject.get("status").toString()) == 0) {
                    Assert.assertTrue(true, jsonObject.get("message").toString());
                }
                else {
                    Assert.assertTrue(false, jsonObject.get("message").toString());
                }
            }
            else {
                Assert.assertTrue(false);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }finally {
            clientUtil.close();
        }
    }

    @DataProvider(name = "LocationSearchDataProvider")
    public Object[][] locationSearchDataProvider() {
        ExcelUtil excelUtil = new ExcelUtil();
        // 默认从classpath中找文件(文件放在resources目录下)，name不能带“/”，否则会抛空指针
        return excelUtil.readData("data/MyData.xlsx", "LocationSearch");
    }

}
