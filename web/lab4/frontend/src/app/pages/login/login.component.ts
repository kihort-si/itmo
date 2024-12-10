import { Component, inject } from '@angular/core'
import { UserService } from '../../services/user.service'
import { ButtonModule } from 'primeng/button'
import { InputTextModule } from 'primeng/inputtext'
import { RouterLink } from '@angular/router'
import { RippleModule } from 'primeng/ripple'
import { FormsModule, NgForm } from '@angular/forms'
import { MessageService } from "primeng/api";

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ButtonModule,
    InputTextModule,
    RouterLink,
    RippleModule,
    FormsModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private userService: UserService = inject(UserService);
  private messageService: MessageService = inject(MessageService);

  private validationFailed!: boolean;

  onSubmit(form: NgForm): void {
    const {email, password} = form.value;

    this.validationFailed = false;
    if (email == null || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email))
      this.showError('Invalid email address')
    if (password == null || password.length < 8 || password.length > 32)
      this.showError('Password must be between 8 and 32 characters')
    if (this.validationFailed) return

    this.userService.login(email, password)
  }

  private showError(message: string): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Error',
      detail: message
    })
    this.validationFailed = true
  }
}
