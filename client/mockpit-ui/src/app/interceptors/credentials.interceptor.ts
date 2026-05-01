import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { ConfigService } from '../services/config.service';

/**
 * Adds withCredentials to backend requests so the JWT cookie is sent on cross-origin XHRs, and
 * routes 401 responses to the login page so users see a useful prompt instead of a silent failure.
 */
@Injectable()
export class CredentialsInterceptor implements HttpInterceptor {
  constructor(private auth: AuthService, private router: Router, private config: ConfigService) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const backend = this.config.getBackendUrl();
    const isBackend = backend && req.url.startsWith(backend);
    const enriched = isBackend ? req.clone({ withCredentials: true }) : req;

    return next.handle(enriched).pipe(
      catchError(err => {
        if (isBackend && err?.status === 401 && !req.url.includes('/auth/login') && !req.url.includes('/auth/me')) {
          this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
        }
        return throwError(() => err);
      }),
    );
  }
}
