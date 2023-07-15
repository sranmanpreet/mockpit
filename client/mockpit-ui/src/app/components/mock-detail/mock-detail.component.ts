import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, FormArray, Validators } from '@angular/forms';
import { ActivatedRoute, ActivatedRouteSnapshot } from '@angular/router';
import { Mock } from 'src/app/models/mock/mock.model';
import { MockService } from 'src/app/services/mock.service';

@Component({
  selector: 'app-mock-detail',
  templateUrl: './mock-detail.component.html',
  styleUrls: ['./mock-detail.component.scss'],
  host: {
    class: "container"
  }
})
export class MockDetailComponent implements OnInit {
  mock: Mock | undefined;

  mockForm: FormGroup;

  constructor(private route: ActivatedRoute, private mockService: MockService) {
    this.mockForm = new FormGroup({
      'name': new FormControl(null, Validators.required),
      'description': new FormControl(null),
      'route': new FormGroup({
        'path': new FormControl(null),
        'method': new FormControl(null)
      }),
      'responseHeaders': new FormArray(new Array()),
      'responseBody': new FormGroup({
        'type': new FormControl(null),
        'content': new FormControl(null),
        'contentType': new FormControl(null)
      }),
      'responseStatus': new FormGroup({
        'code': new FormControl(null)
      }),
      'active': new FormControl(null)

    });
  }

  ngOnInit(): void {
    this.mock = undefined;
    this.initialize();
  }

  initialize() {
    this.mockService.getMockById(this.route.snapshot.paramMap.get('id')).subscribe((response) => this.mock = response.data);
  }

  onSubmit(){
    console.log(this.mockForm);
  }
}
