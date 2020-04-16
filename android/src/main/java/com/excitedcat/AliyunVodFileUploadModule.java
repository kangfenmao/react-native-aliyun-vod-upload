package com.excitedcat;

import androidx.annotation.Nullable;

import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.vod.upload.VODUploadCallback;
import com.alibaba.sdk.android.vod.upload.VODUploadClient;
import com.alibaba.sdk.android.vod.upload.VODUploadClientImpl;
import com.alibaba.sdk.android.vod.upload.model.UploadFileInfo;
import com.alibaba.sdk.android.vod.upload.model.VodInfo;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.ArrayList;
import java.util.List;

public class AliyunVodFileUploadModule extends ReactContextBaseJavaModule {
    private final ReactApplicationContext reactContext;
    private VODUploadClient uploader;
    private String videoId;

    public AliyunVodFileUploadModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.uploader = new VODUploadClientImpl(reactContext.getApplicationContext());
    }

    @Override
    public String getName() {
        return "AliyunVodFileUpload";
    }

    private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    @ReactMethod
    public void init(ReadableMap config, Callback callback) {
        final String uploadAuth = config.getString("uploadAuth");
        final String uploadAddress = config.getString("uploadAddress");

        // setup callback
        VODUploadCallback vodCallback = new VODUploadCallback() {
            public void onUploadSucceed(UploadFileInfo info) {
                OSSLog.logInfo("[VodUpload] onUploadSucceed" + info.getFilePath());
                WritableMap params = Arguments.createMap();
                params.putString("videoId", videoId);
                params.putString("bucket", info.getBucket());
                params.putString("endpoint", info.getEndpoint());
                sendEvent(reactContext, "onUploadSucceed", params);
            }

            public void onUploadFailed(UploadFileInfo info, String code, String message) {
                OSSLog.logError("[VodUpload] onUploadFailed" + info.getFilePath() + " " + code + " " + message);
                WritableMap params = Arguments.createMap();
                params.putString("code", code);
                params.putString("message", message);
                sendEvent(reactContext, "onUploadFailed", params);
            }

            public void onUploadProgress(UploadFileInfo info, long uploadedSize, long totalSize) {
                OSSLog.logInfo("[VodUpload] onProgress" + info.getFilePath() + " " + uploadedSize + " " + totalSize);

                WritableMap params = Arguments.createMap();
                params.putDouble("uploadedSize", uploadedSize);
                params.putDouble("totalSize", totalSize);
                params.putDouble("progress", (float) uploadedSize / totalSize);
                sendEvent(reactContext, "OnUploadProgress", params);
            }

            public void onUploadTokenExpired() {
                OSSLog.logError("[VodUpload] onUploadProgress");
                // 重新刷新上传凭证:RefreshUploadVideo
                String uploadAuth = "此处需要设置重新刷新凭证之后的值";
                uploader.resumeWithAuth(uploadAuth);
                WritableMap params = Arguments.createMap();
                params.putString("message", "upload token expired.");
                sendEvent(reactContext, "onUploadTokenExpired", params);
            }

            public void onUploadRetry(String code, String message) {
                OSSLog.logDebug("[VodUpload] onUploadRetry");
                WritableMap params = Arguments.createMap();
                params.putString("code", code);
                params.putString("message", message);
                sendEvent(reactContext, "onUploadRetry", params);
            }

            public void onUploadRetryResume() {
                OSSLog.logDebug("[VodUpload] onUploadRetryResume");
                WritableMap params = Arguments.createMap();
                params.putString("message", "upload retry resume");
                sendEvent(reactContext, "onUploadRetryResume", params);
            }

            public void onUploadStarted(UploadFileInfo uploadFileInfo) {
                OSSLog.logInfo("[VodUpload] onUploadStarted");
                uploader.setUploadAuthAndAddress(uploadFileInfo, uploadAuth, uploadAddress);
                WritableMap params = Arguments.createMap();
                params.putString("message", "upload upload started.");
                sendEvent(reactContext, "onUploadStarted", params);
            }
        };

        // 清除所有文件
        this.clearFiles();

        //上传初始化
        uploader.init(vodCallback);
        videoId = config.getString("videoId");

        String resultMessage = "[VodUploadClient] init success.";
        callback.invoke(String.format(resultMessage, uploadAuth, uploadAddress));
    }

    @ReactMethod
    public void addFile(ReadableMap file) {
        List<String> tags = new ArrayList<String>();
        tags.add(file.getString("tags"));

        String filePath = file.getString("path");
        VodInfo vodInfo = new VodInfo();
        vodInfo.setTitle(file.getString("title"));
        vodInfo.setDesc(file.getString("desc"));
        vodInfo.setCateId(file.getInt("cateId"));
        vodInfo.setTags(tags);

        uploader.addFile(filePath, vodInfo);
    }

    /**
     * 从队列中删除上传文件
     * 如果待删除的文件正在上传中，则取消上传并自动上传下一个文件
     */
    @ReactMethod
    public void deleteFile(int index) {
        uploader.deleteFile(index);
    }

    /**
     * 清空上传队列
     * 如果有文件在上传，则取消上传
     */
    @ReactMethod
    public void clearFiles() {
        uploader.clearFiles();
    }

    /**
     * 获取上传文件队列
     */
    @ReactMethod
    public void listFiles(Callback callback) {
        List<UploadFileInfo> filesList = uploader.listFiles();

        WritableArray files = Arguments.createArray();

        for (UploadFileInfo fileInfo : filesList) {
            WritableMap fileMap = Arguments.createMap();
            VodInfo vodInfo = fileInfo.getVodInfo();

            fileMap.putString("name", vodInfo.getFileName());
            fileMap.putString("title", vodInfo.getTitle());
            fileMap.putString("size", vodInfo.getFileSize());
            fileMap.putString("path", fileInfo.getFilePath());
            fileMap.putInt("type", fileInfo.getFileType());
            fileMap.putString("desc", vodInfo.getDesc());

            fileMap.putInt("cateId", vodInfo.getCateId());
            fileMap.putString("coverUrl", vodInfo.getCoverUrl());

            files.pushMap(fileMap);
        }

        callback.invoke(files);
    }

    /**
     * 将文件标记为取消
     * 文件任保留在上传列表中。如果待取消的文件正在上传中，则取消上传并自动上传下一个文件
     */
    @ReactMethod
    public void cancelFile(int index) {
        uploader.cancelFile(index);
    }

    /* 上传控制 */

    /**
     * 开始上传
     */
    @ReactMethod
    public void start() {
        uploader.start();
    }

    /**
     * 停止上传
     * 如果有文件正在上传中，则取消上传
     */
    @ReactMethod
    public void stop() {
        uploader.stop();
    }

    /**
     * 恢复待上传文件
     * 或者清空队列后重新添加文件上传
     */
    @ReactMethod
    public void pause() {
        uploader.pause();
    }

    /**
     * 恢复上传
     */
    @ReactMethod
    public void resume() {
        uploader.resume();
    }
}
