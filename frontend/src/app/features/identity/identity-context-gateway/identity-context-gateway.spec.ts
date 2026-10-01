import { ComponentFixture, TestBed } from '@angular/core/testing';
import { IdentityContextGateway } from './identity-context-gateway';

describe('IdentityContextGateway', () => {
  let component: IdentityContextGateway;
  let fixture: ComponentFixture<IdentityContextGateway>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IdentityContextGateway],
    }).compileComponents();

    fixture = TestBed.createComponent(IdentityContextGateway);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
