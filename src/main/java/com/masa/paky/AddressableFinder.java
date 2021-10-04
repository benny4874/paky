package com.masa.paky;

import java.io.Serializable;
import java.util.Optional;

public interface AddressableFinder<T extends Addressable,I extends Serializable> {
    Optional<T> findById(I id);
}
