import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SamochodListComponent } from './samochod-list.component';

describe('SamochodListComponent', () => {
  let component: SamochodListComponent;
  let fixture: ComponentFixture<SamochodListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SamochodListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SamochodListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
