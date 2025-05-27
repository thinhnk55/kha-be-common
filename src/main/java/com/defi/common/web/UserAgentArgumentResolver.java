package com.defi.common.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@code UserAgentArgumentResolver} resolves method parameters annotated with
 * {@link UserAgent}.
 * This resolver extracts the User-Agent header from HTTP requests and injects
 * it into controller methods.
 *
 * <p>
 * The User-Agent string provides information about the client's browser,
 * operating system,
 * and device. This resolver extracts this information from the "User-Agent"
 * HTTP header.
 * </p>
 *
 * <p>
 * If the User-Agent header is missing or empty, the resolver returns "unknown".
 * </p>
 *
 * <p>
 * This resolver only supports parameters of type {@link String} that are
 * annotated with {@link UserAgent}.
 * </p>
 */
@Component
public class UserAgentArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * Determines if this resolver supports the given method parameter.
     * Returns {@code true} only for {@link String} parameters annotated with
     * {@link UserAgent}.
     *
     * @param parameter the method parameter to check
     * @return {@code true} if the parameter is supported, {@code false} otherwise
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UserAgent.class)
                && parameter.getParameterType().equals(String.class);
    }

    /**
     * Resolves the User-Agent string from the HTTP request header.
     * Extracts the "User-Agent" header value and returns it as a string.
     *
     * @param parameter     the method parameter being resolved
     * @param mavContainer  the model and view container (not used)
     * @param webRequest    the current web request
     * @param binderFactory the data binder factory (not used)
     * @return the User-Agent string, or "unknown" if the header is missing or empty
     */
    @Override
    @NonNull
    public Object resolveArgument(@NonNull MethodParameter parameter,
            @NonNull ModelAndViewContainer mavContainer,
            @NonNull NativeWebRequest webRequest,
            @NonNull WebDataBinderFactory binderFactory) {
        HttpServletRequest req = webRequest.getNativeRequest(HttpServletRequest.class);
        String ua = req.getHeader("User-Agent");
        if (ua == null || ua.isEmpty())
            ua = "unknown";
        return ua;
    }
}
