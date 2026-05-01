import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

/**
 * Allows the route only when an authenticated session exists. If /auth/me has not been called yet
 * (e.g. on a hard refresh) we trigger it now and gate on the result; this avoids flashing the
 * protected page before the redirect.
 */
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {}

  canActivate(): Observable<boolean | UrlTree> {
    if (this.auth.isLoggedIn()) {
      return of(true);
    }
    return this.auth.refresh().pipe(
      map(user => (user ? true : this.router.createUrlTree(['/login']))),
      catchError(() => of(this.router.createUrlTree(['/login']))),
    );
  }
}
