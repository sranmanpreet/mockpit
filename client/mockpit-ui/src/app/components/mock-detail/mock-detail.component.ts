import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, ActivatedRouteSnapshot } from '@angular/router';
import { Mock } from 'src/app/models/mock/mock.model';
import { MockService } from 'src/app/services/mock.service';

@Component({
  selector: 'app-mock-detail',
  templateUrl: './mock-detail.component.html',
  styleUrls: ['./mock-detail.component.scss']
})
export class MockDetailComponent implements OnInit{
  mock: Mock | undefined;
  constructor(private route: ActivatedRoute, private mockService: MockService){

  }

  ngOnInit(): void {
      this.mock = undefined;
      this.initialize();
  }

  initialize(){
    this.mockService.getMockById(this.route.snapshot.paramMap.get('id')).subscribe((response)=> this.mock = response.data);
  }

}
