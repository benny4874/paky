package com.masa.endpoint.machinery.beans;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MachineryAnswer implements Serializable {
  private String returnMessage;
}
