package com.masa.paky.recipe.entity;

import io.micronaut.transaction.annotation.ReadOnly;
import io.micronaut.transaction.annotation.TransactionalAdvice;
import jakarta.inject.Singleton;
import java.util.Optional;
import javax.persistence.EntityManager;

@Singleton
public class RecipeRepositoryImpl implements RecipeRepository {

  private final EntityManager entityManager;

  public RecipeRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  @ReadOnly
  public Optional<Recipe> findById(String id) {
    return Optional.ofNullable(entityManager.find(Recipe.class, id));
  }

  @Override
  @TransactionalAdvice
  public void save(Recipe recipe) {
    entityManager.persist(recipe);
  }
}
