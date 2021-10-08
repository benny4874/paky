package com.masa.paky.machinery.entity;

import com.masa.paky.recipe.entity.Recipe;
import java.util.Optional;

public interface MachineryRepository {

  void save(Machinery machinery);

  void update(Machinery machinery);

  Optional<Machinery> findById(String id);

  Optional<Recipe> findActualRecipeFor(String machineryId);
}
