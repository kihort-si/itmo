import {API_URL} from "./api.ts";
import type {PersonResponseDto} from "./PersonService.ts";

export interface MovieResponseDto {
  id: number;
  name: string;
  coordinates: {
    x?: number;
    y: number;
  };
  creationDate: string;
  oscarsCount?: number;
  budget?: number;
  totalBoxOffice?: number;
  mpaaRating: string;
  director: PersonResponseDto;
  screenwriter?: PersonResponseDto;
  operator?: PersonResponseDto;
  length?: number;
  goldenPalmCount?: number;
  genre?: string;
}

export interface MovieRequestDto {
  name: string;
  coordinates?: {
    x?: number;
    y: number;
  };
  oscarsCount?: number;
  budget?: number;
  totalBoxOffice?: number;
  mpaaRating?: string;
  directorId?: number;
  screenwriterId?: number;
  operatorId?: number;
  length?: number;
  goldenPalmCount?: number;
  genre?: string;
}

class MovieService {
  private baseUrl = `${API_URL}/movies`;

  async getMovies(sortKey?: string, sortDirection?: 'asc' | 'desc', filters?: {[key: string]: string}): Promise<MovieResponseDto[]> {
    let url = this.baseUrl;
    const params = new URLSearchParams();

    if (sortKey && sortDirection) {
      params.append('sortBy', sortKey);
      params.append('sortDir', sortDirection);
    }

    if (filters) {
      for (const key in filters) {
        if (filters[key]) {
          params.append(`filter_${key}`, filters[key]);
            }
      }
    }

    if (params.toString()) {
      url += `?${params.toString()}`;
    }

    const response = await fetch(url);
    if (!response.ok) {
      throw new Error('Ошибка при получении фильмов: ' + response.status);
    }
    return response.json();
  }

  async getMovieById(id: number): Promise<MovieResponseDto> {
    const response = await fetch(`${this.baseUrl}/${id}`);
    if (!response.ok) {
      if (response.status === 404) {
        throw new Error(`Фильм с ID ${id} не найден`);
      }
      throw new Error(`Ошибка получения фильма: ${response.status}`);
    }
    return response.json();
  }

  async createMovie(movie: MovieRequestDto): Promise<MovieResponseDto> {
    const response = await fetch(this.baseUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(movie),
    });
    if (!response.ok) {
      throw new Error(`Ошибка создания фильма: ${response.status}`);
    }
    return response.json();
  }

  async updateMovie(id: number, movie: MovieRequestDto): Promise<MovieResponseDto> {
    const response = await fetch(`${this.baseUrl}/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(movie),
    });
    if (!response.ok) {
      if (response.status === 404) {
        throw new Error(`Фильм с ID ${id} не найден`);
      }
      throw new Error(`Ошибка обновления фильма: ${response.status}`);
    }
    return response.json();
  }

  async deleteMovie(id: number): Promise<boolean> {
    const response = await fetch(`${this.baseUrl}/${id}`, {
      method: 'DELETE',
    });
    if (!response.ok && response.status !== 404) {
      throw new Error(`Ошибка удаления фильма: ${response.status}`);
    }
    return response.status === 204;
  }

  async deleteOneByGenre(genre: string): Promise<{ message: string }> {
    const response = await fetch(`${this.baseUrl}/by-genre/${genre}`, {
      method: 'DELETE',
    });
    if (!response.ok) {
      if (response.status === 404) {
        throw new Error(`Фильм с жанром ${genre} не найден`);
      }
      throw new Error(`Ошибка удаления фильма: ${response.status}`);
    }
    return response.json();
  }

  async getGoldenPalmWinnersCount(): Promise<{ totalGoldenPalms: number }> {
    const response = await fetch(`${this.baseUrl}/count-golden-palm-winners`, {
      method: 'GET',
    });
    if (!response.ok) {
      throw new Error('Ошибка при получении количества Золотых Пальмовых ветвей: ' + response.status);
    }
    return response.json();
  }

  async getMoviesWithGoldenPalmCountLessThan(count: number): Promise<MovieResponseDto[]> {
    const response = await fetch(`${this.baseUrl}/golden-palm-count-less-than/${count}`, {
      method: 'GET',
    });
    if (!response.ok) {
      throw new Error('Ошибка при получении фильмов: ' + response.status);
    }
    return response.json();
  }

  async getScreenwritersWithoutOscars(): Promise<PersonResponseDto[]> {
    const response = await fetch(`${this.baseUrl}/screenwriters-without-oscars`, {
      method: 'GET',
    });
    if (!response.ok) {
      throw new Error('Ошибка при получении сценаристов: ' + response.status);
    }
    return response.json();
  }

  async redistributeOscars(fromGenre: string, toGenre: string): Promise<{ message: string }> {
    const response = await fetch(`${this.baseUrl}/redistribute-oscars?from=${fromGenre}&to=${toGenre}`, {
      method: 'POST',
    });
    if (!response.ok) {
      throw new Error(`Ошибка перераспределения: ${response.status}`);
    }
    return response.json();
  }
}

export default new MovieService();