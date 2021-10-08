package com.masa.endpoint.machinery.beans;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MachineryCommand implements Serializable {
  String description;
  String vendorId;
  String MachineryId;
}
