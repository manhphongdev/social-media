package vn.socialmedia.common.utils.message;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Utility class for retrieving localized messages from resource bundles.
 * <p>
 * This class loads messages from a {@code messages.properties} file (and its locale-specific variants)
 * using {@link ResourceBundle}. It also supports argument formatting via
 * {@link MessageFormatter#arrayFormat(String, Object[])}.
 * <p>
 */
@Slf4j
public abstract class MessageUtils {

    /**
     * Base name for message resource bundles (without locale suffix).
     */
    private static final String BASE_NAME = "messages";

    /**
     * Retrieve a message by its code for the given locale and optional arguments.
     *
     * @param code   the message key in {@code messages.properties}
     * @param locale the locale to use for message lookup
     * @param args   optional arguments to be formatted into the message
     * @return the localized and formatted message, or the code itself if not found
     */
    private static String getMessage(String code, Locale locale, Object... args) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BASE_NAME, locale);
        String message;
        try {
            message = resourceBundle.getString(code);
        } catch (Exception ex) {
            log.debug(ex.getMessage(), ex);
            message = code;
        }
        return MessageFormatter.arrayFormat(message, args).getMessage();
    }

    /**
     * Retrieve a message by its code using the current locale from {@link LocaleContextHolder}.
     *
     * @param code the message key in {@code messages.properties}
     * @return the localized message, or the code itself if not found
     */
    public static String getMessage(String code) {
        return getMessage(code, LocaleContextHolder.getLocale());
    }

    /**
     * Retrieve a message by its code with arguments, using the current locale from {@link LocaleContextHolder}.
     *
     * @param code the message key in {@code messages.properties}
     * @param args optional arguments to be formatted into the message
     * @return the localized and formatted message, or the code itself if not found
     */
    public static String getMessage(String code, Object... args) {
        return getMessage(code, LocaleContextHolder.getLocale(), args);
    }
}
