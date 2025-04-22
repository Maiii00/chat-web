import { Injectable } from '@angular/core';
import { Message } from '../models/message';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ChatStateService {
  private messageCache = new Map<string, Message[]>();
  private conversationUpdatedSubject = new BehaviorSubject<void>(undefined);
  conversationUpdated$ = this.conversationUpdatedSubject.asObservable();

  notifyConversationUpdate() {
    this.conversationUpdatedSubject.next();
  }

  getMessages(userId: string): Message[] {
    return this.messageCache.get(userId) || [];
  }

  setMessages(userId: string, messages: Message[]) {
    this.messageCache.set(userId, messages);
  }
}
