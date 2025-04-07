import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls:[ './login.component.scss']
})
export class LoginComponent {
  username = '';
  password = '';
  private http = inject(HttpClient);
  private router = inject(Router);

  constructor() {
    const token = localStorage.getItem('accessToken');
    if (token) {
      this.router.navigate(['/']);
    }
  }

  login() {
    this.http.post('/api/users/login', {
      username: this.username,
      password: this.password,
    }).subscribe((res: any) => {
      localStorage.setItem('accessToken', res.accessToken);
      this.router.navigate(['/']);
    });
  }
}
