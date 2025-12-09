package com.v.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.v.model.Announcement;
import com.v.service.AnnouncementService;

@ControllerAdvice
public class GlobalModelAdvice {
  private final AnnouncementService svc;
  public GlobalModelAdvice(AnnouncementService svc){ this.svc = svc; }

  @ModelAttribute("activeAnn")
  public Announcement activeAnn(){ return svc.getActive().orElse(null); }
}
