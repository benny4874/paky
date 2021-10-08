package com.masa.paky.paky.entity;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class ErrorStatus {
  public static int OK = 0;
  public static int RECEIVED_BY_WRONG_VENDOR = 100;
  public static int SENT_TO_WRONG_VENDOR = 200;
  public static int RECEIVED_BUT_NEVER_SENT = 300;
  public static int RECEIVED_BY_UNIDENTIFIED_VENDOR = 400;
  public static int FILLED_WITH_UNKNOWN_PRODUCT = 500;
}
