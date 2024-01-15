package swisscom.auth

import rego.v1

default allow := false

allow if {
	input.method == "POST"
	"ROLE_ADMIN" in input.roles
}
