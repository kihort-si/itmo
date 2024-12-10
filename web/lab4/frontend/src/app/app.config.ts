import { ApplicationConfig } from '@angular/core'
import { provideRouter, withHashLocation } from '@angular/router'
import { routes } from './app.routes'
import { provideHttpClient, withInterceptors } from '@angular/common/http'
import { authInterceptor } from './interceptors/auth.interceptor'
import { provideAnimations } from '@angular/platform-browser/animations'
import { MessageService } from 'primeng/api'
import { errorInterceptor } from './interceptors/error.interceptor'

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes, withHashLocation()), // keep withHashLocation() for production
    provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
    provideAnimations(),
    MessageService
  ]
}
