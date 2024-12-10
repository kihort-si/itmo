import { HttpEvent, HttpInterceptorFn, HttpRequest } from '@angular/common/http'
import { inject } from '@angular/core'
import { UserService } from '../services/user.service'
import { catchError, Observable, switchMap, throwError } from "rxjs";
import { Router } from "@angular/router";

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const userService: UserService = inject(UserService);
    const router: Router = inject(Router);

    const accessToken: string | null = userService.authToken;

    const clonedRequest: HttpRequest<unknown> = req.clone({
        setHeaders: accessToken
            ? { Authorization: `Bearer ${accessToken}` }
            : {},
    });

    return next(clonedRequest).pipe(
        catchError((error: any) => {
            if (error.status === 401 && error.error === 'Access token expired') {
                const refreshToken: string | null = userService.refreshToken;

                if (!refreshToken) {
                    userService.logout();
                    router.navigate(['login']);
                    return throwError((): Error => new Error('Refresh token missing'));
                }

                if (userService.isRefreshing) {
                    return throwError((): Error => new Error('Token refresh in progress'));
                }

                userService.isRefreshing = true;
                return userService.updateAuthTokenByRefreshToken(refreshToken).pipe(
                    switchMap((response: any): Observable<HttpEvent<unknown>> => {
                        userService.isRefreshing = false;
                        const newAccessToken: any = response.accessToken;
                        userService.authToken = newAccessToken;

                        return next(
                            req.clone({
                                setHeaders: { Authorization: `Bearer ${newAccessToken}` },
                            })
                        );
                    }),
                    catchError((): Observable<never> => {
                        userService.isRefreshing = false;
                        userService.logout();
                        router.navigate(['login']);
                        return throwError((): Error => new Error('Failed to refresh token'));
                    })
                );
            }

            return throwError((): any => error);
        })
    );
};

