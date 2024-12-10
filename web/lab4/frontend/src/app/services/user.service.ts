import { inject, Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { API_URL } from '../constants'
import { Router } from '@angular/router'
import { Observable, Subscription, tap } from "rxjs";

interface Token {
    accessToken: string,
    refreshToken: string
}

@Injectable({
    providedIn: 'root'
})
export class UserService {
    private readonly baseUrl = `${API_URL}/users`
    private http: HttpClient = inject(HttpClient)
    private router: Router = inject(Router)
    isRefreshing: boolean = false;


    get email(): string | null {
        return localStorage.getItem('email')
    }

    set email(email: string | null | undefined) {
        if (email == null) localStorage.removeItem('email')
        else localStorage.setItem('email', email)
    }

    get isLoggedIn(): boolean {
        return this.authToken != null
    }

    get authToken(): string | null {
        return localStorage.getItem('token')
    }

    set authToken(token: string | null | undefined) {
        if (token == null) localStorage.removeItem('token')
        else localStorage.setItem('token', token)
    }

    get refreshToken(): string | null {
        return localStorage.getItem('refreshToken');
    }

    set refreshToken(token: string | null) {
        if (token == null) {
            localStorage.removeItem('refreshToken');
        } else {
            localStorage.setItem('refreshToken', token);
        }
    }

    private auth(email: string, token: string, refreshToken: string): void {
        this.authToken = token
        this.refreshToken = refreshToken;
        this.email = email
        this.router.navigate(['main'])
    }

    login(email: string, password: string): Subscription {
        return this.http.post<Token>(`${this.baseUrl}/login`, {email, password}).subscribe((data) =>
            this.auth(email, data.accessToken, data.refreshToken)
        );
    }

    register(email: string, password: string): Subscription {
        return this.http.post<Token>(`${this.baseUrl}/register`, {email, password}).subscribe((data) =>
            this.auth(email, data.accessToken, data.refreshToken)
        );
    }

    updateAuthTokenByRefreshToken(refreshToken: string): Observable<any> {
        return this.http.post(`${this.baseUrl}/refresh`, {refreshToken}).pipe(
            tap((response: any): void => {
                this.authToken = response.token;
            })
        );
    }

    reset(email: string): void {
        this.http.post(`${this.baseUrl}/reset`, {email}).subscribe({
            next: (): void => {
                console.log(`Password reset request sent to ${email}`);
            },
            error: (err: any): void => {
                console.error('Error while sending password reset request:', err);
            },
        });
    }

    changePassword(password: string, resetToken: string): void {
        this.http.post(`${this.baseUrl}/change`, {password, resetToken}).subscribe();
    }

    checkToken(token: string): Observable<boolean> {
        return this.http.post<boolean>(`${this.baseUrl}/check-token`, {token});
    }

    logout(): void {
        this.authToken = null
        this.email = undefined
        this.router.navigate(['login'])
    }
}
