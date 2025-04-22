import { Component, OnInit, inject, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ChatStateService } from '../../services/chat-state.service';
import { Conversation } from '../../models/conversation';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent implements OnInit {
  private chatState = inject(ChatStateService);
  private http = inject(HttpClient);
  private router = inject(Router);
  private auth = inject(AuthService);

  conversations: Conversation[] = [];
  sender: string | null = null;

  ngOnInit(): void {
    this.sender = this.auth.getUsername();
    if (this.sender) {
      this.loadConversations();

      // 訂閱聊天列表變動通知
      this.chatState.conversationUpdated$.subscribe(() => {
        this.loadConversations();
      });
    } else {
      console.warn('senderId is null');
    }
  }

  loadConversations() {
    if (!this.sender) return;

    this.http.get<Conversation[]>(`/api/messages/chat-list`)
      .subscribe({
        next: data => this.conversations = data,
        error: err => console.error('loadConversations error', err)
      });
  }

  openChat(userId: string) {
    this.router.navigate(['/chat', userId]);
  }
}
