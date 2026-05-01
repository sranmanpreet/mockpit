import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ShellComponent } from './components/shell/shell.component';
import { HomeComponent } from './components/home/home.component';
import { MockListComponent } from './components/mock-list/mock-list.component';
import { MockDetailComponent } from './components/mock-detail/mock-detail.component';
import { NewMockComponent } from './components/new-mock/new-mock.component';
import { LoginComponent } from './components/login/login.component';
import { SignupComponent } from './components/signup/signup.component';
import { AuthGuard } from './guards/auth.guard';

const routes: Routes = [
  {
    path: '', component: ShellComponent, children: [
      { path: '', component: HomeComponent },
      { path: 'login', component: LoginComponent },
      { path: 'signup', component: SignupComponent },
      { path: 'manage', component: MockListComponent, canActivate: [AuthGuard] },
      { path: 'mock/new', component: NewMockComponent, canActivate: [AuthGuard] },
      { path: 'mock/:id', component: MockDetailComponent, canActivate: [AuthGuard] },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule],
})
export class AppRoutingModule {}
