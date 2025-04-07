import { Routes } from '@angular/router';
import { ChatComponent } from './pages/chat/chat.component';
import { LoginComponent } from './pages/login/login.component';
import { ChatListComponent } from './pages/chat-list/chat-list.component';
import { authGuard } from './auth/auth.guard';

export const routes: Routes = [
    {
        path: 'login',
        loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent),
    },
    {
        path: 'chat',
        canActivate: [authGuard],
        loadComponent: () => import('./pages/chat-list/chat-list.component').then(m => m.ChatListComponent),
    },
    {
        path: 'chat/:userId',
        canActivate: [authGuard],
        loadComponent: () => import('./pages/chat/chat.component').then(m => m.ChatComponent),
    },
    {
        path: '',
        redirectTo: 'chat',
        pathMatch: 'full',
    },
    {
        path: '**',
        redirectTo: 'chat',
    },
];
