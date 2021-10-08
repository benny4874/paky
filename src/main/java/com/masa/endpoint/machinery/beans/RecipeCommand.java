package com.masa.endpoint.machinery.beans;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RecipeCommand implements Serializable {
  private float quantity;
  private String description;
  private String unit;
  private String brand;
  private Date expiration;
  private String label;
}
