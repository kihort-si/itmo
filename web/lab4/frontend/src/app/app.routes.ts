import { CanActivateFn, Router, Routes } from '@angular/router'
import { MainComponent } from "./pages/main/main.component"
import { LoginComponent } from "./pages/login/login.component"
import { RegisterComponent } from "./pages/register/register.component"
import { ResetComponent } from "./pages/reset/reset.component"
import { inject } from '@angular/core'
import { UserService } from './services/user.service'
import { ChangePasswordComponent } from "./pages/change-password/change-password";


const authGuard: CanActivateFn = (route, state): boolean => {
  const router: Router = inject(Router);
  const userService: UserService = inject(UserService);
  if (userService.isLoggedIn) return true;
  if (state.url.startsWith('/reset-password')) return true;
  router.navigate(['login']);
  return false;
}

export const routes: Routes = [
  {path: '', component: LoginComponent, pathMatch: 'full'},
  {path: 'main', component: MainComponent, canActivate: [authGuard]},
  {path: 'register', component: RegisterComponent},
  {path: 'reset', component: ResetComponent},
  {path: 'change-password', component: ChangePasswordComponent},
  {path: '**', redirectTo: ''}
]
