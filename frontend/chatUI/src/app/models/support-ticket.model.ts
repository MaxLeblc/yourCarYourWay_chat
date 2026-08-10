export interface SupportTicket {
  id: number;
  subject: string;
  status: 'OPEN' | 'IN_PROGRESS' | 'CLOSED';
  createdAt: string;
  userId: number;
  userName: string;
}
