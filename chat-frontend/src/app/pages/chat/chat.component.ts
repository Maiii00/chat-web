import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, ElementRef, inject, OnInit, ViewChild } from '@angular/core';
import { FormsModule} from '@angular/forms'
import { Message } from '../../models/message';
import { ActivatedRoute } from '@angular/router';
import { WebSocketService } from '../../services/websocket.service';
import { HttpClient } from '@angular/common/http';
import { ChatStateService } from '../../services/chat-state.service';
import { AuthService } from '../../services/auth.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.scss']
})
export class ChatComponent implements OnInit, AfterViewInit {
  id!: string;
  receiverId!: string;
  receiverUsername: string = '';

  messages: Message[] = [];
  newMessage: string = '';

  private chatState = inject(ChatStateService);
  private route = inject(ActivatedRoute);
  private wsService = inject(WebSocketService);
  private http = inject(HttpClient);
  private auth = inject(AuthService);
  private messageSub: Subscription | null = null;

  senderId: string = this.auth.getUserId()!;

  @ViewChild('chatMessages') chatMessagesRef!: ElementRef;

  ngOnInit(): void {
    // 取得 URL 上的接收者 ID
    this.route.paramMap.subscribe(params => {
      this.receiverId = params.get('userId') || '';

      this.http.get<{ username: string }>(`/api/users/${this.receiverId}`).subscribe(data => {
        this.receiverUsername = data.username;
      });
      // 取出快取訊息或設為空陣列
      this.messages = this.chatState.getMessages(this.receiverId);
      
      // 訂閱 WebSocket 訊息
      this.wsService.getMessages().subscribe(message => {
        if (!message) return;
  
        const isCurrent = message.senderId === this.receiverId || message.receiverId === this.receiverId;
        const alreadyExists = this.messages.some(m => m.id === message.id);
  
        if (isCurrent && !alreadyExists) {
          this.messages.push(message);
          this.chatState.setMessages(this.receiverId, [...this.messages]);
          this.scrollToBottom();
        }
      });

      // 從後端撈歷史訊息
      this.http.get<Message[]>(`/api/messages/history?user1=${this.senderId}&user2=${this.receiverId}`)
      .subscribe(history => {
        const existingIds = new Set(this.messages.map(m => m.id));
        const newOnes = history.filter(m => !existingIds.has(m.id));
        this.messages = [...newOnes.reverse(), ...this.messages]; // 舊的放前面
        this.chatState.setMessages(this.receiverId, [...this.messages]);
        this.scrollToBottom();
      });

    });

  }

  scrollToBottom(): void {
    requestAnimationFrame(() => {
      if (this.chatMessagesRef) {
        const el = this.chatMessagesRef.nativeElement;
        el.scrollTop = el.scrollHeight;
      }
    });
  }

  sendMessage(): void {
    if (this.newMessage.trim()) {
      const message: Message = {
        id: this.id,
        receiverId: this.receiverId,
        content: this.newMessage,
      };

      const headers = {
        'Authorization': `Bearer ${this.auth.getToken()}`
      };

      console.log(`Bearer ${this.auth.getToken()}`);
      console.log(localStorage.getItem('refreshToken'));

      
      // 傳送給後端儲存
      this.http.post<Message>('/api/messages', message, { headers }).subscribe({
        next: saved => {
          // 前端顯示訊息（已儲存版本）
          this.messages.push(saved);
          this.chatState.setMessages(this.receiverId, [...this.messages]);

          // 通知對方 (仍然透過 WebSocket)
          this.wsService.sendMessage('/app/private', saved);

          // 發送事件讓 Sidebar 可以重新撈取聊天室清單
          this.chatState.notifyConversationUpdate();

          this.newMessage = '';
          this.scrollToBottom();
        },
        error: err => console.error('sendMessage error', err)
      });
    }
  }

  currentPage = 0;
  loadMore(): void {
    this.currentPage += 1;

    this.http.get<Message[]>(`/api/messages/history?user1=${this.senderId}&user2=${this.receiverId}&page=${this.currentPage}&size=20`)
      .subscribe(history => {
        const existingIds = new Set(this.messages.map(m => m.id));
        const newOnes = history.filter(m => !existingIds.has(m.id));
        this.messages = [...newOnes.reverse(), ...this.messages];
        this.chatState.setMessages(this.receiverId, this.messages);
      });
  }

  handleKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault(); // 阻止換行
      this.sendMessage();
    }
  }

  handleScroll(): void {
    const el = this.chatMessagesRef.nativeElement;
    if (el.scrollTop === 0) {
      this.loadMore();
    }
  }

  ngAfterViewInit(): void {
    this.scrollToBottom();

    this.chatMessagesRef.nativeElement.addEventListener('scroll', this.handleScroll.bind(this));
  }

  ngOnDestroy(): void {
    this.messageSub?.unsubscribe();
    this.chatMessagesRef?.nativeElement.removeEventListener('scroll', this.handleScroll.bind(this));
  }
}
