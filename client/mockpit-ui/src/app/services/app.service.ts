import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ConfigService } from './config.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AppService {

  private backendUrl: string = "";
  private appProperties: any = undefined;

  constructor(private http: HttpClient, private configService: ConfigService){
    this.backendUrl = this.configService.getConfig().backendUrl;
    if(this.backendUrl.charAt(this.backendUrl.length-1) == '/'){
      this.backendUrl = this.backendUrl.slice(0, this.backendUrl.length-1);
    }
  }

  public getAppProperties(){
    if(this.appProperties != undefined){
      return this.appProperties;
    }
    
  }
  
  public getApplicationProperties() : Observable<any> {
    return this.http.get(this.backendUrl + "/native/app/properties") as Observable<any>;
  }

}
