import { Component } from "@angular/core";
import { WebSocketService } from "../server/websocket.service";
import { CommonModule } from "@angular/common";
import { FormsModule } from '@angular/forms';


@Component({
    selector: 'app-chat',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './chat.component.html',
    styleUrls: ['./chat.component.css']
})
export class ChatComponent {
    messageInput = '';
    messages: string[] = [];

    constructor(private webSocketService: WebSocketService) {
        this.webSocketService.message$.subscribe((message) => {
            if (message) this.messages.push(message);
        });
    }

    sendMessage() {
        if (this.messageInput.trim()) {
            this.webSocketService.sendMessage('Alice', 'Bob', this.messageInput);
            this.messageInput = '';
        }
    }
}