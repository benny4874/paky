package com.masa.paky.base.entity;


import io.micronaut.core.annotation.Introspected;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@NoArgsConstructor
@EqualsAndHashCode(of = "customerId")
@Entity
@Introspected
@Getter
@Setter
public class Base {
    @Id
    String baseId;
    String customerId;

}
