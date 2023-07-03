import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {

  private configUrl = 'assets/config/config.json';
  private config: any = {};

  getConfig() : any {
    return {backendUrl: "http://localhost:8080"}
  }

  constructor(private http: HttpClient) {
    
  }

  

  getConfigValue(key: string): any {
    return this.config[key];
  }
}
