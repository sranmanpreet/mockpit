import { APP_BASE_HREF } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Injectable, Inject } from '@angular/core';
import { Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {

  private configUrl = 'assets/config/config.json';
  private config: any;

  constructor(@Inject(APP_BASE_HREF) public baseHref: string, private http: HttpClient) {}

  loadConfig(): Promise<any> {
    return this.http
      .get(this.baseHref + "/" + this.configUrl)
      .toPromise()
      .then((config) => {
        this.config = config;
      })
      .catch((error) => {
        console.error('Error loading configuration:', error);
      });
  }

  getConfig(): any {
    return this.config;
  }

  getBackendUrl(): string {
    if (!this.config || !this.config.backendUrl) {
      return '';
    }
    let url: string = this.config.backendUrl;
    while (url.endsWith('/')) {
      url = url.substring(0, url.length - 1);
    }
    return url;
  }
}
	