import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';
import { ToastService } from '../../shared/toast/toast.service';

@Component({
  selector: 'app-two-factor-verify',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './two-factor-verify.html',
  styleUrl: './two-factor-verify.scss'
})
export class TwoFactorVerifyComponent implements OnInit, OnDestroy {
  verifyForm: FormGroup;
  isLoading = false;
  remainingTime = 300;
  private timer: any;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private toastService: ToastService
  ) {
    this.verifyForm = this.fb.group({
      code: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]
    });
  }

  ngOnInit() {
    if (!this.authService.is2FARequired()) {
      this.router.navigate(['/auth']);
      return;
    }

    this.startTimer();
  }

  ngOnDestroy() {
    if (this.timer) {
      clearInterval(this.timer);
    }
  }

  startTimer() {
    this.timer = setInterval(() => {
      this.remainingTime--;
      if (this.remainingTime <= 0) {
        clearInterval(this.timer);
        this.authService.clearTemporaryToken();
        this.toastService.error('Verification code expired. Please login again.');
        this.router.navigate(['/auth']);
      }
    }, 1000);
  }

  onSubmit() {
    if (this.verifyForm.invalid) {
      return;
    }

    const { code } = this.verifyForm.value;
    this.isLoading = true;

    this.authService.verify2fa(code).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.toastService.success('Two-factor authentication successful!');
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 401) {
          this.toastService.error('Invalid verification code. Please try again.');
        } else if (err.status === 400) {
          this.toastService.error(err.error?.error || 'Invalid request');
        } else {
          this.toastService.error('Verification failed. Please try again.');
        }
        console.error(err);
      }
    });
  }

  onBackToLogin() {
    this.authService.clearTemporaryToken();
    this.router.navigate(['/auth']);
  }

  get minutes(): number {
    return Math.floor(this.remainingTime / 60);
  }

  get seconds(): number {
    return this.remainingTime % 60;
  }
}
