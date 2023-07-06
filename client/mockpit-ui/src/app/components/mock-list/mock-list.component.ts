import { HttpClient } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
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

  constructor(public mockService: MockService) {

  }

  ngOnInit(): void {
    this.initializeMocks();
  }

  initializeMocks(){
    this.mockService.getMocks().subscribe((response: MockResponse) => {
      this.mocks = response.data;
      console.log(response);
    });
  }

  deleteMock(id: number) {
    this.mockService.deleteMockById(id).subscribe(()=> this.initializeMocks());
  }

  ngOnDestroy(): void {

  }
}
