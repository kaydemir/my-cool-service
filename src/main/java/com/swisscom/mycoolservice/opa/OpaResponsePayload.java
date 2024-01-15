package com.swisscom.mycoolservice.opa;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE, force = true)
public class OpaResponsePayload implements Serializable {
    private boolean result;
}
