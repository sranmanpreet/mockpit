import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { AuthConfigComponent } from './auth-config.component';

describe('AuthConfigComponent', () => {
  let component: AuthConfigComponent;
  let fixture: ComponentFixture<AuthConfigComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormsModule],
      declarations: [AuthConfigComponent],
    }).compileComponents();
    fixture = TestBed.createComponent(AuthConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('initialises to NONE', () => {
    expect(component.type).toBe('NONE');
  });

  it('emits a payload when scheme switches to BASIC', done => {
    component.configChange.subscribe(payload => {
      expect(payload.authConfig.type).toBe('BASIC');
      done();
    });
    component.type = 'BASIC';
    component.onTypeChange();
  });

  it('emits a JWT payload when toggled', done => {
    component.configChange.subscribe(payload => {
      expect(payload.authConfig.type).toBe('JWT');
      done();
    });
    component.type = 'JWT';
    component.onTypeChange();
  });

  it('emits a parsed Authorization header on test', done => {
    component.testAuth.subscribe(({ headers }) => {
      expect(headers['Authorization']).toBe('Bearer abc');
      done();
    });
    component.testHeader = 'Authorization: Bearer abc';
    component.onTest();
  });
});
