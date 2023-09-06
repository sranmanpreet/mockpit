import { Component, OnInit } from '@angular/core';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { Mock, MockResponse, ResponseHeader } from 'src/app/models/mock/mock.model';
import { ConfigService } from 'src/app/services/config.service';
import { MockService } from 'src/app/services/mock.service';
import { needConfirmation } from '../shared/confirmation-dialog/confirmation-dialog.decorator';

@Component({
  selector: 'app-new-mock',
  templateUrl: './new-mock.component.html',
  styleUrls: ['./new-mock.component.scss'],
  host: {
    class: "container"
  }
})
export class NewMockComponent implements OnInit {
  mockForm!: FormGroup;

  errorMessage?: string;

  constructor(private route: ActivatedRoute, private router: Router, private mockService: MockService, private toast: ToastrService, public configService: ConfigService) {
  }

  ngOnInit(): void {
    this.initializeForm(undefined);
  }

  initializeForm(mock: Mock | undefined) {
    this.mockForm = new FormGroup({
      'id': new FormControl(null),
      'name': new FormControl(null, Validators.required),
      'description': new FormControl(null),
      'route': new FormGroup({
        'path': new FormControl(null),
        'method': new FormControl(null)
      }),
      'responseHeaders': new FormArray(new Array()),
      'responseBody': new FormGroup({
        'type': new FormControl(null),
        'content': new FormControl(JSON.stringify({}, undefined, 4)),
        'contentType': new FormControl(null)
      }),
      'responseStatus': new FormGroup({
        'code': new FormControl(null)
      }),
      'inactive': new FormControl(null)
    });
  }

  onSubmit() {
    this.mockService.saveMock(this.mockForm.value).subscribe(
      (response: MockResponse) => {
        this.router.navigate(['/mock/'+response.data.id]);
        this.toast.success("Mock saved", "Success");
      },
      (error) => {
        this.toast.error(error.error.message?error.error.message: "Something went wrong while saving mock. Please contact administrator", "Error");
      });
  }

  @needConfirmation({
    title : "Confirmation",
    message : "Are you sure you want to discard unsaved data ?"
  })
  onCancel() {
    this.initializeForm(undefined);
  }

  getResponseHeaderControls() {
    return (this.mockForm.get('responseHeaders') as FormArray).controls;
  }

  addHeader(name?: string, value?: string) {
    (this.mockForm.get('responseHeaders') as FormArray).push(this.createHeader(name, value));
  }

  removeHeader(index: number) {
    const details = this.mockForm.get('responseHeaders') as FormArray;
    details.removeAt(index);
  }

  createHeader(name?: string, value?: string): FormGroup {
    return new FormGroup({
      name: new FormControl(name),
      value: new FormControl(value) 
    });
  }

  initializeResponseHeaderContorls(responseHeaders?: Array<ResponseHeader>){
    responseHeaders?.forEach(responseHeader => this.addHeader(responseHeader.name, responseHeader.value));
  }
}
