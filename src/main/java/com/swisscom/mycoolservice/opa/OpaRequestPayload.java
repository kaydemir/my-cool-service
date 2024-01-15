package com.swisscom.mycoolservice.opa;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE, force = true)
public class OpaRequestPayload implements Serializable {
    private String requestBody;
}
