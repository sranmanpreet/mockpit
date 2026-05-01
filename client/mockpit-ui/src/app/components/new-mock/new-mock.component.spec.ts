import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ToastrService } from 'ngx-toastr';

import { ConfigService } from 'src/app/services/config.service';
import { MockService } from 'src/app/services/mock.service';
import { NewMockComponent } from './new-mock.component';

describe('NewMockComponent', () => {
  let component: NewMockComponent;
  let fixture: ComponentFixture<NewMockComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NewMockComponent],
      imports: [ReactiveFormsModule],
      providers: [
        { provide: MockService, useValue: { saveMock: () => of({ data: { id: 1 } }) } },
        { provide: ConfigService, useValue: { getConfig: () => ({}), getBackendUrl: () => '' } },
        { provide: ToastrService, useValue: { success: () => undefined, error: () => undefined } },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: new Map() } } },
        provideRouter([]),
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(NewMockComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
