import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';

describe('AuthGuard', () => {
  let guard: AuthGuard;
  let auth: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    auth = jasmine.createSpyObj('AuthService', ['isLoggedIn', 'refresh']);
    router = jasmine.createSpyObj('Router', ['createUrlTree']);
    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: router },
      ],
    });
    guard = TestBed.inject(AuthGuard);
  });

  it('allows the route when already logged in', done => {
    auth.isLoggedIn.and.returnValue(true);
    (guard.canActivate() as any).subscribe((v: any) => {
      expect(v).toBeTrue();
      done();
    });
  });

  it('triggers /me when not logged in and allows on success', done => {
    auth.isLoggedIn.and.returnValue(false);
    auth.refresh.and.returnValue(of({ id: 1, email: 'a@b.c', displayName: 'A', role: 'USER', emailVerified: true }));
    (guard.canActivate() as any).subscribe((v: any) => {
      expect(v).toBeTrue();
      done();
    });
  });

  it('redirects to /login on refresh failure', done => {
    auth.isLoggedIn.and.returnValue(false);
    auth.refresh.and.returnValue(throwError(() => new Error('boom')));
    router.createUrlTree.and.returnValue('LOGIN_TREE' as any);
    (guard.canActivate() as any).subscribe((v: any) => {
      expect(v).toBe('LOGIN_TREE');
      expect(router.createUrlTree).toHaveBeenCalledWith(['/login']);
      done();
    });
  });
});
