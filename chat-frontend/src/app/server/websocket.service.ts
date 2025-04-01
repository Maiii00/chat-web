import { Injectable } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { BehaviorSubject } from 'rxjs';
import SockJS from 'sockjs-client';

@Injectable({
    providedIn: 'root'
})
export class WebSocketService {
    private stompClient!: Client;
    private messageSubject = new BehaviorSubject<string | null>(null);
    public message$ = this.messageSubject.asObservable();

    constructor() {
        this.connect();
    }

    private connect() {
        this.stompClient = new Client({
            webSocketFactory: () => new SockJS("http://localhost:8080/ws"),
            debug: (str) =>console.log(str),
            reconnectDelay: 5000,
            onConnect: () => {
                console.log('STOMP 連接成功');
                this.subscribeToMessages();
            },
            onStompError: (frame) => {
                console.error('STOMP 錯誤:', frame);
            }
        });
        this.stompClient.activate();
    }

    private subscribeToMessages() {
        this.stompClient.subscribe('/topic/messages', (message) => {
            try {
                const receivedMessage = JSON.parse(message.body);
                this.messageSubject.next(receivedMessage.content);
            } catch (error) {
                console.error('無法解析訊息:', error);
            }
        });
    }

    public sendMessage(sender: string, receiver: string, content: string) {
        const message = { sender, receiver, content };
        this.stompClient.publish({ destination: 'app/chat', body: JSON.stringify(message) });
    }
}