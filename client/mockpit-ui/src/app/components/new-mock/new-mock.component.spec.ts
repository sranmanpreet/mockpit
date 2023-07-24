import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewMockComponent } from './new-mock.component';

describe('NewMockComponent', () => {
  let component: NewMockComponent;
  let fixture: ComponentFixture<NewMockComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NewMockComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NewMockComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
