import { Component, inject, OnInit } from "@angular/core";
import { FormsModule, NgForm, ReactiveFormsModule } from "@angular/forms";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { UserService } from "../../services/user.service";
import { InputTextModule } from "primeng/inputtext";
import { ButtonModule } from "primeng/button";
import { RippleModule } from "primeng/ripple";
import { NgIf } from "@angular/common";

@Component({
    selector: "app-change-password",
    standalone: true,
    templateUrl: "./change-password.html",
    styleUrls: ["./change-password.css"],
    imports: [
        ReactiveFormsModule,
        FormsModule,
        InputTextModule,
        ButtonModule,
        RippleModule,
        NgIf,
        RouterLink
    ]
})
export class ChangePasswordComponent implements OnInit {
    private userService: UserService = inject(UserService)
    private token: string | null = null;
    protected isSubmitted: boolean = false;

    constructor(private route: ActivatedRoute, private router: Router) {
    }

    ngOnInit(): void {
        this.token = this.route.snapshot.queryParamMap.get("token");

        if (this.token) {
            this.userService.checkToken(this.token).subscribe({
                next: (isValid: boolean): void => {
                    if (!isValid) {
                        this.router.navigate(['/']);
                    }
                },
                error: (): void => {
                    this.router.navigate(['/']);
                }
            });
        } else {
            this.router.navigate(['/']);
        }
    }

    onSubmit(form: NgForm): void {
        const {password} = form.value;

        if (this.token != null)
        this.userService.changePassword(password, this.token);
        this.isSubmitted = true;
    }
}