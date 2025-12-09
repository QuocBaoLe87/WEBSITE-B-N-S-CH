package com.v.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AnnouncementOncePerLoginInterceptor implements HandlerInterceptor {
  @Override
  public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
    // Reserved for future announcement logic
    return true;
  }
}
