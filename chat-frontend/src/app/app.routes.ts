import { Routes } from '@angular/router';
import { authGuard } from './services/auth.guard';

export const routes: Routes = [
    {
        path: 'login',
        loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent),
    },
    {
        path: 'register',
        loadComponent: () => import('./pages/register/register.component').then(m => m.RegisterComponent),
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
        redirectTo: 'login',
    },
      
];
