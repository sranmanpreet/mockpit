import { Component, OnDestroy, OnInit } from '@angular/core';

import { ToastrService } from 'ngx-toastr';

import { Mock, MockResponse } from 'src/app/models/mock/mock.model';
import { MockService } from 'src/app/services/mock.service';

@Component({
  selector: 'app-mock-list',
  templateUrl: './mock-list.component.html',
  styleUrls: ['./mock-list.component.scss'],
  host: {
    class: "container"
  }
})
export class MockListComponent implements OnInit, OnDestroy {
  mocks: Array<Mock> = [];

  constructor(public mockService: MockService, private toast: ToastrService) {

  }

  ngOnInit(): void {
    this.initializeMocks();
  }

  initializeMocks() {
    this.mockService.getMocks().subscribe((response: MockResponse) => {
      this.mocks = response.data;
      console.log(response);
    });
  }

  deleteMock(id: number) {
    this.mockService.deleteMockById(id).subscribe(
      (response) => {
        this.toast.success("Mock deleted.", "Success");
        this.initializeMocks()
      },
      (error) => { }
    );
  }

  ngOnDestroy(): void {

  }
}
