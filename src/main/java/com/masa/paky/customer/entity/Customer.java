package com.masa.paky.customer.entity;

import com.masa.paky.Addressable;
import io.micronaut.core.annotation.Introspected;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@NoArgsConstructor
@EqualsAndHashCode(of = "vendorId")
@Entity
@Introspected
@Getter
@Setter
public class Customer implements Serializable, Addressable {
  @Id String customerId;
  String idSso;

  @Override
  public String getAddress() {
    return "TMP_customerAddress";
  }
}
