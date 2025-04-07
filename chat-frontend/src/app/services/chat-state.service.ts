import { Injectable } from '@angular/core';
import { Message } from '../models/message';

@Injectable({
  providedIn: 'root'
})
export class ChatStateService {
  private messageCache = new Map<string, Message[]>();
  private unreadCounts: Record<string, number> = {};

  getMessages(userId: string): Message[] {
    return this.messageCache.get(userId) || [];
  }

  setMessages(userId: string, messages: Message[]) {
    this.messageCache.set(userId, messages);
  }

  getUnreadCount(userId: string): number {
    return this.unreadCounts[userId] || 0;
  }

  incrementUnread(userId: string): void {
    this.unreadCounts[userId];
  }

  clearUnread(userId: string): void {
    delete this.unreadCounts[userId];
  }

  getAllUnread(): Record<string, number> {
    return this.unreadCounts;
  }
}
