package com.masa.endpoint.machinery.beans;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MachineryCommand implements Serializable {
    String description;
    String vendorId;
    String MachineryId;
}
