import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import {
  AuthConfig,
  AuthFailureResponse,
  AuthType,
} from '../../models/auth-config.model';

interface AuthConfigOutput {
  authConfig: AuthConfig;
  authFailure?: AuthFailureResponse;
}

/**
 * Form for the user-configurable per-mock auth scheme. Renders different controls per type and
 * emits a fully-formed AuthConfig payload upstream for inclusion in the mock save request.
 */
@Component({
  selector: 'app-auth-config',
  templateUrl: './auth-config.component.html',
  styleUrls: ['./auth-config.component.css'],
})
export class AuthConfigComponent implements OnChanges {
  @Input() authConfig: AuthConfig = { type: 'NONE' };
  @Input() authFailure?: AuthFailureResponse;
  @Output() configChange = new EventEmitter<AuthConfigOutput>();
  @Output() testAuth = new EventEmitter<{ headers: { [k: string]: string } }>();

  type: AuthType = 'NONE';
  config: AuthConfig = { type: 'NONE' };
  failure: AuthFailureResponse = { contentType: 'application/json' };
  testHeader = '';

  readonly typeOptions: { value: AuthType; label: string; description: string }[] = [
    { value: 'NONE', label: 'None', description: 'No authentication required.' },
    { value: 'BASIC', label: 'Basic', description: 'HTTP Basic auth (username + password).' },
    { value: 'JWT', label: 'JWT (Bearer)', description: 'Verify a JWT signed with HS / RS / ES.' },
    { value: 'OAUTH2_RS', label: 'OAuth2 Resource Server', description: 'Validate against an OIDC issuer.' },
    { value: 'OAUTH2_INTROSPECT', label: 'OAuth2 Introspection', description: 'RFC 7662 introspection endpoint.' },
  ];

  readonly jwtAlgs = ['HS256', 'HS384', 'HS512', 'RS256', 'RS384', 'RS512', 'ES256', 'ES384', 'ES512'];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['authConfig'] && this.authConfig) {
      this.type = this.authConfig.type || 'NONE';
      this.config = { ...this.authConfig };
    }
    if (changes['authFailure']) {
      this.failure = this.authFailure ? { ...this.authFailure } : { contentType: 'application/json' };
    }
  }

  onTypeChange(): void {
    this.config = { type: this.type } as AuthConfig;
    this.emit();
  }

  emit(): void {
    const payload: AuthConfigOutput = {
      authConfig: this.config,
      authFailure: this.failure?.status || this.failure?.body ? { ...this.failure } : undefined,
    };
    this.configChange.emit(payload);
  }

  onTest(): void {
    const headers: { [k: string]: string } = {};
    if (this.testHeader) {
      const idx = this.testHeader.indexOf(':');
      if (idx > 0) {
        headers[this.testHeader.substring(0, idx).trim()] = this.testHeader.substring(idx + 1).trim();
      }
    }
    this.testAuth.emit({ headers });
  }

  asBasic(): any { return this.config as any; }
  asJwt(): any { return this.config as any; }
  asRs(): any { return this.config as any; }
  asIntrospect(): any { return this.config as any; }
}
