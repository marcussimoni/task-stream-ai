import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FooterComponent } from './footer.component';

describe('FooterComponent', () => {
  let component: FooterComponent;
  let fixture: ComponentFixture<FooterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FooterComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(FooterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display app name', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.app-name')?.textContent).toContain('DailyTrack');
  });

  it('should display app version', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.app-version')?.textContent).toContain('v1.0.0');
  });

  it('should display current year in copyright', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const currentYear = new Date().getFullYear().toString();
    expect(compiled.querySelector('.copyright')?.textContent).toContain(currentYear);
  });

  it('should use semantic footer element', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('footer')).toBeTruthy();
  });

  it('should have correct background color', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const footer = compiled.querySelector('.app-footer') as HTMLElement;
    expect(footer).toBeTruthy();
    const styles = window.getComputedStyle(footer);
    expect(styles.backgroundColor).toBe('rgb(26, 86, 146)');
  });
});
