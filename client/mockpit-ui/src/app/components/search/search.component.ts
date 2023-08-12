import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { Observable, Subject, catchError, debounceTime, distinctUntilChanged, map, of, switchMap, takeUntil, tap } from 'rxjs';
import { Mock } from 'src/app/models/mock/mock.model';
import { MockService } from 'src/app/services/mock.service';

@Component({
  selector: 'mockpit-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.scss']
})
export class SearchComponent implements OnInit, OnDestroy {

  withRefresh = false;
  mocks$!: Observable<Mock[]>;
  query?: String;

  private searchText$ = new Subject<string>();
  private unsubscribeAll$ = new Subject<any>();

  constructor(private mockService: MockService, private router: Router, private toast: ToastrService) {

  }


  ngOnInit() {
    this.mocks$ = this.searchText$.pipe(takeUntil(this.unsubscribeAll$),
      debounceTime(500),
      distinctUntilChanged(),
      switchMap(query =>
        this.mockService.search(query)
          .pipe(
            map(mockResponse => mockResponse.data.content),
            tap(()=> this.query = query),
            catchError((error) => {
              this.toast.error(error.error.message?error.error.message: "Something went wrong while searching. Please contact administrator", "Error");
              return of(null);
            })
          )
      )
    );

  }

  search(query: string) {
    this.searchText$.next(query);
  }

  clearSearch(){
    this.searchText$.next("");
  }

  getValue(event: Event): string {
    return (event.target as HTMLInputElement).value;
  }

  onClickOnSearchResult(id: number) {
    this.router.navigate(['/mock/' + id]);
  }

  ngOnDestroy(): void {
    this.unsubscribeAll$.unsubscribe();
  }
}
