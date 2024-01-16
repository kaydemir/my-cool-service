package com.swisscom.mycoolservice.opa;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Represents the payload structure for an Open Policy Agent (OPA) allow authorization response.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE, force = true)
public class OpaResponsePayload implements Serializable {
    /** boolean response whether OPA auth is allowed or not */
    private boolean result;
}
