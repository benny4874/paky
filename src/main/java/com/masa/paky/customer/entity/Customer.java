package com.masa.paky.customer.entity;

import com.masa.paky.Addressable;
import io.micronaut.core.annotation.Introspected;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@EqualsAndHashCode(of = "customerId")
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
