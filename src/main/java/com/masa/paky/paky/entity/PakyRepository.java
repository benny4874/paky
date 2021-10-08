package com.masa.paky.paky.entity;

import java.util.Optional;

public interface PakyRepository {
  Paky save(Paky paky);

  void update(Paky paky);

  Optional<Paky> findById(String id);
}
