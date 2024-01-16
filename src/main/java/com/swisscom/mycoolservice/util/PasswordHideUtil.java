package com.swisscom.mycoolservice.util;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** utility class to mask password on the logs for API requests */
public class PasswordHideUtil {

    protected static final Logger logger = LogManager.getLogger(PasswordHideUtil.class);

    private static final String PASSWORD_MASK = "****";

    private static final Collection<String> defaultPasswordKeys = Arrays.asList("password");

    private PasswordHideUtil() {}

    public static String hidePassword(final String text) {
        return hidePassword(text, defaultPasswordKeys);
    }

    public static String hidePassword(final String text, final Collection<String> passwordKeys) {
        if (CollectionUtils.isEmpty(passwordKeys)) {
            return text;
        }
        if (StringUtils.isBlank(text)) {
            return text;
        }
        String textWithoutPassword = text;
        for (String key : passwordKeys) {
            textWithoutPassword = hidePassword(textWithoutPassword, key);
        }
        return textWithoutPassword;
    }

    static String hidePassword(final String text, final String passwordKey) {
        if (!StringUtils.contains(text, passwordKey)) {
            return text;
        }

        final String jsonPasswordString = String.format("\"%s\":\"", passwordKey);
        final String jsonEndDelimeter = "\"";

        StringBuilder responseStr = replacePassword(text, jsonPasswordString, jsonEndDelimeter);
        responseStr = replacePassword(responseStr, passwordKey + "=", ",");
        responseStr = replacePassword(responseStr, passwordKey + ": \"", "\"");

        return responseStr.toString();
    }

    /**
     * @param text
     * @param passwordString
     * @param endDelimeter
     * @return
     */
    static StringBuilder replacePassword(
            final CharSequence text,
            final String passwordString,
            final String endDelimeter
    ) {
        StringBuilder responseStr = text instanceof StringBuilder ?
                (StringBuilder) text :
                new StringBuilder(text);

        // To return in case of failure
        StringBuilder inputString = new StringBuilder(responseStr);

        int passwordPos = 0;
        try {
            while ((passwordPos = responseStr.indexOf(passwordString, passwordPos)) > 0) {
                final int startPasswordPos = passwordPos + passwordString.length();
                final int endPasswordPos = responseStr.indexOf(endDelimeter, startPasswordPos);

                if (endPasswordPos < 0) {
                    logger.error("Could not find the delimiter '{}' on string '{}' from position '{}'",
                            endDelimeter, responseStr, startPasswordPos);
                    return inputString;
                }

                passwordPos += PASSWORD_MASK.length();
                // do not log passwords
                responseStr.replace(startPasswordPos, endPasswordPos, PASSWORD_MASK);
            }
        } catch (RuntimeException e) {
            logger.error("Error trying to replace passwords in {}. Message: {}", inputString, e.getMessage());
            logger.debug("StackTrace", e);
            return inputString;
        }

        return responseStr;
    }
}
