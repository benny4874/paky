package com.masa.paky.base.entity;

import java.util.Optional;

public interface BaseRepository {
    Optional<Base> findById(String id);
    void save(Base base);
    void update(Base base);
}
