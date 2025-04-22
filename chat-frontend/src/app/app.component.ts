import { Component, inject } from '@angular/core';
import { WebSocketService } from './services/websocket.service';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from './components/navbar/navbar.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, NavbarComponent, SidebarComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'chat';
  messages: { content: string } [] = [];
  showSidebar = true;
  showLayout = true;

  private router = inject(Router);

  constructor(private wsService: WebSocketService) {
    this.wsService.getMessages().subscribe((message) => {
      this.messages.push(message);
    });

    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        const hiddenRoutes = ['/login', '/register'];
        this.showLayout = !hiddenRoutes.includes(event.urlAfterRedirects);
        this.showSidebar = !hiddenRoutes.includes(event.urlAfterRedirects);
      }
    });
  }

  sendMessage(): void {
    this.wsService.sendMessage('/app/private', { content: 'Hello WebSocket' });
  }

  toggleSidebar() {
    this.showSidebar = !this.showSidebar;
  }
}
