import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ChatStateService } from '../../services/chat-state.service';
import { Conversation } from '../../models/conversation';

@Component({
  selector: 'app-chat-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './chat-list.component.html',
  styleUrls: ['./chat-list.component.scss']
})
export class ChatListComponent implements OnInit {

  private chatState = inject(ChatStateService);
  private http = inject(HttpClient);
  private router = inject(Router);

  conversations: Conversation[] = [];
  unreadCounts = this.chatState.getAllUnread();
  senderId = 'user1'; // 之後從 token 拿

  ngOnInit(): void {
      this.loadConversations();
  }

  loadConversations() {
    this.http.get<Conversation[]>(`/api/messages/chat-list/${this.senderId}`)
        .subscribe(data => this.conversations = data);
  }

  getUnread(userId: string): number {
    return this.chatState.getUnreadCount(userId);
  }

  openChat(userId: string) {
    this.router.navigate(['/chat', userId]);
  }
}
