
import { ComponentType } from '@angular/cdk/portal';
import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ConfirmationDialogService {

  private static instance : ConfirmationDialogService | null= null;

  constructor(private matDialog: MatDialog) { 
    ConfirmationDialogService.instance = this;
  }
  public static getInstance(){
    return ConfirmationDialogService.instance;
  }


  openDialog<T>(data : any,component: ComponentType<T>) : Observable<boolean>{

    return this.matDialog.open(component,{
      data : data,
      disableClose: true,
    }).afterClosed();

  }
}