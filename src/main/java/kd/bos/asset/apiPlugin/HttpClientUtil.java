package kd.bos.asset.apiPlugin;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author sam
 *
 */
public class HttpClientUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    /**
     * post请求
     * @param url         url地址
     * @return
     */
    public static JSONObject doPost(String url, Map<String, String> params){
        // 创建默认的httpClient实例.
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = null;
        CloseableHttpResponse response = null;
        JSONObject jsonResult = null;
        try{
            List<NameValuePair> pairs = null;
            if (params != null && !params.isEmpty()) {
                pairs = new ArrayList<NameValuePair>(params.size());
                for (Map.Entry<String,String> entry : params.entrySet()) {
                    pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
            }
            // 创建httppost
            httpPost = new HttpPost(url);
            if (pairs != null && !pairs.isEmpty()) {
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairs, Charset.forName("UTF-8"));
                httpPost.setEntity(entity);
            }
            //超时设置,设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000).build();
            httpPost.setConfig(requestConfig);
            response = httpClient.execute(httpPost);
            /**请求发送成功，并得到响应**/
            if (response.getStatusLine().getStatusCode() == 200) {
                logger.error("POST请求发送成功，并得到响应");
                String str;
                try {
                    /**读取服务器返回过来的json字符串数据**/
                    HttpEntity httpEntity = response.getEntity();
                    str = EntityUtils.toString(httpEntity);
                    Gson gson = new Gson();
                    logger.info("URL:["+url + "];Params:[" + gson.toJson(params) +"];Response:[" + str + "].");
                    /**把json字符串转换成json对象**/
                    jsonResult = JSONObject.parseObject(str);
                } catch (Exception e) {
                    logger.error("json字符串转换成json对象异常:" + url, e);
                    return null;
                }
            }else{
                logger.error("POST请求异常，异常码是："+ response.getStatusLine().getStatusCode());
                return null;
            }
        } catch (IOException e) {
            logger.error("post请求提交失败:" + url, e);
            return null;
        }finally{
            try {
                if (null != response) {
                    response.close();
                    httpClient.close();
                }
            } catch (IOException e) {
                logger.error("关闭资源异常",e);
            }
        }
        return jsonResult;
    }
}
