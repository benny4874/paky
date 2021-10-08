package com.masa.paky.machinery.entity;

import io.micronaut.core.annotation.Introspected;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "machineryId")
public class Machinery implements Serializable {
  @Id String machineryId;
  String vendorId;
  String description;
  String recipeId;
}
