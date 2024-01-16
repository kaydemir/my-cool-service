package com.swisscom.mycoolservice.opa;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Represents the payload structure for an Open Policy Agent (OPA) allow authorization request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE, force = true)
public class OpaRequestPayload implements Serializable {
    /** The JSON-formatted request body for the OPA authorization request.*/
    private String requestBody;
}
