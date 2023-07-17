import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { ConfigService } from './config.service';
import { Mock, MockResponse } from '../models/mock/mock.model';

@Injectable({
  providedIn: 'root'
})
export class MockService {
  backendUrl: string = "";

  constructor(private http: HttpClient, private configService: ConfigService){
    this.backendUrl = this.configService.getConfig().backendUrl;
  }
  
  public getMocks() : Observable<any> {
      return this.http.get(this.backendUrl + "/native/api/mocks");
  }

  public getMockById(id: number | string | null): Observable<any> {
    return this.http.get(this.backendUrl + "/native/api/mocks/" +id);
  }

  public saveMock(mock:Mock) : Observable<MockResponse>{
    return this.http.post(this.backendUrl + "/native/api/mocks", mock) as Observable<MockResponse>;
  }

  public deleteMockById(id?: number){
    if(id){
      return this.http.delete(this.backendUrl + "/native/api/mocks/"+id);
    } else {
      return of("Mock id not provided");
    }
  }

  public deleteAllMocks(){
    return this.http.delete(this.backendUrl + "/native/api/mocks");
  }

}
