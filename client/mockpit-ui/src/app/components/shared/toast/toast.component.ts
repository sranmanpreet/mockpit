import { Component } from '@angular/core';

@Component({
  selector: 'app-toast',
  templateUrl: './toast.component.html',
  styleUrls: ['./toast.component.scss']
})
export class ToastComponent {
    type: 'success' | 'warning' | 'error' = 'success';
    message: string = "";

    constructor(){
      
    }
}
