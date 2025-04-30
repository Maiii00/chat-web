import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { environment } from '../../../environments/environment.prod';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  username = '';
  password = '';
  confirmPassword = '';

  private http = inject(HttpClient);
  private router = inject(Router);

  register() {
    if (this.password !== this.confirmPassword) {
      alert('Passwords do not match');
      return;
    }

    this.http
      .post(`${environment.apiBaseUrl}/users/register`, {
        username: this.username,
        password: this.password,
      })
      .subscribe({
        next: () => {
          alert('註冊成功');
          this.router.navigate(['/login'])
        },
        error: (err) => {
          const message = typeof err.error === 'string'
            ? err.error 
            : err.error?.message || '未知錯誤'
          alert('註冊失敗: ' + message);
        },
      });
  }
}
