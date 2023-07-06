import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ConfigService } from './config.service';
import { MockResponse } from '../models/mock/mock.model';

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

  public deleteMockById(id: number){
    return this.http.delete(this.backendUrl + "/native/api/mocks/"+id);
  }

  public deleteAllMocks(){
    return this.http.delete(this.backendUrl + "/native/api/mocks");
  }

}
