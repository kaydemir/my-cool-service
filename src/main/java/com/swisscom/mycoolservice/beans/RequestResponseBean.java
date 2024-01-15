package com.swisscom.mycoolservice.beans;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Component
@JsonIgnoreProperties({ "_id", "id" })
public class RequestResponseBean implements Serializable {
    private static final long serialVersionUID = -7912756888709345440L;


    private long requestId;
    private String requestTime = "";
    private String requestMethod = ""; //(GET/POST)
    private String requestURI = "";
    private String requestPostData = "";
    private Map<String, String> requestQueryParams = Collections.emptyMap();
    private String userId = "";
    private String ip = "";
    private String thread = "";
    private long duration;

    private long responseTime; //(in epoch so query can be optimized)
    private String responseData = "";
    private int responseCode;
    private int responseHttpCode;

    //    request headers - each as individual fields, include only our special headers only
//    include x-forwarded-for even though its not our header
//    queryParameters - each as individual fields, add prefix qp_ for each query parameter
    private Map<String,String> requestHeaders;


    public final Map<String,String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(Map<String,String> headers) {
        this.requestHeaders = headers;
    }

    /**
     * @return the requestTime
     */
    public String getRequestTime() {
        return requestTime;
    }

    /**
     * @param requestTime the requestTime to set
     */
    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    /**
     * @return the requestMethod
     */
    public String getRequestMethod() {
        return requestMethod;
    }

    /**
     * @param requestMethod the requestMethod to set
     */
    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * @return the requestURI
     */
    public String getRequestURI() {
        return requestURI;
    }

    /**
     * @param requestURI the requestURI to set
     */
    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    /**
     * @return the requestPostData
     */
    public String getRequestPostData() {
        return requestPostData;
    }

    /**
     * @param requestPostData the requestPostData to set
     */
    public void setRequestPostData(String requestPostData) {
        this.requestPostData = requestPostData;
    }

    /**
     * @return the responseTime
     */
    public long getResponseTime() {
        return responseTime;
    }

    /**
     * @param responseTime the responseTime to set
     */
    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    /**
     * @return the responseData
     */
    public String getResponseData() {
        return responseData;
    }

    /**
     * @param responseData the responseData to set
     */
    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    /**
     * @return the responseCode
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * @param responseCode the responseCode to set
     */
    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * @return the responseHttpCode
     */
    public int getResponseHttpCode() {
        return responseHttpCode;
    }

    /**
     * @param responseHttpCode the responseHttpCode to set
     */
    public void setResponseHttpCode(int responseHttpCode) {
        this.responseHttpCode = responseHttpCode;
    }

    /**
     * @return the requestQueryParams
     */
    public Map<String, String> getRequestQueryParams() {
        return requestQueryParams;
    }

    /**
     * @param requestQueryParams the requestQueryParams to set
     */
    public void setRequestQueryParams(Map<String, String> requestQueryParams) {
        this.requestQueryParams = requestQueryParams;
    }

    /**
     * @return the requestId
     */
    public long getRequestId() {
        return requestId;
    }

    /**
     * @param requestId the requestId to set
     */
    public void setRequestId(long requestId) {
        this.requestId = requestId % 10000;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getThread() {
        return thread;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "RequestResponseBean [requestId=" + requestId + ", requestTime=" + requestTime + ", requestMethod="
                + requestMethod + ", requestURI=" + requestURI + ", requestPostData=" + requestPostData
                + ", requestQueryParams=" + requestQueryParams + ", userId=" + userId + ", ip=" + ip + ", thread="
                + thread + ", duration=" + duration + ", responseTime=" + responseTime + ", responseData="
                + responseData + ", responseCode=" + responseCode + ", responseHttpCode=" + responseHttpCode
                + ", requestHeaders=" + requestHeaders + "]";
    }
}
