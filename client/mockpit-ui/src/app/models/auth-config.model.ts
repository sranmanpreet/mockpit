export type AuthType = 'NONE' | 'BASIC' | 'JWT' | 'OAUTH2_RS' | 'OAUTH2_INTROSPECT';

export interface NoneAuthConfig {
  type: 'NONE';
}

export interface BasicAuthConfig {
  type: 'BASIC';
  username: string;
  password?: string;
  realm?: string;
}

export interface JwtAuthConfig {
  type: 'JWT';
  algorithm: string;
  sharedSecret?: string;
  publicKeyPem?: string;
  jwksUri?: string;
  requiredIssuer?: string;
  requiredAudiences?: string[];
  requiredScopes?: string[];
  requiredClaims?: { [key: string]: string };
  clockSkewSeconds?: number;
  headerName?: string;
  tokenPrefix?: string;
}

export interface OAuth2RsAuthConfig {
  type: 'OAUTH2_RS';
  issuer: string;
  jwksUri?: string;
  audiences?: string[];
  scopes?: string[];
  clockSkewSeconds?: number;
}

export interface OAuth2IntrospectAuthConfig {
  type: 'OAUTH2_INTROSPECT';
  introspectionUri: string;
  clientId: string;
  clientSecret?: string;
  requiredScopes?: string[];
  requiredAudiences?: string[];
  cacheTtlSeconds?: number;
}

export type AuthConfig =
  | NoneAuthConfig
  | BasicAuthConfig
  | JwtAuthConfig
  | OAuth2RsAuthConfig
  | OAuth2IntrospectAuthConfig;

export interface AuthFailureResponse {
  status?: number;
  body?: string;
  contentType?: string;
}
