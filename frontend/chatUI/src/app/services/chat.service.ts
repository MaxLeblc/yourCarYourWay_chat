import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BehaviorSubject, Observable } from 'rxjs';
import { ChatMessage } from '../models/chat-message.model';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private stompClient!: Client;
  private messagesSubject = new BehaviorSubject<ChatMessage[]>([]);
  public messages$: Observable<ChatMessage[]> = this.messagesSubject.asObservable();

  private connectionStatusSubject = new BehaviorSubject<boolean>(false);
  public isConnected$: Observable<boolean> = this.connectionStatusSubject.asObservable();

  private backendUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  public connect(username: string): void {
    // 1. Fetch chat history from PostgreSQL
    this.fetchHistory();

    // 2. Initialize WebSocket STOMP connection
    const socket = new SockJS(`${this.backendUrl}/ws`);
    this.stompClient = new Client({
      webSocketFactory: () => socket,
      debug: (msg: string) => console.log(msg),
      reconnectDelay: 5000,
    });

    this.stompClient.onConnect = () => {
      this.connectionStatusSubject.next(true);

      // Subscribe to public channel
      this.stompClient.subscribe('/topic/public', (message) => {
        const chatMessage: ChatMessage = JSON.parse(message.body);
        const currentMessages = this.messagesSubject.getValue();
        this.messagesSubject.next([...currentMessages, chatMessage]);
      });

      // Send join event
      this.stompClient.publish({
        destination: '/app/chat.addUser',
        body: JSON.stringify({ sender: username, type: 'JOIN' })
      });
    };

    this.stompClient.onDisconnect = () => {
      this.connectionStatusSubject.next(false);
    };

    this.stompClient.activate();
  }

  public sendMessage(sender: string, content: string): void {
    if (this.stompClient && this.stompClient.connected) {
      const chatMessage: ChatMessage = {
        sender,
        content,
        type: 'CHAT'
      };
      this.stompClient.publish({
        destination: '/app/chat.sendMessage',
        body: JSON.stringify(chatMessage)
      });
    }
  }

  private fetchHistory(): void {
    this.http.get<ChatMessage[]>(`${this.backendUrl}/api/chat/history`)
      .subscribe({
        next: (history) => this.messagesSubject.next(history),
        error: (err) => console.error('Error fetching chat history', err)
      });
  }
}
