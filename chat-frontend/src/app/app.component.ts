import { Component } from '@angular/core';
//import { RouterOutlet } from '@angular/router';
import { ChatComponent } from './chat/chat.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [ChatComponent],
  // templateUrl: './app.component.html',
  // styleUrl: './app.component.css'

  template: `
    <h1>CHAT</h1>
    <app-chat></app-chat>
  `
})
export class AppComponent {
  title = 'chat';
}
