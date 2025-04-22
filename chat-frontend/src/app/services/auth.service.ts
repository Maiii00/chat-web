import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, map, Observable, of } from 'rxjs';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly tokenKey = 'accessToken';
  private readonly refreshKey = 'refreshToken';

  private http = inject(HttpClient);
  private router = inject(Router);

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getUsername(): string | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = token.split('.')[1];
      
      const decoded = JSON.parse(atob(payload));
      console.log(decoded.sub);
      
      return decoded.sub ?? null;
    } catch (e) {
      console.error('Failed to decode JWT', e);
      return null;
    }
  }

  getUserId(): string | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = token.split('.')[1];
      
      const decoded = JSON.parse(atob(payload));
      console.log(decoded.id);
      
      return decoded.id ?? null;
    } catch (e) {
      console.error('Failed to decode JWT', e);
      return null;
    }
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  logout(): void {
    const token = this.getToken();

    // 如果 token 存在，通知後端讓它失效
    if (token) {
      this.http.post('/api/users/logout', {}, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }).subscribe({
        next: () => {
          console.log('後端登出成功');
          this.clearTokenAndRedirect();
        },
        error: err => {
          console.warn('後端登出失敗', err);
          this.clearTokenAndRedirect();
        }
      });
    } else {
      this.clearTokenAndRedirect();
    }
  }

  private clearTokenAndRedirect(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.refreshKey);
    this.router.navigate(['/login']);
  }

  refreshAccessToken(refreshToken: string): Observable<string | null> {
    console.log('發送 refresh 請求，token:', refreshToken);
    return this.http.post<{ accessToken: string }>('/api/users/refresh', { refreshToken }).pipe(
      map(res => res.accessToken),
      // 如果沒有拿到新的 accessToken，就回傳 null
      catchError((err) => {
        console.error('refresh 失敗', err);
        return of(null);
      })
    );
  }
}
