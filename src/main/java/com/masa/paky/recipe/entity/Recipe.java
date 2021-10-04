package com.masa.paky.recipe.entity;

import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Introspected
@Getter
@Setter
public class Recipe implements Serializable {
    @Id
    private String recipeId;
    private float quantity;
    private String description;
    private String unit;
    private String brand;
    private Date expiration;
    private String label;

    @Override
    public boolean equals(Object obj) {
        try {
            if (obj == null) return false;
            return getRecipeId().equals(((Recipe) obj).getRecipeId());
        }catch (ClassCastException notSameClass){
            return false;
        }
    }
}
