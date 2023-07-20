import { DomSanitizer } from '@angular/platform-browser';
import { Component, ViewChild } from '@angular/core';
import { Renderer2 } from '@angular/core'

import { ToastrService } from 'ngx-toastr';

import { MockService } from 'src/app/services/mock.service';
import { HttpResponse } from '@angular/common/http';
import { MockResponse } from 'src/app/models/mock/mock.model';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {

  @ViewChild('fileInput') fileInput: any;
  selectedFile : Blob = new Blob();

  constructor(private mockService: MockService, private toast: ToastrService, private sanitizer: DomSanitizer, private renderer: Renderer2) {

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
    if(this.selectedFile)
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
}
