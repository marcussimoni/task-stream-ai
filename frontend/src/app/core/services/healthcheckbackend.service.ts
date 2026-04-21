import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class HealthCheckBackendService {


  constructor(private http: HttpClient) {}

  healthCheck(path: string): Observable<any> {
    return this.http.get(`/actuator/health/${path}`);
  }

  getInfo(): Observable<{app: {name: string, description: string, version: string}}> {
    return this.http.get<{app: {name: string, description: string, version: string}}>(`/actuator/info`);
  }

}
