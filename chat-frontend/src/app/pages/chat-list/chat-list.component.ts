import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Conversation } from '../../models/conversation';
import { AuthService } from '../../services/auth.service';
import { Message } from '../../models/message';
import { firstValueFrom, Subscription } from 'rxjs';
import { ChatStateService } from '../../services/chat-state.service';
import { environment } from '../../../environments/environment.prod';

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
  private auth = inject(AuthService);

  private sub: Subscription | null = null;
  conversations: Conversation[] = [];
  sender: string | null = null;

  ngOnInit(): void {
    this.loadConversations();

    this.sub = this.chatState.conversationUpdated$.subscribe(() => {
      this.loadConversations();
    });
  }

  loadConversations(): void {
    const senderId = this.auth.getUserId();
    if (!senderId) return;
  
    this.http.get<string[]>(`${environment.apiBaseUrl}/messages/chat-list`).subscribe(async userIds => {
      const results: Conversation[] = [];
  
      for (const userId of userIds) {
        const userData = await firstValueFrom(this.http.get<{ username: string }>(`${environment.apiBaseUrl}/users/${userId}`));
        const messages = await firstValueFrom(
          this.http.get<Message[]>(`${environment.apiBaseUrl}/messages/history?user1=${senderId}&user2=${userId}&page=0&size=1`)
        );
  
        results.push({
          userId,
          username: userData.username,
          lastMessage: messages[0]?.content ?? ''
        });
      }
  
      this.conversations = results;
    });
  }

  openChat(userId: string) {
    this.router.navigate(['/chat', userId]);
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }
}
