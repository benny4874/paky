package com.masa.paky.paky.entity;


import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class ErrorStatus {
    public static int OK = 0;
    public static int RECEIVED_BY_WRONG_VENDOR = 100;
    public static int DENT_TO_WRONG_VENDOR = 200;
}
