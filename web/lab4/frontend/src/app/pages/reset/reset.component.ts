import { Component, inject } from "@angular/core";
import { ButtonModule } from "primeng/button";
import { FormsModule, NgForm } from "@angular/forms";
import { InputTextModule } from "primeng/inputtext";
import { RippleModule } from "primeng/ripple";
import { MessageService } from "primeng/api";
import { UserService } from "../../services/user.service";
import { NgIf } from "@angular/common";
import { RouterLink } from "@angular/router";

@Component({
    selector: 'app-reset',
    standalone: true,
    imports: [
        ButtonModule,
        FormsModule,
        InputTextModule,
        RippleModule,
        NgIf,
        RouterLink
    ],
    templateUrl: './reset.component.html',
    styleUrl: './reset.component.css'
})
export class ResetComponent {
    private userService: UserService = inject(UserService)
    private validationFailed!: boolean;
    private messageService: MessageService = inject(MessageService);
    protected isSubmitted: boolean = false;
    protected submittedEmail: string | null = null;

    onSubmit(form: NgForm): void {
        const {email} = form.value

        this.validationFailed = false
        if (email == null)
            this.showError('Email must be between 6 and 20 characters')
        if (this.validationFailed) return

        this.userService.reset(email);
        this.isSubmitted = true;
        this.submittedEmail = email;
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