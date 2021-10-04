package com.masa.endpoint.machinery.beans;

import com.masa.paky.machinery.entity.Machinery;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;


@Getter
@Setter
@NoArgsConstructor
public class MachineryDataAnswer extends MachineryAnswer implements Serializable {
    private Machinery machinery;

    public MachineryDataAnswer(String returnMessage, Machinery machinery) {
        super(returnMessage);
        this.machinery = machinery;
    }
}
