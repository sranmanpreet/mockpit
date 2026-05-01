import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ToastrService } from 'ngx-toastr';

import { MockService } from 'src/app/services/mock.service';
import { SearchComponent } from './search.component';

describe('SearchComponent', () => {
  let component: SearchComponent;
  let fixture: ComponentFixture<SearchComponent>;

  beforeEach(async () => {
    const mockServiceStub = {
      search: () => of({ data: { content: [] } }),
    };
    const toastStub = { error: () => undefined };

    await TestBed.configureTestingModule({
      declarations: [SearchComponent],
      providers: [
        { provide: MockService, useValue: mockServiceStub },
        { provide: ToastrService, useValue: toastStub },
        provideRouter([]),
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(SearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
