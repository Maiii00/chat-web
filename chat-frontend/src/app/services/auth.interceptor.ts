import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';
import { HttpErrorResponse, HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { switchMap, catchError, throwError, of } from 'rxjs';
import { environment } from '../../environments/environment.prod';

export const authInterceptor: HttpInterceptorFn = (req: HttpRequest<any>, next: HttpHandlerFn) => {
  const authService = inject(AuthService);

  if (req.url.includes(`${environment.apiBaseUrl}/users/refresh`)) {
    return next(req);
  }
  
  const accessToken = localStorage.getItem('accessToken');

  const authReq = accessToken
    ? req.clone({ setHeaders: { Authorization: `Bearer ${accessToken}` } })
    : req;

  return next(authReq).pipe(
    catchError((err: HttpErrorResponse) => {
      if ((err.status === 401 || err.status === 403) && localStorage.getItem('refreshToken')) {
        const refreshToken = localStorage.getItem('refreshToken')!;
        console.warn('🔄 access token 過期，準備刷新');

        return authService.refreshAccessToken(refreshToken).pipe(
          switchMap((newAccessToken) => {
            if (!newAccessToken) {
              console.error('❌ 無法取得新 accessToken，登出');
              authService.logout();
              return throwError(() => err);
            }

            console.log('🔁 retry request with new accessToken:', newAccessToken);
            localStorage.setItem('accessToken', newAccessToken);

            const retryReq = req.clone({
              setHeaders: { Authorization: `Bearer ${newAccessToken}` }
            });

            return next(retryReq);
          }),
          catchError(() => {
            console.error('❌ refresh 失敗，登出');
            authService.logout();
            return throwError(() => err);
          })
        );
      }

      return throwError(() => err);
    })
  );
};
