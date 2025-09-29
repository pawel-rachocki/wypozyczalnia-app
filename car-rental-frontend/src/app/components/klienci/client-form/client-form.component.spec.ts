import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KlientFormComponent } from './client-form.component';

describe('KlientFormComponent', () => {
  let component: KlientFormComponent;
  let fixture: ComponentFixture<KlientFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KlientFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(KlientFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
