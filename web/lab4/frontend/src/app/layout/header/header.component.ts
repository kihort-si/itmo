import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ButtonModule } from "primeng/button";
import { UserService } from "../../services/user.service";

@Component({
  selector: 'app-header',
  standalone: true,
    imports: [
        CommonModule,
        ButtonModule
    ],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  protected userService: UserService = inject(UserService)
  exitButton: boolean = false;

  constructor(private router: Router) {
    this.router.events.subscribe((): void => {
      this.updateButtonVisibility();
    })
  }

  updateButtonVisibility(): void {
    const currentRoute: string = this.router.url;
    this.exitButton = currentRoute === '/main';
  }
}
