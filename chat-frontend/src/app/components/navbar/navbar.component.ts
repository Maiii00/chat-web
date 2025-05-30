import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Component, EventEmitter, inject, Output } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { environment } from '../../../environments/environment.prod';

interface SearchResult {
  id: string;
  username: string;
}

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent {
  @Output() toggleSidebar = new EventEmitter<void>();

  private router = inject(Router);
  private http = inject(HttpClient);
  private auth = inject(AuthService);

  searchText = '';
  searchResults: SearchResult[] = [];

  onLogout() {
    this.auth.logout();
  }

  onSearch() {
    if (this.searchText.trim().length === 0) {
      this.searchResults = [];
      return;
    }

    this.http.get<SearchResult[]>(`${environment.apiBaseUrl}/users/search?keyword=${this.searchText}`).subscribe({
      next: users => this.searchResults = users,
      error: () => this.searchResults = []
    });
  }

  openChat(user: SearchResult) {
    this.router.navigate(['/chat', user.id]);
    this.searchText = '';
    this.searchResults = [];
  }
}
