import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

import { CoreModule } from './core/core.module';
import { SharedModule } from './shared/shared.module';

import { TasksComponent } from './features/tasks/tasks.component';
import { TagsComponent } from './features/tags/tags.component';
import { MetricsComponent } from './features/metrics/metrics.component';
import { MonthlyOverviewComponent } from './features/monthly-overview/monthly-overview.component';
import { WeeklyCalendarComponent } from './features/weekly-calendar/weekly-calendar.component';

@NgModule({
  declarations: [
    AppComponent,
    TasksComponent,
    TagsComponent,
    MetricsComponent,
    MonthlyOverviewComponent,
    WeeklyCalendarComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    CoreModule,
    SharedModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
