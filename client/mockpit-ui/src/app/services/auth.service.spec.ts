import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { ConfigService } from './config.service';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;
  const fakeConfig = { backendUrl: 'http://localhost:8080' };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthService,
        {
          provide: ConfigService,
          useValue: {
            getBackendUrl: () => fakeConfig.backendUrl,
            getConfig: () => fakeConfig,
          },
        },
      ],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('login posts credentials and caches the user', () => {
    const user = { id: 1, email: 'a@b.c', displayName: 'A', role: 'USER', emailVerified: true };
    service.login('a@b.c', 'pwd-pwd-pwd-pwd').subscribe(u => expect(u).toEqual(user as any));
    const req = http.expectOne(`${fakeConfig.backendUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBeTrue();
    expect(req.request.body).toEqual({ email: 'a@b.c', password: 'pwd-pwd-pwd-pwd' });
    req.flush(user);
    expect(service.snapshot()).toEqual(user as any);
  });

  it('refresh nulls the user on 401', done => {
    service.refresh().subscribe(u => {
      expect(u).toBeNull();
      expect(service.snapshot()).toBeNull();
      done();
    });
    const req = http.expectOne(`${fakeConfig.backendUrl}/auth/me`);
    req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
  });

  it('logout clears the cached user', () => {
    service['_user$'].next({ id: 1, email: 'a@b.c', displayName: 'A', role: 'USER', emailVerified: true });
    service.logout().subscribe(() => expect(service.snapshot()).toBeNull());
    http.expectOne(`${fakeConfig.backendUrl}/auth/logout`).flush(null);
  });
});
