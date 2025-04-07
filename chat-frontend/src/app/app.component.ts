import { Component } from '@angular/core';
import { WebSocketService } from './services/websocket.service';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `<router-outlet>`,
})
export class AppComponent {
  title = 'chat';
  messages: { content: string } [] = [];

  constructor(private wsService: WebSocketService) {
    this.wsService.getMessages().subscribe((message) => {
      this.messages.push(message);
    });
  }

  sendMessage(): void {
    this.wsService.sendMessage('/app/private', { content: 'Hello WebSocket' });
  }
}
