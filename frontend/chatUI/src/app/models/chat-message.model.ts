export interface ChatMessage {
  id?: number;
  sender: string;
  userId?: number;
  supportTicketId?: number;
  content: string;
  type: 'CHAT' | 'SYSTEM';
  timestamp?: string;
}
