# Identity context and organization onboarding API

This contract is the backend boundary for the Angular identity and onboarding
feature. Both endpoints require an OAuth 2.0 Bearer access token issued by the
configured identity provider for the `opsflow-api` audience.

The backend obtains issuer and subject exclusively from the validated JWT. A
client must not send external identity, user IDs, membership roles, or access
tokens in a request body.

## Read the current identity context

```http
GET /api/identity/context
Authorization: Bearer <access-token>
```

An authenticated identity that does not yet have a local profile receives:

```json
{
  "status": "ONBOARDING_REQUIRED"
}
```

An onboarded identity receives:

```json
{
  "status": "ACTIVE",
  "userProfileId": "be25613d-ba47-4157-8c15-460735fb472c",
  "organizationId": "834bd497-6155-4752-8dc4-e22c1fc672b5",
  "organizationName": "OpsFlow Learning Organization",
  "membershipRole": "OWNER"
}
```

Both successful representations use `200 OK`. The `status` field is the
discriminator the frontend should use when narrowing the response shape.

## Complete organization onboarding

```http
POST /api/identity/onboarding
Authorization: Bearer <access-token>
Content-Type: application/json

{
  "organizationName": "OpsFlow Learning Organization"
}
```

`organizationName` is required, must contain a non-whitespace character, and
must not exceed 120 characters.

A successful request atomically creates the local user profile, organization,
and owner membership. It returns `201 Created` with the `ACTIVE` representation
shown above. Repeating onboarding for the same external identity returns
`409 Conflict` and does not create additional records.

## Error contract

Errors use `Content-Type: application/problem+json`. Alongside the standard
Problem Details fields, `code` is a stable machine-readable value. Client logic
should branch on the HTTP status and `code`, not on the human-readable `detail`.

| HTTP status | Code | Meaning |
| --- | --- | --- |
| `400` | `VALIDATION_ERROR` | One or more request fields are invalid. The response includes `fieldErrors`. |
| `400` | `MALFORMED_REQUEST` | The JSON request body cannot be read. |
| `401` | `AUTHENTICATION_REQUIRED` | A valid Bearer access token was not provided. |
| `403` | `ACCESS_DENIED` | The authenticated identity lacks authority for the requested resource. |
| `409` | `USER_ALREADY_ONBOARDED` | The external identity already has a local organization. |
| `500` | `INTERNAL_ERROR` | An unexpected server failure occurred; internal details are not exposed. |

Example:

```json
{
  "type": "about:blank",
  "title": "Onboarding conflict",
  "status": 409,
  "detail": "User has already completed organization onboarding",
  "instance": "/api/identity/onboarding",
  "code": "USER_ALREADY_ONBOARDED"
}
```

The API follows the Problem Details format defined by
[RFC 9457](https://www.rfc-editor.org/rfc/rfc9457.html).
