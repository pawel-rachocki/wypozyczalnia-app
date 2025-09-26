import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WypozyczeniaComponent } from './wypozyczenia.component';

describe('WypozyczeniaComponent', () => {
  let component: WypozyczeniaComponent;
  let fixture: ComponentFixture<WypozyczeniaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WypozyczeniaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(WypozyczeniaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
