import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of, tap } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ConfigService } from './config.service';

export interface CurrentUser {
  id: number;
  email: string;
  displayName: string;
  role: 'USER' | 'ADMIN';
  emailVerified: boolean;
}

/**
 * Wraps the /auth REST surface and caches the current user. The session itself lives in an
 * HttpOnly cookie set by the server, so we never need to handle a token in JS - we just call
 * /auth/me on bootstrap to discover whether the visitor is logged in.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly _user$ = new BehaviorSubject<CurrentUser | null>(null);
  readonly user$: Observable<CurrentUser | null> = this._user$.asObservable();

  constructor(private http: HttpClient, private config: ConfigService) {}

  private url(path: string): string {
    return `${this.config.getBackendUrl()}/auth${path}`;
  }

  refresh(): Observable<CurrentUser | null> {
    return this.http.get<CurrentUser>(this.url('/me'), { withCredentials: true }).pipe(
      tap(u => this._user$.next(u)),
      catchError(() => {
        this._user$.next(null);
        return of(null);
      }),
    );
  }

  login(email: string, password: string): Observable<CurrentUser> {
    return this.http
      .post<CurrentUser>(this.url('/login'), { email, password }, { withCredentials: true })
      .pipe(tap(u => this._user$.next(u)));
  }

  signup(email: string, password: string, displayName?: string): Observable<CurrentUser> {
    return this.http
      .post<CurrentUser>(this.url('/signup'), { email, password, displayName }, { withCredentials: true })
      .pipe(tap(u => this._user$.next(u)));
  }

  logout(): Observable<void> {
    return this.http.post<void>(this.url('/logout'), {}, { withCredentials: true }).pipe(
      tap(() => this._user$.next(null)),
    );
  }

  fetchCsrf(): Observable<unknown> {
    return this.http.get(this.url('/csrf'), { withCredentials: true });
  }

  isLoggedIn(): boolean {
    return !!this._user$.value;
  }

  snapshot(): CurrentUser | null {
    return this._user$.value;
  }
}
