package com.v.service;

import java.util.List;
import java.util.Optional;

import com.v.model.Announcement;

public interface AnnouncementService {

  List<Announcement> findAll();

  Optional<Announcement> findById(Long id);

  Optional<Announcement> getActive();

  Announcement save(Announcement a);

  void delete(Long id);
}
