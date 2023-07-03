import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MockListComponent } from './mock-list.component';

describe('MockListComponent', () => {
  let component: MockListComponent;
  let fixture: ComponentFixture<MockListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MockListComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MockListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
