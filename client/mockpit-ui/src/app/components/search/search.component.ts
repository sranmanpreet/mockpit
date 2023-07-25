import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, Subject, debounceTime, distinctUntilChanged, map, switchMap, takeUntil } from 'rxjs';
import { Mock } from 'src/app/models/mock/mock.model';
import { MockService } from 'src/app/services/mock.service';

@Component({
  selector: 'mockpit-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.scss']
})
export class SearchComponent implements OnInit, OnDestroy{
  withRefresh = false;
  mocks$!: Observable<Mock[]>;
  private searchText$ = new Subject<string>();

  private unsubscribeAll$ = new Subject<any>();

  constructor(private mockService: MockService, private router: Router){

  }
  

  ngOnInit() {
    this.mocks$ = this.searchText$.pipe(takeUntil(this.unsubscribeAll$),
      debounceTime(500),
      distinctUntilChanged(),
      switchMap(packageName =>
        this.mockService.search(packageName).pipe(map(mockResponse=> mockResponse.data.content)))
    );
  }

  search(query: string) {
      this.searchText$.next(query);
  }

  getValue(event: Event): string {
    return (event.target as HTMLInputElement).value;
  }

  onClickOnSearchResult(id: number){
    this.router.navigate(['/mock/'+id]);
  }

  ngOnDestroy(): void {
    this.unsubscribeAll$.unsubscribe();  
  }
}
