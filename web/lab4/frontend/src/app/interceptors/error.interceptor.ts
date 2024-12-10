import { HttpInterceptorFn } from '@angular/common/http'
import { catchError, Observable, throwError } from 'rxjs'
import { inject } from '@angular/core'
import { MessageService } from 'primeng/api'

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
    const messageService: MessageService = inject(MessageService);

    return next(req).pipe(
        catchError((error: any): Observable<never> => {
            let errorMessages: string[] = [];

            if (error?.error) {
                if (Array.isArray(error.error.errors)) {
                    errorMessages = error.error.errors;
                } else if (typeof error.error === 'string') {
                    errorMessages = [error.error];
                } else if (error.error.message) {
                    errorMessages = [error.error.message];
                }
            }

            errorMessages = errorMessages.filter(
                (msg: string): boolean => msg !== 'Access token expired'
            );


            if (errorMessages.length > 0) {
                messageService.addAll(
                    errorMessages.map((msg: string) => ({
                        severity: 'error',
                        summary: 'Error',
                        detail: msg,
                    }))
                );
            } else if (errorMessages.length === 0 && error.error !== 'Access token expired') {
                messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: 'Unknown error',
                });
            }

            return throwError((): any => error);
        })
    );
};
