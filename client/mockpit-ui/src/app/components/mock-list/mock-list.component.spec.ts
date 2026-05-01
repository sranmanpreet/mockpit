import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { ToastrService } from 'ngx-toastr';

import { MockService } from 'src/app/services/mock.service';
import { MockListComponent } from './mock-list.component';

describe('MockListComponent', () => {
  let component: MockListComponent;
  let fixture: ComponentFixture<MockListComponent>;

  beforeEach(async () => {
    const mockServiceStub = {
      getMocks: () => of({ data: { content: [], totalElements: 0 } }),
      deleteAllMocks: () => of(null),
      exportAllMocks: () => of({}),
      importMocks: () => of({ data: {} }),
    };

    await TestBed.configureTestingModule({
      declarations: [MockListComponent],
      imports: [NoopAnimationsModule],
      providers: [
        { provide: MockService, useValue: mockServiceStub },
        { provide: ToastrService, useValue: { success: () => undefined, error: () => undefined } },
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: new Map() } } },
        provideRouter([]),
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(MockListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
