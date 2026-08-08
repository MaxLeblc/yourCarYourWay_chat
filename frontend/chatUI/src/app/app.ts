import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from './services/chat.service';
import { ChatMessage } from './models/chat-message.model';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class AppComponent implements OnInit {
  username: string = '';
  messageInput: string = '';
  isJoined: boolean = false;

  messages$!: Observable<ChatMessage[]>;
  isConnected$!: Observable<boolean>;

  constructor(private chatService: ChatService) {}

  ngOnInit(): void {
    this.messages$ = this.chatService.messages$;
    this.isConnected$ = this.chatService.isConnected$;
  }

  joinChat(): void {
    if (this.username.trim()) {
      this.isJoined = true;
      this.chatService.connect(this.username.trim());
    }
  }

  sendMessage(): void {
    if (this.messageInput.trim() && this.username.trim()) {
      this.chatService.sendMessage(this.username.trim(), this.messageInput.trim());
      this.messageInput = '';
    }
  }
}
