package com.masa.paky.machinery.entity;

import static java.util.Optional.ofNullable;

import com.masa.paky.recipe.entity.Recipe;
import io.micronaut.transaction.annotation.ReadOnly;
import io.micronaut.transaction.annotation.TransactionalAdvice;
import jakarta.inject.Singleton;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

@Singleton
public class MachineRepositoryImpl implements MachineryRepository {

  private final EntityManager entityManager;

  public MachineRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  @TransactionalAdvice
  public void save(Machinery machinery) {
    entityManager.persist(machinery);
  }

  @Override
  @TransactionalAdvice
  public void update(Machinery machinery) {
    entityManager.merge(machinery);
  }

  @Override
  @ReadOnly
  public Optional<Machinery> findById(String id) {
    return ofNullable(entityManager.find(Machinery.class, id));
  }

  @ReadOnly
  @Override
  public Optional<Recipe> findActualRecipeFor(String machineryId) {
    return ofNullable(retreiveSingle(getRecipeTypedQuery(machineryId)));
  }

  private TypedQuery<Recipe> getRecipeTypedQuery(String machineryId) {
    String sql =
        "select r from Recipe as r where r.recipeId = (select m.recipeId from Machinery as m where m.machineryId = :machineryId)";
    TypedQuery<Recipe> query = entityManager.createQuery(sql, Recipe.class);
    query.setParameter("machineryId", machineryId);
    return query;
  }

  private Recipe retreiveSingle(TypedQuery<Recipe> query) {
    try {
      return query.getSingleResult();
    } catch (NoResultException notFound) {
      return null;
    }
  }
}
