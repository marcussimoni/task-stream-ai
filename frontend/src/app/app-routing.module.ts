import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TasksComponent } from './features/tasks/tasks.component';
import { TagsComponent } from './features/tags/tags.component';
import { MetricsComponent } from './features/metrics/metrics.component';
import { MonthlyOverviewComponent } from './features/monthly-overview/monthly-overview.component';
import { WeeklyCalendarComponent } from './features/weekly-calendar/weekly-calendar.component';
import { DatabaseBackupComponent } from './features/admin/components/database-backup/database-backup.component';
import { ApplicationLogsComponent } from './features/admin/components/application-logs/application-logs.component';

export const routes: Routes = [
  { path: '', redirectTo: '/tasks', pathMatch: 'full' },
  { path: 'tasks', component: TasksComponent },
  { path: 'tags', component: TagsComponent },
  { path: 'metrics', component: MetricsComponent },
  { path: 'monthly-overview', component: MonthlyOverviewComponent },
  { path: 'weekly-calendar', component: WeeklyCalendarComponent },
  { path: 'database-backup', component: DatabaseBackupComponent },
  { path: 'application-logs', component: ApplicationLogsComponent },
  { path: '**', redirectTo: '/tasks' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
