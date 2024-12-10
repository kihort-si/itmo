import { Component } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';


@Component({
  selector: 'app-graph',
  standalone: true,
  imports: [
    NgOptimizedImage
  ],
  templateUrl: './graph.component.html',
  styleUrl: './graph.component.css'
})
export class GraphComponent {}
