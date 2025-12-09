package com.v.repository;

import java.util.List;

public interface BookImageRepository {
    List<String> findByBookId(Long bookId);
}
