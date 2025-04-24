import { Component, inject } from '@angular/core';
import { WebSocketService } from './services/websocket.service';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from './components/navbar/navbar.component';
import { AuthService } from './services/auth.service';
import { ChatListComponent } from "./pages/chat-list/chat-list.component";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, NavbarComponent, ChatListComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'chat';
  messages: { content: string } [] = [];
  showLayout = true;

  private router = inject(Router);
  private authService = inject(AuthService);

  constructor(private wsService: WebSocketService) {
    this.wsService.getMessages().subscribe((message) => {
      this.messages.push(message);
    });

    this.router.events.subscribe(event => {
      const url = this.router.url;
      this.showLayout = !(url.includes('/login') || url.includes('/register'));
    });
  }

  ngOnInit() {
    const refreshToken = localStorage.getItem('refreshToken');
    if (refreshToken) {
      const payload = JSON.parse(atob(refreshToken.split('.')[1]));
      const exp = payload.exp * 1000;
      const now = Date.now();

      if (exp < now) {
        console.warn('Refresh token 已過期，自動登出');
        this.authService.logout();
      }
    }
  }

  sendMessage(): void {
    this.wsService.sendMessage('/app/private', { content: 'Hello WebSocket' });
  }
}
