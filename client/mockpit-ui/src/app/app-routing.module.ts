import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ShellComponent } from './components/shell/shell.component';
import { HomeComponent } from './components/home/home.component';
import { MockListComponent } from './components/mock-list/mock-list.component';
import { MockDetailComponent } from './components/mock-detail/mock-detail.component';

const routes: Routes = [
  {
    path: "", component: ShellComponent, children: [
      {
        path: '',
        component: HomeComponent
      },
      {
        path: 'manage',
        component: MockListComponent
      },
      {
        path: 'mock/new',
        component: MockDetailComponent
      },
      {
        path: 'mock/:id',
        component: MockDetailComponent
      }
    ],
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
