import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KlientListComponent } from './client-list.component';

describe('KlientListComponent', () => {
  let component: KlientListComponent;
  let fixture: ComponentFixture<KlientListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KlientListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(KlientListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
