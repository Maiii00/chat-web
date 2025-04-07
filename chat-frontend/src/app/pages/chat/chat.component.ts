import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, ElementRef, inject, OnInit, ViewChild } from '@angular/core';
import { FormsModule} from '@angular/forms'
import { Message } from '../../models/message';
import { ActivatedRoute } from '@angular/router';
import { WebSocketService } from '../../services/websocket.service';
import { HttpClient } from '@angular/common/http';
import { ChatStateService } from '../../services/chat-state.service';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.scss']
})
export class ChatComponent implements OnInit, AfterViewInit {
  senderId!: string ;
  receiverId!: string;

  messages: Message[] = [];
  newMessage: string = '';

  private chatState = inject(ChatStateService);
  private route = inject(ActivatedRoute);
  private wsService = inject(WebSocketService);
  private http = inject(HttpClient);

  @ViewChild('chatMessages') chatMessagesRef!: ElementRef;

  ngOnInit(): void {
    // 取得 URL 上的接收者 ID
    this.route.paramMap.subscribe(params => {
      this.receiverId = params.get('userId') || '';
      // 取出快取訊息或設為空陣列
      this.messages = this.chatState.getMessages(this.receiverId);
      // 標記已讀
      this.markMessagesAsRead();

      this.scrollToBottom();
    });

    // 訂閱 WebSocket 訊息
    this.wsService.getMessages().subscribe(message => {
      if (!message) return;

      const isCurrent = message.senderId === this.receiverId || message.receiverId === this.receiverId;
      const isMine = message.senderId === this.senderId;

      if (isCurrent) {
        this.messages.push(message);
        this.chatState.setMessages(this.receiverId, [...this.messages]);

        if (!isMine) this.markMessagesAsRead();

        this.scrollToBottom();
      } else {
        // 未讀數累加
        const from = message.senderId;
        this.chatState.incrementUnread(from);
      }
    });
  }

  markMessagesAsRead(): void {
    this.http.post('/api/messages/read', {
      senderId: this.receiverId,
      receiverId: this.senderId,
    }).subscribe(() => {
      this.chatState.clearUnread(this.receiverId);
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
        senderId: this.senderId,
        receiverId: this.receiverId,
        content: this.newMessage,
        timestamp: new Date().toISOString(),
      };
      this.messages.push(message); // 顯示畫面
      this.chatState.setMessages(this.receiverId, [...this.messages]);
      this.wsService.sendMessage('/app/private', message); // 傳送到後端
      this.newMessage = '';
      this.scrollToBottom();
    }
  }

  ngAfterViewInit(): void {
    this.scrollToBottom();
  }

  ngOnDestroy(): void {
    // 之後可加入：清除訂閱、暫存未送出訊息等
  }
}
