import { DomSanitizer } from '@angular/platform-browser';
import { Renderer2 } from '@angular/core'
import { AfterViewInit, ViewChild, Component, OnDestroy, OnInit } from '@angular/core';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';

import { ToastrService } from 'ngx-toastr';
import { Observable } from 'rxjs';

import { Mock, MockResponse } from 'src/app/models/mock/mock.model';
import { MockService } from 'src/app/services/mock.service';
import { HttpResponse } from '@angular/common/http';

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

  @ViewChild('fileInput') fileInput: any;
  selectedFile: Blob = new Blob();

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }

  constructor(private mockService: MockService, private toast: ToastrService, private sanitizer: DomSanitizer, private renderer: Renderer2) {
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


  initializeMocks(pageNo?: number, pageSize?: number) {
    this.isLoading = true;
    this.mockService.getMocks(pageNo, pageSize).subscribe((response: MockResponse) => {
      this.mocks = response.data.content;
      this.dataSource = new MatTableDataSource<Mock>(this.mocks);
      this.length = response.data.totalElements;
      this.isLoading = false;
    });
  }

  setMockList(mocks: Mock[]) {
    this.mocks = mocks;
    this.dataSource = new MatTableDataSource<Mock>(this.mocks);
    this.length = mocks.length;
    this.isLoading = false;
  }

  setSearchResults(mocks: Observable<Mock[]>) {
    mocks.subscribe(data => this.setMockList(data));
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

  onExport() {
    this.mockService.exportAllMocks().subscribe(
      (response: HttpResponse<any>) => {
        console.log(response);
        const file = new Blob([response.body], { type: 'application/octet-stream' });

        const fileUrl = URL.createObjectURL(file);
        const contentDisposition: string | undefined | null = response.headers.get('Content-Disposition');

        let fileName: string | undefined = '';
        if (contentDisposition && contentDisposition?.indexOf('"') > -1) {
          fileName = contentDisposition?.substring(contentDisposition.indexOf('filename') + 10, contentDisposition.length - 1);
        } else {
          fileName = contentDisposition?.substring(contentDisposition.indexOf('filename') + 9, contentDisposition.length - 1);
        }
        const link = this.renderer.createElement('a');
        link.setAttribute('target', '_blank');
        link.setAttribute('href', fileUrl);
        link.setAttribute('download', fileName);
        link.click();
        link.remove();
        console.log(fileUrl);
        this.toast.success("Mocks exported", "Success");
      }
    );
  }


  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length) {
      this.selectedFile = input.files[0];
      this.uploadFile();
    }
  }

  uploadFile() {
    const formData = new FormData();
    if (this.selectedFile)
      formData.append('file', this.selectedFile);

    this.mockService.importMocks(formData).subscribe(
      (response: MockResponse) => {
        this.toast.success(response.message, 'Success');
      },
      (error) => {
        console.log(error);
        this.toast.error(error.error.message, 'Error');
      }
    );
  }

  ngOnDestroy(): void {

  }
}