package com.masa.paky.vendor.entity;

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
@EqualsAndHashCode(of = "vendorId")
@Entity
@Introspected
@Getter
@Setter
public class Vendor implements Serializable, Addressable {
  @Id String vendorId;
  String idSso;

  @Override
  public String getAddress() {
    return "TMP_vendorAddress";
  }
}
