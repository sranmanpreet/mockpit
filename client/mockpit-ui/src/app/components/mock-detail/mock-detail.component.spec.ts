import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MockDetailComponent } from './mock-detail.component';

describe('MockDetailComponent', () => {
  let component: MockDetailComponent;
  let fixture: ComponentFixture<MockDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MockDetailComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MockDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
