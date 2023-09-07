import { Component } from '@angular/core';
import { AppService } from 'src/app/services/app.service';

@Component({
  selector: 'app-shell',
  templateUrl: './shell.component.html',
  styleUrls: ['./shell.component.scss']
})
export class ShellComponent {

  version: string = "";

  constructor(private appService: AppService) {
    this.getAppVersion();
  }

  getAppVersion(){
    this.appService.getApplicationProperties().subscribe(properties => {
      this.version = properties.version;
    });
  }
}
