package com.example.jy.testapplication.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Base64;

/**
 * 上传工具类
 * 支持上传文件和参数
 */
public class UploadUtil {
    private static final String TAG = "UploadUtil";
    public static String IPADRESS = "http://192.168.0.101:8001/api/AndroidApi/UpdateImage";
    private static UploadUtil uploadUtil;
    private static final String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
    private static final String PREFIX = "--";
    private static final String LINE_END = "\r\n";
    private static final String CONTENT_TYPE = "multipart/form-data"; // 内容类型

    private int readTimeOut = 10 * 1000; // 读取超时
    private int connectTimeout = 10 * 1000; // 超时时间
    /***
     * 请求使用多长时间
     */
    private static int requestTime = 0;

    private static final String CHARSET = "utf-8"; // 设置编码

    /***
     * 上传成功
     */
    public static final int UPLOAD_SUCCESS_CODE = 1;
    /**
     * 文件不存在
     */
    public static final int UPLOAD_FILE_NOT_EXISTS_CODE = 2;
    /**
     * 服务器出错
     */
    public static final int UPLOAD_SERVER_ERROR_CODE = 3;
    protected static final int WHAT_TO_UPLOAD = 1;
    protected static final int WHAT_UPLOAD_DONE = 2;

    private UploadUtil() {
    }

    /**
     * 单例模式获取上传工具类
     *
     * @return
     */
    public static UploadUtil getInstance() {
        if (null == uploadUtil) {
            uploadUtil = new UploadUtil();
        }
        return uploadUtil;
    }

    /**
     * android上传文件到服务器
     *
     * @param filePath   需要上传的文件的路径
     * @param fileKey    在网页上<input type=file name=xxx/> xxx就是这里的fileKey
     * @param RequestURL 请求的URL
     */
    public void uploadFile(String filePath, String fileKey, String RequestURL,
                           Map<String, String> param) {
        if (filePath == null) {
            sendMessage(UPLOAD_FILE_NOT_EXISTS_CODE, "文件不存在");
            return;
        }
        try {
            File file = new File(filePath);
            uploadFile(file, fileKey, RequestURL, param);
        } catch (Exception e) {
            sendMessage(UPLOAD_FILE_NOT_EXISTS_CODE, "文件不存在");
            e.printStackTrace();
            return;
        }
    }

    /**
     * android上传文件到服务器
     *
     * @param file       需要上传的文件
     * @param fileKey    在网页上<input type=file name=xxx/> xxx就是这里的fileKey
     * @param RequestURL 请求的URL
     */
    public void uploadFile(final File file, final String fileKey,
                           final String RequestURL, final Map<String, String> param) {
        if (file == null || (!file.exists())) {
            sendMessage(UPLOAD_FILE_NOT_EXISTS_CODE, "文件不存在");
            return;
        }

        LogUtil.i(TAG, "请求的URL=" + RequestURL);
        LogUtil.i(TAG, "请求的fileName=" + file.getName());
        LogUtil.i(TAG, "请求的fileKey=" + fileKey);
        new Thread(new Runnable() {  //开启线程上传文件
            @Override
            public void run() {
                try {
                    doJsonPost(file, RequestURL);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     * 发送上传结果
     *
     * @param responseCode
     * @param responseMessage
     */
    private void sendMessage(int responseCode, String responseMessage) {
        onUploadProcessListener.onUploadDone(responseCode, responseMessage);
    }

    /**
     * 下面是一个自定义的回调函数，用到回调上传文件是否完成
     *
     * @author shimingzheng
     */
    public static interface OnUploadProcessListener {
        /**
         * 上传响应
         *
         * @param responseCode
         * @param message
         */
        void onUploadDone(int responseCode, String message);

        /**
         * 上传中
         *
         * @param uploadSize
         */
        void onUploadProcess(int uploadSize);

        /**
         * 准备上传
         *
         * @param fileSize
         */
        void initUpload(int fileSize);
    }

    private OnUploadProcessListener onUploadProcessListener;


    public void setOnUploadProcessListener(
            OnUploadProcessListener onUploadProcessListener) {
        this.onUploadProcessListener = onUploadProcessListener;
    }

    /**
     * 获取上传使用的时间
     *
     * @return
     */
    public static int getRequestTime() {
        return requestTime;
    }

    public static interface uploadProcessListener {

    }

    private String doJsonPost(File file, String RequestURL) throws IOException {
        LogUtil.d(TAG, "doJsonPost, file.getName(): " + file.getName());
        LogUtil.d(TAG, "doJsonPost, file.getAbsolutePath(): " + file.getAbsolutePath());
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        byte[] byt = new byte[is.available()];
        is.read(byt);
        is.close();
        LogUtil.d(TAG, "doJsonPost, byt: " + byt.toString() + ", length:" + byt.length);
        JSONObject object = new JSONObject();
        try {
            object.put("image", Base64.encodeToString(byt, 0));
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        try {
            LogUtil.d(TAG, "object.getString: " + object.getString("image").trim());
        } catch (JSONException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        String content = null;
        try {
            //服务器对于json数据接收的格式要求(此处要求不能有key，且必须加上双引号)
            content = "\"" + object.getString("image").trim() + "\"";
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        String result = "";
        BufferedReader reader = null;
        try {
            URL url = new URL(RequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            // 设置文件类型:
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            // 设置接收类型否则返回415错误
            //conn.setRequestProperty("accept","*/*")此处为暴力方法设置接受所有类型，以此来防范返回415;
            //conn.setRequestProperty("accept","application/json");
            // 往服务器里面发送数据
            if (content != null && !TextUtils.isEmpty(content)) {
                LogUtil.d(TAG, "json: " + content);
                byte[] writebytes = content.getBytes();
                LogUtil.d(TAG, "writebytes: " + writebytes);
                LogUtil.d(TAG, "writebytes.length: " + writebytes.length);
                // 设置文件长度
                conn.setRequestProperty("Content-Length", String.valueOf(writebytes.length));
                OutputStream outwritestream = conn.getOutputStream();
                //outwritestream.write(json.getBytes());
                outwritestream.write(writebytes);
                outwritestream.flush();
                outwritestream.close();
                LogUtil.d(TAG, "doJsonPost: conn" + conn.getResponseCode());
            }
            if (conn.getResponseCode() == 200) {
                reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                result = reader.readLine();
                //sendMessage(UPLOAD_SUCCESS_CODE, "上传结果：" + result);
            }

            LogUtil.d(TAG, "conn.getResponseCode(): " + conn.getResponseCode());
            LogUtil.d(TAG, "result: " + result);
        } catch (Exception e) {
            //sendMessage(UPLOAD_SERVER_ERROR_CODE, "上传失败：error=" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //sendMessage(UPLOAD_SERVER_ERROR_CODE, "上传失败：error=" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}