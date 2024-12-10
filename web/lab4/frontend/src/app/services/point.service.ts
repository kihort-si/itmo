import { inject, Injectable } from '@angular/core'
import { HttpClient } from "@angular/common/http"
import { Point } from '../domain/point'
import { API_URL } from '../constants'

@Injectable({
  providedIn: 'root'
})
export class PointService {
  private readonly baseUrl = `${API_URL}/results`
  private http: HttpClient = inject(HttpClient)

  retrievePoints() {
    return this.http.get<Point[]>(this.baseUrl)
  }

  createPoint(x: number | string, y: number | string, r: number | string) {
    return this.http.post<Point>(this.baseUrl, {x, y, r})
  }

  deletePoints() {
    return this.http.delete(this.baseUrl)
  }
}
