import { Component, EventEmitter, inject, OnInit } from '@angular/core'
import { HeaderComponent } from '../../layout/header/header.component'
import { TableModule } from 'primeng/table'
import { Point } from '../../domain/point'
import { InputGroupModule } from 'primeng/inputgroup'
import { InputGroupAddonModule } from 'primeng/inputgroupaddon'
import { InputTextModule } from 'primeng/inputtext'
import { RouterLink } from '@angular/router'
import { ButtonModule } from 'primeng/button'
import { FormsModule, NgForm } from '@angular/forms'
import { MessageService } from 'primeng/api'
import { PointService } from '../../services/point.service'
import { CheckboxModule } from 'primeng/checkbox'
import { GraphComponent } from "../../components/graph/graph.component";
import { TableComponent } from "../../components/table/table.component";
import { SliderModule } from "primeng/slider";

@Component({
  selector: 'app-main',
  standalone: true,
  imports: [
    HeaderComponent,
    TableModule,
    InputGroupModule,
    InputGroupAddonModule,
    InputTextModule,
    RouterLink,
    ButtonModule,
    FormsModule,
    CheckboxModule,
    GraphComponent,
    TableComponent,
    SliderModule
  ],
  templateUrl: './main.component.html',
  styleUrl: './main.component.css'
})
export class MainComponent implements OnInit {
  private messageService: MessageService = inject(MessageService)

  private pointService: PointService = inject(PointService)
  public points: Point[] = []

  tableColumns: string[] = ['X', 'Y', 'R', 'Результат', 'Время запроса', 'Время выполнения (нс)'];
  tableData : {
    X: string;
    Y: string;
    R: string;
    Результат: string;
    "Время запроса": string;
    "Время выполнения (нс)": number
  }[] = [];
  xValue: number = 5;
  yValue: string = '';
  rValue: number = 5;
  private valueChange: EventEmitter<string> = new EventEmitter<string>();

  ngOnInit(): void {
    this.pointService.retrievePoints().subscribe((points: Point[]): void => {
      this.points = points
      this.updateTable();
      this.drawPoints();
    });
  }

  updateTable(): void {
    this.tableData = this.points.map(item => ({
      X: item.x.toFixed(2),
      Y: item.y.toFixed(2),
      R: item.r.toFixed(2),
      Результат: item.result ? "Попал" : "Не попал",
      "Время запроса": item.createdAt.slice(0, 8),
      "Время выполнения (нс)": item.executionTime
    }))
  }

  point(x: number | string, y: number | string, r: number | string): void {
    this.pointService.createPoint(x, y, r).subscribe({
      next: (response): void => {
        this.ngOnInit();
      }
    })
  }

  getColors(x: number, y: number, r: number): boolean {
    if (x >= 0 && y >= 0) return (y <= r) && (x <= r / 2) && (2*x + y <= r);
    if (x > 0 && y < 0) return (-y <= r) && (x <= r / 2);
    if (x < 0 && y > 0) return ((x * x + y * y) <= (r / 2 * r / 2) && r >= 0);
    return false;
  }

  drawPoints(): void {
    const svgContainer = document.getElementById('circles') as HTMLElement;
    if (svgContainer) {
      svgContainer.innerHTML = '';
      this.tableData.forEach((item): void => {
        const x: number = parseFloat(item.X.replace(',', '.'));
        const y: number = parseFloat(item.Y.replace(',', '.'));

        if (isNaN(x) || isNaN(y)) {
          console.error(`Некорректные значения координат: x=${x}, y=${y}`);
          return;
        }

        const svgX: number = ((x + 5) * 320) / 10 + 40;
        const svgY: number = ((5 - y) * 320) / 10 + 40;
        const r: number = this.rValue;
        let color: string = this.getColors(x, y, r) ? 'green' : 'red';


        const circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
        circle.setAttribute('cx', svgX.toFixed(8));
        circle.setAttribute('cy', svgY.toFixed(8));
        circle.setAttribute('r', '5');
        circle.setAttribute('fill', color);

        svgContainer.appendChild(circle);
      });
    }
  }

  onSubmit(form: NgForm): void {
    const {x, y, r} = form.value
    this.point(x, y, r);
  }

  isFormValid(form: NgForm): boolean {
    const {x, y, r} = form.value;
    return this.rValue > 0 && (Number(this.yValue) >= -5 && Number(this.yValue) <= 3) && this.yValue != '';
  }

  deleteData(): void {
    this.pointService.deletePoints().subscribe({
      next: (response) => {
        this.ngOnInit();
      }
    })
  }

  onTextChange({event}: { event: any }): void {
    this.yValue = event.target.value.replace(',', '.');
    this.valueChange.emit(this.yValue);
    this.validateY();
  }

  onGraphClick(event: MouseEvent): void {
    const container = document.getElementById('graphContainer') as HTMLElement;

    if (container) {
      const rect: DOMRect = container.getBoundingClientRect();

      const clickX: number = event.clientX - rect.left;
      const clickY: number = event.clientY - rect.top;

      const x: number = ((clickX - 40) / 320) * 10 - 5;
      const y: number = 5 - ((clickY - 40) / 320) * 10;

      if (x <= 6.2 && y <= 6.2 && x >= -6.2 && y >= -6.2 && this.rValue >= 0) {
        this.point(x, y, this.rValue);
      }
    }
  }

  private throttle(func: Function, limit: number): Function {
    let lastCall: number = 0;
    return (...args: any[]): void => {
      const now: number = Date.now();
      if (now - lastCall >= limit) {
        lastCall = now;
        func(...args);
      }
    };
  }

  onSliderChange(event: any): void {
    this.validateR();
    const throttledUpdate: Function = this.throttle((rValue: number): void => {
      (window as any).updateGraphScale(rValue);
      this.drawPoints();
    }, 100);
    throttledUpdate(Number(this.rValue));
  }

  validateR(): void {
    const rError: HTMLElement | null = document.getElementById('rError');
    if (this.rValue < 0) {
      if (rError) {
        (rError as HTMLSpanElement).textContent = "Ошибка: R должен быть положительным";
      }
    } else {
      if (rError) {
        (rError as HTMLSpanElement).textContent = "";
      }
    }
  }

  validateY(): void {
    const yError: HTMLElement | null = document.getElementById('yError');
    const yValue: string = this.yValue.trim();
    const regex = /^-?\d+(\.\d+)?$/;

    if (Number(yValue) < -5 || Number(yValue) > 3) {
      if (yError) {
        (yError as HTMLSpanElement).textContent = "Ошибка: Y должен быть от -5 до 3";
      }
    } else if (!regex.test(yValue)) {
      if (yError) {
        (yError as HTMLSpanElement).textContent = "Ошибка: Y должен быть числом";
      }
    } else {
      if (yError) {
        (yError as HTMLSpanElement).textContent = "";
      }
    }
  }

  protected readonly event: Event | undefined = event;
}
