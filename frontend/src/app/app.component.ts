import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './shared/components/header/header.component';
import { NavComponent } from './shared/components/nav/nav.component';
import { FooterComponent } from './shared/components/footer/footer.component';
import { ToastContainerComponent } from './shared/components/toast/toast-container.component';
import { AlarmModalComponent } from './shared/components/alarm-modal/alarm-modal.component';
import { AlarmService } from './core/services/alarm.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  imports: [RouterOutlet, HeaderComponent, NavComponent, FooterComponent, ToastContainerComponent, AlarmModalComponent]
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'Study Tracker & Brag Document';
  alarmService = inject(AlarmService);

  ngOnInit(): void {
    // Start SSE connection for alarms on app init
    // This ensures alarms work on any page, not just weekly calendar
    this.alarmService.connect();
  }

  ngOnDestroy(): void {
    this.alarmService.disconnect();
  }
}
