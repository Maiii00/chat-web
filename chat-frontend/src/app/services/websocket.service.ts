import { Injectable } from '@angular/core';
import { Client, Message } from '@stomp/stompjs';
import { BehaviorSubject, Observable } from 'rxjs';
import SockJS from 'sockjs-client';
import { AuthService } from './auth.service';
import { environment } from '../../enviroments/enviroment.prod';

@Injectable({
  providedIn: 'root',
})
export class WebSocketService {
  private stompClient: Client | null = null; // STOMP 客戶端實例，初始為 null
  private messagesSubject = new BehaviorSubject<Message | null>(null); // 用來存儲訊息的行為主題 (BehaviorSubject)，它會保存最近的訊息並發佈到訂閱者

  constructor(private auth: AuthService) {
    this.connect();
  }

  private connect(): void {
    const userId = this.auth.getUserId();

    this.stompClient = new Client({
        webSocketFactory: () => new SockJS(environment.wsUrl), // 使用 SockJS 建立 WebSocket 連線
        reconnectDelay: 5000, // 斷線後 5 秒自動重連
        connectHeaders: {
          userId: userId ?? '', // 必須要傳給後端，WebSocketUserInterceptor 才能設定 Principal
        },
        debug: (str) => console.log(str), // 開啟 debug 訊息
    });

    this.stompClient.onConnect = (frame) => {
        console.log('STOMP WebSocket 連線成功', frame);

        // 訂閱私聊訊息通道，並在收到訊息時將其加入消息隊列
        this.stompClient!.subscribe('/user/queue/messages', (message: Message) => {
          // 更新 messagesSubject，發佈新的訊息
          const parsed: Message = JSON.parse(message.body);
          this.messagesSubject.next(parsed);
        });
    };

    this.stompClient.onStompError = (frame) => {
        console.error('STOMP 錯誤', frame);
    };
  
    this.stompClient.activate(); // 啟動 STOMP 連線
  }

  // 發送訊息到指定的目的地
  sendMessage(destination: string, message: any): void {
    // 檢查 STOMP 客戶端是否已經連線，並確保能夠發送訊息
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.publish({ destination, body: JSON.stringify(message) });
    } else {
      console.error('STOMP WebSocket 未連線，訊息無法發送');
    }
  }

  // 返回一個 Observable，允許訂閱訊息更新
  getMessages(): Observable<any> {
    return this.messagesSubject.asObservable();
  }

  // 當服務被銷毀時，清理 STOMP 客戶端，關閉 WebSocket 連線
  ngOnDestroy(): void {
    if (this.stompClient) {
      this.stompClient.deactivate();
    }
  }
}
