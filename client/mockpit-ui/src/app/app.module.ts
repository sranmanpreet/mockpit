import { NgModule, APP_INITIALIZER } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';


import { AppRoutingModule } from './app-routing.module';
import { environment as devEnvironment, environment } from '../environments/environment';
import { environment as prodEnvironment } from '../environments/environment.prod';


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


export function initializeApp(configService: ConfigService): () => Promise<any> {
  return () => configService.loadConfig();
}

function trimLastSlashFromUrl(baseUrl: string) {
  if (baseUrl == null || baseUrl == "") {
      return null;
  } else if (baseUrl[baseUrl.length - 1] == '/') {
      var trimmedUrl = baseUrl.substring(0, baseUrl.length - 1);
      return trimmedUrl;
  }
  return null;
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
    FooterComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    BrowserAnimationsModule,
    ToastrModule.forRoot(
      {
        timeOut: 5000,
        positionClass: 'toast-bottom-right'
      }
    ),
    MatTableModule,
    MatPaginatorModule
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
      deps: [PlatformLocation]
    }
  ],
  bootstrap: [AppComponent]
})

export class AppModule { }
