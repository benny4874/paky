package com.masa.paky.machinery.entity;

import io.micronaut.core.annotation.Introspected;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Introspected
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "machineryId")
public class Machinery implements Serializable {
    @Id
    String machineryId;
    String vendorId;
    String description;
    String recipeId;
}
