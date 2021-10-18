package com.masa.endpoint.base.beans;

import com.masa.paky.base.entity.Base;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BaseAnswer implements Serializable {
    private String message;
    private Base base;



    public BaseAnswer( Base base) {
        this.base = base;
        message="OK";
    }

    public BaseAnswer(String message) {
        this.message = message;
        base = null;
    }

}
