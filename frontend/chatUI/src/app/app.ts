import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { AuthService } from './services/auth.service';
import { TicketService } from './services/ticket.service';
import { ChatService } from './services/chat.service';
import { User } from './models/user.model';
import { SupportTicket } from './models/support-ticket.model';
import { ChatMessage } from './models/chat-message.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class AppComponent implements OnInit, OnDestroy {
  public currentUser: User | null = null;
  public tickets: SupportTicket[] = [];
  public activeTicket: SupportTicket | null = null;
  public messages: ChatMessage[] = [];
  public isConnected = false;

  // Auth Form State
  public authMode: 'login' | 'register' = 'login';
  public loginEmail = '';
  public loginPassword = '';
  public regFirstName = '';
  public regLastName = '';
  public regEmail = '';
  public regPassword = '';
  public authError = '';

  // Ticket & Chat State
  public newTicketSubject = '';
  public messageContent = '';

  private subs: Subscription[] = [];

  constructor(
    public authService: AuthService,
    private ticketService: TicketService,
    public chatService: ChatService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.subs.push(
      this.authService.currentUser$.subscribe((user) => {
        this.currentUser = user;
        if (user) {
          this.loadTickets();
        } else {
          this.tickets = [];
          this.activeTicket = null;
          this.messages = [];
        }
        this.cdr.detectChanges();
      })
    );

    this.subs.push(
      this.chatService.messages$.subscribe((msgs) => {
        this.messages = msgs;
        this.cdr.detectChanges();
        this.scrollToBottom();
      })
    );

    this.subs.push(
      this.chatService.isConnected$.subscribe((status) => {
        this.isConnected = status;
        this.cdr.detectChanges();
      })
    );
  }

  ngOnDestroy(): void {
    this.subs.forEach((s) => s.unsubscribe());
  }

  public onLogin(): void {
    this.authError = '';
    if (!this.loginEmail || !this.loginPassword) {
      this.authError = 'Please fill in all fields';
      return;
    }

    this.authService.login(this.loginEmail, this.loginPassword).subscribe({
      next: () => {
        this.loginEmail = '';
        this.loginPassword = '';
        this.loadTickets();
      },
      error: (err) => {
        this.authError = err.error?.message || 'Invalid credentials';
        this.cdr.detectChanges();
      }
    });
  }

  public onRegister(): void {
    this.authError = '';
    if (!this.regFirstName || !this.regLastName || !this.regEmail || !this.regPassword) {
      this.authError = 'Please fill in all registration fields';
      return;
    }

    const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;
    if (!passwordPattern.test(this.regPassword)) {
      this.authError = 'Password must be at least 8 characters long and contain uppercase, lowercase, and numbers.';
      return;
    }

    this.authService.register(this.regFirstName, this.regLastName, this.regEmail, this.regPassword).subscribe({
      next: () => {
        this.regFirstName = '';
        this.regLastName = '';
        this.regEmail = '';
        this.regPassword = '';
        this.loadTickets();
      },
      error: (err) => {
        this.authError = err.error?.message || 'Registration failed';
        this.cdr.detectChanges();
      }
    });
  }

  public quickFill(email: string): void {
    this.authMode = 'login';
    this.loginEmail = email;
    this.loginPassword = 'password123';
    this.onLogin();
  }

  public logout(): void {
    if (this.activeTicket && this.currentUser) {
      this.chatService.leaveTicket(this.activeTicket.id);
    }
    this.authService.logout();
  }

  public loadTickets(): void {
    if (!this.currentUser) return;

    if (this.currentUser.role === 'ROLE_AGENCY_STAFF') {
      this.ticketService.getAllTickets().subscribe((data) => {
        this.tickets = data;
        if (!this.activeTicket && data.length > 0) {
          this.selectTicket(data[0]);
        }
        this.cdr.detectChanges();
      });
    } else {
      this.ticketService.getTicketsForUser(this.currentUser.id).subscribe((data) => {
        this.tickets = data;
        if (!this.activeTicket && data.length > 0) {
          this.selectTicket(data[0]);
        }
        this.cdr.detectChanges();
      });
    }
  }

  public createTicket(): void {
    if (!this.newTicketSubject.trim() || !this.currentUser) return;

    this.ticketService.createTicket(this.currentUser.id, this.newTicketSubject).subscribe({
      next: (ticket) => {
        this.newTicketSubject = '';
        this.loadTickets();
        this.selectTicket(ticket);
      },
      error: (err) => console.error('Error creating ticket', err)
    });
  }

  public updateTicketStatus(status: 'OPEN' | 'CLOSED'): void {
    if (!this.activeTicket || !this.currentUser) return;

    this.ticketService.updateTicketStatus(this.activeTicket.id, status).subscribe({
      next: (updatedTicket) => {
        this.activeTicket = updatedTicket;
        this.loadTickets();
        this.chatService.sendMessage(
          updatedTicket.id,
          'System',
          undefined,
          `Ticket #${updatedTicket.id} status updated to ${status} by ${this.currentUser?.name}.`
        );
      },
      error: (err) => console.error('Error updating status', err)
    });
  }

  public deleteTicket(ticketId: number, event: Event): void {
    event.stopPropagation();
    if (confirm('Are you sure you want to delete this closed ticket?')) {
      this.ticketService.deleteTicket(ticketId).subscribe({
        next: () => {
          if (this.activeTicket?.id === ticketId) {
            this.activeTicket = null;
            this.messages = [];
          }
          this.loadTickets();
        },
        error: (err) => console.error('Error deleting ticket', err)
      });
    }
  }

  public selectTicket(ticket: SupportTicket): void {
    if (this.activeTicket && this.activeTicket.id !== ticket.id && this.currentUser) {
      this.chatService.leaveTicket(this.activeTicket.id);
    }
    this.activeTicket = ticket;
    if (this.currentUser) {
      this.chatService.connectToTicket(ticket.id, this.currentUser.name, this.currentUser.id);
    }
    this.cdr.detectChanges();
  }

  public sendMessage(): void {
    if (!this.messageContent.trim() || !this.activeTicket || !this.currentUser) return;

    const content = this.messageContent;
    this.messageContent = '';

    this.chatService.sendMessage(
      this.activeTicket.id,
      this.currentUser.name,
      this.currentUser.id,
      content
    );
    this.cdr.detectChanges();
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      const container = document.getElementById('chat-messages-container');
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    }, 50);
  }
}
