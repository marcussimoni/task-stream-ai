import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class NavService {
  private sidebarOpen = new BehaviorSubject<boolean>(false);
  sidebarOpen$ = this.sidebarOpen.asObservable();

  toggleSidebar() {
    this.sidebarOpen.next(!this.sidebarOpen.value);
  }

  closeSidebar() {
    this.sidebarOpen.next(false);
  }
}
