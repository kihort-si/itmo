import {apiClient, extractApiError, GATEWAY_BASE_URL, LOGIN_URL, setSkipRedirect} from './config';
import axios from 'axios';

export type AccountRole =
    | 'CLIENT'
    | 'SALES_MANAGER'
    | 'CONSTRUCTOR'
    | 'CNC_OPERATOR'
    | 'WAREHOUSE_WORKER'
    | 'SUPPLY_MANAGER'
    | 'ADMIN';

export interface ClientRegistrationRequestDto {
    username: string;
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    phoneNumber: string;
}

export interface ClientRegistrationResponseDto {
    clientId: number;
    accountId: number;
    email: string;
    enabled: boolean;
}

export interface CurrentUserDto {
    username: string;
    role: AccountRole;
    employee?: {
        id: number;
        accountId: number;
        person: {
            id: number;
            firstName: string;
            lastName: string;
        };
    };
    client?: {
        id: number;
        accountId: number;
        email: string;
        phoneNumber: string;
        person: {
            id: number;
            firstName: string;
            lastName: string;
        };
    };
}

export interface UpdateProfileRequestDto {
    firstName: string;
    lastName: string;
    email: string;
    phoneNumber: string;
}

export interface ChangePasswordRequestDto {
    currentPassword: string;
    newPassword: string;
}

export const authService = {
    login(): never {
        const returnTo = window.location.href;
        window.location.href = `${LOGIN_URL}?returnTo=${encodeURIComponent(returnTo)}`;
        throw new Error('Redirecting to login');
    },

    async register(data: ClientRegistrationRequestDto): Promise<ClientRegistrationResponseDto> {
        try {
            if (!data.email || !data.password || !data.firstName || !data.lastName || !data.phoneNumber) {
                throw new Error('Все поля обязательны для заполнения');
            }
            const response = await apiClient.post<ClientRegistrationResponseDto>('/register', data);
            return response.data;
        } catch (error) {
            const apiError = extractApiError(error);
            if (axios.isAxiosError(error) && error.response) {
                apiError.status = error.response.status;
            }
            throw apiError;
        }
    },

    async logout(): Promise<void> {
        const form = document.createElement("form");
        form.method = "POST";
        form.action = `${GATEWAY_BASE_URL}/logout`;
        document.body.appendChild(form);
        form.submit();
    },

    hasToken(): boolean {
        if (typeof window === 'undefined') return false;
        const token = localStorage.getItem('accessToken');
        if (token) return true;
        return false;
    },

    async isAuthenticated(): Promise<boolean> {
        setSkipRedirect(true);
        try {
            await this.getCurrentUser();
            return true;
        } catch (e: any) {
            if (e?.status === 401) return false;
            return false;
        } finally {
            setSkipRedirect(false);
        }
    },

    async getCurrentUser(): Promise<CurrentUserDto> {
        try {
            const response = await apiClient.get<CurrentUserDto>('/me');
            return response.data;
        } catch (error) {
            throw extractApiError(error);
        }
    },

    async updateProfile(data: UpdateProfileRequestDto): Promise<void> {
        try {
            await apiClient.put('/me', data);
            await this.getCurrentUser();
        } catch (error) {
            throw extractApiError(error);
        }
    },

    async changePassword(data: ChangePasswordRequestDto): Promise<void> {
        try {
            await apiClient.post('/change-password', data);
        } catch (error) {
            throw extractApiError(error);
        }
    },

    async getRole(): Promise<AccountRole | null> {
        setSkipRedirect(true);
        try {
            const me = await this.getCurrentUser();
            return me.role ?? null;
        } catch {
            return null;
        } finally {
            setSkipRedirect(false);
        }
    },
};
