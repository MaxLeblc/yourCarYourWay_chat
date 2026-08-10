import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { AuthResponse } from '../models/auth.model';
import { User } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private backendUrl = 'http://localhost:8080/api/auth';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$: Observable<User | null> = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    const savedUser = localStorage.getItem('ycyw_user');
    if (savedUser) {
      this.currentUserSubject.next(JSON.parse(savedUser));
    }
  }

  public login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.backendUrl}/login`, { email, password }).pipe(
      tap((res) => {
        const user: User = { id: res.id, email: res.email, name: res.name, role: res.role };
        localStorage.setItem('ycyw_user', JSON.stringify(user));
        localStorage.setItem('ycyw_token', res.token);
        this.currentUserSubject.next(user);
      })
    );
  }

  public register(firstName: string, lastName: string, email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.backendUrl}/register`, { firstName, lastName, email, password }).pipe(
      tap((res) => {
        const user: User = { id: res.id, email: res.email, name: res.name, role: res.role };
        localStorage.setItem('ycyw_user', JSON.stringify(user));
        localStorage.setItem('ycyw_token', res.token);
        this.currentUserSubject.next(user);
      })
    );
  }

  public logout(): void {
    localStorage.removeItem('ycyw_user');
    localStorage.removeItem('ycyw_token');
    this.currentUserSubject.next(null);
  }

  public getCurrentUser(): User | null {
    return this.currentUserSubject.getValue();
  }
}
