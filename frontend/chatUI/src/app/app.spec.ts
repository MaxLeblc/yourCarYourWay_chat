import { describe, it, expect } from 'vitest';
import { ChatMessage } from './models/chat-message.model';

describe('ChatMessage Model', () => {
  it('should create a valid chat message object', () => {
    const msg: ChatMessage = {
      sender: 'John',
      content: 'Hello Support',
      type: 'CHAT'
    };

    expect(msg.sender).toBe('John');
    expect(msg.content).toBe('Hello Support');
    expect(msg.type).toBe('CHAT');
  });
});
