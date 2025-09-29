import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SamochodFormComponent } from './car-form.component';

describe('SamochodFormComponent', () => {
  let component: SamochodFormComponent;
  let fixture: ComponentFixture<SamochodFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SamochodFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SamochodFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
