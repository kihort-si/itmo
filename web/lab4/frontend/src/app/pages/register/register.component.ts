import { Component, inject } from '@angular/core'
import { UserService } from '../../services/user.service'
import { FormsModule, NgForm } from '@angular/forms'
import { PasswordModule } from 'primeng/password'
import { ButtonModule } from 'primeng/button'
import { MessageService } from 'primeng/api'
import { InputTextModule } from 'primeng/inputtext'
import { RouterLink } from '@angular/router'
import { RippleModule } from 'primeng/ripple'

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    InputTextModule,
    PasswordModule,
    FormsModule,
    ButtonModule,
    RouterLink,
    RippleModule
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  private userService: UserService = inject(UserService)
  private messageService: MessageService = inject(MessageService)

  private validationFailed!: boolean

  onSubmit(form: NgForm): void {
    const {email, password, passwordConfirmation} = form.value

    this.validationFailed = false
    if (email == null || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email))
      this.showError('Invalid email address')
    if (password != passwordConfirmation)
      this.showError('Passwords don\'t match')
    if (password == null || password.length < 8 || password.length > 32)
      this.showError('Password must be between 8 and 32 characters')
    if (this.validationFailed) return

    this.userService.register(email, password)
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
