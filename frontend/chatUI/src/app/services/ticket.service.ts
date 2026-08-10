import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SupportTicket } from '../models/support-ticket.model';

@Injectable({
  providedIn: 'root'
})
export class TicketService {
  private backendUrl = 'http://localhost:8080/api/tickets';

  constructor(private http: HttpClient) {}

  public createTicket(userId: number, subject: string): Observable<SupportTicket> {
    return this.http.post<SupportTicket>(this.backendUrl, { userId, subject });
  }

  public updateTicketStatus(ticketId: number, status: 'OPEN' | 'IN_PROGRESS' | 'CLOSED'): Observable<SupportTicket> {
    return this.http.put<SupportTicket>(`${this.backendUrl}/${ticketId}/status?status=${status}`, {});
  }

  public deleteTicket(ticketId: number): Observable<void> {
    return this.http.delete<void>(`${this.backendUrl}/${ticketId}`);
  }

  public getTicketsForUser(userId: number): Observable<SupportTicket[]> {
    return this.http.get<SupportTicket[]>(`${this.backendUrl}/user/${userId}`);
  }

  public getAllTickets(): Observable<SupportTicket[]> {
    return this.http.get<SupportTicket[]>(this.backendUrl);
  }

  public getTicketById(ticketId: number): Observable<SupportTicket> {
    return this.http.get<SupportTicket>(`${this.backendUrl}/${ticketId}`);
  }
}
