import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavService } from '../../../core/services/nav.service';
import { Subscription } from 'rxjs';

interface NavSubItem {
  path: string;
  label: string;
}

interface NavGroup {
  label: string;
  expanded: boolean;
  items: NavSubItem[];
}

@Component({
  selector: 'app-nav',
  templateUrl: './nav.component.html',
  styleUrls: ['./nav.component.css'],
  imports: [CommonModule, RouterLink]
})
export class NavComponent implements OnInit, OnDestroy {
  navGroups: NavGroup[] = [
    {
      label: 'Tasks',
      expanded: false,
      items: [
        { path: '/tasks', label: 'Tasks' },
        { path: '/monthly-overview', label: 'Monthly Overview' },
        { path: '/weekly-calendar', label: 'Weekly Calendar' },
        { path: '/tags', label: 'Tags' }
      ]
    },
    {
      label: 'Admin Panel',
      expanded: false,
      items: [
        { path: '/database-backup', label: 'Database Backup' },
        { path: '/application-logs', label: 'Application Logs' }
      ]
    }
  ];

  standaloneItems: NavSubItem[] = [
    { path: '/metrics', label: 'Metrics' }
  ];

  isOpen = false;
  private subscription: Subscription = new Subscription();

  constructor(private router: Router, private navService: NavService) {}

  ngOnInit() {
    this.subscription = this.navService.sidebarOpen$.subscribe(
      isOpen => this.isOpen = isOpen
    );
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  isActive(path: string): boolean {
    return this.router.url === path;
  }

  onNavClick() {
    this.navService.closeSidebar();
  }

  toggleGroup(group: NavGroup): void {
    group.expanded = !group.expanded;
  }
}
