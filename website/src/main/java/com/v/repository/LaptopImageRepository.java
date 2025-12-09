package com.v.repository;

import java.util.List;

public interface LaptopImageRepository {
    List<String> findByLaptopId(Long laptopId);
}