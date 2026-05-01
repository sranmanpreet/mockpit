import { NgModule, APP_INITIALIZER, ENVIRONMENT_INITIALIZER, inject } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HttpClientXsrfModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatPaginatorIntl, MatPaginatorModule } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';

import { AppRoutingModule } from './app-routing.module';

import { AppComponent } from './app.component';
import { HomeComponent } from './components/home/home.component';
import { ShellComponent } from './components/shell/shell.component';
import { HeaderComponent } from './components/header/header.component';
import { MockListComponent } from './components/mock-list/mock-list.component';
import { MockDetailComponent } from './components/mock-detail/mock-detail.component';
import { ConfigService } from './services/config.service';
import { ToastrModule } from 'ngx-toastr';
import { NewMockComponent } from './components/new-mock/new-mock.component';
import { SearchComponent } from './components/search/search.component';
import { TruncatePipe } from './pipes/truncate.pipe';
import { FooterComponent } from './components/footer/footer.component';
import { APP_BASE_HREF, PlatformLocation } from '@angular/common';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ConfirmationDialogService } from './components/shared/confirmation-dialog/confirmation-dialog.service';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ConfirmationDialogComponent } from './components/shared/confirmation-dialog/confirmation-dialog/confirmation-dialog.component';

import { LoginComponent } from './components/login/login.component';
import { SignupComponent } from './components/signup/signup.component';
import { AuthConfigComponent } from './components/auth-config/auth-config.component';
import { CredentialsInterceptor } from './interceptors/credentials.interceptor';

export function initializeApp(configService: ConfigService): () => Promise<any> {
  return () => configService.loadConfig();
}

function CustomPaginator() {
  const customPaginatorIntl = new MatPaginatorIntl();
  customPaginatorIntl.itemsPerPageLabel = 'Mocks per page';
  return customPaginatorIntl;
}

function trimLastSlashFromUrl(baseUrl: string) {
  if (baseUrl == null || baseUrl == '') {
    return null;
  } else if (baseUrl[baseUrl.length - 1] == '/') {
    return baseUrl.substring(0, baseUrl.length - 1);
  }
  return null;
}

export function initializeDialogService() {
  return () => {
    inject(ConfirmationDialogService);
  };
}

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    ShellComponent,
    HeaderComponent,
    MockListComponent,
    MockDetailComponent,
    NewMockComponent,
    SearchComponent,
    TruncatePipe,
    FooterComponent,
    ConfirmationDialogComponent,
    LoginComponent,
    SignupComponent,
    AuthConfigComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    HttpClientXsrfModule.withOptions({
      cookieName: 'XSRF-TOKEN',
      headerName: 'X-XSRF-TOKEN',
    }),
    FormsModule,
    ReactiveFormsModule,
    MatIconModule,
    MatDialogModule,
    MatButtonModule,
    BrowserAnimationsModule,
    ToastrModule.forRoot({
      timeOut: 5000,
      positionClass: 'toast-bottom-right',
    }),
    MatTableModule,
    MatPaginatorModule,
  ],
  providers: [
    ConfigService,
    {
      provide: APP_INITIALIZER,
      useFactory: initializeApp,
      deps: [ConfigService],
      multi: true,
    },
    {
      provide: APP_BASE_HREF,
      useFactory: (s: PlatformLocation) => trimLastSlashFromUrl(s.getBaseHrefFromDOM()),
      deps: [PlatformLocation],
    },
    { provide: MatPaginatorIntl, useValue: CustomPaginator() },
    {
      provide: ENVIRONMENT_INITIALIZER,
      useFactory: initializeDialogService,
      deps: [MatDialog],
      multi: true,
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: CredentialsInterceptor,
      multi: true,
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
