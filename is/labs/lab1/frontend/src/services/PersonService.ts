import {API_URL} from "./api.ts";

export interface PersonResponseDto {
  id: number;
  name: string;
  location: {
    x: number;
    y?: number;
    z?: number;
    name: string;
  };
  passportID: string;
  eyeColor?: string;
  hairColor?: string;
  nationality: string;
}

export interface PersonRequestDto {
  name: string;
  location?: {
    x: number;
    y?: number;
    z?: number;
    name: string;
  };
  passportID?: string;
  eyeColor?: string;
  hairColor?: string
  nationality: string;
}

class PersonService {
  private baseUrl = `${API_URL}/people`;

  async getPeople(sortKey?: string, sortDirection?: 'asc' | 'desc', filters?: {[key: string]: string}): Promise<PersonResponseDto[]> {
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
      throw new Error('Ошибка при получении людей: ' + response.status);
    }
    return response.json();
  }

  async getPersonById(id: number): Promise<PersonResponseDto> {
    const response = await fetch(`${this.baseUrl}/${id}`);
    if (!response.ok) {
      if (response.status === 404) {
        throw new Error(`Человек с ID ${id} не найден`);
      }
      throw new Error(`Ошибка получения человека: ${response.status}`);
    }
    return response.json();
  }

  async createPerson(Person: PersonRequestDto): Promise<PersonResponseDto> {
    const response = await fetch(this.baseUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(Person),
    });
    if (!response.ok) {
      throw new Error(`Ошибка создания человека: ${response.status}`);
    }
    return response.json();
  }

  async updatePerson(id: number, person: PersonRequestDto): Promise<PersonResponseDto> {
    const response = await fetch(`${this.baseUrl}/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(person),
    });

    if (!response.ok) {
      if (response.status === 404) {
        throw new Error(`Человек с ID ${id} не найден`);
      }
      throw new Error(`Ошибка обновления: ${response.status}`);
    }

    return await response.json();
  }

  async deletePerson(id: number): Promise<boolean> {
    const response = await fetch(`${this.baseUrl}/${id}`, {
      method: 'DELETE',
    });
    if (!response.ok && response.status !== 404) {
      throw new Error(`Ошибка удаления человека: ${response.status}`);
    }
    return response.status === 204;
  }
}

export default new PersonService();