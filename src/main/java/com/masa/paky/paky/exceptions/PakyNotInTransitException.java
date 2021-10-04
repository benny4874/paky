package com.masa.paky.paky.exceptions;

import static java.lang.String.format;

public class PakyNotInTransitException extends RuntimeException {
    public PakyNotInTransitException(String action, String pakyId, String step) {
        super(
                format("Paky %s is in status %s instead of intransit: impossible to %s"
                        , pakyId,
                        step,
                        action
                )
        );
    }
}
