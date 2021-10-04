package com.masa.endpoint.machinery.beans;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MachineryAnswer implements Serializable {
    private String returnMessage;
}
