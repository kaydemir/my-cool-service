package com.swisscom.mycoolservice.logging;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.swisscom.mycoolservice.beans.RequestResponseBean;
import com.swisscom.mycoolservice.servicesimpl.ConfigurationService;
import com.swisscom.mycoolservice.util.PasswordHideUtil;

/**
 * responsible for logging every API request and response
 */
@Component
public class LoggingFilter extends OncePerRequestFilter {

    protected static final Logger logger = LogManager.getLogger(LoggingFilter.class);

    private static final String PASSWORD_KEY = "assw";

    private static final String PASSWORD_MASK = "****";

    private static final int DEFAULT_MAX_REQUEST_TIME = 2000;
    public static final String QUERY_PARAM_DELIMITER = "&";
    public static final String QUERY_PARAM_VALUE_DELIMITER = "=";
    public static final String MEDIA_TYPE_MULTIPART_FORM_DATA = "multipart/form-data";
    public static final String MEDIA_TYPE_MULTIPART_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final String REQUEST_ID = "requestId";
    private static final Collection<String> defaultPasswordKeys = Arrays.asList("password", "secret");
    private static final int MAX_BYTES_TO_WRITE = 10240;
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssX";

    private AtomicLong id = new AtomicLong(1);

    @Autowired
    ConfigurationService configurationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long requestTime = System.currentTimeMillis();
        ContentCachingRequestWrapper contentCachingRequestWrapper = new ContentCachingRequestWrapper(request);
        if (logger.isDebugEnabled()) {
            long requestId = this.id.incrementAndGet();
            contentCachingRequestWrapper.setAttribute(REQUEST_ID, requestId);
            response = new ResponseWrapper(requestId, response);
        }
        try {
            logRequest(contentCachingRequestWrapper);
            filterChain.doFilter(contentCachingRequestWrapper, response);
        } finally {
            long responseTime = System.currentTimeMillis();
            final String maxRequestTimeString = this.getServletContext().getInitParameter("maxRequestTimeInMillis");
            final long maxRequestTime = StringUtils.isNotBlank(maxRequestTimeString) ?
                    Long.parseLong(maxRequestTimeString) :
                    DEFAULT_MAX_REQUEST_TIME;
            final long duration = responseTime - requestTime;
            if (duration > maxRequestTime) {
                logger.warn("TOO LONG REQUEST url: {} duration: {} MSECS",
                        getRequestLogMessage(contentCachingRequestWrapper), duration);
            }

            if (logger.isDebugEnabled()) {
                RequestResponseBean bean = createRequestResponseBean(contentCachingRequestWrapper, requestTime, (ResponseWrapper) response, responseTime);
                if (logger.isDebugEnabled()) {
                    logResponse(bean);
                }
            }
        }
    }

    private RequestResponseBean createRequestResponseBean(ContentCachingRequestWrapper request, long requestTime, ResponseWrapper response, long responseTime) {
        RequestResponseBean rrBean = new RequestResponseBean();
        rrBean.setRequestId((long) request.getAttribute(REQUEST_ID));
        rrBean.setRequestMethod(request.getMethod());
        FastDateFormat instance = FastDateFormat.getInstance(ISO_DATE_FORMAT, TimeZone.getTimeZone("GMT"), null);
        rrBean.setRequestTime(instance.format(new Date(System.currentTimeMillis())));
        rrBean.setRequestURI(request.getRequestURL().toString());
        rrBean.setUserId(configurationService.getCurrentUserName());
        rrBean.setIp(getExternalIP());
        rrBean.setThread(Thread.currentThread().getName());
        rrBean.setResponseTime(responseTime);
        rrBean.setDuration(responseTime - requestTime);
        if (request.getQueryString() != null) {
            rrBean.setRequestQueryParams(getQueryParams(request.getQueryString()));
        }
        Map<String, String> headers = processHeaders(request);
        rrBean.setRequestHeaders(headers);

        // Add request body if present.
        if (!isMultipart(request)) {
            Optional.of(getRequestPayload(request))
                    .filter(StringUtils::isNoneEmpty)
                    .ifPresent(rrBean::setRequestPostData);
        }
        // Add response
        rrBean.setResponseHttpCode(response.getStatus());
        try {
            byte[] responseBytes = response.toByteArray();
            if (responseBytes.length > 0) {
                String responseStr = new String(response.toByteArray(), response.getCharacterEncoding());

                if (responseStr.startsWith("{\"code\":")) {
                    String str = responseStr.substring(8, 14).split(",")[0];
                    rrBean.setResponseCode(Integer.parseInt(str));
                }
                //if trace is not enabled, restrict the size of the response to log
                if (!logger.isTraceEnabled() &&
                        StringUtils.length(responseStr) > MAX_BYTES_TO_WRITE) {
                    responseStr = StringUtils.substring(responseStr, 0, MAX_BYTES_TO_WRITE);
                }
                responseStr = PasswordHideUtil.hidePassword(responseStr);
                rrBean.setResponseData(responseStr);
            }
        } catch (UnsupportedEncodingException e) {
            logger.debug("Unable to parse response payload - {}", e.getMessage());
        }
        rrBean.setResponseTime(responseTime);
        return rrBean;
    }



    private void logRequest(ContentCachingRequestWrapper request) {
        if (logger.isDebugEnabled()) {
            logger.debug("===========================BEGIN API - request begin=======================================");
            try {
                logger.debug("Method      : {}", request.getMethod());
                logger.debug("URI         : {}", request.getRequestURI());
                logger.debug("Headers     : {}", processHeaders(request));
                if (StringUtils.isNotEmpty(request.getQueryString())) {
                    logger.debug("Query params: {}", getQueryParams(request.getQueryString()));
                }
                logger.debug("Request id  : {}", request.getAttribute(REQUEST_ID));
                String requestBody = getRequestPayload(request);
                if (StringUtils.isNotEmpty(requestBody)) {
                    logger.debug("Request body: {}", requestBody);
                }
            } catch (RuntimeException e) {
                logger.error("An exception occured during logging request.", e);
            }
            logger.debug("==========================request end=======================================================");

        }
    }

    private void logResponse(RequestResponseBean bean) {
        logger.debug("============================response begin==================================================");
        try {
            logger.debug("HTTP code  : {}", bean.getResponseHttpCode());
            logger.debug("URI         : {}", bean.getRequestURI());
            logger.debug("Request id   : {}", bean.getRequestId() % 10000);
            logger.debug("Response body: {}", bean.getResponseData());
            String duration = DurationFormatUtils.formatDurationHMS(bean.getDuration());
            logger.debug("Duration: {}", duration);
        } catch (Exception e) {
            logger.warn("An exception occured during logging response.", e);
        }
        logger.debug("=======================END API - response end==============================================");
    }

    private Map<String, String> getQueryParams(String queryStr) {
        String[] queryParams = queryStr.split(QUERY_PARAM_DELIMITER);
        Map<String, String> queryParamsMap = new HashMap<>();
        for (String queryParam : queryParams) {
            String[] keyValue = queryParam.split(QUERY_PARAM_VALUE_DELIMITER, 2);
            final String key = keyValue[0];
            final String value = StringUtils.contains(key, PASSWORD_KEY) ?
                    PASSWORD_MASK :
                    keyValue.length > 1 ?
                            keyValue[1] :
                            null;

            queryParamsMap.put(key, value);
        }
        return queryParamsMap;
    }

    /**
     * Generates the headers to be logged.
     */
    private Map<String, String> processHeaders(HttpServletRequest request) {
        // print all the headers
        Map<String, String> headers = new HashMap<>();

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            if (StringUtils.contains(headerName, PASSWORD_KEY)) {
                //do not log passwords
                headerValue = PASSWORD_MASK;
            }
            headers.put(headerName, headerValue);
        }
        headers.put("request-ip", getRemoteIpAddr(request));
        return headers;
    }

    private boolean isMultipart(final HttpServletRequest request) {
        return request.getContentType() != null &&
                request.getContentType().startsWith(MEDIA_TYPE_MULTIPART_FORM_DATA);
    }

    private boolean isJSON(final HttpServletRequest request) {
        return request.getContentType() != null
                && request.getContentType().toLowerCase().contains("json");
    }

    private String getRequestPayload(ContentCachingRequestWrapper request) {
        if(isMultiPartUrlEncoded(request)){
            return getBodyParams(request);
        } else {
            try {
                String charEncoding = getCharEncoding(request.getCharacterEncoding());
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(request.getContentAsByteArray());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(byteArrayInputStream));
                String requestPayload = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
                byte[] bytes = requestPayload.getBytes();
                byte[] bytesToCopy = bytes;
                if (bytes.length > MAX_BYTES_TO_WRITE) {
                    bytesToCopy = Arrays.copyOf(bytes, MAX_BYTES_TO_WRITE);
                }
                if (bytes.length > 0) {
                    requestPayload = new String(bytesToCopy, charEncoding);
                    if (isJSON(request)) {
                        return PasswordHideUtil.hidePassword(requestPayload);
                    } else if (StringUtils.contains(requestPayload, PASSWORD_KEY)) {
                        //do not log passwords
                        return PASSWORD_MASK;
                    }
                    return requestPayload;
                }

            } catch (IOException e) {
                logger.warn("Failed to parse request payload", e);
            }
        }
        return StringUtils.EMPTY;
    }

    private String getBodyParams(final ContentCachingRequestWrapper request) {
        StringBuilder stringBuilder = new StringBuilder("[");
        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String name = (String)e.nextElement();
            String value = request.getParameter(name);
            stringBuilder.append(name);
            stringBuilder.append(":");
            if (defaultPasswordKeys.contains(name)) {
                stringBuilder.append(PASSWORD_MASK);
            } else {
                stringBuilder.append(value);
            }
            if (e.hasMoreElements()) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public static boolean isMultiPartUrlEncoded(final HttpServletRequest request){
        return request.getContentType() != null && request.getContentType().startsWith(MEDIA_TYPE_MULTIPART_FORM_URLENCODED);
    }

    private String getCharEncoding(String characterEncoding) {
        return characterEncoding != null ? characterEncoding : "UTF-8";
    }

    private String getRequestLogMessage(ContentCachingRequestWrapper request) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("REQUEST ");
        stringBuilder.append("method=[").append(request.getMethod()).append("] ");
        stringBuilder.append("path=[").append(request.getRequestURI()).append("] ");
        stringBuilder.append("headers=[").append(processHeaders(request)).append("] ");

        Optional.of(getRequestPayload(request))
                .filter(StringUtils::isNotEmpty)
                .ifPresent(payload -> stringBuilder.append("payload=[").append(payload).append("] "));

        Optional.ofNullable(request.getQueryString())
                .map(this::getQueryParams)
                .ifPresent(queryParams -> stringBuilder.append("queryParams=[").append(queryParams).append("] "));

        return stringBuilder.toString();
    }

    public static String getRemoteIpAddr(final HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isEmpty(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        } else {
            return ip;
        }
        if (StringUtils.isEmpty(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        } else {
            return ip;
        }
        if (StringUtils.isEmpty(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        } else {
            return ip;
        }
        if (StringUtils.isEmpty(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        } else {
            return ip;
        }
        if (StringUtils.isEmpty(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private static String getExternalIP() {
        URL url = null;
        try {
            url = new URL("https://httpbin.org/ip");
        } catch (MalformedURLException e) {
            logger.error("MalformedURLException while creating URL ",e);
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            logger.error("IOException while opening connection ",e);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            int startIndex = response.indexOf("\"origin\":\"") + 10;
            int endIndex = response.indexOf("\"", startIndex);

            return response.substring(startIndex, endIndex);
        } catch (IOException e) {
            logger.warn("IOException when reading line ", e);
        } finally {
            connection.disconnect();
        }
        return null;
    }



}
