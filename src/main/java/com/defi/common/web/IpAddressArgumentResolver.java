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
 * {@code IpAddressArgumentResolver} resolves method parameters annotated with
 * {@link IpAddress}.
 * This resolver extracts the client's IP address from HTTP requests and injects
 * it into controller methods.
 *
 * <p>
 * The IP address resolution follows this priority order:
 * </p>
 * <ol>
 * <li>X-Forwarded-For header (commonly used by proxies and load balancers)</li>
 * <li>Remote address from the HTTP request</li>
 * <li>"unknown" if no valid IP address can be determined</li>
 * </ol>
 *
 * <p>
 * This resolver only supports parameters of type {@link String} that are
 * annotated with {@link IpAddress}.
 * </p>
 */
@Component
public class IpAddressArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * Determines if this resolver supports the given method parameter.
     * Returns {@code true} only for {@link String} parameters annotated with
     * {@link IpAddress}.
     *
     * @param parameter the method parameter to check
     * @return {@code true} if the parameter is supported, {@code false} otherwise
     */
    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.hasParameterAnnotation(IpAddress.class)
                && parameter.getParameterType().equals(String.class);
    }

    /**
     * Resolves the client's IP address from the HTTP request.
     * First attempts to extract from X-Forwarded-For header, then falls back to
     * remote address.
     *
     * @param parameter     the method parameter being resolved
     * @param mavContainer  the model and view container (not used)
     * @param webRequest    the current web request
     * @param binderFactory the data binder factory (not used)
     * @return the resolved IP address as a string, or "unknown" if not determinable
     */
    @Override
    @NonNull
    public Object resolveArgument(
            @NonNull MethodParameter parameter,
            @NonNull ModelAndViewContainer mavContainer,
            @NonNull NativeWebRequest webRequest,
            @NonNull WebDataBinderFactory binderFactory) {
        HttpServletRequest req = webRequest.getNativeRequest(HttpServletRequest.class);
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getRemoteAddr();
        }
        if (ip == null || ip.isEmpty())
            ip = "unknown";
        return ip;
    }
}
