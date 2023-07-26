import { AfterViewInit, ViewChild, Component, OnDestroy, OnInit } from '@angular/core';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';

import { ToastrService } from 'ngx-toastr';
import { Observable } from 'rxjs';

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
export class MockListComponent implements OnInit, OnDestroy, AfterViewInit {
  mocks: Array<Mock> = [];
  isLoading: boolean = false;
  searchResults$?: Observable<Mock[]>;

  displayedColumns: string[] = ['name', 'description', 'method', 'path', 'action'];
  dataSource: any;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  length = 0;
  pageSize = 10;
  pageIndex = 0;
  pageSizeOptions = [10, 25, 50, 100];

  hidePageSize = false;
  showPageSizeOptions = true;
  showFirstLastButtons = true;
  disabled = false;

  pageEvent!: PageEvent;

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }

  constructor(public mockService: MockService, private toast: ToastrService) { 
    this.dataSource = new MatTableDataSource<Mock>(this.mocks);
  }
 
  ngOnInit(): void {
    this.initializeMocks(this.pageIndex, this.pageSize);
  }

  handlePageEvent(e: PageEvent) {
    console.log(e);
    this.pageEvent = e;
    this.length = e.length;
    this.pageSize = e.pageSize;
    this.pageIndex = e.pageIndex;
    this.initializeMocks(this.pageIndex, this.pageSize);
  }

  setPageSizeOptions(setPageSizeOptionsInput: string) {
    if (setPageSizeOptionsInput) {
      this.pageSizeOptions = setPageSizeOptionsInput.split(',').map(str => +str);
    }
  }


  initializeMocks(pageNo?: number, pageSize?:number) {
    this.isLoading = true;
    this.mockService.getMocks(pageNo, pageSize).subscribe((response: MockResponse) => {
      this.mocks = response.data.content;
      this.dataSource = new MatTableDataSource<Mock>(this.mocks);
      this.length = response.data.totalElements;
      this.isLoading = false;
    });
  }

  setSearchResults(mocks: Observable<Mock[]>){
    console.log(mocks);
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