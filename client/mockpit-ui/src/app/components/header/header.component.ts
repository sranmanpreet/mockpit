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

}
