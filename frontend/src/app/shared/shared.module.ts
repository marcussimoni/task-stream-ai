import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from './components/header/header.component';
import { NavComponent } from './components/nav/nav.component';
import { TagSelectorComponent } from './components/tag-selector/tag-selector.component';
import { FooterComponent } from './components/footer/footer.component';

@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    HeaderComponent,
    NavComponent,
    TagSelectorComponent,
    FooterComponent
  ],
  exports: [
    HeaderComponent,
    NavComponent,
    TagSelectorComponent,
    FooterComponent,
    CommonModule,
    RouterModule
  ]
})
export class SharedModule { }
