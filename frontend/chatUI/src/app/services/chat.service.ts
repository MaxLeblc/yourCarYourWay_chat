import { Injectable, NgZone } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Client, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BehaviorSubject, Observable } from 'rxjs';
import { ChatMessage } from '../models/chat-message.model';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private stompClient!: Client;
  private currentSubscription?: StompSubscription;
  private messagesSubject = new BehaviorSubject<ChatMessage[]>([]);
  public messages$: Observable<ChatMessage[]> = this.messagesSubject.asObservable();

  private connectionStatusSubject = new BehaviorSubject<boolean>(false);
  public isConnected$: Observable<boolean> = this.connectionStatusSubject.asObservable();

  private activeTicketIdSubject = new BehaviorSubject<number | null>(null);
  public activeTicketId$: Observable<number | null> = this.activeTicketIdSubject.asObservable();

  private backendUrl = 'http://localhost:8080';

  constructor(private http: HttpClient, private ngZone: NgZone) {}

  public connectToTicket(ticketId: number, username: string, userId?: number): void {
    const isSameTicket = this.activeTicketIdSubject.getValue() === ticketId;
    this.activeTicketIdSubject.next(ticketId);

    // Always fetch history synchronously on ticket selection
    this.fetchTicketHistory(ticketId);

    if (isSameTicket && this.stompClient?.connected && this.currentSubscription) {
      return;
    }

    if (this.currentSubscription) {
      this.currentSubscription.unsubscribe();
      this.currentSubscription = undefined;
    }

    const setupSubscription = () => {
      if (this.stompClient && this.stompClient.connected) {
        this.subscribeToTicketChannel(ticketId);
      }
    };

    if (!this.stompClient || !this.stompClient.active) {
      const socket = new SockJS(`${this.backendUrl}/ws`);
      const token = localStorage.getItem('ycyw_token');

      this.stompClient = new Client({
        webSocketFactory: () => socket,
        connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
        debug: (msg: string) => console.log(msg),
        reconnectDelay: 5000,
      });

      this.stompClient.onConnect = () => {
        this.ngZone.run(() => {
          this.connectionStatusSubject.next(true);
          setupSubscription();
        });
      };

      this.stompClient.onDisconnect = () => {
        this.ngZone.run(() => {
          this.connectionStatusSubject.next(false);
        });
      };

      this.stompClient.onStompError = (frame) => {
        console.error('[STOMP Security Error]', frame.headers['message'], frame.body);
      };

      this.stompClient.activate();
    } else if (this.stompClient.connected) {
      setupSubscription();
    } else {
      const prevOnConnect = this.stompClient.onConnect;
      this.stompClient.onConnect = (frame) => {
        if (prevOnConnect) prevOnConnect(frame);
        this.ngZone.run(() => setupSubscription());
      };
    }
  }

  private subscribeToTicketChannel(ticketId: number): void {
    if (this.currentSubscription) {
      this.currentSubscription.unsubscribe();
    }

    // Subscribe to ticket-specific topic /topic/ticket/{ticketId}
    this.currentSubscription = this.stompClient.subscribe(`/topic/ticket/${ticketId}`, (message) => {
      const chatMessage: ChatMessage = JSON.parse(message.body);
      
      this.ngZone.run(() => {
        const currentMessages = this.messagesSubject.getValue();
        const isDuplicate = currentMessages.some(
          (m) => m.id && chatMessage.id && m.id === chatMessage.id
        );

        if (!isDuplicate) {
          this.messagesSubject.next([...currentMessages, chatMessage]);
        }
      });
    });
  }

  public sendMessage(ticketId: number, sender: string, userId: number | undefined, content: string): void {
    if (this.stompClient && this.stompClient.connected) {
      const chatMessage: ChatMessage = {
        sender,
        userId,
        supportTicketId: ticketId,
        content,
        type: 'CHAT'
      };
      this.stompClient.publish({
        destination: `/app/chat.sendMessage/${ticketId}`,
        body: JSON.stringify(chatMessage)
      });
    }
  }

  public leaveTicket(ticketId: number): void {
    if (this.currentSubscription) {
      this.currentSubscription.unsubscribe();
      this.currentSubscription = undefined;
    }
    this.ngZone.run(() => {
      this.activeTicketIdSubject.next(null);
      this.messagesSubject.next([]);
    });
  }

  public disconnect(): void {
    if (this.currentSubscription) {
      this.currentSubscription.unsubscribe();
      this.currentSubscription = undefined;
    }
    if (this.stompClient && this.stompClient.active) {
      this.stompClient.deactivate();
    }
    this.ngZone.run(() => {
      this.activeTicketIdSubject.next(null);
      this.messagesSubject.next([]);
      this.connectionStatusSubject.next(false);
    });
  }

  public fetchTicketHistory(ticketId: number): void {
    this.http.get<ChatMessage[]>(`${this.backendUrl}/api/tickets/${ticketId}/history`)
      .subscribe({
        next: (history) => {
          this.ngZone.run(() => {
            this.messagesSubject.next(history);
          });
        },
        error: (err) => console.error('Error fetching ticket history', err)
      });
  }
}
