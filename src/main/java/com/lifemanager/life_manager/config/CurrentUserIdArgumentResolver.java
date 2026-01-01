package com.lifemanager.life_manager.config;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {


    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // @CurrentuserId 어노테이션이 있는 Long 타입 파라미터를 지원
        return parameter.hasParameterAnnotation(CurrentUserId.class)
                && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        // X-User-Id 헤더에서 값 추출
        String userIdHeader = webRequest.getHeader("X-User-Id");

        if (userIdHeader == null) {
            throw new IllegalArgumentException("X-User-Id 헤더가 필요합니다.");
        }

        return Long.parseLong(userIdHeader);
    }
}
