import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { MovieSearchRequest } from '../../models/movie.model';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './search-bar.component.html',
  styleUrl: './search-bar.component.scss'
})
export class SearchBarComponent {
  @Output() search = new EventEmitter<MovieSearchRequest>();

  searchRequest: MovieSearchRequest = {};

  onSearchChange() {
    this.search.emit(this.searchRequest);
  }

  onSearch() {
    this.search.emit(this.searchRequest);
  }

  onClear() {
    this.searchRequest = {};
    this.search.emit(this.searchRequest);
  }
}
