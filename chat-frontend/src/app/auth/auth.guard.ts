import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = (route, state) => {
  const token = localStorage.getItem('accessToken');
  const router = inject(Router);

  if (token) return true;

  router.navigate(['/login']); // 導回登入頁
  return false;
};
