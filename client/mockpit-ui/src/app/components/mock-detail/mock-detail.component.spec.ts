import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ToastrService } from 'ngx-toastr';

import { ConfigService } from 'src/app/services/config.service';
import { MockService } from 'src/app/services/mock.service';
import { MockDetailComponent } from './mock-detail.component';

describe('MockDetailComponent', () => {
  let component: MockDetailComponent;
  let fixture: ComponentFixture<MockDetailComponent>;

  beforeEach(async () => {
    const mockServiceStub = {
      getMockById: () => of({ data: {} }),
      saveMock: () => of({ data: { id: 1 } }),
      deleteMockById: () => of(null),
    };

    await TestBed.configureTestingModule({
      declarations: [MockDetailComponent],
      imports: [ReactiveFormsModule],
      providers: [
        { provide: MockService, useValue: mockServiceStub },
        { provide: ConfigService, useValue: { getConfig: () => ({}), getBackendUrl: () => '' } },
        { provide: ToastrService, useValue: { success: () => undefined, error: () => undefined } },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: new Map([['id', '1']]) } },
        },
        provideRouter([]),
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(MockDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
